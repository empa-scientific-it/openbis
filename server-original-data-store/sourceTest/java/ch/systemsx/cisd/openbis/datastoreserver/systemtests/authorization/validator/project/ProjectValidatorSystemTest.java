/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.project;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.project.ProjectValidatorTestService;

/**
 * @author pkupczyk
 */
public class ProjectValidatorSystemTest extends CommonValidatorSystemTest<Project>
{

    @Override
    protected Project createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        Project project = new Project();

        Space space = new Space();
        space.setCode(spacePE.getCode());

        project.setCode(projectPE.getCode());
        project.setSpace(space);

        return project;
    }

    @Override
    protected Project validateObject(ProjectAuthorizationUser user, Project object, Object param)
    {
        return getBean(ProjectValidatorTestService.class).testProjectValidator(user.getSessionProvider(), object);
    }

}
