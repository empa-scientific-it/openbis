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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import static ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext.DRIVER_KEY;
import static ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext.PASSWORD_KEY;
import static ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext.URL_KEY;
import static ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext.USER_KEY;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.dbmigration.MonitoringDataSource;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceWithDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.util.IDataSourceFactory;

/**
 * Data source provider based on configuration per Data Store Server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServerBasedDataSourceProvider implements IDataSourceProvider,
        IDataStoreDataSourceManager, InitializingBean
{
    public static final String ROOT_KEY = "dss-based-data-source-provider";

    public static final String DATA_STORE_SERVERS_KEY = "data-store-servers";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStoreServerBasedDataSourceProvider.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private Map<String, DataSource> dataSourcesByKey = new HashMap<String, DataSource>();

    private Map<Mapping, DataSource> dataSourcesByMapping = new HashMap<Mapping, DataSource>();

    private Map<String, Properties> configParametersByKeys = new HashMap<String, Properties>();

    private final IDAOFactory daoFactory;

    private final MappingManager mappingManager;

    private final IDataSourceFactory dataSourceFactory;

    public DataStoreServerBasedDataSourceProvider(IDAOFactory daoFactory,
            String dssDataSourceMappingFilePath)
    {
        this(daoFactory, dssDataSourceMappingFilePath, new IDataSourceFactory()
            {
                @Override
                public DataSourceWithDefinition create(Properties props)
                {
                    return new DataSourceWithDefinition(new SimpleDatabaseConfigurationContext(
                            props).getDataSource(), null);
                }
            });
    }

    DataStoreServerBasedDataSourceProvider(IDAOFactory daoFactory,
            String dssDataSourceMappingFilePath, IDataSourceFactory dataSourceFactory)
    {
        this.daoFactory = daoFactory;
        this.dataSourceFactory = dataSourceFactory;
        mappingManager = new MappingManager(dssDataSourceMappingFilePath);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        init(ExtendedProperties.getSubset(configurer.getResolvedProps(), ROOT_KEY + ".", true));
    }

    void init(Properties props)
    {
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(props, DATA_STORE_SERVERS_KEY,
                        false);
        for (SectionProperties sectionProperties : sectionsProperties)
        {
            String key = sectionProperties.getKey().toUpperCase();
            configParametersByKeys.put(key, sectionProperties.getProperties());
        }
        mappingManager.init(configParametersByKeys);
        Map<String, List<DataSourceDefinition>> originalDataSourceDefinitions =
                new HashMap<String, List<DataSourceDefinition>>();
        List<DataStorePE> dataStores = daoFactory.getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            String code = dataStore.getCode();
            List<DataSourceDefinition> definitions =
                    DataSourceDefinition.listFromString(dataStore
                            .getSerializedDataSourceDefinitions());
            originalDataSourceDefinitions.put(code, definitions);
        }
        for (Entry<String, List<DataSourceDefinition>> entry : originalDataSourceDefinitions
                .entrySet())
        {
            String dataStoreCode = entry.getKey();
            mappingManager.handle(dataStoreCode, entry.getValue());
            clearDataSourceCaches(dataStoreCode);
        }
    }

    @Override
    public synchronized DataSource getDataSourceByDataStoreServerCode(String dssCode,
            String technology)
    {
        String normalizedDssCode = dssCode.toUpperCase();
        String moduleCode = technology.toUpperCase();
        String key = normalizedDssCode + "[" + moduleCode + "]";
        DataSource dataSource = dataSourcesByKey.get(key);
        if (dataSource == null)
        {
            dataSource = createDataSource(normalizedDssCode, moduleCode);
            dataSourcesByKey.put(key, dataSource);
        }
        return dataSource;
    }

    private DataSource createDataSource(String dataStoreCode, String moduleCode)
    {
        Mapping mapping = mappingManager.getMapping(dataStoreCode, moduleCode);
        DataSource dataSource = dataSourcesByMapping.get(mapping);
        if (dataSource == null)
        {
            dataSource = createDataSource(mapping);
            dataSourcesByMapping.put(mapping, dataSource);
        }
        return dataSource;
    }

    private DataSource createDataSource(Mapping mapping)
    {
        Properties properties = configParametersByKeys.get(mapping.configKey);
        if (properties == null)
        {
            throw new ConfigurationFailureException("No data source configured for '"
                    + mapping.configKey + "'.");
        }
        properties = ExtendedProperties.createWith(properties);
        DataSourceDefinition definitionOrNull = mapping.definitionOrNull;
        Properties props = createMergedProperties(properties, definitionOrNull);
        return dataSourceFactory.create(props).getDataSource();
    }

    private Properties createMergedProperties(Properties properties,
            DataSourceDefinition definitionOrNull)
    {
        Properties props = ExtendedProperties.createWith(properties);
        if (definitionOrNull != null)
        {
            String driverClassName = definitionOrNull.getDriverClassName();
            setProperty(props, DRIVER_KEY, driverClassName);
            setProperty(props, USER_KEY, definitionOrNull.getUsername());
            setProperty(props, PASSWORD_KEY, definitionOrNull.getPassword());
            String sid = definitionOrNull.getSid();
            if (properties.getProperty(URL_KEY) == null && driverClassName != null && sid != null)
            {
                DatabaseEngine engine = DatabaseEngine.getEngineForDriverClass(driverClassName);
                String url = engine.getURL(definitionOrNull.getHostPart(), sid);
                props.setProperty(URL_KEY, url);
            }
        }
        return props;
    }

    private void setProperty(Properties properties, String key, String valueOrNull)
    {
        if (properties.getProperty(key) == null && valueOrNull != null)
        {
            properties.setProperty(key, valueOrNull);
        }
    }

    @Override
    public synchronized void handle(String dataStoreCode,
            List<DataSourceDefinition> dataSourceDefinitions)
    {
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();
        DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new EnvironmentFailureException("Unknown data store: " + dataStoreCode);
        }
        assertMandatoryAttributesDefined(dataSourceDefinitions);
        dataStore.setSerializedDataSourceDefinitions(DataSourceDefinition
                .toString(dataSourceDefinitions));
        dataStoreDAO.createOrUpdateDataStore(dataStore);
        mappingManager.handle(dataStoreCode, dataSourceDefinitions);
        clearDataSourceCaches(dataStoreCode);
    }

    private void clearDataSourceCaches(String dataStoreCode)
    {
        LinkedList<Entry<Mapping, DataSource>> entries =
                new LinkedList<Entry<Mapping, DataSource>>(dataSourcesByMapping.entrySet());
        dataSourcesByMapping.clear();
        for (Entry<Mapping, DataSource> entry : entries)
        {
            Mapping mapping = entry.getKey();
            DataSource dataSource = entry.getValue();
            if (dataStoreCode.equals(mapping.dataStoreCode))
            {
                if (dataSource instanceof MonitoringDataSource)
                {
                    try
                    {
                        ((MonitoringDataSource) dataSource).close();
                    } catch (SQLException ex)
                    {
                        DataSourceDefinition definition = mapping.definitionOrNull;
                        if (definition == null)
                        {
                            operationLog.warn("Couldn't close data source for " + mapping.configKey
                                    + ".");
                        } else
                        {
                            operationLog.warn("Couldn't close data source for database "
                                    + definition.getSid() + " on " + dataStoreCode + ".");
                        }
                    }
                }
            } else
            {
                dataSourcesByMapping.put(mapping, dataSource);
            }
        }
        dataSourcesByKey.clear();
    }

    private void assertMandatoryAttributesDefined(List<DataSourceDefinition> definitions)
    {
        StringBuilder errors = new StringBuilder();
        for (DataSourceDefinition definition : definitions)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            if (StringUtils.isBlank(definition.getCode()))
            {
                builder.append("code");
            }
            if (StringUtils.isBlank(definition.getSid()))
            {
                builder.append("sid");
            }
            String driverClassName = definition.getDriverClassName();
            if (driverClassName == null
                    || DatabaseEngine.hasEngineForDriverClass(driverClassName) == false)
            {
                builder.append("driverClassName");
            }
            String error = builder.toString();
            if (error.length() > 0)
            {
                errors.append("\n").append(error).append("\n[").append(definition).append("]");
            }
        }
        if (errors.length() > 0)
        {
            throw new EnvironmentFailureException(
                    "Some data source definitions have missing or wrong mandatory attributes: "
                            + errors);
        }
    }

    private static final class MappingManager
    {
        private final String mappingFilePath;

        private List<MappingEntry> mappingEntries;

        private Map<String, Properties> configs;

        private Map<String, Map<String, DataSourceDefinition>> dataSourceDefinitionsByCodes =
                new HashMap<String, Map<String, DataSourceDefinition>>();

        public MappingManager(String mappingFilePath)
        {
            this.mappingFilePath = mappingFilePath;
        }

        public void init(Map<String, Properties> propertiesByKey)
        {
            mappingEntries = parse(mappingFilePath);
            configs = propertiesByKey;
        }

        private static List<MappingEntry> parse(String mappingFilePath)
        {
            List<MappingEntry> mappingEntries = new ArrayList<MappingEntry>();
            File file = new File(mappingFilePath);
            if (file.isFile())
            {
                List<String> lines = FileUtilities.loadToStringList(file);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    if (StringUtils.isBlank(line) || line.startsWith("#"))
                    {
                        continue;
                    }
                    try
                    {
                        mappingEntries.add(parseLine(line));
                    } catch (Exception e)
                    {
                        builder.append("\nLine ").append(i + 1).append(": ").append(e.getMessage());
                    }
                }
                if (builder.length() > 0)
                {
                    throw new ConfigurationFailureException("Error(s) in mapping file "
                            + mappingFilePath + ":" + builder);
                }
            }
            return mappingEntries;
        }

        private static MappingEntry parseLine(String line)
        {
            int indexOfEqualSign = line.indexOf('=');
            if (indexOfEqualSign < 0)
            {
                throw new IllegalArgumentException("Missing '='");
            }
            String[] description = line.substring(0, indexOfEqualSign).trim().split("\\.");
            if (description.length != 3)
            {
                throw new IllegalArgumentException(
                        "Mapping description should have three parts separated by '.'");
            }
            Pattern dssCodePattern = createPattern(description[0].toUpperCase());
            Pattern dataSourceCodePattern = createPattern(description[1].toUpperCase());
            String type = description[2];
            String value = line.substring(indexOfEqualSign + 1).trim();
            return new MappingEntry(dssCodePattern, dataSourceCodePattern, type, value);
        }

        private static Pattern createPattern(String wildcardPattern)
        {
            String regex = wildcardPattern.replace("*", ".*");
            return Pattern.compile(regex);
        }

        public Mapping getMapping(String dataStoreCode, String moduleCode)
        {
            String configKey = dataStoreCode + "[" + moduleCode + "]";
            String dataSourceCode = null;
            Map<Type, String> replacementsByType = new EnumMap<Type, String>(Type.class);
            for (MappingEntry mappingEntry : mappingEntries)
            {
                String value = mappingEntry.value;
                if (mappingEntry.matches(dataStoreCode, moduleCode) && value != null)
                {
                    Type type = mappingEntry.type;
                    switch (type)
                    {
                        case CONFIG:
                            configKey =
                                    value.replace("[*]", "[" + moduleCode + "]").replace("*",
                                            dataStoreCode);
                            break;
                        case DATA_SOURCE:
                            dataSourceCode = value;
                            break;
                        default:
                            replacementsByType.put(type, value);
                    }
                }
            }
            DataSourceDefinition definition =
                    getDefinitionsOrNull(dataStoreCode, dataSourceCode, replacementsByType);
            if (configs.containsKey(configKey) == false)
            {
                if (configs.containsKey(dataStoreCode) == false)
                {
                    throw new EnvironmentFailureException("Couldn't find data source core plugin '"
                            + configKey + "' nor '" + dataStoreCode + "'.");
                }
                configKey = dataStoreCode;
            }
            return new Mapping(dataStoreCode, configKey, definition);
        }

        private DataSourceDefinition getDefinitionsOrNull(String dataStoreCode,
                String dataSourceCode, Map<Type, String> replacementsByType)
        {
            Map<String, DataSourceDefinition> definitionsByCode =
                    dataSourceDefinitionsByCodes.get(dataStoreCode);
            if (definitionsByCode == null)
            {
                return null;
            }
            DataSourceDefinition definition = definitionsByCode.get(dataSourceCode);
            if (definition == null && definitionsByCode.size() == 1)
            {
                definition = definitionsByCode.values().iterator().next();
            }
            if (definition != null)
            {
                definition = definition.clone();
                Set<Entry<Type, String>> entrySet = replacementsByType.entrySet();
                for (Entry<Type, String> entry : entrySet)
                {
                    entry.getKey().modify(definition, entry.getValue());
                }
            }
            return definition;
        }

        public void handle(String dataStoreCode, List<DataSourceDefinition> dataSourceDefinitions)
        {
            Map<String, DataSourceDefinition> definitionsByCode =
                    dataSourceDefinitionsByCodes.get(dataStoreCode);
            if (definitionsByCode == null)
            {
                definitionsByCode = new HashMap<String, DataSourceDefinition>();
                dataSourceDefinitionsByCodes.put(dataStoreCode, definitionsByCode);
            }
            for (DataSourceDefinition definition : dataSourceDefinitions)
            {
                definitionsByCode.put(definition.getCode(), definition);
            }
        }

        private static final class MappingEntry
        {
            private final Pattern dssCodePattern;

            private final Pattern moduleCodePattern;

            private final Type type;

            private final String value;

            public MappingEntry(Pattern dssCodePattern, Pattern moduleCodePattern, String typeName,
                    String value)
            {
                this.dssCodePattern = dssCodePattern;
                this.moduleCodePattern = moduleCodePattern;
                type = Type.getType(typeName);
                this.value = type.valueInUpperCase ? value.toUpperCase() : value;
            }

            public boolean matches(String dataStoreCode, String moduleCode)
            {
                if (dssCodePattern.matcher(dataStoreCode).matches() == false)
                {
                    return false;
                }
                return moduleCodePattern.matcher(moduleCode).matches();
            }
        }

        private static enum Type
        {
            HOST_PART("host-part")
            {
                @Override
                public void modify(DataSourceDefinition definition, String value)
                {
                    definition.setHostPart(value);
                }
            },
            USERNAME("username")
            {
                @Override
                public void modify(DataSourceDefinition definition, String value)
                {
                    definition.setUsername(value);
                }
            },
            PASSWORD("password")
            {
                @Override
                public void modify(DataSourceDefinition definition, String value)
                {
                    definition.setPassword(value);
                }
            },
            SID("sid")
            {
                @Override
                public void modify(DataSourceDefinition definition, String value)
                {
                    definition.setSid(value);
                }
            },
            CONFIG("config", true), DATA_SOURCE("data-source-code");

            static Type getType(String typeName)
            {
                Type[] values = Type.values();
                CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                for (Type type : values)
                {
                    if (type.name.equals(typeName))
                    {
                        return type;
                    }
                    builder.append(type.name);
                }

                throw new IllegalArgumentException("Unknown type '" + typeName
                        + "', possible values are: " + builder);
            }

            private final String name;

            private final boolean valueInUpperCase;

            private Type(String name)
            {
                this(name, false);
            }

            private Type(String name, boolean valueInUpperCase)
            {
                this.name = name;
                this.valueInUpperCase = valueInUpperCase;
            }

            public void modify(DataSourceDefinition definition, String value)
            {
            }
        }
    }

    private static final class Mapping
    {
        private final String dataStoreCode;

        private final String configKey;

        private final DataSourceDefinition definitionOrNull;

        public Mapping(String dataStoreCode, String configKey, DataSourceDefinition definitionOrNull)
        {
            this.dataStoreCode = dataStoreCode;
            this.configKey = configKey;
            this.definitionOrNull = definitionOrNull;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof Mapping == false)
            {
                return false;
            }
            Mapping that = (Mapping) obj;
            return this.configKey.equals(that.configKey)
                    && (this.definitionOrNull == null ? null == that.definitionOrNull
                            : this.definitionOrNull.equals(that.definitionOrNull));
        }

        @Override
        public int hashCode()
        {
            int sum = definitionOrNull == null ? 0 : definitionOrNull.hashCode();
            return 37 * sum + configKey.hashCode();
        }

        @Override
        public String toString()
        {
            return configKey + "[" + (definitionOrNull == null ? "?" : definitionOrNull) + "]";
        }

    }

}
