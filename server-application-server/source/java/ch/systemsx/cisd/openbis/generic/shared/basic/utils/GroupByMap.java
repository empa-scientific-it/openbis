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

package ch.systemsx.cisd.openbis.generic.shared.basic.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map which groups all elements added at the same key.
 * 
 * @author Tomasz Pylak
 */
public class GroupByMap<K, E>
{
    private final Map<K, List<E>> map;

    private final IGroupKeyExtractor<K, E> extractor;

    /** @param extractor computes a key for the row */
    public GroupByMap(final IGroupKeyExtractor<K, E> extractor)
    {
        this.extractor = extractor;
        this.map = new HashMap<K, List<E>>();
    }

    /** Creates a map for the specified rows with a given key extractor. */
    public static <K, E> GroupByMap<K, E> create(final Iterable<? extends E> rows,
            final IGroupKeyExtractor<K, E> extractor)
    {
        GroupByMap<K, E> table = new GroupByMap<K, E>(extractor);
        for (E row : rows)
        {
            table.add(row);
        }
        return table;
    }

    /** Adds a new row. */
    public void add(E row)
    {
        K key = extractor.getKey(row);
        List<E> elements = map.get(key);
        if (elements == null)
        {
            elements = new ArrayList<E>();
        }
        elements.add(row);
        map.put(key, elements);
    }

    /** Returns all rows added at the specified key. */
    public List<E> tryGet(K key)
    {
        return map.get(key);
    }

    /**
     * Returns all rows added at the specified key.
     * 
     * @throws IllegalStateException if the key is not in the map
     */
    public List<E> getOrDie(K key)
    {
        List<E> result = tryGet(key);
        if (result == null)
        {
            throw new IllegalStateException("No element with the key " + key);
        }
        return result;
    }

    /** @return all available keys */
    public Set<K> getKeys()
    {
        return map.keySet();
    }

    /** @return unmodifiable map from key to a list of its values. */
    public Map<K, List<E>> getMap()
    {
        return Collections.unmodifiableMap(map);
    }

}
