package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.PersonalAccessTokenDAO.HashGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

public class PersonalAccessTokenDAOTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
    }

    @Test
    public void test() throws IOException
    {
        File file = getFile("test-personal-access-tokens.json");
        file.delete();

        Properties properties = new Properties();
        properties.setProperty(PersonalAccessTokenDAO.PERSONAL_ACCESS_TOKENS_FILE_PATH, file.getAbsolutePath());

        TestGenerator generator = new TestGenerator();
        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(properties, generator);

        PersonalAccessToken tokenCreationA1 = tokenCreation("test owner", "test session A", new Date(5), new Date(10));
        PersonalAccessToken tokenCreationB1 = tokenCreation("test owner", "test session B", new Date(10), new Date(20));

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

        PersonalAccessToken tokenCreationA2 =
                tokenCreation(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName(), new Date(10), new Date(15));

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

        file.delete();
    }

    @Test
    public void testWithNonExistentFile()
    {
        File file = getFile("i-do-not-exist.json");

        Properties properties = new Properties();
        properties.setProperty(PersonalAccessTokenDAO.PERSONAL_ACCESS_TOKENS_FILE_PATH, file.getAbsolutePath());

        TestGenerator generator = new TestGenerator();
        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(properties, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);
    }

    @Test
    public void testWithIncorrectFile()
    {
        File file = getFile("test-incorrect.json");

        Properties properties = new Properties();
        properties.setProperty(PersonalAccessTokenDAO.PERSONAL_ACCESS_TOKENS_FILE_PATH, file.getAbsolutePath());

        TestGenerator generator = new TestGenerator();
        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(properties, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);

        assertContains("ERROR OPERATION.PersonalAccessTokenDAO - Loading of personal access tokens file failed.",
                logRecorder.getLogContent());
    }

    @Test
    public void testWithTokensOnly() throws IOException
    {
        File file = getFile("test-tokens-only.json");

        Properties properties = new Properties();
        properties.setProperty(PersonalAccessTokenDAO.PERSONAL_ACCESS_TOKENS_FILE_PATH, file.getAbsolutePath());

        TestGenerator generator = new TestGenerator();
        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(properties, generator);

        assertEquals(dao.listTokens().size(), 3);
        assertEquals(dao.listSessions().size(), 2);

        assertJsonFile(file.getName(), "test-tokens-only-with-generated-sessions.json");
    }

    private PersonalAccessToken tokenCreation(String ownerId, String sessionName, Date validFrom, Date validTo)
    {
        PersonalAccessToken creation = new PersonalAccessToken();
        creation.setOwnerId(ownerId);
        creation.setRegistratorId("test registrator");
        creation.setModifierId("test modifier");
        creation.setSessionName(sessionName);
        creation.setValidFromDate(validFrom);
        creation.setValidToDate(validTo);
        creation.setRegistrationDate(new Date(1));
        creation.setModificationDate(new Date(1));
        return creation;
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

    private static class TestGenerator implements HashGenerator
    {
        private int tokenCounter = 1;

        private int sessionCounter = 1;

        @Override public String generateTokenHash(String user)
        {
            return "token-" + tokenCounter++;
        }

        @Override public String generateSessionHash(String user)
        {
            return "session-" + sessionCounter++;
        }
    }
}
