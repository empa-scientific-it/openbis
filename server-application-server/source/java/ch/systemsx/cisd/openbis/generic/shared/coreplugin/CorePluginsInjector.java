/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;

/**
 * Injector of DSS plugin.properties from core plugins folder into service.properties.
 * 
 * @author Franz-Josef Elmer
 */
public class CorePluginsInjector
{
    static final String DELETE_KEY_WORD = "__DELETE__";

    private static final String UNALLOWED_PLUGIN_NAME_CHARACTERS = " ,=";

    static final String DISABLED_CORE_PLUGINS_KEY = "disabled-core-plugins";

    static final String DISABLED_MARKER_FILE_NAME = "disabled";

    static final String INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME = "initialize-master-data";

    static final String PLUGIN_PROPERTIES_FILE_NAME = "plugin.properties";

    private static final ISimpleLogger DEFAULT_LOGGER = new Log4jSimpleLogger(LogFactory.getLogger(
            LogCategory.OPERATION, CorePluginsInjector.class));

    private final ISimpleLogger logger;

    private final Set<String> keysOfKeyLists;

    private final ScannerType scannerType;

    private final IPluginType[] pluginTypes;

    public CorePluginsInjector(ScannerType scannerType, IPluginType[] pluginTypes)
    {
        this(scannerType, pluginTypes, DEFAULT_LOGGER);
    }

    CorePluginsInjector(ScannerType scannerType, IPluginType[] pluginTypes, ISimpleLogger logger)
    {
        this.scannerType = scannerType;
        this.pluginTypes = pluginTypes;
        this.logger = logger;
        keysOfKeyLists = new HashSet<String>();
        for (IPluginType type : pluginTypes)
        {
            String keyOfKeyListPropertyOrNull = type.getKeyOfKeyListPropertyOrNull();
            if (keyOfKeyListPropertyOrNull != null)
            {
                keysOfKeyLists.add(keyOfKeyListPropertyOrNull);
            }
        }
    }

    public Map<String, File> injectCorePlugins(Properties properties)
    {
        String corePluginsFolderPath =
                CorePluginsUtils.getCorePluginsFolder(properties, scannerType);
        return injectCorePlugins(properties, corePluginsFolderPath);
    }

    public Map<String, File> injectCorePlugins(Properties properties,
            String corePluginsFolderPath)
    {
        ModuleEnabledChecker moduleEnabledChecker =
                new ModuleEnabledChecker(properties, Constants.ENABLED_MODULES_KEY);
        List<String> disabledPlugins = PropertyUtils.getList(properties, DISABLED_CORE_PLUGINS_KEY);
        PluginKeyBundles pluginKeyBundles = new PluginKeyBundles(properties, pluginTypes);
        Set<String> pluginNames = new HashSet<String>();
        pluginKeyBundles.addAndCheckUniquePluginNames(pluginNames);
        LinkedList<String> listOfDisabledMasterDataInitialization = new LinkedList<String>();

        Map<IPluginType, Map<String, NamedCorePluginFolder>> plugins =
                scanForCorePlugins(corePluginsFolderPath, moduleEnabledChecker, disabledPlugins,
                        pluginNames, listOfDisabledMasterDataInitialization);

        injectProperty(properties, Constants.DISABLED_MASTER_DATA_INITIALIZATION, 
                StringUtils.join(listOfDisabledMasterDataInitialization, ","));

        for (Entry<IPluginType, Map<String, NamedCorePluginFolder>> entry : plugins.entrySet())
        {
            IPluginType pluginType = entry.getKey();
            Map<String, NamedCorePluginFolder> map = entry.getValue();
            for (Entry<String, NamedCorePluginFolder> entry2 : map.entrySet())
            {
                NamedCorePluginFolder plugin = entry2.getValue();
                File definingFolder = plugin.getDefiningFolder();
                if (new File(definingFolder, DISABLED_MARKER_FILE_NAME).exists())
                {
                    continue;
                }
                String technology = plugin.getTechnology();
                Properties pluginProperties = plugin.getPluginProperties();
                if (pluginType.isUniquePluginNameRequired())
                {
                    String pluginKey =
                            pluginType.getPluginKey(technology, plugin.getName(), pluginProperties);
                    pluginKeyBundles.addPluginNameFor(pluginType, pluginKey);
                    String prefix = pluginType.getPrefix() + pluginKey + ".";
                    for (Entry<Object, Object> keyValuePair : pluginProperties.entrySet())
                    {
                        String value = keyValuePair.getValue().toString();
                        injectProperty(properties, prefix + keyValuePair.getKey(), value);
                    }
                } else
                {
                    PluginKeyBundles miscPluginKeyBundles =
                            new PluginKeyBundles(pluginProperties, pluginTypes);
                    for (Entry<Object, Object> keyValuePair : pluginProperties.entrySet())
                    {
                        String value = keyValuePair.getValue().toString();
                        String key = keyValuePair.getKey().toString();
                        if (keysOfKeyLists.contains(key) == false)
                        {
                            injectProperty(properties, key, value);
                        }
                    }
                    pluginKeyBundles.add(miscPluginKeyBundles);
                }
                logger.log(LogLevel.INFO, "Plugin " + plugin + " added.");
            }
        }
        pluginKeyBundles.addOrReplaceKeyBundleIn(properties);
        return createPluginFoldersMap(plugins);
    }

    private Map<String, File> createPluginFoldersMap(Map<IPluginType, Map<String, NamedCorePluginFolder>> plugins)
    {
        Map<String, File> pluginFolders = new HashMap<String, File>();
        for (Map<String, NamedCorePluginFolder> map : plugins.values())
        {
            for (String name : map.keySet())
            {
                pluginFolders.put(name, map.get(name).getDefiningFolder());
            }
        }
        return pluginFolders;
    }

    private void injectProperty(Properties properties, String key, String value)
    {
        String property = properties.getProperty(key);
        if (property != null && property.trim().equals(DELETE_KEY_WORD))
        {
            properties.remove(key);
        } else
        {
            properties.setProperty(key, value);
        }
    }

    private Map<IPluginType, Map<String, NamedCorePluginFolder>> scanForCorePlugins(
            String corePluginsFolderPath, ModuleEnabledChecker moduleEnabledChecker,
            List<String> disabledPlugins, Set<String> pluginNames, List<String> disabledMasterDataInitialization)
    {
        Map<IPluginType, Map<String, NamedCorePluginFolder>> typeToPluginsMap =
                new LinkedHashMap<IPluginType, Map<String, NamedCorePluginFolder>>();
        CorePluginScanner scanner = new CorePluginScanner(corePluginsFolderPath, scannerType, logger);
        List<CorePlugin> plugins = scanner.scanForPlugins();

        Set<String> enabledPlugins = moduleEnabledChecker.getEnabledPlugins(plugins);
        for (CorePlugin corePlugin : plugins)
        {
            String module = corePlugin.getName();
            // special treatment for initialize master data, as it is not a core plugin atm
            if (isDisabled(enabledPlugins, disabledPlugins, module + ":" + INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME))
            {
                disabledMasterDataInitialization.add(module);
            }

            for (IPluginType pluginType : pluginTypes)
            {
                File file =
                        new File(corePluginsFolderPath, CorePluginScanner.constructPath(corePlugin,
                                scannerType, pluginType));
                if (file.isDirectory())
                {
                    File[] pluginFolders = file.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
                                return name.startsWith(".") == false;
                            }
                        });
                    for (File pluginFolder : pluginFolders)
                    {
                        String pluginName = pluginFolder.getName();
                        NamedCorePluginFolder plugin =
                                new NamedCorePluginFolder(module, pluginType, pluginFolder);
                        String fullPluginName = plugin.getFullPluginName();
                        if (isDisabled(enabledPlugins, disabledPlugins, fullPluginName) == false)
                        {
                            String fullPluginKey =
                                    pluginType.getPrefix()
                                            + pluginType.getPluginKey(module, pluginName,
                                                    plugin.getPluginProperties());
                            assertAndAddPluginName(fullPluginKey, pluginNames, pluginType);
                            Map<String, NamedCorePluginFolder> map = typeToPluginsMap.get(pluginType);
                            if (map == null)
                            {
                                map = new LinkedHashMap<String, CorePluginsInjector.NamedCorePluginFolder>();
                                typeToPluginsMap.put(pluginType, map);
                            }
                            map.put(fullPluginKey, plugin);
                        }
                    }
                }
            }
        }
        return typeToPluginsMap;
    }
    
    private boolean isDisabled(Set<String> enabledPlugins, List<String> disabledPlugins, String fullPluginName)
    {
        for (String disabledPlugin : disabledPlugins)
        {
            if (fullPluginName.startsWith(disabledPlugin))
            {
                return true;
            }
        }
        for (String enabledPlugin : enabledPlugins)
        {
            if (fullPluginName.startsWith(enabledPlugin))
            {
                return false;
            }
        }
        return true;
    }

    private void assertAndAddPluginName(String pluginName, Set<String> pluginNames,
            IPluginType pluginType)
    {
        for (int i = 0; i < UNALLOWED_PLUGIN_NAME_CHARACTERS.length(); i++)
        {
            char c = UNALLOWED_PLUGIN_NAME_CHARACTERS.charAt(i);
            if (pluginName.contains(Character.toString(c)))
            {
                throw new EnvironmentFailureException("Plugin name contains '" + c + "': "
                        + pluginName);
            }
        }
        if (pluginType.isUniquePluginNameRequired())
        {
            if (pluginNames.contains(pluginName))
            {
                throw new ConfigurationFailureException("There is already a plugin named '"
                        + pluginName + "'.");
            }
            pluginNames.add(pluginName);
        }
    }

    private static final class PluginKeyBundles
    {
        private Map<IPluginType, KeyBundle> keyBundles =
                new LinkedHashMap<IPluginType, KeyBundle>();

        PluginKeyBundles(Properties properties, IPluginType[] pluginTypes)
        {
            for (IPluginType pluginType : pluginTypes)
            {
                String key = pluginType.getKeyOfKeyListPropertyOrNull();
                if (key != null)
                {
                    keyBundles.put(pluginType, new KeyBundle(properties, pluginType.getPrefix(),
                            key));
                }
            }
        }

        public void add(PluginKeyBundles bundles)
        {
            Set<Entry<IPluginType, KeyBundle>> entrySet = keyBundles.entrySet();
            for (Entry<IPluginType, KeyBundle> entry : entrySet)
            {
                IPluginType pluginType = entry.getKey();
                KeyBundle keyBundle = bundles.keyBundles.get(pluginType);
                if (keyBundle != null)
                {
                    entry.getValue().keys.addAll(keyBundle.keys);
                }
            }
        }

        void addAndCheckUniquePluginNames(Set<String> pluginNames)
        {
            for (KeyBundle keyBundle : keyBundles.values())
            {
                keyBundle.addAndCheckUniquePluginNames(pluginNames);
            }
        }

        void addPluginNameFor(IPluginType pluginType, String pluginKey)
        {
            KeyBundle keyBundle = keyBundles.get(pluginType);
            if (keyBundle != null)
            {
                keyBundle.addKey(pluginKey);
            }
        }

        void addOrReplaceKeyBundleIn(Properties properties)
        {
            for (KeyBundle bundle : keyBundles.values())
            {
                bundle.addOrReplaceKeyBundleIn(properties);
            }
        }
    }

    private static final class KeyBundle
    {
        private final String key;

        private final Set<String> keys;

        private final String prefix;

        KeyBundle(Properties properties, String prefix, String key)
        {
            this.prefix = prefix;
            this.key = key;
            keys = new TreeSet<String>();
            String keysAsString = properties.getProperty(key);
            if (keysAsString != null)
            {
                String[] keyArray = PropertyParametersUtil.parseItemisedProperty(keysAsString, key);
                keys.addAll(Arrays.asList(keyArray));
            }
        }

        public void addKey(String newKey)
        {
            keys.add(newKey);
        }

        void addAndCheckUniquePluginNames(Set<String> pluginNames)
        {
            for (String keyPrefix : keys)
            {
                String fullPrefix = prefix + keyPrefix;
                if (pluginNames.contains(fullPrefix))
                {
                    throw new ConfigurationFailureException("Property key '" + fullPrefix
                            + "' for key list '" + key
                            + "' is already defined in some other key list.");
                }
                pluginNames.add(fullPrefix);
            }
        }

        void addOrReplaceKeyBundleIn(Properties properties)
        {
            if (keys.isEmpty() == false)
            {
                CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                for (String k : keys)
                {
                    builder.append(k);
                }
                properties.setProperty(key, builder.toString());
            }
        }
    }

    private static final class NamedCorePluginFolder
    {
        private final String fullPluginName;

        private final File definingFolder;

        private final String technology;

        private final String name;

        private Properties pluginProperties;

        NamedCorePluginFolder(String technology, IPluginType pluginType, File definingFolder)
        {
            this.technology = technology;
            name = definingFolder.getName();
            fullPluginName = technology + ":" + pluginType.getSubFolderName() + ":" + name;
            this.definingFolder = definingFolder;
            if (definingFolder.isDirectory() == false)
            {
                throw new EnvironmentFailureException("Is not a directory: " + definingFolder);
            }
            pluginProperties = getPluginProperties(definingFolder);
        }

        String getTechnology()
        {
            return technology;
        }

        String getName()
        {
            return name;
        }

        String getFullPluginName()
        {
            return fullPluginName;
        }

        File getDefiningFolder()
        {
            return definingFolder;
        }

        Properties getPluginProperties()
        {
            return pluginProperties;
        }

        @Override
        public String toString()
        {
            return fullPluginName + " [" + definingFolder + "]";
        }

        /**
         * Load plugin properties file where all references to script names are replaced by script paths.
         */
        private Properties getPluginProperties(File folder)
        {
            File pluginPropertiesFile = new File(folder, PLUGIN_PROPERTIES_FILE_NAME);
            if (pluginPropertiesFile.exists() == false)
            {
                throw new EnvironmentFailureException("Missing plugin properties: "
                        + pluginPropertiesFile);
            }
            File[] scripts = folder.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String fileName)
                    {
                        return fileName.equals(PLUGIN_PROPERTIES_FILE_NAME) == false;
                    }
                });
            Properties properties = loadProperties(pluginPropertiesFile);
            for (Entry<Object, Object> keyValuePair : properties.entrySet())
            {
                String value = keyValuePair.getValue().toString();
                for (File script : scripts)
                {
                    value = value.replace(script.getName(), script.getPath());
                }
                keyValuePair.setValue(value);
            }
            return properties;
        }

        private Properties loadProperties(File pluginPropertiesFile)
        {
            Properties properties = new Properties();
            FileInputStream inStream = null;
            try
            {
                inStream = new FileInputStream(pluginPropertiesFile);
                properties.load(inStream);
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("Couldn't load plugin properties '"
                        + pluginPropertiesFile + "'.", ex);
            } finally
            {
                IOUtils.closeQuietly(inStream);
            }
            return properties;
        }

    }
}
