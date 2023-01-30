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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.space;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSpaceIdentifierOrProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author pkupczyk
 */
@Component
public class SpacePredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSpaceIdentifierPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) SpaceIdentifier spaceIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExistingSpaceIdentifierPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class) SpaceIdentifier spaceIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExistingSpaceIdentifierOrProjectPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierOrProjectPredicate.class) SpaceIdentifier spaceIdentifier)
    {
    }

}
