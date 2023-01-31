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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.entity;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author pkupczyk
 */
public class AtomicOperationsPredicateWithProjectRegistrationSystemTest extends AtomicOperationsPredicateSystemTest
{

    @Override
    protected AtomicEntityOperationDetails createNonexistentObject(Object param)
    {
        NewProject newProject = new NewProject("/IDONTEXIST/IDONTEXIST", "idontexist");

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.project(newProject);

        return builder.getDetails();
    }

    @Override
    protected AtomicEntityOperationDetails createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        NewProject newProject = new NewProject("/" + spacePE.getCode() + "/" + projectPE.getCode(), "description");

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.project(newProject);

        return builder.getDetails();
    }

}
