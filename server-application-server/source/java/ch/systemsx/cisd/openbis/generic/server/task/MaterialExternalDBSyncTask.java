/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator.SupportedDatePattern;

/**
 * Task which feeds a reporting database with recently added/changed Materials.
 * 
 * @author Franz-Josef Elmer
 */
public class MaterialExternalDBSyncTask implements IMaintenanceTask
{
    @Private
    static final String READ_TIMESTAMP_SQL_KEY = "read-timestamp-sql";

    @Private
    static final String UPDATE_TIMESTAMP_SQL_KEY = "update-timestamp-sql";

    @Private
    static final String INSERT_TIMESTAMP_SQL_KEY = "insert-timestamp-sql";

    @Private
    static final String MAPPING_FILE_KEY = "mapping-file";

    private static final class Column
    {
        private final String name;

        private final int index;

        private DataTypeCode dataTypeCode;

        Column(String name, int index)
        {
            this.name = name;
            this.index = index;
        }
    }

    private enum IndexingSchema
    {
        INSERT(0, 2), UPDATE(-1, 1);

        private final int codeIndex;

        private final int offset;

        private IndexingSchema(int codeIndex, int offset)
        {
            this.codeIndex = codeIndex;
            this.offset = offset;
        }

        public int getCodeIndex(int size)
        {
            return 1 + (size + 1 + codeIndex) % (size + 1);
        }

        public int getPropertyIndexOffset()
        {
            return offset;
        }
    }

    @Private
    static final class MappingInfo
    {
        private final int SELECT_CHUNK_SIZE = 500;

        private final String materialTypeCode;

        private final String tableName;

        private final String codeColumnName;

        private final Map<String, Column> propertyMapping = new LinkedHashMap<String, Column>();

        MappingInfo(String materialTypeCode, String tableName, String codeColumnName)
        {
            this.materialTypeCode = materialTypeCode;
            this.tableName = tableName;
            this.codeColumnName = codeColumnName;
        }

        String getMaterialTypeCode()
        {
            return materialTypeCode;
        }

        String getTableName()
        {
            return tableName;
        }

        String getCodeColumnName()
        {
            return codeColumnName;
        }

        boolean hasProperties()
        {
            return propertyMapping.isEmpty() == false;
        }

        void addPropertyMapping(String propertyTypeCode, String propertyColumnName)
        {
            Column column = new Column(propertyColumnName, propertyMapping.size());
            propertyMapping.put(propertyTypeCode, column);
        }

        void injectDataTypeCodes(Map<String, DataTypeCode> columns,
                Map<String, PropertyType> propertyTypes)
        {
            DataTypeCode codeColumnType = columns.remove(codeColumnName);
            if (codeColumnType == null)
            {
                throw new EnvironmentFailureException("Missing column '" + codeColumnName
                        + "' in table '" + tableName + "' of report database.");
            }
            if (codeColumnType.equals(DataTypeCode.VARCHAR) == false)
            {
                throw new EnvironmentFailureException("Column '" + codeColumnName + "' of table '"
                        + tableName + "' is not of type VARCHAR.");
            }
            for (Entry<String, Column> entry : propertyMapping.entrySet())
            {
                String propertyTypeCode = entry.getKey();
                PropertyType propertyType = propertyTypes.get(propertyTypeCode);
                if (propertyType == null)
                {
                    throw new ConfigurationFailureException(
                            "Mapping file refers to an unknown property type: " + propertyTypeCode);
                }
                Column column = entry.getValue();
                DataTypeCode dataTypeCode = columns.get(column.name);
                if (dataTypeCode == null)
                {
                    throw new EnvironmentFailureException("Missing column '" + column.name
                            + "' in table '" + tableName + "' of report database.");
                }
                DataTypeCode correspondingType =
                        getCorrespondingType(propertyType.getDataType().getCode());
                if (dataTypeCode.equals(correspondingType) == false)
                {
                    throw new EnvironmentFailureException("Column '" + column.name + "' in table '"
                            + tableName
                            + "' of report database should be of a type which corresponds to "
                            + correspondingType + ".");
                }
                column.dataTypeCode = dataTypeCode;
            }
        }

        private DataTypeCode getCorrespondingType(DataTypeCode code)
        {
            switch (code)
            {
                case INTEGER:
                case REAL:
                case TIMESTAMP:
                    return code;
                default:
                    return DataTypeCode.VARCHAR;
            }
        }

        Map<String, Map<String, Object>> groupByMaterials(List<Map<String, Object>> rows)
        {
            HashMap<String, Map<String, Object>> result =
                    new HashMap<String, Map<String, Object>>();
            for (Map<String, Object> map : rows)
            {
                String materialCode = map.remove(codeColumnName).toString();
                result.put(materialCode, map);
            }
            return result;
        }

        List<String> createSelectStatement(List<Material> materials)
        {
            final List<String> sqls = new ArrayList<String>();
            int startIndex = 0;
            int endIndex = Math.min(SELECT_CHUNK_SIZE, materials.size());
            while (startIndex != endIndex)
            {
                sqls.add(createPartialSelectStatement(materials, startIndex, endIndex));
                startIndex = endIndex;
                endIndex = Math.min(endIndex + SELECT_CHUNK_SIZE, materials.size());
            }
            return sqls;
        }

        String createPartialSelectStatement(List<Material> materials, int startIndex, int endIndex)
        {
            final StringBuilder builder = new StringBuilder("select * from ");
            builder.append(tableName).append(" where ");
            builder.append(codeColumnName).append(" in ");
            String delim = "(";
            for (int i = startIndex; i < endIndex; ++i)
            {
                final Material material = materials.get(i);
                builder.append(delim).append('\'').append(material.getCode()).append('\'');
                delim = ", ";
            }
            builder.append(")");
            return builder.toString();
        }

        String createInsertStatement()
        {
            StringBuilder builder = new StringBuilder("insert into ").append(tableName);
            builder.append(" (").append(codeColumnName);
            for (Column column : propertyMapping.values())
            {
                builder.append(", ").append(column.name);
            }
            builder.append(") values(?");
            for (int i = 0; i < propertyMapping.size(); i++)
            {
                builder.append(", ?");
            }
            builder.append(")");
            return builder.toString();
        }

        String createUpdateStatement()
        {
            StringBuilder builder = new StringBuilder("update ").append(tableName);
            String delim = " set ";
            for (Column column : propertyMapping.values())
            {
                builder.append(delim).append(column.name).append("=?");
                delim = ", ";
            }
            builder.append(" where ").append(codeColumnName).append("=?");
            return builder.toString();
        }

        BatchPreparedStatementSetter createSetter(final List<Material> materials,
                final IndexingSchema indexing)
        {
            return new BatchPreparedStatementSetter()
                {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException
                    {
                        Material material = materials.get(index);
                        List<IEntityProperty> properties = material.getProperties();
                        ps.setObject(indexing.getCodeIndex(propertyMapping.size()),
                                material.getCode());
                        int propertyIndexOffset = indexing.getPropertyIndexOffset();
                        for (int i = 0; i < propertyMapping.size(); i++)
                        {
                            ps.setObject(i + propertyIndexOffset, null);
                        }
                        for (IEntityProperty property : properties)
                        {
                            PropertyType propertyType = property.getPropertyType();
                            String code = propertyType.getCode();
                            Column column = propertyMapping.get(code);
                            if (column != null)
                            {
                                String value = getValue(property);
                                Serializable typedValue =
                                        DataTypeUtils.convertValueTo(column.dataTypeCode, value);
                                if (typedValue instanceof Date)
                                {
                                    ps.setObject(column.index + propertyIndexOffset, typedValue,
                                            Types.TIMESTAMP);
                                } else
                                {
                                    ps.setObject(column.index + propertyIndexOffset, typedValue);
                                }
                            }
                        }
                    }

                    @Override
                    public int getBatchSize()
                    {
                        return materials.size();
                    }
                };
        }

        private String getValue(IEntityProperty property)
        {
            String value;
            switch (property.getPropertyType().getDataType().getCode())
            {
                case CONTROLLEDVOCABULARY:
                    value = property.getVocabularyTerm().getCodeOrLabel();
                    break;
                case MATERIAL:
                    value = property.getMaterial().getCode();
                    break;
                default:
                    value = property.getStringValue();
            }
            return value;
        }

    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaterialExternalDBSyncTask.class);

    private final ICommonServerForInternalUse server;

    private final ITimeProvider timeProvider;

    private Map<String, MappingInfo> mapping;

    private SimpleDatabaseConfigurationContext dbConfigurationContext;

    private JdbcTemplate jdbcTemplate;

    private String readTimestampSql;

    private String insertTimestampSql;

    private String updateTimestampSql;

    public MaterialExternalDBSyncTask()
    {
        this(CommonServiceProvider.getCommonServer(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public MaterialExternalDBSyncTask(ICommonServerForInternalUse server, ITimeProvider timeProvider)
    {
        this.server = server;
        this.timeProvider = timeProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        dbConfigurationContext = new SimpleDatabaseConfigurationContext(properties);
        readTimestampSql = PropertyUtils.getMandatoryProperty(properties, READ_TIMESTAMP_SQL_KEY);
        updateTimestampSql =
                PropertyUtils.getMandatoryProperty(properties, UPDATE_TIMESTAMP_SQL_KEY);
        insertTimestampSql = properties.getProperty(INSERT_TIMESTAMP_SQL_KEY, updateTimestampSql);
        String mappingFileName = PropertyUtils.getMandatoryProperty(properties, MAPPING_FILE_KEY);
        mapping = readMappingFile(mappingFileName);
        Map<String, Map<String, PropertyType>> materialTypes = getMaterialTypes();
        Map<String, Map<String, DataTypeCode>> metaData = retrieveDatabaseMetaData();
        for (MappingInfo mappingInfo : mapping.values())
        {
            String materialTypeCode = mappingInfo.getMaterialTypeCode();
            Map<String, PropertyType> propertyTypes = materialTypes.get(materialTypeCode);
            if (propertyTypes == null)
            {
                throw new ConfigurationFailureException(
                        "Mapping file refers to an unknown material type: " + materialTypeCode);
            }
            String tableName = mappingInfo.getTableName();
            Map<String, DataTypeCode> columns = metaData.get(tableName);
            if (columns == null)
            {
                throw new EnvironmentFailureException("Missing table '" + tableName
                        + "' in report database.");
            }
            mappingInfo.injectDataTypeCodes(columns, propertyTypes);
        }
        jdbcTemplate = new JdbcTemplate(dbConfigurationContext.getDataSource());
        checkTimestampReadingWriting();
    }

    @Override
    public void execute()
    {
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            return;
        }
        Date newTimestamp = new Date(timeProvider.getTimeInMilliseconds());
        operationLog.info("Start reporting added or changed materials to the report database.");
        Map<String, List<Material>> materialsByType =
                getRecentlyAddedOrChangedMaterials(contextOrNull.getSessionToken());
        Set<Entry<String, MappingInfo>> entrySet = mapping.entrySet();
        for (Entry<String, MappingInfo> entry : entrySet)
        {
            String materialTypeCode = entry.getKey();
            List<Material> materials = materialsByType.get(materialTypeCode);
            if (materials != null)
            {
                addOrUpdate(entry.getValue(), materials);
            }
        }
        writeTimestamp(newTimestamp);
        operationLog.info("Reporting finished.");
    }

    private Map<String, Map<String, PropertyType>> getMaterialTypes()
    {
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            throw new EnvironmentFailureException("Can not authenticate as system.");
        }
        List<MaterialType> materialTypes =
                server.listMaterialTypes(contextOrNull.getSessionToken());
        Map<String, Map<String, PropertyType>> result =
                new HashMap<String, Map<String, PropertyType>>();
        for (MaterialType materialType : materialTypes)
        {
            List<MaterialTypePropertyType> assignedPropertyTypes =
                    materialType.getAssignedPropertyTypes();
            Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();
            for (MaterialTypePropertyType materialTypePropertyType : assignedPropertyTypes)
            {
                PropertyType propertyType = materialTypePropertyType.getPropertyType();
                propertyTypes.put(propertyType.getCode(), propertyType);
            }
            result.put(materialType.getCode(), propertyTypes);
        }
        return result;
    }

    private Map<String, Map<String, DataTypeCode>> retrieveDatabaseMetaData()
    {
        Collection<MappingInfo> values = mapping.values();
        final Set<String> tableNames = new HashSet<String>();
        for (MappingInfo mappingInfo : values)
        {
            tableNames.add(mappingInfo.getTableName());
        }
        try
        {
            final Map<String, Map<String, DataTypeCode>> map =
                    new HashMap<String, Map<String, DataTypeCode>>();
            JdbcUtils.extractDatabaseMetaData(dbConfigurationContext.getDataSource(),
                    new DatabaseMetaDataCallback()
                        {
                            @Override
                            public Object processMetaData(DatabaseMetaData metaData)
                                    throws SQLException, MetaDataAccessException
                            {
                                ResultSet rs = metaData.getColumns(null, null, null, null);
                                while (rs.next())
                                {
                                    String tableName = rs.getString("TABLE_NAME").toLowerCase();
                                    if (tableNames.contains(tableName))
                                    {
                                        Map<String, DataTypeCode> columns = map.get(tableName);
                                        if (columns == null)
                                        {
                                            columns = new TreeMap<String, DataTypeCode>();
                                            map.put(tableName, columns);
                                        }
                                        String columnName =
                                                rs.getString("COLUMN_NAME").toLowerCase();
                                        int sqlTypeCode = rs.getInt("DATA_TYPE");
                                        DataTypeCode dataTypeCode =
                                                DataTypeUtils.getDataTypeCode(sqlTypeCode);
                                        columns.put(columnName, dataTypeCode);
                                    }
                                }
                                rs.close();
                                return null;
                            }
                        });
            return map;
        } catch (MetaDataAccessException ex)
        {
            throw new ConfigurationFailureException("Couldn't retrieve meta data of database.", ex);
        }
    }

    private void addOrUpdate(MappingInfo mappingInfo, final List<Material> materials)
    {
        final List<String> sql = mappingInfo.createSelectStatement(materials);
        List<Map<String, Object>> rows = retrieveRowsToBeUpdated(sql);
        Map<String, Map<String, Object>> reportedMaterials = mappingInfo.groupByMaterials(rows);
        List<Material> newMaterials = new ArrayList<Material>();
        List<Material> updateMaterials = new ArrayList<Material>();
        for (Material material : materials)
        {
            Map<String, Object> reportedMaterial = reportedMaterials.get(material.getCode());
            if (reportedMaterial != null)
            {
                updateMaterials.add(material);
            } else
            {
                newMaterials.add(material);
            }
        }
        if (updateMaterials.isEmpty() == false && mappingInfo.hasProperties())
        {
            String updateStatement = mappingInfo.createUpdateStatement();
            jdbcTemplate.batchUpdate(updateStatement,
                    mappingInfo.createSetter(updateMaterials, IndexingSchema.UPDATE));
            operationLog.info(updateMaterials.size() + " materials of type "
                    + mappingInfo.getMaterialTypeCode() + " have been updated in report database.");
        }
        if (newMaterials.isEmpty() == false)
        {
            String insertStatement = mappingInfo.createInsertStatement();
            jdbcTemplate.batchUpdate(insertStatement,
                    mappingInfo.createSetter(newMaterials, IndexingSchema.INSERT));
            operationLog.info(newMaterials.size() + " materials of type "
                    + mappingInfo.getMaterialTypeCode()
                    + " have been inserted into report database.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> retrieveRowsToBeUpdated(List<String> sqls)
    {
        final List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String sql : sqls)
        {
            rows.addAll(jdbcTemplate.query(sql, new ColumnMapRowMapper()
                {
                    @Override
                    protected String getColumnKey(String columnName)
                    {
                        return columnName.toLowerCase();
                    }
                }));
        }
        return rows;
    }

    private Map<String, List<Material>> getRecentlyAddedOrChangedMaterials(String sessionToken)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        DetailedSearchCriterion criterion =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(MaterialAttributeSearchFieldKind.MODIFICATION_DATE),
                        CompareType.MORE_THAN_OR_EQUAL, readTimestamp());
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        List<Material> materials = server.searchForMaterials(sessionToken, criteria);
        Map<String, List<Material>> result = new TreeMap<String, List<Material>>();
        for (Material material : materials)
        {
            String typeCode = material.getMaterialType().getCode();
            List<Material> list = result.get(typeCode);
            if (list == null)
            {
                list = new ArrayList<Material>();
                result.put(typeCode, list);
            }
            list.add(material);
        }
        return result;
    }

    private void checkTimestampReadingWriting()
    {
        Date timestamp;
        try
        {
            timestamp = tryToReadTimestamp();
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException(
                    "Couldn't get timestamp from report database. Property '"
                            + READ_TIMESTAMP_SQL_KEY + "' could be invalid.", ex);
        }
        try
        {
            writeTimestamp(timestamp == null ? new Date(0) : timestamp);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException(
                    "Couldn't save timestamp to report database. Property '"
                            + INSERT_TIMESTAMP_SQL_KEY + "' or '" + UPDATE_TIMESTAMP_SQL_KEY
                            + "' could be invalid.", ex);
        }
        try
        {
            writeTimestamp(timestamp == null ? new Date(0) : timestamp);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException(
                    "Couldn't save timestamp to report database. Property '"
                            + UPDATE_TIMESTAMP_SQL_KEY + "' could be invalid.", ex);
        }
    }

    private String readTimestamp()
    {
        Date timestamp = tryToReadTimestamp();
        return timestamp == null ? "1970-01-01" : DateFormatUtils.format(timestamp,
                SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());
    }

    private Date tryToReadTimestamp()
    {
        @SuppressWarnings("unchecked")
        List<Date> list = jdbcTemplate.queryForList(readTimestampSql, Date.class);
        return list.isEmpty() ? null : list.get(0);
    }

    private void writeTimestamp(Date newTimestamp)
    {
        String sql = tryToReadTimestamp() == null ? insertTimestampSql : updateTimestampSql;
        jdbcTemplate.update(sql, new Object[]
        { newTimestamp }, new int[]
        { Types.TIMESTAMP });
    }

    @Private
    static Map<String, MappingInfo> readMappingFile(String mappingFileName)
    {
        Map<String, MaterialExternalDBSyncTask.MappingInfo> map =
                new LinkedHashMap<String, MaterialExternalDBSyncTask.MappingInfo>();
        List<String> lines = FileUtilities.loadToStringList(new File(mappingFileName));
        MappingInfo currentMappingInfo = null;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            ExecptionFactory factory = new ExecptionFactory(mappingFileName, line, i);
            if (line.startsWith("#") || line.trim().length() == 0)
            {
                continue;
            }
            if (line.startsWith("["))
            {
                if (line.endsWith("]") == false)
                {
                    throw factory.exception("Missing ']'");
                }
                String[] splittedLine =
                        splitAndCheck(line.substring(0, line.length() - 1).substring(1), ":", 2,
                                factory);
                String materialTypeCode =
                        trimAndCheck(splittedLine[0], factory, "material type code");
                splittedLine = splitAndCheck(splittedLine[1], ",", 2, factory);
                String tableName = trimAndCheck(splittedLine[0], factory, "table name");
                String codeColumnName = trimAndCheck(splittedLine[1], factory, "code column name");
                currentMappingInfo =
                        new MappingInfo(materialTypeCode, tableName.toLowerCase(),
                                codeColumnName.toLowerCase());
                map.put(materialTypeCode, currentMappingInfo);
            } else if (currentMappingInfo != null)
            {
                String[] splittedLine = splitAndCheck(line, ":", 2, factory);
                String propertyTypeCode =
                        trimAndCheck(splittedLine[0], factory, "property type code");
                currentMappingInfo.addPropertyMapping(propertyTypeCode, splittedLine[1].trim()
                        .toLowerCase());
            } else
            {
                throw factory.exception("Missing first material type table definition of form "
                        + "'[<material type tode>: <table name>, <code column name>]'");
            }
        }
        return map;
    }

    private static String[] splitAndCheck(String string, String delimiter, int numberOfItems,
            ExecptionFactory factory)
    {
        String[] splittedString = string.split(delimiter);
        if (splittedString.length < numberOfItems)
        {
            throw factory.exception(numberOfItems + " items separated by '" + delimiter
                    + "' expected.");
        }
        return splittedString;
    }

    private static String trimAndCheck(String string, ExecptionFactory factory, String name)
    {
        String trimmedString = string.trim();
        if (trimmedString.length() == 0)
        {
            throw factory.exception("Missing " + name + ".");
        }
        return trimmedString;
    }

    private static final class ExecptionFactory
    {
        private final String mappingFileName;

        private final String line;

        private final int lineIndex;

        ExecptionFactory(String mappingFileName, String line, int lineIndex)
        {
            this.mappingFileName = mappingFileName;
            this.line = line;
            this.lineIndex = lineIndex;
        }

        ConfigurationFailureException exception(String message)
        {
            return new ConfigurationFailureException("Error in mapping file '" + mappingFileName
                    + "' at line " + (lineIndex + 1) + " '" + line + "': " + message);
        }
    }

    @Private
    void closeDatabaseConnections()
    {
        if (dbConfigurationContext != null)
        {
            dbConfigurationContext.closeConnections();
        }
    }

}
