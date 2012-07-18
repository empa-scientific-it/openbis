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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.DssUniqueFilenameGenerator;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DefaultDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.IOmniscientEntityRegistrator;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDbModifyingAggregationService<T extends DataSetInformation> extends
        AbstractAggregationServiceReportingPlugin implements IOmniscientEntityRegistrator<T>
{

    private static final String AGGREGATION_SERVICE_SCRATCH_DIR_NAME = "aggregation-service";

    private static final long serialVersionUID = 1L;

    private static IMailClient getMailClientFromProperties(Properties dssProperties)
    {
        Properties mailProperties = Parameters.createMailProperties(dssProperties);
        return new MailClient(mailProperties);
    }

    private final Properties dssProperties;

    // openBisService may be initialized lazily
    private IEncapsulatedOpenBISService openBisService;

    private final IMailClient mailClient;

    private final DssUniqueFilenameGenerator filenameGenerator;

    /**
     * Constructor for the AbstractDbModifyingAggegation service. This constructor is used by the
     * ReportingPluginTaskFactory.
     * 
     * @param properties
     * @param storeRoot
     */
    public AbstractDbModifyingAggregationService(Properties properties, File storeRoot)
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
    public AbstractDbModifyingAggregationService(Properties dssProperties,
            Properties instanceProperties, File storeRoot)
    {
        this(dssProperties, instanceProperties, storeRoot, null,
                getMailClientFromProperties(dssProperties));
    }

    /**
     * Internal constructor that allows explicit configuration of all services. Used in testing.
     * 
     * @param dssProperties
     * @param instanceProperties
     * @param storeRoot
     * @param openBisService
     * @param mailClient
     */
    public AbstractDbModifyingAggregationService(Properties dssProperties,
            Properties instanceProperties, File storeRoot,
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient)
    {
        super(instanceProperties, storeRoot);
        this.dssProperties = dssProperties;
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        filenameGenerator =
                new DssUniqueFilenameGenerator(getClass().getSimpleName(), "mock-file",
                        "serialized");
    }

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
     * Directory used for the fake incoming files used by the infrastructure. These fake files are
     * necessary because much of the infrastructure assumes the existance of a file in a dropbox.
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
            throws IOException
    {
        // Create a file that represents the parameters
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

        File dssInternalTempDir = DssPropertyParametersUtil.getDssInternalTempDir(dssProperties);
        File dssRegistrationLogDir =
                DssPropertyParametersUtil.getDssRegistrationLogDir(dssProperties);
        File dssRecoveryStateDir = DssPropertyParametersUtil.getDssRecoveryStateDir(dssProperties);
        String dssCode = DssPropertyParametersUtil.getDataStoreCode(dssProperties);
        String shareId = getShareId();
        DataSetValidator dataSetValidator = new DataSetValidator(dssProperties);
        DataSourceQueryService dataSourceQueryService = new DataSourceQueryService();
        ThreadParameters threadParameters = createThreadParameters();

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState(dssCode, shareId, storeRoot,
                        dssInternalTempDir, dssRegistrationLogDir, dssRecoveryStateDir,
                        getOpenBisService(), mailClient, dataSetValidator, dataSourceQueryService,
                        new DynamicTransactionQueryFactory(), shouldNotifySuccessfulRegistration(),
                        threadParameters, new DataSetStorageRecoveryManager());

        return globalState;
    }

    protected ThreadParameters createThreadParameters()
    {
        Properties threadParameterProperties = new Properties();
        threadParameterProperties.put(ch.systemsx.cisd.etlserver.ThreadParameters.INCOMING_DIR,
                getMockIncomingDir().getAbsolutePath());
        return new ThreadParameters(threadParameterProperties, this.getClass().getName());
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
        // hard coded to share 1 -- could be made configurable in the future.
        return "1";
    }

    protected boolean shouldNotifySuccessfulRegistration()
    {
        return false;
    }

    protected TableModel errorTableModel(Map<String, Object> parameters, Exception e)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("Parameters");
        builder.addHeader("Error");
        IRowBuilder row = builder.addRow();
        row.setCell("Parameters", parameters.toString());
        row.setCell("Error", e.getMessage());
        return builder.getTableModel();
    }

    protected void logInvocationError(Map<String, Object> parameters, Exception e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Error producing aggregation report\n");
        sb.append("Class: ");
        sb.append(getClass().getName());
        sb.append("\n");
        sb.append("Parameters: ");
        sb.append(parameters);

        operationLog.error(sb.toString(), e);
    }
}
