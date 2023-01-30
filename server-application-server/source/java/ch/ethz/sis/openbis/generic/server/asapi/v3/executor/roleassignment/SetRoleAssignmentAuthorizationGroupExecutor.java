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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.IMapGroupPEByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetRoleAssignmentAuthorizationGroupExecutor 
        extends AbstractSetEntityToOneRelationExecutor<RoleAssignmentCreation, RoleAssignmentPE, IAuthorizationGroupId, AuthorizationGroupPE>
        implements ISetRoleAssignmentAuthorizationGroupExecutor
{
    @Autowired
    private IMapGroupPEByIdExecutor mapGroupByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "roleassignment-authorizationgroup";
    }

    @Override
    protected IAuthorizationGroupId getRelatedId(RoleAssignmentCreation creation)
    {
        return creation.getAuthorizationGroupId();
    }

    @Override
    protected Map<IAuthorizationGroupId, AuthorizationGroupPE> map(IOperationContext context, List<IAuthorizationGroupId> relatedIds)
    {
        return mapGroupByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, RoleAssignmentPE entity, IAuthorizationGroupId relatedId, AuthorizationGroupPE related)
    {
    }

    @Override
    protected void set(IOperationContext context, RoleAssignmentPE entity, AuthorizationGroupPE related)
    {
        entity.setAuthorizationGroupInternal(related);
    }

}
