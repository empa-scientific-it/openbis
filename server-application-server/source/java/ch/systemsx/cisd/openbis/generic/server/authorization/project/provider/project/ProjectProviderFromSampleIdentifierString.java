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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.ProjectFromIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.SingleObjectProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSpaceCodeAndSampleCode.SpaceCodeAndSampleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author pkupczyk
 */
public class ProjectProviderFromSampleIdentifierString extends SingleObjectProvider<String>
{

    public ProjectProviderFromSampleIdentifierString(String sampleIdentifierString)
    {
        super(sampleIdentifierString);
    }

    @Override
    protected IProject createProject(IAuthorizationDataProvider dataProvider, String sampleIdentifierString)
    {
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sampleIdentifierString);

        if (sampleIdentifier.getProjectLevel() != null)
        {
            return new ProjectFromIdentifier(sampleIdentifier.getProjectLevel().toString());

        } else if (sampleIdentifier.getSpaceLevel() != null && sampleIdentifier.getSampleCode() != null)
        {
            String spaceCode = sampleIdentifier.getSpaceLevel().getSpaceCode();
            String sampleCode = sampleIdentifier.getSampleCode();

            ProjectProviderFromSpaceCodeAndSampleCode provider = new ProjectProviderFromSpaceCodeAndSampleCode(spaceCode, sampleCode);
            return provider.createProject(dataProvider, new SpaceCodeAndSampleCode(spaceCode, sampleCode));
        }

        return null;
    }

}
