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
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
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
        PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));
        creation.setSessionName("test session name");
        creation.setValidFromDate(new Date(1));
        creation.setValidToDate(new Date(2));

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, creation);

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setSessionName("updated session name");
        update.setValidFromDate(new Date(3));
        update.setValidToDate(new Date(4));

        PersonalAccessToken updated = updateToken(INSTANCE_ADMIN_USER, PASSWORD, update);

        assertEquals(updated.getPermId(), token.getPermId());
        assertEquals(updated.getOwner().getUserId(), TEST_GROUP_OBSERVER);
        assertEquals(updated.getRegistrator().getUserId(), TEST_USER);
        assertEquals(updated.getModifier().getUserId(), INSTANCE_ADMIN_USER);
        assertEquals(updated.getValidFromDate(), update.getValidFromDate().getValue());
        assertEquals(updated.getValidToDate(), update.getValidToDate().getValue());
        assertEquals(updated.getRegistrationDate(), token.getRegistrationDate());
        assertToday(token.getModificationDate());
        assertNull(token.getAccessDate());
    }

    @Test
    public void testUpdateByOwner()
    {
        testUpdateBy(TEST_GROUP_OBSERVER, TEST_GROUP_OBSERVER);
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
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                testUpdateBy(TEST_USER, TEST_GROUP_OBSERVER);
            }
        }, null);
    }

    private void testUpdateBy(String ownerId, String updaterId)
    {
        PersonalAccessToken token = createToken(ownerId, PASSWORD, testCreation());
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setSessionName("updated session name");
        updateToken(updaterId, PASSWORD, update);
    }

    @Test
    public void testUpdateWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());
        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setSessionName("updated session name");

        v3api.updatePersonalAccessTokens(sessionToken, Arrays.asList(update));
    }

    @Test
    public void testUpdateWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setSessionName("updated session name");

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
                updateToken(TEST_USER, PASSWORD, update);
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
                updateToken(TEST_USER, PASSWORD, update);
            }
        }, update.getPersonalAccessTokenId());
    }

    @Test
    public void testUpdateWithSessionNameNull()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setSessionName(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                updateToken(TEST_USER, PASSWORD, update);
            }
        }, "Session name cannot be empty");
    }

    @Test
    public void testUpdateWithValidFromNull()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setValidFromDate(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                updateToken(TEST_USER, PASSWORD, update);
            }
        }, "Valid from date cannot be null");
    }

    @Test
    public void testUpdateWithValidToNull()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setValidToDate(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                updateToken(TEST_USER, PASSWORD, update);
            }
        }, "Valid to date cannot be null");
    }

    @Test
    public void testUpdateWithValidFromAfterValidTo()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setValidFromDate(new Date(2));
        update.setValidToDate(new Date(1));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                updateToken(TEST_USER, PASSWORD, update);
            }
        }, "Valid from date cannot be after valid to date");
    }

    @Test
    public void testUpdateWithAccessDateByETLServerUser()
    {
        testUpdateWithAccessDateBy(TEST_INSTANCE_OBSERVER, TEST_INSTANCE_ETLSERVER);
    }

    @Test
    public void testUpdateWithAccessDateByInstanceAdminUser()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                testUpdateWithAccessDateBy(TEST_INSTANCE_OBSERVER, INSTANCE_ADMIN_USER);
            }
        }, "Access date can only be changed by system user or ETL server user");
    }

    @Test
    public void testUpdateWithAccessDateByRegularUser()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                testUpdateWithAccessDateBy(TEST_INSTANCE_OBSERVER, TEST_GROUP_OBSERVER);
            }
        }, "Access date can only be changed by system user or ETL server user");
    }

    private void testUpdateWithAccessDateBy(String ownerId, String updaterId)
    {
        PersonalAccessToken token = createToken(ownerId, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(token.getPermId());
        update.setAccessDate(new Date());

        PersonalAccessToken updated = updateToken(updaterId, PASSWORD, update);

        assertEquals(updated.getAccessDate(), update.getAccessDate().getValue());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessToken token1 = createToken(TEST_USER, PASSWORD, testCreation());
        PersonalAccessToken token2 = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenUpdate update1 = new PersonalAccessTokenUpdate();
        update1.setPersonalAccessTokenId(token1.getPermId());

        PersonalAccessTokenUpdate update2 = new PersonalAccessTokenUpdate();
        update2.setPersonalAccessTokenId(token2.getPermId());

        v3api.updatePersonalAccessTokens(sessionToken, Arrays.asList(update1, update2));

        assertAccessLog(
                "update-personal-access-tokens  PERSONAL_ACCESS_TOKEN_UPDATES('[PersonalAccessTokenUpdate[personalAccessTokenId=" + token1.getHash()
                        + "], PersonalAccessTokenUpdate[personalAccessTokenId=" + token2.getHash() + "]]')");
    }

}
