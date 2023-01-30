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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class ProjectAuthorizationExecutor implements IProjectAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public void canCreate(IOperationContext context, @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_PROJECT")
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void canUpdate(IOperationContext context, IProjectId id, @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROJECT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_PROJECT")
    public void canDelete(IOperationContext context, IProjectId id, @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_PROJECT")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_PROJECT")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("FREEZE_PROJECT")
    public void canFreeze(IOperationContext context, @AuthorizationGuard(guardClass = ProjectPEPredicate.class) ProjectPE project)
    {
    }

}
