/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
@Test
public class CreatePersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    @Test
    public void testCreate()
    {
        PersonalAccessTokenCreation creation = tokenCreation();

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, creation);

        assertNotNull(token.getHash());
        assertNotNull(token.getPermId());
        assertEquals(token.getSessionName(), creation.getSessionName());
        assertEquals(token.getOwner().getUserId(), TEST_USER);
        assertEquals(token.getRegistrator().getUserId(), TEST_USER);
        assertEquals(token.getModifier().getUserId(), TEST_USER);
        assertEquals(token.getValidFromDate(), creation.getValidFromDate());
        assertEquals(token.getValidToDate(), creation.getValidToDate());
        assertToday(token.getRegistrationDate());
        assertToday(token.getModificationDate());
        assertNull(token.getAccessDate());
    }

    @Test
    public void testCreateWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<PersonalAccessTokenPermId> ids = v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(tokenCreation()));

        assertEquals(ids.size(), 1);
    }

    @Test
    public void testCreateWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createPersonalAccessTokens(token.getHash(), Arrays.asList(tokenCreation()));
            }
        }, "Personal access tokens cannot be used to manage personal access tokens");
    }

    @Test
    public void testCreateWithOwnerIdNull()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(null);

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, creation);

        assertEquals(token.getOwner().getUserId(), TEST_USER);
    }

    @Test
    public void testCreateWithOwnerIdSetToMyselfAsInstanceAdmin()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(INSTANCE_ADMIN_USER));

        PersonalAccessToken token = createToken(INSTANCE_ADMIN_USER, PASSWORD, creation);

        assertEquals(token.getOwner().getUserId(), INSTANCE_ADMIN_USER);
    }

    @Test
    public void testCreateWithOwnerIdSetToMyselfAsRegularUser()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken token = createToken(TEST_GROUP_OBSERVER, PASSWORD, creation);

        assertEquals(token.getOwner().getUserId(), TEST_GROUP_OBSERVER);
    }

    @Test
    public void testCreateWithOwnerIdSetToSomebodyElseAsInstanceAdmin()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken token = createToken(INSTANCE_ADMIN_USER, PASSWORD, creation);

        assertEquals(token.getOwner().getUserId(), TEST_GROUP_OBSERVER);
    }

    @Test
    public void testCreateWithOwnerIdSetToSomebodyElseAsRegularUser()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                PersonalAccessTokenCreation creation = tokenCreation();
                creation.setOwnerId(new PersonPermId(TEST_USER));

                createToken(TEST_GROUP_OBSERVER, PASSWORD, creation);
            }
        }, "User '" + TEST_GROUP_OBSERVER + "' is not allowed to create a personal access token for user '" + TEST_USER + "'");
    }

    @Test
    public void testCreateWithSessionNameNull()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                PersonalAccessTokenCreation creation = tokenCreation();
                creation.setSessionName(null);
                createToken(TEST_USER, PASSWORD, creation);
            }
        }, "Session name cannot be empty");
    }

    @Test
    public void testCreateWithValidFromNull()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                PersonalAccessTokenCreation creation = tokenCreation();
                creation.setValidFromDate(null);
                createToken(TEST_USER, PASSWORD, creation);
            }
        }, "Valid from date cannot be null");
    }

    @Test
    public void testCreateWithValidToNull()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                PersonalAccessTokenCreation creation = tokenCreation();
                creation.setValidToDate(null);
                createToken(TEST_USER, PASSWORD, creation);
            }
        }, "Valid to date cannot be null");
    }

    @Test
    public void testCreateWithValidFromAfterValidTo()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                PersonalAccessTokenCreation creation = tokenCreation();
                creation.setValidFromDate(new Date(2));
                creation.setValidToDate(new Date(1));
                createToken(TEST_USER, PASSWORD, creation);
            }
        }, "Valid from date cannot be after valid to date");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));
        creation.setSessionName("LOG_TEST_1");

        PersonalAccessTokenCreation creation2 = tokenCreation();
        creation2.setSessionName("LOG_TEST_2");

        v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-personal-access-tokens  NEW_PERSONAL_ACCESS_TOKENS('[PersonalAccessTokenCreation[ownerId=observer,sessionName=LOG_TEST_1], PersonalAccessTokenCreation[ownerId=<null>,sessionName=LOG_TEST_2]]')");
    }

}
