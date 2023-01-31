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

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.NullStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStoragePrecommitRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageStoredRecoveryAlgorithm;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * An implementation of the storage algorithm that registers data sets without file contents. This requires a different registration process. In
 * particular there is no storage part.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractNoFileDataSetStorageAlgorithm<T extends DataSetInformation> extends
        DataSetStorageAlgorithm<T>
{

    /**
     * @param incomingDataSetFile
     * @param registrationDetails
     * @param dataStoreStrategy
     * @param storageProcessor
     * @param dataSetValidator
     * @param dataStoreCode
     * @param fileOperations
     * @param mailClient
     * @param stagingDirectory
     */
    public AbstractNoFileDataSetStorageAlgorithm(File incomingDataSetFile,
            DataSetRegistrationDetails<? extends T> registrationDetails,
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IDataSetValidator dataSetValidator, String dataStoreCode,
            IFileOperations fileOperations, IMailClient mailClient, File stagingDirectory,
            File precommitDirectory)
    {
        super(incomingDataSetFile, registrationDetails, dataStoreStrategy, storageProcessor,
                dataSetValidator, dataStoreCode, fileOperations, mailClient, stagingDirectory,
                precommitDirectory);
    }

    /**
     * Creates algorithm from a recovery algorithm.
     */
    public AbstractNoFileDataSetStorageAlgorithm(IDataStoreStrategy dataStoreStrategy,
            IStorageProcessorTransactional storageProcessor, IFileOperations fileOperations,
            IMailClient mailClient, DataSetStorageRecoveryAlgorithm<T> recoveryAlgorithm)
    {
        super(dataStoreStrategy, storageProcessor, fileOperations, mailClient, recoveryAlgorithm);
    }

    @Override
    public IStorageProcessorTransaction prepare(IRollbackStack rollbackStack)
    {
        // assert initialized - move to prepared state.
        return new NullStorageProcessorTransaction();
    }

    @Override
    public void preCommit() throws Throwable
    {
        // assert prepared state, leave in precommit
    }

    @Override
    public void moveToTheStore() throws Throwable
    {
        // assert commited state, leave in stored
    }

    @Override
    public void transitionToRolledbackState(Throwable throwable)
    {
        // return in rolledback state
    }

    @Override
    public void transitionToUndoneState()
    {
        // return in undone state
    }

    @Override
    public void commitStorageProcessor()
    {
        // assert precommitted state - return in committed state
    }

    @Override
    public void cleanPrecommitDirectory()
    {
        // do nothing
    }

    @Override
    public String getSuccessRegistrationMessage()
    {
        // The success registration message is the same as the superclass, but for clarity, make
        // that explicit.
        return super.getSuccessRegistrationMessage();
    }

    @Override
    public String getFailureRegistrationMessage()
    {
        return "Error trying to register no-file-contents data set '"
                + getDataSetInformation().toString()
                + "'.";
    }

    @Override
    public DataSetStoragePrecommitRecoveryAlgorithm<T> getPrecommitRecoveryAlgorithm()
    {
        return new DataSetStoragePrecommitRecoveryAlgorithm<T>(getDataSetInformation(),
                getDataStoreStrategy().getKey(), getIncomingDataSetFile(), getStagingDirectory(),
                getPreCommitDirectory(), getDataStoreCode(), getDataSetKind());
    }

    @Override
    public DataSetStorageStoredRecoveryAlgorithm<T> getStoredRecoveryAlgorithm()
    {
        return new DataSetStorageStoredRecoveryAlgorithm<T>(getDataSetInformation(),
                getDataStoreStrategy().getKey(), getIncomingDataSetFile(), getStagingDirectory(),
                getPreCommitDirectory(), getDataStoreCode(), getDataSetKind());
    }

    @Override
    public abstract DataSetKind getDataSetKind();
}
