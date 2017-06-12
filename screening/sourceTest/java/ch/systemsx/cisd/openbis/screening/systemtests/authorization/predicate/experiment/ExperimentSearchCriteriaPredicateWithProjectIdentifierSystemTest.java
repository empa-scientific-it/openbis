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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonPredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriteriaPredicateWithProjectIdentifierSystemTest extends CommonPredicateScreeningSystemTest<ExperimentSearchCriteria>
{

    @Override
    protected ExperimentSearchCriteria createNonexistentObject()
    {
        return ExperimentSearchCriteria.createAllExperimentsForProject(new BasicProjectIdentifier("IDONTEXIST", "IDONTEXIST"));
    }

    @Override
    protected ExperimentSearchCriteria createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return ExperimentSearchCriteria.createAllExperimentsForProject(new BasicProjectIdentifier(spacePE.getCode(), projectPE.getCode()));
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<ExperimentSearchCriteria> objects)
    {
        getBean(ExperimentPredicateScreeningTestService.class).testExperimentSearchCriteriaPredicate(session, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

}
