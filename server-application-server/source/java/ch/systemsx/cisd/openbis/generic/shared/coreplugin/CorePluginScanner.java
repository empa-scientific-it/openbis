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

package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;

/**
 * The {@link CorePluginScanner} contains no special logic. Its sole function is to understand the plugins folder hierarchy and to load plugins from
 * the file system.
 * 
 * @author Kaloyan Enimanev
 */
public class CorePluginScanner implements ICorePluginResourceLoader
{
    static final String CORE_PLUGIN_PROPERTIES_FILE_NAME = "core-plugin.properties";

    static final String REQUIRED_PLUGINS_KEY = "required-plugins";
    
    /**
     * the type of plugins we are scanning for.
     */
    public enum ScannerType
    {
        AS("as"), DSS("dss");

        private final String subFolderName;

        private ScannerType(String subFolderName)
        {
            this.subFolderName = subFolderName;
        }

        public String getSubFolderName()
        {
            return subFolderName;
        }
    }

    public static String constructPath(CorePlugin corePlugin, ScannerType scannerType,
            IPluginType pluginType)
    {
        return corePlugin.getName() + "/" + corePlugin.getVersion() + "/"
                + scannerType.getSubFolderName() + "/" + pluginType.getSubFolderName();
    }

    private static final ISimpleLogger DEFAULT_LOGGER = new Log4jSimpleLogger(LogFactory.getLogger(
            LogCategory.OPERATION, CorePluginScanner.class));

    private final ISimpleLogger log;

    private final File pluginsFolder;

    private final ScannerType scannerType;

    public CorePluginScanner(String pluginsFolderName, ScannerType scannerType)
    {
        this(pluginsFolderName, scannerType, DEFAULT_LOGGER);
    }

    public CorePluginScanner(String pluginsFolderName, ScannerType scannerType, ISimpleLogger logger)
    {
        this.pluginsFolder = new File(pluginsFolderName);
        this.scannerType = scannerType;
        this.log = logger;
        if (pluginsFolder.exists())
        {
            if (false == pluginsFolder.isDirectory())
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Core-plugins folder '%s' is a file.", pluginsFolderName);
            }
        } else
        {
            log.log(LogLevel.WARN, "Core plugins folder '" + pluginsFolderName
                    + "' does not exists.");
        }
    }

    @Override
    public String tryLoadToString(CorePlugin plugin, String path)
    {
        File result = new File(getFolderForPlugin(plugin), path);
        if (result.isFile())
        {
            return FileUtilities.loadToString(result);
        } else
        {
            return null;
        }
    }

    @Override
    public String getPath(CorePlugin plugin, String path)
    {
        File result = new File(getFolderForPlugin(plugin), path);
        return result.getAbsolutePath();
    }

    public List<CorePlugin> scanForPlugins()
    {
        List<CorePlugin> result = new ArrayList<CorePlugin>();
        if (pluginsFolder.isDirectory())
        {
            List<File> pluginDirectories = listFiles(pluginsFolder);
            Collections.sort(pluginDirectories);
            for (File pluginDir : pluginDirectories)
            {
                CorePlugin plugin = tryLoadLatestVersion(pluginDir);
                if (plugin != null)
                {
                    result.add(plugin);
                }
            }
        }
        return result;
    }

    private CorePlugin tryLoadLatestVersion(File pluginRootDir)
    {
        List<CorePlugin> allVersionsForPlugin = new ArrayList<CorePlugin>();
        for (File versionDir : listFiles(pluginRootDir))
        {
            if (isNumber(versionDir) == false)
            {
                continue;
            }
            if (isValidVersionDir(versionDir))
            {
                CorePlugin pluginVersion = createPlugin(pluginRootDir, versionDir);
                File folder = getFolderForPlugin(pluginVersion);
                if (folder.exists())
                {
                    allVersionsForPlugin.add(pluginVersion);
                }
            } else
            {
                log.log(LogLevel.WARN, String.format("Invalid version '%s' for plugin '%s'. "
                        + "Plugin version must be non-negative integer numbers.",
                        versionDir.getName(), pluginRootDir.getAbsolutePath()));
            }
        }

        if (allVersionsForPlugin.isEmpty())
        {
            log.log(LogLevel.WARN, String.format(
                    "No valid versions have been detected for plugin '%s'.", pluginRootDir));
            return null;
        } else
        {
            return Collections.max(allVersionsForPlugin);
        }
    }

    private List<File> listFiles(File folder)
    {
        List<File> files = FileUtilities.listDirectories(folder, false);
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();)
        {
            File file = iterator.next();
            if (file.getName().startsWith("."))
            {
                iterator.remove();
            }
        }
        return files;
    }

    private CorePlugin createPlugin(File pluginDir, File versionDir)
    {
        String name = pluginDir.getName();
        int version = parseVersion(versionDir);
        CorePlugin corePlugin = new CorePlugin(name, version);
        File corePluginPropertiesFile = new File(versionDir, CORE_PLUGIN_PROPERTIES_FILE_NAME);
        if (corePluginPropertiesFile.isFile())
        {
            Properties corePluginProperties = PropertyIOUtils.loadProperties(corePluginPropertiesFile);
            for (String requiredPlugin : corePluginProperties.getProperty(REQUIRED_PLUGINS_KEY, "").split(","))
            {
                corePlugin.addRequiredPlugin(requiredPlugin.trim());
            }
        }

        return corePlugin;
    }

    /**
     * only integer numbers are accepted for plugins.
     */
    private boolean isValidVersionDir(File versionDir)
    {
        return parseVersion(versionDir) >= 0;
    }

    private int parseVersion(File versionDir)
    {
        try
        {
            return Integer.parseInt(versionDir.getName());
        } catch (NumberFormatException nfe)
        {
            return -1;
        }
    }
    
    private boolean isNumber(File versionDir)
    {
        try
        {
            Integer.parseInt(versionDir.getName());
            return true;
        } catch (NumberFormatException nfe)
        {
            return false;
        }
    }

    private File getFolderForPlugin(CorePlugin plugin)
    {
        File unversionedFolder = new File(pluginsFolder, plugin.getName());
        String version = String.valueOf(plugin.getVersion());
        File versionFolder = new File(unversionedFolder, version);
        return new File(versionFolder, scannerType.getSubFolderName());
    }
}
