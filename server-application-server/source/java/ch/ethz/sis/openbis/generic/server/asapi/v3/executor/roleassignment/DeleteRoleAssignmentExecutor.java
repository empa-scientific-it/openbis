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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeleteRoleAssignmentExecutor
        extends AbstractDeleteEntityExecutor<Void, IRoleAssignmentId, RoleAssignmentPE, RoleAssignmentDeletionOptions>
        implements IDeleteRoleAssignmentExecutor
{
    @Autowired
    private IMapRoleAssignmentPEByIdExecutor mapRoleAssignmentByIdExecutor;

    @Autowired
    private IRoleAssignmentAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IRoleAssignmentId, RoleAssignmentPE> map(IOperationContext context, List<? extends IRoleAssignmentId> entityIds,
            RoleAssignmentDeletionOptions deletionOptions)
    {
        return mapRoleAssignmentByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IRoleAssignmentId entityId, RoleAssignmentPE roleAssignment)
    {
        ProjectPE project = roleAssignment.getProject();
        if (project != null)
        {
            authorizationExecutor.canDeleteProjectRole(context, project);
        } else
        {
            SpacePE space = roleAssignment.getSpace();
            if (space != null)
            {
                authorizationExecutor.canDeleteSpaceRole(context, space);
            } else
            {
                authorizationExecutor.canDeleteInstanceRole(context);
            }
        }
        final PersonPE personPE = context.getSession().tryGetPerson();
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(roleAssignment, personPE);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, RoleAssignmentPE entity)
    {
    }

    @Override
    protected Void delete(IOperationContext context, Collection<RoleAssignmentPE> entities, RoleAssignmentDeletionOptions deletionOptions)
    {
        IRoleAssignmentDAO roleAssignmentDAO = daoFactory.getRoleAssignmentDAO();
        for (RoleAssignmentPE entity : entities)
        {
            roleAssignmentDAO.delete(entity);
        }
        return null;
    }

}
