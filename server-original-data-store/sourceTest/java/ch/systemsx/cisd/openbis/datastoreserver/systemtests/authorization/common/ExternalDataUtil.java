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
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ExternalDataUtil
{

    public static AbstractExternalData createObject(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExternalDataPE dataSetPE = (ExternalDataPE) test.getDataSet(spacePE, projectPE, (DataSetKind) param);

        PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setCode(dataSetPE.getCode());
        dataSet.setExperiment(convert(dataSetPE.getExperiment()));
        dataSet.setSample(convert(dataSetPE.tryGetSample()));
        dataSet.setStorageConfirmation(dataSetPE.isStorageConfirmation());
        return dataSet;
    }

    private static Sample convert(SamplePE samplePE)
    {
        if (samplePE == null)
        {
            return null;
        }
        Sample sample = new Sample();
        sample.setCode(samplePE.getCode());
        sample.setSpace(convert(samplePE.getSpace()));
        sample.setExperiment(convert(samplePE.getExperiment()));
        sample.setProject(convert(samplePE.getProject()));
        return sample;
    }

    private static Experiment convert(ExperimentPE experimentPE)
    {
        if (experimentPE == null)
        {
            return null;
        }
        Experiment experiment = new Experiment();
        experiment.setCode(experimentPE.getCode());
        experiment.setProject(convert(experimentPE.getProject()));
        return experiment;
    }

    private static Project convert(ProjectPE projectPE)
    {
        if (projectPE == null)
        {
            return null;
        }
        Project project = new Project();
        project.setCode(projectPE.getCode());
        project.setSpace(convert(projectPE.getSpace()));
        return project;
    }

    private static Space convert(SpacePE spacePE)
    {
        if (spacePE == null)
        {
            return null;
        }
        Space space = new Space();
        space.setCode(spacePE.getCode());
        return space;
    }

}
