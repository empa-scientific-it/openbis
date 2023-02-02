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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * A static helper class to help with loading and saving {@link Properties}.
 *
 * @author Bernd Rinn
 */
public class PropertyIOUtils
{

    /**
     * Loads properties from the specified file and adds them to the specified properties.
     * <ul>
     * <li>Empty lines and lines starting with a hash symbol '#' are ignored.
     * <li>All other lines should have a equals symbol '='. The trimmed part before/after '=' specify property key and value , respectively.
     * </ul>
     */
    public static void loadAndAppendProperties(Properties properties, File propertiesFile)
    {
        List<String> lines = FileUtilities.loadToStringList(propertiesFile);
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            if (line.length() == 0 || line.startsWith("#"))
            {
                continue;
            }
            int indexOfEqualSymbol = line.indexOf('=');
            if (indexOfEqualSymbol < 0)
            {
                throw new UserFailureException("Missing '=' in line " + (i + 1)
                        + " of properties file '" + propertiesFile + "': " + line);
            }
            String key = line.substring(0, indexOfEqualSymbol).trim();
            String value = line.substring(indexOfEqualSymbol + 1).trim();
            properties.setProperty(key, value);
        }
    }

    /**
     * Loads properties from the specified file. This is a simpler version of {@link PropertyIOUtils#loadProperties(String)} which does not use
     * {@link Properties#load(InputStream)}:
     * <ul>
     * <li>Empty lines and lines starting with a hash symbol '#' are ignored.
     * <li>All other lines should have a equals symbol '='. The trimmed part before/after '=' specify property key and value , respectively.
     * </ul>
     * There is no character escaping as in {@link Properties#load(InputStream)} because this can lead to problems if this syntax isn't known by the
     * creator of a properties file.
     */
    public static Properties loadProperties(File propertiesFile)
    {
        Properties properties = new Properties();
        loadAndAppendProperties(properties, propertiesFile);
        return properties;
    }

    /**
     * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the properties.
     * @return never <code>null</code> but could return empty properties.
     */
    public final static Properties loadProperties(final InputStream is, final String resourceName)
    {
        assert is != null : "No input stream specified";
        final Properties properties = new Properties();
        try
        {
            properties.load(is);
            PropertyUtils.trimProperties(properties);
            return properties;
        } catch (final Exception ex)
        {
            final String msg =
                    String.format("Could not load the properties from given resource '%s'.",
                            resourceName);
            throw new ConfigurationFailureException(msg, ex);
        } finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the properties.
     * @return never <code>null</code> but could return empty properties.
     */
    public final static Properties loadProperties(final String propertiesFilePath)
    {
        try
        {
            return loadProperties(new FileInputStream(propertiesFilePath), propertiesFilePath);
        } catch (FileNotFoundException ex)
        {
            final String msg = String.format("Properties file '%s' not found.", propertiesFilePath);
            throw new ConfigurationFailureException(msg, ex);
        }
    }

    /**
     * Saves the given <var>properties</var> to the given <var>propertiesFile</var>.
     */
    public static void saveProperties(File propertiesFile, Properties properties)
    {
        final StringBuilder builder = new StringBuilder();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();
            builder.append(key);
            builder.append(" = ");
            builder.append(value);
            builder.append('\n');
        }
        FileUtilities.writeToFile(propertiesFile, builder.toString());
    }

}
