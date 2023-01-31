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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Abstract superclass that has the state necessary for most storage processors.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Kaloyan Enimanev
 */
public abstract class AbstractStorageProcessorTransaction implements IStorageProcessorTransaction
{

    private static final long serialVersionUID = 1L;

    protected File incomingDataSetDirectory;

    protected File rootDirectory;

    // KE: could optionally be serialized in the future
    protected transient DataSetInformation dataSetInformation;

    protected File storedDataDirectory;

    private enum TransactionState
    {
        INITIAL, STORED, COMMITTED, ROLLED_BACK
    }

    private TransactionState state = TransactionState.INITIAL;

    // ---------------
    // abstract methods to be implemented by extenders
    // --------------

    protected abstract File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient);

    protected abstract void executeCommit();

    protected abstract UnstoreDataAction executeRollback(Throwable ex);

    //
    // Default implementation
    //
    public AbstractStorageProcessorTransaction(StorageProcessorTransactionParameters parameters)
    {
        this.dataSetInformation = parameters.getDataSetInformation();
        this.incomingDataSetDirectory = parameters.getIncomingDataSetDirectory();
        this.rootDirectory = parameters.getRootDir();
    }

    @Override
    public final void storeData(final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetOverride)
    {
        ensureState("storeData", TransactionState.INITIAL);
        this.incomingDataSetDirectory = incomingDataSetOverride;
        this.storedDataDirectory = executeStoreData(typeExtractor, mailClient);
        state = TransactionState.STORED;
    }

    @Override
    public final void commit()
    {
        ensureState("commit", TransactionState.STORED);
        executeCommit();
        state = TransactionState.COMMITTED;
    }

    @Override
    public final UnstoreDataAction rollback(Throwable ex)
    {
        ensureState("rollback", TransactionState.INITIAL, TransactionState.STORED);

        UnstoreDataAction result = executeRollback(ex);
        state = TransactionState.ROLLED_BACK;

        return result;

    }

    @Override
    public final File getStoredDataDirectory()
    {
        return storedDataDirectory;
    }

    @Override
    public final void setStoredDataDirectory(File dir)
    {
        storedDataDirectory = dir;
    }

    private void ensureState(String operation, TransactionState... allowedStates)
    {
        for (TransactionState allowedState : allowedStates)
        {
            if (state == allowedState)
            {
                return;
            }
        }

        String error =
                String.format("Illegal transaction state: '%s' is not allowed while "
                        + "in state '%s'", operation, state);
        throw new IllegalStateException(error);
    }
}