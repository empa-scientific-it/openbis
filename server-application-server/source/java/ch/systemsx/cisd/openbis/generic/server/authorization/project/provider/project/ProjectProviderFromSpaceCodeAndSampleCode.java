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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.ProjectFromProjectPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.SingleObjectProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSpaceCodeAndSampleCode.SpaceCodeAndSampleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectProviderFromSpaceCodeAndSampleCode extends SingleObjectProvider<SpaceCodeAndSampleCode>
{

    public ProjectProviderFromSpaceCodeAndSampleCode(String spaceCode, String sampleCode)
    {
        super(new SpaceCodeAndSampleCode(spaceCode, sampleCode));
    }

    @Override
    protected IProject createProject(IAuthorizationDataProvider dataProvider, SpaceCodeAndSampleCode spaceCodeAndSampleCode)
    {
        SpacePE space = dataProvider.tryGetSpace(spaceCodeAndSampleCode.getSpaceCode());
        if (space != null)
        {
            SamplePE sample = dataProvider.tryGetSampleBySpaceAndCode(space, spaceCodeAndSampleCode.getSampleCode());
            if (sample != null)
            {
                ProjectPE project = sample.getExperiment() != null ? sample.getExperiment().getProject() : null;
                if (project != null)
                {
                    return new ProjectFromProjectPE(project);
                }
            }
        }

        return null;
    }

    public static class SpaceCodeAndSampleCode
    {

        public String spaceCode;

        public String sampleCode;

        public SpaceCodeAndSampleCode(String spaceCode, String sampleCode)
        {
            this.spaceCode = spaceCode;
            this.sampleCode = sampleCode;
        }

        public String getSpaceCode()
        {
            return spaceCode;
        }

        public String getSampleCode()
        {
            return sampleCode;
        }

    }

}
