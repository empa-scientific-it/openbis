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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;

/**
 * @author pkupczyk
 */
public class ExperimentReferenceUtil
{

    public static ExperimentReference createObjectWithExperimentCodeAndProjectCodeAndSpaceCode(CommonAuthorizationSystemTest test, SpacePE spacePE,
            ProjectPE projectPE, Object param)
    {
        ExperimentPE experimentPE = test.getExperiment(spacePE, projectPE);
        return new ExperimentReference(-1L, null, experimentPE.getCode(), null, experimentPE.getProject().getCode(),
                experimentPE.getProject().getSpace().getCode());
    }

    public static ExperimentReference createObjectWithExperimentId(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE,
            Object param)
    {
        ExperimentPE experimentPE = test.getExperiment(spacePE, projectPE);
        return new ExperimentReference(experimentPE.getId(), null, null, null, null, null);
    }

    public static ExperimentReference createObjectWithSpaceCode(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE,
            Object param)
    {
        return new ExperimentReference(-1L, null, null, null, null, spacePE.getCode());
    }

    public static ExperimentReference createObjectWithExperimentPermId(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE,
            Object param)
    {
        ExperimentPE experimentPE = test.getExperiment(spacePE, projectPE);
        return new ExperimentReference(-1L, experimentPE.getPermId(), null, null, null, null);
    }

}
