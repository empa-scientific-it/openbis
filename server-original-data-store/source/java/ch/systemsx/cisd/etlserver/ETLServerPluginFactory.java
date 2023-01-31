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

package ch.systemsx.cisd.etlserver;

import java.util.HashMap;

/**
 * A factory for creating IETLServerPlugin objects from ThreadParameters objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ETLServerPluginFactory
{
    private static final HashMap<ThreadParameters, IETLServerPlugin> parametersToPluginsMap =
            new HashMap<ThreadParameters, IETLServerPlugin>();

    public static synchronized IETLServerPlugin getPluginForThread(ThreadParameters threadParameters)
    {

        IETLServerPlugin plugin = parametersToPluginsMap.get(threadParameters);
        if (null == plugin)
        {
            plugin = new PropertiesBasedETLServerPlugin(threadParameters.getThreadProperties());
            parametersToPluginsMap.put(threadParameters, plugin);
        }
        return plugin;
    }
}
