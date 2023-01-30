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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpacePEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class PersonAuthorizationExecutor implements IPersonAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_PERSON")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_PERSON")
    public void canCreate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("DEACTIVATE_PERSON")
    public void canDeactivate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("ACTIVATE_PERSON")
    public void canActivate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("UPDATE_HOME_SPACE")
    public void canUpdateHomeSpace(IOperationContext context,
            @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE newHomeSpace)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_PERSON")
    public void canDelete(IOperationContext context)
    {
    }

}
