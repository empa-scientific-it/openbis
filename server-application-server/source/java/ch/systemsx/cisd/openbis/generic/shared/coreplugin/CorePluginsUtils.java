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
import java.util.Properties;

import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;

/**
 * Helper methods for core plugins.
 * 
 * @author Franz-Josef Elmer
 */
public class CorePluginsUtils
{
    public static final String CORE_PLUGINS_FOLDER_KEY = "core-plugins-folder";

    static final String DEFAULT_CORE_PLUGINS_FOLDER = "../core-plugins";

    static final String DEFAULT_AS_CORE_PLUGINS_FOLDER = "../../core-plugins";

    public static final String CORE_PLUGINS_PROPERTIES_FILE = "core-plugins.properties";

    /**
     * Adds the content of <code>core-plugins.properties</code> file to the specified properties. The folder with the core plugins properties file is
     * specified by the property <code>core-plugins-folder</code> of the specified properties. If undefined a default value is used. Note, that the
     * core plugin properties might overwrite value in the specified properties object.
     */
    public static void addCorePluginsProperties(Properties properties, ScannerType scannerType)
    {
        String corePluginsFolder = getCorePluginsFolder(properties, scannerType);
        File file = new File(corePluginsFolder, CORE_PLUGINS_PROPERTIES_FILE);
        PropertyIOUtils.loadAndAppendProperties(properties, file);
    }

    /**
     * Returns the path to the core plugins folder from the specified properties.
     * 
     * @return scanner type specific default value if property core-plugins-folder is undefined.
     */
    public static String getCorePluginsFolder(Properties properties, ScannerType scannerType)
    {
        String defaultCorePluginsFolder =
                scannerType == ScannerType.DSS ? CorePluginsUtils.DEFAULT_CORE_PLUGINS_FOLDER
                        : CorePluginsUtils.DEFAULT_AS_CORE_PLUGINS_FOLDER;
        return properties.getProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY,
                defaultCorePluginsFolder);
    }

}
