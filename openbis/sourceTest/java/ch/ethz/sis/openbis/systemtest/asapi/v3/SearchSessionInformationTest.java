package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.id.SessionInformationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;

public class SearchSessionInformationTest extends AbstractTest
{

    private String sessionToken1;

    private String sessionToken2;

    private String sessionToken3;

    private PersonalAccessToken pat1;

    private PersonalAccessToken pat2;

    @BeforeMethod
    private void createSessions()
    {
        sessionToken1 = v3api.login(TEST_USER, PASSWORD);
        sessionToken2 = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        sessionToken3 = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);

        pat1 = createToken(TEST_USER, PASSWORD, tokenCreation());
        pat2 = createToken(TEST_GROUP_OBSERVER, PASSWORD, tokenCreation());
    }

    @SuppressWarnings("null")
    @Test
    public void testSearch()
    {
        SessionInformation sessionInformation = searchSessionInformation(TEST_USER, sessionToken1);
        SessionInformation patSessionInformation =
                searchPersonalAccessTokenSessionInformation(TEST_USER, pat1.getOwner().getUserId(), pat1.getSessionName());

        assertNotNull(sessionInformation);
        assertEquals(sessionInformation.getSessionToken(), sessionToken1);
        assertEquals(sessionInformation.getUserName(), TEST_USER);
        assertEquals(sessionInformation.getPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getCreatorPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getHomeGroupCode(), "CISD");
        assertFalse(sessionInformation.isPersonalAccessTokenSession());
        assertNull(sessionInformation.getPersonalAccessTokenSessionName());

        assertNotNull(patSessionInformation);
        assertNotNull(patSessionInformation.getSessionToken());
        assertEquals(patSessionInformation.getUserName(), TEST_USER);
        assertEquals(patSessionInformation.getPerson().getUserId(), TEST_USER);
        assertEquals(patSessionInformation.getCreatorPerson().getUserId(), TEST_USER);
        assertEquals(patSessionInformation.getHomeGroupCode(), "CISD");
        assertTrue(patSessionInformation.isPersonalAccessTokenSession());
        assertEquals(patSessionInformation.getPersonalAccessTokenSessionName(), pat1.getSessionName());

        deleteToken(TEST_USER, PASSWORD, pat1.getPermId());

        sessionInformation = searchSessionInformation(TEST_USER, sessionToken1);
        patSessionInformation = searchPersonalAccessTokenSessionInformation(TEST_USER, pat1.getOwner().getUserId(), pat1.getSessionName());

        assertNotNull(sessionInformation);
        assertEquals(sessionInformation.getSessionToken(), sessionToken1);
        assertEquals(sessionInformation.getUserName(), TEST_USER);
        assertEquals(sessionInformation.getPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getCreatorPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getHomeGroupCode(), "CISD");
        assertFalse(sessionInformation.isPersonalAccessTokenSession());
        assertNull(sessionInformation.getPersonalAccessTokenSessionName());

        assertNull(patSessionInformation);

        v3api.logout(sessionToken1);

        sessionInformation = searchSessionInformation(TEST_USER, sessionToken1);
        patSessionInformation = searchPersonalAccessTokenSessionInformation(TEST_USER, pat1.getOwner().getUserId(), pat1.getSessionName());

        assertNull(sessionInformation);
        assertNull(patSessionInformation);
    }

    @Test
    public void testSearchWithRegularSessionTokenAsSessionToken()
    {
        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken1, new SessionInformationSearchCriteria(), new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertNotNull(sessionInformation.getSessionToken());
        }
    }

    @Test
    public void testSearchWithPersonalAccessTokenAsSessionToken()
    {
        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(pat1.getHash(), new SessionInformationSearchCriteria(), new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertNull(sessionInformation.getSessionToken());
        }
    }

    @Test
    public void testSearchByETLServerUser()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken3));
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat1.getSessionName());
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat2.getSessionName());

        String sessionToken = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertEquals(result.getObjects().size(), 5);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertNotNull(sessionInformation.getSessionToken());
        }
    }

    @Test
    public void testSearchByInstanceAdminUser()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken3));
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat1.getSessionName());
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat2.getSessionName());

        String sessionToken = v3api.login(TEST_INSTANCE_ETLSERVER, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertEquals(result.getObjects().size(), 5);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertNotNull(sessionInformation.getSessionToken());
        }
    }

    @Test
    public void testSearchByRegularUser()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken3));
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat1.getSessionName());
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat2.getSessionName());

        String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertEquals(result.getObjects().size(), 2);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertNotNull(sessionInformation.getSessionToken());
        }
    }

    @Test
    public void testSearchWithPermIds()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken3));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<SessionInformation> result = v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertContainsOnly(result, sessionToken1, sessionToken2, sessionToken3);
    }

    @Test
    public void testSearchWithNonexistentIds()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken1));
        criteria.withId().thatEquals(new SessionInformationPermId(sessionToken2));
        criteria.withId().thatEquals(new SessionInformationPermId("IDONTEXIST"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<SessionInformation> result = v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertContainsOnly(result, sessionToken1, sessionToken2);
    }

    @Test
    public void testSearchWithUserName()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withUserName().thatEquals(TEST_USER);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<SessionInformation> result = v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertEquals(sessionInformation.getUserName(), TEST_USER);
        }
    }

    @Test
    public void testSearchWithPersonalAccessTokenSession()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withPersonalAccessTokenSession().thatEquals(true);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<SessionInformation> result = v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertTrue(sessionInformation.isPersonalAccessTokenSession());
        }
    }

    @Test
    public void testSearchWithPersonalAccessTokenSessionName()
    {
        SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        criteria.withPersonalAccessTokenSessionName().thatEquals(pat1.getSessionName());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<SessionInformation> result = v3api.searchSessionInformation(sessionToken, criteria, new SessionInformationFetchOptions());

        assertTrue(result.getObjects().size() > 0);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            assertEquals(sessionInformation.getPersonalAccessTokenSessionName(), pat1.getSessionName());
        }
    }

    private SessionInformation searchSessionInformation(String userId, String sessionToken)
    {
        SessionInformationFetchOptions fo = new SessionInformationFetchOptions();
        fo.withPerson();
        fo.withCreatorPerson();

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(v3api.login(userId, PASSWORD), new SessionInformationSearchCriteria(), fo);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            if (sessionToken.equals(sessionInformation.getSessionToken()))
            {
                return sessionInformation;
            }
        }

        return null;
    }

    private SessionInformation searchPersonalAccessTokenSessionInformation(String userId, String ownerId,
            String sessionName)
    {
        SessionInformationFetchOptions fo = new SessionInformationFetchOptions();
        fo.withPerson();
        fo.withCreatorPerson();

        SearchResult<SessionInformation> result =
                v3api.searchSessionInformation(v3api.login(userId, PASSWORD), new SessionInformationSearchCriteria(), fo);

        for (SessionInformation sessionInformation : result.getObjects())
        {
            String sessionInformationUserId = sessionInformation.getPerson() != null ? sessionInformation.getPerson().getUserId() : null;
            
            if (ownerId.equals(sessionInformationUserId) && sessionName.equals(
                    sessionInformation.getPersonalAccessTokenSessionName()))
            {
                return sessionInformation;
            }
        }

        return null;
    }

    private void assertContainsOnly(SearchResult<SessionInformation> result, String... expectedSessionTokens)
    {
        Set<String> actualSessionTokens = new HashSet<String>();
        for (SessionInformation sessionInformation : result.getObjects())
        {
            actualSessionTokens.add(sessionInformation.getSessionToken());
        }

        assertCollectionContainsOnly(actualSessionTokens, expectedSessionTokens);
    }

}
