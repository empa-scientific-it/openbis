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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.collection.SimpleComparator;

/**
 * @author Franz-Josef Elmer
 */
public class CreateAuthorizationGroupTest extends AbstractTest
{
    static final Comparator<Person> PERSON_COMPARATOR = new SimpleComparator<Person, String>()
        {
            @Override
            public String evaluate(Person item)
            {
                return item.getUserId();
            }
        };

    @Test
    public void testCreateAuthorizationGroupWithUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupCreation newGroup = new AuthorizationGroupCreation();
        newGroup.setCode("MY_TEST_GROUP");
        newGroup.setDescription("Testing");
        newGroup.setUserIds(Arrays.asList(new PersonPermId(TEST_OBSERVER_CISD), new Me()));

        // When
        List<AuthorizationGroupPermId> groups = v3api.createAuthorizationGroups(sessionToken, Arrays.asList(newGroup));

        // Then
        assertEquals(groups.get(0).getPermId(), newGroup.getCode());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withUsers();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, groups, fetchOptions).get(groups.get(0));
        assertEquals(group.getCode(), newGroup.getCode());
        assertEquals(group.getDescription(), newGroup.getDescription());
        assertEquals(group.getRegistrator().getUserId(), TEST_USER);
        List<Person> users = group.getUsers();
        Collections.sort(users, PERSON_COMPARATOR);
        assertEquals(users.toString(), "[Person observer_cisd, Person test]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateAuthorizationGroupWithInvalidCode()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupCreation newGroup = new AuthorizationGroupCreation();
        newGroup.setCode("invalid code");
        newGroup.setDescription("Testing");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.createAuthorizationGroups(sessionToken, Arrays.asList(newGroup));

                }
            },
                // Then
                "Given code 'invalid code' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .");

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateAuthorizationGroupWithNoUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupCreation newGroup = new AuthorizationGroupCreation();
        newGroup.setCode("NEW_GROUP");
        newGroup.setDescription("testCreateAuthorizationGroupWithNoUsers");

        // When
        List<AuthorizationGroupPermId> groups = v3api.createAuthorizationGroups(sessionToken, Arrays.asList(newGroup));

        // Then
        assertEquals(groups.get(0).getPermId(), newGroup.getCode());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withUsers();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, groups, fetchOptions).get(groups.get(0));
        assertEquals(group.getPermId().getPermId(), newGroup.getCode());
        assertEquals(group.getCode(), newGroup.getCode());
        assertEquals(group.getDescription(), newGroup.getDescription());
        assertEquals(group.getRegistrator().getUserId(), TEST_USER);
        List<Person> users = group.getUsers();
        Collections.sort(users, PERSON_COMPARATOR);
        assertEquals(users.toString(), "[]");

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "usersNotAllowedToCreateAuthorizationGroups")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    AuthorizationGroupCreation newGroup = new AuthorizationGroupCreation();
                    newGroup.setCode("NEW_GROUP");
                    v3api.createAuthorizationGroups(sessionToken, Arrays.asList(newGroup));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode("LOG_TEST_1");

        AuthorizationGroupCreation creation2 = new AuthorizationGroupCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createAuthorizationGroups(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-authorization-groups  NEW_AUTHORIZATION_GROUPS('[AuthorizationGroupCreation[code=LOG_TEST_1], AuthorizationGroupCreation[code=LOG_TEST_2]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreateAuthorizationGroups()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

}
