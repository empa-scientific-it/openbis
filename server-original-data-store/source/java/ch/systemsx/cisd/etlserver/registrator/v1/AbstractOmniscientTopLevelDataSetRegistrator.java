/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.v1;

import static ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY;
import static ch.systemsx.cisd.etlserver.ThreadParameters.ON_ERROR_DECISION_KEY;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.AssertionCatchingImmutableCopierWrapper;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.etlserver.AbstractTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.DssUniqueFilenameGenerator;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.PropertiesBasedETLServerPlugin;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.etlserver.registrator.MarkerFileUtility;
import ch.systemsx.cisd.etlserver.registrator.TopLevelDataSetChecker;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Abstract superclass for data set handlers that manage the entire data set registration process themselves.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractOmniscientTopLevelDataSetRegistrator<T extends DataSetInformation>
        extends AbstractTopLevelDataSetRegistrator implements IOmniscientEntityRegistrator<T>
{
    protected static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    /**
     * Object that contains the global state available for data set handlers.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class OmniscientTopLevelDataSetRegistratorState
    {
        private final TopLevelDataSetRegistratorGlobalState globalState;

        private final IStorageProcessorTransactional storageProcessor;

        private final ReentrantLock registrationLock;

        private final IFileOperations fileOperations;

        private final IDataStrategyStore dataStrategyStore;

        private final MarkerFileUtility markerFileUtility;

        private final DatabaseInstance homeDatabaseInstance;

        private final ValidationScriptRunner validationScriptRunner;

        private final IDataSetOnErrorActionDecision onErrorActionDecision;

        public OmniscientTopLevelDataSetRegistratorState(
                TopLevelDataSetRegistratorGlobalState globalState,
                IStorageProcessorTransactional storageProcessor, ReentrantLock registrationLock,
                IFileOperations fileOperations, IDataSetOnErrorActionDecision onErrorActionDecision)
        {
            this.globalState = globalState;
            this.storageProcessor = storageProcessor;
            this.registrationLock = registrationLock;
            this.fileOperations = fileOperations;
            this.dataStrategyStore =
                    new DataStrategyStore(globalState.getOpenBisService(),
                            globalState.getMailClient());
            this.markerFileUtility =
                    new MarkerFileUtility(operationLog, notificationLog, fileOperations,
                            storageProcessor);
            this.homeDatabaseInstance = globalState.getOpenBisService().getHomeDatabaseInstance();
            this.validationScriptRunner =
                    ValidationScriptRunner.createValidatorFromScriptPaths(globalState
                            .getValidationScriptsOrNull());
            this.onErrorActionDecision = onErrorActionDecision;
        }

        public TopLevelDataSetRegistratorGlobalState getGlobalState()
        {
            return globalState;
        }

        public IStorageProcessorTransactional getStorageProcessor()
        {
            return storageProcessor;
        }

        public ReentrantLock getRegistrationLock()
        {
            return registrationLock;
        }

        public IFileOperations getFileOperations()
        {
            return fileOperations;
        }

        public IDataStrategyStore getDataStrategyStore()
        {
            return dataStrategyStore;
        }

        public MarkerFileUtility getMarkerFileUtility()
        {
            return markerFileUtility;
        }

        public DatabaseInstance getHomeDatabaseInstance()
        {
            return homeDatabaseInstance;
        }

        public ValidationScriptRunner getValidationScriptRunner()
        {
            return validationScriptRunner;
        }

        public IDataSetOnErrorActionDecision getOnErrorActionDecision()
        {
            return onErrorActionDecision;
        }
    }

    public static class DoNothingDelegatedAction extends AbstractDelegatedActionWithResult<Boolean>
    {
        public DoNothingDelegatedAction()
        {
            super(true);
        }
    }

    /**
     * The clean-up action after registration.
     * <p>
     * If registration succeeded, the originalInboxFile is deleted. If registration failed, the hardlink copy is deleted, leaving the
     * orignalInboxFile.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class PostRegistrationCleanUpAction extends
            AbstractDelegatedActionWithResult<Boolean>
    {
        private final DataSetFile incoming;

        private final IDelegatedActionWithResult<Boolean> wrappedAction;

        public PostRegistrationCleanUpAction(DataSetFile incoming,
                IDelegatedActionWithResult<Boolean> wrappedAction)
        {
            super(true);
            this.incoming = incoming;
            this.wrappedAction = wrappedAction;
        }

        @Override
        public Boolean execute(boolean didOperationSucceed)
        {
            boolean operationSuccessful = true;
            if (didOperationSucceed)
            {
                // Registration succeeded -- delete original file
                boolean stillExists = incoming.getRealIncomingFile().exists();
                if (false == stillExists)
                {
                    operationLog
                            .warn("Incoming file ["
                                    + incoming.getRealIncomingFile()
                                    + "] was deleted outside of openBIS after processing started. The data had already been registered in the database.");
                } else
                {
                    operationSuccessful =
                            FileUtilities.deleteRecursively(incoming.getRealIncomingFile());
                }

                // Clean up step: Delete pre-staging folder
                FileUtilities.deleteRecursively(incoming.getLogicalIncomingFile().getParentFile());
            } else
            {
                // Registration failed -- remove the copy, leaving the original.
                operationSuccessful =
                        FileUtilities.deleteRecursively(incoming.getLogicalIncomingFile());
            }
            boolean wrappedActionResult = wrappedAction.execute(didOperationSucceed);

            return operationSuccessful && wrappedActionResult;
        }

    }

    public static class NoOpDelegate implements ITopLevelDataSetRegistratorDelegate
    {
        private final DataSetRegistrationPreStagingBehavior preStagingBehavior;

        public NoOpDelegate(DataSetRegistrationPreStagingBehavior preStagingBehavior)
        {
            this.preStagingBehavior = preStagingBehavior;
        }

        @Override
        public void didRegisterDataSets(List<DataSetInformation> dataSetInformations)
        {
        }

        @Override
        public DataSetRegistrationPreStagingBehavior getPrestagingBehavior()
        {
            return preStagingBehavior;
        }

    }

    protected final OmniscientTopLevelDataSetRegistratorState state;

    private boolean stopped;

    /**
     * Constructor.
     * 
     * @param globalState
     */
    protected AbstractOmniscientTopLevelDataSetRegistrator(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        IStorageProcessorTransactional storageProcessor =
                PropertiesBasedETLServerPlugin.create(IStorageProcessorTransactional.class,
                        globalState.getThreadParameters().getThreadProperties(),
                        STORAGE_PROCESSOR_KEY, true);
        storageProcessor.setStoreRootDirectory(globalState.getStoreRootDir());

        Properties onErrorDecisionProperties =
                ExtendedProperties.getSubset(globalState.getThreadParameters()
                        .getThreadProperties(), ON_ERROR_DECISION_KEY + ".", true);
        IDataSetOnErrorActionDecision onErrorDecision =
                ClassUtils.create(
                        IDataSetOnErrorActionDecision.class,
                        globalState.getThreadParameters().getOnErrorActionDecisionClass(
                                ConfiguredOnErrorActionDecision.class),
                        onErrorDecisionProperties);

        state =
                new OmniscientTopLevelDataSetRegistratorState(globalState, storageProcessor,
                        new ReentrantLock(), FileOperations.getMonitoredInstanceForCurrentThread(),
                        onErrorDecision);

        state.fileOperations.mkdirs(getRollBackStackParentFolder());

        DataSetRegistrationTransaction.rollbackDeadTransactions(getRollBackStackParentFolder());

    }

    @Override
    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        return state;
    }

    @Override
    public Lock getRegistrationLock()
    {
        return state.registrationLock;
    }

    @Override
    public File getRollBackStackParentFolder()
    {
        return getGlobalState().getDssInternalTempDir();
    }

    /**
     * A file has arrived in the drop box. Handle it.
     * <p>
     * Setup necessary for data set handling is done, then the handleDataSet method (a subclass responsibility) is invoked.
     */
    @Override
    public final void handle(final File incomingDataSetFileOrIsFinishedFile)
    {
        if (stopped)
        {
            return;
        }

        // get the original incoming dataset file
        final File incomingDataSetFile =
                getGlobalState().isUseIsFinishedMarkerFile() ? state.getMarkerFileUtility()
                        .getIncomingDataSetPathFromMarker(incomingDataSetFileOrIsFinishedFile)
                        : incomingDataSetFileOrIsFinishedFile;

        final IDelegatedActionWithResult<Boolean> markerFileCleanupAction;

        // Figure out what the real incoming data is -- if we use a marker file, it will tell us the
        // name
        if (getGlobalState().isUseIsFinishedMarkerFile())
        {
            markerFileCleanupAction = new IDelegatedActionWithResult<Boolean>()
                {
                    @Override
                    public Boolean execute(boolean didOperationSucceed)
                    {
                        return state.getMarkerFileUtility().deleteAndLogIsFinishedMarkerFile(
                                incomingDataSetFileOrIsFinishedFile);
                    }
                };
        } else
        {
            markerFileCleanupAction = new DoNothingDelegatedAction();
        }

        // read from configuration prestaging parameter.
        DataSetRegistrationPreStagingBehavior preStagingUsage =
                state.getGlobalState().getThreadParameters()
                        .getDataSetRegistrationPreStagingBehavior();

        if (preStagingUsage == DataSetRegistrationPreStagingBehavior.USE_ORIGINAL)
        {
            DataSetFile incoming = new DataSetFile(incomingDataSetFile);
            handle(incoming, null, null, new NoOpDelegate(
                    DataSetRegistrationPreStagingBehavior.USE_ORIGINAL), markerFileCleanupAction);
        } else
        {
            // If we use the prestaging phase, we make a hardlink copy in prestaging area
            operationLog.info("Make hardlink copy of " + incomingDataSetFile);
            File copyOfIncoming = tryCopyIncomingFileToPreStaging(incomingDataSetFile);
            if (null == copyOfIncoming)
            {
                // Nothing to do
                return;
            }

            operationLog.info("Hardlink copy is ready is a pre staging area. " + copyOfIncoming);

            DataSetFile dsf = new DataSetFile(incomingDataSetFile, copyOfIncoming);

            // For cleanup we use the postRegistrationCleanUpAction wich clears the prestaging area.
            PostRegistrationCleanUpAction cleanupAction =
                    new PostRegistrationCleanUpAction(dsf, markerFileCleanupAction);
            handle(dsf, null, null,
                    new NoOpDelegate(DataSetRegistrationPreStagingBehavior.USE_PRESTAGING),
                    cleanupAction);
        }
    }

    /**
     * Make a copy of the file to the prestaging directory.
     * 
     * @return The file in the prestaging directory or null if a copy could not be made.
     */
    private File tryCopyIncomingFileToPreStaging(File incomingDataSetFile)
    {
        TopLevelDataSetRegistratorGlobalState globalState = state.getGlobalState();
        File preStagingRootDir = globalState.getPreStagingDir();
        String incomingDirName =
                new DssUniqueFilenameGenerator(globalState.getThreadParameters().getThreadName(),
                        incomingDataSetFile.getName(), null).generateFilename();
        File preStagingDir = new File(preStagingRootDir, incomingDirName);

        preStagingDir.mkdir();

        // Try to find a hardlink maker
        IImmutableCopier hardlinkMaker =
                new AssertionCatchingImmutableCopierWrapper(
                        FastRecursiveHardLinkMaker.tryCreate(RSyncConfig.getInstance().getAdditionalCommandLineOptions()));
        boolean linkWasMade = false;

        // Use the hardlink maker if we got one
        Status status = hardlinkMaker.copyImmutably(incomingDataSetFile, preStagingDir, null);
        linkWasMade = status.isOK();
        if (status.isError())
        {
            final String msg =
                    status.tryGetErrorMessage() == null ? "Unknown error" : status
                            .tryGetErrorMessage();
            operationLog.warn("Failed to make a hard link copy of " + incomingDataSetFile + " to "
                    + preStagingDir + ": " + msg);
        }

        if (false == linkWasMade)
        {
            // First check if the original file still exists
            if (false == incomingDataSetFile.exists())
            {
                operationLog.warn(incomingDataSetFile.getAbsolutePath()
                        + " has been deleted. Nothing to process.");
                return null;
            }
            FileOperations.getMonitoredInstanceForCurrentThread().copyToDirectory(
                    incomingDataSetFile, preStagingDir);
        }

        return new File(preStagingDir, incomingDataSetFile.getName());
    }

    /**
     * A file has arrived via RPC, handle it!
     * <p>
     * The handleDataSet method (a subclass responsibility) is invoked.
     */
    @Override
    public final void handle(File incomingDataSetFile, String userSessionToken,
            DataSetInformation callerDataSetInformation, ITopLevelDataSetRegistratorDelegate delegate)
    {
        handle(new DataSetFile(incomingDataSetFile), userSessionToken, callerDataSetInformation, delegate);
    }

    /**
     * A file has arrived via RPC, handle it!
     * <p>
     * The handleDataSet method (a subclass responsibility) is invoked.
     */
    public final void handle(DataSetFile incomingDataSetFile, String userSessionToken,
            DataSetInformation callerDataSetInformation, ITopLevelDataSetRegistratorDelegate delegate)
    {
        if (stopped)
        {
            return;
        }

        // In this case, don't make a hardlink copy, since the user has the file on their local
        // machine

        DataSetRegistrationService<T> service =
                handle(incomingDataSetFile, userSessionToken, callerDataSetInformation, delegate,
                        new DoNothingDelegatedAction());
        if (service.didErrorsArise())
        {
            Throwable firstError = service.getEncounteredErrors().get(0);
            throw new EnvironmentFailureException("Could not process file "
                    + incomingDataSetFile.getLogicalIncomingFile().getName(),
                    asSerializableException(firstError));
        }
    }

    /**
     * Not all instances of PyExceptions are serializable, because they keep references to non-serializable objects e.g. java.lang.reflect.Method.
     * <p>
     * Subclasses may need to override if they encounter other kinds of exceptions that cannot happen in this generic context.
     */
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof HighLevelException)
        {
            return new RuntimeException(throwable.getMessage());
        }
        Throwable cause = throwable;
        while (cause.getCause() != null)
        {
            cause = cause.getCause();
        }
        return new RuntimeException(cause.toString());
    }

    /**
     * Set up the infrastructure and forward control to subclasses. Clients can query the service for information about what happened.
     */
    private DataSetRegistrationService<T> handle(DataSetFile incomingDataSetFile, String userSessionToken,
            DataSetInformation callerDataSetInformationOrNull,
            ITopLevelDataSetRegistratorDelegate delegate,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction)
    {
        DataSetRegistrationService<T> service =
                createDataSetRegistrationService(incomingDataSetFile,
                        callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);

        try
        {
            ValidationScriptRunner validationScriptRunner =
                    ValidationScriptRunner.createValidatorFromScriptPaths(getGlobalState()
                            .getValidationScriptsOrNull());

            List<ValidationError> validationErrors =
                    validationScriptRunner.validate(incomingDataSetFile.getLogicalIncomingFile());
            if (validationErrors.size() > 0)
            {
                handleValidationErrors(validationErrors, incomingDataSetFile, service);
            } else
            {
                handleDataSet(incomingDataSetFile, service);
                service.commit();
            }
        } catch (Throwable ex)
        {
            operationLog.error("Could not process file " + incomingDataSetFile, ex);
            rollback(service, ex);

            // If we are here, it is because there was an error thrown in Java before trying to
            // register the data set. This is considered a script error
            UnstoreDataAction action =
                    getRegistratorState().getOnErrorActionDecision().computeUndoAction(
                            ErrorType.REGISTRATION_SCRIPT_ERROR, ex);
            DataSetStorageRollbacker rollbacker =
                    new DataSetStorageRollbacker(getRegistratorState(), operationLog, action,
                            incomingDataSetFile, null, ex, ErrorType.REGISTRATION_SCRIPT_ERROR);
            operationLog.info(rollbacker.getErrorMessageForLog());

            service.getDssRegistrationLog().log("Processing failed : " + ex.toString());
            rollbacker.doRollback(service.getDssRegistrationLog());

            service.getDssRegistrationLog().registerFailure();
        } finally
        {
            service.cleanAfterRegistrationIfNecessary();
        }

        return service;
    }

    /**
     * Validation errors were found in the incoming data set file, display them. Subclasses may override.
     */
    protected void handleValidationErrors(List<ValidationError> validationErrors,
            DataSetFile incomingDataSetFile, DataSetRegistrationService<T> service)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation script [");
        sb.append(getGlobalState().getValidationScriptsOrNull());
        sb.append("] found errors in incoming data set [");
        sb.append(incomingDataSetFile.getLogicalIncomingFile());
        sb.append("]:\n");
        for (ValidationError error : validationErrors)
        {
            sb.append("\t");
            sb.append(error.getErrorMessage());
            sb.append("\n");
        }

        UnstoreDataAction action =
                getRegistratorState().getOnErrorActionDecision().computeUndoAction(
                        ErrorType.INVALID_DATA_SET, null);
        DataSetStorageRollbacker rollbacker =
                new DataSetStorageRollbacker(getRegistratorState(), operationLog, action,
                        incomingDataSetFile, null, null, ErrorType.INVALID_DATA_SET);
        sb.append(rollbacker.getErrorMessageForLog());
        String logMessage = sb.toString();
        service.getDssRegistrationLog().info(operationLog, logMessage);
        rollbacker.doRollback(service.getDssRegistrationLog());
        service.getDssRegistrationLog().registerFailure();
    }

    @Override
    public boolean isStopped()
    {
        return stopped;
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    //
    // ISelfTestable
    //
    @Override
    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        new TopLevelDataSetChecker(operationLog, state.storageProcessor, state.fileOperations)
                .runCheck();
    }

    /**
     * Rollback a failure when trying to commit a transaction.
     * <p>
     * Subclasses may override, but should call super.
     */

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        updateStopped(ex instanceof InterruptedExceptionUnchecked);
    }

    /**
     * A method called after a successful commit of a transaction.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    @Override
    public void didCommitTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction)
    {
    }

    /**
     * A method called just before the registration of datasets in application server.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    @Override
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
    }

    /**
     * A method called just after the successful registration of datasets in application server.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    @Override
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
    }

    /**
     * A method called when there is an error in one of the secondary transactions.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    @Override
    public void didEncounterSecondaryTransactionErrors(
            DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
    }

    /**
     * Rollback a failure that occurs outside of any *particular* data set registration, but with the whole processing of the incoming folder itself.
     * <p>
     * Subclasses may override, but should call super.
     */
    protected void rollback(DataSetRegistrationService<T> service, Throwable throwable)
    {
        updateStopped(throwable instanceof InterruptedExceptionUnchecked);

        service.abort(throwable);
    }

    /**
     * Create the data set registration service.
     * 
     * @param incomingDataSetFile
     * @param callerDataSetInformationOrNull
     */
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        DataSetRegistrationService<T> service =
                new DataSetRegistrationService(this, incomingDataSetFile,
                        new DefaultDataSetRegistrationDetailsFactory(getRegistratorState(),
                                callerDataSetInformationOrNull),
                        cleanAfterwardsAction, delegate);
        return service;
    }

    /**
     * Register the data with openBIS
     */
    public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
            NewExternalData data) throws Throwable
    {
        getGlobalState().getOpenBisService().registerDataSet(dataSetInformation, data);
    }

    /**
     * Update the value of stopped using the argument.
     * <p>
     * To be called by subclasses.
     */
    protected void updateStopped(boolean update)
    {
        stopped |= update;
    }

    /**
     * For subclasses to implement.
     * 
     * @throws Throwable
     */
    protected abstract void handleDataSet(DataSetFile dataSetFile,
            DataSetRegistrationService<T> service) throws Throwable;
}
