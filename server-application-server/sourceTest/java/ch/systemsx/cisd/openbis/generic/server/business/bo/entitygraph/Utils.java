/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class Utils
{
    public static Sample createSample(SampleNode sampleNode)
    {
        if (sampleNode == null)
        {
            return null;
        }
        Sample sample = new Sample();
        sample.setId(sampleNode.getId());
        sample.setCode(sampleNode.getCode());
        sample.setIdentifier(sampleNode.getIdentifier());
        sample.setExperiment(createExperiment(sampleNode.getExperiment()));
        sample.setContainer(createSample(sampleNode.getContainer()));
        return sample;
    }

    public static ExternalDataPE createData(DataSetNode dataSetNode)
    {
        ExternalDataPE data = new ExternalDataPE();
        Long id = dataSetNode.getId();
        data.setId(id);
        data.setCode(dataSetNode.getCode());
        if (dataSetNode.isDeletable() == false)
        {
            data.setStatus(DataSetArchivingStatus.ARCHIVE_PENDING);
        }
        ExperimentNode experiment;
        SampleNode sample = dataSetNode.getSample();
        if (sample != null)
        {
            SamplePE samplePE = new SamplePE();
            samplePE.setId(sample.getId());
            samplePE.setCode(sample.getCode());
            if (sample.isShared() == false)
            {
                SpacePE space = new SpacePE();
                space.setCode(sample.getSpace() == null ? "" : sample.getSpace());
                samplePE.setSpace(space);
            }
            data.setSample(samplePE);
            experiment = sample.getExperiment();
        } else
        {
            experiment = dataSetNode.getExperiment();
        }
        if (experiment != null)
        {
            ExperimentPE experimentPE = new ExperimentPE();
            experimentPE.setId(experiment.getId());
            experimentPE.setCode(experiment.getCode());
            ProjectPE projectPE = new ProjectPE();
            projectPE.setCode(experiment.getProject() == null ? "" : experiment.getProject());
            SpacePE spacePE = new SpacePE();
            spacePE.setCode(experiment.getSpace() == null ? "" : experiment.getSpace());
            projectPE.setSpace(spacePE);
            experimentPE.setProject(projectPE);
            data.setExperiment(experimentPE);
        }
        return data;
    }

    public static AbstractExternalData createExternalData(DataSetNode dataSetNode)
    {
        PhysicalDataSet data = new PhysicalDataSet();
        Long id = dataSetNode.getId();
        data.setId(id);
        data.setCode(dataSetNode.getCode());
        data.setStatus(dataSetNode.isDeletable() ? DataSetArchivingStatus.AVAILABLE : DataSetArchivingStatus.ARCHIVE_PENDING);
        ExperimentNode experimentNode;
        SampleNode sampleNode = dataSetNode.getSample();
        if (sampleNode != null)
        {
            Sample sample = new Sample();
            sample.setId(sampleNode.getId());
            sample.setCode(sampleNode.getCode());
            sample.setIdentifier(sampleNode.getCode());
            data.setSample(sample);
            experimentNode = sampleNode.getExperiment();
        } else
        {
            experimentNode = dataSetNode.getExperiment();
        }
        data.setExperiment(createExperiment(experimentNode));
        return data;
    }

    private static Experiment createExperiment(ExperimentNode experimentNode)
    {
        if (experimentNode == null)
        {
            return null;
        }
        Experiment experiment = new Experiment();
        experiment.setId(experimentNode.getId());
        experiment.setCode(experimentNode.getCode());
        experiment.setIdentifier(experimentNode.getCode());
        return experiment;
    }

    public static ExperimentPE createExperimentPE(ExperimentNode experimentNode)
    {
        if (experimentNode == null)
        {
            return null;
        }
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(experimentNode.getId());
        String identifier = experimentNode.getIdentifier();
        if (identifier.startsWith("///"))
        {
            identifier = "/S0/P0/" + experimentNode.getCode();
        }
        ExperimentIdentifier experimentIdentifier = ExperimentIdentifierFactory.parse(identifier);
        experiment.setCode(experimentIdentifier.getExperimentCode());
        ProjectPE project = new ProjectPE();
        project.setCode(experimentIdentifier.getProjectCode());
        SpacePE space = new SpacePE();
        space.setCode(experimentIdentifier.getSpaceCode());
        project.setSpace(space);
        experiment.setProject(project);
        return experiment;
    }

    static final void appendTo(StringBuilder builder, String label, List<? extends EntityNode> entityNodes)
    {
        if (entityNodes.isEmpty())
        {
            return;
        }
        builder.append(", ").append(label).append(": ");
        String delim = "[";
        for (EntityNode entityNode : entityNodes)
        {
            builder.append(delim).append(entityNode.getCode());
            delim = ", ";
        }
        builder.append("]");
    }

    static final void appendTo(StringBuilder builder, String label, EntityNode entityNodeOrNull)
    {
        if (entityNodeOrNull == null)
        {
            return;
        }
        builder.append(", ").append(label).append(": ").append(entityNodeOrNull.getCode());
    }

}
