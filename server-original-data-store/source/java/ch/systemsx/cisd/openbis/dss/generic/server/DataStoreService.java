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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverPluginFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the openBIS server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreServiceInternal, InitializingBean
{
    private static final String COPYING_TO_ARCHIVE_PROCESSING_PLUGIN_KEY = "Copying data sets to archive";

    private static final String ARCHIVING_PROCESSING_PLUGIN_KEY = "Archiving";

    private static final String UNARCHIVING_PROCESSING_PLUGIN_KEY = "Unarchiving";

    private final SessionTokenManager sessionTokenManager;

    private final OpenbisSessionTokenCache sessionTokenCache;

    private final IDataSetCommandExecutorProvider dataSetCommandExecutorProvider;

    private final MailClientParameters mailClientParameters;

    private final IPluginTaskInfoProvider pluginTaskInfoProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IShareIdManager shareIdManager;

    private String cifexAdminUserOrNull;

    private String cifexAdminPasswordOrNull;

    private File storeRoot;

    private PutDataSetService putService;

    private IConfigProvider config;

    private final Map<String, List<IDelegatedAction>> cleanupActions = new HashMap<>();

    public DataStoreService(SessionTokenManager sessionTokenManager, OpenbisSessionTokenCache sessionTokenCache,
            MailClientParameters mailClientParameters, IPluginTaskInfoProvider pluginTaskParameters,
            IDataSetCommandExecutorProvider dataSetCommandExecutorProvider)
    {
        this.sessionTokenManager = sessionTokenManager;
        this.sessionTokenCache = sessionTokenCache;
        this.mailClientParameters = mailClientParameters;
        this.pluginTaskInfoProvider = pluginTaskParameters;
        this.dataSetCommandExecutorProvider = dataSetCommandExecutorProvider;
        storeRoot = pluginTaskParameters.getStoreRoot();
    }

    void setShareIdManager(IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
    }

    public void setCifexAdminUserOrNull(String cifexAdminUserOrNull)
    {
        this.cifexAdminUserOrNull = cifexAdminUserOrNull;
    }

    public void setCifexAdminPasswordOrNull(String cifexAdminPasswordOrNull)
    {
        this.cifexAdminPasswordOrNull = cifexAdminPasswordOrNull;
    }

    @Override
    public void afterPropertiesSet()
    {
        String prefix = "Property 'storeRoot' ";
        if (storeRoot == null)
        {
            throw new IllegalStateException(prefix + "not set.");
        }
        String storeRootPath = storeRoot.getAbsolutePath();
        if (storeRoot.isFile())
        {
            throw new IllegalArgumentException(prefix + "is a file instead of a directory: "
                    + storeRootPath);
        }
        if (storeRoot.exists() == false)
        {
            if (storeRoot.mkdirs() == false)
            {
                throw new IOExceptionUnchecked(new IOException(
                        "Couldn't create root directory of the data store: " + storeRootPath));
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Creates root directory of the data store: " + storeRootPath);
            }
        }
        migrateStore();
        ArchiverPluginFactory factory = pluginTaskInfoProvider.getArchiverPluginFactory();
        if (factory.isArchiverConfigured())
        {
            factory.createInstance(storeRoot); // checks configuration of the archiver
        }
    }

    @Override
    public void initialize()
    {
        dataSetCommandExecutorProvider.init(storeRoot);
        getShareIdManager().isKnown(""); // initializes ShareIdManager: reading all share ids from
                                         // the data base
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Initialization finished.");
        }

    }

    private void migrateStore()
    {
        File defaultShare = new File(storeRoot, Constants.DEFAULT_SHARE_ID);
        if (defaultShare.exists() == false)
        {
            if (defaultShare.mkdirs() == false)
            {
                throw new IOExceptionUnchecked(new IOException(
                        "Couldn't create default share in data store: " + defaultShare));
            }
            File[] stores = storeRoot.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return UuidUtil.isValidUUID(name);
                    }
                });
            for (File file : stores)
            {
                if (file.renameTo(new File(defaultShare, file.getName())) == false)
                {
                    throw new IOExceptionUnchecked(new IOException("Couldn't move '" + file
                            + "' into default share '" + defaultShare + "'."));
                }
            }
            operationLog.info("Store migrated to default share");
        }
    }

    @Override
    public IDataStoreService createLogger(IInvocationLoggerContext context)
    {
        return new DataStoreServiceLogger(operationLog, context);
    }

    @Override
    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        return IDataStoreService.VERSION;
    }

    @Override
    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSets, boolean ignoreNonExistingLocation)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        IServiceConversationProgressListener listener =
                ServiceConversationsThreadContext.getProgressListener();

        List<String> knownLocations = new ArrayList<String>();
        IShareIdManager manager = getShareIdManager();
        int index = 0;

        for (IDatasetLocation dataSet : dataSets)
        {
            String datasetCode = dataSet.getDataSetCode();
            String location = dataSet.getDataSetLocation();
            manager.lock(datasetCode);
            try
            {
                if (manager.isKnown(datasetCode)
                        && (ignoreNonExistingLocation || new File(new File(storeRoot,
                                manager.getShareId(datasetCode)), location).exists()))
                {
                    knownLocations.add(location);
                }
            } finally
            {
                manager.releaseLock(datasetCode);
            }

            listener.update("getKnownDataSets", dataSets.size(), ++index);
        }
        return knownLocations;
    }

    @Override
    public void uploadDataSetsToCIFEX(String sessionToken, List<AbstractExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        if (context.getCifexURL() == null)
        {
            throw new ConfigurationFailureException(
                    "Upload of data sets to CIFEX not possible: no CIFEX URL configured.");
        }
        ICIFEXRPCServiceFactory serviceFactory =
                createCIFEXRPCServiceFactory(context.getCifexURL());
        ICIFEXComponent cifex = serviceFactory.createCIFEXComponent();
        String userID = context.getUserID();
        String password = context.getPassword();
        if (UploadingCommand.canLoginToCIFEX(cifex, context.isUserAuthenticated(), userID,
                password, cifexAdminUserOrNull, cifexAdminPasswordOrNull) == false)
        {
            throw new InvalidSessionException("User failed to be authenticated by CIFEX.");
        }
        IDataSetCommandExecutor commandExecutor = dataSetCommandExecutorProvider.getDefaultExecutor();
        commandExecutor.scheduleUploadingDataSetsToCIFEX(serviceFactory, mailClientParameters,
                dataSets, context, cifexAdminUserOrNull, cifexAdminPasswordOrNull);
    }

    protected ICIFEXRPCServiceFactory createCIFEXRPCServiceFactory(String cifexURL)
    {
        return new CIFEXRPCServiceFactory(cifexURL);
    }

    @Override
    public TableModel createReportFromDatasets(String sessionToken, String userSessionToken,
            String serviceKey, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        return internalCreateReportFromDatasets(userSessionToken, serviceKey, datasets, userId,
                userEmailOrNull);
    }

    @Override
    public void processDatasets(String sessionToken, String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userId, String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IProcessingPluginTask> plugins =
                pluginTaskInfoProvider.getProcessingPluginsProvider();

        IProcessingPluginTask task = plugins.getPluginInstance(serviceKey);
        DatastoreServiceDescription pluginDescription = plugins.getPluginDescription(serviceKey);
        scheduleTask(userSessionToken, serviceKey, task, datasets, userId, userEmailOrNull,
                pluginDescription, parameterBindings);
    }

    @Override
    public void unarchiveDatasets(String sessionToken, String userSessionToken,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull)
    {
        IProcessingPluginTask task = new UnarchiveProcessingPluginTask(getArchiverPlugin());

        scheduleTask(sessionToken, userSessionToken, UNARCHIVING_PROCESSING_PLUGIN_KEY, task, datasets, userId,
                userEmailOrNull, new HashMap<>());
    }

    @Override
    public boolean isArchivingPossible(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        return getArchiverPlugin().isArchivingPossible();
    }

    @Override
    public List<String> getDataSetCodesForUnarchiving(String sessionToken, String userSessionToken, List<String> datasets, String userId)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        return getArchiverPlugin().getDataSetCodesForUnarchiving(datasets);
    }

    @Override
    public void archiveDatasets(String sessionToken, String userSessionToken, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull, boolean removeFromDataStore, Map<String, String> options)
    {
        String description = removeFromDataStore ? ARCHIVING_PROCESSING_PLUGIN_KEY : COPYING_TO_ARCHIVE_PROCESSING_PLUGIN_KEY;
        IProcessingPluginTask task = new ArchiveProcessingPluginTask(getArchiverPlugin(), removeFromDataStore);
        System.err.println("archive " + datasets + " options:" + options);

        scheduleTask(sessionToken, userSessionToken, description, task, datasets, userId, userEmailOrNull, options);
    }

    @Override public Properties getArchiverProperties()
    {
        ArchiverPluginFactory archiverPluginFactory = pluginTaskInfoProvider.getArchiverPluginFactory();

        if (archiverPluginFactory.isArchiverConfigured())
        {
            return archiverPluginFactory.getArchiverProperties();
        } else
        {
            return null;
        }
    }

    @Override
    public IArchiverPlugin getArchiverPlugin()
    {
        ArchiverPluginFactory factory = pluginTaskInfoProvider.getArchiverPluginFactory();
        return factory.createInstance(storeRoot);
    }

    @Override
    public IDataSetDirectoryProvider getDataSetDirectoryProvider()
    {
        return new DataSetDirectoryProvider(storeRoot, getShareIdManager());
    }

    @Override
    public ISessionWorkspaceProvider getSessionWorkspaceProvider(String userSessionToken)
    {
        return new SessionWorkspaceProvider(pluginTaskInfoProvider
                .getSessionWorkspaceRootDir(), userSessionToken);
    }

    @Override
    public TableModel createReportFromAggregationService(String sessionToken,
            String userSessionToken, String serviceKey, Map<String, Object> parameters,
            String userId, String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        return internalCreateReportFromAggregationService(userSessionToken, serviceKey, parameters,
                userId, userEmailOrNull);
    }

    @Override
    public IMailClient createEMailClient()
    {
        return new MailClient(mailClientParameters);
    }

    private void scheduleTask(String sessionToken, String userSessionToken, String description,
            IProcessingPluginTask processingTask, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull, Map<String, String> options)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        DatastoreServiceDescription pluginDescription =
                DatastoreServiceDescription.processing(description, description, null, null);
        scheduleTask(userSessionToken, description, processingTask, datasets, userId, userEmailOrNull,
                pluginDescription, options);
    }

    @Override
    public void scheduleTask(String taskKey, IProcessingPluginTask task, Map<String, String> parameterBindings,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull, String userSessionToken)
    {
        DatastoreServiceDescription description = DatastoreServiceDescription.processing(taskKey, taskKey, null, null);
        scheduleTask(userSessionToken, taskKey, task, datasets, userId, userEmailOrNull, description, parameterBindings);
    }

    private void scheduleTask(String userSessionToken, String taskKey, IProcessingPluginTask processingTask,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull,
            DatastoreServiceDescription pluginDescription, Map<String, String> parameterBindings)
    {
        IDataSetCommandExecutor commandExecutor = dataSetCommandExecutorProvider.getExecutor(processingTask, taskKey);
        commandExecutor.scheduleProcessDatasets(processingTask, datasets, parameterBindings,
                userId, userEmailOrNull, userSessionToken, pluginDescription, mailClientParameters);
    }

    private static class ArchiveProcessingPluginTask implements IProcessingPluginTask
    {

        private static final long serialVersionUID = 1L;

        private IArchiverPlugin archiverTask;

        private boolean removeFromDataStore;

        public ArchiveProcessingPluginTask(final IArchiverPlugin archiverTask,
                final boolean removeFromDataStore)
        {
            this.archiverTask = archiverTask;
            this.removeFromDataStore = removeFromDataStore;
        }

        @Override
        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext = createContext(context);
            return archiverTask.archive(datasets, archiverContext, removeFromDataStore);
        }

    }

    private static class UnarchiveProcessingPluginTask implements IProcessingPluginTask
    {

        private static final long serialVersionUID = 1L;

        private IArchiverPlugin archiverTask;

        public UnarchiveProcessingPluginTask(final IArchiverPlugin archiverTask)
        {
            this.archiverTask = archiverTask;
        }

        @Override
        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext = createContext(context);
            return archiverTask.unarchive(datasets, archiverContext);
        }
    }

    private static ArchiverTaskContext createContext(DataSetProcessingContext context)
    {
        ArchiverTaskContext archiverTaskContext = new ArchiverTaskContext(context.getDirectoryProvider(),
                context.getHierarchicalContentProviderUnfiltered());
        archiverTaskContext.setUserId(context.getUserId());
        archiverTaskContext.setUserEmail(context.getUserEmailOrNull());
        archiverTaskContext.setUserSessionToken(context.trySessionToken());
        archiverTaskContext.setOptions(context.getParameterBindings());
        return archiverTaskContext;
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String sessionToken, String serviceKey,
            DatasetDescription dataSet)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        return task.createLink(dataSet);
    }

    @Override
    public IDataSetDeleter getDataSetDeleter()
    {
        return dataSetCommandExecutorProvider.getDefaultExecutor();
    }

    @Override
    public String putDataSet(String sessionToken, String dropboxName,
            CustomImportFile customImportFile)
    {
        PutDataSetService service = getPutDataSetService();
        return service.putDataSet(sessionToken, dropboxName, customImportFile);
    }

    @Override
    public void cleanupSession(String userSessionToken)
    {
        sessionTokenCache.removeSessionToken(userSessionToken);
        final File sessionWorkspace =
                new File(pluginTaskInfoProvider.getSessionWorkspaceRootDir(), userSessionToken);
        if (sessionWorkspace.exists())
        {
            QueueingPathRemoverService.removeRecursively(sessionWorkspace);
        }
        List<IDelegatedAction> actions = cleanupActions.remove(userSessionToken);
        if (actions != null)
        {
            for (IDelegatedAction action : actions)
            {
                action.execute();
            }
        }

        getPutDataSetService().cleanupSession(userSessionToken);
    }

    @Override
    public void addCleanupAction(String userSessionToken, IDelegatedAction action)
    {
        List<IDelegatedAction> actions = cleanupActions.get(userSessionToken);
        if (actions == null)
        {
            actions = new ArrayList<>();
            cleanupActions.put(userSessionToken, actions);
        }
        actions.add(action);
    }

    @Override
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<ISearchDomainService> provider = pluginTaskInfoProvider.getSearchDomainServiceProvider();
        List<DatastoreServiceDescription> pluginDescriptions = provider.getPluginDescriptions();
        List<SearchDomain> result = new ArrayList<SearchDomain>();
        for (DatastoreServiceDescription description : pluginDescriptions)
        {
            String name = description.getKey();
            ISearchDomainService service = provider.getPluginInstance(name);
            if (service.isAvailable())
            {
                SearchDomain searchDomain = new SearchDomain();
                searchDomain.setDataStoreCode(config.getDataStoreCode());
                searchDomain.setName(name);
                searchDomain.setLabel(description.getLabel());
                searchDomain.setPossibleSearchOptionsKey(service.getPossibleSearchOptionsKey());
                searchDomain.setPossibleSearchOptions(service.getPossibleSearchOptions());
                result.add(searchDomain);
            }
        }
        return result;
    }

    @Override
    public List<SearchDomainSearchResult> searchForEntitiesWithSequences(String sessionToken,
            String preferredSearchDomainOrNull, String sequenceSnippet,
            Map<String, String> optionalParametersOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<ISearchDomainService> provider = pluginTaskInfoProvider.getSearchDomainServiceProvider();
        DatastoreServiceDescription serviceDescription = findSearchDomainService(provider, preferredSearchDomainOrNull);
        if (serviceDescription != null)
        {
            ISearchDomainService service = provider.getPluginInstance(serviceDescription.getKey());
            List<SearchDomainSearchResult> result = service.search(sequenceSnippet, optionalParametersOrNull);
            SearchDomain searchDomain = new SearchDomain();
            searchDomain.setDataStoreCode(config.getDataStoreCode());
            searchDomain.setName(serviceDescription.getKey());
            searchDomain.setLabel(serviceDescription.getLabel());
            for (SearchDomainSearchResult sequenceSearchResult : result)
            {
                sequenceSearchResult.setSearchDomain(searchDomain);
            }
            return result;
        }
        operationLog.warn("No available sequence database found.");
        return new ArrayList<SearchDomainSearchResult>();
    }

    private DatastoreServiceDescription findSearchDomainService(PluginTaskProvider<ISearchDomainService> provider,
            String preferredSequenceDatabaseOrNull)
    {
        List<DatastoreServiceDescription> pluginDescriptions = provider.getPluginDescriptions();
        Collections.sort(pluginDescriptions, new SimpleComparator<DatastoreServiceDescription, String>()
            {
                @Override
                public String evaluate(DatastoreServiceDescription item)
                {
                    return item.getKey();
                }
            });
        DatastoreServiceDescription availableService = null;
        for (DatastoreServiceDescription description : pluginDescriptions)
        {
            ISearchDomainService service = provider.getPluginInstance(description.getKey());
            if (service.isAvailable())
            {
                if (description.getKey().equals(preferredSequenceDatabaseOrNull))
                {
                    return description;
                }
                if (availableService == null)
                {
                    availableService = description;
                }
            }
        }
        return availableService;
    }

    @Override
    public PutDataSetService getPutDataSetService()
    {
        if (putService == null)
        {
            this.putService =
                    new PutDataSetService(ServiceProvider.getOpenBISService(), operationLog);
            putService.setStoreDirectory(storeRoot);
        }
        return putService;
    }

    private IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    public void setConfig(IConfigProvider config)
    {
        this.config = config;
    }

    @Override
    public TableModel internalCreateReportFromDatasets(String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull)
    {
        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        IShareIdManager manager = getShareIdManager();
        try
        {
            String dssCode = config.getDataStoreCode();

            for (DatasetDescription dataSet : datasets)
            {
                if (dssCode.equals(dataSet.getDataStoreCode()))
                {
                    manager.lock(dataSet.getDataSetCode());
                }
            }
            IMailClient mailClient = createEMailClient();
            return task.createReport(
                    datasets,
                    new DataSetProcessingContext(getHierarchicalContentProvider(),
                            new DataSetDirectoryProvider(storeRoot, manager),
                            new SessionWorkspaceProvider(pluginTaskInfoProvider
                                    .getSessionWorkspaceRootDir(), userSessionToken),
                            new HashMap<String, String>(), mailClient, userId, userEmailOrNull,
                            userSessionToken));

        } finally
        {
            manager.releaseLocks();
        }
    }

    @Override
    public TableModel internalCreateReportFromAggregationService(String userSessionToken,
            String serviceKey, Map<String, Object> parameters, String userId, String userEmailOrNull)
    {
        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        IShareIdManager manager = getShareIdManager();
        try
        {
            IMailClient mailClient = createEMailClient();
            return task.createAggregationReport(
                    parameters,
                    new DataSetProcessingContext(getHierarchicalContentProvider(),
                            new DataSetDirectoryProvider(storeRoot, manager),
                            new SessionWorkspaceProvider(pluginTaskInfoProvider
                                    .getSessionWorkspaceRootDir(), userSessionToken),
                            new HashMap<String, String>(), mailClient, userId, userEmailOrNull,
                            userSessionToken));

        } finally
        {
            manager.releaseLocks();
        }
    }
}
