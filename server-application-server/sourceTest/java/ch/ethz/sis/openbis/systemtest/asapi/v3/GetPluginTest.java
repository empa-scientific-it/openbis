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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;

/**
 * @author Franz-Josef Elmer
 */
public class GetPluginTest extends AbstractTest
{
    @Test
    public void testGetPluginForNoSpecificEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("properties");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        fetchOptions.withRegistrator();

        // When
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).get(id);

        // Then
        assertEquals(plugin.getName(), id.getPermId());
        assertEquals(plugin.getPermId(), id);
        assertEquals(plugin.getDescription(), "number of properties");
        assertEquals(plugin.getEntityKinds(), EnumSet.allOf(EntityKind.class));
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getPluginType(), PluginType.DYNAMIC_PROPERTY);
        assertEquals(plugin.isAvailable(), true);
        assertEquals(plugin.getScript(), "str(entity.properties().size()) + ' properties'");
        assertEqualsDate(plugin.getRegistrationDate(), "2010-10-27 15:16:48");
        assertEquals(plugin.getRegistrator().getPermId().getPermId(), "test");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetPluginForASpecificEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("testEXPERIMENT");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        fetchOptions.withRegistrator();

        // When
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).get(id);

        // Then
        assertEquals(plugin.getName(), id.getPermId());
        assertEquals(plugin.getPermId(), id);
        assertEquals(plugin.getDescription(), null);
        assertEquals(plugin.getEntityKinds().toString(), "[EXPERIMENT]");
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getPluginType(), PluginType.ENTITY_VALIDATION);
        assertEquals(plugin.isAvailable(), true);
        assertEquals(plugin.getScript(), "import time;\ndef validate(entity, isNew):\n  pass\n ");
        assertEqualsDate(plugin.getRegistrationDate(), "2010-10-27 15:16:48");
        assertEquals(plugin.getRegistrator().getPermId().getPermId(), "test");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetPluginWithEmptyFetchOptions()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("managed list");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();

        // When
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).get(id);

        // Then
        assertEquals(plugin.getName(), id.getPermId());
        assertEquals(plugin.getPermId(), id);
        assertEquals(plugin.getDescription(), null);
        assertEquals(plugin.getEntityKinds(), EnumSet.allOf(EntityKind.class));
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getPluginType(), PluginType.MANAGED_PROPERTY);
        assertEquals(plugin.isAvailable(), true);
        assertEqualsDate(plugin.getRegistrationDate(), "2010-10-27 15:16:48");
        assertEquals(plugin.getFetchOptions().hasRegistrator(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginFetchOptions fo = new PluginFetchOptions();
        fo.withRegistrator();
        fo.withScript();

        v3api.getPlugins(sessionToken, Arrays.asList(new PluginPermId("properties"), new PluginPermId("validateOK")), fo);

        assertAccessLog(
                "get-plugins  PLUGIN_IDS('[properties, validateOK]') FETCH_OPTIONS('Plugin\n    with Registrator\n    with Script\n')");
    }

}
