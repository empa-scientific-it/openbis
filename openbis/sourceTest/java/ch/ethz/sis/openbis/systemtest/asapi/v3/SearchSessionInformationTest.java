package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsAtLeast;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.id.SessionInformationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;

public class SearchSessionInformationTest extends AbstractTest
{
    @SuppressWarnings("null")
    @Test
    public void testSearch()
    {
        PersonalAccessToken personalAccessToken = createToken(TEST_USER, PASSWORD, tokenCreation());

        SessionInformationFetchOptions fo = new SessionInformationFetchOptions();
        fo.withPerson();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, new SessionInformationSearchCriteria(), fo);
        SessionInformation sessionInformation = getSessionInformation(result.getObjects(), sessionToken);
        SessionInformation personalAccessTokenSessionInformation =
                getPersonalAccessTokenSessionInformation(result.getObjects(), personalAccessToken.getOwner().getUserId(),
                        personalAccessToken.getSessionName());

        assertNotNull(sessionInformation);
        assertNotNull(personalAccessTokenSessionInformation);

        deleteToken(TEST_USER, PASSWORD, personalAccessToken.getPermId());

        result =
                v3api.searchSessionInformation(sessionToken, new SessionInformationSearchCriteria(), fo);
        sessionInformation = getSessionInformation(result.getObjects(), sessionToken);
        personalAccessTokenSessionInformation =
                getPersonalAccessTokenSessionInformation(result.getObjects(), personalAccessToken.getOwner().getUserId(),
                        personalAccessToken.getSessionName());

        assertNotNull(sessionInformation);
        assertNull(personalAccessTokenSessionInformation);
    }

    @Test
    public void testSearchWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, new SessionInformationSearchCriteria(), new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);
    }

    @Test
    public void testSearchWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.searchSessionInformation(token.getHash(), new SessionInformationSearchCriteria(), new SessionInformationFetchOptions());
            }
        }, "Personal access tokens cannot be used to get session information");
    }

    @Test
    public void testSearchByETLServerUser()
    {
        testSearchBy(TEST_INSTANCE_ETLSERVER);
    }

    @Test
    public void testSearchByInstanceAdminUser()
    {
        testSearchBy(INSTANCE_ADMIN_USER);
    }

    @Test
    public void testSearchByRegularUser()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                testSearchBy(TEST_GROUP_OBSERVER);
            }
        });
    }

    private void testSearchBy(String userId)
    {
        testSearch(userId, new SessionInformationSearchCriteria());
    }

    @Test
    public void testSearchWithPermIds()
    {
        String sessionToken1 = v3api.login(TEST_USER, PASSWORD);
        String sessionToken2 = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        String sessionToken3 = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);

        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken3));

        testSearch(TEST_USER, criteria, sessionToken1, sessionToken2, sessionToken3);
    }

    @Test
    public void testSearchWithNonexistentIds()
    {
        String sessionToken1 = v3api.login(TEST_USER, PASSWORD);
        String sessionToken2 = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);

        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId("IDONTEXIST"));

        testSearch(TEST_USER, criteria, sessionToken1, sessionToken2);
    }

    private void testSearch(String user, SessionInformationSearchCriteria criteria, String... expectedSessionTokens)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        Set<String> actualSessionTokens = new HashSet<String>();
        for (SessionInformation sessionInformation : result.getObjects())
        {
            actualSessionTokens.add(sessionInformation.getSessionToken());
        }

        assertCollectionContainsAtLeast(actualSessionTokens, expectedSessionTokens);

        v3api.logout(sessionToken);
    }

    private SessionInformation getSessionInformation(List<SessionInformation> sessionInformationList, String sessionToken)
    {
        for (SessionInformation sessionInformation : sessionInformationList)
        {
            if (sessionToken.equals(sessionInformation.getSessionToken()))
            {
                return sessionInformation;
            }
        }

        return null;
    }

    private SessionInformation getPersonalAccessTokenSessionInformation(List<SessionInformation> sessionInformationList, String ownerId,
            String sessionName)
    {
        for (SessionInformation sessionInformation : sessionInformationList)
        {
            if (ownerId.equals(sessionInformation.getPerson().getUserId()) && sessionName.equals(
                    sessionInformation.getPersonalAccessTokenSessionName()))
            {
                return sessionInformation;
            }
        }

        return null;
    }

}
