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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceWithDefinition;
import ch.systemsx.cisd.openbis.generic.shared.util.IDataSourceFactory;

/**
 * Stores and provides access to data sources defined in properties file.
 * <p>
 * Properties file example: <blockquote>
 * 
 * <pre>
 * # Define names of data sources
 * data-sources = my-data-source-1, my-data-source-2 
 * 
 * # Configure data source my-data-source-1
 * my-data-source-1.version-holder-class = ch.example.databases.MyDatabase1VersionHolder
 * my-data-source-1.databaseEngineCode = postgresql
 * my-data-source-1.basicDatabaseName = my-database-1
 * my-data-source-1.databaseKind = dev
 * my-data-source-1.scriptFolder = sql
 * my-data-source-1.maxIdle = 50
 * my-data-source-1.maxActive = 50
 * 
 * # Configure data source my-data-source-2
 * my-data-source-2.version-holder-class = ch.example.databases.MyDatabase2VersionHolder
 * my-data-source-2.factory-class = ch.example.databases.MyDataSourceFactory
 * my-data-source-2.exampleParameter1 = my-database-2
 * my-data-source-2.exampleParameter2 = 22
 * 
 * # Use my-data-source-1 as data source for archiver
 * archiver.class = ch.example.archivers.MyArchiver
 * archiver.data-source = my-data-source-1
 * 
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @author Izabela Adamczyk
 */
public class DataSourceProvider implements IDataSourceProvider
{
    private static final String DATA_SOURCE_FACTORY_CLASS_KEY = "factory-class";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataSourceProvider.class);

    private final Map<String, DataSourceWithDefinition> dataSources;

    private final List<DataSourceDefinition> dataSourceDefinitions =
            new ArrayList<DataSourceDefinition>();

    public static final String DATA_SOURCE_KEY = "data-source";

    private DataSourceProvider()
    {
        Properties properties = DssPropertyParametersUtil.loadServiceProperties();
        dataSources = new HashMap<String, DataSourceWithDefinition>();
        SectionProperties[] props =
                PropertyParametersUtil
                        .extractSectionProperties(properties, Constants.DATA_SOURCES_KEY, false);
        for (SectionProperties sectionProperties : props)
        {
            Properties dataSourceProperties = sectionProperties.getProperties();
            String dataSourceName = sectionProperties.getKey();
            String dataSourceFactoryClass =
                    sectionProperties.getProperties().getProperty(DATA_SOURCE_FACTORY_CLASS_KEY,
                            DefaultDataSourceFactory.class.getName());
            try
            {
                IDataSourceFactory factory =
                        ClassUtils.create(IDataSourceFactory.class, dataSourceFactoryClass);
                DataSourceWithDefinition dataSource = factory.create(dataSourceProperties);
                DataSourceDefinition definition = dataSource.getDefinitionOrNull();
                if (definition != null)
                {
                    definition.setCode(dataSourceName);
                    dataSourceDefinitions.add(definition);
                }
                dataSources.put(dataSourceName, dataSource);
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Data source '" + dataSourceName + "' defined.");
                }
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException(
                        "Error occured while creating data source '" + dataSourceName + "': "
                                + ex.getMessage(), ex);
            }
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data source provider initialized with " + dataSources.size()
                    + " data sources.");
        }
    }

    /**
     * Returns data source configured with given name or throws {@link IllegalArgumentException} if not configured.
     */
    @Override
    public DataSource getDataSource(String name) throws IllegalArgumentException
    {
        DataSourceWithDefinition result = dataSources.get(name);
        if (result == null)
        {
            String message = "Data source '" + name + "' has not been configured.";
            throw new IllegalArgumentException(message);
        } else
        {
            return result.getDataSource();
        }
    }

    /**
     * Extracts data source name ({@link #DATA_SOURCE_KEY}) from properties and returns requested data source. See also {@link #getDataSource(String)}
     * .
     */
    @Override
    public DataSource getDataSource(Properties properties)
    {
        return getDataSource(extractDataSourceName(properties));
    }

    @Override
    public List<DataSourceDefinition> getAllDataSourceDefinitions()
    {
        return dataSourceDefinitions;
    }

    /**
     * Extracts data source name ({@link #DATA_SOURCE_KEY}) from properties.
     */
    public static final String extractDataSourceName(Properties properties)
    {
        return PropertyUtils.getMandatoryProperty(properties, DATA_SOURCE_KEY);
    }

}
