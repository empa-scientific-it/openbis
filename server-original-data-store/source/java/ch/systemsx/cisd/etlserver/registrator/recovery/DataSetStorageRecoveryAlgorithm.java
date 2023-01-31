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
import ch.systemsx.cisd.etlserver.registrator.v2.ContainerDataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.v2.LinkDataSetStorageAlgorithm;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author jakubs
 */
public class DataSetStorageRecoveryAlgorithm<T extends DataSetInformation> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final DataStoreStrategyKey dssKey;

    private final File incomingDataSetFile;

    private final File stagingDirectory;

    private final File preCommitDirectory;

    private final String dataStoreCode;

    private final T dataSetInformation;

    private final DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths;

    private final DataSetKind dataSetKind;

    public DataSetStorageRecoveryAlgorithm(T dataSetInformation,
            DataStoreStrategyKey dataStoreStrategyKey, File incomingDataSetFile,
            File stagingDirectory, File preCommitDirectory, String dataStoreCode,
            DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths, DataSetKind dataSetKind)
    {
        this.dataSetInformation = dataSetInformation;

        this.dssKey = dataStoreStrategyKey;
        this.incomingDataSetFile = incomingDataSetFile;
        this.stagingDirectory = stagingDirectory;
        this.preCommitDirectory = preCommitDirectory;

        this.dataStoreCode = dataStoreCode;

        this.dataSetStoragePaths = dataSetStoragePaths;

        this.dataSetKind = dataSetKind;
    }

    public String getDataSetCode()
    {
        return dataSetInformation.getDataSetCode();
    }

    public T getDataSetInformation()
    {
        return dataSetInformation;
    }

    public DataStoreStrategyKey getDataStoreStrategyKey()
    {
        return dssKey;
    }

    public File getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public File getStagingDirectory()
    {
        return stagingDirectory;
    }

    public File getPreCommitDirectory()
    {
        return preCommitDirectory;
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public DataSetStorageAlgorithm.DataSetStoragePaths getDataSetStoragePaths()
    {
        return dataSetStoragePaths;
    }

    public DataSetKind getDataSetKind()
    {
        return dataSetKind;
    }

    public DataSetStorageAlgorithm<T> recoverDataSetStorageAlgorithm(
            OmniscientTopLevelDataSetRegistratorState state,
            IDataSetStorageRecoveryAlgorithmWithState<T> algorithm)
    {
        IDataStoreStrategy dataStoreStrategy =
                state.getDataStrategyStore().getDataStoreStrategy(getDataStoreStrategyKey());

        IMailClient mailClient = state.getGlobalState().getMailClient();
        IFileOperations fileOperations = state.getFileOperations();

        IStorageProcessorTransactional storageProcessor = state.getStorageProcessor();

        switch (getDataSetKind())
        {
            case CONTAINER:
                return new ContainerDataSetStorageAlgorithm<T>(dataStoreStrategy, storageProcessor,
                        fileOperations, mailClient, this);
            case LINK:
                return new LinkDataSetStorageAlgorithm<T>(dataStoreStrategy, storageProcessor,
                        fileOperations, mailClient, this);
            case PHYSICAL:
                return algorithm.createExternalDataSetStorageAlgorithm(dataStoreStrategy,
                        storageProcessor, fileOperations, mailClient);
            default:
                throw new IllegalStateException("Unknown data set kind " + getDataSetKind());
        }
    }
}
