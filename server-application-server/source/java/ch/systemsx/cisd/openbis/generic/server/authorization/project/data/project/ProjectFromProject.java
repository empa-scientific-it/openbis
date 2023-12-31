/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class ProjectFromProject implements IProject
{

    private Project project;

    public ProjectFromProject(Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null");
        }

        this.project = project;
    }

    @Override
    public Long getId()
    {
        return project.getId();
    }

    @Override
    public String getPermId()
    {
        return project.getPermId();
    }

    @Override
    public String getIdentifier()
    {
        if (project.getIdentifier() != null)
        {
            return project.getIdentifier();
        } else if (project.getCode() != null && project.getSpace() != null && project.getSpace().getCode() != null)
        {
            return new ProjectIdentifier(project.getSpace().getCode(), project.getCode()).toString();
        } else
        {
            return null;
        }
    }

}
