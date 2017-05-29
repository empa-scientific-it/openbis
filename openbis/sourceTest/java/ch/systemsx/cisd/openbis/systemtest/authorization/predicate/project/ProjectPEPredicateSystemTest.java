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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.CommonPredicateSystemTest;

/**
 * @author pkupczyk
 */
public class ProjectPEPredicateSystemTest extends CommonPredicateSystemTest<ProjectPE>
{

    @Autowired
    private ProjectPredicateTestService service;

    @Override
    protected ProjectPE createNonexistentObject()
    {
        SpacePE space = new SpacePE();
        space.setCode("IDONTEXIST");

        ProjectPE project = new ProjectPE();
        project.setCode("IDONTEXIST");
        project.setSpace(space);

        return project;
    }

    @Override
    protected ProjectPE createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return projectPE;
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<ProjectPE> objects)
    {
        service.testProjectPEPredicate(session, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

}
