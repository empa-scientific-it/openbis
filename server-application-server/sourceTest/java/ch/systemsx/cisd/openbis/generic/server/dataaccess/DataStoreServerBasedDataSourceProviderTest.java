/*
 * Copyright 2012 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.server.dataaccess.DataStoreServerBasedDataSourceProvider.DATA_STORE_SERVERS_KEY;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.dbmigration.MonitoringDataSource;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceWithDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.util.IDataSourceFactory;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreServerBasedDataSourceProviderTest extends AbstractFileSystemTestCase
{
    private static final String HEADER_LINE_OF_ERROR_MESSAGE =
            "Error(s) in mapping file targets/unit-test-wd/"
                    + DataStoreServerBasedDataSourceProviderTest.class.getName()
                    + "/mapping.txt:\n";

    private static final String PLUGIN_KEY = "key";

    private static final String DRIVER_CLASS = DatabaseEngine.POSTGRESQL.getDriverClass();

    private static class MockDataSource extends MonitoringDataSource
    {
        private final Properties properties;

        private boolean hasBeenClosed;

        MockDataSource(Properties properties)
        {
            this.properties = properties;
        }

        @Override
        public synchronized void close() throws SQLException
        {
            hasBeenClosed = true;
        }
    }

    private Mockery context;

    private IDAOFactory daofactory;

    private File mappingFile;

    private IDataStoreDAO dataStoreDAO;

    private Properties props;

    @Override
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daofactory = context.mock(IDAOFactory.class);
        dataStoreDAO = context.mock(IDataStoreDAO.class);
        mappingFile = new File(workingDirectory, "mapping.txt");
        mappingFile.delete();
        context.checking(new Expectations()
            {
                {
                    allowing(daofactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));
                }
            });
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("dss1", null).driver("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost/db1");
        builder.plugin("dss2", "tech1").driver("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost/db21");
        builder.plugin("dss2", "tech2").driver("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost/db22");
        props = builder.get();
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSourceByDataStoreServerCode()
    {
        prepareListDataStores();
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource dataSource =
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "my-tech");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://localhost/db1]", dataSource);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSourceByDataStoreServerCodeAndModuleCode()
    {
        prepareListDataStores();
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource dataSource =
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "tech1");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://localhost/db21]", dataSource);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSourceByDataStoreServerCodeNotFound()
    {
        prepareListDataStores();
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        try
        {
            dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "my-tech");
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Couldn't find data source core plugin 'DSS2[MY-TECH]' nor 'DSS2'.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithInvalidType()
    {
        assertFailedMappingFile("Line 1: Unknown type 'CONFIG', possible values are: "
                + "host-part, username, password, sid, config, data-source-code",
                "*.*.CONFIG = DSS\n");
    }

    @Test
    public void testMappingFileWithMissingValue()
    {
        assertFailedMappingFile("Line 3: Missing '='", "# example\n\n" + "alpha.beta*.sid\n");
    }

    @Test
    public void testMappingFileWithInvalidMappingDescription()
    {
        assertFailedMappingFile(
                "Line 1: Mapping description should have three parts separated by '.'", "a.b = c\n");
        assertFailedMappingFile(
                "Line 1: Mapping description should have three parts separated by '.'",
                "a.b.c.d = c\n");
    }

    @Test
    public void testMappingFileWithInvalidDataStoreCodePattern()
    {
        assertFailedMappingFile("Line 1: Unclosed group near index 3\n" + "A(B\n" + "   ^",
                "a(b.a.sid=b\n");
    }

    @Test
    public void testMappingFileWithUnspecifiedDataSourceCodeAndConfigMappingAllOnOne()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS", null).property(PLUGIN_KEY, "DSS");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = DSS\n");
        DataSourceDefinition dataSourceDefinition =
                new DataSourceDefinitionBuilder().code("my_db").driverClassName(DRIVER_CLASS)
                        .hostPart("ab").sid("mydb_dev").username("answer").password("42").get();
        DataStorePE dss1 = dataStore("DSS1", dataSourceDefinition);
        DataStorePE dss2 = dataStore("DSS2", dataSourceDefinition.clone());
        prepareListDataStores(dss1, dss2);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 =
                dataSourceProvider.getDataSourceByDataStoreServerCode("Dss1", "proteomics");
        DataSource ds2 = dataSourceProvider.getDataSourceByDataStoreServerCode("Dss2", "screening");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=42, database-url=jdbc:postgresql://ab/mydb_dev, "
                + "database-username=answer, key=DSS]", ds1);
        assertSame(ds1, ds2);
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingOnFixedModuleCode()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS2", null).user("alpha").property(PLUGIN_KEY, "DSS2");
        builder.plugin("DSS3", null).user("beta").property(PLUGIN_KEY, "DSS3");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = *\n");
        prepareListDataStores();
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "m1");
        DataSource ds2 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "m2");
        DataSource ds3 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss3", "m2");

        assertDataSourceProps("[database-username=alpha, key=DSS2]", ds1);
        assertDataSourceProps("[database-username=beta, key=DSS3]", ds3);
        assertSame(ds1, ds2);
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingOnFixedDataStoreCode()
    {
        FileUtilities.writeToFile(mappingFile, "*.*.config = dSS2[*]\n");
        prepareListDataStores();
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "tech1");
        DataSource ds2 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "tech2");
        DataSource ds3 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss3", "tech2");
        DataSource ds4 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss4", "tech1");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://localhost/db21]", ds1);
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://localhost/db22]", ds2);
        assertSame(ds2, ds3);
        assertSame(ds1, ds4);
        context.assertIsSatisfied();
    }

    @Test
    public void testRestartDSS2WithChangedDatabase()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS", null).property(PLUGIN_KEY, "DSS");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = DSS\n"
                + "*.screening.data-source-code = imaging_db\n");
        DataStorePE dss1 =
                dataStore("DSS1", new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("ab").sid("imaging_dev").get());
        DataStorePE dss2 =
                dataStore("DSS2", new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev").get());
        prepareListDataStores(dss1, dss2);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();
        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening");
        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening"));
        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening"));
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://ab/imaging_dev, key=DSS]", ds1);
        DataSource ds2 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening");
        assertSame(ds2, dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening"));
        final DataStorePE dataStore2 = dataStore("DSS2");
        prepareLoadAndUpdateDataStore(dataStore2);

        dataSourceProvider.handle(
                "DSS2",
                Arrays.asList(new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("ab").sid("imaging_dev").get()));

        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening"));
        assertEquals(false, ((MockDataSource) ds1).hasBeenClosed);
        assertEquals(true, ((MockDataSource) ds2).hasBeenClosed);
        DataSource ds22 =
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening");
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://ab/imaging_dev, key=DSS]", ds22);
        assertNotSame(ds2, ds22);
        assertEquals(
                "code=imaging_db\tdriverClassName=org.postgresql.Driver\thostPart=ab\tsid=imaging_dev\t\n",
                dataStore2.getSerializedDataSourceDefinitions());
        context.assertIsSatisfied();
    }

    @Test
    public void testRestartDSS2WithUnchangedDatabase()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS", null).property(PLUGIN_KEY, "DSS");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = DSS\n"
                + "*.screening.data-source-code = imaging_db\n");
        DataStorePE dss1 =
                dataStore("DSS1", new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev").get());
        DataStorePE dss2 =
                dataStore("DSS2", new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev").get());
        prepareListDataStores(dss1, dss2);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();
        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening");
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-url=jdbc:postgresql://abc/imaging_dev, key=DSS]", ds1);
        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening"));
        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening"));
        final DataStorePE dataStore2 = dataStore("DSS2");
        prepareLoadAndUpdateDataStore(dataStore2);

        dataSourceProvider.handle(
                "DSS2",
                Arrays.asList(new DataSourceDefinitionBuilder().code("imaging_db")
                        .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev").get()));

        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening"));
        assertSame(ds1, dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening"));
        assertEquals(
                "code=imaging_db\tdriverClassName=org.postgresql.Driver\thostPart=abc\tsid=imaging_dev\t\n",
                dataStore2.getSerializedDataSourceDefinitions());
        context.assertIsSatisfied();
    }

    @Test
    public void testKeepUsernameAndPasswordOfASCorePluginIgnoreDSSSettings()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS", null).property(PLUGIN_KEY, "DSS").user("einstein")
                .password("c is constant");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = DSS\n"
                + "*.screening.data-source-code = imaging_db\n");
        DataStorePE dss1 =
                dataStore(
                        "DSS1",
                        new DataSourceDefinitionBuilder().code("imaging_db")
                                .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev")
                                .username("newton").password("no limits").get());
        prepareListDataStores(dss1);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=c is constant, "
                + "database-url=jdbc:postgresql://abc/imaging_dev, "
                + "database-username=einstein, key=DSS]", ds1);
        context.assertIsSatisfied();
    }

    @Test
    public void testKeepUsernameAndPasswordOfASCorePluginIgnoreMappingFile()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("DSS", null).property(PLUGIN_KEY, "DSS").user("einstein")
                .password("c is constant");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "*.*.config = DSS\n"
                + "*.screening.data-source-code = imaging_db\n" + "*.screening.username = bohr\n");
        DataStorePE dss1 =
                dataStore(
                        "DSS1",
                        new DataSourceDefinitionBuilder().code("imaging_db")
                                .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev")
                                .username("newton").password("no limits").get());
        prepareListDataStores(dss1);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=c is constant, "
                + "database-url=jdbc:postgresql://abc/imaging_dev, "
                + "database-username=einstein, key=DSS]", ds1);
        context.assertIsSatisfied();
    }

    /**
     * This use case simulates the following use case:
     * <ol>
     * <li>Three DSS instances all using imaging_db
     * <li>DSS1 and DSS2 using a DB at AS.
     * <li>DSS3 using a DB at DSS.
     * </ol>
     * The data source instances for DSS1 and DSS2 should be the same.
     */
    @Test
    public void testThreeDssDBOneAtDSSTwoOnAS()
    {
        DataSourceConfigBuilder builder = new DataSourceConfigBuilder();
        builder.plugin("all", "screening").property(PLUGIN_KEY, "all[screening]");
        props = builder.get();
        FileUtilities.writeToFile(mappingFile, "# example mapping file\n"
                + "*.proteomics.config = all[proteomics]\n"
                + "*.proteomics.data-source-code = proteomics_db\n" + "  \n"
                + "*.screening.config = all[screening]\n"
                + "*.screening.data-source-code = imaging_db\n"
                + "*.screening.host-part = localhost:1234\n" + "*.screening.username = openbis\n"
                + "*.screening.password = abcd\n" + "DSS3.screening.host-part = a.b.c\n");
        DataStorePE dss1 =
                dataStore(
                        "DSS1",
                        new DataSourceDefinitionBuilder().code("imaging_db")
                                .driverClassName(DRIVER_CLASS).hostPart("abc").sid("imaging_dev")
                                .username("dss1").password("42").get());
        DataStorePE dss2 =
                dataStore(
                        "DSS2",
                        new DataSourceDefinitionBuilder().code("imaging_db")
                                .driverClassName(DRIVER_CLASS).hostPart("123").sid("imaging_dev")
                                .username("dss2").password("42").get());
        DataStorePE dss3 =
                dataStore(
                        "DSS3",
                        new DataSourceDefinitionBuilder().code("imaging_db")
                                .driverClassName(DRIVER_CLASS).hostPart("def").sid("imaging_dev")
                                .username("dss3").password("42").get());
        prepareListDataStores(dss1, dss2, dss3);
        DataStoreServerBasedDataSourceProvider dataSourceProvider = createDataSourceProvider();

        DataSource ds1 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "screening");
        DataSource ds2 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "screening");
        DataSource ds3 = dataSourceProvider.getDataSourceByDataStoreServerCode("dss3", "screening");

        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=abcd, "
                + "database-url=jdbc:postgresql://localhost:1234/imaging_dev, "
                + "database-username=openbis, " + "key=all[screening]]", ds1);
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=abcd, "
                + "database-url=jdbc:postgresql://localhost:1234/imaging_dev, "
                + "database-username=openbis, " + "key=all[screening]]", ds2);
        assertSame(ds1, ds2);
        assertDataSourceProps("[database-driver=org.postgresql.Driver, "
                + "database-password=abcd, " + "database-url=jdbc:postgresql://a.b.c/imaging_dev, "
                + "database-username=openbis, " + "key=all[screening]]", ds3);

        context.assertIsSatisfied();
    }

    private void assertDataSourceProps(String expectedProps, DataSource dataSource)
    {
        Set<Entry<Object, Object>> entrySet = ((MockDataSource) dataSource).properties.entrySet();
        List<Entry<Object, Object>> sortedProps = new ArrayList<Entry<Object, Object>>(entrySet);
        Collections.sort(sortedProps, new Comparator<Entry<Object, Object>>()
            {
                @Override
                public int compare(Entry<Object, Object> o1, Entry<Object, Object> o2)
                {
                    return o1.getKey().toString().compareTo(o2.getKey().toString());
                }
            });
        assertEquals(expectedProps, sortedProps.toString());
    }

    private void assertFailedMappingFile(String expectedErrorMessage, String mappingFileContent)
    {
        FileUtilities.writeToFile(mappingFile, mappingFileContent);
        try
        {
            createDataSourceProvider();
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertTrue(e.getMessage(), (HEADER_LINE_OF_ERROR_MESSAGE + expectedErrorMessage).startsWith(e.getMessage()));
        }
        context.assertIsSatisfied();
    }

    private void prepareListDataStores(final DataStorePE... dataStores)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    will(returnValue(Arrays.asList(dataStores)));
                }
            });
    }

    private void prepareLoadAndUpdateDataStore(final DataStorePE dataStore)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).tryToFindDataStoreByCode(dataStore.getCode());
                    will(returnValue(dataStore));

                    one(dataStoreDAO).createOrUpdateDataStore(dataStore);
                }
            });
    }

    private DataStorePE dataStore(String code, DataSourceDefinition... definitions)
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(code);
        String serializedDefinitions = DataSourceDefinition.toString(Arrays.asList(definitions));
        dataStore.setSerializedDataSourceDefinitions(serializedDefinitions);
        return dataStore;
    }

    private DataStoreServerBasedDataSourceProvider createDataSourceProvider()
    {
        DataStoreServerBasedDataSourceProvider dataSourceProvider =
                new DataStoreServerBasedDataSourceProvider(daofactory, mappingFile.getPath(),
                        new IDataSourceFactory()
                            {
                                @Override
                                public DataSourceWithDefinition create(Properties dbProps)
                                {
                                    return new DataSourceWithDefinition(
                                            new MockDataSource(dbProps), null);
                                }
                            });
        dataSourceProvider.init(props);
        return dataSourceProvider;
    }

    private static final class DataSourceDefinitionBuilder
    {
        private DataSourceDefinition definition = new DataSourceDefinition();

        DataSourceDefinition get()
        {
            return definition;
        }

        DataSourceDefinitionBuilder code(String code)
        {
            definition.setCode(code);
            return this;
        }

        DataSourceDefinitionBuilder driverClassName(String driverClassName)
        {
            definition.setDriverClassName(driverClassName);
            return this;
        }

        DataSourceDefinitionBuilder hostPart(String hostPart)
        {
            definition.setHostPart(hostPart);
            return this;
        }

        DataSourceDefinitionBuilder sid(String sid)
        {
            definition.setSid(sid);
            return this;
        }

        DataSourceDefinitionBuilder username(String username)
        {
            definition.setUsername(username);
            return this;
        }

        DataSourceDefinitionBuilder password(String password)
        {
            definition.setPassword(password);
            return this;
        }
    }

    private static final class DataSourceConfigBuilder
    {
        private final Set<String> pluginKeys = new TreeSet<String>();

        private final Properties properties = new Properties();

        public Properties get()
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (String pluginKey : pluginKeys)
            {
                builder.append(pluginKey);
            }
            properties.setProperty(DATA_STORE_SERVERS_KEY, builder.toString());
            return properties;
        }

        public SubConfigBuilder plugin(String dataStoreCode, String moduleCodeOrNull)
        {
            return new SubConfigBuilder(this, pluginkey(dataStoreCode, moduleCodeOrNull));
        }

        public DataSourceConfigBuilder property(String pluginKey, String key, String value)
        {
            pluginKeys.add(pluginKey);
            return property(pluginKey + "." + key, value);
        }

        public DataSourceConfigBuilder property(String key, String value)
        {
            properties.setProperty(key, value);
            return this;
        }

        private String pluginkey(String dataStoreCode, String moduleCodeOrNull)
        {
            return dataStoreCode + (moduleCodeOrNull == null ? "" : "[" + moduleCodeOrNull + "]");
        }
    }

    private static final class SubConfigBuilder
    {
        private final DataSourceConfigBuilder builder;

        private final String pluginKey;

        SubConfigBuilder(DataSourceConfigBuilder builder, String pluginKey)
        {
            this.builder = builder;
            this.pluginKey = pluginKey;
        }

        public SubConfigBuilder driver(String value)
        {
            return property(SimpleDatabaseConfigurationContext.DRIVER_KEY, value);
        }

        public SubConfigBuilder url(String value)
        {
            return property(SimpleDatabaseConfigurationContext.URL_KEY, value);
        }

        public SubConfigBuilder user(String value)
        {
            return property(SimpleDatabaseConfigurationContext.USER_KEY, value);
        }

        public SubConfigBuilder password(String value)
        {
            return property(SimpleDatabaseConfigurationContext.PASSWORD_KEY, value);
        }

        public SubConfigBuilder property(String key, String value)
        {
            builder.property(pluginKey, key, value);
            return this;
        }
    }

}
