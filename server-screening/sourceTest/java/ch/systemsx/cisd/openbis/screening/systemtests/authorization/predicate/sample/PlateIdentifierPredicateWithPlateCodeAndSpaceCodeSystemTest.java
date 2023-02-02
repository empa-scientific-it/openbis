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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author pkupczyk
 */
public class PlateIdentifierPredicateWithPlateCodeAndSpaceCodeSystemTest extends PlateIdentifierPredicateSystemTest
{

    @Override
    protected PlateIdentifier createNonexistentObject(Object param)
    {
        return PlateIdentifierUtil.createNonexistentObjectWithPlateCodeAndSpaceCode(param);
    }

    @Override
    protected PlateIdentifier createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return PlateIdentifierUtil.createObjectWithPlateCodeAndSpaceCode(this, spacePE, projectPE, param);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<PlateIdentifier> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<PlateIdentifier>(super.getAssertions())
            {
                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else if (user.isInstanceUser() || SampleKind.SHARED_READ.equals(param))
                    {
                        assertNoException(t);
                    } else
                    {
                        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
                    }
                }
            };
    }

}
