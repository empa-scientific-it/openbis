/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to fill {@link Map} instances in fluid api style. 
 *
 * @author Franz-Josef Elmer
 */
public class MapBuilder<K, T>
{
    private final Map<K, T> map;
    
    public MapBuilder()
    {
        this(new HashMap<K, T>());
    }
    
    public MapBuilder(Map<K, T> map)
    {
        this.map = map;
    }
    
    public Map<K, T> getMap()
    {
        return map;
    }

    public MapBuilder<K, T> entry(K key, T value)
    {
        map.put(key, value);
        return this;
    }
}
