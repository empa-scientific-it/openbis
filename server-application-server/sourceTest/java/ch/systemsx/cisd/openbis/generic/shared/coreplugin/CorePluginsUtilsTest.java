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

import static ch.systemsx.cisd.common.test.RegExConstants.END_QUOTING;
import static ch.systemsx.cisd.common.test.RegExConstants.START_QUOTING;
import static ch.systemsx.cisd.common.test.RegExConstants.WILDCARD;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;

/**
 * @author Franz-Josef Elmer
 */
public class CorePluginsUtilsTest extends AbstractFileSystemTestCase
{
    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = START_QUOTING
            + CorePluginsUtils.DEFAULT_AS_CORE_PLUGINS_FOLDER
            + "/"
            + CorePluginsUtils.CORE_PLUGINS_PROPERTIES_FILE + END_QUOTING + WILDCARD)
    public void testAddCorePluginsPropertiesForASFromNotExistingDefaultCorePluginsFolder()
    {
        CorePluginsUtils.addCorePluginsProperties(new Properties(), ScannerType.AS);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = START_QUOTING
            + CorePluginsUtils.DEFAULT_CORE_PLUGINS_FOLDER
            + "/"
            + CorePluginsUtils.CORE_PLUGINS_PROPERTIES_FILE + END_QUOTING + WILDCARD)
    public void testAddCorePluginsPropertiesForDSSFromNotExistingDefaultCorePluginsFolder()
    {
        CorePluginsUtils.addCorePluginsProperties(new Properties(), ScannerType.DSS);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = (WILDCARD
            + START_QUOTING + CorePluginsUtils.CORE_PLUGINS_PROPERTIES_FILE + END_QUOTING + WILDCARD))
    public void testAddCorePluginsPropertiesButMissingCorePluginsPropertiesFile()
    {
        File corePluginsFolder = new File(workingDirectory, "core-plugins");
        corePluginsFolder.mkdirs();
        Properties properties = new Properties();
        properties.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY,
                corePluginsFolder.getPath());

        CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.DSS);
    }

    @Test
    public void testAddCorePluginsProperties()
    {
        File corePluginsFolder = new File(workingDirectory, "core-plugins");
        corePluginsFolder.mkdirs();
        String enabledTechnology = "tech-" + (int) (100 * Math.random());
        FileUtilities.writeToFile(new File(corePluginsFolder,
                CorePluginsUtils.CORE_PLUGINS_PROPERTIES_FILE), "  " + Constants.ENABLED_MODULES_KEY + " =   "
                + enabledTechnology + "   ");

        Properties properties = new Properties();
        properties.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY,
                corePluginsFolder.getPath());

        CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.DSS);

        assertEquals(enabledTechnology, properties.getProperty(Constants.ENABLED_MODULES_KEY));
    }
}
