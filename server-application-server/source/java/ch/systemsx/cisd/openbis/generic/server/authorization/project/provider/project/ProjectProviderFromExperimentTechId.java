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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ProjectProviderFromExperimentTechId extends SingleObjectProvider<TechId>
{

    public ProjectProviderFromExperimentTechId(TechId techId)
    {
        super(techId);
    }

    @Override
    protected IProject createProject(IAuthorizationDataProvider dataProvider, TechId techId)
    {
        ExperimentPE experimentPE = dataProvider.tryGetExperimentByTechId(techId);

        if (experimentPE != null)
        {
            return new ProjectFromProjectPE(experimentPE.getProject());
        } else
        {
            return null;
        }
    }

}
