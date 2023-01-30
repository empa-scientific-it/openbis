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

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * The <i>GWT</i> equivalent to GroupPE.
 * 
 * @author Franz-Josef Elmer
 */
public final class Space extends CodeWithRegistration<Space> implements IIdHolder, ISpaceUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String description;

    private String identifier;

    private Date modificationDate;

    @Override
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    //
    // Object
    //

    // equals and hashCode methods based on simple toString value representation
    // instead of Equals/HashCodeBuilder from apache.commons library like in GroupPE

    @Override
    public String toString()
    {
        return "/" + getCode();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Space == false)
        {
            return false;
        }
        final Space that = (Space) obj;
        return this.toString().equals(that.toString());
    }

    @Override
    public final int hashCode()
    {
        return this.toString().hashCode();
    }

}
