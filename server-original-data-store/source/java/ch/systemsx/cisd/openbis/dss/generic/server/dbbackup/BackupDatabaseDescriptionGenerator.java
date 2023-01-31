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

package ch.systemsx.cisd.openbis.dss.generic.server.dbbackup;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPluginType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Given a list of properties files generates a list of well-known databases to be backed up as part of the openBIS upgrade process.
 * 
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionGenerator
{
    public static final String AS_DB_KEY_PREFIX = "database.";

    public static final String HCS_IMAGING_DB_VERSION_HOLDER =
            "ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseVersionHolder";

    private static final String AS_BASIC_DB_NAME = "openbis";

    private Set<String> result = new TreeSet<String>();

    private void process(Properties properties)
    {
        String openBisDatabase =
                BackupDatabaseParser.getAppServerDatabaseDescription(properties, AS_DB_KEY_PREFIX,
                        AS_BASIC_DB_NAME);
        addIfFound(openBisDatabase);

        List<String> descriptions =
                BackupDatabaseParser.getDssServerDatabaseDescriptions(properties);
        for (String description : descriptions)
        {
            addIfFound(description);
        }
    }

    private void addIfFound(String databaseDescription)
    {
        if (false == StringUtils.isEmpty(databaseDescription))
        {
            result.add(databaseDescription);
        }
    }

    void process(String[] fileNames)
    {
        for (String fileName : fileNames)
        {
            File file = new File(fileName);
            if (file.isFile() && file.canRead())
            {
                Properties properties = readPropertiesAndInjectCorePluginsIfDSS(file);
                process(properties);
            } else
            {
                System.err.println("Cannot read from specified file " + fileName);
            }
        }
    }

    private Properties readPropertiesAndInjectCorePluginsIfDSS(File propertiesFile)
    {
        Properties properties = PropertyIOUtils.loadProperties(propertiesFile);
        if (isDSSPropertiesFile(propertiesFile))
        {
            String corePluginsFolderLocation =
                    CorePluginsUtils.getCorePluginsFolder(properties, ScannerType.DSS);
            File workingDirectory = propertiesFile.getParentFile().getParentFile();

            File corePluginsFolder = new File(corePluginsFolderLocation);
            if (corePluginsFolder.isAbsolute() == false)
            {
                corePluginsFolder = new File(workingDirectory, corePluginsFolderLocation);
            }
            File file = new File(corePluginsFolder, CorePluginsUtils.CORE_PLUGINS_PROPERTIES_FILE);
            PropertyIOUtils.loadAndAppendProperties(properties, file);
            CorePluginsInjector injector =
                    new CorePluginsInjector(ScannerType.DSS, DssPluginType.values());
            injector.injectCorePlugins(properties, corePluginsFolder.getAbsolutePath());
        }
        return ExtendedProperties.createWith(properties);
    }

    private boolean isDSSPropertiesFile(File propertiesFile)
    {
        String grandParentName = propertiesFile.getParentFile().getParentFile().getName();
        if (grandParentName.equals("datastore_server"))
        {
            return true;
        }
        if (grandParentName.equals("jetty"))
        {
            return false;
        }
        throw new IllegalArgumentException("Neither DSS nor AS service.properties file: "
                + propertiesFile.getAbsolutePath());

    }

    String getResult()
    {
        StringBuilder builder = new StringBuilder();
        for (String description : result)
        {
            if (builder.length() > 0)
            {
                builder.append('\n');
            }
            builder.append(description);
        }
        return builder.toString();
    }

    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            System.err.println("Please specify a list of properties files to be parsed.");
            System.exit(1);
        }

        try
        {
            System.out.println(getDescriptions(args));
        } catch (IOExceptionUnchecked e)
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }

    }

    public static String getDescriptions(String[] args)
    {
        BackupDatabaseDescriptionGenerator generator = new BackupDatabaseDescriptionGenerator();
        generator.process(args);
        return generator.getResult();
    }

}
