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

import static ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME;
import static ch.systemsx.cisd.openbis.generic.shared.Constants.ENABLED_MODULES_KEY;
import static ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.CORE_PLUGIN_PROPERTIES_FILE_NAME;
import static ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector.DISABLED_CORE_PLUGINS_KEY;
import static ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector.PLUGIN_PROPERTIES_FILE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.JettyWebAppPluginInjector;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;

/**
 * @author Franz-Josef Elmer
 */
public class CorePluginsInjectorTest extends AbstractFileSystemTestCase
{
    private static final String INPUT_THREAD_NAMES = "inputs";

    private static final String PLUGIN_SERVICES_LIST_KEY = "plugin-services";

    private static final String REPORTING_PLUGIN_NAMES = "reporting-plugins";

    private Mockery context;

    private ISimpleLogger logger;

    private CorePluginsInjector injector;

    private File corePluginsFolder;

    private String corePluginsFolderProperty;

    private String enabledScreeningProperty;

    private String noMasterDataDisabled;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        logger = context.mock(ISimpleLogger.class);
        PluginType type1 = new PluginType("drop-boxes", INPUT_THREAD_NAMES);
        PluginType type2 = new PluginType("reporting-plugins", REPORTING_PLUGIN_NAMES);
        PluginType type3 = new PluginType("services", PLUGIN_SERVICES_LIST_KEY);
        PluginType type4 = new PluginType("miscellaneous", null);
        PluginType type5 = new PluginType("processing-plugins", "processing-plugins");
        PluginType type6 = new PluginType("dss-data-sources", "prefix.dss")
            {
                @Override
                public String getPluginKey(String technology, String pluginFolderName,
                        Properties properties)
                {
                    String actualTechnology = properties.getProperty("technology", technology);
                    return pluginFolderName + "[" + actualTechnology + "]";
                }

                @Override
                public String getPrefix()
                {
                    return "prefix.";
                }
            };
        PluginType typeWebapps = new PluginType("webapps", "webapps");
        injector = new CorePluginsInjector(ScannerType.DSS, new IPluginType[]
        { type1, type2, type3, type4, type5, type6, typeWebapps }, logger);
        corePluginsFolder = new File(workingDirectory, "core-plugins");
        corePluginsFolder.mkdirs();
        corePluginsFolderProperty =
                CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY + " = " + corePluginsFolder + "\n";
        enabledScreeningProperty =
                ch.systemsx.cisd.openbis.generic.shared.Constants.ENABLED_MODULES_KEY
                        + " = screening\n";
        noMasterDataDisabled = Constants.DISABLED_MASTER_DATA_INITIALIZATION + " = \n";
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingPluginProperties()
    {
        File alpha = new File(corePluginsFolder, "screening/1/dss/drop-boxes/alpha");
        alpha.mkdirs();
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "a, b");

        try
        {
            injector.injectCorePlugins(properties);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing plugin properties: " + alpha.getPath() + "/"
                    + PLUGIN_PROPERTIES_FILE_NAME, ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testInvalidPluginNameAndIgnoringDotFilesAndFolders() throws IOException
    {
        File alpha = new File(corePluginsFolder, "screening/1/dss/drop-boxes/a b");
        alpha.mkdirs();
        FileUtilities.writeToFile(new File(alpha, PLUGIN_PROPERTIES_FILE_NAME), "");
        new File(alpha.getParentFile(), ".svn").mkdirs();
        new File(alpha.getParentFile(), ".blabla").createNewFile();
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "a, b");

        try
        {
            injector.injectCorePlugins(properties);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Plugin name contains ' ': a b", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testPluginIsNotAFolder() throws IOException
    {
        File alpha = new File(corePluginsFolder, "screening/1/dss/drop-boxes/alpha");
        alpha.getParentFile().mkdirs();
        alpha.createNewFile();
        Properties properties = createProperties();

        try
        {
            injector.injectCorePlugins(properties);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Is not a directory: " + alpha.getPath(), ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDropBoxAndReportingPluginWithSameKey()
    {
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "key1, key2");
        properties.setProperty(REPORTING_PLUGIN_NAMES, "key2, key3");

        try
        {
            injector.injectCorePlugins(properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Property key 'key2' for key list 'reporting-plugins' "
                    + "is already defined in some other key list.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testADropBoxWithScriptFile()
    {
        File myDropBox = new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box");
        myDropBox.mkdirs();
        FileUtilities.writeToFile(new File(myDropBox, PLUGIN_PROPERTIES_FILE_NAME),
                "class = blabla\n" + "incoming = ${fdata}\n" + "script = handler.py");
        FileUtilities.writeToFile(new File(myDropBox, "handler.py"), "print 'hello world'");
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "a, b");
        properties.setProperty("fdata", "../my-data");
        preparePluginNameLog("screening:drop-boxes:my-drop-box [" + myDropBox + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty
                + "fdata = ../my-data\n" + "inputs = a, b, my-drop-box\n"
                + "my-drop-box.class = blabla\n" + "my-drop-box.incoming = ${fdata}\n"
                + "my-drop-box.script = " + myDropBox + "/handler.py\n", properties);
        assertEquals("../my-data", properties.getProperty("my-drop-box.incoming"));

        context.assertIsSatisfied();
    }

    @Test
    public void testDropBoxAndProcessingPluginsFromDifferentTechnologiesAndVersions()
    {
        File myDropBox = new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box");
        myDropBox.mkdirs();
        FileUtilities.writeToFile(new File(myDropBox, PLUGIN_PROPERTIES_FILE_NAME),
                "script1 = script1.py\n" + "script2 = script2.py");
        FileUtilities.writeToFile(new File(myDropBox, "script1.py"), "print 'hello world'");
        FileUtilities.writeToFile(new File(myDropBox, "script2.py"), "print 'hello universe'");
        File myProcessingPlugin =
                new File(corePluginsFolder, "proteomics/2/dss/processing-plugins/my-processing");
        myProcessingPlugin.mkdirs();
        FileUtilities.writeToFile(new File(myProcessingPlugin, PLUGIN_PROPERTIES_FILE_NAME),
                "script = script.py");
        FileUtilities.writeToFile(new File(myProcessingPlugin, "script.py"), "print 'hello world'");
        Properties properties = createProperties("prot.*, screening");
        properties.setProperty(REPORTING_PLUGIN_NAMES, "a, b");
        preparePluginNameLog("screening:drop-boxes:my-drop-box [" + myDropBox + "]",
                "proteomics:processing-plugins:my-processing [" + myProcessingPlugin + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled
                + ch.systemsx.cisd.openbis.generic.shared.Constants.ENABLED_MODULES_KEY
                + " = prot.*, screening\n" + "inputs = my-drop-box\n" + "my-drop-box.script1 = "
                + myDropBox + "/script1.py\n" + "my-drop-box.script2 = " + myDropBox
                + "/script2.py\n" + "my-processing.script = " + myProcessingPlugin + "/script.py\n"
                + "processing-plugins = my-processing\n" + "reporting-plugins = a, b\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testADuplicatedReportingPluginVersion2()
    {
        new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box").mkdirs();
        File dropBox2 = new File(corePluginsFolder, "screening/2/dss/drop-boxes/k2");
        dropBox2.mkdirs();
        FileUtilities.writeToFile(new File(dropBox2, PLUGIN_PROPERTIES_FILE_NAME),
                "class = blabla\n");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "k1, k2");

        try
        {
            injector.injectCorePlugins(properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("There is already a plugin named 'k2'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testADuplicatedButDisabledReportingPluginVersion2()
    {
        new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box").mkdirs();
        File dropBox2 = new File(corePluginsFolder, "screening/2/dss/drop-boxes/k2");
        dropBox2.mkdirs();
        FileUtilities.writeToFile(new File(dropBox2, PLUGIN_PROPERTIES_FILE_NAME),
                "class = blabla\n");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "k1, k2");
        properties.setProperty(DISABLED_CORE_PLUGINS_KEY, "screening");

        injector.injectCorePlugins(properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testAMiscellaneousPlugin()
    {
        File misc = new File(corePluginsFolder, "screening/1/dss/miscellaneous/a");
        misc.mkdirs();
        FileUtilities.writeToFile(new File(misc, PLUGIN_PROPERTIES_FILE_NAME),
                REPORTING_PLUGIN_NAMES + " = my-report\n" + "my-report.script = r.py\n"
                        + DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME + " = t1, t2\n"
                        + "t1.class = blabla\n" + "t2.script = task.py");
        FileUtilities.writeToFile(new File(misc, "r.py"), "print 'hello world'");
        FileUtilities.writeToFile(new File(misc, "task.py"), "print 'hello task'");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "k1, k2");
        preparePluginNameLog("screening:miscellaneous:a [" + misc + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty
                + "maintenance-plugins = t1, t2\n" + "my-report.script = " + misc + "/r.py\n"
                + "reporting-plugins = k1, k2, my-report\n" + "t1.class = blabla\n"
                + "t2.script = " + misc + "/task.py\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testWebappsPlugin() throws IOException
    {
        File webapps = new File(corePluginsFolder, "screening/1/dss/webapps");
        webapps.mkdirs();

        File exampleWebapp =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/openbis/generic/shared/coreplugin/webapps/example-webapp");
        FileUtils.copyDirectoryToDirectory(exampleWebapp, webapps);

        Properties properties = createProperties();
        preparePluginNameLog("screening:webapps:example-webapp [" + webapps + "/example-webapp]");

        injector.injectCorePlugins(properties);

        List<String> appList =
                PropertyUtils.tryGetListInOriginalCase(properties, BasicConstant.WEB_APPS_PROPERTY);
        assertEquals(1, appList.size());
        assertEquals("example-webapp", appList.get(0));
        Properties exampleWebappProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties, appList.get(0),
                        false).getProperties();
        String webappFolder =
                exampleWebappProperties
                        .getProperty(JettyWebAppPluginInjector.WEB_APP_FOLDER_PROPERTY);
        assertEquals(webapps.toString() + "/example-webapp/html", webappFolder);

        context.assertIsSatisfied();
    }
    
    @Test
    public void testDependentPlugins()
    {
        File r1 = new File(corePluginsFolder, "dep2/2/dss/reporting-plugins/r1");
        r1.mkdirs();
        FileUtilities.writeToFile(new File(r1, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dpa = new File(corePluginsFolder, "dep/1/dss/drop-boxes/a");
        dpa.mkdirs();
        FileUtilities.writeToFile(new File(dpa, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dpb = new File(corePluginsFolder, "dep/1/dss/drop-boxes/b");
        dpb.mkdirs();
        FileUtilities.writeToFile(new File(dpb, PLUGIN_PROPERTIES_FILE_NAME), "");
        File s1 = new File(corePluginsFolder, "dep/1/dss/services/s1");
        s1.mkdirs();
        FileUtilities.writeToFile(new File(s1, PLUGIN_PROPERTIES_FILE_NAME), "");
        File s2 = new File(corePluginsFolder, "dep/1/dss/services/s2");
        s2.mkdirs();
        FileUtilities.writeToFile(new File(s2, PLUGIN_PROPERTIES_FILE_NAME), "");
        FileUtilities.writeToFile(new File(corePluginsFolder, "dep/1/" + CORE_PLUGIN_PROPERTIES_FILE_NAME), 
                CorePluginScanner.REQUIRED_PLUGINS_KEY + " = dep2:reporting-plugins, dep2:services");
        File misc = new File(corePluginsFolder, "screening/1/dss/miscellaneous/c");
        misc.mkdirs();
        FileUtilities.writeToFile(new File(misc, PLUGIN_PROPERTIES_FILE_NAME), "");
        FileUtilities.writeToFile(new File(corePluginsFolder, "screening/1/" + CORE_PLUGIN_PROPERTIES_FILE_NAME), 
                CorePluginScanner.REQUIRED_PLUGINS_KEY + " = dep:drop-boxes:a, dep:services");
        
        Properties properties = createProperties();
        preparePluginNameLog("dep2:reporting-plugins:r1 [" + r1 + "]", "dep:drop-boxes:a [" + dpa + "]", 
                "dep:services:s1 [" + s1 + "]", 
                "dep:services:s2 [" + s2 + "]", "screening:miscellaneous:c [" + misc + "]");
        
        injector.injectCorePlugins(properties);
        
        assertProperties(corePluginsFolderProperty 
                + Constants.DISABLED_MASTER_DATA_INITIALIZATION + " = dep,dep2\n" + enabledScreeningProperty
                + "inputs = a\n" + "plugin-services = s1, s2\n"
                + "reporting-plugins = r1\n", properties);
        context.assertIsSatisfied();
    }

    @Test
    public void testDisabledPluginsByPropertiesAndNotEnabledTechnology()
    {
        File dpa = new File(corePluginsFolder, "screening/1/dss/miscellaneous/a");
        dpa.mkdirs();
        FileUtilities.writeToFile(new File(dpa, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dpb = new File(corePluginsFolder, "screening/1/dss/miscellaneous/b");
        dpb.mkdirs();
        FileUtilities.writeToFile(new File(dpb, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dp1 = new File(corePluginsFolder, "screening/1/dss/drop-boxes/dp1");
        dp1.mkdirs();
        FileUtilities.writeToFile(new File(dp1, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dp2 = new File(corePluginsFolder, "screening/1/dss/drop-boxes/dp2");
        dp2.mkdirs();
        FileUtilities.writeToFile(new File(dp2, PLUGIN_PROPERTIES_FILE_NAME), "");
        File dp3 = new File(corePluginsFolder, "proteomics/1/dss/drop-boxes/dp3");
        dp3.mkdirs();
        FileUtilities.writeToFile(new File(dp3, PLUGIN_PROPERTIES_FILE_NAME), "");
        Properties properties = createProperties();
        properties.setProperty(DISABLED_CORE_PLUGINS_KEY,
                "screening:miscellaneous, screening:drop-boxes:dp1, screening:"
                        + CorePluginsInjector.INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME);
        preparePluginNameLog("screening:drop-boxes:dp2 [" + dp2 + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty
                + "disabled-core-plugins = screening:miscellaneous, screening:drop-boxes:dp1, screening:"
                + CorePluginsInjector.INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME + "\n"
                + Constants.DISABLED_MASTER_DATA_INITIALIZATION + " = proteomics,screening\n" 
                + enabledScreeningProperty + "inputs = dp2\n", properties);
        context.assertIsSatisfied();
    }

    @Test
    public void testDisabledPluginsByMarkerFile() throws Exception
    {
        File dp1 = new File(corePluginsFolder, "screening/1/dss/drop-boxes/dp1");
        dp1.mkdirs();
        FileUtilities.writeToFile(new File(dp1, PLUGIN_PROPERTIES_FILE_NAME), "");
        new File(dp1, CorePluginsInjector.DISABLED_MARKER_FILE_NAME).createNewFile();
        File dp2 = new File(corePluginsFolder, "screening/1/dss/drop-boxes/dp2");
        dp2.mkdirs();
        FileUtilities.writeToFile(new File(dp2, PLUGIN_PROPERTIES_FILE_NAME), "");
        Properties properties = createProperties();
        preparePluginNameLog("screening:drop-boxes:dp2 [" + dp2 + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty + "inputs = dp2\n",
                properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDeletedPropertyForPluginTypeNotMiscellaneous()
    {
        File pluginFolder = new File(corePluginsFolder, "screening/1/dss/services/z");
        pluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(pluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "alpha = 42\nbeta = 43");
        Properties properties = createProperties();
        properties.setProperty("z.beta", CorePluginsInjector.DELETE_KEY_WORD);
        preparePluginNameLog("screening:services:z [" + pluginFolder + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty
                + PLUGIN_SERVICES_LIST_KEY + " = z\n" + "z.alpha = 42\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDeletedPropertyForPluginTypeMiscellaneous()
    {
        File pluginFolder = new File(corePluginsFolder, "screening/1/dss/miscellaneous/z");
        pluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(pluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "z = 42\nbeta = 43");
        Properties properties = createProperties();
        properties.setProperty("beta", CorePluginsInjector.DELETE_KEY_WORD);
        preparePluginNameLog("screening:miscellaneous:z [" + pluginFolder + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty + "z = 42\n",
                properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDssDataSource()
    {
        File pluginFolder = new File(corePluginsFolder, "screening/1/dss/dss-data-sources/dss1");
        pluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(pluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "driver = alpha\nurl = blabla");
        Properties properties = createProperties();
        properties.setProperty("prefix.dss", "dss1[proteomics]");
        properties.setProperty("prefix.dss1[proteomics].driver", "gamma");
        preparePluginNameLog("screening:dss-data-sources:dss1 [" + pluginFolder + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled + enabledScreeningProperty
                + "prefix.dss = dss1[proteomics], dss1[screening]\n"
                + "prefix.dss1[proteomics].driver = gamma\n"
                + "prefix.dss1[screening].driver = alpha\n"
                + "prefix.dss1[screening].url = blabla\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDssDataSourcesForDifferentTechnologiesButSameDSS()
    {
        File screeningPluginFolder =
                new File(corePluginsFolder, "screening/1/dss/dss-data-sources/dss1");
        screeningPluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(screeningPluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "driver = alpha\nurl = blabla");
        File proteomicsPluginFolder =
                new File(corePluginsFolder, "proteomics/1/dss/dss-data-sources/dss1");
        proteomicsPluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(proteomicsPluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "driver = beta\nurl = blub");
        Properties properties = createProperties("screening, proteomics");
        preparePluginNameLog("screening:dss-data-sources:dss1 [" + screeningPluginFolder + "]");
        preparePluginNameLog("proteomics:dss-data-sources:dss1 [" + proteomicsPluginFolder + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled
                + ch.systemsx.cisd.openbis.generic.shared.Constants.ENABLED_MODULES_KEY
                + " = screening, proteomics\n" + "prefix.dss = dss1[proteomics], dss1[screening]\n"
                + "prefix.dss1[proteomics].driver = beta\n"
                + "prefix.dss1[proteomics].url = blub\n"
                + "prefix.dss1[screening].driver = alpha\n"
                + "prefix.dss1[screening].url = blabla\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDssDataSourcesForSameTechnologyButDifferentDSSInDifferentTechFolders()
    {
        File screeningPluginFolder =
                new File(corePluginsFolder, "screening/1/dss/dss-data-sources/dss1");
        screeningPluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(screeningPluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "driver = alpha\nurl = blabla");
        File devPluginFolder = new File(corePluginsFolder, "dev/1/dss/dss-data-sources/dss2");
        devPluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(devPluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "technology = screening\ndriver = beta\nurl = blub");
        Properties properties = createProperties("screening, dev");
        preparePluginNameLog("screening:dss-data-sources:dss1 [" + screeningPluginFolder + "]");
        preparePluginNameLog("dev:dss-data-sources:dss2 [" + devPluginFolder + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + noMasterDataDisabled
                + ch.systemsx.cisd.openbis.generic.shared.Constants.ENABLED_MODULES_KEY
                + " = screening, dev\n" + "prefix.dss = dss1[screening], dss2[screening]\n"
                + "prefix.dss1[screening].driver = alpha\n"
                + "prefix.dss1[screening].url = blabla\n"
                + "prefix.dss2[screening].driver = beta\n"
                + "prefix.dss2[screening].technology = screening\n"
                + "prefix.dss2[screening].url = blub\n", properties);

        context.assertIsSatisfied();
    }

    @Test
    public void testDssDataSourceFailedBecauseOfDuplicatedPlugin()
    {
        File pluginFolder = new File(corePluginsFolder, "screening/1/dss/dss-data-sources/dss1");
        pluginFolder.mkdirs();
        FileUtilities.writeToFile(new File(pluginFolder, PLUGIN_PROPERTIES_FILE_NAME),
                "driver = alpha\nurl = blabla");
        Properties properties = createProperties();
        properties.setProperty("prefix.dss", "dss1[screening]");
        properties.setProperty("prefix.dss1[screening].driver", "gamma");

        try
        {
            injector.injectCorePlugins(properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("There is already a plugin named 'prefix.dss1[screening]'.",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private void preparePluginNameLog(final String... fullPluginNames)
    {
        context.checking(new Expectations()
            {
                {
                    for (String fullPluginName : fullPluginNames)
                    {
                        one(logger).log(LogLevel.INFO, "Plugin " + fullPluginName + " added.");
                    }
                }
            });
    }

    private void assertProperties(String expectedProperties, Properties properties)
    {
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        List<String> actualProperties = new ArrayList<String>();
        for (Entry<Object, Object> entry : entrySet)
        {
            actualProperties.add(entry.getKey() + " = " + entry.getValue());
        }
        Collections.sort(actualProperties);
        StringBuilder builder = new StringBuilder();
        for (String actualProperty : actualProperties)
        {
            builder.append(actualProperty).append("\n");
        }
        assertEquals(expectedProperties, builder.toString());
    }

    private Properties createProperties()
    {
        return createProperties("screening");
    }

    private Properties createProperties(String technologies)
    {
        Properties properties = new ExtendedProperties();
        properties.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY, corePluginsFolder.getPath());
        properties.setProperty(ENABLED_MODULES_KEY, technologies);
        return properties;
    }

}
