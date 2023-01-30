/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.verify;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;

/**
 * @author pkupczyk
 */
public abstract class VerifyObjectsOperationExecutor<ID extends IObjectId> implements
        IVerifyObjectsOperationExecutor
{

    @Override
    public void verify(IOperationContext context, List<? extends IOperation> operations, Map<IOperation, IOperationResult> results)
    {
        Collection<ID> allIds = new LinkedHashSet<ID>();

        Class<?> createObjectsOperationResultClass = getCreateObjectsOperationResultClass();
        Class<?> updateObjectsOperationResultClass = getUpdateObjectsOperationResultClass();

        for (IOperation operation : operations)
        {
            IOperationResult result = results.get(operation);

            if (result != null)
            {
                if (createObjectsOperationResultClass.isAssignableFrom(result.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    List<ID> ids = ((CreateObjectsOperationResult<ID>) result).getObjectIds();
                    allIds.addAll(ids);
                } else if (updateObjectsOperationResultClass.isAssignableFrom(result.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    List<ID> ids = ((UpdateObjectsOperationResult<ID>) result).getObjectIds();
                    allIds.addAll(ids);
                }
            }
        }

        CollectionBatch<ID> batch = new CollectionBatch<ID>(0, 0, allIds.size(), allIds, allIds.size());

        doVerify(context, batch);
    }

    protected abstract Class<? extends CreateObjectsOperationResult<? extends ID>> getCreateObjectsOperationResultClass();

    protected abstract Class<? extends UpdateObjectsOperationResult<? extends ID>> getUpdateObjectsOperationResultClass();

    protected abstract void doVerify(IOperationContext context, CollectionBatch<ID> ids);

}
