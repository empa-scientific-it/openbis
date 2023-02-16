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
package ch.systemsx.cisd.openbis.generic.server.pat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
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
        TestCleanUp cleanUp = new TestCleanUp();
        session.addCleanupListener(cleanUp);

        testWithKnownSession(sessionToken, new Date(session.getSessionStart()),
                new Date(session.getSessionStart() + session.getSessionExpirationTime()));
        assertFalse(cleanUp.hasBeenCalled);

        sessionManager.expireSession(sessionToken);
        testWithUnknownSession(sessionToken);
        assertTrue(cleanUp.hasBeenCalled);

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
        TestCleanUp cleanUp = new TestCleanUp();
        session.addCleanupListener(cleanUp);

        testWithKnownSession(sessionToken, new Date(session.getSessionStart()),
                new Date(session.getSessionStart() + session.getSessionExpirationTime()));
        assertFalse(cleanUp.hasBeenCalled);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        testWithTimedOutSession(sessionToken);
        assertTrue(cleanUp.hasBeenCalled);
    }

    @Test
    public void testWithPersonalAccessTokenSession()
    {
        Date validFrom = new Date();
        Date validTo = new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY);

        String sessionToken = createPersonalAccessTokenSession(validFrom, validTo);
        Session session = sessionManager.getSession(sessionToken);
        TestCleanUp cleanUp = new TestCleanUp();
        session.addCleanupListener(cleanUp);

        testWithKnownSession(sessionToken, validFrom, validTo);
        assertFalse(cleanUp.hasBeenCalled);

        sessionManager.expireSession(sessionToken);
        testWithKnownSession(sessionToken, validFrom, validTo);
        assertFalse(cleanUp.hasBeenCalled);

        sessionManager.closeSession(sessionToken);
        testWithKnownSession(sessionToken, validFrom, validTo);
        assertFalse(cleanUp.hasBeenCalled);
    }

    @Test
    public void testWithPersonalAccessTokenSessionWithMultipleTokens() throws InterruptedException
    {
        final long SESSION_VALIDITY_PERIOD = 2000;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        String sessionName = "test session " + UUID.randomUUID();

        assertEquals(searchPersonalAccessTokenSession(sessionName).size(), 0);

        PersonalAccessTokenCreation patCreation1 = new PersonalAccessTokenCreation();
        patCreation1.setSessionName(sessionName);
        patCreation1.setValidFromDate(new Date());
        patCreation1.setValidToDate(new Date(System.currentTimeMillis() + SESSION_VALIDITY_PERIOD));

        v3api.createPersonalAccessTokens(sessionToken, Collections.singletonList(patCreation1));

        List<SessionInformation> sessionInformationList1 = searchPersonalAccessTokenSession(sessionName);
        assertEquals(sessionInformationList1.size(), 1);
        testWithKnownSession(sessionInformationList1.get(0).getSessionToken(), patCreation1.getValidFromDate(), patCreation1.getValidToDate());

        Session session1 = sessionManager.getSession(sessionInformationList1.get(0).getSessionToken());
        TestCleanUp cleanUp = new TestCleanUp();
        session1.addCleanupListener(cleanUp);

        PersonalAccessTokenCreation patCreation2 = new PersonalAccessTokenCreation();
        patCreation2.setSessionName(sessionName);
        patCreation2.setValidFromDate(patCreation1.getValidToDate());
        patCreation2.setValidToDate(new Date(patCreation1.getValidToDate().getTime() + SESSION_VALIDITY_PERIOD));

        v3api.createPersonalAccessTokens(sessionToken, Collections.singletonList(patCreation2));

        List<SessionInformation> sessionInformationList2 = searchPersonalAccessTokenSession(sessionName);
        assertEquals(sessionInformationList2.size(), 1);
        testWithKnownSession(sessionInformationList2.get(0).getSessionToken(), patCreation1.getValidFromDate(), patCreation2.getValidToDate());

        Session session2 = sessionManager.getSession(sessionInformationList2.get(0).getSessionToken());
        assertSame(session1, session2);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        sessionManager.expireSession(sessionInformationList2.get(0).getSessionToken());
        sessionManager.closeSession(sessionInformationList2.get(0).getSessionToken());
        testWithKnownSession(sessionInformationList2.get(0).getSessionToken(), patCreation1.getValidFromDate(), patCreation2.getValidToDate());
        assertFalse(cleanUp.hasBeenCalled);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        testWithTimedOutSession(sessionInformationList2.get(0).getSessionToken());
        assertTrue(cleanUp.hasBeenCalled);
    }

    @Test
    public void testWithPersonalAccessTokenSessionWithTwoOpenBISSessions() throws InterruptedException
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        String sessionName = "test session " + UUID.randomUUID();

        assertEquals(searchPersonalAccessTokenSession(sessionName).size(), 0);

        PersonalAccessTokenCreation patCreation1 = new PersonalAccessTokenCreation();
        patCreation1.setSessionName(sessionName);
        patCreation1.setValidFromDate(new Date());
        patCreation1.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));

        PersonalAccessTokenPermId personalAccessTokenPermId1 = v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(patCreation1)).get(0);

        List<SessionInformation> sessionInformationList1 = searchPersonalAccessTokenSession(sessionName);
        assertEquals(sessionInformationList1.size(), 1);
        testWithKnownSession(sessionInformationList1.get(0).getSessionToken(), patCreation1.getValidFromDate(), patCreation1.getValidToDate());

        PersonalAccessTokenDeletionOptions deletionOptions = new PersonalAccessTokenDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deletePersonalAccessTokens(sessionToken, Collections.singletonList(personalAccessTokenPermId1), deletionOptions);

        PersonalAccessTokenCreation patCreation2 = new PersonalAccessTokenCreation();
        patCreation2.setSessionName(sessionName);
        patCreation2.setValidFromDate(new Date());
        patCreation2.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));

        v3api.createPersonalAccessTokens(sessionToken, Collections.singletonList(patCreation2));

        List<SessionInformation> sessionInformationList2 = searchPersonalAccessTokenSession(sessionName);
        assertEquals(sessionInformationList2.size(), 2);
        testWithKnownSession(sessionInformationList2.get(1).getSessionToken(), patCreation2.getValidFromDate(), patCreation2.getValidToDate());

        assertEquals(sessionInformationList1.get(0).getSessionToken(), sessionInformationList2.get(0).getSessionToken());
        assertNotEquals(sessionInformationList2.get(0).getSessionToken(), sessionInformationList2.get(1).getSessionToken());

        assertFalse(sessionManager.isSessionActive(sessionInformationList2.get(0).getSessionToken()));
        assertTrue(sessionManager.isSessionActive(sessionInformationList2.get(1).getSessionToken()));
    }

    @Test
    public void testWithPersonalAccessTokenSessionTimeout() throws InterruptedException
    {
        final long SESSION_VALIDITY_PERIOD = 2000;

        Date validFrom = new Date();
        Date validTo = new Date(System.currentTimeMillis() + SESSION_VALIDITY_PERIOD);

        String sessionToken = createPersonalAccessTokenSession(validFrom, validTo);
        testWithKnownSession(sessionToken, validFrom, validTo);

        Thread.sleep(SESSION_VALIDITY_PERIOD);

        testWithTimedOutSession(sessionToken);
    }

    private void testWithKnownSession(String sessionToken, Date validFrom, Date validTo)
    {
        assertTrue(sessionManager.isSessionActive(sessionToken));

        Session tryGetSession = sessionManager.tryGetSession(sessionToken);
        assertEquals(tryGetSession.getSessionToken(), sessionToken);

        Session getSession = sessionManager.getSession(sessionToken);
        assertEquals(getSession.getSessionToken(), sessionToken);

        assertSame(tryGetSession, getSession);

        assertEquals(tryGetSession.getSessionStart(), validFrom.getTime());
        assertEquals(tryGetSession.getSessionExpirationTime(), validTo.getTime() - validFrom.getTime());

        List<Session> getSessions = new ArrayList<>();
        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                getSessions.add(session);
            }
        }
        assertEquals(getSessions.size(), 1);
        assertSame(tryGetSession, getSessions.get(0));
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

        List<Session> getSessions = new ArrayList<>();
        for (Session session : sessionManager.getSessions())
        {
            if (sessionToken.equals(session.getSessionToken()))
            {
                getSessions.add(session);
            }
        }
        assertEquals(getSessions.size(), 1);
        assertSame(tryGetSession, getSessions.get(0));

        assertInvalidSessionException(() ->
        {
            // removes the invalid session
            sessionManager.getSession(sessionToken);
        });
        assertInvalidSessionException(() ->
        {
            sessionManager.expireSession(sessionToken);
        });
        assertInvalidSessionException(() ->
        {
            sessionManager.closeSession(sessionToken);
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

        List<SessionInformation> sessionInformationList = patSessionResult.getObjects();

        if (sessionInformationList.size() > 1)
        {
            Assert.fail("Only one session expected");
            return null;
        } else if (sessionInformationList.size() == 1)
        {
            return sessionInformationList.get(0).getSessionToken();
        } else
        {
            return null;
        }
    }

    private List<SessionInformation> searchPersonalAccessTokenSession(String sessionName)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SessionInformationSearchCriteria patSessionCriteria = new SessionInformationSearchCriteria();
        patSessionCriteria.withPersonalAccessTokenSession().thatEquals(true);
        patSessionCriteria.withPersonalAccessTokenSessionName().thatEquals(sessionName);

        SearchResult<SessionInformation> patSessionResult =
                v3api.searchSessionInformation(sessionToken, patSessionCriteria, new SessionInformationFetchOptions());

        List<SessionInformation> sortedSessions = new ArrayList<>(patSessionResult.getObjects());
        sortedSessions.sort(Comparator.comparing(SessionInformation::getSessionToken));
        return sortedSessions;
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

    private static class TestCleanUp implements Session.ISessionCleaner
    {

        private boolean hasBeenCalled = false;

        @Override public void cleanup()
        {
            hasBeenCalled = true;
        }

    }
}
