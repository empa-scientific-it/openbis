/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.delete;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IOperationExecutionAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteOperationExecutionExecutor
        extends AbstractDeleteEntityExecutor<Void, IOperationExecutionId, OperationExecutionPE, OperationExecutionDeletionOptions> implements
        IDeleteOperationExecutionExecutor
{
    @Autowired
    private IOperationExecutionAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IOperationExecutionStore executionStore;

    @Override
    protected Map<IOperationExecutionId, OperationExecutionPE> map(IOperationContext context, List<? extends IOperationExecutionId> ids,
            OperationExecutionDeletionOptions deletionOptions)
    {
        Map<IOperationExecutionId, OperationExecutionPE> map = new LinkedHashMap<IOperationExecutionId, OperationExecutionPE>();

        for (IOperationExecutionId id : ids)
        {
            OperationExecution execution = executionStore.getExecution(context, id, new OperationExecutionFetchOptions());
            if (execution != null)
            {
                OperationExecutionPE executionPE = daoFactory.getOperationExecutionDAO().tryFindByCode(execution.getCode());
                if (executionPE != null)
                {
                    map.put(id, executionPE);
                }
            }

        }

        return map;
    }

    @Override
    protected void checkAccess(IOperationContext context, IOperationExecutionId entityId, OperationExecutionPE entity)
    {
        try
        {
            authorizationExecutor.canDelete(context, entityId, entity);
        } catch (AuthorizationFailureException ex)
        {
            throw new UnauthorizedObjectAccessException(entityId);
        }
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, OperationExecutionPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<OperationExecutionPE> executions, OperationExecutionDeletionOptions deletionOptions)
    {
        for (OperationExecutionPE execution : executions)
        {
            executionStore.executionAvailability(context, new OperationExecutionPermId(execution.getCode()),
                    OperationExecutionAvailability.DELETE_PENDING);
        }
        return null;
    }

}
