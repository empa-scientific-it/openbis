/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest.base;

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author anttil
 */
public class RelationshipServiceStub implements IRelationshipService
{

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentPE experiment,
            ProjectPE project)
    {
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectPE project,
            SpacePE space)
    {
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SamplePE sample,
            ExperimentPE experiment)
    {
    }

    @Override
    public void checkCanUnassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
    }

    @Override
    public void unshareSample(IAuthSession session, SamplePE sample, SpacePE space)
    {
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SamplePE sample,
            SpacePE space)
    {
    }

    @Override
    public void shareSample(IAuthSession session, SamplePE sample)
    {
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, DataPE data, ExperimentPE experimentOrNull)
    {
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, DataPE data, SamplePE sampleOrNull)
    {
    }

    @Override
    public void addParentToSample(IAuthSession session, SamplePE sample,
            SamplePE parent)
    {
    }

    @Override
    public void setSampleParentChildAnnotations(IAuthSession session, SamplePE child, SamplePE parent,
            Map<String, String> childAnnotations, Map<String, String> parentAnnotations)
    {
    }

    @Override
    public void removeParentFromSample(IAuthSession session, SamplePE sample,
            SamplePE parent)
    {
    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SamplePE sample, SamplePE container)
    {
    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SamplePE sample)
    {
    }

    @Override
    public void addParentToDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
    }

    @Override
    public void removeParentFromDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
    }

    @Override
    public void assignDataSetToContainer(IAuthSession session, DataPE data, DataPE container)
    {
    }

    @Override
    public void removeDataSetFromContainer(IAuthSession session, DataPE data, DataPE container)
    {
    }

    @Override
    public void assignSampleToProject(IAuthSession session, SamplePE sample, ProjectPE project)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void reassignSampleToProject(final IAuthSession session, final SamplePE sample, final ProjectPE project)
    {

    }

    @Override
    public void unassignSampleFromProject(IAuthSession session, SamplePE sample)
    {
        // TODO Auto-generated method stub

    }
}
