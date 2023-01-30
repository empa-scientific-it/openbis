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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.UnlockDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.UnlockDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UnlockDataSetsOperationExecutor
        extends OperationExecutor<UnlockDataSetsOperation, UnlockDataSetsOperationResult>
        implements IUnlockDataSetsOperationExecutor
{
    @Autowired
    private IUnlockDataSetExecutor executor;

    @Override
    protected Class<? extends UnlockDataSetsOperation> getOperationClass()
    {
        return UnlockDataSetsOperation.class;
    }

    @Override
    protected UnlockDataSetsOperationResult doExecute(IOperationContext context, UnlockDataSetsOperation operation)
    {
        executor.unlock(context, operation.getDataSetIds(), operation.getOptions());
        return new UnlockDataSetsOperationResult();
    }
}
