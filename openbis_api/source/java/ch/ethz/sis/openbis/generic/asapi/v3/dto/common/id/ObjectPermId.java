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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Base class for ids that identify objects by a perm id. A perm id is an immutable system-generated string. A perm id is assigned to an object during
 * the object creation and cannot be changed afterwards. An object's perm id is guaranteed to be always the same, e.g. a sample perm id remains the
 * same even if the sample is moved to a different space.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.common.id.ObjectPermId")
public class ObjectPermId implements IObjectId
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String permId;

    public ObjectPermId(String permId)
    {
        setPermId(permId);
    }

    public String getPermId()
    {
        return permId;
    }

    //
    // JSON-RPC
    //

    protected ObjectPermId()
    {
    }

    @JsonIgnore
    private void setPermId(String permId)
    {
        if (permId == null)
        {
            throw new IllegalArgumentException("PermId cannot be null");
        }
        this.permId = permId;
    }

    @Override
    public String toString()
    {
        return getPermId();
    }

    @Override
    public int hashCode()
    {
        return ((getPermId() == null) ? 0 : getPermId().hashCode());
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
        ObjectPermId other = (ObjectPermId) obj;
        if (getPermId() == null)
        {
            if (other.getPermId() != null)
            {
                return false;
            }
        } else if (!getPermId().equals(other.getPermId()))
        {
            return false;
        }
        return true;
    }

}
