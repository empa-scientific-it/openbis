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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory;

/**
 * The provider of {@link IClientPluginFactory} implementations.
 * 
 * @author Christian Ribeaud
 */
public class DefaultClientPluginFactoryProvider implements IClientPluginFactoryProvider
{
    private final Set<IClientPluginFactory> pluginFactories = new HashSet<IClientPluginFactory>();

    private final WildcardSupportingPluginFactoryMap pluginFactoryMap =
            new WildcardSupportingPluginFactoryMap();

    private final IClientPluginFactory genericPluginFactory;

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
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.ClientPluginFactory(originalViewContext));
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.ClientPluginFactory(originalViewContext));
        // Automatically generated part - END
    }

    protected final void registerPluginFactory(final IClientPluginFactory pluginFactory)
    {
        assert pluginFactory != null : "Unspecified client plugin factory.";
        pluginFactories.add(pluginFactory);
        for (final EntityKind entityKind : EntityKind.values())
        {
            for (final String entityType : pluginFactory.getEntityTypeCodes(entityKind))
            {
                final EntityKindAndTypeCode key = new EntityKindAndTypeCode(entityKind, entityType);
                final IClientPluginFactory previousValue = pluginFactoryMap.tryPluginFactory(key);
                if (previousValue != null)
                {
                    throw new IllegalArgumentException("There is already a client plugin factory '"
                            + previousValue.getClass().getName() + "' registered for key '" + key
                            + "'.");

                }
                pluginFactoryMap.addMapping(key, pluginFactory);
            }
        }
    }

    protected final void registerPluginFactory(
            final IClientPluginFactoryUsingWildcards pluginFactory)
    {
        assert pluginFactory != null : "Unspecified client plugin factory.";
        pluginFactories.add(pluginFactory);
        for (final EntityKind entityKind : EntityKind.values())
        {
            for (final String entityType : pluginFactory.getOrderedEntityTypeCodes(entityKind))
            {
                final EntityKindAndTypeCode key = new EntityKindAndTypeCode(entityKind, entityType);
                pluginFactoryMap.addMapping(key, pluginFactory);
            }
        }
    }

    //
    // IClientPluginFactoryProvider
    //
    @Override
    public final IClientPluginFactory getClientPluginFactory(final EntityKind entityKind,
            final BasicEntityType entityType)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert entityType != null : "Unspecified entity type.";

        final IClientPluginFactory pluginFactory =
                pluginFactoryMap
                        .tryPluginFactory(new EntityKindAndTypeCode(entityKind, entityType));
        if (pluginFactory != null && pluginFactory.isEnabled())
        {
            return pluginFactory;
        }
        return genericPluginFactory;
    }

    private final List<IModule> getUninitializedModules()
    {
        ArrayList<IModule> modules = new ArrayList<IModule>();
        for (IClientPluginFactory factory : pluginFactories)
        {
            if (factory.isEnabled())
            {
                IModule m = factory.tryGetModule();
                if (m != null)
                {
                    modules.add(m);
                }
            }
        }
        return modules;
    }

    @Override
    public void registerModuleInitializationObserver(IModuleInitializationObserver observer)
    {
        ModuleInitializationController.createAndInitialize(getUninitializedModules()).addObserver(
                observer);
    }
}
