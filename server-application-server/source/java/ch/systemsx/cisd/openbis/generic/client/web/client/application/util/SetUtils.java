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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for sets and collections.
 *
 * @author Tomasz Pylak
 */
public class SetUtils
{
    /** Adds specified items to the specified collection. */
    public static <T> void addAll(Collection<T> collection, T[] items)
    {
        for (T newElem : items)
        {
            collection.add(newElem);
        }
    }

    /** @return true if the specified set contains any item from the specified items */
    public static <T> boolean containsAny(Set<T> set, Set<T> items)
    {
        Set<T> setCopy = new HashSet<T>(set);
        setCopy.retainAll(items);
        return set.size() > 0;
    }

    /** @return true if the specified set contains any item from the specified items */
    public static <T> boolean containsAny(Set<T> set, T[] items)
    {
        Set<T> itemsSet = new HashSet<T>();
        addAll(itemsSet, items);
        return containsAny(set, itemsSet);
    }
}
