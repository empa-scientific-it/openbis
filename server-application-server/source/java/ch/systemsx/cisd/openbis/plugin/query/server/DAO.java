/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.JdbcUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.common.string.Template.IToken;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
public class DAO extends JdbcDaoSupport implements IDAO
{

    private static final int FETCH_SIZE = 1000;

    private static final int MAX_ROWS = 100 * FETCH_SIZE; // 100.000

    private static final int QUERY_TIMEOUT_SECS = 5 * 60; // 5 minutes

    private static final String ENTITY_COLUMN_NAME_SUFFIX = "_KEY";

    private static final Map<String, Integer> SQL_TYPE_CODE_TO_TYPE_MAP =
            new HashMap<String, Integer>();

    private static final Map<Integer, String> SQL_TYPE_TO_TYPE_CODE_MAP =
            new HashMap<Integer, String>();

    static
    {
        SQL_TYPE_CODE_TO_TYPE_MAP.put("ARRAY", Types.ARRAY);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("BIGINT", Types.BIGINT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("BINARY", Types.BINARY);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("BIT", Types.BIT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("BLOB", Types.BLOB);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("BOOLEAN", Types.BOOLEAN);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("CHAR", Types.CHAR);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("CLOB", Types.CLOB);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("DATALINK", Types.DATALINK);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("DATE", Types.DATE);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("DECIMAL", Types.DECIMAL);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("DISTINCT", Types.DISTINCT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("DOUBLE", Types.DOUBLE);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("FLOAT", Types.FLOAT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("INTEGER", Types.INTEGER);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("JAVA_OBJECT", Types.JAVA_OBJECT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("LONGNVARCHAR", Types.LONGNVARCHAR);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("LONGVARBINARY", Types.LONGVARBINARY);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("LONGVARCHAR", Types.LONGVARCHAR);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("NCHAR", Types.NCHAR);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("NCLOB", Types.NCLOB);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("NULL", Types.NULL);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("NUMERIC", Types.NUMERIC);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("NVARCHAR", Types.NVARCHAR);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("OTHER", Types.OTHER);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("REAL", Types.REAL);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("REF", Types.REF);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("ROWID", Types.ROWID);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("SMALLINT", Types.SMALLINT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("SQLXML", Types.SQLXML);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("STRUCT", Types.STRUCT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("TIME", Types.TIME);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("TIMESTAMP", Types.TIMESTAMP);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("TINYINT", Types.TINYINT);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("VARBINARY", Types.VARBINARY);
        SQL_TYPE_CODE_TO_TYPE_MAP.put("VARCHAR", Types.VARCHAR);
        for (Map.Entry<String, Integer> entry : SQL_TYPE_CODE_TO_TYPE_MAP.entrySet())
        {
            SQL_TYPE_TO_TYPE_CODE_MAP.put(entry.getValue(), entry.getKey());
        }
        // Convenience mapping
        SQL_TYPE_CODE_TO_TYPE_MAP.put("STRING", Types.VARCHAR);
    }

    private static Map<String, EntityKind> entityKindByColumnName =
            new HashMap<String, EntityKind>();

    static
    {
        for (EntityKind entityKind : EntityKind.values())
        {
            entityKindByColumnName.put(entityKind.name() + ENTITY_COLUMN_NAME_SUFFIX, entityKind);
        }
    }

    private static EntityKind tryGetEntityKind(String columnName)
    {
        return entityKindByColumnName.get(columnName.toUpperCase());
    }

    public DAO(DataSource dataSource)
    {
        setDataSource(dataSource);
        afterPropertiesSet();
    }

    public static void checkQuery(String sqlQuery)
    {
        if (sqlQuery.toLowerCase().trim().startsWith("select") == false)
        {
            throw new UserFailureException("Sorry, only select statements are allowed.");
        }
        int indexOfSemicolon = sqlQuery.trim().indexOf(';');
        if (indexOfSemicolon >= 0 && indexOfSemicolon < sqlQuery.trim().length() - 1)
        {
            throw new UserFailureException("Sorry, only one query statement is allowed: "
                    + "A ';' somewhere in the middle has been found.");
        }
    }

    @Override
    public TableModel query(String sqlQuery, QueryParameterBindings bindingsOrNull)
    {
        checkQuery(sqlQuery);

        PreparedStatementCallback callback = new PreparedStatementCallback()
            {
                @Override
                public Object doInPreparedStatement(PreparedStatement ps) throws SQLException,
                        DataAccessException
                {
                    ResultSet resultSet = null;
                    try
                    {
                        resultSet = ps.executeQuery();
                        ResultSetMetaData metaData = ps.getMetaData();
                        List<TableModelColumnHeader> headers =
                                new ArrayList<TableModelColumnHeader>();
                        int columnCount = metaData.getColumnCount();
                        Counters<String> counters = new Counters<String>();
                        for (int i = 1; i <= columnCount; i++)
                        {
                            String columnName = JdbcUtils.lookupColumnName(metaData, i);
                            String id = columnName;
                            EntityKind entityKindOrNull = tryGetEntityKind(columnName);
                            if (entityKindOrNull != null)
                            {
                                columnName = entityKindOrNull.getDescription();
                                id = entityKindOrNull.name(); // id shouldn't contain spaces
                            }
                            int count = counters.count(id);
                            if (count > 1)
                            {
                                id += count;
                            }
                            TableModelColumnHeader header =
                                    new TableModelColumnHeader(columnName, id, i - 1);
                            header.setDataType(DataTypeUtils.getDataTypeCode(metaData
                                    .getColumnType(i)));
                            header.setEntityKind(entityKindOrNull);
                            headers.add(header);
                        }
                        List<TableModelRow> rows = new ArrayList<TableModelRow>();
                        int rowCounter = 0;
                        String messageOrNull = null;
                        while (resultSet.next())
                        {
                            if (++rowCounter > MAX_ROWS)
                            {
                                messageOrNull =
                                        String.format("Result size is limited to a maximum of %d.",
                                                MAX_ROWS);
                                break;
                            }
                            rows.add(createRow(resultSet, columnCount));
                        }
                        return new TableModel(headers, rows, messageOrNull);
                    } finally
                    {
                        JdbcUtils.closeResultSet(resultSet);
                    }
                }

                private TableModelRow createRow(ResultSet resultSet, int columnCount)
                        throws SQLException
                {
                    List<ISerializableComparable> cells = new ArrayList<ISerializableComparable>();
                    for (int i = 1; i <= columnCount; i++)
                    {
                        Object value = JdbcUtils.getResultSetValue(resultSet, i);
                        if (value instanceof Integer || value instanceof Long)
                        {
                            cells.add(new IntegerTableCell(((Number) value).longValue()));
                        } else if (value instanceof Number)
                        {
                            Number number = (Number) value;
                            cells.add(new DoubleTableCell(number.doubleValue()));
                        } else if (value instanceof Date)
                        {
                            Date date = (Date) value;
                            cells.add(new DateTableCell(date));

                        } else
                        {
                            String string = value == null ? "" : value.toString();
                            cells.add(new StringTableCell(string));
                        }
                    }
                    TableModelRow row = new TableModelRow(cells);
                    return row;
                }

            };

        final JdbcTemplate template = getJdbcTemplate();
        template.setFetchSize(FETCH_SIZE);
        template.setMaxRows(MAX_ROWS + 1); // fetch one more row than allowed to detect excess
        template.setQueryTimeout(QUERY_TIMEOUT_SECS);
        final PreparedStatementCreator resolvedQuery =
                createSQLPreparedStatement(sqlQuery, bindingsOrNull);
        return (TableModel) template.execute(resolvedQuery, callback);
    }

    /**
     * A class for figuring out the parameter type.
     * <p>
     * Note that some JDBC drivers (like the one from Oracle) have {@link ParameterMetaData#getParameterType(int)} not implemented and thus requires
     * setting it explicitly in the variable metadata.
     */
    private static class ParameterTypeProvider
    {
        private static final String TYPE_PREFIX = "type=";

        private static final String META_DATA_SEPARATOR = "::";

        private final Template template;

        private final Map<Integer, Entry<String, String>> indexMap;

        private final ParameterMetaData paramMD;

        ParameterTypeProvider(Template template, Map<Integer, Entry<String, String>> indexMap,
                ParameterMetaData paramMD)
        {
            this.template = template;
            this.indexMap = indexMap;
            this.paramMD = paramMD;
        }

        int getParameterCount() throws SQLException
        {
            return paramMD.getParameterCount();
        }

        int getParameterType(int param) throws SQLException
        {
            final Entry<String, String> entry = indexMap.get(param - 1);
            final String overrideTypeCode =
                    tryGetOverrideTypeCode(entry);
            if (overrideTypeCode != null)
            {
                final Integer paramType = SQL_TYPE_CODE_TO_TYPE_MAP.get(overrideTypeCode);
                if (paramType == null)
                {
                    throw new SQLDataException("Invalid SQL type code '" + overrideTypeCode + "'");
                }
                return paramType;
            } else
            {
                return paramMD.getParameterType(param);
            }
        }

        private String tryGetOverrideTypeCode(final Entry<String, String> entry)
        {
            final String md = template.tryGetMetadata(entry.getKey());
            if (md == null)
            {
                return null;
            }
            final String[] splitMD = md.split(META_DATA_SEPARATOR);
            for (String field : splitMD)
            {
                if (field.startsWith(TYPE_PREFIX))
                {
                    return field.substring(TYPE_PREFIX.length()).toUpperCase();
                }
            }
            return null;
        }

        String getParameterTypeName(int param) throws SQLException
        {
            return SQL_TYPE_TO_TYPE_CODE_MAP.get(getParameterType(param));
        }
    }

    private static PreparedStatementCreator createSQLPreparedStatement(final String sqlQuery,
            final QueryParameterBindings bindingsOrNull)
    {
        return new PreparedStatementCreator()
            {
                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException
                {
                    final Map<Integer, Entry<String, String>> indexMap =
                            new HashMap<Integer, Entry<String, String>>();
                    final Template template = new Template(sqlQuery);
                    final Set<String> arrayVariableNames = new HashSet<String>();
                    // Support for legacy PostgreSQL array specifications
                    addToSet(template.replaceBrackets("'{", "}'", "", ""), arrayVariableNames);
                    // Support for legacy string parameters: replace '${var}' with ${var}.
                    template.replaceBrackets("'", "'", "", "");
                    // Support for simplified array specification in PostgreSQL
                    addToSet(template.replaceBrackets("{", "}", "", ""), arrayVariableNames);
                    for (String arrayVariableName : arrayVariableNames)
                    {
                        final IToken token = template.tryGetRightNeighbor(arrayVariableName);
                        if (token != null && token.tryGetValue() != null
                                && token.tryGetValue().startsWith("::text[]") == false)
                        {
                            token.setSubString(0, 0, "::text[]", "");
                        }
                    }
                    if (bindingsOrNull != null)
                    {
                        for (Entry<String, String> entry : bindingsOrNull.getBindings().entrySet())
                        {
                            template.bind(entry.getKey(), "?");
                            final int index = template.tryGetIndex(entry.getKey());
                            if (index >= 0)
                            {
                                indexMap.put(index, entry);
                            }
                        }
                    }
                    final PreparedStatement psm = con.prepareStatement(template.createText());
                    final ParameterTypeProvider ptp =
                            new ParameterTypeProvider(template, indexMap,
                                    psm.getParameterMetaData());
                    for (int i = 1; i <= ptp.getParameterCount(); ++i)
                    {
                        final Entry<String, String> entry = indexMap.get(i - 1);
                        if (entry == null)
                        {
                            throw new SQLDataException("No variable found for parameter " + i);
                        }
                        final String strValue = entry.getValue();
                        try
                        {
                            switch (ptp.getParameterType(i))
                            {
                                case Types.BIT:
                                case Types.BOOLEAN:
                                    psm.setBoolean(i, Boolean.parseBoolean(strValue));
                                    break;
                                case Types.TINYINT:
                                    psm.setByte(i, Byte.parseByte(strValue));
                                    break;
                                case Types.SMALLINT:
                                    psm.setShort(i, Short.parseShort(strValue));
                                    break;
                                case Types.INTEGER:
                                    psm.setInt(i, Integer.parseInt(strValue));
                                    break;
                                case Types.BIGINT:
                                    psm.setLong(i, Long.parseLong(strValue));
                                    break;
                                case Types.FLOAT:
                                case Types.REAL:
                                    psm.setFloat(i, Float.parseFloat(strValue));
                                    break;
                                case Types.DOUBLE:
                                    psm.setDouble(i, Double.parseDouble(strValue));
                                    break;
                                case Types.NUMERIC:
                                case Types.DECIMAL:
                                    psm.setBigDecimal(i, new BigDecimal(strValue));
                                    break;
                                case Types.CHAR:
                                case Types.VARCHAR:
                                case Types.LONGVARCHAR:
                                case Types.NCHAR:
                                case Types.NVARCHAR:
                                case Types.LONGNVARCHAR:
                                    psm.setString(i, strValue);
                                    break;
                                case Types.ARRAY:
                                    psm.setString(
                                            i,
                                            arrayVariableNames.contains(entry.getKey()) ? "{"
                                                    + strValue + "}" : strValue);
                                    break;
                                case Types.TIME:
                                    psm.setTime(i, Time.valueOf(strValue));
                                    break;
                                case Types.DATE:
                                    psm.setDate(i, java.sql.Date.valueOf(strValue));
                                    break;
                                case Types.TIMESTAMP:
                                    psm.setTimestamp(i, Timestamp.valueOf(strValue));
                                    break;
                                default:
                                    throw new SQLDataException("Unsupported SQL type "
                                            + ptp.getParameterTypeName(i) + "("
                                            + ptp.getParameterType(i) + ") for variable "
                                            + entry.getKey());
                            }
                        } catch (RuntimeException ex)
                        {
                            throw new SQLDataException("Invalid value '" + entry.getValue()
                                    + "' for variable " + entry.getKey(), ex);
                        }
                    }
                    return psm;
                }

                private void addToSet(List<IToken> tokens, Set<String> nameSet)
                {
                    for (IToken token : tokens)
                    {
                        nameSet.add(token.tryGetName());
                    }
                }
            };
    }
}
