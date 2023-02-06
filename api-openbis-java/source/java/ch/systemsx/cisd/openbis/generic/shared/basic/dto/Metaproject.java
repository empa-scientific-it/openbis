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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil;

/**
 * @author Pawel Glyzewski
 */
@SuppressWarnings("unused")
@JsonObject("Metaproject")
public class Metaproject implements Serializable, IIdAndCodeHolder, IMetaprojectRegistration,
        IMetaprojectUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String name;

    private String description;

    private String ownerId;

    private boolean isPrivate;

    private Date creationDate;

    @Override
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        this.id = id;
    }

    public String getIdentifier()
    {
        return new MetaprojectIdentifier(ownerId, name).format();
    }

    public void setIdentifier(String identifier)
    {
        MetaprojectIdentifier identifierObject = MetaprojectIdentifier.parse(identifier);
        if (identifierObject != null)
        {
            this.ownerId = identifierObject.getMetaprojectOwnerId();
            this.name = identifierObject.getMetaprojectName();
        } else
        {
            this.ownerId = null;
            this.name = null;
        }
    }

    @Override
    @JsonIgnore
    public String getCode()
    {
        return getName();
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public boolean isPrivate()
    {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    //
    // JSON-RPC
    //

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

}
