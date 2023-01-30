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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.Serializable;

/**
 * Values object which represents a unique key.
 *
 * @author Franz-Josef Elmer
 */
public final class Key implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Object[] objects;

    public Key(Object... objects)
    {
        this.objects = objects;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Key == false)
        {
            return false;
        }
        Key key = (Key) obj;
        if (key.objects.length != objects.length)
        {
            return false;
        }
        for (int i = 0, n = objects.length; i < n; i++)
        {
            Object object = objects[i];
            Object keyObject = key.objects[i];
            if (object == null ? object != keyObject : object.equals(keyObject) == false)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int sum = 0;
        for (Object object : objects)
        {
            sum = 37 * sum + (object == null ? 0 : object.hashCode());
        }
        return sum;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(object);
        }
        return "[" + builder.toString() + "]";
    }
}