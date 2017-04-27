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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectByIdentiferValidatorTest extends CommonValidatorTest<Project>
{

    @Override
    protected Project createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return new Project(spacePE.getCode(), projectPE.getCode());
    }

    @Override
    protected boolean validateObject(PersonPE personPE, Project object)
    {
        ProjectByIdentiferValidator validator = new ProjectByIdentiferValidator();
        validator.init(provider);
        return validator.isValid(personPE, object);
    }

}
