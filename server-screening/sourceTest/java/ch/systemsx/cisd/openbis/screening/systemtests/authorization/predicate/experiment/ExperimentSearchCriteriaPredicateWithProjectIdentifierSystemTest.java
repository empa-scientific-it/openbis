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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.experiment;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriteriaPredicateWithProjectIdentifierSystemTest extends ExperimentSearchCriteriaPredicateSystemTest
{

    @Override
    protected ExperimentSearchCriteria createNonexistentObject(Object param)
    {
        return ExperimentSearchCriteria.createAllExperimentsForProject(new BasicProjectIdentifier("IDONTEXIST", "IDONTEXIST"));
    }

    @Override
    protected ExperimentSearchCriteria createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return ExperimentSearchCriteria.createAllExperimentsForProject(new BasicProjectIdentifier(spacePE.getCode(), projectPE.getCode()));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<ExperimentSearchCriteria> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<ExperimentSearchCriteria>(super.getAssertions())
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
