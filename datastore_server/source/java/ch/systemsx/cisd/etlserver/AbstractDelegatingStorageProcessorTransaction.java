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

import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;

/**
 * TODO KE: find a better name.
 * <p>
 * An abstract transaction that wraps another transaction.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractDelegatingStorageProcessorTransaction extends
        AbstractStorageProcessorTransaction
{
    protected final IStorageProcessorTransaction nestedTransaction;

    public AbstractDelegatingStorageProcessorTransaction(IStorageProcessorTransaction transaction)
    {
        this.nestedTransaction = transaction;
    }

    public File tryGetProprietaryData()
    {
        return nestedTransaction.tryGetProprietaryData();
    }

}
