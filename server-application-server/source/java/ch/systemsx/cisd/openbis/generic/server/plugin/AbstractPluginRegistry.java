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

package ch.systemsx.cisd.openbis.generic.server.plugin;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;

/**
 * Abstract super class for plug-in registration.
 * <p>
 * It implements {@link BeanFactoryAware} to set the field <code>genericServerPlugin</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Franz-Josef Elmer
 */
abstract class AbstractPluginRegistry<P extends IServerPlugin> implements BeanFactoryAware
{
    private final Logger operationLog;

    private final WildcardSupportingPluginMap<P> pluginMap = new WildcardSupportingPluginMap<P>();

    private P genericServerPlugin;

    protected AbstractPluginRegistry()
    {
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
    }

    /**
     * Registers specified plug-in.
     */
    public final synchronized void registerPlugin(final P plugin)
    {
        if (plugin instanceof IServerPluginWithWildcards)
        {
            registerPluginWithWildcards(plugin);
        } else
        {
            registerPluginWithoutWildcards(plugin);
        }
    }

    /**
     * Registers specified plug-in.
     */
    private final synchronized void registerPluginWithWildcards(final P plugin)
    {
        assert plugin != null : "Unspecified plugin.";
        for (EntityKind entityKind : EntityKind.values())
        {
            for (String entityTypeCode : ((IServerPluginWithWildcards) plugin)
                    .getOrderedEntityTypeCodes(entityKind))
            {
                EntityKindAndTypeCode key = new EntityKindAndTypeCode(entityKind, entityTypeCode);
                pluginMap.addMapping(key, plugin);
            }
        }
    }

    /**
     * Registers specified plug-in.
     */
    private final synchronized void registerPluginWithoutWildcards(final P plugin)
    {
        assert plugin != null : "Unspecified plugin.";
        for (EntityKind entityKind : EntityKind.values())
        {
            for (String entityTypeCode : plugin.getEntityTypeCodes(entityKind))
            {
                EntityKindAndTypeCode key = new EntityKindAndTypeCode(entityKind, entityTypeCode);
                final P previousPlugin = pluginMap.tryPlugin(key);
                if (previousPlugin != null)
                {
                    operationLog.error(String.format(
                            "There is already a plugin '%s' registered for "
                                    + "entity kind '%s' and entity type '%s.'", previousPlugin
                                    .getClass().getName(), entityKind, entityTypeCode));
                } else if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format("Plugin '%s' registered for "
                            + "entity kind '%s' and entity type '%s.'",
                            plugin.getClass().getName(), entityKind, entityTypeCode));
                }

                pluginMap.addMapping(key, plugin);
            }
        }
    }

    /**
     * Returns the appropriate plug-in for the specified entity kind and entity type.
     * 
     * @return never <code>null</code> but could return the <i>generic</i> implementation if none has been found.
     */
    public final synchronized P getPlugin(EntityKind entityKind, EntityTypePE entityType)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert entityType != null : "Unspecified entity type.";

        P serverPlugin =
                pluginMap.tryPlugin(new EntityKindAndTypeCode(entityKind, entityType.getCode()));
        return serverPlugin == null ? genericServerPlugin : serverPlugin;
    }

    //
    // BeanFactoryAware
    //

    @Override
    @SuppressWarnings("unchecked")
    public final void setBeanFactory(final BeanFactory beanFactory) throws BeansException
    {
        genericServerPlugin = (P) beanFactory.getBean(getBeanNameOfGenericPlugin());
    }

    /**
     * Returns the name of the Spring bean where the generic server plug-in is bound to.
     */
    protected abstract String getBeanNameOfGenericPlugin();

}
