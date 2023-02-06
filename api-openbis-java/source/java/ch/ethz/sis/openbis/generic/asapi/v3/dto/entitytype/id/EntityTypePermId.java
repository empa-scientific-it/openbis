/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Entity type perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.entitytype.id.EntityTypePermId")
public class EntityTypePermId extends ObjectPermId implements IEntityTypeId
{

    private static final long serialVersionUID = 1L;

    private EntityKind entityKind;

    /**
     * @param permId Entity type perm id, e.g. "MY_ENTITY_TYPE".
     */
    public EntityTypePermId(String permId)
    {
        this(permId, null);
    }

    /**
     * @param permId Entity type perm id, e.g. "MY_ENTITY_TYPE".
     * @param entityKind Entity kind, e.g. "SAMPLE"
     */
    public EntityTypePermId(String permId, EntityKind entityKind)
    {
        super(permId != null ? permId.toUpperCase() : null);
        setEntityKind(entityKind);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            EntityTypePermId that = (EntityTypePermId) obj;
            return this.getEntityKind() == null ? that.getEntityKind() == null : this.getEntityKind().equals(that.getEntityKind());
        } else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        if (getEntityKind() != null)
        {
            return super.toString() + " (" + getEntityKind() + ")";
        } else
        {
            return super.toString();
        }
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private EntityTypePermId()
    {
        super();
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    private void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

}
