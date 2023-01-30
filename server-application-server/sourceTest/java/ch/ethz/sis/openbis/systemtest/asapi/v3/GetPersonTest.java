/*
 * Copyright 2017 ETH Zuerich, SIS
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSetting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.create.WebAppSettingCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.update.WebAppSettingsUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;

/**
 * @author Franz-Josef Elmer
 */
public class GetPersonTest extends AbstractTest
{

    private static final String WEB_APP_1 = "testWebApp1";

    private static final String WEB_APP_2 = "testWebApp2";

    private static final String WEB_APP_3 = "testWebApp3";

    @Test
    public void testGetPersons()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        IPersonId id1 = new PersonPermId("observer");
        IPersonId id2 = new PersonPermId("test_role");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRegistrator();

        // When
        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(id1, id2), fetchOptions);

        // Then
        assertEquals(renderPerson(persons.get(id1)), "observer, home space:CISD, [SPACE_OBSERVER Space TESTGROUP]");
        assertEquals(renderPerson(persons.get(id2)), "test_role, home space:CISD, [SPACE_POWER_USER Space CISD], registrator: test");
    }

    @Test
    public void testGetWithNoWebApps()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonWithWebAppSettings();

        PersonFetchOptions fetchOptions = new PersonFetchOptions();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(persons.size(), 1);

        Person person = persons.get(permId);
        assertWebAppsNotFetched(person);
    }

    @Test
    public void testGetWithChosenWebAppsWithNoSettings()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonWithWebAppSettings();

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withWebAppSettings(WEB_APP_1);
        fetchOptions.withWebAppSettings(WEB_APP_2);

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(persons.size(), 1);

        Person person = persons.get(permId);
        assertWebAppsFetched(person, 2);

        assertWebAppFetched(person, WEB_APP_1);
        assertWebAppFetched(person, WEB_APP_2);
        assertWebAppNotFetched(person, WEB_APP_3);

        assertSettingsNotFetched(person, WEB_APP_1);
        assertSettingNotFetched(person, WEB_APP_1, "n");
        assertSettingNotFetched(person, WEB_APP_1, "n2");

        assertSettingsNotFetched(person, WEB_APP_2);
        assertSettingNotFetched(person, WEB_APP_2, "n");
        assertSettingNotFetched(person, WEB_APP_2, "n3");
    }

    @Test
    public void testGetWithChosenWebAppsWithAllSettings()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonWithWebAppSettings();

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withWebAppSettings(WEB_APP_1).withAllSettings();
        fetchOptions.withWebAppSettings(WEB_APP_2).withAllSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(persons.size(), 1);

        Person person = persons.get(permId);
        assertWebAppsFetched(person, 2);

        assertWebAppFetched(person, WEB_APP_1);
        assertWebAppFetched(person, WEB_APP_2);
        assertWebAppNotFetched(person, WEB_APP_3);

        assertSettingsFetched(person, WEB_APP_1, 2);
        assertSettingFetched(person, WEB_APP_1, "n", "some value");
        assertSettingFetched(person, WEB_APP_1, "n2", "v2");

        assertSettingsFetched(person, WEB_APP_2, 2);
        assertSettingFetched(person, WEB_APP_2, "n", "different value");
        assertSettingFetched(person, WEB_APP_2, "n3", "v3");
    }

    @Test
    public void testGetWithChosenWebAppsWithChosenSettings()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonWithWebAppSettings();

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withWebAppSettings(WEB_APP_1).withSetting("n");
        fetchOptions.withWebAppSettings(WEB_APP_1).withSetting("n2");
        fetchOptions.withWebAppSettings(WEB_APP_2).withSetting("n3");

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(persons.size(), 1);

        Person person = persons.get(permId);
        assertWebAppsFetched(person, 2);

        assertWebAppFetched(person, WEB_APP_1);
        assertWebAppFetched(person, WEB_APP_2);
        assertWebAppNotFetched(person, WEB_APP_3);

        assertSettingsFetched(person, WEB_APP_1, 2);
        assertSettingFetched(person, WEB_APP_1, "n", "some value");
        assertSettingFetched(person, WEB_APP_1, "n2", "v2");

        assertSettingsFetched(person, WEB_APP_2, 1);
        assertSettingNotFetched(person, WEB_APP_2, "n");
        assertSettingFetched(person, WEB_APP_2, "n3", "v3");
    }

    @Test
    public void testGetWithAllWebApps()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonWithWebAppSettings();

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(persons.size(), 1);

        Person person = persons.get(permId);
        assertWebAppsFetched(person, 3);

        assertWebAppFetched(person, WEB_APP_1);
        assertWebAppFetched(person, WEB_APP_2);
        assertWebAppFetched(person, WEB_APP_3);

        assertSettingsFetched(person, WEB_APP_1, 2);
        assertSettingFetched(person, WEB_APP_1, "n", "some value");
        assertSettingFetched(person, WEB_APP_1, "n2", "v2");

        assertSettingsFetched(person, WEB_APP_2, 2);
        assertSettingFetched(person, WEB_APP_2, "n", "different value");
        assertSettingFetched(person, WEB_APP_2, "n3", "v3");

        assertSettingsFetched(person, WEB_APP_3, 1);
        assertSettingFetched(person, WEB_APP_3, "n4", "v4");
    }

    @Test
    public void testGetWithWebAppsOfMyOwnAndOfDifferentPersonAsInstanceAdmin()
    {
        PersonPermId permId1 = createPersonWithWebAppSettings("person_with_web_app_settings_1", null, true, true, false);
        PersonPermId permId2 = createPersonWithWebAppSettings("person_with_web_app_settings_2", Role.ADMIN, false, false, true);

        String sessionToken = v3api.login(permId2.getPermId(), PASSWORD);

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);
        assertEquals(persons.size(), 2);

        Person person1 = persons.get(permId1);
        Person person2 = persons.get(permId2);

        // person 1

        assertWebAppsFetched(person1, 2);

        assertWebAppFetched(person1, WEB_APP_1);
        assertWebAppFetched(person1, WEB_APP_2);
        assertWebAppNull(person1, WEB_APP_3);

        assertSettingsFetched(person1, WEB_APP_1, 2);
        assertSettingFetched(person1, WEB_APP_1, "n", "some value");
        assertSettingFetched(person1, WEB_APP_1, "n2", "v2");

        assertSettingsFetched(person1, WEB_APP_2, 2);
        assertSettingFetched(person1, WEB_APP_2, "n", "different value");
        assertSettingFetched(person1, WEB_APP_2, "n3", "v3");

        // person 2

        assertWebAppsFetched(person2, 1);

        assertWebAppNull(person2, WEB_APP_1);
        assertWebAppNull(person2, WEB_APP_2);
        assertWebAppFetched(person2, WEB_APP_3);

        assertSettingsFetched(person2, WEB_APP_3, 1);
        assertSettingFetched(person2, WEB_APP_3, "n4", "v4");
    }

    @Test
    public void testGetWithWebAppsOfMyOwnAndOfDifferentPersonAsNonInstanceAdmin()
    {
        PersonPermId permId1 = createPersonWithWebAppSettings("person_with_web_app_settings_1", null, true, true, false);
        PersonPermId permId2 = createPersonWithWebAppSettings("person_with_web_app_settings_2", Role.OBSERVER, false, false, true);

        String sessionToken = v3api.login(permId2.getPermId(), PASSWORD);

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);
        assertEquals(persons.size(), 2);

        Person person1 = persons.get(permId1);
        Person person2 = persons.get(permId2);

        // person 1

        assertWebAppsNull(person1);

        // person 2

        assertWebAppsFetched(person2, 1);

        assertWebAppNull(person2, WEB_APP_1);
        assertWebAppNull(person2, WEB_APP_2);
        assertWebAppFetched(person2, WEB_APP_3);

        assertSettingsFetched(person2, WEB_APP_3, 1);
        assertSettingFetched(person2, WEB_APP_3, "n4", "v4");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withSpace();
        fo.withRoleAssignments();

        v3api.getPersons(sessionToken, Arrays.asList(new PersonPermId("observer"), new PersonPermId("test_role")), fo);

        assertAccessLog(
                "get-persons  IDS('[observer, test_role]') FETCH_OPTIONS('Person\n    with Space\n    with RoleAssignments\n')");
    }

    private PersonPermId createPersonWithWebAppSettings()
    {
        return createPersonWithWebAppSettings("person_with_web_app_settings", null, true, true, true);
    }

    private PersonPermId createPersonWithWebAppSettings(String userId, Role userRole, boolean hasApp1, boolean hasApp2, boolean hasApp3)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation creation = new PersonCreation();
        creation.setUserId(userId);

        List<PersonPermId> permIds = v3api.createPersons(sessionToken, Arrays.asList(creation));

        if (hasApp1 || hasApp2 || hasApp3)
        {
            PersonUpdate update = new PersonUpdate();
            update.setUserId(permIds.get(0));

            if (hasApp1)
            {
                WebAppSettingsUpdateValue webApp1 = update.getWebAppSettings(WEB_APP_1);
                webApp1.add(new WebAppSettingCreation("n", "some value"));
                webApp1.add(new WebAppSettingCreation("n2", "v2"));
            }

            if (hasApp2)
            {
                WebAppSettingsUpdateValue webApp2 = update.getWebAppSettings(WEB_APP_2);
                webApp2.add(new WebAppSettingCreation("n", "different value"));
                webApp2.add(new WebAppSettingCreation("n3", "v3"));
            }

            if (hasApp3)
            {
                WebAppSettingsUpdateValue webApp3 = update.getWebAppSettings(WEB_APP_3);
                webApp3.add(new WebAppSettingCreation("n4", "v4"));
            }

            v3api.updatePersons(sessionToken, Arrays.asList(update));
        }

        if (userRole != null)
        {
            RoleAssignmentCreation role = new RoleAssignmentCreation();
            role.setUserId(permIds.get(0));
            role.setRole(userRole);

            v3api.createRoleAssignments(sessionToken, Arrays.asList(role));
        }

        return permIds.get(0);
    }

    private void assertWebAppsFetched(Person person, int count)
    {
        Map<String, WebAppSettings> settings = person.getWebAppSettings();
        assertNotNull(settings);
        assertEquals(settings.size(), count);
    }

    private void assertWebAppsNull(Person person)
    {
        Map<String, WebAppSettings> settings = person.getWebAppSettings();
        assertNull(settings);
    }

    private void assertWebAppsNotFetched(Person person)
    {
        try
        {
            person.getWebAppSettings();
            fail();
        } catch (NotFetchedException e)
        {
            assertEquals(e.getMessage(), "Settings have not been fetched.");
        }
    }

    private void assertWebAppFetched(Person person, String webAppId)
    {
        WebAppSettings settings = person.getWebAppSettings(webAppId);
        assertNotNull(settings);
        assertEquals(settings.getWebAppId(), webAppId);
    }

    private void assertWebAppNull(Person person, String webAppId)
    {
        WebAppSettings settings = person.getWebAppSettings(webAppId);
        assertNull(settings);
    }

    private void assertWebAppNotFetched(Person person, String webAppId)
    {
        try
        {
            person.getWebAppSettings(webAppId);
            fail();
        } catch (NotFetchedException e)
        {
            assertEquals(e.getMessage(), "Settings for web app '" + webAppId + "' have not been fetched.");
        }
    }

    private void assertSettingsFetched(Person person, String webAppId, int count)
    {
        Map<String, WebAppSetting> settings = person.getWebAppSettings(webAppId).getSettings();
        assertNotNull(settings);
        assertEquals(settings.size(), count);
    }

    private void assertSettingsNotFetched(Person person, String webAppId)
    {
        WebAppSettings settings = person.getWebAppSettings(webAppId);

        try
        {
            settings.getSettings();
            fail();
        } catch (NotFetchedException e)
        {
            assertEquals(e.getMessage(), "Settings have not been fetched.");
        }
    }

    private void assertSettingFetched(Person person, String webAppId, String name, String expectedValue)
    {
        WebAppSetting setting = person.getWebAppSettings(webAppId).getSetting(name);
        assertEquals(setting.getName(), name);
        assertEquals(setting.getValue(), expectedValue);
    }

    private void assertSettingNotFetched(Person person, String webAppId, String name)
    {
        WebAppSettings settings = person.getWebAppSettings(webAppId);

        try
        {
            settings.getSetting(name);
            fail();
        } catch (NotFetchedException e)
        {
            assertEquals(e.getMessage(), "Setting '" + name + "' has not been fetched.");
        }
    }

}
