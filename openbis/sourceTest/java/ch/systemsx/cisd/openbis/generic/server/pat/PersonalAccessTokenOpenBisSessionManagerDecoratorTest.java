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
    }

    private void testWithUnknownSession(String sessionToken)
    {
        assertFalse(sessionManager.isSessionActive(sessionToken));
        assertNull(sessionManager.tryGetSession(sessionToken));
        assertInvalidSessionException(() ->
        {
            sessionManager.getSession(sessionToken);
        });

        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                fail("Session should not exist");
            }
        }

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
    public void testWithRegularSession()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        testWithKnownSession(sessionToken);

        sessionManager.expireSession(sessionToken);
        testWithUnknownSession(sessionToken);

        assertInvalidSessionException(() ->
        {
            sessionManager.closeSession(sessionToken);
        });
    }

    @Test
    public void testWithPersonalAccessTokenSessionValid()
    {
        String sessionToken = createPersonalAccessTokenSession(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY),
                new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));
        testWithKnownSession(sessionToken);

        sessionManager.expireSession(sessionToken);
        testWithKnownSession(sessionToken);

        sessionManager.closeSession(sessionToken);
        testWithKnownSession(sessionToken);
    }

    @Test
    public void testWithPersonalAccessTokenSessionInvalid()
    {
        String sessionToken = createPersonalAccessTokenSession(new Date(System.currentTimeMillis() - 2 * DateUtils.MILLIS_PER_DAY),
                new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
        assertNull(sessionToken);
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
