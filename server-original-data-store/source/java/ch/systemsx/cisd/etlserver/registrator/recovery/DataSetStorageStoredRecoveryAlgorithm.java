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
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm.DataSetStoragePaths;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author jakubs
 */
public class DataSetStorageStoredRecoveryAlgorithm<T extends DataSetInformation> implements
        Serializable, IDataSetStorageRecoveryAlgorithmWithState<T>
{
    private static final long serialVersionUID = 1L;

    private DataSetStorageRecoveryAlgorithm<T> recoveryAlgorithm;

    public DataSetStorageStoredRecoveryAlgorithm(T dataSetInformation,
            DataStoreStrategyKey dataStoreStrategyKey, File incomingDataSetFile,
            File stagingDirectory, File preCommitDirectory, String dataStoreCode,
            DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths)
    {
        recoveryAlgorithm =
                new DataSetStorageRecoveryAlgorithm<T>(dataSetInformation, dataStoreStrategyKey,
                        incomingDataSetFile, stagingDirectory, preCommitDirectory, dataStoreCode,
                        dataSetStoragePaths, DataSetKind.PHYSICAL);
    }

    public DataSetStorageStoredRecoveryAlgorithm(T dataSetInformation,
            DataStoreStrategyKey dataStoreStrategyKey, File incomingDataSetFile,
            File stagingDirectory, File preCommitDirectory, String dataStoreCode,
            DataSetKind dataSetKind)
    {
        recoveryAlgorithm =
                new DataSetStorageRecoveryAlgorithm<T>(dataSetInformation, dataStoreStrategyKey,
                        incomingDataSetFile, stagingDirectory, preCommitDirectory, dataStoreCode,
                        null, dataSetKind);
    }

    public DataSetStorageAlgorithm<T> recoverDataSetStorageAlgorithm(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        return recoveryAlgorithm.recoverDataSetStorageAlgorithm(state, this);
    }

    public DataSetStorageRecoveryAlgorithm<T> getRecoveryAlgorithm()
    {
        return recoveryAlgorithm;
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

    @Override
    public DataSetStorageAlgorithm<T> createExternalDataSetStorageAlgorithm(
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IFileOperations fileOperations, IMailClient mailClient)
    {
        return DataSetStorageAlgorithm.createFromStoredRecoveryAlgorithm(dataStoreStrategy,
                storageProcessor, fileOperations, mailClient, this);
    }
}
