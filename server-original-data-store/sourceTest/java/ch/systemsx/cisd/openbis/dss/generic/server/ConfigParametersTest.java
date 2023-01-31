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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;
import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PluginServletConfig;

/**
 * @author Franz-Josef Elmer
 */
public class ConfigParametersTest extends AssertJUnit
{
    @Test
    public void testMinimumConfig()
    {
        ConfigParameters configParameters = new ConfigParameters(createMandatoryProperties());

        assertEquals("store", configParameters.getStorePath().getPath());
        assertEquals(4711, configParameters.getPort());
        assertEquals("my-url", configParameters.getServerURL());
        assertEquals("download-url", configParameters.getDownloadURL());
        assertEquals(true, configParameters.isUseSSL());
        assertEquals("key-store", configParameters.getKeystorePath());
        assertEquals("key-store-password", configParameters.getKeystorePassword());
        assertEquals("key-store-key-password", configParameters.getKeystoreKeyPassword());
        assertEquals(false, configParameters.isUseNIO());
        assertEquals(ConfigParameters.DEFAULT_AUTH_CACHE_CLEANUP_TIMER_PERIOD_MINS,
                configParameters.getAuthCacheCleanupTimerPeriodMins());
        assertEquals(ConfigParameters.DEFAULT_AUTH_CACHE_EXPIRATION_TIME_MINS,
                configParameters.getAuthCacheExpirationTimeMins());
        assertEquals(1, configParameters.getPluginServlets().size());
        assertEquals(ConfigParameters.WEBSTART_JAR_PATH_DEFAULT,
                configParameters.getWebstartJarPath());
    }

    @Test
    public void testPluginServices()
    {
        Properties properties = createMandatoryProperties();
        properties.setProperty(Constants.PLUGIN_SERVICES_LIST_KEY, "s1, s2");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class1");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");
        properties.setProperty("s2." + ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class2");
        properties.setProperty("s2." + ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path2");
        properties.setProperty("s2.a", "alpha");
        ConfigParameters configParameters = new ConfigParameters(properties);

        List<PluginServletConfig> pluginServlets = configParameters.getPluginServlets();
        assertEquals("class1", pluginServlets.get(1).getServletClass());
        assertEquals("path1", pluginServlets.get(1).getServletPath());
        assertEquals("class2", pluginServlets.get(2).getServletClass());
        assertEquals("path2", pluginServlets.get(2).getServletPath());
        assertTrue(pluginServlets.get(2).getServletProperties().containsKey("a"));
        assertEquals(3, pluginServlets.size());
    }

    @Test
    public void testPluginServicesWithDuplicatedPath()
    {
        Properties properties = createMandatoryProperties();
        properties.setProperty(Constants.PLUGIN_SERVICES_LIST_KEY, "s1, s2");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class1");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");
        properties.setProperty("s2." + ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class2");
        properties.setProperty("s2." + ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");
        properties.setProperty("s2.a", "alpha");
        try
        {
            new ConfigParameters(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Servlet configuration [s2]: There has already been a servlet "
                    + "configured for the path 'path1'.", ex.getMessage());
        }
    }

    @Test
    public void testAddServletProperties()
    {
        ConfigParameters configParameters = new ConfigParameters(createMandatoryProperties());

        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class1");
        properties.setProperty(ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");
        configParameters.addServletProperties("my-servlet", properties);

        List<PluginServletConfig> pluginServlets = configParameters.getPluginServlets();
        // System-injected servlets come first. Of these, there are one.
        assertEquals("class1", pluginServlets.get(1).getServletClass());
        assertEquals("path1", pluginServlets.get(1).getServletPath());
        assertEquals(2, pluginServlets.size());
    }

    @Test
    public void testAddServletPropertiesWithExistingPath()
    {
        Properties properties = createMandatoryProperties();
        properties.setProperty(Constants.PLUGIN_SERVICES_LIST_KEY, "s1");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class1");
        properties.setProperty("s1." + ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");
        ConfigParameters configParameters = new ConfigParameters(properties);
        Properties props = new Properties();
        props.setProperty(ConfigParameters.PLUGIN_SERVICE_CLASS_KEY, "class1");
        props.setProperty(ConfigParameters.PLUGIN_SERVICE_PATH_KEY, "path1");

        try
        {
            configParameters.addServletProperties("my-servlet", props);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Servlet configuration [my-servlet]: There has already been a servlet "
                    + "configured for the path 'path1'.", ex.getMessage());
        }
    }

    private Properties createMandatoryProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.STOREROOT_DIR_KEY, "store");
        properties.setProperty(ConfigParameters.PORT_KEY, "4711");
        properties.setProperty(ConfigParameters.SERVER_URL_KEY, "my-url");
        properties.setProperty(ConfigParameters.DOWNLOAD_URL, "download-url");
        properties.setProperty(ConfigParameters.SESSION_TIMEOUT_KEY, "42");
        properties.setProperty(ConfigParameters.KEYSTORE_PATH_KEY, "key-store");
        properties.setProperty(ConfigParameters.KEYSTORE_PASSWORD_KEY, "key-store-password");
        properties.setProperty(ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY, "key-store-key-password");
        return properties;
    }

}
