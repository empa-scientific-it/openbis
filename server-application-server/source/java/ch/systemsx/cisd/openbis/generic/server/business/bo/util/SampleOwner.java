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
package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Determines who is the <i>owner</i> of the sample: project, space or non
 * <p>
 * Stores the owner <i>PEs</i>.
 * </p>
 */
public final class SampleOwner
{
    private ProjectPE projectOrNull;
    private SpacePE spaceOrNull;
    
    public SampleOwner(ProjectPE projectOrNull)
    {
        this.projectOrNull = projectOrNull;
        if (projectOrNull != null)
        {
            spaceOrNull = projectOrNull.getSpace();
        }
    }

    public SampleOwner(final SpacePE spaceOrNull)
    {
        this.spaceOrNull = spaceOrNull;
    }
    
    public static SampleOwner createProject(ProjectPE project)
    {
        return new SampleOwner(project);
    }

    public static SampleOwner createSpace(final SpacePE group)
    {
        return new SampleOwner(group);
    }

    public static SampleOwner createDatabaseInstance()
    {
        return new SampleOwner((SpacePE) null);
    }
    
    public boolean isProjectLevel()
    {
        return projectOrNull != null;
    }

    public boolean isSpaceLevel()
    {
        return projectOrNull == null && spaceOrNull != null;
    }

    public boolean isDatabaseInstanceLevel()
    {
        return projectOrNull == null &&  spaceOrNull == null;
    }
    
    public ProjectPE tryGetProject()
    {
        return projectOrNull;
    }

    public SpacePE tryGetSpace()
    {
        return spaceOrNull;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        if (isProjectLevel())
        {
            return "project: " + projectOrNull;
        }
        if (isSpaceLevel())
        {
            return "space: " + spaceOrNull;
        } else
        {
            return "db instance";
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleOwner == false)
        {
            return false;
        }
        final SampleOwner that = (SampleOwner) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(projectOrNull, that.tryGetProject());
        builder.append(spaceOrNull, that.tryGetSpace());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(projectOrNull);
        builder.append(spaceOrNull);
        return builder.toHashCode();
    }
}
