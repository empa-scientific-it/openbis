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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class SampleAugmentedCodePredicateSystemTest extends CommonPredicateSystemTest<String>
{

    @Override
    public Object[] getParams()
    {
        return getSampleKinds(SampleKind.SHARED_READ);
    }

    @Override
    protected String createNonexistentObject(Object param)
    {
        switch ((SampleKind) param)
        {
            case SHARED_READ:
                return "/IDONTEXIST";
            case SPACE:
                return "/IDONTEXIST/IDONTEXIST";
            case SPACE_CONTAINED:
                return "/IDONTEXIST/IDONTEXIST:IDONTEXIST";
            case PROJECT:
                return "/IDONTEXIST/IDONTEXIST/IDONTEXIST";
            case EXPERIMENT:
                return "/IDONTEXIST/IDONTEXIST";
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected String createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = getSample(spacePE, projectPE, (SampleKind) param);

        switch ((SampleKind) param)
        {
            case SHARED_READ:
                return "/" + samplePE.getCode();
            case SPACE:
                return "/" + spacePE.getCode() + "/" + samplePE.getCode();
            case SPACE_CONTAINED:
                return "/" + spacePE.getCode() + "/" + samplePE.getContainer().getCode() + ":" + samplePE.getCode();
            case PROJECT:
                return "/" + spacePE.getCode() + "/" + projectPE.getCode() + "/" + samplePE.getCode();
            case EXPERIMENT:
                return "/" + spacePE.getCode() + "/" + samplePE.getCode();
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<String> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testSampleAugmentedCodePredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<String> getAssertions()
    {
        return new CommonPredicateSystemTestSampleAssertions<String>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    assertException(t, UserFailureException.class, "No sample specified.");
                }

                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isInstanceUser() || SampleKind.SHARED_READ.equals(param))
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
