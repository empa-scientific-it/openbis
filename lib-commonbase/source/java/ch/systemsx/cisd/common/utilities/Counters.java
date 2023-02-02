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

package ch.systemsx.cisd.common.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of counters identified by objects of type <code>T</code>.
 *
 * @author Franz-Josef Elmer
 */
public class Counters<T>
{
    private static final class Counter
    {
        private int count;

        public int increment()
        {
            return ++count;
        }
    }

    private final Map<T, Counter> counters = new HashMap<T, Counter>();

    /**
     * Returns how often method {@link #count(Object)} has been invoked for the specified object.
     */
    public int getCountOf(T object)
    {
        Counter counter = counters.get(object);
        return counter == null ? 0 : counter.count;
    }

    /**
     * Counts the specified object and return how often it has already been counted.
     */
    public int count(T object)
    {
        Counter counter = counters.get(object);
        if (counter == null)
        {
            counter = new Counter();
            counters.put(object, counter);
        }
        return counter.increment();
    }

    /**
     * Returns the number of different object for which {@link #count(Object)} has been invoked.
     */
    public int getNumberOfDifferentObjectsCounted()
    {
        return counters.size();
    }
}
