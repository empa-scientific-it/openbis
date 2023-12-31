/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator;

import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_PROJECT_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_2;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.SampleKind;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class CommonValidatorSystemTestSampleAssertions<O> extends CommonValidatorSystemTestAssertionsDelegate<O>
{

    public CommonValidatorSystemTestSampleAssertions(CommonValidatorSystemTestAssertions<O> delegate)
    {
        super(delegate);
    }

    @Override
    public void assertWithProject11Object(ProjectAuthorizationUser user, O result, Throwable t, Object param)
    {
        assertWithObject(user, result, t, param, new ProjectIdentifier(AUTH_SPACE_1, AUTH_PROJECT_1));
    }

    @Override
    public void assertWithProject21Object(ProjectAuthorizationUser user, O result, Throwable t, Object param)
    {
        assertWithObject(user, result, t, param, new ProjectIdentifier(AUTH_SPACE_2, AUTH_PROJECT_1));
    }

    private void assertWithObject(ProjectAuthorizationUser user, O result, Throwable t, Object param, ProjectIdentifier project)
    {
        if (user.isDisabledProjectUser())
        {
            CommonAuthorizationSystemTest.assertNull(result);
            CommonAuthorizationSystemTest.assertAuthorizationFailureExceptionThatNoRoles(t);
        } else
        {
            CommonAuthorizationSystemTest.assertNoException(t);

            if (SampleKind.SHARED_READ.equals(param) || SampleKind.SHARED_READ_WRITE.equals(param))
            {
                CommonAuthorizationSystemTest.assertNotNull(result);
            } else if (SampleKind.SPACE.equals(param) || SampleKind.SPACE_CONTAINED.equals(param))
            {
                if (user.isInstanceUser())
                {
                    CommonAuthorizationSystemTest.assertNotNull(result);
                } else
                {
                    if (user.isSpaceUser(project.getSpaceCode()))
                    {
                        CommonAuthorizationSystemTest.assertNotNull(result);
                    } else
                    {
                        CommonAuthorizationSystemTest.assertNull(result);
                    }
                }
            } else
            {
                if (user.isInstanceUser())
                {
                    CommonAuthorizationSystemTest.assertNotNull(result);
                } else
                {
                    if (user.isSpaceUser(project.getSpaceCode())
                            || (user.isProjectUser(project.getSpaceCode(), project.getProjectCode()) && user.hasPAEnabled()))
                    {
                        CommonAuthorizationSystemTest.assertNotNull(result);
                    } else
                    {
                        CommonAuthorizationSystemTest.assertNull(result);
                    }
                }
            }
        }
    }

}