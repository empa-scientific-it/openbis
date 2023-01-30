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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Script} &lt;---&gt; {@link ScriptPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class ScriptTranslator
{
    private ScriptTranslator()
    {
    }

    public final static List<Script> translate(final List<ScriptPE> scripts)
    {
        final List<Script> result = new ArrayList<Script>();
        for (final ScriptPE script : scripts)
        {
            result.add(ScriptTranslator.translate(script));
        }
        return result;
    }

    public static Script translate(final ScriptPE script)
    {
        if (script == null)
        {
            return null;
        }
        final Script result = new Script();
        result.setId(HibernateUtils.getId(script));
        result.setScriptType(script.getScriptType());
        result.setPluginType(script.getPluginType());
        result.setEntityKind(script.getEntityKind() == null ? null : new EntityKind[]
        { script.getEntityKind() });
        result.setName(script.getName());
        result.setDescription(script.getDescription());
        result.setScript(script.getScript());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setRegistrationDate(script.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(script.getRegistrator()));
        result.setModificationDate(script.getModificationDate());
        result.setAvailable(script.isAvailable());
        return result;
    }

    public static List<Script> enhancePredeployedPlugins(List<Script> scripts,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        for (Script script : scripts)
        {
            enhancePredeployedPlugin(script, entityValidationFactory,
                    dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
        }
        return scripts;
    }

    public static Script enhancePredeployedPlugin(Script script,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        if (script.getPluginType() == PluginType.PREDEPLOYED)
        {
            ICommonPropertyBasedHotDeployPlugin plugin = null;
            switch (script.getScriptType())
            {
                case ENTITY_VALIDATION:
                    plugin =
                            entityValidationFactory.tryGetPredeployedPluginByName(script.getName());
                    break;
                case DYNAMIC_PROPERTY:
                    plugin =
                            dynamicPropertyCalculatorFactory.tryGetPredeployedPluginByName(script
                                    .getName());
                    break;
                case MANAGED_PROPERTY:
                    plugin =
                            managedPropertyEvaluatorFactory.tryGetPredeployedPluginByName(script
                                    .getName());
            }

            if (plugin != null)
            {
                script.setEntityKind(translateEntityKinds(plugin.getSupportedEntityKinds()));
            }
        }
        return script;
    }

    public static EntityKind[] translateEntityKinds(
            EnumSet<ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind> entityKinds)
    {
        if (entityKinds == null)
        {
            return null;
        } else if (entityKinds.size() == ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind
                .values().length)
        {
            return null;
        }

        EntityKind[] kinds = new EntityKind[entityKinds.size()];
        int counter = 0;
        for (ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin.EntityKind kind : entityKinds)
        {
            kinds[counter++] = EntityKind.valueOf(kind.name());
        }

        return kinds;
    }
}
