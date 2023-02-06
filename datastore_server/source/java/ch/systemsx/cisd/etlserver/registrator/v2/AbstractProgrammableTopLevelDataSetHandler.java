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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.python.core.PyException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DistinctExceptionsCollection;
import ch.systemsx.cisd.etlserver.registrator.MarkerFileUtility;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.etlserver.registrator.api.v2.DataSetRegistrationTransactionV2Delegate;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonDataSetRegistrationServiceV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2.ProgrammableDropboxObjectFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AbstractTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor.DssRegistrationHealthState;
import ch.systemsx.cisd.etlserver.registrator.recovery.AbstractRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStoragePrecommitRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo.RecoveryStage;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner.IPrePostRegistrationHook;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractProgrammableTopLevelDataSetHandler<T extends DataSetInformation>
        extends AbstractOmniscientTopLevelDataSetRegistrator<T>
{
    private final int processMaxRetryCount;

    private final int processRetryPauseInSec;

    /**
     * @param globalState
     */
    protected AbstractProgrammableTopLevelDataSetHandler(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        this.processMaxRetryCount = globalState.getThreadParameters().getProcessMaxRetryCount();
        this.processRetryPauseInSec = globalState.getThreadParameters().getProcessRetryPauseInSec();
    }

    @Override
    public abstract boolean shouldNotAddToFaultyPathsOrNull(File file);

    @Override
    abstract protected void handleDataSet(DataSetFile dataSetFile,
            DataSetRegistrationService<T> service) throws Throwable;

    protected void executeProcessFunctionWithRetries(IJavaDataSetRegistrationDropboxV2 v2Programm,
            JythonDataSetRegistrationServiceV2<T> service, DataSetFile incomingDataSetFile)
    {
        DssRegistrationLogger logger = service.getDssRegistrationLog();

        DistinctExceptionsCollection errors = new DistinctExceptionsCollection();

        // create initial transaction
        service.transaction();

        while (true)
        {
            waitUntilApplicationIsReady(service, incomingDataSetFile);

            Exception problem;
            try
            {
                logger.info(operationLog, "Start processing");
                v2Programm.process(wrapTransaction(service.getTransaction()));
                // if function succeeded - than we are happy
                return;
            } catch (Exception ex)
            {
                problem = ex;

                logger.info(
                        operationLog
                        , "Exception occured during jython script processing. Will check if can retry.",
                        ex);
            }

            int errorCount = errors.add(problem);

            if (errorCount > processMaxRetryCount)
            {
                logger.error(operationLog, "The jython script processing has failed too many times. Rolling back.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            } else
            {
                logger.info(operationLog, "The same error happened for the " + errorCount
                        + " time (max allowed is " + processMaxRetryCount + ")");
            }

            DataSetRegistrationContext registrationContext =
                    service.getTransaction().getRegistrationContext();

            boolean retryFunctionResult = false;
            try
            {
                retryFunctionResult =
                        v2Programm.shouldRetryProcessing(registrationContext, problem);
            } catch (Exception ex)
            {
                logger.error(operationLog, "The retry function has failed. Rolling back.", ex);
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }

            if (false == retryFunctionResult)
            {
                logger.error(operationLog, "The should_retry_processing function returned false. Will not retry.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            }

            service.rollbackAndForgetTransaction();
            // TODO: now the transaction is rolled back and everything should be in place again.
            // should we catch some exceptions here? can we recover if whatever went wrong in here

            // creates the new transaction and propagates the values in the persistent map
            service.transaction().getRegistrationContext().getPersistentMap()
                    .putAll(registrationContext.getPersistentMap());

            waitTheRetryPeriod(processRetryPauseInSec);
        }
    }

    /**
     * Wraps the transaction - to hide methods which we don't want to expose in the api.
     */
    protected IDataSetRegistrationTransactionV2 wrapTransaction(
            DataSetRegistrationTransaction<T> transaction)
    {
        IDataSetRegistrationTransactionV2 v2transaction =
                new DataSetRegistrationTransactionV2Delegate(transaction);
        return v2transaction;
    }

    protected void waitUntilApplicationIsReady(DataSetRegistrationService<T> service, DataSetFile incomingDataSetFile)
    {
        DssRegistrationHealthState healthState;

        while ((healthState = DssRegistrationHealthMonitor.getInstance().checkHealthState(
                incomingDataSetFile.getRealIncomingFile().getParentFile())).isUnavailable())
        {
            service.getDssRegistrationLog().info(operationLog,
                    "Cannot process unless filesystems and application server are available. Reason: " + healthState);
            waitTheRetryPeriod(10);
            // do nothing. just repeat until the application is ready
        }
    }

    protected void waitTheRetryPeriod(int retryPeriod)
    {
        ConcurrencyUtilities.sleep(retryPeriod * 1000); // in seconds
    }

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
    {
        try
        {
            getV2DropboxProgram(service).rollbackPreRegistration(
                    transaction.getRegistrationContext(), ex);
        } catch (NotImplementedException e)
        {
            // silently ignore if function is not implemented
        }

        super.didRollbackTransaction(service, transaction, algorithmRunner, ex);
    }

    @Override
    public void didCommitTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction)
    {
        super.didCommitTransaction(service, transaction);
        try
        {
            getV2DropboxProgram(service).postStorage(transaction.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            // silently ignore if function is not implemented
        }
    }

    @Override
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        super.didPreRegistration(service, registrationContextHolder);
        try
        {
            getV2DropboxProgram(service).preMetadataRegistration(
                    registrationContextHolder.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            // ignore
        }
    }

    @Override
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        super.didPostRegistration(service, registrationContextHolder);
        try
        {
            getV2DropboxProgram(service).postMetadataRegistration(
                    registrationContextHolder.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            // ignore
        }
    }

    @Override
    protected void handleRecovery(final File incomingFileOriginal)
    {
        // get the marker file
        final File recoveryMarkerFile =
                state.getGlobalState().getStorageRecoveryManager()
                        .getProcessingMarkerFile(incomingFileOriginal);

        // deserialize recovery state
        final AbstractRecoveryState<T> recoveryState =
                state.getGlobalState().getStorageRecoveryManager()
                        .extractRecoveryCheckpoint(recoveryMarkerFile);

        // then we should ensure that the recovery will actually take place itself!
        final DataSetStorageRecoveryInfo recoveryInfo =
                state.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(recoveryMarkerFile);

        final File recoveryFile = recoveryInfo.getRecoveryStateFile();

        if (false == recoveryFile.exists())
        {
            operationLog.error("Recovery file does not exist. " + recoveryFile);

            throw new IllegalStateException("Recovery file " + recoveryFile + " doesn't exist");
        }

        if (false == retryPeriodHasPassed(recoveryInfo))
        {
            return;
        }

        operationLog.info("Will recover from broken registration. Found marker file "
                + recoveryMarkerFile + " and " + recoveryFile);

        final DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        logger.log("Starting recovery at checkpoint " + recoveryInfo.getRecoveryStage());

        IRecoveryCleanupDelegate recoveryMarkerFileCleanupAction = new IRecoveryCleanupDelegate()
            {
                @Override
                public void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount)
                {
                    if (false == shouldStopRecovery
                            && recoveryInfo.getTryCount() >= state.getGlobalState()
                                    .getStorageRecoveryManager().getMaximumRertyCount())
                    {
                        notificationLog.error("The dataset "
                                + recoveryState.getIncomingDataSetFile().getRealIncomingFile()
                                + " has failed to register. Giving up.");
                        deleteMarkerFile();

                        File errorRecoveryMarkerFile =
                                new File(recoveryMarkerFile.getParent(),
                                        recoveryMarkerFile.getName() + ".ERROR");
                        state.getFileOperations().move(recoveryMarkerFile, errorRecoveryMarkerFile);

                        logger.info(operationLog, "Recovery failed. Giving up.");
                        logger.registerFailure();

                    } else
                    {
                        if (shouldStopRecovery)
                        {
                            deleteMarkerFile();

                            recoveryMarkerFile.delete();
                            recoveryFile.delete();
                        } else
                        {
                            // this replaces the recovery file with a new one with increased
                            // count
                            // FIXME: is this safe operation (how to assure, that it won't
                            // corrupt the recoveryMarkerFile?)
                            DataSetStorageRecoveryInfo rInfo =
                                    state.getGlobalState().getStorageRecoveryManager()
                                            .getRecoveryFileFromMarker(recoveryMarkerFile);
                            if (shouldIncreaseTryCount)
                            {
                                rInfo.increaseTryCount();
                            }
                            rInfo.setLastTry(new Date());
                            rInfo.writeToFile(recoveryMarkerFile);
                        }
                    }
                }

                private void deleteMarkerFile()
                {
                    File incomingMarkerFile =
                            MarkerFileUtility.getMarkerFileFromIncoming(recoveryState
                                    .getIncomingDataSetFile().getRealIncomingFile());
                    if (incomingMarkerFile.exists())
                    {

                        incomingMarkerFile.delete();
                    }
                }
            };

        PostRegistrationCleanUpAction cleanupAction =
                new PostRegistrationCleanUpAction(recoveryState.getIncomingDataSetFile(),
                        new DoNothingDelegatedAction(), getGlobalState());

        handleRecoveryState(recoveryInfo.getRecoveryStage(), recoveryState, cleanupAction,
                recoveryMarkerFileCleanupAction);
    }

    /**
     * Check wheter the last retry + retry period < date.now
     */
    private boolean retryPeriodHasPassed(final DataSetStorageRecoveryInfo recoveryInfo)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(recoveryInfo.getLastTry());
        c.add(Calendar.SECOND, state.getGlobalState().getStorageRecoveryManager()
                .getRetryPeriodInSeconds());
        return c.getTime().before(new Date());
    }

    private void handleRecoveryState(RecoveryStage recoveryStage,
            final AbstractRecoveryState<T> recoveryState,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            final IRecoveryCleanupDelegate recoveryMarkerCleanup)
    {

        final DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        // keeps track of whether we should keep or delete the recovery files.
        // we can delete if succesfully recovered, or rolledback.
        // This code is not executed at all in case of a recovery give-up
        boolean shouldStopRecovery = false;

        // by default in case of failure we increase try count
        boolean shouldIncreaseTryCount = true;

        IRollbackDelegate<T> rollbackDelegate = new IRollbackDelegate<T>()
            {
                @Override
                public void didRollbackStorageAlgorithmRunner(
                        DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex,
                        ErrorType errorType)
                {
                    // do nothing. recovery takes care of everything
                }

                @Override
                public void markReadyForRecovery(DataSetStorageAlgorithmRunner<T> algorithm,
                        Throwable ex)
                {
                    // don't have to do nothing.
                }
            };

        // hookAdaptor
        RecoveryHookAdaptor hookAdaptor =
                getRecoveryHookAdaptor(recoveryState.getIncomingDataSetFile()
                        .getLogicalIncomingFile());

        DataSetRegistrationContext.IHolder registrationContextHolder =
                new DataSetRegistrationContext.IHolder()
                    {

                        @Override
                        public DataSetRegistrationContext getRegistrationContext()
                        {
                            return new DataSetRegistrationContext(recoveryState.getPersistentMap(),
                                    state.getGlobalState(), null);
                        }
                    };

        ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms =
                recoveryState.getDataSetStorageAlgorithms(state);

        RollbackStack rollbackStack = recoveryState.getRollbackStack();

        DataSetStorageAlgorithmRunner<T> runner =
                new DataSetStorageAlgorithmRunner<T>(
                        recoveryState.getIncomingDataSetFile(), // incoming
                        dataSetStorageAlgorithms, // algorithms
                        rollbackDelegate, // rollback delegate,
                        rollbackStack, // rollbackstack
                        logger, // registrationLogger
                        state.getGlobalState().getOpenBisService(), // openBisService
                        hookAdaptor, // the hooks
                        state.getGlobalState().getStorageRecoveryManager(),
                        registrationContextHolder, state.getGlobalState());

        boolean registrationSuccessful = false;

        operationLog.info("Recovery succesfully deserialized the state of the registration");
        try
        {
            EntityOperationsState entityOperationsState;

            if (recoveryStage.beforeOrEqual(RecoveryStage.PRECOMMIT))
            {
                TechId registrationId =
                        ((DataSetStoragePrecommitRecoveryState<T>) recoveryState)
                                .getRegistrationId();
                if (registrationId == null)
                {
                    throw new IllegalStateException(
                            "Recovery state cannot have null registrationId at the precommit phase");
                }
                entityOperationsState =
                        state.getGlobalState().getOpenBisService()
                                .didEntityOperationsSucceed(registrationId);
            } else
            {
                // if we are at the later stage than precommit - it means that the entity operations
                // have succeeded
                entityOperationsState = EntityOperationsState.OPERATION_SUCCEEDED;
            }

            if (EntityOperationsState.IN_PROGRESS == entityOperationsState)
            {
                shouldIncreaseTryCount = false;
            } else if (EntityOperationsState.NO_OPERATION == entityOperationsState)
            {
                operationLog
                        .info("Recovery hasn't found registration artifacts in the application server. Registration of metadata was not successful.");

                IRollbackStackDelegate rollbackStackDelegate =
                        new AbstractTransactionState.LiveTransactionRollbackDelegate(state
                                .getGlobalState().getStagingDir());

                rollbackStack.setLockedState(false);

                rollbackStack.rollbackAll(rollbackStackDelegate);
                UnstoreDataAction action =
                        state.getOnErrorActionDecision().computeUndoAction(
                                ErrorType.OPENBIS_REGISTRATION_FAILURE, null);
                DataSetStorageRollbacker rollbacker =
                        new DataSetStorageRollbacker(state, operationLog, action,
                                recoveryState.getIncomingDataSetFile(), null, null,
                                ErrorType.OPENBIS_REGISTRATION_FAILURE);
                operationLog.info(rollbacker.getErrorMessageForLog());
                rollbacker.doRollback(logger);

                logger.log("Operations haven't been registered in AS - recovery rollback");
                logger.registerFailure();

                shouldStopRecovery = true;

                hookAdaptor.executePreRegistrationRollback(registrationContextHolder, null);

                finishRegistration(dataSetStorageAlgorithms, rollbackStack);
            } else
            {

                operationLog
                        .info("Recovery has found datasets in the AS. The registration of metadata was successful.");

                if (recoveryStage.before(RecoveryStage.POST_REGISTRATION_HOOK_EXECUTED))
                {
                    runner.postRegistration();
                }

                boolean success = true;
                if (recoveryStage.before(RecoveryStage.STORAGE_COMPLETED))
                {
                    success = runner.commitAndStore();
                }

                if (success)
                {
                    success = runner.cleanPrecommitAndConfirmStorage();
                }
                if (success)
                {
                    hookAdaptor.executePostStorage(registrationContextHolder);

                    registrationSuccessful = true;
                    shouldStopRecovery = true;

                    logger.registerSuccess();

                    // do the actions performed when the registration comes into terminal state.
                    finishRegistration(dataSetStorageAlgorithms, rollbackStack);

                }
            }
        } catch (Throwable error)
        {
            if ("org.jmock.api.ExpectationError".equals(error.getClass().getCanonicalName()))
            {
                // this exception can by only thrown by tests.
                // propagation of the exception is essential to test some functionalities
                // implemented like this to avoid dependency to jmock in production
                throw (Error) error;
            }
            logger.error(operationLog, "Uncaught error during recovery", error);
            // in this case we should ignore, and run the recovery again after some time
        }

        cleanAfterwardsAction.execute(registrationSuccessful);

        recoveryMarkerCleanup.execute(shouldStopRecovery, shouldIncreaseTryCount);
    }

    private void finishRegistration(ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            RollbackStack rollbackStack)
    {
        for (DataSetStorageAlgorithm<T> algorithm : dataSetStorageAlgorithms)
        {
            algorithm.getStagingFile().delete();
        }
        rollbackStack.discard();
    }

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    @SuppressWarnings("unchecked")
    public IDataSetRegistrationDetailsFactory<T> createObjectFactory(
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return (IDataSetRegistrationDetailsFactory<T>) new ProgrammableDropboxObjectFactory<DataSetInformation>(
                getRegistratorState(), userProvidedDataSetInformationOrNull)
            {
                @Override
                protected DataSetInformation createDataSetInformation()
                {
                    return new DataSetInformation();
                }
            };
    }

    /**
     * Create a registration service.
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationService(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);
    }

    /**
     * Create a registration service.
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationService(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new DataSetRegistrationService<T>(this, incomingDataSetFile,
                this.createObjectFactory(userProvidedDataSetInformationOrNull),
                cleanAfterwardsAction, delegate);
    }

    protected abstract RecoveryHookAdaptor getRecoveryHookAdaptor(File incoming);

    protected abstract IJavaDataSetRegistrationDropboxV2 getV2DropboxProgram(
            DataSetRegistrationService<T> service);

    interface IRecoveryCleanupDelegate
    {
        void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount);
    }

    /**
     * Create an adaptor that offers access to the recovery hook functions.
     */
    protected abstract class RecoveryHookAdaptor implements IPrePostRegistrationHook<T>
    {
        protected abstract IJavaDataSetRegistrationDropboxV2 getV2DropboxProgramInternal();

        protected final File incoming;

        public RecoveryHookAdaptor(File incoming)
        {
            this.incoming = incoming;
        }

        @Override
        public void executePreRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            throw new NotImplementedException("Recovery cannot execute pre-registration hook.");
        }

        @Override
        public void executePostRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            try
            {
                getV2DropboxProgramInternal().postMetadataRegistration(
                        registrationContextHolder.getRegistrationContext());
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }

        /**
         * This method does not belong to the IPrePostRegistrationHook interface. Is called directly by recovery.
         */
        public void executePostStorage(DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            try
            {
                getV2DropboxProgramInternal().postStorage(
                        registrationContextHolder.getRegistrationContext());
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }

        public void executePreRegistrationRollback(
                DataSetRegistrationContext.IHolder registrationContextHolder, Throwable throwable)
        {
            try
            {
                getV2DropboxProgramInternal().rollbackPreRegistration(
                        registrationContextHolder.getRegistrationContext(), throwable);
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }
    }

    @Override
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof PyException)
        {
            return new RuntimeException(throwable.toString());
        }

        return super.asSerializableException(throwable);
    }
}
