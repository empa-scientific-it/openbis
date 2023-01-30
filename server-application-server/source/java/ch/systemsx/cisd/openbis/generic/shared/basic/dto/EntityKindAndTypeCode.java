/*
 * Copyright 2009 ETH Zuerich, CISD
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

/**
 * Bean combining {@link EntityKind} with entity type code. It determines plug-ins.
 * 
 * @author Franz-Josef Elmer
 */
public final class EntityKindAndTypeCode implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final EntityKind entityKind;

    private final String entityTypeCode;

    public EntityKindAndTypeCode(final EntityKind entityKind, final BasicEntityType entityType)
    {
        this(entityKind, entityType.getCode());
    }

    public EntityKindAndTypeCode(final EntityKind entityKind, final String entityTypeCode)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert entityTypeCode != null : "Unspecified entity type code.";
        this.entityKind = entityKind;
        this.entityTypeCode = entityTypeCode;
    }

    public boolean entityKindsMatch(EntityKindAndTypeCode other)
    {
        return this.entityKind.equals(other.entityKind);
    }

    public String getEntityTypeCode()
    {
        return entityTypeCode;
    }

    //
    // Object
    //
    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityKindAndTypeCode == false)
        {
            return false;
        }
        final EntityKindAndTypeCode that = (EntityKindAndTypeCode) obj;
        return entityKind.equals(that.entityKind) && entityTypeCode.equals(that.entityTypeCode);
    }

    @Override
    public final int hashCode()
    {
        int result = 17;
        result = 37 * result + entityKind.hashCode();
        result = 37 * result + entityTypeCode.hashCode();
        return result;
    }

    @Override
    public final String toString()
    {
        return "[entityKind=" + entityKind + ",entityTypeCode=" + entityTypeCode + "]";
    }
}
