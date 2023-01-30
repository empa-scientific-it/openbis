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
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreatePluginTest extends AbstractTest
{
    @Test
    public void testCreateMinimalManagedPropertyJythonPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginCreation creation = new PluginCreation();
        creation.setName("test " + System.currentTimeMillis());
        creation.setPluginType(PluginType.MANAGED_PROPERTY);
        creation.setScript("pass");

        // When
        List<PluginPermId> ids = v3api.createPlugins(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        fetchOptions.withRegistrator();
        Plugin plugin = v3api.getPlugins(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(plugin.getName(), creation.getName());
        assertEquals(plugin.getPermId().getPermId(), creation.getName());
        assertEquals(plugin.getDescription(), null);
        assertEquals(plugin.getEntityKinds(), EnumSet.allOf(EntityKind.class));
        assertEquals(plugin.getPluginType(), PluginType.MANAGED_PROPERTY);
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getScript(), creation.getScript());
        assertEquals(plugin.getRegistrator().getUserId(), TEST_USER);
        assertEquals(plugin.isAvailable(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateDynamicPropertyJythonPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginCreation creation = new PluginCreation();
        creation.setName("test " + System.currentTimeMillis());
        creation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        creation.setScript("42");

        // When
        List<PluginPermId> ids = v3api.createPlugins(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withRegistrator();
        Plugin plugin = v3api.getPlugins(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(plugin.getName(), creation.getName());
        assertEquals(plugin.getPermId().getPermId(), creation.getName());
        assertEquals(plugin.getDescription(), null);
        assertEquals(plugin.getEntityKinds(), EnumSet.allOf(EntityKind.class));
        assertEquals(plugin.getPluginType(), PluginType.DYNAMIC_PROPERTY);
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getFetchOptions().hasScript(), false);
        assertEquals(plugin.getRegistrator().getUserId(), TEST_USER);
        assertEquals(plugin.isAvailable(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateEntityEvaluationJythonPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginCreation creation = new PluginCreation();
        creation.setName("test " + System.currentTimeMillis());
        creation.setPluginType(PluginType.ENTITY_VALIDATION);
        creation.setScript("42");
        creation.setEntityKind(EntityKind.DATA_SET);

        // When
        List<PluginPermId> ids = v3api.createPlugins(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        fetchOptions.withRegistrator();
        Plugin plugin = v3api.getPlugins(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(plugin.getName(), creation.getName());
        assertEquals(plugin.getPermId().getPermId(), creation.getName());
        assertEquals(plugin.getDescription(), null);
        assertEquals(plugin.getEntityKinds(), EnumSet.of(EntityKind.DATA_SET));
        assertEquals(plugin.getPluginType(), PluginType.ENTITY_VALIDATION);
        assertEquals(plugin.getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugin.getScript(), creation.getScript());
        assertEquals(plugin.getRegistrator().getUserId(), TEST_USER);
        assertEquals(plugin.isAvailable(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMissingName()
    {
        PluginCreation creation = createBasic();
        creation.setName(null);
        assertUserFailureException(creation, "Name cannot be empty.");
    }

    @Test
    public void testEmptyName()
    {
        PluginCreation creation = createBasic();
        creation.setName("");
        assertUserFailureException(creation, "Name cannot be empty.");
    }

    @Test
    public void testMissingScriptType()
    {
        PluginCreation creation = createBasic();
        creation.setPluginType(null);
        assertUserFailureException(creation, "Plugin type cannot be unspecified.");
    }

    @Test
    public void testMissingScript()
    {
        PluginCreation creation = createBasic();
        creation.setScript(null);
        assertUserFailureException(creation, "Script cannot be empty.");
    }

    @Test
    public void testScriptIsAnEmptyString()
    {
        PluginCreation creation = createBasic();
        creation.setScript("");
        assertUserFailureException(creation, "Script cannot be empty.");
    }

    @Test(dataProvider = "scriptTypes")
    public void testScriptCanNotCompile(PluginType scriptType)
    {
        PluginCreation creation = createBasic();
        creation.setPluginType(scriptType);
        creation.setScript("d:\n");
        assertUserFailureException(creation, "SyntaxError");
    }

    @DataProvider
    Object[][] scriptTypes()
    {
        PluginType[] values = PluginType.values();
        Object[][] result = new Object[values.length][];
        for (int i = 0; i < values.length; i++)
        {
            result[i] = new Object[] { values[i] };
        }
        return result;
    }

    @Test(dataProvider = "usersNotAllowedToCreatePlugins")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PluginCreation creation = createBasic();
                    v3api.createPlugins(sessionToken, Arrays.asList(creation));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginCreation creation = new PluginCreation();
        creation.setName("LOG_TEST_1");
        creation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        creation.setScript("1");

        PluginCreation creation2 = new PluginCreation();
        creation2.setName("LOG_TEST_2");
        creation2.setPluginType(PluginType.MANAGED_PROPERTY);
        creation2.setScript("pass");

        v3api.createPlugins(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-plugins  NEW_PLUGINS('[PluginCreation[pluginType=DYNAMIC_PROPERTY,name=LOG_TEST_1], PluginCreation[pluginType=MANAGED_PROPERTY,name=LOG_TEST_2]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreatePlugins()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    private PluginCreation createBasic()
    {
        PluginCreation creation = new PluginCreation();
        creation.setName("test " + System.currentTimeMillis());
        creation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        creation.setDescription("a test");
        creation.setScript("pass");
        creation.setAvailable(false);
        creation.setEntityKind(EntityKind.EXPERIMENT);
        return creation;
    }

    private void assertUserFailureException(PluginCreation creation, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.createPlugins(sessionToken, Arrays.asList(creation));
                }
            },

                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }
}
