/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreatePersonTest extends AbstractTest
{
    @Test
    public void testCreatePerson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("user-" + System.currentTimeMillis());
        personCreation.setSpaceId(new SpacePermId("CISD"));

        // When
        List<PersonPermId> persons = v3api.createPersons(sessionToken, Arrays.asList(personCreation));

        // Then
        assertEquals(persons.toString(), "[" + personCreation.getUserId() + "]");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, persons, fetchOptions).get(persons.get(0));
        assertEquals(person.getUserId(), personCreation.getUserId());
        assertEquals(person.getRegistrator().getUserId(), TEST_USER);
        assertEquals(person.getSpace().getCode(), "CISD");
    }

    @Test
    public void testCreateExistingPerson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("user-existing");
        // When
        List<PersonPermId> persons = v3api.createPersons(sessionToken, List.of(personCreation));

        // Then
        assertEquals(persons.toString(), "[" + personCreation.getUserId() + "]");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRegistrator();
        Person person = v3api.getPersons(sessionToken, persons, fetchOptions).get(persons.get(0));
        assertEquals(person.getUserId(), personCreation.getUserId());
        assertEquals(person.getRegistrator().getUserId(), TEST_USER);

        // Given
        PersonCreation existingPersonCreation = new PersonCreation();
        existingPersonCreation.setUserId("user-existing");
        // When

        try
        {
            List<PersonPermId> existingPersons = v3api.createPersons(sessionToken, List.of(existingPersonCreation));
        } catch (UserFailureException e)
        {
            assertEquals("User with User Id [" + existingPersonCreation.getUserId() + "] already exists! (Context: [])", e.getMessage());
        }
    }

    @Test(dataProvider = "usersNotAllowedToCreatePersons")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PersonCreation newPerson = new PersonCreation();
                    newPerson.setUserId("newuser");
                    v3api.createPersons(sessionToken, Arrays.asList(newPerson));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation creation = new PersonCreation();
        creation.setUserId("LOG_TEST_1");

        PersonCreation creation2 = new PersonCreation();
        creation2.setUserId("LOG_TEST_2");

        v3api.createPersons(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-persons  NEW_PERSONS('[PersonCreation[userId=LOG_TEST_1], PersonCreation[userId=LOG_TEST_2]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreatePersons()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

}
