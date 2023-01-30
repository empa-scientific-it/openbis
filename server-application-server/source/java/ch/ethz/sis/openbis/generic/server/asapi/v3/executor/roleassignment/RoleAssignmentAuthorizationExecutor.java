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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpacePEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class RoleAssignmentAuthorizationExecutor implements IRoleAssignmentAuthorizationExecutor
{

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("GET_ROLE_ASSIGNMENT")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("CREATE_INSTANCE_ROLE")
    public void canCreateInstanceRole(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("CREATE_SPACE_ROLE")
    public void canCreateSpaceRole(IOperationContext context,
            @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE space)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("CREATE_PROJECT_ROLE")
    public void canCreateProjectRole(IOperationContext context,
            @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
        if (false == authorizationConfig.isProjectLevelEnabled())
        {
            throw new UserFailureException("Project authorization is disabled. Project level roles cannot be manipulated.");
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("SEARCH_ROLE_ASSIGNMENT")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("DELETE_INSTANCE_ROLE")
    public void canDeleteInstanceRole(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("DELETE_SPACE_ROLE")
    public void canDeleteSpaceRole(IOperationContext context,
            @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE space)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("DELETE_PROJECT_ROLE")
    public void canDeleteProjectRole(IOperationContext context,
            @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
        if (false == authorizationConfig.isProjectLevelEnabled())
        {
            throw new UserFailureException("Project authorization is disabled. Project level roles cannot be manipulated.");
        }
    }

}
