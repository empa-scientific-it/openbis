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
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdateAuthorizationGroupTest extends AbstractTest
{
    @Test
    public void testUpdateDescription()
    {
        // Given
        String sessionToken = v3api.login("instance_admin", PASSWORD);
        AuthorizationGroupUpdate update = new AuthorizationGroupUpdate();
        AuthorizationGroupPermId id = new AuthorizationGroupPermId("AGROUP");
        update.setAuthorizationGroupId(id);
        update.setDescription("a new description");

        // When
        v3api.updateAuthorizationGroups(sessionToken, Arrays.asList(update));

        // Then
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(group.getDescription(), update.getDescription().getValue());
        long diff = group.getModificationDate().getTime() - group.getRegistrationDate().getTime();
        assertTrue(diff > 10000, "modification date (" + group.getModificationDate()
                + ") is larger than registration date (" + group.getRegistrationDate() + ").");
        assertEquals(group.getRegistrator().getUserId(), "test");

        v3api.logout(sessionToken);
    }

    @Test
    public void testAddUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupUpdate update = new AuthorizationGroupUpdate();
        AuthorizationGroupPermId id = new AuthorizationGroupPermId("AGROUP");
        update.setAuthorizationGroupId(id);
        update.getUserIds().add(new PersonPermId(TEST_GROUP_ADMIN));
        update.getUserIds().add(new PersonPermId(TEST_GROUP_POWERUSER));
        update.getUserIds().add(new Me());

        // When
        v3api.updateAuthorizationGroups(sessionToken, Arrays.asList(update));

        // Then
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        List<Person> users = group.getUsers();
        Collections.sort(users, CreateAuthorizationGroupTest.PERSON_COMPARATOR);
        assertEquals(users.toString(), "[Person admin, Person agroup_member, Person poweruser, Person test]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testRemoveUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupUpdate update = new AuthorizationGroupUpdate();
        AuthorizationGroupPermId id = new AuthorizationGroupPermId("AGROUP");
        update.setAuthorizationGroupId(id);
        update.getUserIds().remove(new PersonPermId("agroup_member"));
        update.getUserIds().remove(new PersonPermId(TEST_NO_HOME_SPACE));

        // When
        v3api.updateAuthorizationGroups(sessionToken, Arrays.asList(update));

        // Then
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        List<Person> users = group.getUsers();
        Collections.sort(users, CreateAuthorizationGroupTest.PERSON_COMPARATOR);
        assertEquals(users.toString(), "[]");

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "usersNotAllowedToUpdateAuthorizationGroups")
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    AuthorizationGroupUpdate update = new AuthorizationGroupUpdate();
                    AuthorizationGroupPermId id = new AuthorizationGroupPermId("AGROUP");
                    update.setAuthorizationGroupId(id);
                    update.setDescription("a new description");

                    // When
                    v3api.updateAuthorizationGroups(sessionToken, Arrays.asList(update));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AuthorizationGroupUpdate update = new AuthorizationGroupUpdate();
        update.setAuthorizationGroupId(new AuthorizationGroupPermId("AGROUP"));

        v3api.updateAuthorizationGroups(sessionToken, Arrays.asList(update));

        assertAccessLog(
                "update-authorization-groups  AUTHORIZATION_GROUP_UPDATES('[AuthorizationGroupUpdate[groupId=AGROUP]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdateAuthorizationGroups()
    {
        String[] users = { TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER,
                TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER };
        Object[][] objects = new Object[users.length][];
        for (int i = 0; i < users.length; i++)
        {
            objects[i] = new Object[] { users[i] };
        }
        return objects;
    }

}
