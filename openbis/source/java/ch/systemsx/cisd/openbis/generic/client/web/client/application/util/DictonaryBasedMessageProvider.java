/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Set;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Message provider based on a {@link Dictionary} instance. The messages are dynamically loaded
 * at runtime from a JavaScript file.
 *
 * @author Franz-Josef Elmer
 */
public class DictonaryBasedMessageProvider implements IMessageProvider
{
    private Dictionary dictionary;
    
    /**
     * Creates a new instance for the specified dictionary name.
     */
    public DictonaryBasedMessageProvider(String dictonaryName)
    {
        dictionary = Dictionary.getDictionary(dictonaryName);
    }
    
    public Set<String> keySet()
    {
        return dictionary.keySet();
    }

    public String getMessage(String key, String... parameters)
    {
        String message;
        try
        {
            message = dictionary.get(key);
        } catch (Exception ex)
        {
            return "Unknown key '" + key + "' in " + dictionary + ".";
        }
        for (int i = 0; i < parameters.length; i++)
        {
            message = message.replace("{" + i + "}", parameters[i]);
        }
        return message;
    }

}
