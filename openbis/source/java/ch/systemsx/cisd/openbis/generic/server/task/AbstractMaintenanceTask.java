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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractMaintenanceTask implements IMaintenanceTask
{
    static final String CONFIGURATION_FILE_PATH_PROPERTY = "configuration-file-path";

    static final String DEFAULT_CONFIGURATION_FILE_PATH = "etc/user-management-maintenance-config.json";

    private final boolean configMandatory;

    protected final Logger operationLog;

    protected final Logger notificationLog;

    protected File configurationFile;

    protected MaintenanceTaskParameters parameters;

    AbstractMaintenanceTask(boolean configMandatory)
    {
        this.configMandatory = configMandatory;
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Setup plugin " + pluginName);
        configurationFile = new File(properties.getProperty(CONFIGURATION_FILE_PATH_PROPERTY, DEFAULT_CONFIGURATION_FILE_PATH));
        if (configMandatory && configurationFile.isFile() == false)
        {
            throw new ConfigurationFailureException("Configuration file '" + configurationFile.getAbsolutePath()
                    + "' doesn't exist or is a directory.");
        }
        parameters = new MaintenanceTaskParameters(properties, pluginName);
        setUpSpecific(properties);
        operationLog.info("Plugin '" + pluginName + "' initialized."
                + (configurationFile.isFile() ? " Configuration file: " + configurationFile.getAbsolutePath() : ""));
    }

    protected abstract void setUpSpecific(Properties properties);

    protected UserManagerConfig readGroupDefinitions(IChangedHandler handlerOrNull)
    {
        if (configurationFile.isFile() == false)
        {
            if (configMandatory)
            {
                operationLog.error("Configuration file '" + configurationFile.getAbsolutePath() + "' doesn't exist or is a directory.");
            }
            return null;
        }
        String serializedConfig = FileUtilities.loadToString(configurationFile);
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            UserManagerConfig config = mapper.readValue(serializedConfig, UserManagerConfig.class);
            boolean hasChanged = hasChanged(serializedConfig);
            if (hasChanged && handlerOrNull != null)
            {
                handlerOrNull.changed(serializedConfig, new Date(configurationFile.lastModified()));
            }
            return config;
        } catch (Exception e)
        {
            operationLog.error("Invalid content of configuration file '" + configurationFile.getAbsolutePath() + "': " + e, e);
            return null;
        }
    }

    private boolean hasChanged(String serializedConfig)
    {
        String hash = StringUtilities.computeMD5Hash(configurationFile.lastModified() + serializedConfig);
        File hashFile = new File(configurationFile.getParentFile(), configurationFile.getName() + ".hash");
        String previousHash = null;
        if (hashFile.isFile())
        {
            previousHash = FileUtilities.loadExactToString(hashFile);
        }
        FileUtilities.writeToFile(hashFile, hash);
        return hash.equals(previousHash) == false;
    }

}
