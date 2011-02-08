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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityRegistrationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * Abstract superclass for the states a DataSetRegistrationTransaction can be in.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractTransactionState<T extends DataSetInformation>
{
    protected final DataSetRegistrationTransaction<T> parent;

    protected AbstractTransactionState(DataSetRegistrationTransaction<T> parent)
    {
        this.parent = parent;
    }

    public abstract boolean isCommitted();

    public abstract boolean isRolledback();

    /**
     * The state where the transaction is still modifyiable.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class LiveTransactionState<T extends DataSetInformation> extends
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
        private final DataSetRegistrationService<T> registrationService;

        // The interface to openBIS
        private final IEncapsulatedOpenBISService openBisService;

        private final IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory;

        private final ArrayList<DataSet<T>> registeredDataSets = new ArrayList<DataSet<T>>();

        private final List<Experiment> experimentsToBeRegistered = new ArrayList<Experiment>();

        public LiveTransactionState(DataSetRegistrationTransaction<T> parent,
                RollbackStack rollbackStack, File workingDirectory, File stagingDirectory,
                DataSetRegistrationService<T> registrationService,
                IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
        {
            super(parent);
            this.rollbackStack = rollbackStack;
            this.workingDirectory = workingDirectory;
            this.stagingDirectory = stagingDirectory;
            this.registrationService = registrationService;
            this.openBisService =
                    this.registrationService.getRegistratorContext().getGlobalState()
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
            ArrayList<DataSetStorageAlgorithm<T>> algorithms =
                    new ArrayList<DataSetStorageAlgorithm<T>>(registeredDataSets.size());
            for (DataSet<T> dataSet : registeredDataSets)
            {
                File contents = dataSet.getDataSetContents();
                DataSetRegistrationDetails<T> details = dataSet.getRegistrationDetails();
                algorithms.add(registrationService.createStorageAlgorithm(contents, details));
            }

            DataSetStorageAlgorithmRunner<T> runner =
                    new DataSetStorageAlgorithmRunner<T>(algorithms, parent, parent);
            runner.prepareAndRunStorageAlgorithms();
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

        AtomicEntityRegistrationDetails<T> createRegistrationDetails(
                List<DataSetRegistrationInformation<T>> dataSetRegistrations)
        {
            ArrayList<NewExperiment> experimentRegistrations = new ArrayList<NewExperiment>();
            ArrayList<ExperimentUpdatesDTO> experimentUpdates =
                    new ArrayList<ExperimentUpdatesDTO>();
            ArrayList<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();
            ArrayList<NewSample> sampleRegistrations = new ArrayList<NewSample>();

            AtomicEntityRegistrationDetails<T> registrationDetails =
                    new AtomicEntityRegistrationDetails<T>(experimentUpdates,
                            experimentRegistrations, sampleUpdates, sampleRegistrations,
                            dataSetRegistrations);
            return registrationDetails;
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
            super(liveState.parent);
            this.liveState = liveState;
            deleteStagingFolders();
            this.liveState.rollbackStack.discard();
        }

        private void deleteStagingFolders()
        {
            for (DataSet<T> dataSet : liveState.registeredDataSets)
            {
                dataSet.getDataSetStagingFolder().delete();
            }
        }

    }

    /**
     * State where the transaction has been committed.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class CommitedTransactionState<T extends DataSetInformation> extends
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

    static class RolledbackTransactionState<T extends DataSetInformation> extends
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
