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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.deletion;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DeletionTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.RevertDeletionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3DeletionIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class DeletionPredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDeletionTechIdCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DeletionTechIdCollectionPredicate.class) List<TechId> deletionTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testRevertDeletionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = RevertDeletionPredicate.class) List<TechId> deletionTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testV3DeletionIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = V3DeletionIdPredicate.class) List<IDeletionId> deletionIds)
    {
    }

}
