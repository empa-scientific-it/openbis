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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate;

import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_PROJECT_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_2;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class CommonPredicateSystemTestSpaceAssertions<O> extends CommonPredicateSystemTestAssertionsDelegate<O>
{

    public CommonPredicateSystemTestSpaceAssertions(CommonPredicateSystemTestAssertions<O> delegate)
    {
        super(delegate);
    }

    @Override
    public void assertWithProject11Object(ProjectAuthorizationUser user, Throwable t, Object param)
    {
        assertWithObject(user, t, param, new ProjectIdentifier(AUTH_SPACE_1, AUTH_PROJECT_1));
    }

    @Override
    public void assertWithProject21Object(ProjectAuthorizationUser user, Throwable t, Object param)
    {
        assertWithObject(user, t, param, new ProjectIdentifier(AUTH_SPACE_2, AUTH_PROJECT_1));
    }

    @Override
    public void assertWithProject11ObjectAndProject21Object(ProjectAuthorizationUser user, Throwable t, Object param)
    {
        assertWithObject(user, t, param, new ProjectIdentifier(AUTH_SPACE_1, AUTH_PROJECT_1), new ProjectIdentifier(AUTH_SPACE_2, AUTH_PROJECT_1));
    }

    private void assertWithObject(ProjectAuthorizationUser user, Throwable t, Object param, ProjectIdentifier... projects)
    {
        if (user.isDisabledProjectUser())
        {
            CommonAuthorizationSystemTest.assertAuthorizationFailureExceptionThatNoRoles(t);
        } else
        {
            if (user.isInstanceUser())
            {
                CommonAuthorizationSystemTest.assertNoException(t);
            } else
            {
                boolean hasAccess = true;

                for (ProjectIdentifier project : projects)
                {
                    hasAccess = hasAccess && user.isSpaceUser(project.getSpaceCode());
                }

                if (hasAccess)
                {
                    CommonAuthorizationSystemTest.assertNoException(t);
                } else
                {
                    CommonAuthorizationSystemTest.assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
                }
            }
        }
    }

}
