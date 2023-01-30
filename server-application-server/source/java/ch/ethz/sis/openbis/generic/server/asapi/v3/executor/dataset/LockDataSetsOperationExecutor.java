/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.LockDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.LockDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class LockDataSetsOperationExecutor
        extends OperationExecutor<LockDataSetsOperation, LockDataSetsOperationResult>
        implements ILockDataSetsOperationExecutor
{
    @Autowired
    private ILockDataSetExecutor executor;

    @Override
    protected Class<? extends LockDataSetsOperation> getOperationClass()
    {
        return LockDataSetsOperation.class;
    }

    @Override
    protected LockDataSetsOperationResult doExecute(IOperationContext context, LockDataSetsOperation operation)
    {
        executor.lock(context, operation.getDataSetIds(), operation.getOptions());
        return new LockDataSetsOperationResult();
    }
}
