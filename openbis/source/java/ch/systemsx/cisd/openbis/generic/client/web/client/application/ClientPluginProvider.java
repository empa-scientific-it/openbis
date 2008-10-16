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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory;

/**
 * The provider of {@link IClientPluginFactory} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginProvider
{
    private final static IClientPluginFactory GENERIC_PLUGIN_FACTORY = new ClientPluginFactory();

    private final static Set<IClientPluginFactory> plugins = new HashSet<IClientPluginFactory>();

    private ClientPluginProvider()
    {
        // Can not be instantiated.
    }

    static
    {
        // Automatically generated part - START
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory());
        // Automatically generated part - END
    }

    private final static void registerPluginFactory(final IClientPluginFactory pluginFactory)
    {
        plugins.add(pluginFactory);
    }

    /**
     * For given sample type code return corresponding {@link IClientPluginFactory}.
     * 
     * @return never <code>null</code> but could return the <i>generic</i> implementation.
     */
    public final static IClientPluginFactory getPluginFactory(final String sampleTypeCode)
    {
        assert sampleTypeCode != null : "Unspecified sample type code";
        for (final IClientPluginFactory pluginFactory : plugins)
        {
            if (pluginFactory.getSampleTypeCodes().contains(sampleTypeCode))
            {
                return pluginFactory;
            }
        }
        return GENERIC_PLUGIN_FACTORY;
    }
}
