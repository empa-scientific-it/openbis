/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class ProjectAuthorizationMigrationTestService
{

    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void methodWithSpaceObserver(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void methodWithProjectObserver(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void methodWithSpaceUser(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void methodWithProjectUser(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    public void methodWithSpacePowerUser(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    public void methodWithSpacePowerUserAndProjectAdmin(IAuthSessionProvider provider,
            @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    public void methodWithProjectPowerUser(IAuthSessionProvider provider, @AuthorizationGuard(guardClass = TestPredicate.class) String value)
    {
    }

}
