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

package ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.IObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.Object;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.ProjectFromProjectPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public class ProjectsProviderFromSampleV1Collection implements IObjectsProvider<Sample>
{

    private List<Sample> samples;

    public ProjectsProviderFromSampleV1Collection(List<Sample> samples)
    {
        this.samples = samples;
    }

    @Override
    public Collection<Sample> getOriginalObjects()
    {
        return samples;
    }

    @Override
    public Collection<IObject<Sample>> getObjects(IAuthorizationDataProvider dataProvider)
    {
        Collection<IObject<Sample>> objects = new ArrayList<IObject<Sample>>();

        if (samples != null)
        {
            List<TechId> techIds = new ArrayList<TechId>();

            for (Sample sample : samples)
            {
                techIds.add(new TechId(sample.getId()));
            }

            Map<TechId, SamplePE> samplePEMap = dataProvider.tryGetSamplesByTechIds(techIds);

            for (Sample sample : samples)
            {
                TechId techId = new TechId(sample.getId());
                SamplePE samplePE = samplePEMap.get(techId);

                if (samplePE != null)
                {
                    ProjectPE projectPE = samplePE.getExperiment() != null ? samplePE.getExperiment().getProject() : samplePE.getProject();
                    IProject project = projectPE != null ? new ProjectFromProjectPE(projectPE) : null;
                    objects.add(new Object<Sample>(sample, project));
                } else
                {
                    objects.add(new Object<Sample>(sample, null));
                }
            }
        }

        return objects;
    }

}
