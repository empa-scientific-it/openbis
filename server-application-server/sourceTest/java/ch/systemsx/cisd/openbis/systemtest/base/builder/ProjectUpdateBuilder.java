/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ProjectUpdateBuilder extends UpdateBuilder<ProjectUpdatesDTO>
{
    private ProjectUpdatesDTO updates;

    public ProjectUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, Project projectx)
    {
        super(commonServer, genericServer);
        Project project = refresh(projectx);
        this.updates = new ProjectUpdatesDTO();
        updates.setAttachments(new ArrayList<NewAttachment>());
        updates.setDescription(project.getDescription());
        updates.setTechId(new TechId(project.getId()));
        updates.setVersion(project.getVersion());
    }

    public ProjectUpdateBuilder toSpace(Space space)
    {
        updates.setSpaceCode(space.getCode());
        return this;
    }

    @Override
    public ProjectUpdatesDTO create()
    {
        return updates;
    }

    private Project refresh(Project project)
    {
        return commonServer.getProjectInfo(sessionToken, new TechId(project.getId()));
    }

    @Override
    public void perform()
    {
        commonServer.updateProject(this.sessionToken, this.create());
    }
}
