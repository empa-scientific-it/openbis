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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.ProjectFromProjectPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.Object;

/**
 * @author pkupczyk
 */
public class ProjectsProviderFromProjectTechIdCollection implements IObjectsProvider<TechId>
{

    private List<TechId> techIds;

    public ProjectsProviderFromProjectTechIdCollection(List<TechId> techIds)
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
            Map<TechId, ProjectPE> projectPEMap = dataProvider.tryGetProjectsByTechIds(techIds);

            for (TechId techId : techIds)
            {
                ProjectPE projectPE = projectPEMap.get(techId);
                IProject project = projectPE != null ? new ProjectFromProjectPE(projectPE) : null;
                objects.add(new Object<TechId>(techId, project));
            }
        }

        return objects;
    }

}
