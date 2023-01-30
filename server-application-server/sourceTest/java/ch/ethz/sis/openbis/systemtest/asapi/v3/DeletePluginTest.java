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
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class DeletePluginTest extends AbstractTest
{
    @Test
    public void testDelete()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("managed list");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        assertEquals(v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions).size(), 1);
        PluginDeletionOptions deletionOptions = new PluginDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deletePlugins(sessionToken, Arrays.asList(id), deletionOptions);

        // Then
        Map<IPluginId, Plugin> plugins = v3api.getPlugins(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(plugins.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "usersNotAllowedToDeletePlugins")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        PluginPermId id = new PluginPermId("managed list");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PluginDeletionOptions deletionOptions = new PluginDeletionOptions();
                    deletionOptions.setReason("test");
                    v3api.deletePlugins(sessionToken, Arrays.asList(id), deletionOptions);
                }
            }, id);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginDeletionOptions o = new PluginDeletionOptions();
        o.setReason("test-reason");

        v3api.deletePlugins(sessionToken, Arrays.asList(new PluginPermId("TEST-LOGGING-1"), new PluginPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-plugins  PLUGIN_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('PluginDeletionOptions[reason=test-reason]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToDeletePlugins()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
}
