/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
@Test
public class UpdatePersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    @Test
    public void testUpdate()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, creation);

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setAccessDate(new Date());

        PersonalAccessToken updated = updateToken(TEST_INSTANCE_ETLSERVER, PASSWORD, update);

        assertEquals(updated.getPermId(), token.getPermId());
        assertEquals(updated.getOwner().getUserId(), TEST_GROUP_OBSERVER);
        assertEquals(updated.getRegistrator().getUserId(), TEST_USER);
        assertEquals(updated.getModifier().getUserId(), TEST_INSTANCE_ETLSERVER);
        assertEquals(updated.getRegistrationDate(), token.getRegistrationDate());
        assertToday(token.getModificationDate());
        assertEquals(updated.getAccessDate(), update.getAccessDate().getValue());
    }

    @Test
    public void testUpdateByOwner()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                testUpdateBy(TEST_GROUP_OBSERVER, TEST_GROUP_OBSERVER);
            }
        });
    }

    @Test
    public void testUpdateByNonOwnerETLServerUser()
    {
        testUpdateBy(TEST_GROUP_OBSERVER, TEST_INSTANCE_ETLSERVER);
    }

    @Test
    public void testUpdateByNonOwnerInstanceAdminUser()
    {
        testUpdateBy(TEST_GROUP_OBSERVER, INSTANCE_ADMIN_USER);
    }

    @Test
    public void testUpdateByNonOwnerRegularUser()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                testUpdateBy(TEST_USER, TEST_GROUP_OBSERVER);
            }
        });
    }

    private void testUpdateBy(String ownerId, String updaterId)
    {
        PersonalAccessToken token = createToken(ownerId, PASSWORD, tokenCreation());
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        updateToken(updaterId, PASSWORD, update);
    }

    @Test
    public void testUpdateWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setAccessDate(new Date());

        v3api.updatePersonalAccessTokens(sessionToken, Arrays.asList(update));
    }

    @Test
    public void testUpdateWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updatePersonalAccessTokens(token.getHash(), Arrays.asList(update));
            }
        }, "Personal access tokens cannot be used to manage personal access tokens");
    }

    @Test
    public void testUpdateWithPersonalAccessTokenIdNull()
    {
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();

        assertUserFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                updateToken(TEST_INSTANCE_ETLSERVER, PASSWORD, update);
            }
        }, "Personal access token id cannot be null");
    }

    @Test
    public void testUpdateWithPersonalAccessTokenIdNonexistent()
    {
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(new PersonalAccessTokenPermId("I_DONT_EXIST"));

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                updateToken(TEST_INSTANCE_ETLSERVER, PASSWORD, update);
            }
        }, update.getPersonalAccessTokenId());
    }

    @Test
    public void testLogging()
    {
        PersonalAccessToken token1 = createToken(TEST_USER, PASSWORD, tokenCreation());
        PersonalAccessToken token2 = createToken(TEST_USER, PASSWORD, tokenCreation());

        PersonalAccessTokenUpdate update1 = new PersonalAccessTokenUpdate();
        update1.setPersonalAccessTokenId(token1.getPermId());

        PersonalAccessTokenUpdate update2 = new PersonalAccessTokenUpdate();
        update2.setPersonalAccessTokenId(token2.getPermId());

        String sessionToken = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);
        v3api.updatePersonalAccessTokens(sessionToken, Arrays.asList(update1, update2));

        assertAccessLog(
                "update-personal-access-tokens  PERSONAL_ACCESS_TOKEN_UPDATES('[PersonalAccessTokenUpdate[personalAccessTokenId=" + token1.getHash()
                        + "], PersonalAccessTokenUpdate[personalAccessTokenId=" + token2.getHash() + "]]')");
    }

}
