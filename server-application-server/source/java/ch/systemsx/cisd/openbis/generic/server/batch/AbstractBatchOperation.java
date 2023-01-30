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

package ch.systemsx.cisd.openbis.generic.server.batch;

/**
 * An abstract superclass for batch operations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractBatchOperation<T> implements IBatchOperation<T>
{
    protected final IBatchOperationDelegate<T> delegateOrNull;

    protected AbstractBatchOperation(IBatchOperationDelegate<T> delegateOrNull)
    {
        this.delegateOrNull = delegateOrNull;
    }

    /**
     * Calls {@link IBatchOperationDelegate#batchOperationWillSave()} on the delegate.
     */
    protected void batchOperationWillSave()
    {
        if (null != delegateOrNull)
        {
            this.delegateOrNull.batchOperationWillSave();
        }
    }

    /**
     * Calls {@link IBatchOperationDelegate#batchOperationDidSave()} on the delegate.
     */
    protected void batchOperationDidSave()
    {
        if (null != delegateOrNull)
        {
            this.delegateOrNull.batchOperationDidSave();
        }
    }
}
