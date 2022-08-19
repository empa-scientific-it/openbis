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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchPersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    private PersonalAccessToken token1;

    private PersonalAccessToken token2;

    private PersonalAccessToken token3;

    @BeforeMethod
    private void createTokens()
    {
        // delete existing tokens created by other tests
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<PersonalAccessToken> result =
                v3api.searchPersonalAccessTokens(sessionToken, new PersonalAccessTokenSearchCriteria(), new PersonalAccessTokenFetchOptions());

        PersonalAccessTokenDeletionOptions options = new PersonalAccessTokenDeletionOptions();
        options.setReason("cleaning up before test");

        v3api.deletePersonalAccessTokens(sessionToken, result.getObjects().stream().map(PersonalAccessToken::getPermId).collect(Collectors.toList()),
                options);

        // create new tests tokens
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
    public void testSearch()
    {
        testSearch(TEST_USER, new PersonalAccessTokenSearchCriteria(), token1.getHash(), token2.getHash(), token3.getHash());
    }

    @Test
    public void testSearchWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<PersonalAccessToken> result =
                v3api.searchPersonalAccessTokens(sessionToken, new PersonalAccessTokenSearchCriteria(), new PersonalAccessTokenFetchOptions());

        assertEquals(result.getObjects().size(), 3);
    }

    @Test
    public void testSearchWithPersonalAccessTokenAsSessionToken()
    {
        SearchResult<PersonalAccessToken> result =
                v3api.searchPersonalAccessTokens(token1.getHash(), new PersonalAccessTokenSearchCriteria(), new PersonalAccessTokenFetchOptions());

        assertEquals(result.getTotalCount(), 1);
        assertEquals(result.getObjects().size(), 1);
        assertEquals(result.getObjects().get(0).getPermId(), token1.getPermId());
    }

    @Test
    public void testSearchWithPermIds()
    {
        PersonalAccessTokenSearchCriteria criteria = new PersonalAccessTokenSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(token1.getPermId());
        criteria.withId().thatEquals(token2.getPermId());

        testSearch(TEST_USER, criteria, token1.getHash(), token2.getHash());
    }

    @Test
    public void testSearchWithNonexistentIds()
    {
        PersonalAccessTokenSearchCriteria criteria = new PersonalAccessTokenSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(token1.getPermId());
        criteria.withId().thatEquals(token2.getPermId());
        criteria.withId().thatEquals(new PersonalAccessTokenPermId("IDONTEXIST"));

        testSearch(TEST_USER, criteria, token1.getHash(), token2.getHash());
    }

    @Test
    public void testSearchWithDuplicatedIds()
    {
        PersonalAccessTokenSearchCriteria criteria = new PersonalAccessTokenSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new PersonalAccessTokenPermId(token1.getHash()));
        criteria.withId().thatEquals(new PersonalAccessTokenPermId(token1.getHash()));

        testSearch(TEST_USER, criteria, token1.getHash());
    }

    @Test
    public void testSearchWithUnauthorizedIds()
    {
        PersonalAccessTokenSearchCriteria criteria = new PersonalAccessTokenSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(token1.getPermId());
        criteria.withId().thatEquals(token2.getPermId());
        criteria.withId().thatEquals(token3.getPermId());

        testSearch(TEST_USER, criteria, token1.getHash(), token2.getHash(), token3.getHash());
        testSearch(TEST_GROUP_OBSERVER, criteria, token3.getHash());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenSearchCriteria criteria = new PersonalAccessTokenSearchCriteria();
        criteria.withId().thatEquals(token1.getPermId());

        PersonalAccessTokenFetchOptions fo = new PersonalAccessTokenFetchOptions();
        fo.withOwner();
        fo.withRegistrator();
        fo.withModifier();

        v3api.searchPersonalAccessTokens(sessionToken, criteria, fo);

        assertAccessLog(
                "search-personal-access-tokens  SEARCH_CRITERIA:\n'PERSONAL_ACCESS_TOKEN\n    with id '" + token1.getHash() + "'\n'\n"
                        + "FETCH_OPTIONS:\n'PersonalAccessToken\n    with Owner\n    with Registrator\n    with Modifier\n'");
    }

    private void testSearch(String user, PersonalAccessTokenSearchCriteria criteria, String... expectedHashes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<PersonalAccessToken> result =
                v3api.searchPersonalAccessTokens(sessionToken, criteria, new PersonalAccessTokenFetchOptions());

        Set<String> actualHashes = new HashSet<String>();
        for (PersonalAccessToken token : result.getObjects())
        {
            actualHashes.add(token.getHash());
        }

        assertCollectionContainsOnly(actualHashes, expectedHashes);

        v3api.logout(sessionToken);
    }

}
