/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IProjectImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class ProjectImmutable implements IProjectImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project;

    private final boolean isExistingProject;

    public ProjectImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project)
    {
        this(project, true);
    }

    public ProjectImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project,
            boolean isExistingProject)
    {
        this.project = project;
        this.isExistingProject = isExistingProject;
    }

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
    public String getSpaceCode()
    {
        return project.getSpace().getCode();
    }

    @Override
    public String getCode()
    {
        return project.getCode();
    }

    @Override
    public String getProjectIdentifier()
    {
        return project.getIdentifier();
    }

    @Override
    public boolean isExistingProject()
    {
        return isExistingProject;
    }

    /**
     * Throw an exception if the project does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingProject())
        {
            throw new UserFailureException("Project does not exist.");
        }
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project getProject()
    {
        return project;
    }

    @Override
    public String getDescription()
    {
        return project.getDescription();
    }

    @Override
    public int hashCode()
    {
        return getProjectIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (false == (obj instanceof ProjectImmutable))
        {
            return false;
        }
        ProjectImmutable other = (ProjectImmutable) obj;
        if (getProjectIdentifier() == null)
        {
            if (other.getProjectIdentifier() != null)
            {
                return false;
            }
        } else if (getProjectIdentifier().equals(other.getProjectIdentifier()) == false)
        {
            return false;
        }
        return true;
    }
}
