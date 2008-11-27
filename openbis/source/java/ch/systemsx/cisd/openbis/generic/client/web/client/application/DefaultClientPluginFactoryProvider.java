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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory;

/**
 * The provider of {@link IClientPluginFactory} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultClientPluginFactoryProvider implements IClientPluginFactoryProvider
{
    private final Set<IClientPluginFactory> plugins = new HashSet<IClientPluginFactory>();

    private IClientPluginFactory genericPluginFactory;

    public DefaultClientPluginFactoryProvider(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        genericPluginFactory = new ClientPluginFactory(originalViewContext);
        registerPluginFactories(originalViewContext);
    }

    private final void registerPluginFactories(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        // Automatically generated part - START
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory(originalViewContext));
        // Automatically generated part - END
    }

    private final void registerPluginFactory(final IClientPluginFactory pluginFactory)
    {
        for (final IClientPluginFactory plugin : plugins)
        {
            final Set<String> set = new HashSet<String>(plugin.getSampleTypeCodes());
            set.retainAll(pluginFactory.getSampleTypeCodes());
            if (set.size() > 0)
            {
                throw new IllegalArgumentException("There is already a plugin factory ("
                        + plugin.getClass().getName() + ") registered for sample type code(s) '"
                        + set + "'.");
            }
        }
        plugins.add(pluginFactory);
    }

    //
    // IClientPluginFactoryProvider
    //

    public final IClientPluginFactory getClientPluginFactory(final String sampleTypeCode)
    {
        assert genericPluginFactory != null : "No plugin factories registered.";
        assert sampleTypeCode != null : "Unspecified sample type code";
        for (final IClientPluginFactory pluginFactory : plugins)
        {
            final Set<String> sampleTypeCodes = pluginFactory.getSampleTypeCodes();
            if (sampleTypeCodes.contains(sampleTypeCode))
            {
                return pluginFactory;
            }
        }
        return genericPluginFactory;
    }
}
