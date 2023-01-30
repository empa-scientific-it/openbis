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

package ch.systemsx.cisd.openbis.generic.server.coreplugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.ModuleEnabledChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginRegistrator implements InitializingBean
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private ICommonServerForInternalUse commonServer;

    private String pluginsFolderName;

    private ModuleEnabledChecker moduleEnabledChecker;

    private ModuleEnabledChecker disabledMasterDataInitializationChecker;

    /**
     * Loads and installs the deployed core plugins. Invoked from the Spring container after the object is initialized.
     */
    public void registerPlugins()
    {
        if ("${core-plugins-folder}".equals(pluginsFolderName)
                || pluginsFolderName.contains("webapps/openbis/core-plugins"))
        {
            pluginsFolderName = "../../core-plugins";
        }
        CorePluginScanner pluginScanner = new CorePluginScanner(pluginsFolderName, ScannerType.AS);
        String sessionToken = getSessionToken();
        List<CorePlugin> plugins = pluginScanner.scanForPlugins();
        for (CorePlugin plugin : moduleEnabledChecker.getModuleWithEnabledMasterDataInitializations(plugins))
        {
            if (disabledMasterDataInitializationChecker.isModuleEnabled(plugin.getName()))
            {
                operationLog.info("Registering of master data for plugin " + plugin + " is disabled");
            } else
            {
                try
                {
                    commonServer.registerPlugin(sessionToken, plugin, pluginScanner);
                } catch (Exception ex)
                {
                    operationLog.error("Failed to install core plugin: " + plugin, ex);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        registerPlugins();
    }

    public void setPluginsFolderName(String pluginsFolderName)
    {
        this.pluginsFolderName = pluginsFolderName;
    }

    public void setEnabledTechnologies(String listOfEnabledTechnologies)
    {
        moduleEnabledChecker =
                new ModuleEnabledChecker(new ArrayList<String>(
                        ServerUtils.extractSet(listOfEnabledTechnologies)));
    }

    public void setDisabledMasterDataInitialization(String listOfDisabledMasterDataInitialization)
    {
        disabledMasterDataInitializationChecker =
                new ModuleEnabledChecker(new ArrayList<String>(
                        ServerUtils.extractSet(listOfDisabledMasterDataInitialization)));
    }

    public void setCommonServer(ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    private String getSessionToken()
    {
        SessionContextDTO sessionDTO = commonServer.tryToAuthenticateAsSystem();
        final String sessionToken = sessionDTO.getSessionToken();
        return sessionToken;
    }
}
