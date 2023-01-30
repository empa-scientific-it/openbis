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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateAuthorizationGroupExecutor 
        extends AbstractUpdateEntityExecutor<AuthorizationGroupUpdate, AuthorizationGroupPE, IAuthorizationGroupId, AuthorizationGroupPermId> 
        implements IUpdateAuthorizationGroupExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;
    
    @Autowired
    private IMapGroupPEByIdExecutor mapGroupByIdExecutor;

    @Autowired
    private IUpdateAuthorizationGroupUsersExecutor updateAuthorizationGroupUsersExecutor;
    
    @Override
    protected IAuthorizationGroupId getId(AuthorizationGroupUpdate update)
    {
        return update.getAuthorizationGroupId();
    }

    @Override
    protected AuthorizationGroupPermId getPermId(AuthorizationGroupPE entity)
    {
        return new AuthorizationGroupPermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, AuthorizationGroupUpdate update)
    {
        if (update.getAuthorizationGroupId() == null)
        {
            throw new UserFailureException("Authorization id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IAuthorizationGroupId id, AuthorizationGroupPE entity)
    {
        authorizationExecutor.canUpdate(context);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<AuthorizationGroupUpdate, AuthorizationGroupPE> batch)
    {
        for (Map.Entry<AuthorizationGroupUpdate, AuthorizationGroupPE> entry : batch.getObjects().entrySet())
        {
            AuthorizationGroupUpdate update = entry.getKey();
            AuthorizationGroupPE group = entry.getValue();
            if (update.getDescription() != null && update.getDescription().isModified())
            {
                group.setDescription(update.getDescription().getValue());
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<AuthorizationGroupUpdate, AuthorizationGroupPE> batch)
    {
        updateAuthorizationGroupUsersExecutor.update(context, batch);
    }

    @Override
    protected Map<IAuthorizationGroupId, AuthorizationGroupPE> map(IOperationContext context, Collection<IAuthorizationGroupId> ids)
    {
        return mapGroupByIdExecutor.map(context, ids);
    }

    @Override
    protected List<AuthorizationGroupPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getAuthorizationGroupDAO().listByIds(ids);
    }

    @Override
    protected void save(IOperationContext context, List<AuthorizationGroupPE> entities, boolean clearCache)
    {
        // Entities updated by Hibernate at the end of the transaction
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "authorization group", null);
    }
}
