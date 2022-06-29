/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
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
public class GetPersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    private PersonalAccessToken token1;

    private PersonalAccessToken token2;

    private PersonalAccessToken token3;

    @BeforeMethod
    private void createTokens()
    {
        PersonalAccessTokenCreation creation1 = tokenCreation();
        creation1.setSessionName("test session name 1");

        PersonalAccessTokenCreation creation2 = tokenCreation();
        creation2.setSessionName("test session name 2");

        PersonalAccessTokenCreation creation3 = tokenCreation();
        creation3.setSessionName("test session name 3");

        token1 = createToken(TEST_USER, PASSWORD, creation1);
        token2 = createToken(TEST_USER, PASSWORD, creation2);
        token3 = createToken(TEST_GROUP_OBSERVER, PASSWORD, creation3);
    }

    @Test
    public void testGetWithRegularSessionTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(token.getPermId()), new PersonalAccessTokenFetchOptions());

        assertEquals(map.size(), 1);
    }

    @Test
    public void testGetWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.getPersonalAccessTokens(token.getHash(), Arrays.asList(token.getPermId()), new PersonalAccessTokenFetchOptions());
            }
        }, "Personal access tokens cannot be used to manage personal access tokens");
    }

    @Test
    public void testGetWithPermIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenPermId permId1 = token1.getPermId();
        PersonalAccessTokenPermId permId2 = token2.getPermId();

        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(permId1, permId2),
                        new PersonalAccessTokenFetchOptions());

        assertEquals(2, map.size());

        Iterator<PersonalAccessToken> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithNonexistentIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenPermId permId1 = token1.getPermId();
        PersonalAccessTokenPermId permId2 = token2.getPermId();
        PersonalAccessTokenPermId permId3 = new PersonalAccessTokenPermId("IDONTEXIST");

        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(permId1, permId2, permId3), new PersonalAccessTokenFetchOptions());

        assertEquals(2, map.size());

        Iterator<PersonalAccessToken> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithDuplicatedIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenPermId permId1 = new PersonalAccessTokenPermId(token1.getHash());
        PersonalAccessTokenPermId permId2 = new PersonalAccessTokenPermId(token1.getHash());

        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(permId1, permId2), new PersonalAccessTokenFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(permId1).getPermId(), token1.getPermId());
        assertEquals(map.get(permId2).getPermId(), token1.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithUnauthorizedIds()
    {
        List<? extends IPersonalAccessTokenId> ids = Arrays.asList(token1.getPermId(), token2.getPermId(), token3.getPermId());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, ids, new PersonalAccessTokenFetchOptions());

        assertEquals(map.size(), 3);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        map = v3api.getPersonalAccessTokens(sessionToken, ids, new PersonalAccessTokenFetchOptions());

        assertEquals(map.size(), 1);

        Iterator<PersonalAccessToken> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), token3.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
        creation.setSessionName("test session");
        creation.setValidFromDate(new Date(1));
        creation.setValidToDate(new Date(2));

        PersonalAccessToken createdToken = createToken(TEST_USER, PASSWORD, creation);

        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(createdToken.getPermId()), new PersonalAccessTokenFetchOptions());
        PersonalAccessToken fetchedToken = map.values().iterator().next();

        assertNotNull(fetchedToken.getPermId());
        assertNotNull(fetchedToken.getHash());
        assertEquals(fetchedToken.getSessionName(), creation.getSessionName());
        assertEquals(fetchedToken.getValidFromDate(), creation.getValidFromDate());
        assertEquals(fetchedToken.getValidToDate(), creation.getValidToDate());
        assertToday(fetchedToken.getRegistrationDate());
        assertToday(fetchedToken.getModificationDate());
        assertNull(fetchedToken.getAccessDate());

        assertOwnerNotFetched(fetchedToken);
        assertRegistratorNotFetched(fetchedToken);
        assertModifierNotFetched(fetchedToken);
    }

    @Test
    public void testGetWithOwnerFetched()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken createdToken = createToken(TEST_USER, PASSWORD, creation);

        PersonalAccessTokenFetchOptions fo = new PersonalAccessTokenFetchOptions();
        fo.withOwner();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(createdToken.getPermId()), fo);
        PersonalAccessToken fetchedToken = map.values().iterator().next();

        assertEquals(fetchedToken.getOwner().getPermId(), creation.getOwnerId());
        assertRegistratorNotFetched(fetchedToken);
        assertModifierNotFetched(fetchedToken);
    }

    @Test
    public void testGetWithRegistratorFetched()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken createdToken = createToken(TEST_USER, PASSWORD, creation);

        PersonalAccessTokenFetchOptions fo = new PersonalAccessTokenFetchOptions();
        fo.withRegistrator();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(createdToken.getPermId()), fo);
        PersonalAccessToken fetchedToken = map.values().iterator().next();

        assertEquals(fetchedToken.getRegistrator().getUserId(), TEST_USER);
        assertOwnerNotFetched(fetchedToken);
        assertModifierNotFetched(fetchedToken);
    }

    @Test
    public void testGetWithModifierFetched()
    {
        PersonalAccessTokenCreation creation = tokenCreation();
        creation.setOwnerId(new PersonPermId(TEST_GROUP_OBSERVER));

        PersonalAccessToken createdToken = createToken(TEST_USER, PASSWORD, creation);

        PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(createdToken.getPermId());

        PersonalAccessToken updatedToken = updateToken(INSTANCE_ADMIN_USER, PASSWORD, update);

        PersonalAccessTokenFetchOptions fo = new PersonalAccessTokenFetchOptions();
        fo.withModifier();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IPersonalAccessTokenId, PersonalAccessToken> map =
                v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(createdToken.getPermId()), fo);
        PersonalAccessToken fetchedToken = map.values().iterator().next();

        assertEquals(fetchedToken.getModifier().getUserId(), INSTANCE_ADMIN_USER);
        assertOwnerNotFetched(fetchedToken);
        assertRegistratorNotFetched(fetchedToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenFetchOptions fo = new PersonalAccessTokenFetchOptions();
        fo.withOwner();
        fo.withRegistrator();
        fo.withModifier();

        v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(token1.getPermId(), token2.getPermId()), fo);

        assertAccessLog(
                "get-personal-access-tokens  PERSONAL_ACCESS_TOKEN_IDS('[" + token1.getHash() + ", " + token2.getHash()
                        + "]') FETCH_OPTIONS('PersonalAccessToken\n    with Owner\n    with Registrator\n    with Modifier\n')");
    }

}
