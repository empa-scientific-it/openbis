/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;

/**
 * Immutable value object representing a project. A project is specified by its code and the code of the space to which it belongs.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("Project")
public final class Project implements Serializable, IIdentifierHolder
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String permId;

    private String spaceCode;

    private String code;

    private String description;

    private EntityRegistrationDetails registrationDetails;

    /**
     * Creates a new instance for the specified tech id, perm id, space code and project code.
     * 
     * @throws IllegalArgumentException if either the code or the space code is <code>null</code> or an empty string.
     */
    public Project(Long id, String permId, String spaceCode, String code, String description)
    {
        this(id, permId, spaceCode, code, description, null);
    }

    /**
     * Creates a new instance for the specified space code and project code.
     * 
     * @throws IllegalArgumentException if either the code or the space code is <code>null</code> or an empty string.
     */
    public Project(String spaceCode, String code)
    {
        this(spaceCode, code, null);
    }

    /**
     * Creates a new instance for the specified tech id, perm id, space code and project code.
     * 
     * @throws IllegalArgumentException if either the code or the space code is <code>null</code> or an empty string.
     */
    public Project(String spaceCode, String code, EntityRegistrationDetails registrationDetails)
    {
        checkAndSetCodes(spaceCode, code);
        this.registrationDetails = registrationDetails;
    }

    /**
     * Creates a new instance for the specified space code and project code.
     * 
     * @throws IllegalArgumentException if either the code or the space code is <code>null</code> or an empty string.
     */
    public Project(Long id, String permId, String spaceCode, String code, String description,
            EntityRegistrationDetails registrationDetails)
    {
        if (id == null || id == 0)
        {
            throw new IllegalArgumentException("Unspecified tech id.");
        }
        this.id = id;
        if (permId == null || permId.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified permanent id.");
        }
        this.permId = permId;
        checkAndSetCodes(spaceCode, code);
        this.description = description;
        this.registrationDetails = registrationDetails;
    }

    @SuppressWarnings("hiding")
    private void checkAndSetCodes(String spaceCode, String code)
    {
        if (spaceCode == null || spaceCode.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified space code.");
        }
        this.spaceCode = spaceCode;
        if (code == null || code.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified code.");
        }
        this.code = code;
    }

    /**
     * Returns the techical database id of the project.
     * 
     * @since 1.22
     */
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the permanent id of the project.
     * 
     * @since 1.22
     */
    public String getPermId()
    {
        return permId;
    }

    /**
     * Returns the space code.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    /**
     * Returns the project code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the project description.
     */
    public String getDescription()
    {
        return description;
    }

    @Override
    @JsonIgnore
    public String getIdentifier()
    {
        return "/" + spaceCode + "/" + code;
    }

    /**
     * Return the vocabulary term registration details.
     * 
     * @since 1.11
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((permId == null) ? 0 : permId.hashCode());
        result =
                prime * result
                        + ((registrationDetails == null) ? 0 : registrationDetails.hashCode());
        result = prime * result + ((spaceCode == null) ? 0 : spaceCode.hashCode());
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Project other = (Project) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (code.equals(other.code) == false)
        {
            return false;
        }
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (id.equals(other.id) == false)
        {
            return false;
        }
        if (permId == null)
        {
            if (other.permId != null)
            {
                return false;
            }
        } else if (permId.equals(other.permId) == false)
        {
            return false;
        }
        if (registrationDetails == null)
        {
            if (other.registrationDetails != null)
            {
                return false;
            }
        } else if (registrationDetails.equals(other.registrationDetails) == false)
        {
            return false;
        }
        if (spaceCode == null)
        {
            if (other.spaceCode != null)
            {
                return false;
            }
        } else if (spaceCode.equals(other.spaceCode) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append(getIdentifier());
        if (permId != null || id != null)
        {
            buf.append('[');
            if (getPermId() != null)
            {
                buf.append(getPermId());
            }
            if (getId() != null)
            {
                buf.append("(" + getId() + ")");
            }
            buf.append(']');
        }
        return getIdentifier();
    }

    //
    // JSON-RPC
    //
    private Project()
    {
    }

    @JsonIgnore
    private void setId(Long id)
    {
        this.id = id;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

    private void setPermId(String permId)
    {
        this.permId = permId;
    }

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

}
