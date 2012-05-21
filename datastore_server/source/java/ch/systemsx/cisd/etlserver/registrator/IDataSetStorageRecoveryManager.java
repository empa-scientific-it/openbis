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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

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
     */
    <T extends DataSetInformation> void checkpointPrecommittedState(
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Remove recovery checkpoint.
     */
    <T extends DataSetInformation> void removeCheckpoint(
            DataSetStorageAlgorithmRunner<T> runner);

    
    /**
     * Note that registration has completed.
     */
    <T extends DataSetInformation> void registrationCompleted(
            DataSetStorageAlgorithmRunner<T> runner);

    /**
     * Use the marker file to recreate the state necessary to complete registration.
     */
    <T extends DataSetInformation> DataSetStoragePrecommitRecoveryState<T> extractPrecommittedCheckpoint(
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
}