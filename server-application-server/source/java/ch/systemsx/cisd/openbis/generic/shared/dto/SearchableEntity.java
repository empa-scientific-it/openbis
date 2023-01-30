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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * An entity that is searchable by <i>Hibernate Search</i>.
 * 
 * @author Christian Ribeaud
 */
public enum SearchableEntity
{
    EXPERIMENT("Experiment", EntityKind.EXPERIMENT),

    SAMPLE("Sample", EntityKind.SAMPLE),

    DATA_SET("Data Set", EntityKind.DATA_SET),

    MATERIAL("Material", EntityKind.MATERIAL);

    private final String description;

    private final EntityKind entityKind;

    SearchableEntity(final String description, final EntityKind entityKind)
    {
        this.description = description;
        this.entityKind = entityKind;
    }

    /**
     * Returns entity kind for this searchable entity.
     */
    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    /**
     * Returns a description for this searchable entity.
     */
    public final String getDescription()
    {
        return description;
    }

    /**
     * For <i>bean</i> conversion.
     */
    public final String getName()
    {
        return name();
    }

    /**
     * Returns the <code>class</code> of this searchable entity.
     */
    public <T extends IMatchingEntity> Class<T> getMatchingEntityClass()
    {
        return cast(entityKind.getEntityClass());
    }

    @SuppressWarnings("unchecked")
    final static <T> Class<T> cast(final Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getDescription();
    }
}
