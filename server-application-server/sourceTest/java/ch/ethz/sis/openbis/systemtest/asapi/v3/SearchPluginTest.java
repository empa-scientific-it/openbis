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
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author Franz-Josef Elmer
 */
public class SearchPluginTest extends AbstractTest
{
    @Test
    public void testSearchWithId()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        PluginPermId pluginPermId = new PluginPermId("propertiesSAMPLE");
        searchCriteria.withId().thatEquals(pluginPermId);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.get(0).getPermId().getPermId(), pluginPermId.getPermId());
        assertEquals(plugins.get(0).getName(), pluginPermId.getPermId());
        assertEquals(plugins.get(0).getPluginType(), PluginType.DYNAMIC_PROPERTY);
        assertEquals(plugins.get(0).getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugins.get(0).getEntityKinds(), EnumSet.of(EntityKind.SAMPLE));
        assertEquals(plugins.get(0).getFetchOptions().hasScript(), false);
        assertEquals(plugins.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIds()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        PluginPermId id1 = new PluginPermId("validateUpdateFAIL");
        PluginPermId id2 = new PluginPermId("validateOK");
        searchCriteria.withIds().thatIn(Arrays.asList(id1, id2));
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.sortBy().name().desc();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(),
                "[validateUpdateFAIL, validateOK]");
        assertEquals(plugins.get(0).getPermId().getPermId(), id1.getPermId());
        assertEquals(plugins.get(0).getName(), id1.getPermId());
        assertEquals(plugins.get(0).getPluginType(), PluginType.ENTITY_VALIDATION);
        assertEquals(plugins.get(0).getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugins.get(0).getEntityKinds(), EnumSet.allOf(EntityKind.class));
        assertEquals(plugins.get(0).getFetchOptions().hasScript(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithName()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withName().thatContains("ida");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.sortBy().name().desc();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(),
                "[validateUpdateFAIL, validateOK, validateFAIL, validateChildren]");
        assertEquals(plugins.get(0).getPluginType(), PluginType.ENTITY_VALIDATION);
        assertEquals(plugins.get(0).getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugins.get(0).getFetchOptions().hasScript(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithNameAndPluginType()
    {
        // Given
        String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withName().thatContains("date");
        searchCriteria.withPluginType().thatEquals(PluginType.DYNAMIC_PROPERTY);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        fetchOptions.sortBy().name().asc();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(),
                "[code_date, date]");
        assertEquals(plugins.get(0).getPluginType(), PluginType.DYNAMIC_PROPERTY);
        assertEquals(plugins.get(0).getPluginKind(), PluginKind.JYTHON);
        assertEquals(plugins.get(0).getFetchOptions().hasScript(), true);
        assertEquals(plugins.get(0).getScript(), "\"%s %s\" % (entity.code(), str(currentDate().getTime()))");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPluginKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withPluginKind().thatEquals(PluginKind.PREDEPLOYED);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(), "[]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithUnspecifiedPluginKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withPluginKind();
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.sortBy().name();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        AssertionUtil.assertCollectionContainsAtLeast(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()),
                "code", "code_date", "date", "managed list", "properties", "propertiesEXPERIMENT", "propertiesSAMPLE", "test", ""
                        + "testEXPERIMENT",
                "testSAMPLE", "validateChildren", "validateFAIL", "validateOK", "validateUpdateFAIL", "waitOK");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithUnspecifiedPluginType()
    {
        // Given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withPluginType();
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.sortBy().name();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        AssertionUtil.assertCollectionContainsAtLeast(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()),
                "code", "code_date", "date", "managed list", "properties", "propertiesEXPERIMENT", "propertiesSAMPLE", "test", ""
                        + "testEXPERIMENT",
                "testSAMPLE", "validateChildren", "validateFAIL", "validateOK", "validateUpdateFAIL", "waitOK");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginSearchCriteria c = new PluginSearchCriteria();
        c.withName().thatEquals("validateOK");

        PluginFetchOptions fo = new PluginFetchOptions();
        fo.withRegistrator();
        fo.withScript();

        v3api.searchPlugins(sessionToken, c, fo);

        assertAccessLog(
                "search-plugins  SEARCH_CRITERIA:\n'PLUGIN\n    with attribute 'name' equal to 'validateOK'\n'\nFETCH_OPTIONS:\n'Plugin\n    with Registrator\n    with Script\n'");
    }

}
