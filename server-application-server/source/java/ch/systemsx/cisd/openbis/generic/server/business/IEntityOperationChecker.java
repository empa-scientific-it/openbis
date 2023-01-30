/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author pkupczyk
 */
public interface IEntityOperationChecker
{

    @RolesAllowed(
    { RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    public void assertInstanceSampleCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) List<? extends NewSample> instanceSamples);

    @RolesAllowed(
    { RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    public void assertInstanceSampleUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class) List<? extends SampleIdentifier> instanceSamples);

}
