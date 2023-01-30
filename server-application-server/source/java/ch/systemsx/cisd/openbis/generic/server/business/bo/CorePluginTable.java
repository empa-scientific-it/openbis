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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.AsCorePluginPaths;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.IMasterDataScriptRegistrationRunner;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.ICorePluginResourceLoader;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.CorePluginTranslator;

/**
 * @author Kaloyan Enimanev
 */
public final class CorePluginTable extends AbstractBusinessObject implements ICorePluginTable
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final IMasterDataScriptRegistrationRunner masterDataScriptRunner;

    public CorePluginTable(IDAOFactory daoFactory, Session session,
            IMasterDataScriptRegistrationRunner masterDataScriptRunner,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.masterDataScriptRunner = masterDataScriptRunner;
    }

    @Override
    public List<CorePlugin> listCorePluginsByName(String name)
    {
        List<CorePluginPE> pluginPEs = getCorePluginDAO().listCorePluginsByName(name);
        return CorePluginTranslator.translate(pluginPEs);
    }

    @Override
    public void registerPlugin(CorePlugin plugin, ICorePluginResourceLoader resourceLoader)
    {
        assert plugin != null : "Unspecified plugin.";

        if (isNewVersionDetected(plugin))
        {
            installNewPluginVersion(plugin, resourceLoader);
        } else
        {
            operationLog.info("Deployed core plugin detected :" + plugin);
        }

    }

    private boolean isNewVersionDetected(CorePlugin plugin)
    {
        List<CorePluginPE> installedVersions =
                getCorePluginDAO().listCorePluginsByName(plugin.getName());
        if (installedVersions.isEmpty())
        {
            return true;
        }
        CorePluginPE latestVersionInstalled = Collections.max(installedVersions);
        return latestVersionInstalled.getVersion() < plugin.getVersion();
    }

    private void installNewPluginVersion(CorePlugin plugin, ICorePluginResourceLoader resourceLoader)
    {
        String masterDataScript =
                resourceLoader.tryLoadToString(plugin, AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
        String masterDataScriptPath = resourceLoader.getPath(plugin, AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);

        if (false == StringUtils.isEmpty(masterDataScript))
        {
            String[] jythonPath = JythonUtils.getScriptDirectoryPythonPath(masterDataScriptPath);
            runInitializeMasterDataScript(plugin, masterDataScript, jythonPath);
            CorePluginPE pluginPE = CorePluginTranslator.translate(plugin, masterDataScript);
            getCorePluginDAO().createCorePlugins(Collections.singletonList(pluginPE));
            operationLog.info(plugin + " installed succesfully.");
        } else
        {
            operationLog.info(String.format("No '%s' script found for '%s'. Skipping..",
                    AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT, plugin));
        }
    }

    private void runInitializeMasterDataScript(CorePlugin plugin, String masterDataScript, String[] jythonPath)
    {
        operationLog.info("Executing master data initialization script for plugin '" + plugin
                + "'...");
        try
        {
            masterDataScriptRunner.executeScript(masterDataScript, jythonPath);
        } catch (MasterDataRegistrationException mdre)
        {
            Log4jSimpleLogger errorLogger = new Log4jSimpleLogger(operationLog);
            errorLogger.log(LogLevel.ERROR, String.format("Failed to commit all transactions in "
                    + "the master data registration script for plugin '%s'.", plugin));
            mdre.logErrors(errorLogger);
            throw ConfigurationFailureException.fromTemplate(
                    "Failed to run iniitalization script '%s'", plugin);
        }
    }

}
