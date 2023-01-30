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
 * A shell superclass for batch operation delegates. Subclasses only need to override those methods they actually use.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractBatchOperationDelegate<T> implements IBatchOperationDelegate<T>
{

    @Override
    public void batchOperationWillSave()
    {
        // subclasses may override
    }

    @Override
    public void batchOperationDidSave()
    {
        // subclasses may override
    }

}
