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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.entity;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AtomicOperationsPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.BasicEntityDescriptionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class EntityPredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testBasicEntityDescriptionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = BasicEntityDescriptionPredicate.class) BasicEntityDescription description)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testAtomicOperationsPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = AtomicOperationsPredicate.class) AtomicEntityOperationDetails operation)
    {
    }

}
