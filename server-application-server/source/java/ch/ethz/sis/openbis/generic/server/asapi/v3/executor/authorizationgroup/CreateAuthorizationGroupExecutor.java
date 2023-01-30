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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
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
public class CreateAuthorizationGroupExecutor 
        extends AbstractCreateEntityExecutor<AuthorizationGroupCreation, AuthorizationGroupPE, AuthorizationGroupPermId> 
        implements ICreateAuthorizationGroupExecutor
{
    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private ISetAuthorizationGroupUsersExecutor setAuthorizationGroupUsersExecutor;

    @Override
    protected IObjectId getId(AuthorizationGroupPE entity)
    {
        return new AuthorizationGroupPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, AuthorizationGroupCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

    @Override
    protected void checkAccess(IOperationContext context, AuthorizationGroupPE entity)
    {
    }

    @Override
    protected List<AuthorizationGroupPE> createEntities(IOperationContext context, CollectionBatch<AuthorizationGroupCreation> batch)
    {
        final List<AuthorizationGroupPE> groups = new LinkedList<AuthorizationGroupPE>();
        new CollectionBatchProcessor<AuthorizationGroupCreation>(context, batch)
            {
                @Override
                public void process(AuthorizationGroupCreation object)
                {
                    AuthorizationGroupPE group = new AuthorizationGroupPE();
                    group.setCode(object.getCode());
                    group.setDescription(object.getDescription());
                    group.setRegistrator(context.getSession().tryGetCreatorPerson());
                    groups.add(group);
                }

                @Override
                public IProgress createProgress(AuthorizationGroupCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        return groups;
    }

    @Override
    protected AuthorizationGroupPermId createPermId(IOperationContext context, AuthorizationGroupPE entity)
    {
        return new AuthorizationGroupPermId(entity.getPermId());
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<AuthorizationGroupCreation, AuthorizationGroupPE> batch)
    {
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<AuthorizationGroupCreation, AuthorizationGroupPE> batch)
    {
        setAuthorizationGroupUsersExecutor.set(context, batch);
    }

    @Override
    protected List<AuthorizationGroupPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getAuthorizationGroupDAO().listByIds(ids);
    }

    @Override
    protected void save(IOperationContext context, List<AuthorizationGroupPE> entities, boolean clearCache)
    {
        for (AuthorizationGroupPE group : entities)
        {
            daoFactory.getAuthorizationGroupDAO().create(group);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "authorization group", null);
    }
}
