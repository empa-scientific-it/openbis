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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Table cell for an entity link.
 * 
 * @author Piotr Buczek
 */
public class EntityTableCell implements ISerializableComparable
{
    private static final String MISSING_ENTITY_SUFFIX = " (missing)";

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private String permId;

    private String identifierOrNull; // 'null' when entity is missing

    private String linkTextOrNull; // 'null' when link text wasn't redefined (use identifier)

    private boolean invalid = false;

    private boolean fake = false;

    public EntityTableCell(EntityKind entityKind, String permId)
    {
        this(entityKind, permId, permId);
    }

    public EntityTableCell(EntityKind entityKind, String permId, String identifierOrNull)
    {
        if (entityKind == null)
        {
            throw new IllegalArgumentException("Unspecified entityKind");
        }
        if (permId == null)
        {
            throw new IllegalArgumentException("Unspecified permId");
        }
        this.entityKind = entityKind;
        this.permId = permId;
        this.identifierOrNull = identifierOrNull;
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return this.toString().compareTo(o.toString());
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public String getPermId()
    {
        return permId;
    }

    public String getIdentifierOrNull()
    {
        return identifierOrNull;
    }

    public boolean isMissing()
    {
        return identifierOrNull == null;
    }

    @Override
    public int hashCode()
    {
        return getPermId().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityTableCell == false)
        {
            return false;
        }
        EntityTableCell otherCell = (EntityTableCell) obj;
        return entityKind.equals(otherCell.entityKind) && permId.equals(otherCell.permId);
    }

    private String createLinkText()
    {
        if (isMissing())
        {
            return (linkTextOrNull != null ? linkTextOrNull : permId) + MISSING_ENTITY_SUFFIX;
        } else
        {
            return linkTextOrNull != null ? linkTextOrNull : identifierOrNull;
        }
    }

    public void setLinkText(String linkText)
    {
        this.linkTextOrNull = linkText;
    }

    public boolean isInvalid()
    {
        return invalid;
    }

    public void setInvalid(boolean invalid)
    {
        this.invalid = invalid;
    }

    public boolean isFake()
    {
        return fake;
    }

    public void setFake(boolean fake)
    {
        this.fake = fake;
    }

    @Override
    public String toString()
    {
        return createLinkText();
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private EntityTableCell()
    {
    }

}
