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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Property assignment perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.property.id.PropertyAssignmentPermId")
public class PropertyAssignmentPermId implements IPropertyAssignmentId
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IEntityTypeId entityTypeId;

    @JsonProperty
    private IPropertyTypeId propertyTypeId;

    public PropertyAssignmentPermId(IEntityTypeId entityTypeId, IPropertyTypeId propertyTypeId)
    {
        setEntityTypeId(entityTypeId);
        setPropertyTypeId(propertyTypeId);
    }

    @Override
    public int hashCode()
    {
        return ((getEntityTypeId() == null) ? 0 : getEntityTypeId().hashCode())
                + ((getPropertyTypeId() == null) ? 0 : getPropertyTypeId().hashCode());
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        PropertyAssignmentPermId other = (PropertyAssignmentPermId) obj;

        if (getEntityTypeId() == null)
        {
            if (other.getEntityTypeId() != null)
            {
                return false;
            }
        } else if (!getEntityTypeId().equals(other.getEntityTypeId()))
        {
            return false;
        }

        if (getPropertyTypeId() == null)
        {
            if (other.getPropertyTypeId() != null)
            {
                return false;
            }
        } else if (!getPropertyTypeId().equals(other.getPropertyTypeId()))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return getEntityTypeId() + ", " + getPropertyTypeId();
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private PropertyAssignmentPermId()
    {
        super();
    }

    @JsonIgnore
    public IEntityTypeId getEntityTypeId()
    {
        return entityTypeId;
    }

    @JsonIgnore
    private void setEntityTypeId(IEntityTypeId entityTypeId)
    {
        if (entityTypeId == null)
        {
            throw new IllegalArgumentException("Entity type id cannot be null");
        }
        this.entityTypeId = entityTypeId;
    }

    @JsonIgnore
    public IPropertyTypeId getPropertyTypeId()
    {
        return propertyTypeId;
    }

    @JsonIgnore
    private void setPropertyTypeId(IPropertyTypeId propertyTypeId)
    {
        if (propertyTypeId == null)
        {
            throw new IllegalArgumentException("Property type id cannot be null");
        }
        this.propertyTypeId = propertyTypeId;
    }

}
