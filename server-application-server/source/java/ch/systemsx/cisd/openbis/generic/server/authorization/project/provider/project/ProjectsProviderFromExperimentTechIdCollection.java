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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ProjectsProviderFromExperimentTechIdCollection implements IObjectsProvider<TechId>
{

    private List<TechId> techIds;

    public ProjectsProviderFromExperimentTechIdCollection(List<TechId> techIds)
    {
        this.techIds = techIds;
    }

    @Override
    public Collection<TechId> getOriginalObjects()
    {
        return techIds;
    }

    @Override
    public Collection<IObject<TechId>> getObjects(IAuthorizationDataProvider dataProvider)
    {
        Collection<IObject<TechId>> objects = new ArrayList<IObject<TechId>>();

        if (techIds != null)
        {
            Map<TechId, ExperimentPE> experimentPEMap = dataProvider.tryGetExperimentsByTechIds(techIds);

            for (TechId techId : techIds)
            {
                ExperimentPE experimentPE = experimentPEMap.get(techId);
                IProject project = experimentPE != null ? new ProjectFromProjectPE(experimentPE.getProject()) : null;
                objects.add(new Object<TechId>(techId, project));
            }
        }

        return objects;
    }

}
