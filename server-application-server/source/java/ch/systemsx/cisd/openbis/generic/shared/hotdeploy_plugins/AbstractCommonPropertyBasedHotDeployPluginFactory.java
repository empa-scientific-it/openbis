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

package ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.cisd.hotdeploy.PluginEvent;
import ch.ethz.cisd.hotdeploy.PluginEventListener;
import ch.ethz.cisd.hotdeploy.PluginMapHolder;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.IHotDeploymentController;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractCommonPropertyBasedHotDeployPluginFactory<T extends ICommonPropertyBasedHotDeployPlugin>
        implements ICommonPropertyBasedHotDeployPluginFactory<T>
{
    private final static String DEFAULT_PLUGINS_LOCATION = "../../entity-related-plugins/";

    private PluginMapHolder<T> predeployedPlugins;

    private final String pluginDirectoryPath;

    private IHotDeploymentController hotDeploymentController;

    public AbstractCommonPropertyBasedHotDeployPluginFactory(String pluginDirectoryPath)
    {
        if (pluginDirectoryPath != null && pluginDirectoryPath.startsWith("${")
                && pluginDirectoryPath.endsWith("}"))
        {
            this.pluginDirectoryPath = DEFAULT_PLUGINS_LOCATION + getDefaultPluginSubDirName();
        } else
        {
            this.pluginDirectoryPath = pluginDirectoryPath;
        }
    }

    @Override
    public List<String> listPredeployedPlugins()
    {
        if (predeployedPlugins == null)
        {
            return Collections.emptyList();
        }

        return new ArrayList<String>(predeployedPlugins.getPluginNames());
    }

    @Override
    public T tryGetPredeployedPluginByName(String name)
    {
        if (predeployedPlugins == null)
        {
            throw new UserFailureException("Predeployed " + getPluginDescription()
                    + " plugins are not configured properly.");
        }
        return predeployedPlugins.tryGet(name);
    }

    @Override
    public void initializeHotDeployment(final IHotDeploymentController controller)
    {
        hotDeploymentController = controller;
        if (false == StringUtils.isBlank(pluginDirectoryPath))
        {
            this.predeployedPlugins = hotDeploymentController.getPluginMap(getPluginClass());

            predeployedPlugins.addListener(new PluginEventListener()
                {
                    @Override
                    public void pluginChanged(PluginEvent event)
                    {
                        hotDeploymentController.pluginChanged(event,
                                predeployedPlugins.tryGet(event.getPluginName()), getScriptType());
                    }
                });
            hotDeploymentController.addPluginDirectory(new File(pluginDirectoryPath));
        } else
        {
            this.predeployedPlugins = null;
        }
    }

    @Override
    public IHotDeploymentController getHotDeploymentController()
    {
        return hotDeploymentController;
    }

    protected abstract String getPluginDescription();

    protected abstract String getDefaultPluginSubDirName();

    protected abstract Class<T> getPluginClass();

    protected abstract ScriptType getScriptType();
}
