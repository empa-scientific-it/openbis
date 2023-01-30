/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest.task;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpacePEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 *
 */
@Component
public class UserManagerTestService
{
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void allowedForSpaceObservers(IOperationContext context, @AuthorizationGuard(guardClass = SpacePEPredicate.class)  SpacePE space)
    {
    }

    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void allowedForSpaceUsers(IOperationContext context, @AuthorizationGuard(guardClass = SpacePEPredicate.class)  SpacePE space)
    {
    }
    
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    public void allowedForSpaceAdmins(IOperationContext context, @AuthorizationGuard(guardClass = SpacePEPredicate.class)  SpacePE space)
    {
    }
    
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void allowedForInstanceAdmins(IOperationContext context, @AuthorizationGuard(guardClass = SpacePEPredicate.class)  SpacePE space)
    {
    }
    
}
