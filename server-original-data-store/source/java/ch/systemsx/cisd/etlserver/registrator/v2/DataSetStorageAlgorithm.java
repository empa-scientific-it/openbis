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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataStoreStrategyKey;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MkdirsCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MoveFileCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.NewFileCommand;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.ConversionUtils;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStoragePrecommitRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageStoredRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * DataSetStorageAlgorithm is a state machine that executes steps to store a data set in the store and transitions between states.
 * <p>
 * The states and transitions are as follows: <br>
 * 
 * <pre>
 *                                             /-> (Committed*)
 * (Initialized) -> (Prepared) -> (Stored) -<
 *                     \                       \-> (Rolledback) -> (UndoneState*)
 *                      \ -------------------------------------/
 * </pre>
 * 
 * States marked with a "*" are terminal.
 * <p>
 * N.b. Methods invoked on states for which the method is not valid will yield a class cast exception since the state object will not be castable to
 * the desired class.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageAlgorithm<T extends DataSetInformation>
{
    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageAlgorithm.class);

    private final File incomingDataSetFile;

    private final DataSetRegistrationDetails<? extends T> registrationDetails;

    private final T dataSetInformation;

    private final IDataStoreStrategy dataStoreStrategy;

    private final IStorageProcessorTransactional storageProcessor;

    private final String dataStoreCode;

    private final DataSetType dataSetType;

    private final File storeRoot;

    private final IFileOperations fileOperations;

    // Used by the storage processor
    private final IMailClient mailClient;

    // Used to create a staging directory for the storage processor
    private final File stagingDirectory;

    // Used to create a staging directory for the storage processor
    private final File preCommitDirectory;

    // State that changes during execution
    private DataSetStorageAlgorithmState<T> state;

    /**
     * Utility method for creating base directories
     */
    public static File createBaseDirectory(final IDataStoreStrategy strategy, final File baseDir,
            IFileOperations fileOperations, final DataSetInformation dataSetInfo,
            DataSetType dataSetType, File incomingDataSetFile)
    {
        final File baseDirectory = strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
        baseDirectory.mkdirs();
        if (fileOperations.isDirectory(baseDirectory) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Creating data set base directory '%s' for data set '%s' failed.",
                    baseDirectory.getAbsolutePath(), incomingDataSetFile);
        }
        return baseDirectory;
    }

    public static File createBaseDirectory(final IDataStoreStrategy strategy, final File baseDir,
            IFileOperations fileOperations, final DataSetInformation dataSetInfo,
            DataSetType dataSetType, File incomingDataSetFile, IRollbackStack rollbackStack)
    {
        final File baseDirectory = strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
        rollbackStack.pushAndExecuteCommand(new MkdirsCommand(baseDirectory.getAbsolutePath()));
        if (fileOperations.isDirectory(baseDirectory) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Creating data set base directory '%s' for data set '%s' failed.",
                    baseDirectory.getAbsolutePath(), incomingDataSetFile);
        }
        return baseDirectory;
    }

    public DataSetStorageAlgorithm(File incomingDataSetFile,
            DataSetRegistrationDetails<? extends T> registrationDetails,
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IDataSetValidator dataSetValidator, String dataStoreCode,
            IFileOperations fileOperations, IMailClient mailClient, File stagingDirectory,
            File precommitDirectory)
    {
        this.incomingDataSetFile = incomingDataSetFile;
        this.registrationDetails = registrationDetails;
        this.dataSetInformation = registrationDetails.getDataSetInformation();
        this.dataStoreStrategy = dataStoreStrategy;
        this.storageProcessor = storageProcessor;
        this.dataStoreCode = dataStoreCode;
        this.fileOperations = fileOperations;
        this.mailClient = mailClient;
        this.stagingDirectory = stagingDirectory;
        this.preCommitDirectory = precommitDirectory;

        this.storeRoot = storageProcessor.getStoreRootDirectory();
        this.dataSetType = registrationDetails.getDataSetType();

        assert dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED : "Data set must be associated with an experiment ";

        dataSetValidator.assertValidDataSet(dataSetType, incomingDataSetFile);

        state = new InitializedState<T>(this);
    }

    /**
     * Creates algorithm in a precommitted state from a recovery algorithm.
     */
    public static <T extends DataSetInformation> DataSetStorageAlgorithm<T> createFromPrecommittedRecoveryAlgorithm(
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IFileOperations fileOperations, IMailClient mailClient,
            DataSetStoragePrecommitRecoveryAlgorithm<T> recoveryAlgorithm)
    {
        DataSetStorageAlgorithm<T> storageAlgorithm =
                new DataSetStorageAlgorithm<T>(dataStoreStrategy, storageProcessor, fileOperations,
                        mailClient, recoveryAlgorithm.getRecoveryAlgorithm());
        storageAlgorithm.state = new PrecommittedState<T>(storageAlgorithm, recoveryAlgorithm);
        return storageAlgorithm;
    }

    public static <T extends DataSetInformation> DataSetStorageAlgorithm<T> createFromStoredRecoveryAlgorithm(
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IFileOperations fileOperations, IMailClient mailClient,
            DataSetStorageStoredRecoveryAlgorithm<T> recoveryAlgorithm)
    {
        DataSetStorageAlgorithm<T> storageAlgorithm =
                new DataSetStorageAlgorithm<T>(dataStoreStrategy, storageProcessor, fileOperations,
                        mailClient, recoveryAlgorithm.getRecoveryAlgorithm());
        storageAlgorithm.state = new StoredState<T>(storageAlgorithm, recoveryAlgorithm);
        return storageAlgorithm;
    }

    protected DataSetStorageAlgorithm(IDataStoreStrategy dataStoreStrategy,
            IStorageProcessorTransactional storageProcessor, IFileOperations fileOperations,
            IMailClient mailClient, DataSetStorageRecoveryAlgorithm<T> storedRecoveryAlgorithm)
    {
        this.incomingDataSetFile = storedRecoveryAlgorithm.getIncomingDataSetFile();
        this.registrationDetails = null;
        this.dataSetInformation = storedRecoveryAlgorithm.getDataSetInformation();

        this.dataStoreStrategy = dataStoreStrategy;
        this.storageProcessor = storageProcessor;
        this.dataStoreCode = storedRecoveryAlgorithm.getDataStoreCode();
        this.fileOperations = fileOperations;
        this.mailClient = mailClient;
        this.stagingDirectory = storedRecoveryAlgorithm.getStagingDirectory();
        this.preCommitDirectory = storedRecoveryAlgorithm.getPreCommitDirectory();

        this.storeRoot = storageProcessor.getStoreRootDirectory();
        this.dataSetType = null;
    }

    public DataSetStoragePrecommitRecoveryAlgorithm<T> getPrecommitRecoveryAlgorithm()
    {
        if (false == state instanceof PrecommittedState<?>)
        {
            throw new IllegalStateException(
                    "Precommit recovery algorithm available only at precommited state");
        }
        PrecommittedState<T> precommittedState = (PrecommittedState<T>) state;
        DataSetStoragePrecommitRecoveryAlgorithm<T> recoveryAlgorithm =
                new DataSetStoragePrecommitRecoveryAlgorithm<T>(dataSetInformation,
                        dataStoreStrategy.getKey(), incomingDataSetFile, stagingDirectory,
                        preCommitDirectory, dataStoreCode, precommittedState.storagePaths,
                        precommittedState.markerFile, precommittedState.transaction);

        return recoveryAlgorithm;
    }

    public DataSetStorageStoredRecoveryAlgorithm<T> getStoredRecoveryAlgorithm()
    {
        if (false == state instanceof StoredState<?>)
        {
            throw new IllegalStateException(
                    "Stored recovery algorithm available only at stored state");
        }
        StoredState<T> storedState = (StoredState<T>) state;
        DataSetStorageStoredRecoveryAlgorithm<T> recoveryAlgorithm =
                new DataSetStorageStoredRecoveryAlgorithm<T>(dataSetInformation,
                        dataStoreStrategy.getKey(), incomingDataSetFile, stagingDirectory,
                        preCommitDirectory, dataStoreCode, storedState.storagePaths);

        return recoveryAlgorithm;
    }

    /**
     * Prepare registration of a data set. Expects initialized state, and changes into prepared state
     * 
     * @param rollbackStack
     */
    public IStorageProcessorTransaction prepare(IRollbackStack rollbackStack)
    {
        InitializedState<T> initializedState = (InitializedState<T>) state;
        initializedState.prepare(rollbackStack);

        state = new PreparedState<T>(initializedState);
        return ((PreparedState<T>) state).transaction;
    }

    /**
     * Stores the data in precommit. Expects prepared state and leaves in precommited state.
     */
    public void preCommit() throws Throwable
    {
        PreparedState<T> preparedState = (PreparedState<T>) state;
        preparedState.storeDataInPrecommit();

        state = new PrecommittedState<T>(preparedState);
    }

    /**
     * Moves the data from precommit to the store. Expects Commited State and leaves in stored state.
     */
    public void moveToTheStore() throws Throwable
    {
        CommittedState<T> committed = (CommittedState<T>) state;
        committed.moveToTheStore();
        state = new StoredState<T>(committed);
    }

    /**
     * Does some idempotent cleanup - doesn't change the state
     */
    public void cleanPrecommitDirectory()
    {
        StoredState<T> stored = (StoredState<T>) state;
        stored.cleanPrecommitDirectory();
    }

    /**
     * Transition to the rolledback state, but don't actually do anything. The rollback logic will be carried out by the rollback stack.
     */
    public void transitionToRolledbackState(Throwable throwable)
    {
        // Rollback in initialized state means that preparation has failed.
        if (state instanceof InitializedState)
        {
            return;
        }

        // Rollback may be called on in the precommit state or in the prepared state.
        if (state instanceof PreparedState)
        {
            PreparedState<T> preparedState = (PreparedState<T>) state;
            if (preparedState.wasStoreDataAttempted())
            {
                // If a storeData() was attempted and failed, then we need to move to the stored
                // state in order to rollback
                state = new PrecommittedState<T>((PreparedState<T>) state);
            } else
            {
                // If no storeData() was invoked, there is nothing to do
                return;
            }
        }

        PrecommittedState<T> storedState = (PrecommittedState<T>) state;
        storedState.cleanUpMarkerFile();

        state = new RolledbackState<T>(storedState, UnstoreDataAction.LEAVE_UNTOUCHED, throwable);
    }

    public void transitionToUndoneState()
    {
        // Rollback may be called on in the stored state or in the prepared/initialized state. In
        // the prepared state, there is nothing to do.

        if (state instanceof InitializedState)
        {
            state = new PreparedState<T>((InitializedState<T>) state);
        }

        if (state instanceof PreparedState)
        {
            state = new UndoneState<T>((PreparedState<T>) state);
            return;
        }

        RolledbackState<T> rolledbackState = (RolledbackState<T>) state;

        state = new UndoneState<T>(rolledbackState);
    }

    /**
     * Ask the storage processor to commit. Used by clients of the algorithm. Expects stored state, and changes to commited state.
     */
    public void commitStorageProcessor()
    {
        PrecommittedState<T> storedState = (PrecommittedState<T>) state;
        storedState.commitStorageProcessor();

        state = new CommittedState<T>(storedState);
    }

    public File getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public T getDataSetInformation()
    {
        return dataSetInformation;
    }

    protected Logger getOperationLog()
    {
        return operationLog;
    }

    public NewExternalData createExternalData()
    {
        File dataFile = ((PrecommittedState<T>) state).getStoredDirectory();
        String relativePath = FileUtilities.getRelativeFilePath(storeRoot, dataFile);
        String absolutePath = dataFile.getAbsolutePath();
        assert relativePath != null : String.format(
                TransferredDataSetHandler.TARGET_NOT_RELATIVE_TO_STORE_ROOT, absolutePath,
                storeRoot.getAbsolutePath());
        StorageFormat storageFormat = storageProcessor.getStorageFormat();

        return ConversionUtils.convertToNewExternalData(registrationDetails, dataStoreCode,
                storageFormat, relativePath);
    }

    public String getSuccessRegistrationMessage()
    {
        return dataSetInformation.toString();
    }

    public String getFailureRegistrationMessage()
    {
        return "Error trying to register data set '" + incomingDataSetFile.getName() + "'.";
    }

    protected DataSetType getDataSetType()
    {
        return dataSetType;
    }

    protected IDataStoreStrategy getDataStoreStrategy()
    {
        return dataStoreStrategy;
    }

    protected File getStoreRoot()
    {
        return storeRoot;
    }

    protected IStorageProcessorTransactional getStorageProcessor()
    {
        return storageProcessor;
    }

    protected String getDataStoreCode()
    {
        return dataStoreCode;
    }

    protected DataSetRegistrationDetails<? extends T> getRegistrationDetails()
    {
        return registrationDetails;
    }

    /**
     * The global staging directory where all datasets are kept during the registration
     */
    public File getStagingDirectory()
    {
        return stagingDirectory;
    }

    public File getPreCommitDirectory()
    {
        return preCommitDirectory;
    }

    /**
     * @returns The staging file for this dataset. (combined path getStagingDirectory + dataSetCode)
     */
    public File getStagingFile()
    {
        return new File(stagingDirectory, dataSetInformation.getDataSetCode());
    }

    private static abstract class DataSetStorageAlgorithmState<T extends DataSetInformation>
    {
        protected final DataSetStorageAlgorithm<T> storageAlgorithm;

        protected final File incomingDataSetFile;

        protected DataSetStorageAlgorithmState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            this.storageAlgorithm = storageAlgorithm;
            this.incomingDataSetFile = storageAlgorithm.getIncomingDataSetFile();
        }

        protected Logger getOperationLog()
        {
            return storageAlgorithm.getOperationLog();
        }

        protected IFileOperations getFileOperations()
        {
            return storageAlgorithm.fileOperations;
        }
    }

    private static class InitializedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected DataSetStoragePaths storagePaths;

        protected IStorageProcessorTransaction transaction;

        protected IRollbackStack rollbackStack;

        public InitializedState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            super(storageAlgorithm);
        }

        /**
         * Prepare registration of a data set.
         */
        public void prepare(IRollbackStack aStack)
        {
            this.rollbackStack = aStack;
            IDataStoreStrategy dataStoreStrategy = storageAlgorithm.getDataStoreStrategy();

            // Create the staging base directory
            File stagingBaseDirectory =
                    new File(storageAlgorithm.getStagingDirectory(), storageAlgorithm
                            .getDataSetInformation().getDataSetCode() + "-storage");
            this.rollbackStack.pushAndExecuteCommand(new MkdirsCommand(stagingBaseDirectory
                    .getAbsolutePath()));

            File storeBaseDirectory =
                    DataSetStorageAlgorithm.createBaseDirectory(dataStoreStrategy,
                            storageAlgorithm.getStoreRoot(), getFileOperations(),
                            storageAlgorithm.getDataSetInformation(),
                            storageAlgorithm.getDataSetType(), incomingDataSetFile, rollbackStack);

            StorageProcessorTransactionParameters transactionParameters =
                    new StorageProcessorTransactionParameters(
                            storageAlgorithm.getDataSetInformation(), incomingDataSetFile,
                            stagingBaseDirectory);
            transaction =
                    storageAlgorithm.getStorageProcessor().createTransaction(transactionParameters);

            File precommitBaseDirectory =
                    new File(storageAlgorithm.getPreCommitDirectory(), storageAlgorithm
                            .getDataSetInformation().getDataSetCode() + "-precommit");

            storagePaths =
                    new DataSetStoragePaths(stagingBaseDirectory, storeBaseDirectory,
                            precommitBaseDirectory);

        }
    }

    private static class PreparedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        protected final DataSetInformation dataSetInformation;

        protected final IRollbackStack rollbackStack;

        protected final IStorageProcessorTransaction transaction;

        protected final DataSetStoragePaths storagePaths;

        protected File markerFile;

        public PreparedState(InitializedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.dataSetInformation = storageAlgorithm.getDataSetInformation();
            this.transaction = oldState.transaction;
            this.rollbackStack = oldState.rollbackStack;
            this.storagePaths = oldState.storagePaths;
        }

        public void storeDataInPrecommit()
        {
            // will throw exception if marker file already exists
            markerFile = createProcessingMarkerFile();

            transactionStoreData();

            moveFilesFromStagingToPrecommit();
        }

        private void moveFilesFromStagingToPrecommit()
        {
            File stagedStoredDataDirectory = transaction.getStoredDataDirectory();
            assert stagedStoredDataDirectory != null : "The folder that contains the stored data should not be null.";

            File[] stagedFiles = stagedStoredDataDirectory.listFiles();
            if (null == stagedFiles)
            {
                return;
            }

            for (File stagedFile : stagedFiles)
            {
                rollbackStack
                        .pushAndExecuteCommand(new MoveFileCommand(stagedStoredDataDirectory
                                .getAbsolutePath(), stagedFile.getName(),
                                storagePaths.precommitBaseDirectory.getAbsolutePath(), stagedFile
                                        .getName()));
            }
        }

        private void transactionStoreData()
        {
            // human readable description for logging purposes
            String entityDescription = createEntityDescription();
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info("Start storing data set for " + entityDescription + ".");
            }

            // prepare watch for time calculation
            final StopWatch watch = new StopWatch();
            watch.start();

            // actual call to transaction.storeData
            transaction.storeData(storageAlgorithm.getRegistrationDetails(), getMailClient(),
                    incomingDataSetFile);

            // log time
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info(
                        "Finished storing data set for " + entityDescription + ", took " + watch);
            }
        }

        private IMailClient getMailClient()
        {
            return storageAlgorithm.mailClient;
        }

        private final File createProcessingMarkerFile()
        {
            final File baseDirectory = storagePaths.stagingBaseDirectory;
            final File baseParentDirectory = baseDirectory.getParentFile();
            final String processingDirName = baseDirectory.getName();
            markerFile =
                    new File(baseParentDirectory, FileConstants.PROCESSING_PREFIX
                            + processingDirName);
            try
            {
                // will throw exception if marker file already exists
                rollbackStack
                        .pushAndExecuteCommand(new NewFileCommand(markerFile.getAbsolutePath()));
            } catch (final IOExceptionUnchecked ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Cannot create marker file '%s'.", markerFile.getPath());
            }
            return markerFile;
        }

        private String createEntityDescription()
        {
            SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
            if (sampleIdentifier != null)
            {
                return "sample '" + sampleIdentifier + "'";
            }
            return "experiment '" + dataSetInformation.getExperimentIdentifier() + "'";
        }

        private boolean wasStoreDataAttempted()
        {
            return transaction != null;
        }
    }

    private static class PrecommittedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected final IStorageProcessorTransaction transaction;

        protected final File markerFile;

        protected final DataSetStoragePaths storagePaths;

        public PrecommittedState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.transaction = oldState.transaction;
            this.markerFile = oldState.markerFile;
            this.storagePaths = oldState.storagePaths;
        }

        public PrecommittedState(DataSetStorageAlgorithm<T> algorithm,
                DataSetStoragePrecommitRecoveryAlgorithm<T> recoveryAlgorithm)
        {
            super(algorithm);
            this.transaction = recoveryAlgorithm.getTransaction();
            this.markerFile = recoveryAlgorithm.getMarkerFile();
            this.storagePaths = recoveryAlgorithm.getDataSetStoragePaths();
        }

        /**
         * Ask the storage processor to commit. Used by clients of the algorithm.
         * <p>
         */
        public void commitStorageProcessor()
        {
            transaction.setStoredDataDirectory(storagePaths.precommitBaseDirectory);
            transaction.commit();
            cleanUpMarkerFile();
        }

        /**
         * The directory with the stored dataset.
         */
        public File getStoredDirectory()
        {
            return storagePaths.storeBaseDirectory;
        }

        /**
         * Cleanup from the processing -- done after a commit or rollback
         */
        private void cleanUpMarkerFile()
        {
            if (markerFile != null && markerFile.exists())
            {
                getFileOperations().delete(markerFile);
                if (markerFile.exists())
                {
                    operationLog.error("Marker file '" + markerFile + "' could not be deleted.");
                }
            }
        }
    }

    private static class CommittedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        protected final DataSetStoragePaths storagePaths;

        CommittedState(PrecommittedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.storagePaths = oldState.storagePaths;
            cleanUpStagingDirectory();
        }

        private void cleanUpStagingDirectory()
        {
            getFileOperations().delete(storagePaths.stagingBaseDirectory);
            if (storagePaths.stagingBaseDirectory.exists())
            {
                operationLog.error("Staging directory '" + storagePaths.stagingBaseDirectory
                        + "' could not be deleted.");
            }
        }

        /**
         * Moves files from the precommited directory to the store
         */
        private void moveToTheStore()
        {
            if (false == storagePaths.precommitBaseDirectory.exists())
            {
                IOException ioException =
                        new FileNotFoundException("Can't find precommit directory "
                                + storagePaths.precommitBaseDirectory);
                throw new IOExceptionUnchecked(ioException);
            }

            File[] stagedFiles = storagePaths.precommitBaseDirectory.listFiles();
            if (null == stagedFiles)
            {
                return;
            }

            for (File stagedFile : stagedFiles)
            {
                new MoveFileCommand(storagePaths.precommitBaseDirectory.getAbsolutePath(),
                        stagedFile.getName(), storagePaths.storeBaseDirectory.getAbsolutePath(),
                        stagedFile.getName()).execute();
            }
        }
    }

    private static class StoredState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        protected final DataSetStoragePaths storagePaths;

        StoredState(CommittedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            storagePaths = oldState.storagePaths;
        }

        public StoredState(DataSetStorageAlgorithm<T> algorithm,
                DataSetStorageStoredRecoveryAlgorithm<T> recoveryAlgorithm)
        {
            super(algorithm);
            this.storagePaths = recoveryAlgorithm.getDataSetStoragePaths();
        }

        public void cleanPrecommitDirectory()
        {
            storagePaths.precommitBaseDirectory.delete();
        }
    }

    private static class RolledbackState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        public RolledbackState(PrecommittedState<T> oldState, UnstoreDataAction action,
                Throwable throwable)
        {
            super(oldState.storageAlgorithm);
        }
    }

    private static class UndoneState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        UndoneState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

        UndoneState(RolledbackState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }
    }

    public static class DataSetStoragePaths implements Serializable
    {
        private static final long serialVersionUID = 1L;

        protected final File stagingBaseDirectory;

        protected final File storeBaseDirectory;

        protected final File precommitBaseDirectory;

        public DataSetStoragePaths(File stagingBaseDirectory, File storeBaseDirectory,
                File precommitBaseDirectory)
        {
            super();
            this.stagingBaseDirectory = stagingBaseDirectory;
            this.storeBaseDirectory = storeBaseDirectory;
            this.precommitBaseDirectory = precommitBaseDirectory;
        }

        public File getStagingBaseDirectory()
        {
            return stagingBaseDirectory;
        }

        public File getStoreBaseDirectory()
        {
            return storeBaseDirectory;
        }

        public File getPrecommitBaseDirectory()
        {
            return precommitBaseDirectory;
        }
    }

    /**
     * should be overriden by subclasses
     */
    public DataSetKind getDataSetKind()
    {
        return DataSetKind.PHYSICAL;
    }

}
