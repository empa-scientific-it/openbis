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

package ch.systemsx.cisd.common.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A static helper class that should help to extract typed values from {@link Properties}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyUtils
{

    static final String EMPTY_STRING_FORMAT = "Property '%s' is not specified.";

    static final String NON_BOOLEAN_VALUE_FORMAT =
            "Invalid boolean '%s'. Default value '%s' will be used.";

    static final String NON_CHAR_VALUE_FORMAT =
            "Invalid character '%s' (incorrect length). Default value '%s' will be used.";

    static final String NON_INT_VALUE_FORMAT =
            "Invalid integer '%s'. Default value '%s' will be used.";

    static final String NOT_POSITIVE_INT_VALUE_FORMAT =
            "Invalid positive integer '%s'. Default value '%s' will be used.";

    static final String NON_LONG_VALUE_FORMAT =
            "Invalid long '%s'. Default value '%s' will be used.";

    static final String NON_DOUBLE_VALUE_FORMAT =
            "Invalid double '%s'. Default value '%s' will be used.";

    static final String NOT_POSITIVE_LONG_VALUE_FORMAT =
            "Invalid positive long '%s'. Default value '%s' will be used.";

    static final String NOT_FOUND_PROPERTY_FORMAT = "Given key '%s' not found in properties '%s'";

    private static final String LIST_SEPARATOR = ",";

    private PropertyUtils()
    {
        // This class can not be instantiated.
    }

    private static void assertParameters(final Properties properties, final String propertyKey)
    {
        assert properties != null : "Given properties can not be null.";
        assert propertyKey != null : "Given property key can not be null.";
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>true</code> if the <var>propertyKey</var> is found and the trimmed value isn't an empty string. <code>false</code> otherwise.
     */
    public final static boolean hasProperty(final Properties properties, final String propertyKey)
    {
        assertParameters(properties, propertyKey);
        final String propOrNull = getProperty(properties, propertyKey);
        return (propOrNull != null);
    }

    /**
     * Searches for the property with the specified key in this property list. <code>null</code> is returned if there is no property for the specified
     * key or it contains only white space characters.
     * 
     * @return <code>null</code> or the value trimmed if found.
     */
    public final static String getProperty(final Properties properties, final String propertyKey)
    {
        assertParameters(properties, propertyKey);
        final String property = properties.getProperty(propertyKey);
        return StringUtils.isBlank(property) ? null : property.trim();
    }

    /**
     * Searches for the property with the specified key in this property list. <code>null</code> is returned if there is no property for the specified
     * key or it contains only white space characters.
     * 
     * @return <code>null</code> or the non-trimmed value if found.
     */
    public final static String getPropertyDontTrim(final Properties properties, final String propertyKey)
    {
        assertParameters(properties, propertyKey);
        final String property = properties.getProperty(propertyKey);
        return property;
    }

    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @return <code>null</code> or the value trimmed if found.
     */
    public final static String getPropertyKeepEmptyString(final Properties properties,
            final String propertyKey)
    {
        assertParameters(properties, propertyKey);
        final String property = properties.getProperty(propertyKey);
        return (property == null) ? null : property.trim();
    }

    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @return <code>null</code> or the value trimmed if found.
     */
    public final static String getProperty(final Properties properties, final String propertyKey,
            final String defaultValue)
    {
        final String property = getProperty(properties, propertyKey);
        return property == null ? defaultValue : property;
    }

    /**
     * Looks up given mandatory <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @throws ConfigurationFailureException if given <var>propertyKey</var> could not be found or if it is empty.
     */
    public final static String getMandatoryProperty(final Properties properties,
            final String propertyKey) throws ConfigurationFailureException
    {
        assertParameters(properties, propertyKey);
        String property = getProperty(properties, propertyKey);
        if (property == null)
        {
            throw createPropertyNotFoundException(properties, propertyKey);
        }
        if (property.length() == 0)
        {
            throw ConfigurationFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
        }
        return property;
    }

    private static ConfigurationFailureException createPropertyNotFoundException(
            final Properties properties, final String propertyKey)
    {
        return ConfigurationFailureException.fromTemplate(NOT_FOUND_PROPERTY_FORMAT, propertyKey,
                CollectionUtils.abbreviate(Collections.list(properties.propertyNames()), 10));
    }
    
    /**
     * Returns a list of Pattern objects compiled from a comma-separated list of regular expressions.
     * @throws ConfigurationFailureException if a regular expression is invalid
     */
    public static List<Pattern> getPatterns(Properties properties, String key)
    {
        List<Pattern> patterns = new ArrayList<Pattern>();
        List<String> dataSetTypeRegexs = tryGetList(properties, key);
        if (dataSetTypeRegexs != null)
        {
            for (String regex : dataSetTypeRegexs)
            {
                try
                {
                    patterns.add(compilePattern(regex, key));
                } catch (PatternSyntaxException ex)
                {
                    throw new ConfigurationFailureException("Property '" + key
                            + "' has invalid regular expression '" + regex + "': " + ex.getMessage());
                }
            }
        }
        return patterns;
    }
    
    public static Pattern getPattern(Properties properties, String key, String defaultValue)
    {
        return compilePattern(properties.getProperty(key, defaultValue), key);
    }
    
    private static Pattern compilePattern(String regex, String key)
    {
        try
        {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException ex)
        {
            throw new ConfigurationFailureException("Property '" + key
                    + "' has invalid regular expression '" + regex + "': " + ex.getMessage());
        }
    }

    /**
     * @returns a list of comma separated values at the specified property key. Each item is trimmed and in upper case.
     * @throws ConfigurationFailureException when a property is not specified or is empty
     */
    public final static List<String> getMandatoryList(Properties properties, String propertyKey)
    {
        List<String> list = tryGetList(properties, propertyKey);
        if (list == null)
        {
            throw createPropertyNotFoundException(properties, propertyKey);
        }
        if (list.size() == 0)
        {
            throw ConfigurationFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
        }
        return list;
    }

    /**
     * @returns a list of comma separated values at the specific property key. Each item is trimmed and in upper cases.
     */
    public final static List<String> tryGetList(Properties properties, String propertyKey)
    {
        String itemsList = PropertyUtils.getProperty(properties, propertyKey);
        if (itemsList == null)
        {
            return null;
        }
        String[] items = itemsList.split(LIST_SEPARATOR);
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim().toUpperCase();
        }
        return Arrays.asList(items);
    }

    /**
     * @returns A list of comma separated values at the specific property key. Each item is trimmed, but case is not changed.
     */
    public final static List<String> tryGetListInOriginalCase(Properties properties,
            String propertyKey)
    {
        String itemsList = PropertyUtils.getProperty(properties, propertyKey);
        if (itemsList == null)
        {
            return null;
        }
        String[] items = itemsList.split(LIST_SEPARATOR);
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim();
        }
        return Arrays.asList(items);
    }

    /**
     * Returns list of comma separated values at the specific property key. Each element is trimmed.
     * 
     * @return an empty list if the property is undefined or an empty string.
     */
    public static List<String> getList(Properties properties, String propertyKey)
    {
        List<String> result = new ArrayList<String>();
        String property = PropertyUtils.getProperty(properties, propertyKey);
        if (StringUtils.isNotBlank(property))
        {
            String[] splittedProperty = property.split(",");
            for (String term : splittedProperty)
            {
                result.add(term.trim());
            }
        }
        return result;
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getLong(final Properties properties, final String propertyKey,
            final long defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String longOrNull = getProperty(properties, propertyKey);
        if (longOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isCreatable(longOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_LONG_VALUE_FORMAT, longOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return NumberUtils.createNumber(longOrNull).longValue();
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getLong(final Properties properties, final String propertyKey,
            final long defaultValue)
    {
        return getLong(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getPosLong(final Properties properties, final String propertyKey,
            final long defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        assert defaultValue > -1 : "Negative default value (< 0).";
        final String longOrNull = getProperty(properties, propertyKey);
        if (longOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isDigits(longOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_LONG_VALUE_FORMAT, longOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return Long.parseLong(longOrNull);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getPosLong(final Properties properties, final String propertyKey,
            final long defaultValue)
    {
        return getPosLong(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getInt(final Properties properties, final String propertyKey,
            final int defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String intOrNull = getProperty(properties, propertyKey);
        if (intOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isCreatable(intOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_INT_VALUE_FORMAT, intOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return NumberUtils.createNumber(intOrNull).intValue();
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getInt(final Properties properties, final String propertyKey,
            final int defaultValue)
    {
        return getInt(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getPosInt(final Properties properties, final String propertyKey,
            final int defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        assert defaultValue > -1 : "Negative default value (< 0).";
        final String intOrNull = getProperty(properties, propertyKey);
        if (intOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isDigits(intOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_INT_VALUE_FORMAT, intOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return Integer.parseInt(intOrNull);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getPosInt(final Properties properties, final String propertyKey,
            final int defaultValue)
    {
        return getPosInt(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static double getDouble(final Properties properties, final String propertyKey,
            final double defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String doubleOrNull = getProperty(properties, propertyKey);
        if (doubleOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isCreatable(doubleOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO,
                        String.format(NON_DOUBLE_VALUE_FORMAT, doubleOrNull,
                                defaultValue));
            }
            return defaultValue;
        }
        return Double.parseDouble(doubleOrNull);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static double getDouble(final Properties properties, final String propertyKey,
            final double defaultValue)
    {
        return getDouble(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static boolean getBoolean(final Properties properties, final String propertyKey,
            final boolean defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String booleanOrNull = getProperty(properties, propertyKey);
        if (booleanOrNull == null)
        {
            return defaultValue;
        }
        final Boolean bool = Boolean.getBoolean(booleanOrNull);
        if (bool == null)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_BOOLEAN_VALUE_FORMAT,
                        booleanOrNull, defaultValue));
            }
            return defaultValue;
        }
        return Boolean.toBoolean(bool);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static char getChar(final Properties properties, final String propertyKey,
            final char defaultValue)
    {
        return getChar(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static char getChar(final Properties properties, final String propertyKey,
            final char defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        String charOrNull = getPropertyDontTrim(properties, propertyKey);
        if (charOrNull == null)
        {
            return defaultValue;
        }
        if (charOrNull.length() == 1)
        {
            return charOrNull.charAt(0);
        }
        charOrNull = charOrNull.trim();
        if (charOrNull.length() == 1)
        {
            return charOrNull.charAt(0);
        }
        else
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_CHAR_VALUE_FORMAT, charOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static boolean getBoolean(final Properties properties, final String propertyKey,
            final boolean defaultValue)
    {
        return getBoolean(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     * @throws ConfigurationFailureException if property value is not empty, and does not represent existing directory.
     */
    public static final File getDirectory(final Properties properties, final String propertyKey, final File defaultValue)
    {
        String propertyValue = getProperty(properties, propertyKey, null);
        if (propertyValue == null)
        {
            return defaultValue;
        }
        File file = new File(propertyValue);
        if (file.isDirectory())
        {
            return file;
        }
        else
        {
            throw new ConfigurationFailureException("Property '" + propertyKey + "' is expected to be existing directory. " + propertyValue);
        }
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     * @throws ConfigurationFailureException if property value is not empty, and does not represent existing file (but not directory).
     */
    public static final File getFile(final Properties properties, final String propertyKey, final File defaultValue)
    {
        String propertyValue = getProperty(properties, propertyKey, null);
        if (propertyValue == null)
        {
            return defaultValue;
        }
        File file = new File(propertyValue);
        if (file.isFile())
        {
            return file;
        }
        else
        {
            throw new ConfigurationFailureException("Property '" + propertyKey + "' is expected to be existing normal file. " + propertyValue);
        }
    }

    //
    // Helper classes
    //

    public static enum Boolean
    {
        TRUE("1", "yes", "true"), FALSE("0", "no", "false");

        private final String[] values;

        Boolean(final String... values)
        {
            this.values = values;
        }

        public final static Boolean getBoolean(final String value)
        {
            if (contains(TRUE.values, value))
            {
                return TRUE;
            }
            if (contains(FALSE.values, value))
            {
                return FALSE;
            }
            return null;
        }

        public final boolean toBoolean()
        {
            return toBoolean(this);
        }

        public final static boolean toBoolean(final Boolean bool)
        {
            if (bool == TRUE)
            {
                return true;
            }
            return false;
        }

        private final static boolean contains(final String[] values, final String value)
        {
            for (final String string : values)
            {
                if (string.equalsIgnoreCase(value))
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Trims each value of given <var>properties</var> using {@link StringUtils#trim(String)}.
     */
    @SuppressWarnings("unchecked")
    public final static void trimProperties(final Properties properties)
    {
        assert properties != null : "Unspecified properties";
        for (final Enumeration<String> enumeration =
                (Enumeration<String>) properties.propertyNames(); enumeration.hasMoreElements(); /**/)
        {
            final String key = enumeration.nextElement();
            properties.setProperty(key, StringUtils.trim(properties.getProperty(key)));
        }
    }

}
