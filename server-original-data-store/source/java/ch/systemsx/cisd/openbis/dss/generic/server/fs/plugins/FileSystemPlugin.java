/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins;

import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverPlugin;

/**
 * @author Jakub Straszewski
 */
public class FileSystemPlugin
{
    public final String pluginCode;

    public final Class<?> pluginResolverClass;

    private final String pluginName;

    public FileSystemPlugin(String pluginName, String pluginCode, Class<?> pluginResolverClass)
    {
        this.pluginName = pluginName;
        this.pluginCode = pluginCode;
        this.pluginResolverClass = pluginResolverClass;
        try
        {
            // cast to test if the instantiation can be done and is creating an object of a proper type
            @SuppressWarnings("unused")
            IResolverPlugin result = (IResolverPlugin) pluginResolverClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalStateException("Couldn't instantiate object of type " + pluginResolverClass);
        }
    }

    public String getPluginCode()
    {
        return pluginCode;
    }

    public IResolverPlugin getPluginResolver()
    {
        try
        {
            IResolverPlugin plugin = (IResolverPlugin) pluginResolverClass.newInstance();
            plugin.initialize(pluginName, pluginCode);
            return plugin;
        } catch (InstantiationException | IllegalAccessException ex)
        {
            // this should be impossible as before creating this the check should have already been done.
            throw new IllegalStateException("Couldn't instantiate object of type " + pluginResolverClass);
        }
    }

}
