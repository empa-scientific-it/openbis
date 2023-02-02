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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.sample;

import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.experiment.ExperimentReferenceUtil;

/**
 * @author pkupczyk
 */
public class WellContentValidatorWithExperimentIdSystemTest extends WellContentValidatorSystemTest
{

    @Override
    protected WellContent createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentReference reference = ExperimentReferenceUtil.createObjectWithExperimentId(this, spacePE, projectPE, param);
        return new WellContent(null, null, null, reference);
    }

}
