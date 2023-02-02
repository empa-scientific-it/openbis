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

package ch.systemsx.cisd.common.spring;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @author pkupczyk
 */
public class PropertyPlaceholderUtils
{

    private static final String PLACEHOLDER_PREFIX =
            PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX;

    public static final Integer getInteger(String value, Integer defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value.startsWith(PLACEHOLDER_PREFIX))
        {
            return defaultValue;
        } else
        {
            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Incorrect integer property value: " + value, e);
            }
        }
    }

    public static final Boolean getBoolean(String value, Boolean defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value.startsWith(PLACEHOLDER_PREFIX))
        {
            return defaultValue;
        } else
        {
            if ("true".equalsIgnoreCase(value))
            {
                return true;
            } else if ("false".equalsIgnoreCase(value))
            {
                return false;
            } else
            {
                throw new IllegalArgumentException("Incorrect boolean property value: " + value);
            }
        }
    }

}
