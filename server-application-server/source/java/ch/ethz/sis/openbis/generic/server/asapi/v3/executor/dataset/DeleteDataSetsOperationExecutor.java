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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.delete.DeleteObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class DeleteDataSetsOperationExecutor extends DeleteObjectsOperationExecutor<IDataSetId, DataSetDeletionOptions> implements
        IDeleteDataSetsOperationExecutor
{

    @Autowired
    private IDeleteDataSetExecutor executor;

    @Override
    protected Class<? extends DeleteObjectsOperation<IDataSetId, DataSetDeletionOptions>> getOperationClass()
    {
        return DeleteDataSetsOperation.class;
    }

    @Override
    protected DeleteObjectsOperationResult doExecute(IOperationContext context, DeleteObjectsOperation<IDataSetId, DataSetDeletionOptions> operation)
    {
        return new DeleteDataSetsOperationResult(executor.delete(context, operation.getObjectIds(), operation.getOptions()));
    }

}
