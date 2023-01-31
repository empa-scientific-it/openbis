/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.HashMap;

/**
 * The collection that behaves like a MultiSet<Throwable> with two Exceptions beeing treaded equal if they have the same class, and their cause
 * exceptions are equal in the same sense. The implementation has weak performance, but the expected size of data is very small. If that would ever
 * change, consider reimplementing it.
 * 
 * @author jakubs
 */
public class DistinctExceptionsCollection
{
    private final HashMap<Throwable, Integer> collection;

    public DistinctExceptionsCollection()
    {
        collection = new HashMap<Throwable, Integer>();
    }

    /**
     * Adds exception to the list of exceptions and returns the integer denoting how many similar exceptions are already in this collection.
     * 
     * @return the number of times this exception has been add to the collection (incl. this operation)
     */
    public int add(Throwable t)
    {
        Throwable key = findEqualKey(t);
        Integer oldValue = collection.get(key);
        if (oldValue == null)
        {
            oldValue = 0;
        }
        collection.put(key, oldValue + 1);
        return oldValue + 1;
    }

    /**
     * @return the count for the given exception
     */
    public int get(Throwable t)
    {
        Integer value = collection.get(findEqualKey(t));
        if (value == null)
        {
            return 0;
        } else
        {
            return value;
        }
    }

    /**
     * @return the Throwable that can be used as a key in the internal collection. It's either already an existing key or the
     *         <code>param<code> if no suitable throwable exists.
     */
    private Throwable findEqualKey(Throwable param)
    {
        for (Throwable key : collection.keySet())
        {
            if (equal(key, param))
            {
                return key;
            }
        }
        return param;
    }

    /**
     * check the equality of two throwables
     */
    private boolean equal(Throwable first, Throwable second)
    {
        if (first == null && second == null)
        {
            return true;
        }
        if (first == null || second == null)
        {
            return false;
        }
        if (first.getClass().equals(second.getClass()))
        {
            return equal(first.getCause(), second.getCause());
        }
        return false;
    }
}
