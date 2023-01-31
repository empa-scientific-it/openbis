/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithmRunner.IDataSetRegistrationAlgorithmRunnerDelegate;
import ch.systemsx.cisd.etlserver.registrator.MarkerFileUtility;
import ch.systemsx.cisd.etlserver.registrator.TopLevelDataSetChecker;
import ch.systemsx.cisd.etlserver.utils.PostRegistrationExecutor;
import ch.systemsx.cisd.etlserver.utils.PreRegistrationExecutor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * The class that handles the incoming data set.
 * 
 * @author Bernd Rinn
 */
public final class TransferredDataSetHandler extends AbstractTopLevelDataSetRegistrator implements
        IDataSetHandler, IExtensibleDataSetHandler
{

    public static final String TARGET_NOT_RELATIVE_TO_STORE_ROOT =
            "Target path '%s' is not relative to store root directory '%s'.";

    static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            TransferredDataSetHandler.class);

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TransferredDataSetHandler.class);

    public static final NamedDataStrategy ERROR_DATA_STRATEGY = new NamedDataStrategy(
            DataStoreStrategyKey.ERROR);

    private final IStoreRootDirectoryHolder storeRootDirectoryHolder;

    private final IEncapsulatedOpenBISService limsService;

    private final IDataStrategyStore dataStrategyStore;

    private final IDataSetInfoExtractor dataSetInfoExtractor;

    private final IFileOperations fileOperations;

    private final Lock registrationLock;

    private final ITypeExtractor typeExtractor;

    private final IStorageProcessorTransactional storageProcessor;

    private final IMailClient mailClient;

    private final String dssCode;

    private final boolean notifySuccessfulRegistration;

    private final boolean useIsFinishedMarkerFile;

    private boolean stopped = false;

    private boolean deleteUnidentified = false;

    private DatabaseInstance homeDatabaseInstance;

    private final IDataSetHandler dataSetHandler;

    private final IDataSetValidator dataSetValidator;

    private final IPreRegistrationAction preRegistrationAction;

    private final IPostRegistrationAction postRegistrationAction;

    private final MarkerFileUtility markerFileUtility;

    private static class IdentificationTrackingRunnerDelegate implements
            IDataSetRegistrationAlgorithmRunnerDelegate
    {
        private boolean didNotIdentifyDataSet = false;

        @Override
        public void didNotIdentifyDataSet()
        {
            didNotIdentifyDataSet = true;
        }

    }

    /**
     * The designated constructor.
     * 
     * @param globalState
     */
    public TransferredDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        this(globalState, ETLServerPluginFactory.getPluginForThread(globalState
                .getThreadParameters()));
    }

    /**
     * A constructor used for testing.
     * 
     * @param globalState
     * @param plugin
     */
    TransferredDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
            IETLServerPlugin plugin)
    {
        super(globalState);

        plugin.getStorageProcessor().setStoreRootDirectory(globalState.getStoreRootDir());

        this.dssCode = globalState.getDssCode();
        assert dssCode != null : "Unspecified data store code";

        assert plugin != null : "IETLServerPlugin implementation can not be null.";

        storeRootDirectoryHolder = plugin.getStorageProcessor();
        assert storeRootDirectoryHolder != null : "Given store root directory holder can not be null.";

        this.limsService = globalState.getOpenBisService();
        assert limsService != null : "IEncapsulatedLimsService implementation can not be null.";

        this.mailClient = globalState.getMailClient();
        assert mailClient != null : "IMailClient implementation can not be null.";

        this.dataSetInfoExtractor = plugin.getDataSetInfoExtractor();
        this.typeExtractor = plugin.getTypeExtractor();
        this.storageProcessor = plugin.getStorageProcessor();
        dataSetHandler = plugin.getDataSetHandler(this, limsService);
        if (dataSetHandler instanceof IDataSetHandlerWithMailClient)
        {
            ((IDataSetHandlerWithMailClient) dataSetHandler).initializeMailClient(mailClient);
        }
        this.dataSetValidator = globalState.getDataSetValidator();
        this.dataStrategyStore = new DataStrategyStore(this.limsService, mailClient);
        this.notifySuccessfulRegistration = globalState.isNotifySuccessfulRegistration();
        this.registrationLock = new ReentrantLock();
        this.fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        this.useIsFinishedMarkerFile = globalState.isUseIsFinishedMarkerFile();
        this.deleteUnidentified = globalState.isDeleteUnidentified();
        this.preRegistrationAction =
                PreRegistrationExecutor.create(globalState.getPreRegistrationScript());
        this.postRegistrationAction =
                PostRegistrationExecutor.create(globalState.getPostRegistrationScript());

        this.markerFileUtility =
                new MarkerFileUtility(operationLog, notificationLog, fileOperations,
                        storeRootDirectoryHolder);
    }

    /**
     * Returns the lock one needs to hold before one interrupts a data set registration.
     */
    @Override
    public Lock getRegistrationLock()
    {
        return registrationLock;
    }

    //
    // IPathHandler
    //

    @Override
    public final void handle(final File file)
    {
        if (stopped)
        {
            return;
        }
        dataSetHandler.handleDataSet(file);
    }

    @Override
    public void handle(File file, String userSessionToken,
            DataSetInformation callerDataSetInformation, ITopLevelDataSetRegistratorDelegate delegate)
    {
        if (stopped)
        {
            return;
        }
        final DataSetRegistrationHelper registrationHelper = createRegistrationHelper(file);
        IdentificationTrackingRunnerDelegate runnerDelegate =
                new IdentificationTrackingRunnerDelegate();
        List<DataSetInformation> registeredDataSets =
                new DataSetRegistrationAlgorithmRunner(registrationHelper, runnerDelegate)
                        .runAlgorithm();

        if (runnerDelegate.didNotIdentifyDataSet)
        {
            throw new UserFailureException("No owning sample or experiment specified for data set");
        }
        if (false == registeredDataSets.isEmpty())
        {
            delegate.didRegisterDataSets(registeredDataSets);
        }
    }

    @Override
    public List<DataSetInformation> handleDataSet(final File dataSet)
    {
        final DataSetRegistrationHelper registrationHelper = createRegistrationHelper(dataSet);
        return new DataSetRegistrationAlgorithmRunner(registrationHelper).runAlgorithm();
    }

    @Override
    public List<DataSetInformation> handleDataSet(final File dataSet,
            DataSetInformation dataSetInformation,
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator)
    {
        dataSetInformation.setInstanceUUID(getHomeDatabaseInstance().getUuid());
        final DataSetRegistrationHelper registrationHelper =
                createRegistrationHelper(dataSet, dataSetInformation, registrator);
        return new DataSetRegistrationAlgorithmRunner(registrationHelper).runAlgorithm();
    }

    @Override
    public boolean isStopped()
    {
        return stopped;
    }

    //
    // ISelfTestable
    //

    @Override
    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        new TopLevelDataSetChecker(operationLog, storeRootDirectoryHolder, fileOperations)
                .runCheck();
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = limsService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    //
    // Helper class
    //

    private DataSetRegistrationHelper createRegistrationHelper(File file)
    {
        if (useIsFinishedMarkerFile)
        {
            return createRegistrationHelperWithIsFinishedFile(file, null, null);
        } else
        {
            return createRegistrationHelperWithQuietPeriodFilter(file, null, null);
        }
    }

    private DataSetRegistrationHelper createRegistrationHelper(File dataSet,
            DataSetInformation dataSetInformation,
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator)
    {
        if (useIsFinishedMarkerFile)
        {
            return createRegistrationHelperWithIsFinishedFile(dataSet, dataSetInformation,
                    registrator);
        } else
        {
            return createRegistrationHelperWithQuietPeriodFilter(dataSet, dataSetInformation,
                    registrator);
        }
    }

    private DataSetRegistrationHelper createRegistrationHelperWithIsFinishedFile(
            final File isFinishedFile, final DataSetInformation dsInfo,
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator)
    {
        assert isFinishedFile != null : "Unspecified is-finished file.";
        final String name = isFinishedFile.getName();
        assert name.startsWith(FileConstants.IS_FINISHED_PREFIX) : "A finished file must starts with '"
                + FileConstants.IS_FINISHED_PREFIX + "'.";

        File incomingDataSetFile = getIncomingDataSetPathFromMarker(isFinishedFile);
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new AbstractDelegatedActionWithResult<Boolean>(false)
                    {
                        @Override
                        public Boolean execute()
                        {
                            return deleteAndLogIsFinishedMarkerFile(isFinishedFile);
                        }
                    };
        if (null != registrator)
        {
            return new OverridingRegistrationHelper(this, incomingDataSetFile, getGlobalState()
                    .getShareId(), cleanAftrewardsAction, preRegistrationAction,
                    postRegistrationAction, registrator)
                {
                    @Override
                    protected DataSetInformation extractDataSetInformation(File incomingDataSetPath)
                    {
                        return dsInfo;
                    }

                };
        } else
        {
            return new RegistrationHelper(this, incomingDataSetFile, getGlobalState().getShareId(),
                    cleanAftrewardsAction, preRegistrationAction, postRegistrationAction);
        }
    }

    private DataSetRegistrationHelper createRegistrationHelperWithQuietPeriodFilter(
            File incomingDataSetFile, final DataSetInformation dsInfo,
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator)
    {
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new AbstractDelegatedActionWithResult<Boolean>(true);
        if (registrator != null)
        {
            return new OverridingRegistrationHelper(this, incomingDataSetFile, getGlobalState()
                    .getShareId(), cleanAftrewardsAction, preRegistrationAction,
                    postRegistrationAction, registrator)
                {
                    @Override
                    protected DataSetInformation extractDataSetInformation(File incomingDataSetPath)
                    {
                        return dsInfo;
                    }

                };
        } else
        {
            return new RegistrationHelper(this, incomingDataSetFile, getGlobalState().getShareId(),
                    cleanAftrewardsAction, preRegistrationAction, postRegistrationAction);
        }
    }

    /**
     * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
     * 
     * @return <code>null</code> if a problem has happened. Otherwise a useful and usable incoming data set path is returned.
     */
    private final File getIncomingDataSetPathFromMarker(final File isFinishedPath)
    {
        return markerFileUtility.getIncomingDataSetPathFromMarker(isFinishedPath);
    }

    private boolean deleteAndLogIsFinishedMarkerFile(File isFinishedFile)
    {
        return markerFileUtility.deleteAndLogIsFinishedMarkerFile(isFinishedFile);
    }

    private class RegistrationHelper extends DataSetRegistrationHelper
    {

        RegistrationHelper(TransferredDataSetHandler transferredDataSetHandler,
                File incomingDataSetFile, String shareId,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction)
        {
            super(incomingDataSetFile, shareId, cleanAftrewardsAction, preRegistrationAction,
                    postRegistrationAction);
        }

        RegistrationHelper(TransferredDataSetHandler transferredDataSetHandler,
                File incomingDataSetFile, String shareId,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction,
                IDataSetInApplicationServerRegistrator appServerRegistrator)
        {
            super(incomingDataSetFile, shareId, cleanAftrewardsAction, preRegistrationAction,
                    postRegistrationAction, appServerRegistrator);
        }

        // state accessors
        @Override
        protected IEncapsulatedOpenBISService getOpenBisService()
        {
            return limsService;
        }

        @Override
        protected ITypeExtractor getTypeExtractor()
        {
            return typeExtractor;
        }

        @Override
        protected IStorageProcessorTransactional getStorageProcessor()
        {
            return storageProcessor;
        }

        @Override
        protected IDataSetInfoExtractor getDataSetInfoExtractor()
        {
            return dataSetInfoExtractor;
        }

        @Override
        protected DatabaseInstance getHomeDatabaseInstance()
        {
            return TransferredDataSetHandler.this.getHomeDatabaseInstance();
        }

        @Override
        protected Logger getOperationLog()
        {
            return operationLog;
        }

        @Override
        protected Logger getNotificationLog()
        {
            return notificationLog;
        }

        @Override
        protected IMailClient getMailClient()
        {
            return mailClient;
        }

        @Override
        protected IFileOperations getFileOperations()
        {
            return fileOperations;
        }

        @Override
        protected String getDataStoreCode()
        {
            return dssCode;
        }

        @Override
        protected IDataStrategyStore getDataStrategyStore()
        {
            return dataStrategyStore;
        }

        @Override
        protected IDataSetValidator getDataSetValidator()
        {
            return dataSetValidator;
        }

        @Override
        protected Lock getRegistrationLock()
        {
            return TransferredDataSetHandler.this.getRegistrationLock();
        }

        @Override
        protected boolean shouldDeleteUnidentified()
        {
            return deleteUnidentified;
        }

        @Override
        protected boolean shouldNotifySuccessfulRegistration()
        {
            return notifySuccessfulRegistration;
        }

        @Override
        protected void rollback(final Throwable throwable) throws Error
        {
            stopped |= throwable instanceof InterruptedExceptionUnchecked;

            new DataSetRegistrationRollbacker(stopped, registrationAlgorithm, incomingDataSetFile,
                    notificationLog, operationLog, throwable).doRollback();
        }
    }

    private class OverridingRegistrationHelper extends RegistrationHelper
    {

        /**
         * @param transferredDataSetHandler
         * @param incomingDataSetFile
         * @param cleanAftrewardsAction
         * @param postRegistrationAction
         */
        OverridingRegistrationHelper(TransferredDataSetHandler transferredDataSetHandler,
                File incomingDataSetFile, String shareId,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction,
                DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator)
        {
            super(transferredDataSetHandler, incomingDataSetFile, shareId, cleanAftrewardsAction,
                    preRegistrationAction, postRegistrationAction, registrator);
        }

        @Override
        protected boolean shouldDeleteUnidentified()
        {
            return true;
        }

        @Override
        protected boolean shouldNotifySuccessfulRegistration()
        {
            return false;
        }
    }

    @Override
    public File getStoreRootDir()
    {
        return getGlobalState().getStoreRootDir();
    }

    /**
     * Any path can be added to faulty paths as far as we are concerned.
     */
    @Override
    public boolean shouldNotAddToFaultyPathsOrNull(File storeItem)
    {
        return false;
    }
}
