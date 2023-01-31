/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * Stores plugin tasks factories of one type (the one specified as a generic).
 * 
 * @author Tomasz Pylak
 */
public class PluginTaskProvider<P>
{
    private final TableMap<String, PluginTaskFactory<P>> factories;

    public PluginTaskProvider(List<PluginTaskFactory<P>> factories)
    {
        this.factories =
                new TableMap<String, PluginTaskFactory<P>>(factories,
                        new IKeyExtractor<String, PluginTaskFactory<P>>()
                            {
                                @Override
                                public String getKey(PluginTaskFactory<P> factory)
                                {
                                    return factory.getPluginDescription().getKey();
                                }
                            });
    }

    /**
     * returns an instance of the plugin with the given key
     */
    public P getPluginInstance(String pluginKey)
    {
        return getFactory(pluginKey).getPluginInstance();
    }

    public DatastoreServiceDescription getPluginDescription(String pluginKey)
    {
        return getFactory(pluginKey).getPluginDescription();
    }

    public List<DatastoreServiceDescription> getPluginDescriptions()
    {
        List<DatastoreServiceDescription> descriptions =
                new ArrayList<DatastoreServiceDescription>();
        for (PluginTaskFactory<?> factory : factories.values())
        {
            descriptions.add(factory.getPluginDescription());
        }
        return descriptions;
    }

    /** checks that all factories can produce plugins */
    public void check(boolean checkIfSerializable)
    {
        for (PluginTaskFactory<P> factory : factories.values())
        {
            factory.check(checkIfSerializable);
        }
    }

    /** Writes information about all plugin factory configurations to the log */
    public void logConfigurations()
    {
        for (PluginTaskFactory<P> factory : factories.values())
        {
            factory.logConfiguration();
        }
    }

    private PluginTaskFactory<P> getFactory(String pluginKey)
    {
        PluginTaskFactory<P> factory = factories.tryGet(pluginKey);
        if (factory == null)
        {
            throw new IllegalArgumentException("No plugin registered for key '" + pluginKey + "'.");
        }
        return factory;
    }
}
