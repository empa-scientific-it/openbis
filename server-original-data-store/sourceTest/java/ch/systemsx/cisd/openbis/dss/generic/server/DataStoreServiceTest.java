/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases.AbstractSearchDomainService;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverPluginFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceTest extends AssertJUnit
{
    private static final String DATA_STORE_CODE = "DSS";

    private static final String EMAIL = "email";

    private static final String USER_ID = "user";

    private static final Map<String, String> OPTIONAL_PARAMETERS = Collections.singletonMap("greeting", "hi");

    private static final String SEQUENCE_SNIPPET = "GATTACA";

    private static final String INVALID_SESSION_TOKEN_MSG = "Invalid session token.";

    private static final String CIFEX_URL = "cifexURL";

    private static final File TEST_FOLDER = new File("targets/data-store-service-test");

    private static final File TEST_STORE = new File(TEST_FOLDER, "store");

    private static final class MockDataStoreService extends DataStoreService
    {
        private final ICIFEXRPCServiceFactory cifexServiceFactory;

        private final String expectedCIFEXURL;

        MockDataStoreService(SessionTokenManager sessionTokenManager, OpenbisSessionTokenCache sessionTokenCache,
                MailClientParameters mailClientParameters,
                ICIFEXRPCServiceFactory cifexServiceFactory, String expectedCIFEXURL,
                IPluginTaskInfoProvider pluginTaskParameters, IDataSetCommandExecutorProvider executorProvider)
        {
            super(sessionTokenManager, sessionTokenCache, mailClientParameters,
                    pluginTaskParameters, executorProvider);
            this.cifexServiceFactory = cifexServiceFactory;
            this.expectedCIFEXURL = expectedCIFEXURL;
        }

        @Override
        protected ICIFEXRPCServiceFactory createCIFEXRPCServiceFactory(String cifexURL)
        {
            AssertJUnit.assertEquals(expectedCIFEXURL, cifexURL);
            return cifexServiceFactory;
        }
    }

    public static final class MockSequenceDatabase extends AbstractSearchDomainService
    {
        private static final String DATA_SET_KEY = "data-set";

        private static final String AVAILABLE_KEY = "available";

        private boolean available;

        private String dataSetCode;

        public MockSequenceDatabase(Properties properties, File storeRoot)
        {
            super(properties, storeRoot);
            available = "true".equals(properties.getProperty(AVAILABLE_KEY));
            dataSetCode = properties.getProperty(DATA_SET_KEY);
        }

        @Override
        public boolean isAvailable()
        {
            return available;
        }

        @Override
        public List<SearchDomainSearchResult> search(String sequenceSnippet, Map<String, String> optionalParametersOrNull)
        {
            assertSame(SEQUENCE_SNIPPET, sequenceSnippet);
            assertSame(OPTIONAL_PARAMETERS, optionalParametersOrNull);
            SearchDomainSearchResult sequenceSearchResult = new SearchDomainSearchResult();
            DataSetFileSearchResultLocation resultLocation = new DataSetFileSearchResultLocation();
            sequenceSearchResult.setResultLocation(resultLocation);
            resultLocation.setPermId(dataSetCode);
            return Arrays.asList(sequenceSearchResult);
        }
    }

    public static final class MockProcessingTask implements IProcessingPluginTask
    {
        private static final long serialVersionUID = 1L;

        private List<List<DatasetDescription>> recordedDataSets = new ArrayList<List<DatasetDescription>>();

        public MockProcessingTask(Properties properties, File storeRoot)
        {
        }

        @Override
        public ProcessingStatus process(List<DatasetDescription> datasets, DataSetProcessingContext context)
        {
            recordedDataSets.add(datasets);
            return new ProcessingStatus();
        }
    }

    private SessionTokenManager sessionTokenManager;

    private String sessionToken;

    private String userSessionToken;

    private Mockery context;

    private IDataSetCommandExecutor commandExecutor;

    private ICIFEXRPCServiceFactory cifexServiceFactory;

    private MailClientParameters mailClientParameters;

    private ICIFEXComponent cifex;

    private IPluginTaskInfoProvider pluginTaskParameters;

    private IShareIdManager shareIdManager;

    private IServiceForDataStoreServer openbisService;

    private IDataSetCommandExecutorProvider executorProvider;

    private IConfigProvider configProvider;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        sessionTokenManager = new SessionTokenManager();
        sessionToken = sessionTokenManager.drawSessionToken();
        userSessionToken = sessionTokenManager.drawSessionToken();
        openbisService = context.mock(IServiceForDataStoreServer.class);
        executorProvider = context.mock(IDataSetCommandExecutorProvider.class);
        commandExecutor = context.mock(IDataSetCommandExecutor.class);
        shareIdManager = context.mock(IShareIdManager.class);
        cifexServiceFactory = context.mock(ICIFEXRPCServiceFactory.class);
        cifex = context.mock(ICIFEXComponent.class);
        configProvider = context.mock(IConfigProvider.class);
        mailClientParameters = new MailClientParameters();
        mailClientParameters.setFrom("a@bc.de");
        mailClientParameters.setSmtpHost("file://targets/email");
        FileUtilities.deleteRecursively(TEST_FOLDER);
        pluginTaskParameters = context.mock(IPluginTaskInfoProvider.class);
        TEST_STORE.mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(executorProvider).getDefaultExecutor();
                    will(returnValue(commandExecutor));

                    allowing(pluginTaskParameters).getStoreRoot();
                    will(returnValue(TEST_STORE));

                    allowing(pluginTaskParameters).getArchiverPluginFactory();
                    will(returnValue(new ArchiverPluginFactory(new SectionProperties("archiver", new Properties()))));
                    
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForEntitiesWithSequencesUndefinedDatabases()
    {
        prepareSearchDomainInfoProvider();

        List<SearchDomainSearchResult> result = createService().searchForEntitiesWithSequences(sessionToken,
                null, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals(0, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForEntitiesWithSequencesUnAvailableDatabases()
    {
        prepareSearchDomainInfoProvider("-1", "-2");

        List<SearchDomainSearchResult> result = createService().searchForEntitiesWithSequences(sessionToken,
                null, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals(0, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForEntitiesWithSequencesWithUnspecifiedDatabase()
    {
        prepareSearchDomainInfoProvider("1", "2");

        List<SearchDomainSearchResult> result = createService().searchForEntitiesWithSequences(sessionToken,
                null, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals("DS-1", ((DataSetFileSearchResultLocation) result.get(0).getResultLocation()).getPermId());
        assertEquals(1, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForEntitiesWithSequencesWithSpecifiedAndAvailableDatabase()
    {
        prepareSearchDomainInfoProvider("1", "2");

        List<SearchDomainSearchResult> result = createService().searchForEntitiesWithSequences(sessionToken,
                "db-2", SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals("DS-2", ((DataSetFileSearchResultLocation) result.get(0).getResultLocation()).getPermId());
        assertEquals(1, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForEntitiesWithSequencesWithSpecifiedButUnailableDatabase()
    {
        prepareSearchDomainInfoProvider("-1", "2", "-3", "4");

        List<SearchDomainSearchResult> result = createService().searchForEntitiesWithSequences(sessionToken,
                "db-3", SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals("DS-2", ((DataSetFileSearchResultLocation) result.get(0).getResultLocation()).getPermId());
        assertEquals(1, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetVersionForInvalidSessionToken()
    {
        try
        {
            createService().getVersion("invalid");
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetVersion()
    {
        assertEquals(IDataStoreService.VERSION, createService().getVersion(sessionToken));

        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetKnownDataSetsForInvalidSessionToken()
    {
        try
        {
            createService().getKnownDataSets("invalid", null, false);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetKnownDataSets() throws IOException
    {
        final String shareId = "share-1";
        String location = "ds1";
        DatasetDescriptionBuilder ds1 = new DatasetDescriptionBuilder("ds1").location(location);
        DatasetDescriptionBuilder ds2 = new DatasetDescriptionBuilder("ds2").location(location);
        File share = new File(TEST_STORE, shareId);
        share.mkdirs();
        new File(share, location).createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock("ds1");
                    one(shareIdManager).isKnown("ds1");
                    will(returnValue(true));
                    one(shareIdManager).getShareId("ds1");
                    will(returnValue(shareId));
                    one(shareIdManager).releaseLock("ds1");

                    one(shareIdManager).lock("ds2");
                    one(shareIdManager).isKnown("ds2");
                    will(returnValue(false));
                    one(shareIdManager).releaseLock("ds2");
                }
            });

        IDataStoreService service = createService();
        @SuppressWarnings("deprecation")
        List<String> knownDataSets =
                service.getKnownDataSets(sessionToken,
                        Arrays.asList(ds1.getDatasetDescription(), ds2.getDatasetDescription()),
                        false);

        assertEquals(1, knownDataSets.size());
        assertSame(location, knownDataSets.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetsForInvalidSessionToken()
    {
        try
        {
            createService().uploadDataSetsToCIFEX("invalid", null, null);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetsForInvalidPassword()
    {
        final List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL(CIFEX_URL);
        uploadContext.setUserID(USER_ID);
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(cifexServiceFactory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(null));
                }
            });

        try
        {
            createService().uploadDataSetsToCIFEX(sessionToken, dataSets, uploadContext);
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals("User failed to be authenticated by CIFEX.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSets()
    {
        final List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL(CIFEX_URL);
        uploadContext.setUserID(USER_ID);
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(cifexServiceFactory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue("token"));
                    one(cifex).logout("token");

                    one(commandExecutor).scheduleUploadingDataSetsToCIFEX(cifexServiceFactory,
                            mailClientParameters, dataSets, uploadContext, null, null);
                }
            });

        createService().uploadDataSetsToCIFEX(sessionToken, dataSets, uploadContext);

        context.assertIsSatisfied();
    }

    @Test
    public void testProcessDatasets()
    {
        List<DatasetDescription> datasets = Arrays.<DatasetDescription> asList(new DatasetDescription());
        Map<String, String> parameterBindings = new HashMap<String, String>();
        RecordingMatcher<DatastoreServiceDescription> recordingMatcher = prepareProcessingTaskInfoProvider(datasets, parameterBindings, "my-task");

        createService().processDatasets(sessionToken, userSessionToken, "my-task", datasets,
                parameterBindings, USER_ID, EMAIL);

        assertEquals("[PROCESSING; my-task; DSS; my-task; .* ; null]", recordingMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    private RecordingMatcher<DatastoreServiceDescription> prepareProcessingTaskInfoProvider(
            final List<DatasetDescription> datasets, final Map<String, String> parameterBindings,
            final String... tasks)
    {
        final RecordingMatcher<DatastoreServiceDescription> recorder = new RecordingMatcher<DatastoreServiceDescription>();
        context.checking(new Expectations()
            {
                {
                    List<PluginTaskFactory<IProcessingPluginTask>> factories
                    = new ArrayList<PluginTaskFactory<IProcessingPluginTask>>();
                    for (String task : tasks)
                    {
                        Properties properties = new Properties();
                        properties.setProperty(PluginTaskFactory.LABEL_PROPERTY_NAME, task);
                        properties.setProperty(PluginTaskFactory.CLASS_PROPERTY_NAME, MockProcessingTask.class.getName());
                        properties.setProperty(PluginTaskFactory.DATASET_CODES_PROPERTY_NAME, ".*");
                        factories.add(new PluginTaskFactory<IProcessingPluginTask>(null,
                                new SectionProperties(task, properties), DATA_STORE_CODE,
                                IProcessingPluginTask.class, task, TEST_STORE));
                        allowing(executorProvider).getExecutor(with(any(MockProcessingTask.class)), with(task));
                        will(returnValue(commandExecutor));
                        allowing(commandExecutor).scheduleProcessDatasets(with(any(MockProcessingTask.class)),
                                with(datasets), with(parameterBindings), with(USER_ID), with(EMAIL),
                                with(userSessionToken), with(recorder),
                                with(mailClientParameters));
                    }
                    allowing(pluginTaskParameters).getProcessingPluginsProvider();
                    will(returnValue(new PluginTaskProvider<IProcessingPluginTask>(factories)));
                }
            });
        return recorder;
    }

    private void prepareSearchDomainInfoProvider(final String... databases)
    {
        context.checking(new Expectations()
            {
                {
                    List<PluginTaskFactory<ISearchDomainService>> factories
                    = new ArrayList<PluginTaskFactory<ISearchDomainService>>();
                    for (String database : databases)
                    {
                        boolean available = database.startsWith("-") == false;
                        if (available == false)
                        {
                            database = database.substring(1);
                        }
                        Properties properties = new Properties();
                        properties.setProperty(PluginTaskFactory.LABEL_PROPERTY_NAME, database);
                        properties.setProperty(PluginTaskFactory.CLASS_PROPERTY_NAME, MockSequenceDatabase.class.getName());
                        properties.setProperty(MockSequenceDatabase.AVAILABLE_KEY, Boolean.toString(available));
                        properties.setProperty(MockSequenceDatabase.DATA_SET_KEY, "DS-" + database.toUpperCase());
                        factories.add(new PluginTaskFactory<ISearchDomainService>(null,
                                new SectionProperties("db-" + database, properties), DATA_STORE_CODE,
                                ISearchDomainService.class, "Test-db-" + database, TEST_STORE));
                    }
                    allowing(pluginTaskParameters).getSearchDomainServiceProvider();
                    will(returnValue(new PluginTaskProvider<ISearchDomainService>(factories)));
                }
            });
    }

    private IDataStoreService createService()
    {
        OpenbisSessionTokenCache sessionTokenCache = new OpenbisSessionTokenCache(openbisService);
        MockDataStoreService service =
                new MockDataStoreService(sessionTokenManager, sessionTokenCache,
                        mailClientParameters, cifexServiceFactory,
                        CIFEX_URL, pluginTaskParameters, executorProvider);
        service.setShareIdManager(shareIdManager);
        service.afterPropertiesSet();
        service.setConfig(configProvider);
        return service;
    }
}
