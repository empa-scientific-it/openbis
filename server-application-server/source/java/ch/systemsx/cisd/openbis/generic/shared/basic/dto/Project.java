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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;

/**
 * The <i>GWT</i> equivalent to ProjectPE.
 * 
 * @author Tomasz Pylak
 */
public class Project extends CodeWithRegistrationAndModificationDate<Project> implements
        IAttachmentHolder, IIdAndCodeHolder, IIdentifierHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Space space;

    private String description;

    private Person projectLeader;

    private String identifier;

    private String permId;

    private Long id;

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    // TODO 2009-06-17, Piotr Buczek: remove and create NewProject with NewAttachments, ...
    private List<NewAttachment> newAttachments;

    @Override
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.PROJECT;
    }

    public Space getSpace()
    {
        return space;
    }

    public void setSpace(final Space space)
    {
        this.space = space;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public Person getProjectLeader()
    {
        return projectLeader;
    }

    public void setProjectLeader(final Person projectLeader)
    {
        this.projectLeader = projectLeader;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public void setNewAttachments(List<NewAttachment> newAttachments)
    {
        this.newAttachments = newAttachments;
    }

    public List<NewAttachment> getNewAttachments()
    {
        return newAttachments;
    }

    @Override
    public String toString()
    {
        return identifier;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
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
        if (!(obj instanceof Project))
        {
            return false;
        }
        Project other = (Project) obj;
        if (identifier == null)
        {
            if (other.identifier != null)
            {
                return false;
            }
        } else if (!identifier.equals(other.identifier))
        {
            return false;
        }
        return true;
    }

}
