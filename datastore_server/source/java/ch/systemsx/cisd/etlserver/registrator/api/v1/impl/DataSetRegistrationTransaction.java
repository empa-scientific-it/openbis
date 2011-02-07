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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if
 * necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction<T extends DataSetInformation> implements
        IDataSetRegistrationTransaction
{
    private static final String ROLLBACK_QUEUE1_FILE_NAME_SUFFIX = "rollBackQueue1";

    private static final String ROLLBACK_QUEUE2_FILE_NAME_SUFFIX = "rollBackQueue2";

    private final static String ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationTransaction.class);

    /**
     * Check if there are any uncompleted transactions and roll them back. To be called during
     * startup of a thread.
     */
    public static synchronized void rollbackDeadTransactions(File rollBackStackParentFolder)
    {
        File[] rollbackQueue1Files = rollBackStackParentFolder.listFiles(new FilenameFilter()
            {

                public boolean accept(File dir, String name)
                {
                    return name.endsWith(ROLLBACK_QUEUE1_FILE_NAME_SUFFIX);
                }

            });

        for (File rollbackStackQueue1 : rollbackQueue1Files)
        {
            RollbackStack stack = createExistingRollbackStack(rollbackStackQueue1);
            operationLog.info("Found dead rollback stack: " + rollbackStackQueue1
                    + ". Rolling back.");

            try
            {
                stack.rollbackAll();
            } catch (Throwable ex)
            {
                // This should ever happen since rollbackAll should handle execptions, but is here
                // as a safeguard.
                operationLog.error("Encountered error rolling back transaction:");
                operationLog.error(ex);
            }
            stack.discard();
        }
    }

    /**
     * Create a new persistent rollback stack in the supplied folder.
     */
    private static RollbackStack createNewRollbackStack(File rollBackStackParentFolder)
    {
        String fileNamePrefix =
                DateFormatUtils.format(new Date(), ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN)
                        + "-";
        return new RollbackStack(new File(rollBackStackParentFolder, fileNamePrefix
                + ROLLBACK_QUEUE1_FILE_NAME_SUFFIX), new File(rollBackStackParentFolder,
                fileNamePrefix + ROLLBACK_QUEUE2_FILE_NAME_SUFFIX));
    }

    /**
     * Given a queue1 file, create an existing rollback stack
     */
    private static RollbackStack createExistingRollbackStack(File rollbackStackQueue1)
    {
        String rollbackStack1FileName = rollbackStackQueue1.getName();
        // Remove the ROLLBACK_QUEUE1_FILE_NAME_SUFFIX and append the
        // ROLLBACK_QUEUE2_FILE_NAME_SUFFIX
        String rollbackStack2FileName =
                rollbackStack1FileName.substring(0, rollbackStack1FileName.length()
                        - ROLLBACK_QUEUE1_FILE_NAME_SUFFIX.length())
                        + ROLLBACK_QUEUE2_FILE_NAME_SUFFIX;
        return new RollbackStack(rollbackStackQueue1, new File(rollbackStackQueue1.getParentFile(),
                rollbackStack2FileName));
    }

    private AbstractTransactionState<T> state;

    // The registration service that owns this transaction
    private final DataSetRegistrationService registrationService;

    // The interface to openBIS
    private final IEncapsulatedOpenBISService openBisService;

    public DataSetRegistrationTransaction(File rollBackStackParentFolder, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        this(createNewRollbackStack(rollBackStackParentFolder), workingDirectory, stagingDirectory,
                registrationService, registrationDetailsFactory);
    }

    DataSetRegistrationTransaction(RollbackStack rollbackStack, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        state =
                new LiveTransactionState<T>(rollbackStack, workingDirectory, stagingDirectory,
                        registrationService, registrationDetailsFactory);
        this.registrationService = registrationService;
        this.openBisService =
                this.registrationService.getRegistratorState().getGlobalState().getOpenBisService();
    }

    public IDataSet createNewDataSet()
    {
        return ((LiveTransactionState<T>) state).createNewDataSet();
    }

    public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
    {
        return ((LiveTransactionState<T>) state).createNewDataSet(registrationDetails);
    }

    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        SampleIdentifier sampleIdentifier =
                new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();
        return new Sample(openBisService.tryGetSampleWithExperiment(sampleIdentifier));
    }

    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return ((LiveTransactionState<T>) state).getSampleForUpdate(sampleIdentifierString);
    }

    public ISample createNewSample(String sampleIdentifierString)
    {
        return ((LiveTransactionState<T>) state).createNewSample(sampleIdentifierString);
    }

    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();
        ExperimentImmutable experiment =
                new ExperimentImmutable(openBisService.tryToGetExperiment(experimentIdentifier));
        return experiment;
    }

    public IExperiment getExperimentForUpdate(String experimentIdentifierString)
    {
        return ((LiveTransactionState<T>) state).getExperimentForUpdate(experimentIdentifierString);
    }

    public IExperiment createNewExperiment(String experimentIdentifierString)
    {
        return ((LiveTransactionState<T>) state).createNewExperiment(experimentIdentifierString);
    }

    public String moveFile(String src, IDataSet dst)
    {
        return ((LiveTransactionState<T>) state).moveFile(src, dst);
    }

    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return ((LiveTransactionState<T>) state).moveFile(src, dst, dstInDataset);
    }

    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return ((LiveTransactionState<T>) state).createNewDirectory(dst, dirName);
    }

    public String createNewFile(IDataSet dst, String fileName)
    {
        return ((LiveTransactionState<T>) state).createNewFile(dst, fileName);
    }

    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return ((LiveTransactionState<T>) state).createNewFile(dst, dstInDataset, fileName);
    }

    public void deleteFile(String src)
    {
        ((LiveTransactionState<T>) state).deleteFile(src);
    }

    /**
     * Commit the transaction
     */
    public void commit()
    {
        LiveTransactionState<T> liveState = (LiveTransactionState<T>) state;
        liveState.commit();

        state = new CommitedTransactionState<T>(liveState);
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of
     * execution.
     */
    public void rollback()
    {
        LiveTransactionState<T> liveState = (LiveTransactionState<T>) state;
        liveState.rollback();

        state = new RolledbackTransactionState<T>(liveState);
    }

    /**
     * Abstract superclass for the states a DataSetRegistrationTransaction can be in.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static abstract class AbstractTransactionState<T extends DataSetInformation>
    {
        public abstract boolean isCommitted();

        public abstract boolean isRolledback();
    }

    /**
     * The state where the transaction is still modifyiable.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class LiveTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {
        // Keeps track of steps that have been executed and may need to be reverted. Elements are
        // kept in the order they need to be reverted.
        private final RollbackStack rollbackStack;

        // The directory to use as "local" for paths
        private final File workingDirectory;

        // The directory in which new data sets get staged
        private final File stagingDirectory;

        // The registration service that owns this transaction
        private final DataSetRegistrationService registrationService;

        // The interface to openBIS
        private final IEncapsulatedOpenBISService openBisService;

        private final IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory;

        private final ArrayList<DataSet<T>> registeredDataSets = new ArrayList<DataSet<T>>();

        private final List<Experiment> experimentsToBeRegistered = new ArrayList<Experiment>();

        public LiveTransactionState(RollbackStack rollbackStack, File workingDirectory,
                File stagingDirectory, DataSetRegistrationService registrationService,
                IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
        {
            this.rollbackStack = rollbackStack;
            this.workingDirectory = workingDirectory;
            this.stagingDirectory = stagingDirectory;
            this.registrationService = registrationService;
            this.openBisService =
                    this.registrationService.getRegistratorState().getGlobalState()
                            .getOpenBisService();
            this.registrationDetailsFactory = registrationDetailsFactory;
        }

        public IDataSet createNewDataSet()
        {
            // Create registration details for the new data set
            DataSetRegistrationDetails<T> registrationDetails =
                    registrationDetailsFactory.createDataSetRegistrationDetails();

            return createNewDataSet(registrationDetails);
        }

        public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
        {
            // Request a code, so we can keep the staging file name and the data set code in sync
            String dataSetCode = registrationDetails.getDataSetInformation().getDataSetCode();
            if (null == dataSetCode)
            {
                dataSetCode = generateDataSetCode(registrationDetails);
                registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);
            }

            // Create a directory for the data set
            File stagingFolder = new File(stagingDirectory, dataSetCode);
            MkdirsCommand cmd = new MkdirsCommand(stagingFolder.getAbsolutePath());
            executeCommand(cmd);

            DataSet<T> dataSet =
                    registrationDetailsFactory.createDataSet(registrationDetails, stagingFolder);
            registeredDataSets.add(dataSet);
            return dataSet;
        }

        public ISample getSampleForUpdate(String sampleIdentifierString)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public ISample createNewSample(String sampleIdentifierString)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public IExperiment getExperimentForUpdate(String experimentIdentifierString)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public IExperiment createNewExperiment(String experimentIdentifierString)
        {
            String permID = openBisService.createDataSetCode();
            Experiment experiment = new Experiment(experimentIdentifierString, permID);
            experimentsToBeRegistered.add(experiment);
            return experiment;
        }

        public String moveFile(String src, IDataSet dst)
        {
            File srcFile = new File(src);
            return moveFile(src, dst, srcFile.getName());
        }

        public String moveFile(String src, IDataSet dst, String dstInDataset)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;

            // See if this is an absolute path
            File srcFile = new File(src);
            if (false == srcFile.exists())
            {
                // Try it relative
                srcFile = new File(workingDirectory, src);
            }

            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dstInDataset);

            FileUtilities.checkInputFile(srcFile);

            MoveFileCommand cmd =
                    new MoveFileCommand(srcFile.getParentFile().getAbsolutePath(),
                            srcFile.getName(), dstFile.getParentFile().getAbsolutePath(),
                            dstFile.getName());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public String createNewDirectory(IDataSet dst, String dirName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dirName);
            MkdirsCommand cmd = new MkdirsCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public String createNewFile(IDataSet dst, String fileName)
        {
            return createNewFile(dst, "/", fileName);
        }

        public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFolder = new File(dataSetFolder, dstInDataset);
            File dstFile = new File(dstFolder, fileName);
            NewFileCommand cmd = new NewFileCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public void deleteFile(String src)
        {
            // TODO Auto-generated method stub

        }

        /**
         * Commit the transaction
         */
        public void commit()
        {
            for (DataSet<T> dataSet : registeredDataSets)
            {
                registrationService.queueDataSetRegistration(dataSet.getDataSetContents(),
                        dataSet.getRegistrationDetails());

                // File contents = dataSet.getDataSetContents();
                // DataSetRegistrationDetails<T> details = dataSet.getRegistrationDetails();
                // registrationService.getRegistratorState().getDataStrategyStore()
                // .getDataStoreStrategy(details.getDataSetInformation(), contents);
                // DataSetStorageAlgorithm<T> algorithm =
                // new DataSetStorageAlgorithm<T>(contents, details, null, null, null, null, null,
                // null);
            }
            registrationService.commit();
        }

        /**
         * Rollback any commands that have been executed. Rollback is done in the reverse order of
         * execution.
         */
        public void rollback()
        {
            rollbackStack.rollbackAll();
            registeredDataSets.clear();
        }

        /**
         * Execute the command and add it to the list of commands that have been executed.
         */
        private void executeCommand(ITransactionalCommand cmd)
        {
            rollbackStack.pushAndExecuteCommand(cmd);
        }

        /**
         * Generate a data set code for the registration details. Just calls openBisService to get a
         * data set code by default.
         * 
         * @return A data set code
         */
        private String generateDataSetCode(DataSetRegistrationDetails<T> registrationDetails)
        {
            return openBisService.createDataSetCode();
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public boolean isRolledback()
        {
            return false;
        }
    }

    private static abstract class TerminalTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {
        private final LiveTransactionState<T> liveState;

        protected TerminalTransactionState(LiveTransactionState<T> liveState)
        {
            this.liveState = liveState;
            deleteStagingFolders();
        }

        private void deleteStagingFolders()
        {
            for (DataSet<T> dataSet : liveState.registeredDataSets)
            {
                dataSet.getDataSetStagingFolder().delete();
            }
        }
    }

    private static class CommitedTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {

        public CommitedTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isCommitted()
        {
            return true;
        }

        @Override
        public boolean isRolledback()
        {
            return false;
        }
    }

    private static class RolledbackTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {
        public RolledbackTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public boolean isRolledback()
        {
            return true;
        }
    }

}
