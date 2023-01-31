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

package ch.systemsx.cisd.etlserver.registrator.recovery;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo.RecoveryStage;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Class responsible for recovering after an environmental error.
 * 
 * @author jakubs
 */
public class DataSetStorageRecoveryManager implements IDataSetStorageRecoveryManager
{
    private static final String PRECOMMIT_SERIALIZED = ".PRECOMMIT_SERIALIZED";

    private static final String PROCESSING_MARKER = ".PROCESSING_MARKER";

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageRecoveryManager.class);

    private File dropboxRecoveryStateDir;

    private File recoveryMarkerFilesDir;

    private int maxRetryCount = 50;

    private int retryPeriodInSeconds = 60;

    /**
     * Serializes data for recovery information
     */
    @Override
    public <T extends DataSetInformation> void checkpointPrecommittedState(TechId registrationId,
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();

        DataSetStoragePrecommitRecoveryState<T> recoveryState =
                new DataSetStoragePrecommitRecoveryState<T>(registrationId,
                        runner.getDataSetStorageAlgorithms(), runner.getDssRegistrationLogger(),
                        runner.getRollbackStack(), incoming, runner.getRegistrationContext()
                                .getPersistentMap());

        checkpointState(runner, recoveryState, RecoveryStage.PRECOMMIT);
    }

    @Override
    public <T extends DataSetInformation> void checkpointPrecommittedStateAfterPostRegistrationHook(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();

        DataSetStoragePrecommitRecoveryState<T> recoveryState =
                new DataSetStoragePrecommitRecoveryState<T>(null,
                        runner.getDataSetStorageAlgorithms(), runner.getDssRegistrationLogger(),
                        runner.getRollbackStack(), incoming, runner.getRegistrationContext()
                                .getPersistentMap());

        checkpointState(runner, recoveryState, RecoveryStage.POST_REGISTRATION_HOOK_EXECUTED);
    }

    @Override
    public <T extends DataSetInformation> void checkpointStoredStateBeforeStorageConfirmation(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();

        DataSetStorageStorageRecoveryState<T> recoveryState =
                new DataSetStorageStorageRecoveryState<T>(runner.getDataSetStorageAlgorithms(),
                        runner.getDssRegistrationLogger(), runner.getRollbackStack(), incoming,
                        runner.getRegistrationContext().getPersistentMap());
        checkpointState(runner, recoveryState, RecoveryStage.STORAGE_COMPLETED);
    }

    public <T extends DataSetInformation> void checkpointState(
            DataSetStorageAlgorithmRunner<T> runner, Serializable recoveryState,
            RecoveryStage recoveryStage)
    {
        File serializedFile = getSerializedFile(runner);

        runner.getRollbackStack().setLockedState(true);

        FileUtilities.writeToFile(serializedFile, recoveryState);

        File processingMarkerFile = getProcessingMarkerFile(runner);

        DataSetStorageRecoveryInfo info =
                new DataSetStorageRecoveryInfo(serializedFile, new Date(), 0, recoveryStage);

        info.writeToFile(processingMarkerFile);

        operationLog.info("Store recovery checkpoint with markerfile " + processingMarkerFile);
    }

    @Override
    public <T extends DataSetInformation> void removeCheckpoint(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        cleanup(runner);
    }

    private <T extends DataSetInformation> File getProcessingMarkerFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        return getProcessingMarkerFile(runner.getIncomingDataSetFile().getRealIncomingFile());
    }

    /**
     * @return processing marker file for a given incoming file.
     */
    @Override
    public File getProcessingMarkerFile(File incoming)
    {
        return new File(recoveryMarkerFilesDir, incoming.getName() + PROCESSING_MARKER);
    }

    private <T extends DataSetInformation> File getSerializedFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();
        String incomingFileName = incoming.getRealIncomingFile().getName();

        return new File(dropboxRecoveryStateDir, incomingFileName + PRECOMMIT_SERIALIZED);
    }

    @Override
    public DataSetStorageRecoveryInfo getRecoveryFileFromMarker(File markerFile)
    {
        return DataSetStorageRecoveryInfo.loadFromFile(markerFile);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataSetInformation> AbstractRecoveryState<T> extractRecoveryCheckpoint(
            File markerFile)
    {
        DataSetStorageRecoveryInfo info = getRecoveryFileFromMarker(markerFile);
        return FileUtilities.loadToObject(info.getRecoveryStateFile(),
                DataSetStoragePrecommitRecoveryState.class);
    }

    @Override
    public <T extends DataSetInformation> void registrationCompleted(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        cleanup(runner);
    }

    public <T extends DataSetInformation> void cleanup(DataSetStorageAlgorithmRunner<T> runner)
    {
        File markerFile = getProcessingMarkerFile(runner);
        File recoveryState = getSerializedFile(runner);

        operationLog.info("Cleanup recovery with marker file " + markerFile);

        runner.getRollbackStack().setLockedState(false);
        // Cleanup the state we have accumulated
        FileUtilities.delete(markerFile);
        FileUtilities.delete(recoveryState);
    }

    @Override
    public boolean canRecoverFromError(Throwable ex)
    {
        if (ex instanceof HighLevelException)
        {
            return HighLevelException.isRetriable(ex);
        }
        else if (ex instanceof ServiceExecutionException)
        {
            try
            {
                // somewhat ugly fix to check if an exception hidden behind service conversation error
                // is retriable high level exception
                ServiceExecutionException sex = (ServiceExecutionException) ex;
                String desc = sex.getDescription();
                String exceptionClassname = desc.substring(0, desc.indexOf(":"));
                Class<?> exceptionClass = Class.forName(exceptionClassname);
                if (HighLevelException.class.isAssignableFrom(exceptionClass))
                {
                    return EnvironmentFailureException.class.isAssignableFrom(exceptionClass);
                }
            } catch (ClassNotFoundException cnfex)
            {
                // at least we tried
            }
        }
        return true;
    }

    @Override
    public void setDropboxRecoveryStateDir(File dropboxRecoveryStateDir)
    {
        this.dropboxRecoveryStateDir = dropboxRecoveryStateDir;
    }

    @Override
    public void setRecoveryMarkerFilesDir(File recoveryMarkerFileDir)
    {
        this.recoveryMarkerFilesDir = recoveryMarkerFileDir;
    }

    @Override
    public void setMaximumRertyCount(int maxRetryCount)
    {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public int getMaximumRertyCount()
    {
        return this.maxRetryCount;
    }

    @Override
    public int getRetryPeriodInSeconds()
    {
        return retryPeriodInSeconds;
    }

    @Override
    public void setRetryPeriodInSeconds(int retryPeriodInSeconds)
    {
        this.retryPeriodInSeconds = retryPeriodInSeconds;

    }

}
