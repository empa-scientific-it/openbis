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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.entity;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestSpaceAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class EntityHistoryValidatorWithSpaceSystemTest extends EntityHistoryValidatorSystemTest
{

    @Override
    protected EntityHistory createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        Space space = new Space();
        space.setCode(spacePE.getCode());

        EntityHistory history = new EntityHistory();
        history.setRelatedSpace(space);

        return history;
    }

    @Override
    protected CommonValidatorSystemTestAssertions<EntityHistory> getAssertions()
    {
        return new CommonValidatorSystemTestSpaceAssertions<EntityHistory>(super.getAssertions())
            {
                @Override
                public void assertWithProject11Object(ProjectAuthorizationUser user, EntityHistory result, Throwable t, Object param)
                {
                    if (user.isProject11User() && false == user.isDisabledProjectUser())
                    {
                        CommonAuthorizationSystemTest.assertNoException(t);
                        CommonAuthorizationSystemTest.assertNotNull(result);
                    } else
                    {
                        super.assertWithProject11Object(user, result, t, param);
                    }
                }

                @Override
                public void assertWithProject21Object(ProjectAuthorizationUser user, EntityHistory result, Throwable t, Object param)
                {
                    if (user.isProject21User() && false == user.isDisabledProjectUser())
                    {
                        CommonAuthorizationSystemTest.assertNoException(t);
                        CommonAuthorizationSystemTest.assertNotNull(result);
                    } else
                    {
                        super.assertWithProject21Object(user, result, t, param);
                    }
                }
            };
    }

}
