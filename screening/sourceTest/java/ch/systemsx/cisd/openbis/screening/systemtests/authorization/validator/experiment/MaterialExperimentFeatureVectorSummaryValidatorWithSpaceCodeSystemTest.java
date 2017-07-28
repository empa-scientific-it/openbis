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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.experiment;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;

/**
 * @author pkupczyk
 */
public class MaterialExperimentFeatureVectorSummaryValidatorWithSpaceCodeSystemTest
        extends MaterialExperimentFeatureVectorSummaryValidatorSystemTest
{

    @Override
    protected MaterialSimpleFeatureVectorSummary createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentReference reference = new ExperimentReference(-1L, null, null, null, null, spacePE.getCode());
        return new MaterialSimpleFeatureVectorSummary(reference);
    }

    @Override
    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, MaterialSimpleFeatureVectorSummary result, Throwable t,
            Object param)
    {
        // given only a space code the project authorization does not work
        assertNull(result);
    }

}
