/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.io.Serializable;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier.Constants;

/**
 * Identifies an owner of a sample: project, space or non.
 * <ol><li>Project if <code>projectIdentifier != null</code>
 * <li>Space if <code>projectIdentifier == null</code> and <code>spaceIdentOrNull != null</code>
 * <li>Non if <code>projectIdentifier == null</code> and <code>spaceIdentOrNull == null</code>
 * </ol>
 * 
 * @author Tomasz Pylak
 */
public class SampleOwnerIdentifier extends AbstractHashable implements Serializable,
        Comparable<SampleOwnerIdentifier>
{
    private static final long serialVersionUID = IServer.VERSION;

    // if not null, sample is defined on the space level
    private SpaceIdentifier spaceIdentOrNull;
    
    private ProjectIdentifier projectIdentifier;

    /** Database-instance level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier()
    {
    }

    /** Space level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier(final SpaceIdentifier identifier)
    {
        assert identifier != null : "space identifier cannot be null";
        this.spaceIdentOrNull = identifier;
    }

    /** Project level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier(final ProjectIdentifier identifier)
    {
        assert identifier != null : "project identifier cannot be null";
        this.projectIdentifier = identifier;
    }
    
    /** @return if sample is connected to its home space */
    public boolean isInsideHomeSpace()
    {
        return isSpaceLevel() && spaceIdentOrNull.isHomeSpace();
    }
    
    public boolean isProjectLevel()
    {
        return projectIdentifier != null;
    }

    /**
     * true if sample belongs to a space. {@link #isDatabaseInstanceLevel()} will return false in such a case.
     */
    public boolean isSpaceLevel()
    {
        return projectIdentifier == null && spaceIdentOrNull != null;
    }

    /**
     * true if sample belongs to the database instance. {@link #isSpaceLevel()} will return false in such a case.
     */
    public boolean isDatabaseInstanceLevel()
    {
        return projectIdentifier == null && spaceIdentOrNull == null;
    }

    /**
     * String representation of this identifier.
     */
    @Override
    public String toString()
    {
        if (isProjectLevel())
        {
            return projectIdentifier.asProjectIdentifierString() + Constants.IDENTIFIER_SEPARATOR;
        } else if (isSpaceLevel())
        {
            if (isInsideHomeSpace())
            {
                return "";
            } else
            {
                return spaceIdentOrNull.toString() + Constants.IDENTIFIER_SEPARATOR;
            }
        } else if (isDatabaseInstanceLevel())
        {
            return "" + Constants.IDENTIFIER_SEPARATOR;
        } else
        {
            throw new IllegalStateException("sample owner is unknown");
        }
    }
    
    public ProjectIdentifier getProjectLevel()
    {
        return projectIdentifier;
    }

    /**
     * Returns the space identifier. In case of project samples the project identifier is returned.
     * In case of shared sample <code>null</code> is returned.
     */
    public SpaceIdentifier getSpaceLevel()
    {
        return projectIdentifier != null ? projectIdentifier : spaceIdentOrNull;
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final SampleOwnerIdentifier other)
    {
        if (isProjectLevel())
        {
            if (other.isProjectLevel())
            {
                return getProjectLevel().toString().compareTo(other.getProjectLevel().toString());
            } else
            {
                return 1;
            }
        } else if (isSpaceLevel())
        {
            if (other.isProjectLevel())
            {
                return -1;
            }
            if (other.isSpaceLevel())
            {
                return getSpaceLevel().compareTo(other.getSpaceLevel());
            } else
            {
                return 1;
            }
        } else if (isDatabaseInstanceLevel())
        {
            if (other.isProjectLevel() || other.isSpaceLevel())
            {
                return -1;
            } else
            {
                return 0;
            }
        } else
            throw new IllegalStateException("sample owner is unknown");
    }
}
