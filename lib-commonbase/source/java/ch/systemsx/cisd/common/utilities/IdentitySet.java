/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.util.IdentityHashMap;
import java.util.LinkedList;

/**
 * Implementation of a set (not implementing java Set interface) that uses objects identity instead of hashCode and equals
 * 
 * @author Jakub Straszewski
 */
public class IdentitySet<T>
{
    IdentityHashMap<T, Object> map = new IdentityHashMap<T, Object>();

    public void add(T t)
    {
        map.put(t, t);
    }

    public boolean contains(T t)
    {
        return map.containsKey(t);
    }

    public void remove(T t)
    {
        map.remove(t);
    }

    public Iterable<T> getItems()
    {
        return new LinkedList<T>(map.keySet());
    }
}
