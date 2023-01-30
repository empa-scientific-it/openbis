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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An <i>abstract</i> entity type.
 * 
 * @author Christian Ribeaud
 */
abstract public class EntityType extends BasicEntityType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String ALL_TYPES_CODE = "(all)";

    public static final String DEFINED_IN_FILE = "(multiple)";

    abstract public List<? extends EntityTypePropertyType<?>> getAssignedPropertyTypes();

    public final boolean isAllTypesCode()
    {
        return isAllTypesCode(getCode());
    }

    public static final boolean isDefinedInFileEntityTypeCode(String entityTypeCode)
    {
        return DEFINED_IN_FILE.equals(entityTypeCode);
    }

    public static final boolean isAllTypesCode(String entityTypeCode)
    {
        return ALL_TYPES_CODE.equals(entityTypeCode);
    }

    public final boolean isDefinedInFileEntityTypeCode()
    {
        return isDefinedInFileEntityTypeCode(getCode());
    }

    public static <T extends EntityTypePropertyType<?>> List<T> sortedInternally(List<T> etpts)
    {
        Collections.sort(etpts, new Comparator<EntityTypePropertyType<?>>()
            {

                @Override
                public int compare(EntityTypePropertyType<?> o1, EntityTypePropertyType<?> o2)
                {
                    return o1.getOrdinal().compareTo(o2.getOrdinal());
                }
            });
        return etpts;
    }

    /**
     * return true if this entity type is of given kind, or if the kind is null
     */
    public abstract boolean isEntityKind(EntityKind kind);

    public abstract EntityKind getEntityKind();
}
