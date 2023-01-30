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

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.ProjectFromProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.SingleObjectProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author pkupczyk
 */
public class ProjectProviderFromSample extends SingleObjectProvider<Sample>
{

    public ProjectProviderFromSample(Sample sample)
    {
        super(sample);
    }

    @Override
    protected IProject createProject(IAuthorizationDataProvider dataProvider, Sample sample)
    {
        if (sample.getProject() != null)
        {
            return new ProjectFromProject(sample.getProject());
        } else if (sample.getExperiment() != null && sample.getExperiment().getProject() != null)
        {
            return new ProjectFromProject(sample.getExperiment().getProject());
        } else
        {
            return null;
        }
    }

}
