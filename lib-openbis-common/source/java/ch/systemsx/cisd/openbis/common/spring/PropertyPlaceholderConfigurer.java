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

package ch.systemsx.cisd.openbis.common.spring;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * A <code>PropertyPlaceholderConfigurer</code> extension which allows us to set the placeholder resolver mode via the <code>service.properties</code>
 * file.
 * <p>
 * If not specified, the default mode is
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#SYSTEM_PROPERTIES_MODE_FALLBACK}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class PropertyPlaceholderConfigurer extends
        org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
{

    /**
     * Default value which is taken when {@link #setSystemPropertiesModeName(String)} method is not called.
     */
    private final static String DEFAULT_PLACEHOLDER_RESOLVER_MODE =
            "SYSTEM_PROPERTIES_MODE_FALLBACK";

    private String systemPropertiesModePlaceholder;

    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    private final String cleanPropertyKey(final String propertyKey)
    {
        return propertyKey.substring(placeholderPrefix.length(), propertyKey.length()
                - placeholderSuffix.length());
    }

    //
    // PropertyPlaceholderConfigurer
    //

    @Override
    protected final String convertPropertyValue(final String originalValue)
    {
        // Can handle null value
        return StringUtils.trimWhitespace(originalValue);
    }

    @Override
    protected final void processProperties(
            final ConfigurableListableBeanFactory beanFactoryToProcess, final Properties props)
            throws BeansException
    {
        String resolvedPlaceholder = null;
        if (systemPropertiesModePlaceholder != null)
        {
            try
            {
                // Default working mode of 'parseStringValue' is 'SYSTEM_PROPERTIES_MODE_FALLBACK'.
                resolvedPlaceholder =
                        resolvePlaceholder(systemPropertiesModePlaceholder, props);
            } catch (final BeanDefinitionStoreException ex)
            {
                // Does nothing. Rest of code can handle this.
            }
        }
        if (resolvedPlaceholder == null
                || resolvedPlaceholder.equals(systemPropertiesModePlaceholder))
        {
            resolvedPlaceholder = DEFAULT_PLACEHOLDER_RESOLVER_MODE;
        }
        super.setSystemPropertiesModeName(resolvedPlaceholder);
        if (systemPropertiesModePlaceholder != null)
        {
            props.setProperty(cleanPropertyKey(systemPropertiesModePlaceholder),
                    resolvedPlaceholder);
        }
        super.processProperties(beanFactoryToProcess, props);
    }

    @Override
    public final void setSystemPropertiesModeName(final String placeHolder)
            throws IllegalArgumentException
    {
        systemPropertiesModePlaceholder = placeHolder;
    }

    /**
     * Set the prefix that a placeholder string starts with. The default is "${".
     * 
     * @see #DEFAULT_PLACEHOLDER_PREFIX
     */
    @Override
    public final void setPlaceholderPrefix(final String placeholderPrefix)
    {
        this.placeholderPrefix = placeholderPrefix;
        super.setPlaceholderPrefix(placeholderPrefix);
    }

    /**
     * Set the suffix that a placeholder string ends with. The default is "}".
     * 
     * @see #DEFAULT_PLACEHOLDER_SUFFIX
     */
    @Override
    public void setPlaceholderSuffix(final String placeholderSuffix)
    {
        this.placeholderSuffix = placeholderSuffix;
        super.setPlaceholderSuffix(placeholderSuffix);
    }
}
