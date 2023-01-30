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
