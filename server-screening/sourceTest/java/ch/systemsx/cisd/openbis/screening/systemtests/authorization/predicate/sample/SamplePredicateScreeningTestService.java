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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.sample;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.PlateIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ScreeningPlateListReadOnlyPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellSearchCriteriaPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * @author pkupczyk
 */
@Component
public class SamplePredicateScreeningTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testWellIdentifierPredicate(IAuthSessionProvider session,
            @AuthorizationGuard(guardClass = WellIdentifierPredicate.class) WellIdentifier wellIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testPlateIdentifierPredicate(IAuthSessionProvider session,
            @AuthorizationGuard(guardClass = PlateIdentifierPredicate.class) PlateIdentifier plateIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testScreeningPlateListReadOnlyPredicate(IAuthSessionProvider session,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<PlateIdentifier> plateIdentifiers)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testWellSearchCriteriaPredicate(IAuthSessionProvider session,
            @AuthorizationGuard(guardClass = WellSearchCriteriaPredicate.class) WellSearchCriteria wellSearchCriteria)
    {
    }

}
