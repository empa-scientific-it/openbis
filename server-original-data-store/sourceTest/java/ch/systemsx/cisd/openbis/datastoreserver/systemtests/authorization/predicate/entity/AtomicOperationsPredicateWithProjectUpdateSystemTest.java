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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.entity;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author pkupczyk
 */
public class AtomicOperationsPredicateWithProjectUpdateSystemTest extends AtomicOperationsPredicateSystemTest
{

    @Override
    protected AtomicEntityOperationDetails createNonexistentObject(Object param)
    {
        ProjectUpdatesDTO update = new ProjectUpdatesDTO();
        update.setIdentifier("/IDONTEXIST/IDONTEXIST");

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.projectUpdate(update);

        return builder.getDetails();
    }

    @Override
    protected AtomicEntityOperationDetails createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ProjectUpdatesDTO update = new ProjectUpdatesDTO();
        update.setIdentifier("/" + spacePE.getCode() + "/" + projectPE.getCode());

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.projectUpdate(update);

        return builder.getDetails();
    }

    @Override
    protected CommonPredicateSystemTestAssertions<AtomicEntityOperationDetails> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<AtomicEntityOperationDetails>(super.getAssertions())
            {
                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else if (user.isInstanceUser())
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
