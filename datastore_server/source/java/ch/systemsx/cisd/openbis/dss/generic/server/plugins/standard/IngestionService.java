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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.action.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.DssUniqueFilenameGenerator;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext.IHolder;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v2.DataSetRegistrationTransactionV2Delegate;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.v2.DefaultDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetOnErrorActionDecision;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.v2.IOmniscientEntityRegistrator;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class IngestionService<T extends DataSetInformation> extends AggregationService
        implements IOmniscientEntityRegistrator<T>
{

    private static final String AGGREGATION_SERVICE_SCRATCH_DIR_NAME = "aggregation-service";

    private static final String AGGREGATION_SERVICE_SHARE_ID = "share-id";

    private static final long serialVersionUID = 1L;

    private static IMailClient getMailClientFromProperties(Properties dssProperties)
    {
        Properties mailProperties = Parameters.createMailProperties(dssProperties);
        return new MailClient(mailProperties);
    }

    private final Properties dssProperties;

    // openBisService may be initialized lazily
    private IEncapsulatedOpenBISService openBisService;

    private final File dssInternalTempDir;

    private final File dssRegistrationLogDir;

    private final File dssRecoveryStateDir;

    private final File dssRecoveryMarkerDir;

    private final String dssCode;

    private final IMailClient mailClient;

    /**
     * Constructor for the AbstractDbModifyingAggegation service. This constructor is used by the ReportingPluginTaskFactory.
     * 
     * @param properties
     * @param storeRoot
     */
    public IngestionService(Properties properties, File storeRoot)
    {
        this(DssPropertyParametersUtil.loadServiceProperties(), properties, storeRoot);
    }

    /**
     * Internal constructor that uses the full DSS properties.
     * 
     * @param dssProperties
     * @param instanceProperties
     * @param storeRoot
     */
    public IngestionService(Properties dssProperties, Properties instanceProperties, File storeRoot)
    {
        this(dssProperties, instanceProperties, storeRoot, null,
                getMailClientFromProperties(dssProperties));
    }

    private String shareId;

    /**
     * Internal constructor that allows explicit configuration of all services. Used in testing.
     * 
     * @param dssProperties
     * @param instanceProperties
     * @param storeRoot
     * @param openBisService
     * @param mailClient
     */
    public IngestionService(Properties dssProperties, Properties instanceProperties,
            File storeRoot, IEncapsulatedOpenBISService openBisService, IMailClient mailClient)
    {
        super(instanceProperties, storeRoot);
        this.dssProperties = dssProperties;
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        this.shareId = instanceProperties.getProperty(AGGREGATION_SERVICE_SHARE_ID);
        this.dssInternalTempDir = DssPropertyParametersUtil.getDssInternalTempDir(dssProperties);
        this.dssRegistrationLogDir =
                DssPropertyParametersUtil.getDssRegistrationLogDir(dssProperties);
        this.dssRecoveryStateDir = DssPropertyParametersUtil.getDssRecoveryStateDir(dssProperties);
        this.dssRecoveryMarkerDir = DssPropertyParametersUtil.getDssRecoveryMarkerDir(dssProperties);
        this.dssCode = DssPropertyParametersUtil.getDataStoreCode(dssProperties);
    }

    @Override
    public final TableModel createAggregationReport(Map<String, Object> parameters,
            DataSetProcessingContext context)
    {
        DataSetRegistrationService<T> service = null;
        try
        {
            service = createRegistrationService(parameters);
            if (context.trySessionToken() != null)
            {
                service.setUserSessionToken(context.trySessionToken());
            }
            IDataSetRegistrationTransactionV2 transaction = createTransaction(service);
            TableModel tableModel = process(transaction, parameters, context);

            service.commit();

            if (service.encounteredErrors.size() > 0)
            {
                throw service.encounteredErrors.get(0);
            }

            return tableModel;
        } catch (Throwable e)
        {
            if (service != null)
            {
                service.abort(e);
            }
            logInvocationError(parameters, e);
            return errorTableModel(parameters, e);
        } finally
        {
            if (service != null)
            {
                service.cleanAfterRegistrationIfNecessary();
            }
        }
    }

    /**
     * Create a transaction, wrapped in a delegate. Subclasses may override.
     */
    protected IDataSetRegistrationTransactionV2 createTransaction(
            DataSetRegistrationService<T> service)
    {
        return new DataSetRegistrationTransactionV2Delegate(service.transaction());
    }

    /**
     * Do the processing using the user-provided parameters. Subclasses must implement.
     */
    protected abstract TableModel process(IDataSetRegistrationTransactionV2 transaction,
            Map<String, Object> parameters, DataSetProcessingContext context);

    /**
     * Return the share that this service should use to store its data sets.
     * 
     * @return A file that is the root of a share.
     */
    private File getShare()
    {
        return new File(storeRoot, getShareId());
    }

    /**
     * Directory used for scratch by the aggregation service.
     */
    private File getServiceScratchDir()
    {
        return new File(getShare(), AGGREGATION_SERVICE_SCRATCH_DIR_NAME);
    }

    /**
     * Directory used for the fake incoming files used by the infrastructure. These fake files are necessary because much of the infrastructure
     * assumes the existance of a file in a dropbox.
     */
    protected File getMockIncomingDir()
    {
        File incomingDir = new File(getServiceScratchDir(), "incoming");
        if (false == incomingDir.exists())
        {
            incomingDir.mkdirs();
        }
        return incomingDir;
    }

    protected DataSetRegistrationService<T> createRegistrationService(Map<String, Object> parameters)
    {
        // Make sure the health monitor has been initialized
        DssRegistrationHealthMonitor.getInstance(getGlobalState().getOpenBisService(),
                getGlobalState().getRecoveryStateDir());

        // Create a file that represents the parameters
        try
        {
            final File mockIncomingDataSetFile = createMockIncomingFile(parameters);
            DataSetFile incoming = new DataSetFile(mockIncomingDataSetFile);

            // Create a clean-up action
            IDelegatedActionWithResult<Boolean> cleanUpAction =
                    new AbstractDelegatedActionWithResult<Boolean>(true)
                        {

                            @Override
                            public Boolean execute()
                            {
                                mockIncomingDataSetFile.delete();
                                return true;
                            }
                        };

            DataSetRegistrationPreStagingBehavior preStagingUsage =
                    DataSetRegistrationPreStagingBehavior.USE_ORIGINAL;

            NoOpDelegate delegate = new NoOpDelegate(preStagingUsage);

            DataSetRegistrationService<T> service =
                    createRegistrationService(incoming, cleanUpAction, delegate);

            return service;
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    /**
     * Create a registration service using the given parameters. Subclasses may override.
     */
    protected DataSetRegistrationService<T> createRegistrationService(DataSetFile incoming,
            IDelegatedActionWithResult<Boolean> cleanUpAction, NoOpDelegate delegate)
    {
        @SuppressWarnings("unchecked")
        IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory =
                (IDataSetRegistrationDetailsFactory<T>) new DefaultDataSetRegistrationDetailsFactory(
                        getRegistratorState(), null);

        DataSetRegistrationService<T> service =
                new DataSetRegistrationService<T>(this, incoming, registrationDetailsFactory,
                        cleanUpAction, delegate);
        return service;
    }

    /**
     * The file the registration infrastructure should treat as the incoming file in the dropbox.
     */
    protected File createMockIncomingFile(Map<String, Object> parameters) throws IOException
    {
        HashMap<String, Object> parameterHashMap = new HashMap<String, Object>(parameters);
        DssUniqueFilenameGenerator filenameGenerator = new DssUniqueFilenameGenerator(Thread.currentThread().getName(), "file", "serialized");
        File mockIncomingDataSetFile =
                new File(getMockIncomingDir(), filenameGenerator.generateFilename());
        mockIncomingDataSetFile.createNewFile();
        FileUtilities.writeToFile(mockIncomingDataSetFile, parameterHashMap);
        return mockIncomingDataSetFile;
    }

    @Override
    public File getRollBackStackParentFolder()
    {
        return getServiceScratchDir();
    }

    @Override
    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return getRegistratorState().getGlobalState();
    }

    @Override
    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        IStorageProcessorTransactional storageProcessor = createStorageProcessor();
        storageProcessor.setStoreRootDirectory(storeRoot);
        IDataSetOnErrorActionDecision onErrorActionDecision = createOnErrorActionDecision();

        OmniscientTopLevelDataSetRegistratorState registratorState =
                new OmniscientTopLevelDataSetRegistratorState(createGlobalState(),
                        storageProcessor, new ReentrantLock(),
                        FileOperations.getMonitoredInstanceForCurrentThread(),
                        onErrorActionDecision);
        return registratorState;
    }

    private IDataSetOnErrorActionDecision createOnErrorActionDecision()
    {
        return new IDataSetOnErrorActionDecision()
            {
                @Override
                public UnstoreDataAction computeUndoAction(ErrorType errorType,
                        Throwable failureOrNull)
                {
                    return UnstoreDataAction.DELETE;
                }
            };
    }

    /**
     * Create a storage processor for the registration. Subclasses may override.
     */
    protected IStorageProcessorTransactional createStorageProcessor()
    {
        return new DefaultStorageProcessor(properties);
    }

    protected TopLevelDataSetRegistratorGlobalState createGlobalState()
    {
        String localShareId = getShareId();
        DataSetValidator dataSetValidator = new DataSetValidator(dssProperties);
        DataSourceQueryService dataSourceQueryService = new DataSourceQueryService();
        ThreadParameters threadParameters = createThreadParameters();

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState(dssCode, localShareId, storeRoot,
                        dssInternalTempDir, dssRegistrationLogDir, dssRecoveryStateDir,
                        dssRecoveryMarkerDir,
                        getOpenBisService(), mailClient, dataSetValidator, dataSourceQueryService,
                        new DynamicTransactionQueryFactory(), shouldNotifySuccessfulRegistration(),
                        threadParameters, new DataSetStorageRecoveryManager());

        return globalState;
    }

    protected ThreadParameters createThreadParameters()
    {
        Properties threadParameterProperties = new Properties();
        threadParameterProperties.putAll(properties);
        threadParameterProperties.put(ch.systemsx.cisd.etlserver.ThreadParameters.INCOMING_DIR,
                getMockIncomingDir().getAbsolutePath());
        threadParameterProperties.put(ThreadParameters.RECOVERY_DEVELOPMENT_MODE, "true");
        return new ThreadParameters(threadParameterProperties, this.getClass().getSimpleName());
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        if (null != openBisService)
        {
            return openBisService;
        }

        openBisService = ServiceProvider.getOpenBISService();
        return openBisService;
    }

    protected String getShareId()
    {
        if (this.shareId != null)
        {
            return this.shareId;
        } else
        {
            return "1";
        }
    }

    protected boolean shouldNotifySuccessfulRegistration()
    {
        return false;
    }

    /**
     * Callback when a transaction is rolledback. Subclasses may override.
     */
    @Override
    public void didRollbackTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {

    }

    /**
     * Callback when a transaction is committed. Subclasses may override.
     */
    @Override
    public void didCommitTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction)
    {
    }

    /**
     * Callback when a transaction is prepared to be registered. Subclasses may override.
     */
    @Override
    public void didPreRegistration(DataSetRegistrationService<T> service,
            IHolder registrationContextHolder)
    {
    }

    /**
     * Callback when a transaction has been committed with the AS. Subclasses may override.
     */
    @Override
    public void didPostRegistration(DataSetRegistrationService<T> service,
            IHolder registrationContextHolder)
    {
    }

    /**
     * Callback when secondary problems are encountered. Subclasses may override.
     */
    @Override
    public void didEncounterSecondaryTransactionErrors(
            DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
    }
}
