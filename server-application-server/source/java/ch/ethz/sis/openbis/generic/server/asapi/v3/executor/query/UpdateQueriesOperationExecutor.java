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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.UpdateQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.UpdateQueriesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateQueriesOperationExecutor extends UpdateObjectsOperationExecutor<QueryUpdate, IQueryId> implements IUpdateQueriesOperationExecutor
{

    @Autowired
    private IUpdateQueryExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<QueryUpdate>> getOperationClass()
    {
        return UpdateQueriesOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IQueryId> doExecute(IOperationContext context, UpdateObjectsOperation<QueryUpdate> operation)
    {
        return new UpdateQueriesOperationResult(executor.update(context, operation.getUpdates()));
    }

}
