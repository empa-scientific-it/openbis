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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdatePluginTest extends AbstractTest
{
    @Test
    public void testUpdate()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("properties");
        PluginUpdate update = new PluginUpdate();
        update.setPluginId(id);
        update.setDescription("test");
        update.setAvailable(false);
        update.setScript("'test'");

        // When
        v3api.updatePlugins(sessionToken, Arrays.asList(update));

        // Then
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(plugin.getName(), "properties");
        assertEquals(plugin.getScript(), update.getScript().getValue());
        assertEquals(plugin.isAvailable(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdateAndCheckReindexing()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("code");
        PluginUpdate update = new PluginUpdate();
        update.setPluginId(id);
        update.setDescription("test");
        update.setAvailable(true);
        update.setScript("42");

        // When
        v3api.updatePlugins(sessionToken, Arrays.asList(update));

        // Then
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(plugin.getName(), "code");
        assertEquals(plugin.getScript(), update.getScript().getValue());
        assertEquals(plugin.isAvailable(), true);
        assertSamplesExists("200902091219327-1053");

        v3api.logout(sessionToken);
    }

    @Test
    public void testPluginIdMissing()
    {
        PluginUpdate update = new PluginUpdate();
        assertUserFailureException(update, "Plugin id cannot be null.");
    }

    @Test
    public void testPluginScriptCanNotCompile()
    {
        PluginUpdate update = new PluginUpdate();
        update.setPluginId(new PluginPermId("properties"));
        update.setScript("d:");
        assertUserFailureException(update, "SyntaxError");
    }

    @Test(dataProvider = "usersNotAllowedToUpdatePlugins")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        PluginPermId pluginId = new PluginPermId("properties");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PluginUpdate update = new PluginUpdate();
                    update.setPluginId(pluginId);
                    v3api.updatePlugins(sessionToken, Arrays.asList(update));
                }
            }, pluginId);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginUpdate update = new PluginUpdate();
        update.setPluginId(new PluginPermId("properties"));

        PluginUpdate update2 = new PluginUpdate();
        update2.setPluginId(new PluginPermId("code"));

        v3api.updatePlugins(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-plugins  PLUGIN_UPDATES('[PluginUpdate[pluginId=properties], PluginUpdate[pluginId=code]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdatePlugins()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    private void assertUserFailureException(PluginUpdate update, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.updatePlugins(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }
}
