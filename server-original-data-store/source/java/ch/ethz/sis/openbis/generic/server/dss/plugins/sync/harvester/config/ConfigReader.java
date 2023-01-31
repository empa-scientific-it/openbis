/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

public class ConfigReader
{

    private static final String IGNORE_LINE_CHAR = "#";

    private Pattern sectionRegex = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");

    private Pattern keyValueRegex = Pattern.compile("\\s*([^=]*)=(.*)");

    private Map<String, Map<String, String>> entries = new LinkedHashMap<>();

    public ConfigReader(String path) throws IOException
    {
        load(path);
    }

    public ConfigReader(File file) throws IOException
    {
        loadFile(file);
    }

    public int getSectionCount()
    {
        return entries.keySet().size();
    }

    public String getSection(int index)
    {
        if (index > getSectionCount())
        {
            throw new RuntimeException("Section with index " + index + " does not exist.");
        }
        return entries.keySet().toArray(new String[entries.keySet().size()])[index];
    }

    public boolean sectionExists(String name)
    {
        Map<String, String> kvMap = entries.get(name);
        if (kvMap == null)
        {
            return false;
        }
        return true;
    }

    public void loadFile(File file) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            String section = null;
            while ((line = br.readLine()) != null)
            {
                Matcher m = sectionRegex.matcher(line);
                if (m.matches())
                {
                    section = m.group(1).trim();
                } else if (section != null)
                {
                    m = keyValueRegex.matcher(line);
                    if (m.matches() && line.startsWith(IGNORE_LINE_CHAR) == false)
                    {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> map = entries.get(section);
                        if (map == null)
                        {
                            entries.put(section, map = new HashMap<>());
                        }
                        map.put(key, value);
                    }
                }
            }
        }
    }

    public void load(String path) throws IOException
    {
        loadFile(new File(path));
    }

    public List<String> getStrings(String section, String key, List<String> defaultStrings)
    {
        String strings = getValue(section, key);
        if (strings == null)
        {
            return defaultStrings;
        }
        List<String> result = new ArrayList<>();
        for (String string : strings.split(","))
        {
            result.add(string.trim());
        }
        return result;
    }

    public String getString(String section, String key, String defaultvalue, boolean mandatory)
    {
        String val = getValue(section, key);
        if (val == null)
        {
            if (mandatory)
            {
                throw new ConfigurationFailureException("Property '" + key + "' in section '" + section + "'  is mandatory.");
            }
            return defaultvalue;
        }
        return val;
    }

    private String getValue(String section, String key) throws ConfigurationFailureException
    {
        Map<String, String> map = entries.get(section);
        if (map == null)
        {
            throw new ConfigurationFailureException("Section '" + section + " does not exist.");
        }
        String val = map.get(key);
        if (StringUtils.isBlank(val) == true)
        {
            return null;
        }
        return val.trim();
    }

    public Integer getInt(String section, String key, Integer defaultvalue, boolean mandatory)
    {
        String val = getValue(section, key);
        if (val == null)
        {
            if (mandatory)
            {
                throw new ConfigurationFailureException("Property '" + key + "' in section '" + section + "'  is mandatory.");
            }
            return defaultvalue;
        }
        return Integer.parseInt(val);
    }

    public double getDouble(String section, String key, double defaultvalue, boolean mandatory)
    {
        String val = getValue(section, key);
        if (val == null)
        {
            if (mandatory)
            {
                throw new ConfigurationFailureException("Property '" + key + "' in section '" + section + "'  is mandatory.");
            }
            return defaultvalue;
        }
        return Double.parseDouble(val);
    }

    public boolean getBoolean(String section, String key, boolean defaultvalue)
    {
        String val = getValue(section, key);
        return val == null ? defaultvalue : Boolean.parseBoolean(val);
    }
}