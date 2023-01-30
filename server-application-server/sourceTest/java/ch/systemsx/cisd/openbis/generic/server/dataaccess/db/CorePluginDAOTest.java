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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;

/**
 * Test cases for the {@link CorePluginDAO} class.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups =
{ "db", "slow" })
public final class CorePluginDAOTest extends AbstractDAOTest
{
    @Test
    public void testList()
    {
        CorePluginPE plugin1 = createCorePlugin("pluginA", 1);
        CorePluginPE plugin2 = createCorePlugin("pluginA", 3);
        CorePluginPE plugin3 = createCorePlugin("pluginB", 3);
        List<CorePluginPE> plugins = Arrays.asList(plugin1, plugin2, plugin3);
        daoFactory.getCorePluginDAO().createCorePlugins(plugins);

        List<CorePluginPE> saved = daoFactory.getCorePluginDAO().listCorePluginsByName("pluginA");

        assertEquals(Arrays.asList(plugin1, plugin2), saved);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTwoVersionsOfTheSamePlugin()
    {
        CorePluginPE plugin1 = createCorePlugin("pluginA", 2);
        CorePluginPE plugin2 = createCorePlugin("pluginA", 2);
        daoFactory.getCorePluginDAO().createCorePlugins(Arrays.asList(plugin1, plugin2));
    }

    private CorePluginPE createCorePlugin(String name, int version)
    {
        CorePluginPE plugin = new CorePluginPE();
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setMasterDataRegistrationScript("test script");
        return plugin;
    }
}
