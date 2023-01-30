/*
 * Copyright 2017 ETH Zuerich, SIS
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateDataSetTypesOperationExecutor
        extends UpdateObjectsOperationExecutor<DataSetTypeUpdate, IEntityTypeId>
        implements IUpdateDataSetTypesOperationExecutor
{
    @Autowired
    private IUpdateDataSetTypeExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<DataSetTypeUpdate>> getOperationClass()
    {
        return UpdateDataSetTypesOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IEntityTypeId> doExecute(IOperationContext context,
            UpdateObjectsOperation<DataSetTypeUpdate> operation)
    {
        return new UpdateDataSetTypesOperationResult(executor.update(context, operation.getUpdates()));
    }

}
