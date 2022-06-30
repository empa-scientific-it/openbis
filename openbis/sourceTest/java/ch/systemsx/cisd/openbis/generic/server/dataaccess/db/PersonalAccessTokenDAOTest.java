package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.PersonalAccessTokenDAO.HashGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenDAOTest
{

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test() throws IOException
    {
        File file = getFile("test-personal-access-tokens.json");
        file.delete();

        Properties properties = new Properties();
        properties.setProperty(PersonalAccessTokenDAO.PERSONAL_ACCESS_TOKENS_FILE_PATH, file.getAbsolutePath());

        TestGenerator generator = new TestGenerator();
        TestListener listener = new TestListener();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(properties, generator);
        dao.addListener(listener);

        PersonalAccessToken tokenCreationA1 = new PersonalAccessToken();
        tokenCreationA1.setOwnerId("test owner");
        tokenCreationA1.setSessionName("test session A");
        tokenCreationA1.setValidFromDate(new Date(1));
        tokenCreationA1.setValidToDate(new Date(2));

        PersonalAccessToken tokenCreationB1 = new PersonalAccessToken();
        tokenCreationB1.setOwnerId("test owner");
        tokenCreationB1.setSessionName("test session B");
        tokenCreationB1.setValidFromDate(new Date(10));
        tokenCreationB1.setValidToDate(new Date(20));

        // create token A1
        PersonalAccessToken tokenA1 = dao.createToken(tokenCreationA1);
        assertToken(tokenA1, "token-1", tokenCreationA1);
        assertJsonFile(file.getName(), "test-expected-1.json");

        // create token B1
        PersonalAccessToken tokenB1 = dao.createToken(tokenCreationB1);
        assertToken(tokenB1, "token-2", tokenCreationB1);
        assertJsonFile(file.getName(), "test-expected-2.json");

        PersonalAccessTokenSession sessionA1 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertSession(sessionA1, "session-1", tokenA1);

        PersonalAccessTokenSession sessionB1 = dao.getSessionByUserIdAndSessionName(tokenCreationB1.getOwnerId(), tokenCreationB1.getSessionName());
        assertSession(sessionB1, "session-2", tokenB1);

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenA1, tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionA1, sessionB1);

        PersonalAccessToken tokenCreationA2 = new PersonalAccessToken();
        tokenCreationA2.setOwnerId(tokenCreationA1.getOwnerId());
        tokenCreationA2.setSessionName(tokenCreationA1.getSessionName());
        tokenCreationA2.setValidFromDate(new Date(2));
        tokenCreationA2.setValidToDate(new Date(3));

        // create token A2
        PersonalAccessToken tokenA2 = dao.createToken(tokenCreationA2);
        assertToken(tokenA2, "token-3", tokenCreationA2);
        assertJsonFile(file.getName(), "test-expected-3.json");

        PersonalAccessTokenSession sessionA2 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertSession(sessionA2, sessionA1, tokenA1.getValidFromDate(), tokenA2.getValidToDate());

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenA1, tokenA2, tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionA2, sessionB1);

        // delete token A1
        dao.deleteToken(tokenA1.getHash());
        assertJsonFile(file.getName(), "test-expected-4.json");

        PersonalAccessTokenSession sessionA3 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertSession(sessionA3, sessionA2, tokenA2.getValidFromDate(), tokenA2.getValidToDate());

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenA2, tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionA3, sessionB1);

        // delete token A2
        dao.deleteToken(tokenA2.getHash());
        assertJsonFile(file.getName(), "test-expected-5.json");

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionB1);

        PersonalAccessTokenSession sessionA4 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertNull(sessionA4);

        listener.assertCreatedSessions(sessionA1, sessionB1);
        listener.assertUpdatedSessions(sessionA2, sessionA3);
        listener.assertDeletedSessions(sessionA3);

        file.delete();
    }

    private void assertToken(PersonalAccessToken token, String hash, PersonalAccessToken creation)
    {
        assertNotSame(token, creation);
        assertEquals(token.getHash(), hash);
        assertEquals(token.getOwnerId(), creation.getOwnerId());
        assertEquals(token.getSessionName(), creation.getSessionName());
        assertEquals(token.getValidFromDate(), creation.getValidFromDate());
        assertEquals(token.getValidToDate(), creation.getValidToDate());
        assertNull(token.getAccessDate());
    }

    private void assertSession(PersonalAccessTokenSession session, String hash, PersonalAccessToken token)
    {
        assertEquals(session.getHash(), hash);
        assertNotEquals(session.getHash(), token.getHash());
        assertEquals(session.getOwnerId(), token.getOwnerId());
        assertEquals(session.getName(), token.getSessionName());
        assertEquals(session.getValidFromDate(), token.getValidFromDate());
        assertEquals(session.getValidToDate(), token.getValidToDate());
        assertNull(session.getAccessDate());
    }

    private void assertSession(PersonalAccessTokenSession session, PersonalAccessTokenSession previousSession, Date validFrom, Date validTo)
    {
        assertEquals(session.getHash(), previousSession.getHash());
        assertEquals(session.getOwnerId(), previousSession.getOwnerId());
        assertEquals(session.getName(), previousSession.getName());
        assertEquals(session.getValidFromDate(), validFrom);
        assertEquals(session.getValidToDate(), validTo);
        assertNull(session.getAccessDate());
    }

    private void assertJsonFile(String actualFileName, String expectedFileName) throws IOException
    {
        File actualFile = getFile(actualFileName);
        File expectedFile = getFile(expectedFileName);

        String actualContent = FileUtilities.loadToString(actualFile);
        String expectedContent = FileUtilities.loadToString(expectedFile);

        Object actualObject = mapper.readValue(actualContent, Map.class);
        Object expectedObject = mapper.readValue(expectedContent, Map.class);

        assertEquals(actualObject, expectedObject, "Actual:\n" + actualContent + "\nExpected:\n" + expectedContent);
    }

    private File getFile(String fileName)
    {
        TestResources resources = new TestResources(getClass());
        return resources.getResourceFile(fileName);
    }

    private static class TestListener implements IPersonalAccessTokenDAO.Listener
    {

        private final List<PersonalAccessTokenSession> createdSessions = new ArrayList<>();

        private final List<PersonalAccessTokenSession> updatedSessions = new ArrayList<>();

        private final List<PersonalAccessTokenSession> deletedSessions = new ArrayList<>();

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

    private static class TestGenerator implements HashGenerator
    {
        private int tokenCounter = 1;

        private int sessionCounter = 1;

        @Override public String generateTokenHash()
        {
            return "token-" + tokenCounter++;
        }

        @Override public String generateSessionHash()
        {
            return "session-" + sessionCounter++;
        }
    }
}
