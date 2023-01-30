/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * Visit of an entity. Objects of this class are created when the detail view of an entity is opened in the GUI.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityVisit implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String entityKind;

    private String entityTypeCode;

    private String identifier;

    private String permID;

    private long timeStamp;

    public EntityVisit()
    {
    }

    public EntityVisit(IEntityInformationHolderWithIdentifier entity)
    {
        setEntityKind(entity.getEntityKind().toString());
        setEntityTypeCode(entity.getEntityType().getCode());
        setIdentifier(entity.getIdentifier());
        setPermID(entity.getPermId());
        setTimeStamp(System.currentTimeMillis());
    }

    public String getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(String entityKind)
    {
        this.entityKind = entityKind;
    }

    public String getEntityTypeCode()
    {
        return entityTypeCode;
    }

    public void setEntityTypeCode(String entityTypeCode)
    {
        this.entityTypeCode = entityTypeCode;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getPermID()
    {
        return permID;
    }

    public void setPermID(String permID)
    {
        this.permID = permID;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("EntityVisit ");
        sb.append("[entityKind=" + entityKind);
        sb.append(", entityTypeCode=" + entityTypeCode);
        sb.append(", identifier=" + identifier);
        sb.append(", permID=" + permID);
        sb.append(", timeStamp=" + timeStamp + "(" + new Date(timeStamp) + ")]");
        return sb.toString();
    }
}
