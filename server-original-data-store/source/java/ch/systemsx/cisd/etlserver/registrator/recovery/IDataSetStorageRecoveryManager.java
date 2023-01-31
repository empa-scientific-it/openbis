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

import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Interface for recovery managers.
 * 
 * @author jakubs
 */
public interface IDataSetStorageRecoveryManager
{

    // Recovery Mechanics
    /**
     * Create a checkpoint at the precommitted state.
     * 
     * @param registrationId An identifier for the database registration that will happen in the stage change from precommitted to committed.
     * @param runner The algorithm object that manages the registration process.
     */
    <T extends DataSetInformation> void checkpointPrecommittedState(TechId registrationId,
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Create a checkpoint at the precommitted state - after the post-registration hook has executed (so after the entity operations registration has
     * succeeded).
     * 
     * @param runner The algorithm object that manages the registration process.
     */
    <T extends DataSetInformation> void checkpointPrecommittedStateAfterPostRegistrationHook(
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Create a checkpoint after the data has been moved to the store, just before setting the storage confirmed in the application server.
     */
    <T extends DataSetInformation> void checkpointStoredStateBeforeStorageConfirmation(
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Remove recovery checkpoint.
     */
    <T extends DataSetInformation> void removeCheckpoint(DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Note that registration has completed.
     */
    <T extends DataSetInformation> void registrationCompleted(
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Use the marker file to recreate the state necessary to complete registration.
     */
    <T extends DataSetInformation> AbstractRecoveryState<T> extractRecoveryCheckpoint(
            File markerFile);

    /**
     * Extracts the recovery file from the marker file
     */
    DataSetStorageRecoveryInfo getRecoveryFileFromMarker(File markerFile);

    /**
     * @return the path of the recovery marker file for the given incoming
     */
    File getProcessingMarkerFile(File incoming);

    // Simple helper methods
    boolean canRecoverFromError(Throwable ex);

    void setDropboxRecoveryStateDir(File dropboxRecoveryStateDir);

    void setRecoveryMarkerFilesDir(File recoveryMarkerFileDir);

    int getMaximumRertyCount();

    void setMaximumRertyCount(int maxRetryCount);

    /**
     * get's the minimum time period that must pass before the next retry will happen.
     */
    int getRetryPeriodInSeconds();

    /**
     * set's the minimum time period that must pass before the next retry will happen.
     */
    void setRetryPeriodInSeconds(int retryTimeInSeconds);
}