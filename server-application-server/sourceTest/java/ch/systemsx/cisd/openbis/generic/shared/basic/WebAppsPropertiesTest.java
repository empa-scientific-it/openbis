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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;

/**
 * @author pkupczyk
 */
public class WebAppsPropertiesTest
{

    @Test
    public void testGetWebAppsWithEmptyPropertiesShouldReturnEmptyList()
    {
        WebAppsProperties properties = loadProperties("empty");
        List<WebApp> webApps = properties.getWebApps();
        Assert.assertEquals(0, webApps.size());
    }

    @Test
    public void testGetWebAppsWithPropertiesWithoutWebAppsShouldReturnEmptyList()
    {
        WebAppsProperties properties = loadProperties("without_webapps");
        List<WebApp> webApps = properties.getWebApps();
        Assert.assertEquals(0, webApps.size());
    }

    @Test
    public void testGetWebAppsWithPropertiesWithOneWebAppShouldReturnListWithOneWebApp()
    {
        WebAppsProperties properties = loadProperties("with_one_webapp");
        List<WebApp> webApps = properties.getWebApps();
        Assert.assertEquals(1, webApps.size());
        WebApp webApp = webApps.get(0);
        Assert.assertEquals("webapp1", webApp.getCode());
        Assert.assertEquals("Web app 1", webApp.getLabel());
        Assert.assertEquals(Integer.valueOf(100), webApp.getSorting());
        Assert.assertTrue(Arrays.equals(new String[]
        { "queries-menu", "experiment-details-view" }, webApp.getContexts()));
        Assert.assertTrue(Arrays.equals(new String[]
        { "EXP1", "EXP2" }, webApp.getEntityTypes().get(EntityKind.EXPERIMENT)));
        Assert.assertTrue(Arrays.equals(new String[]
        { "SAM1" }, webApp.getEntityTypes().get(EntityKind.SAMPLE)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp.getEntityTypes().get(EntityKind.DATA_SET)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp.getEntityTypes().get(EntityKind.MATERIAL)));
    }

    @Test
    public void testGetWebAppsWithPropertiesWithManyWebAppShouldReturnListWithManyWebApps()
    {
        WebAppsProperties properties = loadProperties("with_many_webapps");
        List<WebApp> webApps = properties.getWebApps();
        Assert.assertEquals(2, webApps.size());

        WebApp webApp1 = webApps.get(0);
        Assert.assertEquals("webapp1", webApp1.getCode());
        Assert.assertEquals("Web app 1", webApp1.getLabel());
        Assert.assertNull(webApp1.getSorting());
        Assert.assertTrue(Arrays.equals(new String[] {}, webApp1.getContexts()));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp1.getEntityTypes().get(EntityKind.EXPERIMENT)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp1.getEntityTypes().get(EntityKind.SAMPLE)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp1.getEntityTypes().get(EntityKind.DATA_SET)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp1.getEntityTypes().get(EntityKind.MATERIAL)));

        WebApp webApp2 = webApps.get(1);
        Assert.assertEquals("webapp2", webApp2.getCode());
        Assert.assertEquals("Web app 2", webApp2.getLabel());
        Assert.assertNull(webApp2.getSorting());
        Assert.assertTrue(Arrays.equals(new String[] {}, webApp2.getContexts()));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp2.getEntityTypes().get(EntityKind.EXPERIMENT)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp2.getEntityTypes().get(EntityKind.SAMPLE)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp2.getEntityTypes().get(EntityKind.DATA_SET)));
        Assert.assertTrue(Arrays.equals(new String[] {},
                webApp2.getEntityTypes().get(EntityKind.MATERIAL)));
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testGetWebAppsWithPropertiesWithDuplicatedCodeShouldThrowException()
    {
        WebAppsProperties properties = loadProperties("with_duplicated_code");
        properties.getWebApps();
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testGetWebAppsWithPropertiesWithIncorrectSortingShouldThrowException()
    {
        WebAppsProperties properties = loadProperties("with_incorrect_sorting");
        properties.getWebApps();
    }

    private WebAppsProperties loadProperties(String fileNameSuffix)
    {
        try
        {
            String fileName = getClass().getSimpleName() + "__" + fileNameSuffix + ".properties";
            Properties properties = new Properties();
            properties
                    .load(new InputStreamReader(getClass().getResourceAsStream(fileName), "UTF-8"));
            return new WebAppsProperties(properties);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
