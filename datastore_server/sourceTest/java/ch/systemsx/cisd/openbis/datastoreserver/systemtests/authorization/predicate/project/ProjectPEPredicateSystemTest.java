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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.project;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.project.ProjectPredicateTestService;

/**
 * @author pkupczyk
 */
public class ProjectPEPredicateSystemTest extends CommonPredicateSystemTest<ProjectPE>
{

    @Override
    protected ProjectPE createNonexistentObject(Object param)
    {
        SpacePE space = new SpacePE();
        space.setCode("IDONTEXIST");

        ProjectPE project = new ProjectPE();
        project.setCode("IDONTEXIST");
        project.setSpace(space);

        return project;
    }

    @Override
    protected ProjectPE createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return projectPE;
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<ProjectPE> objects, Object param)
    {
        getBean(ProjectPredicateTestService.class).testProjectPEPredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<ProjectPE> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<ProjectPE>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    assertException(t, NullPointerException.class, null);
                }

                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isInstanceUser())
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
