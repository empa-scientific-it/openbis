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

package ch.systemsx.cisd.openbis.generic.client.console;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.common.string.Template;

/**
 * @author Franz-Josef Elmer
 */
class ScriptContext
{
    private final Map<String, String> bindings = new HashMap<String, String>();

    ScriptContext(Properties properties)
    {
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            bind((String) entry.getKey(), (String) entry.getValue());
        }
    }

    void bind(String variable, String value)
    {
        bindings.put(variable, value);
    }

    public String resolveVariables(String argument)
    {
        Template template = new Template(argument);
        Set<String> placeholderNames = template.getPlaceholderNames();
        for (String placeholderName : placeholderNames)
        {
            String value = bindings.get(placeholderName);
            if (value == null)
            {
                throw new IllegalArgumentException("Place holder '" + placeholderName
                        + "' is undefined.");
            }
            template.bind(placeholderName, value);
        }
        return template.createText();
    }
}
