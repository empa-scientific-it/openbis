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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;

/**
 * Utility class that manages mappings from entity types/codes (possibly including wildcards) to IServerPlugin objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */

public class WildcardSupportingMap<T>
{
    /**
     * Internal class for keeping mappings from entityKind/codes (which may include wildcards) to factories.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class WildcardedThingMapping<T>
    {
        private final EntityKindAndTypeCode entityKindAndCode;

        private final T thing;

        private WildcardedThingMapping(EntityKindAndTypeCode entityKindAndCode, T thing)
        {
            this.entityKindAndCode = entityKindAndCode;
            this.thing = thing;
        }

        public T getThing()
        {
            return thing;
        }

        boolean matchesEntityKindAndTypeCode(EntityKindAndTypeCode otherEntityKindAndTypeCode)
        {
            if (false == entityKindAndCode.entityKindsMatch(otherEntityKindAndTypeCode))
            {
                return false;
            }

            return otherEntityKindAndTypeCode.getEntityTypeCode().matches(
                    entityKindAndCode.getEntityTypeCode());
        }
    }

    private final ArrayList<WildcardedThingMapping<T>> mappings =
            new ArrayList<WildcardedThingMapping<T>>();

    /**
     * Add a mapping from the entity kind/code to a factory.
     */
    protected void addThingMapping(EntityKindAndTypeCode entityKindAndCode, T thing)
    {
        mappings.add(new WildcardedThingMapping<T>(entityKindAndCode, thing));
    }

    /**
     * Return the first factory that matches the given entityKindAndCode, or null if none is found.
     */
    protected T tryThing(EntityKindAndTypeCode entityKindAndCode)
    {
        for (WildcardedThingMapping<T> mapping : mappings)
        {
            if (mapping.matchesEntityKindAndTypeCode(entityKindAndCode))
            {
                return mapping.getThing();
            }
        }

        return null;
    }
}
