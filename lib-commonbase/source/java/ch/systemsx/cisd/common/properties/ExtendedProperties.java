/*
 * Copyright 2007 ETH Zuerich, CISD
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
package ch.systemsx.cisd.common.properties;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * An extension of {@link Properties}. The extension avoid duplicating properties by reusing. There are two ways to reuse properties:
 * <ol>
 * <li>Use properties in property values. For example,
 * 
 * <pre>
 * A=12345678
 * B=${A}90
 * C=${B} plus more
 * </pre>
 * 
 * will result in <code>getProperty("C")</code> returning the value "1234567890 plus more". Cyclic references are handled by removing the current key
 * before resolving it, i.e. when setting A=${B} and B=${A} and then asking for A, you will get ${A}. Also, default values can be defined. Example:
 * 
 * <pre>
 * greeting = hello ${user:world}
 * </pre>
 * 
 * If property 'user' hasn't been defined <code>getProperty("greeting")</code> returns "hello world".
 * <li>Inherit properties. For example,
 * 
 * <pre>
 * type.code = ALPHA
 * type.label = Alpha
 * validator.order = 1
 * validator.type. = type.
 * my.validator. = validator.
 * my.validator.type.label = A L P H A
 * </pre>
 * 
 * will result in <code>getProperty("my.validator.type.code")</code> returning the value "ALPHA". All keys ending with a dot '.' should refer to a key
 * prefix. All properties starting with this prefix are also properties of the subtree starting with the key with the dot at the end. Inherit
 * properties can be overridden.
 * </ol>
 * 
 * @author Christian Ribeaud
 * @author Franz-Josef Elmer
 */
public final class ExtendedProperties extends Properties
{

    private static final String PATH_DELIMITER = ".";

    private static final long serialVersionUID = 1L;

    /** Usual (or default) property separator in the super class. */
    private static final String PROPERTY_SEPARATOR = ", ";

    /** Default placeholder suffix: "}" */
    private static final String SUFFIX = "}";

    /** Default placeholder delim for default value: ":" */
    private static final String DEFAULT_VALUE_DELIM = ":";

    /** Default placeholder prefix: "${" */
    private static final String PREFIX = "${";

    /** The minimum length a string should have to be considered. */
    private static final int MIN_LENGTH = PREFIX.length() + SUFFIX.length() + 1;

    public static ExtendedProperties createWith(final Properties properties)
    {
        assert properties != null : "Unspecified properties.";
        final ExtendedProperties result = new ExtendedProperties();
        result.putAll(properties);
        return result;
    }

    /**
     * @see Properties#Properties()
     */
    public ExtendedProperties()
    {
        super();
    }

    /**
     * Returns a subset of given <code>Properties</code> based on given property key prefix. The subset contains also inherited properties.
     * 
     * @param prefix string, each property key should start with.
     * @param dropPrefix If <code>true</code> the prefix will be removed from the key.
     */
    public static ExtendedProperties getSubset(final Properties properties, final String prefix,
            final boolean dropPrefix)
    {
        return ExtendedProperties.createWith(properties).getSubset(prefix, dropPrefix);
    }

    public final ExtendedProperties getSubset(final String prefix, final boolean dropPrefix)
    {
        assert prefix != null : "Missing prefix";

        return getSubset(prefix, dropPrefix, new HashSet<String>());
    }

    private ExtendedProperties getSubset(final String prefix, final boolean dropPrefix, Set<String> keys)
    {
        final ExtendedProperties result = new ExtendedProperties();
        final int prefixLength = prefix.length();
        for (final Enumeration<?> enumeration = propertyNames(); enumeration.hasMoreElements();)
        {
            final String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix) && key.endsWith(PATH_DELIMITER))
            {
                assertNoCyclicDependency(keys, key);
                keys.add(key);
                String inheritTree = super.getProperty(key);
                for (Entry<Object, Object> entry : getSubset(inheritTree, true, keys).entrySet())
                {
                    String newKey = key.substring(0, key.length()) + entry.getKey();
                    result.put(createKey(newKey, dropPrefix, prefixLength), entry.getValue());
                }
                keys.remove(key);
            }
        }
        for (final Enumeration<?> enumeration = propertyNames(); enumeration.hasMoreElements();)
        {
            final String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix) && key.endsWith(PATH_DELIMITER) == false)
            {
                result.put(createKey(key, dropPrefix, prefixLength), getProperty(key));
            }
        }
        return result;
    }

    private String createKey(final String key, final boolean dropPrefix, final int prefixLength)
    {
        return dropPrefix ? key.substring(prefixLength) : key;
    }

    /**
     * Removes all properties with names starting with given prefix
     */
    public void removeSubset(final String prefix)
    {
        for (final Enumeration<?> enumeration = propertyNames(); enumeration.hasMoreElements();)
        {
            final String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix))
            {
                remove(key);
            }
        }
    }

    private final String expandValue(final String key, final String value, Set<String> keys)
    {
        if (value == null || value.length() < MIN_LENGTH)
        {
            return value;
        }
        final StringBuilder result = new StringBuilder(value.length());
        result.append(value);
        int startName = result.indexOf(PREFIX);
        final int prefixLen = PREFIX.length();
        int endName = result.indexOf(SUFFIX, startName + prefixLen);
        final int suffixLen = SUFFIX.length();
        while (startName >= 0 && endName > startName)
        {
            String paramName = result.substring(startName + prefixLen, endName);
            String paramValue = null;
            int indexOfDefaultValueDelim = paramName.indexOf(DEFAULT_VALUE_DELIM);
            if (indexOfDefaultValueDelim > 0)
            {
                paramValue = paramName.substring(indexOfDefaultValueDelim + 1);
                paramName = paramName.substring(0, indexOfDefaultValueDelim);
            }
            if (keys.contains(paramName) == false)
            {
                keys.add(key);
                String propertyValue = getProperty(paramName, keys);
                if (propertyValue != null)
                {
                    paramValue = propertyValue;
                }
                keys.remove(key);
            }
            if (paramValue != null)
            {
                result.replace(startName, endName + suffixLen, paramValue);
                startName += paramValue.length();
            } else
            {
                startName = endName + suffixLen;
            }
            startName = result.indexOf(PREFIX, startName);
            endName = result.indexOf(SUFFIX, startName + prefixLen);
        }
        return result.toString();
    }

    /**
     * Returns the value of property <code>key</code> without resolving.
     * <p>
     * So the {@link java.util.Map#get(java.lang.Object)} works as usual and returns raw (not expanded with substituted parameters) property value.
     * </p>
     */
    public final String getUnalteredProperty(final String key)
    {
        return (String) get(key);
    }

    //
    // Properties
    //

    /**
     * Returns the value of specified property or <code>null</code> if undefined. This method behaves differently then the same method of the
     * superclass:
     * <ul>
     * <li>If nothing found for the specified key other keys are tried as follows: For all dot characters '.' in the key (starting from the right
     * most) it looks recursively for a replacement of the left part of the key (including the dot) among all properties.
     * <p>
     * Example:
     * 
     * <pre>
     * type.code = ALPHA
     * type.label = Alpha
     * validator.order = 1
     * validator.type. = type.
     * my.validator. = validator.
     * my.validator.type.label = A L P H A
     * </pre>
     * 
     * The following table shows the returned value of <code>getProperty()</code> for various keys:
     * <table border=1 cellspacing=1 cellpadding=5>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>validator.order</td>
     * <td>1</td>
     * </tr>
     * <tr>
     * <td>validator.type.code</td>
     * <td>ALPHA</td>
     * </tr>
     * <tr>
     * <td>validator.type.label</td>
     * <td>Alpha</td>
     * </tr>
     * <tr>
     * <td>my.validator.order</td>
     * <td>1</td>
     * </tr>
     * <tr>
     * <td>my.validator.type.code</td>
     * <td>ALPHA</td>
     * </tr>
     * <tr>
     * <td>my.validator.type.label</td>
     * <td>A L P H A</td>
     * </tr>
     * </table>
     * This mechanism allows to inherit property values from complete subtrees of properties.
     * <li>Any parameter like <code>${propertyName}</code> in property value will be replaced with the value of property with name
     * <code>propertyName</code>.
     * <p>
     * For example, for the following set of properties:
     * 
     * <pre>
     * param1 = abcd
     * param2 = efgh
     * param3 = Alphabet starts with: ${param1}${param2}
     * </pre>
     * 
     * The call <code>props.getProperty("param3")</code> returns:
     * 
     * <pre>
     * Alphabet starts with: abcdefgh
     * </pre>
     * 
     * </ul>
     * </p>
     * 
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    @Override
    public final String getProperty(final String key)
    {
        return getProperty(key, new HashSet<String>());
    }

    private String getProperty(final String key, Set<String> keys)
    {
        String result = super.getProperty(key);
        if (result == null)
        {
            int index = key.length();
            while (index > 0)
            {
                int lastIndexOfPathDelimiter = key.lastIndexOf(PATH_DELIMITER, index);
                if (lastIndexOfPathDelimiter >= 0)
                {
                    String newPath =
                            super.getProperty(key.substring(0, lastIndexOfPathDelimiter + 1));
                    if (newPath != null)
                    {
                        assertNoCyclicDependency(keys, key);
                        keys.add(key);
                        String newKey = newPath + key.substring(lastIndexOfPathDelimiter + 1);
                        result = getProperty(newKey, keys);
                        keys.remove(key);
                        break;
                    }
                }
                index = lastIndexOfPathDelimiter - 1;
            }
        }
        return result == null ? null : expandValue(key, result, keys);
    }

    private void assertNoCyclicDependency(Set<String> keys, final String key)
    {
        if (keys.contains(key))
        {
            throw new IllegalArgumentException("Cyclic definition of property '" + key + "'.");
        }
    }

    /**
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    @Override
    public final String getProperty(final String key, final String defaultValue)
    {
        final String result = getProperty(key);
        return result == null ? defaultValue : result;
    }

    @Override
    public final synchronized String toString()
    {
        return StringUtils
                .replace(super.toString(), PROPERTY_SEPARATOR, SystemUtils.LINE_SEPARATOR);
    }
}
