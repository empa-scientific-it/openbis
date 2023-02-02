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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonPredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public abstract class PlateIdentifierPredicateSystemTest extends CommonPredicateScreeningSystemTest<PlateIdentifier>
{

    @Override
    public Object[] getParams()
    {
        return PlateIdentifierUtil.provideParams(SampleKind.SHARED_READ);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<PlateIdentifier> objects, Object param)
    {
        getBean(SamplePredicateScreeningTestService.class).testPlateIdentifierPredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<PlateIdentifier> getAssertions()
    {
        return new CommonPredicateSystemTestSampleAssertions<PlateIdentifier>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, UserFailureException.class, "No plate identifier specified.");
                    }
                }
            };
    }

}
