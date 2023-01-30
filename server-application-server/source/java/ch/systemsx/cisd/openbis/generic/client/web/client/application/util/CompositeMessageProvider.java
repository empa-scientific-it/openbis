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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link IMessageProvider} implementation based on <i>Composite</i> pattern.
 * 
 * @author Christian Ribeaud
 */
public final class CompositeMessageProvider implements IMessageProvider
{
    private static final String TRUE = "true";

    private static final String IS_DEFAULT_DICTIONARY = "is_default_dictionary";

    private final List<IMessageProvider> messageProviders = new ArrayList<IMessageProvider>();

    private String name;

    public void add(IMessageProvider messageProvider)
    {
        messageProviders.add(messageProvider);
    }

    //
    // IMessageProvider
    //

    @Override
    public String getName()
    {
        if (name == null)
        {
            StringBuffer buffer = new StringBuffer();
            for (IMessageProvider messageProvider : messageProviders)
            {
                if (buffer.length() > 0)
                {
                    buffer.append(", ");
                }
                buffer.append(messageProvider.getName());
            }
            name = "[" + buffer.toString() + "]";
        }
        return name;
    }

    @Override
    public final boolean containsKey(final String key)
    {
        for (final IMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public final String getMessage(final String key, final Object... parameters)
    {
        IMessageProvider defaultMessageProvider = tryGetDefaultDictionary(key);
        if (defaultMessageProvider != null && defaultMessageProvider.containsKey(key))
        {
            return defaultMessageProvider.getMessage(key, parameters);
        }
        for (final IMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(key))
            {
                return messageProvider.getMessage(key, parameters);
            }
        }
        return "Unknown key '" + key + "' in dictonaries " + getName() + ".";
    }

    /**
     * Returns a {@link IMessageProvider} containing key {@link #IS_DEFAULT_DICTIONARY} with value = {@link #TRUE} or null if no such
     * {@link IMessageProvider} defined.
     */
    private IMessageProvider tryGetDefaultDictionary(final String key)
    {
        for (final IMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(IS_DEFAULT_DICTIONARY))
            {
                String isDefaultMsg = messageProvider.getMessage(IS_DEFAULT_DICTIONARY);
                if (isDefaultMsg.compareToIgnoreCase(TRUE) == 0)
                {
                    return messageProvider;
                }
            }
        }
        return null;
    }

}
