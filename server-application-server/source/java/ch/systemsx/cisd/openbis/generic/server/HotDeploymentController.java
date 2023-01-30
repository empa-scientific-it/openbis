/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.cisd.hotdeploy.PluginContainer;
import ch.ethz.cisd.hotdeploy.PluginDescriptor;
import ch.ethz.cisd.hotdeploy.PluginEvent;
import ch.ethz.cisd.hotdeploy.PluginMapHolder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.ScriptTranslator;

/**
 * @author Pawel Glyzewski
 */
public class HotDeploymentController implements IHotDeploymentController
{
    public static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HotDeploymentController.class);

    private final PluginContainer pluginContainer;

    private ICommonServerForInternalUse commonServer;

    public HotDeploymentController(ICommonServerForInternalUse commonServer,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.commonServer = commonServer;
        PluginContainer.initHotDeployment();
        pluginContainer = PluginContainer.tryGetInstance();

        entityValidationFactory.initializeHotDeployment(this);
        dynamicPropertyCalculatorFactory.initializeHotDeployment(this);
        managedPropertyEvaluatorFactory.initializeHotDeployment(this);
    }

    @Override
    public void addPluginDirectory(File pluginDirectory)
    {
        pluginContainer.addPluginDirectory(pluginDirectory);
    }

    @Override
    public <T extends ICommonPropertyBasedHotDeployPlugin> PluginMapHolder<T> getPluginMap(
            Class<T> pluginClass)
    {
        return new PluginMapHolder<T>(pluginContainer, pluginClass);
    }

    @Override
    @Transactional
    public void pluginChanged(PluginEvent event, ICommonPropertyBasedHotDeployPlugin plugin,
            ScriptType scriptType)
    {
        String pluginName = event.getPluginName();
        SessionContextDTO sessionCtx = commonServer.tryToAuthenticateAsSystem();

        if (sessionCtx != null)
        {
            try
            {
                switch (event.getEventType())
                {
                    case REGISTER_NEW_PLUGIN:
                    case UPDATE_PLUGIN:
                        Script script = new Script();
                        script.setName(pluginName);
                        script.setDescription(plugin.getDescription());
                        script.setScript(null);
                        script.setPluginType(PluginType.PREDEPLOYED);
                        script.setScriptType(scriptType);
                        script.setEntityKind(ScriptTranslator.translateEntityKinds(plugin
                                .getSupportedEntityKinds()));
                        script.setAvailable(true);

                        commonServer.registerOrUpdatePredeployedPlugin(
                                sessionCtx.getSessionToken(), script);
                        break;
                    case UNREGISTER_PLUGIN:
                        commonServer.invalidatePredeployedPlugin(sessionCtx.getSessionToken(),
                                pluginName, scriptType);
                        break;
                }
            } finally
            {
                commonServer.logout(sessionCtx.getSessionToken());
            }
        }
    }

    @Override
    public void disablePlugin(String pluginName)
    {
        PluginDescriptor pluginDescription = tryGetPlugin(pluginName);
        if (pluginDescription == null)
        {
            operationLog.warn("Couldn't disable unknown plugin: " + pluginName);
        } else
        {
            boolean success = pluginContainer.disablePlugin(pluginDescription.getPluginClass());
            if (success)
            {
                operationLog.info("Plugin '" + pluginName + "' successfully disabled.");
            } else
            {
                operationLog.warn("Disabling plugin '" + pluginName + "' failed.");
            }
        }
    }

    private PluginDescriptor tryGetPlugin(String pluginName)
    {
        List<PluginDescriptor> plugins = pluginContainer.getPlugins();
        for (PluginDescriptor pluginDescriptor : plugins)
        {
            if (pluginDescriptor.getPluginName().equals(pluginName))
            {
                return pluginDescriptor;
            }
        }
        return null;
    }

    protected void setCommonServer(ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

}