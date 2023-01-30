/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class PluginTranslator
        extends AbstractCachingTranslator<Long, Plugin, PluginFetchOptions>
        implements IPluginTranslator
{
    @Autowired
    private IPluginBaseTranslator baseTranslator;

    @Autowired
    private IPluginRegistratorTranslator registratorTranslator;

    @Resource(name = "entity-validation-factory")
    private IEntityValidatorFactory entityValidationFactory;
    
    @Resource(name = "dynamic-property-calculator-factory")
    private IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;
    
    @Resource(name = "managed-property-evaluator-factory")
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Override
    protected Plugin createObject(TranslationContext context, Long pluginId, PluginFetchOptions fetchOptions)
    {
        Plugin plugin = new Plugin();
        plugin.setFetchOptions(fetchOptions);
        return plugin;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> ids, PluginFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IPluginBaseTranslator.class, baseTranslator.translate(context, ids, null));
        
        if (fetchOptions.hasRegistrator())
        {
            relations.put(IPluginRegistratorTranslator.class,
                    registratorTranslator.translate(context, ids, fetchOptions.withRegistrator()));
        }
        
        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long pluginId, Plugin plugin, Object objectRelations, PluginFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        PluginRecord baseRecord = relations.get(IPluginBaseTranslator.class, pluginId);
        
        plugin.setName(baseRecord.name);
        plugin.setPermId(new PluginPermId(baseRecord.name));
        plugin.setDescription(baseRecord.description);
        plugin.setRegistrationDate(baseRecord.registration_timestamp);
        plugin.setAvailable(baseRecord.is_available);
        if (baseRecord.entity_kind != null)
        {
            plugin.setEntityKinds(EnumSet.of(EntityKind.valueOf(baseRecord.entity_kind)));
        } else
        {
            plugin.setEntityKinds(EnumSet.allOf(EntityKind.class));
        }
        plugin.setPluginKind(PluginKind.valueOf(baseRecord.plugin_type));
        plugin.setPluginType(PluginType.valueOf(baseRecord.script_type));
        injectEntityKindsFromPredeployed(plugin);
        if (fetchOptions.hasScript())
        {
            plugin.setScript(baseRecord.script);
            plugin.getFetchOptions().withScriptUsing(fetchOptions.withScript());
        }
        
        if (fetchOptions.hasRegistrator())
        {
            plugin.setRegistrator(relations.get(IPluginRegistratorTranslator.class, pluginId));
            plugin.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

    }
    
    private void injectEntityKindsFromPredeployed(Plugin plugin)
    {
        if (plugin.getPluginKind() == PluginKind.PREDEPLOYED)
        {
            ICommonPropertyBasedHotDeployPlugin hotDeployPlugin = null;
            switch (plugin.getPluginType())
            {
                case ENTITY_VALIDATION:
                    hotDeployPlugin =
                            entityValidationFactory.tryGetPredeployedPluginByName(plugin.getName());
                    break;
                case DYNAMIC_PROPERTY:
                    hotDeployPlugin =
                            dynamicPropertyCalculatorFactory.tryGetPredeployedPluginByName(plugin
                                    .getName());
                    break;
                case MANAGED_PROPERTY:
                    hotDeployPlugin =
                            managedPropertyEvaluatorFactory.tryGetPredeployedPluginByName(plugin
                                    .getName());
            }

            if (hotDeployPlugin != null)
            {
                EnumSet<ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind> supportedEntityKinds =
                        hotDeployPlugin.getSupportedEntityKinds();
                plugin.setEntityKinds(translateEntityKinds(supportedEntityKinds));
            }
        }
    }

    private static Set<EntityKind> translateEntityKinds(
            EnumSet<ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind> entityKinds)
    {
        if (entityKinds == null)
        {
            return null;
        }

        List<EntityKind> kinds = new ArrayList<EntityKind>(entityKinds.size());
        for (ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind kind : entityKinds)
        {
            kinds.add(EntityKind.valueOf(kind.name()));
        }
        return EnumSet.copyOf(kinds);
    }

}
