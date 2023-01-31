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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.deletion;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class DeletionValidatorWithSampleSystemTest extends DeletionValidatorSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getSampleKinds(SampleKind.SHARED_READ);
    }

    @Override
    protected Deletion createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE sample = getSample(spacePE, projectPE, (SampleKind) param);
        return getCommonService().trashSample(sample);
    }

    @Override
    protected CommonValidatorSystemTestAssertions<Deletion> getAssertions()
    {
        return new CommonValidatorSystemTestSampleAssertions<Deletion>(super.getAssertions())
            {
                @Override
                public void assertWithProject11Object(ProjectAuthorizationUser user, Deletion result, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        CommonAuthorizationSystemTest.assertNull(result);
                        CommonAuthorizationSystemTest.assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        if (SampleKind.SHARED_READ.equals(param))
                        {
                            assertNoException(t);

                            if (user.isInstanceUser())
                            {
                                assertNotNull(result);
                            } else
                            {
                                assertNull(result);
                            }
                        } else
                        {
                            super.assertWithProject11Object(user, result, t, param);
                        }
                    }
                }

                @Override
                public void assertWithProject21Object(ProjectAuthorizationUser user, Deletion result, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        CommonAuthorizationSystemTest.assertNull(result);
                        CommonAuthorizationSystemTest.assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        if (SampleKind.SHARED_READ.equals(param))
                        {
                            assertNoException(t);

                            if (user.isInstanceUser())
                            {
                                assertNotNull(result);
                            } else
                            {
                                assertNull(result);
                            }
                        } else
                        {
                            super.assertWithProject21Object(user, result, t, param);
                        }
                    }
                }

            };
    }

}
