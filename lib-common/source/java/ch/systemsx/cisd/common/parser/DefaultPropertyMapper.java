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

package ch.systemsx.cisd.common.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * A default <code>IPropertyMapper</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public class DefaultPropertyMapper implements IPropertyMapper
{

    private static final String IGNORED_COLUMN_PREFIX = "!";

    private final TableMap<String, IPropertyModel> propertyModels;

    private final Map<String, String> defaults;

    public DefaultPropertyMapper(final String[] properties, final Map<String, String> defaults)
            throws IllegalArgumentException
    {
        assert properties != null : "Unspecified properties";
        if (defaults == null)
        {
            this.defaults = Collections.emptyMap();
        } else
        {
            this.defaults = defaults;
        }
        propertyModels =
                new TableMap<String, IPropertyModel>(new IKeyExtractor<String, IPropertyModel>()
                    {

                        //
                        // IKeyExtractor
                        //

                        @Override
                        public final String getKey(final IPropertyModel e)
                        {
                            return e.getCode().toLowerCase();
                        }
                    });
        tokensToMap(properties);

    }

    private final void tokensToMap(final String[] properties) throws IllegalArgumentException
    {
        final int len = properties.length;
        for (int i = 0; i < len; i++)
        {
            final String token = properties[i];
            if (StringUtils.isBlank(token))
            {
                throw new IllegalArgumentException(String.format("%s token of %s is blank.",
                        StringUtilities.getOrdinal(i), Arrays.asList(properties)));
            }
            if (token.startsWith(IGNORED_COLUMN_PREFIX) == false)
            {
                propertyModels.add(new MappedProperty(i, token));
            }
        }

        int i = -1;
        for (String defaultKey : defaults.keySet())
        {
            if (propertyModels.tryGet(defaultKey) == null)
            {
                propertyModels.add(new MappedProperty(i--, defaultKey));
            }
        }
    }

    //
    // IPropertyMapper
    //

    @Override
    public final boolean containsPropertyCode(final String propertyCode)
    {
        return propertyModels.tryGet(propertyCode.toLowerCase()) != null;
    }

    @Override
    public final Set<String> getAllPropertyCodes()
    {
        return new TreeSet<String>(propertyModels.keySet());
    }

    @Override
    public final IPropertyModel getPropertyModel(final String propertyCode)
            throws IllegalArgumentException
    {
        if (containsPropertyCode(propertyCode) == false)
        {
            throw new IllegalArgumentException(String.format("Given property '%s' does not exist.",
                    propertyCode));
        }
        return propertyModels.tryGet(propertyCode.toLowerCase());
    }

    @Override
    public String tryGetPropertyDefault(final String propertyCode)
    {
        return defaults.get(propertyCode);
    }
}
