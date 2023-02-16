/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.registrator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container for a jython dropbox that can be used to store information during the registration process. This is the suggested way for the users of
 * dropboxes to share some data beetween the dropbox scripts and and the functions like post_registration, post_storage etc.
 * 
 * @author jakubs
 */
public class DataSetRegistrationPersistentMap implements Serializable
{

    private static final long serialVersionUID = 1L;

    private HashMap<String, Serializable> persistentMap;

    public DataSetRegistrationPersistentMap()
    {
        persistentMap = new HashMap<String, Serializable>();
    }

    public int size()
    {
        return persistentMap.size();
    }

    public boolean isEmpty()
    {
        return persistentMap.isEmpty();
    }

    public Serializable get(Object key)
    {
        return persistentMap.get(key);
    }

    public boolean containsKey(Object key)
    {
        return persistentMap.containsKey(key);
    }

    /**
     * @param value object to put into the map. Method accepts Object instead of Serializable, so that it can fail with informative message if the
     *            jython script calls the method with inappropriate type
     * @throws IllegalArgumentException if the value is not Serializable.
     */
    public Serializable put(String key, Object value)
    {
        if (false == (value instanceof Serializable))
            throw new IllegalArgumentException(
                    "Provided non-serializable argument to persistent map. Argument type is "
                            + value.getClass());
        return persistentMap.put(key, (Serializable) value);

    }

    /**
     * Add all entries from other persistent map.
     */
    public void putAll(DataSetRegistrationPersistentMap other)
    {
        for (Entry<String, Serializable> item : other.persistentMap.entrySet())
        {
            this.persistentMap.put(item.getKey(), item.getValue());
        }
    }

    public Serializable remove(Object key)
    {
        return persistentMap.remove(key);
    }

    public void clear()
    {
        persistentMap.clear();
    }

    public boolean containsValue(Object value)
    {
        return persistentMap.containsValue(value);
    }

    public Set<String> keySet()
    {
        return persistentMap.keySet();
    }

    public Collection<Serializable> values()
    {
        return persistentMap.values();
    }

}
