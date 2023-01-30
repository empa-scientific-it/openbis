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
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeleteAuthorizationGroupExecutor
        extends AbstractDeleteEntityExecutor<Void, IAuthorizationGroupId, AuthorizationGroupPE, AuthorizationGroupDeletionOptions>
        implements IDeleteAuthorizationGroupExecutor
{
    @Autowired
    private IMapGroupPEByIdExecutor mapGroupByIdExecutor;

    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IAuthorizationGroupId, AuthorizationGroupPE> map(IOperationContext context, List<? extends IAuthorizationGroupId> entityIds,
            AuthorizationGroupDeletionOptions deletionOptions)
    {
        return mapGroupByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IAuthorizationGroupId entityId, AuthorizationGroupPE entity)
    {
        authorizationExecutor.canDelete(context);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, AuthorizationGroupPE entity)
    {
    }

    @Override
    protected Void delete(IOperationContext context, Collection<AuthorizationGroupPE> entities, AuthorizationGroupDeletionOptions deletionOptions)
    {
        IAuthorizationGroupBO groupBO = businessObjectFactory.createAuthorizationGroupBO(context.getSession());
        for (AuthorizationGroupPE group : entities)
        {
            groupBO.deleteByTechId(new TechId(group.getId()), deletionOptions.getReason());
        }
        return null;
    }

}
