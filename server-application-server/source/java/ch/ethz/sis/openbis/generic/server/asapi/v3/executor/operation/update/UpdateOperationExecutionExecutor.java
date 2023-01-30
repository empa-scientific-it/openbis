/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.update;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IOperationExecutionAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateOperationExecutionExecutor
        extends AbstractUpdateEntityExecutor<OperationExecutionUpdate, OperationExecutionPE, IOperationExecutionId, OperationExecutionPermId>
        implements IUpdateOperationExecutionExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IOperationExecutionAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IOperationExecutionStore executionStore;

    @Override
    protected IOperationExecutionId getId(OperationExecutionUpdate update)
    {
        return update.getExecutionId();
    }

    @Override
    protected OperationExecutionPermId getPermId(OperationExecutionPE entity)
    {
        return new OperationExecutionPermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, OperationExecutionUpdate update)
    {
        if (update.getExecutionId() == null)
        {
            throw new UserFailureException("Execution id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IOperationExecutionId id, OperationExecutionPE entity)
    {
        try
        {
            authorizationExecutor.canUpdate(context, id, entity);
        } catch (AuthorizationFailureException ex)
        {
            throw new UnauthorizedObjectAccessException(id);
        }

    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<OperationExecutionUpdate, OperationExecutionPE> batch)
    {
        for (Map.Entry<OperationExecutionUpdate, OperationExecutionPE> entry : batch.getObjects().entrySet())
        {
            OperationExecutionUpdate update = entry.getKey();
            OperationExecutionPE execution = entry.getValue();

            if (update.getDescription() != null && update.getDescription().isModified())
            {
                execution.setDescription(update.getDescription().getValue());
            }

            if (update.isDeleteSummary() && ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.AVAILABLE
                    .equals(execution.getSummaryAvailability()))
            {
                executionStore.executionSummaryAvailability(context, new OperationExecutionPermId(execution.getCode()),
                        OperationExecutionAvailability.DELETE_PENDING);
            }

            if (update.isDeleteDetails() && ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.AVAILABLE
                    .equals(execution.getDetailsAvailability()))
            {
                executionStore.executionDetailsAvailability(context, new OperationExecutionPermId(execution.getCode()),
                        OperationExecutionAvailability.DELETE_PENDING);
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<OperationExecutionUpdate, OperationExecutionPE> batch)
    {
    }

    @Override
    protected Map<IOperationExecutionId, OperationExecutionPE> map(IOperationContext context, Collection<IOperationExecutionId> ids)
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
    protected List<OperationExecutionPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getOperationExecutionDAO().findByIds(ids);
    }

    @Override
    protected void save(IOperationContext context, List<OperationExecutionPE> entities, boolean clearCache)
    {
        for (OperationExecutionPE entity : entities)
        {
            daoFactory.getOperationExecutionDAO().validateAndSaveUpdatedEntity(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "operation execution", null);
    }

}
