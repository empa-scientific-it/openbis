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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSetting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.create.WebAppSettingCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.update.WebAppSettingsUpdateValue;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdatePersonTest extends AbstractTest
{

    private static final String WEB_APP_1 = "testWebApp1";

    private static final String WEB_APP_2 = "testWebApp2";

    private static final String WEB_APP_3 = "testWebApp3";

    private static final String WEB_APP_4 = "testWebApp4";

    @Test
    public void testUpdateUnaccesableHomeSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("user-" + System.currentTimeMillis());
        PersonPermId personPermId = v3api.createPersons(sessionToken, Arrays.asList(personCreation)).get(0);
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personPermId);
        personUpdate.setSpaceId(new SpacePermId("TEST-SPACE"));

        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));

        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personPermId), fetchOptions).get(personPermId);
        assertEquals(person.getRoleAssignments().size(), 0);
        assertEquals(person.getPermId().getPermId(), personPermId.getPermId());
        assertEquals(person.getSpace().getCode(), "TEST-SPACE");
    }

    @Test
    public void testSpaceAdminUpdatesHomeSpaceOfAUserInSameSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId("homeless");
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(new SpacePermId("TESTGROUP"));

        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));

        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.getSpace().getCode(), "TESTGROUP");
    }

    @Test
    public void testSpaceObserverUpdatesHomeSpaceOfAUserInSameSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId("homeless");
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(new SpacePermId("CISD"));

        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
                }
            });
    }

    @Test
    public void testSpaceObserverUpdatesItselfToSameHomeSpaceForNotSpecifiedPerson()
    {
        // Given
        String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setSpaceId(new SpacePermId("TESTGROUP"));

        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));

        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        PersonPermId personId = new PersonPermId(TEST_GROUP_OBSERVER);
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.getSpace().getCode(), "TESTGROUP");
    }

    @Test
    public void testSpaceObserverUpdatesItselfToSameHomeSpaceForMe()
    {
        // Given
        String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        IPersonId personId = new Me();
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(new SpacePermId("TESTGROUP"));

        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));

        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), TEST_GROUP_OBSERVER);
        assertEquals(person.getSpace().getCode(), "TESTGROUP");
    }

    @Test
    public void testDeactivateUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId(TEST_GROUP_OBSERVER);
        personUpdate.setUserId(personId);
        personUpdate.deactivate();

        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));

        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.isActive(), Boolean.FALSE);
    }

    @Test
    public void testDeactivateYourself()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId(TEST_USER);
        personUpdate.setUserId(personId);
        personUpdate.deactivate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
                }
                // Then
            }, "You can not deactivate yourself. Ask another instance admin to do that for you.");
    }

    @Test(dataProvider = "usersNotAllowedToDeactivateUsers")
    public void testDeactivateUserWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    PersonUpdate personUpdate = new PersonUpdate();
                    personUpdate.setUserId(new PersonPermId(TEST_GROUP_OBSERVER));
                    personUpdate.deactivate();

                    // When
                    v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
                }
            });
    }

    @Test
    public void testActivateUser()
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        // create a new user with an observer role

        PersonCreation userCreation = new PersonCreation();
        userCreation.setUserId("USER_TO_DEACTIVATE_AND_ACTIVATE");

        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(new PersonPermId(userCreation.getUserId()));
        roleCreation.setRole(Role.OBSERVER);

        PersonPermId userId = v3api.createPersons(adminSessionToken, Arrays.asList(userCreation)).get(0);
        v3api.createRoleAssignments(adminSessionToken, Arrays.asList(roleCreation));

        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments();

        Person user = v3api.getPersons(adminSessionToken, Arrays.asList(userId), fetchOptions).get(userId);

        assertEquals(user.isActive(), Boolean.TRUE);
        assertEquals(user.getRoleAssignments().size(), 1);

        String userSessionToken = v3api.login(userCreation.getUserId(), PASSWORD);
        assertNotNull(userSessionToken);

        // deactivate the user (active flag is set to false, all rights are cleared)

        PersonUpdate userDeactivateUpdate = new PersonUpdate();
        userDeactivateUpdate.setUserId(userId);
        userDeactivateUpdate.deactivate();

        v3api.updatePersons(adminSessionToken, Arrays.asList(userDeactivateUpdate));

        Person deactivatedUser = v3api.getPersons(adminSessionToken, Arrays.asList(userId), fetchOptions).get(userId);

        assertEquals(deactivatedUser.isActive(), Boolean.FALSE);
        assertEquals(deactivatedUser.getRoleAssignments().size(), 0);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.login(userCreation.getUserId(), PASSWORD);
                }
            }, "User 'USER_TO_DEACTIVATE_AND_ACTIVATE' has no role assignments and thus is not permitted to login");

        // assign roles to the deactivated user and try to login

        v3api.createRoleAssignments(adminSessionToken, Arrays.asList(roleCreation));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.login(userCreation.getUserId(), PASSWORD);
                }
            }, "User 'USER_TO_DEACTIVATE_AND_ACTIVATE' has been deactivated and thus is not permitted to login");

        // activate the user again (active flag is set to true)

        PersonUpdate userActivateUpdate = new PersonUpdate();
        userActivateUpdate.setUserId(userId);
        userActivateUpdate.activate();

        v3api.updatePersons(adminSessionToken, Arrays.asList(userActivateUpdate));

        Person activatedUser = v3api.getPersons(adminSessionToken, Arrays.asList(userId), fetchOptions).get(userId);

        assertEquals(activatedUser.isActive(), Boolean.TRUE);
        assertEquals(activatedUser.getRoleAssignments().size(), 1);

        String activatedUserSessionToken = v3api.login(userCreation.getUserId(), PASSWORD);
        assertNotNull(activatedUserSessionToken);
    }

    @Test
    public void testUpdateWebApps()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonToUpdate();

        // Other tests use WEB_APP_1
        PersonFetchOptions personFetchOptions = new PersonFetchOptions();
        personFetchOptions.withWebAppSettings(WEB_APP_1).withAllSettings();
        Person currentPerson = v3api.getPersons(sessionToken, Arrays.asList(permId), personFetchOptions).get(permId);
        WebAppSettings currentWebApp1Settings = currentPerson.getWebAppSettings(WEB_APP_1);
        int currentWebApp1Size = 0;
        if (currentWebApp1Settings != null)
        {
            currentWebApp1Size = currentWebApp1Settings.getSettings().size();
        }
        //

        // 1st update

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);

        WebAppSettingsUpdateValue webApp1Update = update.getWebAppSettings(WEB_APP_1);
        webApp1Update.add(new WebAppSettingCreation("n1a", "v1a"));
        webApp1Update.add(new WebAppSettingCreation("n1b", "v1b"));

        WebAppSettingsUpdateValue webApp2Update = update.getWebAppSettings(WEB_APP_2);
        webApp2Update.add(new WebAppSettingCreation("n2a", "v2a"));

        WebAppSettingsUpdateValue webApp3Update = update.getWebAppSettings(WEB_APP_3);
        webApp3Update.add(new WebAppSettingCreation("n3a", "v3a"));

        WebAppSettingsUpdateValue webApp4Update = update.getWebAppSettings(WEB_APP_4);
        webApp4Update.add(new WebAppSettingCreation("n4a", "v4a"));

        v3api.updatePersons(sessionToken, Arrays.asList(update));

        // 1st assert

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fo);
        Person person = persons.get(permId);

        assertEquals(person.getWebAppSettings().size(), 4);

        Map<String, WebAppSetting> webApp1 = person.getWebAppSettings(WEB_APP_1).getSettings();
        assertEquals(webApp1.size(), 2 + currentWebApp1Size);
        assertEquals(webApp1.get("n1a").getValue(), "v1a");
        assertEquals(webApp1.get("n1b").getValue(), "v1b");

        Map<String, WebAppSetting> webApp2 = person.getWebAppSettings(WEB_APP_2).getSettings();
        assertEquals(webApp2.size(), 1);
        assertEquals(webApp2.get("n2a").getValue(), "v2a");

        Map<String, WebAppSetting> webApp3 = person.getWebAppSettings(WEB_APP_3).getSettings();
        assertEquals(webApp3.size(), 1);
        assertEquals(webApp3.get("n3a").getValue(), "v3a");

        Map<String, WebAppSetting> webApp4 = person.getWebAppSettings(WEB_APP_4).getSettings();
        assertEquals(webApp4.size(), 1);
        assertEquals(webApp4.get("n4a").getValue(), "v4a");

        // 2nd update

        update = new PersonUpdate();
        update.setUserId(permId);

        webApp1Update = update.getWebAppSettings(WEB_APP_1);
        webApp1Update.add(new WebAppSettingCreation("n1c", "v1c"));
        webApp1Update.remove("n1b");

        webApp2Update = update.getWebAppSettings(WEB_APP_2);
        webApp2Update.set(new WebAppSettingCreation("n2a", "v2a updated"), new WebAppSettingCreation("n2c", "v2c"));

        webApp3Update = update.getWebAppSettings(WEB_APP_3);
        webApp3Update.set();

        webApp4Update = update.getWebAppSettings(WEB_APP_4);
        webApp4Update.remove("n4a");

        v3api.updatePersons(sessionToken, Arrays.asList(update));

        // 2nd assert

        persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fo);
        person = persons.get(permId);

        assertEquals(person.getWebAppSettings().size(), 2);

        webApp1 = person.getWebAppSettings(WEB_APP_1).getSettings();
        assertEquals(webApp1.size(), 2 + currentWebApp1Size);
        assertEquals(webApp1.get("n1a").getValue(), "v1a");
        assertEquals(webApp1.get("n1c").getValue(), "v1c");

        webApp2 = person.getWebAppSettings(WEB_APP_2).getSettings();
        assertEquals(webApp2.size(), 2);
        assertEquals(webApp2.get("n2a").getValue(), "v2a updated");
        assertEquals(webApp2.get("n2c").getValue(), "v2c");
    }

    @Test
    public void testUpdateWebAppsOfMyOwn()
    {
        String sessionToken = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);

        PersonPermId permId = new PersonPermId(TEST_INSTANCE_OBSERVER);

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);
        update.getWebAppSettings(WEB_APP_1).set(new WebAppSettingCreation("testName", "testValue"));

        v3api.updatePersons(sessionToken, Arrays.asList(update));

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fo);
        Person person = persons.get(permId);

        assertEquals(person.getWebAppSettings(WEB_APP_1).getSetting("testName").getValue(), "testValue");
    }

    @Test
    public void testUpdateWebAppsOfDifferentPersonAsInstanceAdmin()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);
        update.getWebAppSettings(WEB_APP_1).set(new WebAppSettingCreation("testName", "testValue"));

        v3api.updatePersons(sessionToken, Arrays.asList(update));

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withAllWebAppSettings();

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(permId), fo);
        Person person = persons.get(permId);

        assertEquals(person.getWebAppSettings(WEB_APP_1).getSetting("testName").getValue(), "testValue");
    }

    @Test
    public void testUpdateWebAppsOfDifferentPersonAsNonInstanceAdmin()
    {
        String sessionToken = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);

        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);
        update.getWebAppSettings(WEB_APP_1).set(new WebAppSettingCreation("testName", "testValue"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePersons(sessionToken, Arrays.asList(update));
                }
            }, permId);
    }

    @Test
    public void testUpdateWebAppsWithWebAppNull()
    {
        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);
        update.getWebAppSettings(null);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePersons(sessionToken, Arrays.asList(update));
                }
            }, "Web app id cannot be null");
    }

    @Test
    public void testUpdateWebAppsWithAddSettingNameNull()
    {
        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);

        WebAppSettingsUpdateValue webAppUpdate = update.getWebAppSettings(WEB_APP_1);
        webAppUpdate.add(new WebAppSettingCreation(null, "test value"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePersons(sessionToken, Arrays.asList(update));
                }
            }, "Web app setting name cannot be null");
    }

    @Test
    public void testUpdateWebAppsWithRemoveSettingNameNull()
    {
        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);

        WebAppSettingsUpdateValue webAppUpdate = update.getWebAppSettings(WEB_APP_1);
        webAppUpdate.remove((String) null);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePersons(sessionToken, Arrays.asList(update));
                }
            }, "Web app setting name cannot be null");
    }

    @Test
    public void testUpdateWebAppsWithSetSettingNameNull()
    {
        PersonPermId permId = createPersonToUpdate();

        PersonUpdate update = new PersonUpdate();
        update.setUserId(permId);

        WebAppSettingsUpdateValue webAppUpdate = update.getWebAppSettings(WEB_APP_1);
        webAppUpdate.set(new WebAppSettingCreation(null, "test value"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePersons(sessionToken, Arrays.asList(update));
                }
            }, "Web app setting name cannot be null");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonUpdate update = new PersonUpdate();
        update.setUserId(new PersonPermId("observer"));

        PersonUpdate update2 = new PersonUpdate();
        update2.setUserId(new PersonPermId("test_role"));

        v3api.updatePersons(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-persons  PERSON_UPDATES('[PersonUpdate[userId=observer], PersonUpdate[userId=test_role]]')");
    }

    private PersonPermId createPersonToUpdate()
    {
        PersonCreation creation = new PersonCreation();
        creation.setUserId("person_to_update");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<PersonPermId> permIds = v3api.createPersons(sessionToken, Arrays.asList(creation));

        return permIds.get(0);
    }

    @DataProvider
    Object[][] usersNotAllowedToDeactivateUsers()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
}
