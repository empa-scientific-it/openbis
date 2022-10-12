package ch.systemsx.cisd.openbis.generic.server.pat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.systemsx.cisd.authentication.SessionTokenHash;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

public class PersonalAccessTokenOpenBisSessionManagerDecoratorTest extends SystemTestCase
{

    @Autowired
    private IApplicationServerApi v3api;

    @Autowired
    private IOpenBisSessionManager sessionManager;

    @Test
    public void testWithUnknownSession()
    {
        String unknownSessionToken = SessionTokenHash.create("testing", System.currentTimeMillis()).toString();

        testWithUnknownSession(unknownSessionToken);

        assertInvalidSessionException(() ->
        {
            sessionManager.expireSession(unknownSessionToken);
        });
        assertInvalidSessionException(() ->
        {
            sessionManager.closeSession(unknownSessionToken);
        });
    }

    @Test
    public void testWithRegularSession()
    {
        String sessionToken = sessionManager.tryToOpenSession(TEST_USER, PASSWORD);
        Session session = sessionManager.getSession(sessionToken);
        session.setSessionExpirationTime(DateUtils.MILLIS_PER_DAY);
        testWithKnownSession(sessionToken);

        sessionManager.expireSession(sessionToken);
        testWithUnknownSession(sessionToken);

        assertInvalidSessionException(() ->
        {
            sessionManager.expireSession(sessionToken);
        });
        assertInvalidSessionException(() ->
        {
            sessionManager.closeSession(sessionToken);
        });
    }

    @Test
    public void testWithRegularSessionTimeout() throws InterruptedException
    {
        final long SESSION_VALIDITY_PERIOD = 2000;

        String sessionToken = sessionManager.tryToOpenSession(TEST_USER, PASSWORD);
        Session session = sessionManager.getSession(sessionToken);
        session.setSessionExpirationTime(SESSION_VALIDITY_PERIOD);
        testWithKnownSession(sessionToken);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        testWithTimedOutSession(sessionToken);

        assertInvalidSessionException(() ->
        {
            sessionManager.expireSession(sessionToken);
        });
        assertInvalidSessionException(() ->
        {
            sessionManager.closeSession(sessionToken);
        });
    }

    @Test
    public void testWithPersonalAccessTokenSession()
    {
        String sessionToken = createPersonalAccessTokenSession(new Date(), new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));
        testWithKnownSession(sessionToken);

        sessionManager.expireSession(sessionToken);
        testWithKnownSession(sessionToken);

        sessionManager.closeSession(sessionToken);
        testWithKnownSession(sessionToken);
    }

    @Test
    public void testWithPersonalAccessTokenSessionWithMultipleTokens() throws InterruptedException
    {
        final long SESSION_VALIDITY_PERIOD = 2000;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        String sessionName = "test session " + UUID.randomUUID();

        PersonalAccessTokenCreation patCreation1 = new PersonalAccessTokenCreation();
        patCreation1.setSessionName(sessionName);
        patCreation1.setValidFromDate(new Date());
        patCreation1.setValidToDate(new Date(System.currentTimeMillis() + SESSION_VALIDITY_PERIOD));

        PersonalAccessTokenCreation patCreation2 = new PersonalAccessTokenCreation();
        patCreation2.setSessionName(sessionName);
        patCreation2.setValidFromDate(patCreation1.getValidToDate());
        patCreation2.setValidToDate(new Date(patCreation1.getValidToDate().getTime() + SESSION_VALIDITY_PERIOD));

        v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(patCreation1, patCreation2));

        SessionInformationSearchCriteria patSessionCriteria = new SessionInformationSearchCriteria();
        patSessionCriteria.withPersonalAccessTokenSession().thatEquals(true);
        patSessionCriteria.withPersonalAccessTokenSessionName().thatEquals(sessionName);

        SearchResult<SessionInformation> patSessionResult =
                v3api.searchSessionInformation(sessionToken, patSessionCriteria, new SessionInformationFetchOptions());
        assertEquals(patSessionResult.getObjects().size(), 1);

        String patSessionToken = patSessionResult.getObjects().get(0).getSessionToken();

        testWithKnownSession(patSessionToken);
        Thread.sleep(SESSION_VALIDITY_PERIOD);
        sessionManager.expireSession(patSessionToken);
        sessionManager.closeSession(patSessionToken);
        testWithKnownSession(patSessionToken);
        Thread.sleep(SESSION_VALIDITY_PERIOD);
        testWithTimedOutSession(patSessionToken);
    }

    @Test
    public void testWithPersonalAccessTokenSessionTimeout() throws InterruptedException
    {
        final long SESSION_VALIDITY_PERIOD = 2000;

        String sessionToken = createPersonalAccessTokenSession(new Date(), new Date(System.currentTimeMillis() + SESSION_VALIDITY_PERIOD));
        testWithKnownSession(sessionToken);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        testWithTimedOutSession(sessionToken);

        sessionManager.expireSession(sessionToken);
        sessionManager.closeSession(sessionToken);
    }

    private void testWithKnownSession(String sessionToken)
    {
        assertTrue(sessionManager.isSessionActive(sessionToken));

        Session tryGetSession = sessionManager.tryGetSession(sessionToken);
        assertEquals(tryGetSession.getSessionToken(), sessionToken);

        Session getSession = sessionManager.getSession(sessionToken);
        assertEquals(getSession.getSessionToken(), sessionToken);

        assertSame(tryGetSession, getSession);

        Session getSessions = null;
        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                getSessions = session;
            }
        }
        assertNotNull(getSessions);
        assertEquals(getSessions.getSessionToken(), sessionToken);
        assertSame(tryGetSession, getSessions);
    }

    private void testWithUnknownSession(String sessionToken)
    {
        assertFalse(sessionManager.isSessionActive(sessionToken));
        assertNull(sessionManager.tryGetSession(sessionToken));

        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                fail("Session should not exist");
            }
        }

        assertInvalidSessionException(() ->
        {
            sessionManager.getSession(sessionToken);
        });
    }

    private void testWithTimedOutSession(String sessionToken)
    {
        assertFalse(sessionManager.isSessionActive(sessionToken));

        Session tryGetSession = sessionManager.tryGetSession(sessionToken);
        assertEquals(tryGetSession.getSessionToken(), sessionToken);

        Session getSessions = null;
        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                getSessions = session;
            }
        }
        assertNotNull(getSessions);
        assertEquals(getSessions.getSessionToken(), sessionToken);
        assertSame(tryGetSession, getSessions);

        assertInvalidSessionException(() ->
        {
            // removes the invalid session
            sessionManager.getSession(sessionToken);
        });

        testWithUnknownSession(sessionToken);
    }

    private String createPersonalAccessTokenSession(Date validFrom, Date validTo)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonalAccessTokenCreation patCreation = new PersonalAccessTokenCreation();
        patCreation.setSessionName("test session " + UUID.randomUUID());
        patCreation.setValidFromDate(validFrom);
        patCreation.setValidToDate(validTo);

        v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(patCreation));

        SessionInformationSearchCriteria patSessionCriteria = new SessionInformationSearchCriteria();
        patSessionCriteria.withPersonalAccessTokenSession().thatEquals(true);
        patSessionCriteria.withPersonalAccessTokenSessionName().thatEquals(patCreation.getSessionName());

        SearchResult<SessionInformation> patSessionResult =
                v3api.searchSessionInformation(sessionToken, patSessionCriteria, new SessionInformationFetchOptions());

        if (patSessionResult.getObjects().size() > 0)
        {
            return patSessionResult.getObjects().get(0).getSessionToken();
        } else
        {
            return null;
        }
    }

    private void assertInvalidSessionException(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail();
        } catch (InvalidSessionException e)
        {
            // expected
        }
    }
}
