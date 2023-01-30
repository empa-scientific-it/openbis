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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Izabela Adamczyk
 */
public class GlobalProperties
{

    private final Map<String, String> properties = new HashMap<String, String>();

    public GlobalProperties()
    {
    }

    /**
     * Adds property with given key and value.Throws {@link IllegalArgumentException} if property already defined.
     */
    public void add(String key, String value)
    {
        if (properties.containsKey(key))
        {
            throw new IllegalArgumentException(String.format("Property '%s' defined twice.", key));
        }
        properties.put(key, value);
    }

    /**
     * Returns value of given property. Throws {@link IllegalArgumentException} if property not defined.
     */
    public String get(String key)
    {
        String value = tryGet(key);
        if (value == null)
        {
            throw new IllegalArgumentException(String.format("Property '%s' not defined.", key));
        }
        return value;
    }

    /**
     * Returns value of given property.
     */
    public String tryGet(String key)
    {
        return properties.get(key);
    }

}
