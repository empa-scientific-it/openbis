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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Configuration parameters for Servlets.
 *
 * @author Franz-Josef Elmer
 */
public final class PluginServletConfig
{
    private final String servletClass;

    private final String servletPath;

    private final Map<String, String> servletProperties;

    public PluginServletConfig(String servletClass, String servletPath, Properties servletProperties)
    {
        this.servletClass = servletClass;
        this.servletPath = servletPath;
        this.servletProperties = new HashMap<String, String>();
        for (Entry<Object, Object> e : servletProperties.entrySet())
        {
            this.servletProperties.put(e.getKey().toString(), e.getValue().toString());
        }
    }

    public String getServletClass()
    {
        return servletClass;
    }

    /** URL path at which the servlet will be deployed */
    public String getServletPath()
    {
        return servletPath;
    }

    /** Any additional properties specified in the properties file */
    public Map<String, String> getServletProperties()
    {
        return servletProperties;
    }

    @Override
    public String toString()
    {
        return "class = " + servletClass + ", path = " + servletPath;
    }

}