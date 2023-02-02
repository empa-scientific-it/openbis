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

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonPredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class WellIdentifierPredicateSystemTest extends CommonPredicateScreeningSystemTest<WellIdentifier>
{

    @Override
    public Object[] getParams()
    {
        return getSampleKinds(SampleKind.SHARED_READ_WRITE);
    }

    @Override
    protected WellIdentifier createNonexistentObject(Object param)
    {
        return new WellIdentifier(null, null, "IDONTEXIST");
    }

    @Override
    protected WellIdentifier createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = getSample(spacePE, projectPE, (SampleKind) param);
        return new WellIdentifier(null, null, samplePE.getPermId());
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<WellIdentifier> objects, Object param)
    {
        getBean(SamplePredicateScreeningTestService.class).testWellIdentifierPredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<WellIdentifier> getAssertions()
    {
        return new CommonPredicateSystemTestSampleAssertions<WellIdentifier>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, UserFailureException.class, "No well identifier specified.");
                    }
                }

                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, AuthorizationFailureException.class, ".*There is no sample with perm id 'IDONTEXIST'.*");
                    }
                }
            };
    }

}
