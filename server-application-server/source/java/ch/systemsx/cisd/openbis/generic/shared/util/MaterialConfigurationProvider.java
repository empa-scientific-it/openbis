/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * A singleton to keep application wide material-specific configuration.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialConfigurationProvider
{
    private static MaterialConfigurationProvider instance;

    public static final MaterialConfigurationProvider getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("Instance not yet initialized");
        }
        return instance;
    }

    /**
     * only used for unit tests.
     */
    public static final MaterialConfigurationProvider initializeForTesting(
            boolean isRelaxCodeConstraints)
    {
        MaterialConfigurationProvider oldInstance = instance;
        instance = new MaterialConfigurationProvider(isRelaxCodeConstraints);
        return oldInstance;
    }

    /**
     * only used for unit tests.
     */
    public static void restoreFromTesting(MaterialConfigurationProvider provider)
    {
        instance = provider;
    }

    // invoked from Spring
    static final void initialize(String relaxCodeConstraints)
    {
        boolean isRelaxCodeConstraints = parseBooleanValue(relaxCodeConstraints, false);
        instance = new MaterialConfigurationProvider(isRelaxCodeConstraints);
    }

    private final boolean isRelaxCodeConstraints;

    private MaterialConfigurationProvider(boolean isRelaxCodeConstraints)
    {
        this.isRelaxCodeConstraints = isRelaxCodeConstraints;
    }

    /**
     * Return <code>true</code> if the material codes can contain special characters e.g. $/.()%
     */
    public boolean isRelaxedCodeConstraints()
    {
        return isRelaxCodeConstraints;
    }

    /**
     * Return <code>true</code> if the material codes can contain only alphanumeric characters and underscores.
     */
    public boolean isStrictCodeConstraints()
    {
        return false == isRelaxedCodeConstraints();
    }

    private static boolean parseBooleanValue(String stringValue, boolean defaultValue)
    {
        PropertyUtils.Boolean value = PropertyUtils.Boolean.getBoolean(stringValue);
        if (value == null)
        {
            return defaultValue;
        }
        return value == PropertyUtils.Boolean.TRUE;
    }
}
