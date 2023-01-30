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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;

/**
 * Identifies a project.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectIdentifier extends SpaceIdentifier
{
    private static final long serialVersionUID = IServer.VERSION;

    private String projectCode;

    public ProjectIdentifier(final String spaceCode,
            final String projectCode)
    {
        super(spaceCode);
        setProjectCode(projectCode);
    }

    public ProjectIdentifier(SpaceIdentifier spaceIdentifier, String projectCode)
    {
        this(spaceIdentifier.getSpaceCode(), projectCode);
    }

    public ProjectIdentifier(BasicProjectIdentifier identifier)
    {
        this(identifier.getSpaceCode(), identifier.getProjectCode());
    }

    public String getProjectCode()
    {
        return StringUtils.upperCase(projectCode);
    }

    public void setProjectCode(final String projectCode)
    {
        this.projectCode = projectCode;
    }
    
    public String asProjectIdentifierString()
    {
        return new ProjectIdentifier(getSpaceCode(), getProjectCode()).toString();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ProjectIdentifier == false)
        {
            return false;
        }
        final ProjectIdentifier that = (ProjectIdentifier) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSpaceCode(), that.getSpaceCode());
        builder.append(getProjectCode(), that.getProjectCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        builder.append(getProjectCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        if (getSpaceCode() == null)
        {
            return projectCode;
        } else
        {
            return super.toString() + Constants.IDENTIFIER_SEPARATOR + projectCode;
        }
    }
}
