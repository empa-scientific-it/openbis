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

import java.util.MissingResourceException;

import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.i18n.client.Dictionary;

/**
 * Message provider based on a {@link Dictionary} instance. The messages are dynamically loaded at runtime from a JavaScript file.
 * 
 * @author Franz-Josef Elmer
 */
public final class DictonaryBasedMessageProvider implements IMessageProvider
{
    private final Dictionary dictionary;

    private final String dictionaryName;

    public DictonaryBasedMessageProvider(final String dictionaryName)
    {
        this.dictionaryName = dictionaryName;
        this.dictionary = Dictionary.getDictionary(dictionaryName);
    }

    //
    // IMessageProvider
    //

    @Override
    public String getName()
    {
        return dictionaryName;
    }

    @Override
    public final boolean containsKey(final String key)
    {
        return dictionary.keySet().contains(key);
    }

    @Override
    public final String getMessage(final String key, final Object... parameters)
    {
        String message;
        try
        {
            message = dictionary.get(key);
        } catch (final MissingResourceException ex)
        {
            return "Unknown key '" + key + "' in dictionary '" + dictionaryName + "'.";
        }
        if (parameters.length == 0)
        {
            return message;
        }
        return Format.substitute(message, parameters);
    }

}
