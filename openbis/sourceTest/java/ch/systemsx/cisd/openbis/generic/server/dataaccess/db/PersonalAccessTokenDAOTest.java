package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenDAOTest
{

    @Test
    public void test()
    {
        TestListener listener = new TestListener();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO();
        dao.addListener(listener);

        PersonalAccessToken creation1 = new PersonalAccessToken();
        creation1.setOwnerId("test owner");
        creation1.setSessionName("test session");
        creation1.setValidFromDate(new Date(1));
        creation1.setValidToDate(new Date(2));

        PersonalAccessToken token1 = dao.createToken(creation1);

        assertNotSame(creation1, token1);
        assertNotNull(token1.getHash());
        assertEquals(token1.getOwnerId(), creation1.getOwnerId());
        assertEquals(token1.getSessionName(), creation1.getSessionName());
        assertEquals(token1.getValidFromDate(), creation1.getValidFromDate());
        assertEquals(token1.getValidToDate(), creation1.getValidToDate());
        assertNull(token1.getAccessDate());

        PersonalAccessTokenSession session1 = dao.getSessionByUserIdAndSessionName("test owner", "test session");
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), session1);

        assertNotNull(session1.getHash());
        assertNotEquals(session1.getHash(), token1.getHash());
        assertEquals(session1.getOwnerId(), token1.getOwnerId());
        assertEquals(session1.getName(), token1.getSessionName());
        assertEquals(session1.getValidFromDate(), token1.getValidFromDate());
        assertEquals(session1.getValidToDate(), token1.getValidToDate());
        assertNull(session1.getAccessDate());

        PersonalAccessToken creation2 = new PersonalAccessToken();
        creation2.setOwnerId("test owner");
        creation2.setSessionName("test session");
        creation2.setValidFromDate(new Date(2));
        creation2.setValidToDate(new Date(3));

        PersonalAccessToken token2 = dao.createToken(creation2);

        assertNotSame(creation2, token2);
        assertNotNull(token2.getHash());
        assertEquals(token2.getOwnerId(), creation2.getOwnerId());
        assertEquals(token2.getSessionName(), creation2.getSessionName());
        assertEquals(token2.getValidFromDate(), creation2.getValidFromDate());
        assertEquals(token2.getValidToDate(), creation2.getValidToDate());
        assertNull(token2.getAccessDate());

        PersonalAccessTokenSession session2 = dao.getSessionByUserIdAndSessionName("test owner", "test session");

        assertEquals(session2.getHash(), session1.getHash());
        assertEquals(session2.getOwnerId(), session1.getOwnerId());
        assertEquals(session2.getName(), session1.getName());
        assertEquals(session2.getValidFromDate(), token1.getValidFromDate());
        assertEquals(session2.getValidToDate(), token2.getValidToDate());
        assertNull(session2.getAccessDate());

        dao.deleteToken(token1.getHash());

        PersonalAccessTokenSession session3 = dao.getSessionByUserIdAndSessionName("test owner", "test session");

        assertEquals(session3.getHash(), session2.getHash());
        assertEquals(session3.getOwnerId(), session2.getOwnerId());
        assertEquals(session3.getName(), session2.getName());
        assertEquals(session3.getValidFromDate(), token2.getValidFromDate());
        assertEquals(session3.getValidToDate(), token2.getValidToDate());
        assertNull(session3.getAccessDate());

        dao.deleteToken(token2.getHash());

        PersonalAccessTokenSession session4 = dao.getSessionByUserIdAndSessionName("test owner", "test session");
        assertNull(session4);

        listener.assertCreatedSessions(session1);
        listener.assertUpdatedSessions(session2, session3);
        listener.assertDeletedSessions(session3);
    }

    private static class TestListener implements IPersonalAccessTokenDAO.Listener
    {

        private List<PersonalAccessTokenSession> createdSessions = new ArrayList<>();

        private List<PersonalAccessTokenSession> updatedSessions = new ArrayList<>();

        private List<PersonalAccessTokenSession> deletedSessions = new ArrayList<>();

        @Override public void onSessionCreated(final PersonalAccessTokenSession session)
        {
            createdSessions.add(session);
        }

        @Override public void onSessionUpdated(final PersonalAccessTokenSession session)
        {
            updatedSessions.add(session);
        }

        @Override public void onSessionDeleted(final PersonalAccessTokenSession session)
        {
            deletedSessions.add(session);
        }

        void assertCreatedSessions(PersonalAccessTokenSession... expectedSessions)
        {
            AssertionUtil.assertCollectionContainsOnly(createdSessions, expectedSessions);
        }

        void assertUpdatedSessions(PersonalAccessTokenSession... expectedSessions)
        {
            AssertionUtil.assertCollectionContainsOnly(updatedSessions, expectedSessions);
        }

        void assertDeletedSessions(PersonalAccessTokenSession... expectedSessions)
        {
            AssertionUtil.assertCollectionContainsOnly(deletedSessions, expectedSessions);
        }

    }
}
