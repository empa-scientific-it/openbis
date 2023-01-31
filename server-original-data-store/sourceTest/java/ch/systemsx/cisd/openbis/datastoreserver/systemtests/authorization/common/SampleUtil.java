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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.SampleKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SampleUtil
{

    public static Sample createObject(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);
        Sample sample = new Sample();

        if (samplePE.getSpace() != null)
        {
            Space space = new Space();
            space.setCode(samplePE.getSpace().getCode());

            sample.setSpace(space);
        }

        if (samplePE.getProject() != null)
        {
            Space projectSpace = new Space();
            projectSpace.setCode(samplePE.getProject().getSpace().getCode());

            Project project = new Project();
            project.setCode(samplePE.getProject().getCode());
            project.setSpace(projectSpace);

            sample.setProject(project);
        }

        if (samplePE.getExperiment() != null)
        {
            Space experimentSpace = new Space();
            experimentSpace.setCode(samplePE.getExperiment().getProject().getSpace().getCode());

            Project experimentProject = new Project();
            experimentProject.setCode(samplePE.getExperiment().getProject().getCode());
            experimentProject.setSpace(experimentSpace);

            Experiment experiment = new Experiment();
            experiment.setCode(samplePE.getExperiment().getCode());
            experiment.setProject(experimentProject);

            sample.setExperiment(experiment);
        }

        return sample;
    }

}
