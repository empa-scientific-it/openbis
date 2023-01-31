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

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataStoreStrategyKey;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm.DataSetStoragePaths;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author jakubs
 */
public class DataSetStoragePrecommitRecoveryAlgorithm<T extends DataSetInformation> implements
        Serializable, IDataSetStorageRecoveryAlgorithmWithState<T>
{

    private static final long serialVersionUID = 1L;

    private final DataSetStorageRecoveryAlgorithm<T> recoveryAlgorithm;

    private final IStorageProcessorTransaction transaction;

    private final File markerFile;

    public DataSetStoragePrecommitRecoveryAlgorithm(T dataSetInformation,
            DataStoreStrategyKey dataStoreStrategyKey, File incomingDataSetFile,
            File stagingDirectory, File preCommitDirectory, String dataStoreCode,
            DataSetKind dataSetKind)
    {
        this.recoveryAlgorithm =
                new DataSetStorageRecoveryAlgorithm<T>(dataSetInformation, dataStoreStrategyKey,
                        incomingDataSetFile, stagingDirectory, preCommitDirectory, dataStoreCode,
                        null, dataSetKind);
        this.markerFile = null;
        this.transaction = null;
    }

    public DataSetStoragePrecommitRecoveryAlgorithm(T dataSetInformation,
            DataStoreStrategyKey dataStoreStrategyKey, File incomingDataSetFile,
            File stagingDirectory, File preCommitDirectory, String dataStoreCode,
            DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths, File markerFile,
            IStorageProcessorTransaction transaction)
    {
        this.recoveryAlgorithm =
                new DataSetStorageRecoveryAlgorithm<T>(dataSetInformation, dataStoreStrategyKey,
                        incomingDataSetFile, stagingDirectory, preCommitDirectory, dataStoreCode,
                        dataSetStoragePaths, DataSetKind.PHYSICAL);
        this.transaction = transaction;
        this.markerFile = markerFile;
    }

    public DataSetStorageAlgorithm<T> recoverDataSetStorageAlgorithm(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        return recoveryAlgorithm.recoverDataSetStorageAlgorithm(state, this);
    }

    public IStorageProcessorTransaction getTransaction()
    {
        return transaction;
    }

    public File getMarkerFile()
    {
        return markerFile;
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getDataSetCode()
     */
    public String getDataSetCode()
    {
        return recoveryAlgorithm.getDataSetCode();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getDataSetInformation()
     */
    public T getDataSetInformation()
    {
        return recoveryAlgorithm.getDataSetInformation();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getDataStoreStrategyKey()
     */
    public DataStoreStrategyKey getDataStoreStrategyKey()
    {
        return recoveryAlgorithm.getDataStoreStrategyKey();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getIncomingDataSetFile()
     */
    public File getIncomingDataSetFile()
    {
        return recoveryAlgorithm.getIncomingDataSetFile();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getStagingDirectory()
     */
    public File getStagingDirectory()
    {
        return recoveryAlgorithm.getStagingDirectory();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getPreCommitDirectory()
     */
    public File getPreCommitDirectory()
    {
        return recoveryAlgorithm.getPreCommitDirectory();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getDataStoreCode()
     */
    public String getDataStoreCode()
    {
        return recoveryAlgorithm.getDataStoreCode();
    }

    /**
     * @see ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm#getDataSetStoragePaths()
     */
    public DataSetStoragePaths getDataSetStoragePaths()
    {
        return recoveryAlgorithm.getDataSetStoragePaths();
    }

    public DataSetStorageRecoveryAlgorithm<T> getRecoveryAlgorithm()
    {
        return recoveryAlgorithm;
    }

    @Override
    public DataSetStorageAlgorithm<T> createExternalDataSetStorageAlgorithm(
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IFileOperations fileOperations, IMailClient mailClient)
    {
        return DataSetStorageAlgorithm.createFromPrecommittedRecoveryAlgorithm(dataStoreStrategy,
                storageProcessor, fileOperations, mailClient, this);
    }

}
