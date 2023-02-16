/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class ProjectProviderFromExperimentIdentifierString extends SingleObjectProvider<String>
{

    public ProjectProviderFromExperimentIdentifierString(String experimentIdentifierString)
    {
        super(experimentIdentifierString);
    }

    @Override
    protected IProject createProject(IAuthorizationDataProvider dataProvider, String experimentIdentifierString)
    {
        ExperimentIdentifier experimentIdentifier = ExperimentIdentifierFactory.parse(experimentIdentifierString);
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(experimentIdentifier.getSpaceCode(), experimentIdentifier.getProjectCode());
        return new ProjectFromIdentifier(projectIdentifier.toString());
    }

}
