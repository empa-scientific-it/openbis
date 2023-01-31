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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ListSampleCriteriaPredicateWithExperimentSystemTest extends ListSampleCriteriaPredicateSystemTest
{

    @Override
    protected ListSampleCriteria createNonexistentObject(Object param)
    {
        return ListSampleCriteria.createForExperiment(new TechId(-1));
    }

    @Override
    protected ListSampleCriteria createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentPE experiment = getExperiment(spacePE, projectPE);
        return ListSampleCriteria.createForExperiment(new TechId(experiment.getId()));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<ListSampleCriteria> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<ListSampleCriteria>(super.getAssertions())
            {
                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertUserFailureExceptionThatExperimentDoesNotExist(t);
                    }
                }
            };
    }

}
