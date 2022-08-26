package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.PersonalAccessTokenDAO.HashGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

public class PersonalAccessTokenDAOTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    private BufferedAppender logRecorder;

    private Mockery mockery;

    private IPersonDAO personDAO;

    private static final long TEST_VALIDITY_PERIOD = 365 * 24 * 60 * 60;

    private static final PersonPE TEST_OWNER_USER;

    private static final PersonPE TEST_REGISTRATOR_USER;

    private static final PersonPE TEST_MODIFIER_USER;

    private static final PersonPE INACTIVE_USER;

    static
    {
        TEST_OWNER_USER = new PersonPE();
        TEST_OWNER_USER.setId(1000L);
        TEST_OWNER_USER.setUserId("test owner");
        TEST_OWNER_USER.setActive(true);

        TEST_REGISTRATOR_USER = new PersonPE();
        TEST_REGISTRATOR_USER.setId(2000L);
        TEST_REGISTRATOR_USER.setUserId("test registrator");
        TEST_REGISTRATOR_USER.setActive(true);

        TEST_MODIFIER_USER = new PersonPE();
        TEST_MODIFIER_USER.setId(3000L);
        TEST_MODIFIER_USER.setUserId("test modifier");
        TEST_MODIFIER_USER.setActive(true);

        INACTIVE_USER = new PersonPE();
        INACTIVE_USER.setId(4000L);
        INACTIVE_USER.setUserId("inactive user");
        INACTIVE_USER.setActive(false);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        mockery = new Mockery();
        personDAO = mockery.mock(IPersonDAO.class);
        mockery.checking(new Expectations()
        {
            {
                for (PersonPE user : Arrays.asList(TEST_OWNER_USER, TEST_MODIFIER_USER, TEST_REGISTRATOR_USER, INACTIVE_USER))
                {
                    allowing(personDAO).tryFindPersonByUserId(with(user.getUserId()));
                    will(returnValue(user));

                    allowing(personDAO).tryGetByTechId(new TechId(user.getId()));
                    will(returnValue(user));
                }

                allowing(personDAO).tryFindPersonByUserId(with(any(String.class)));
                will(returnValue(null));

                allowing(personDAO).tryGetByTechId(with(any(TechId.class)), with(any(String[].class)));
                will(returnValue(null));
            }
        });
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
        mockery.assertIsSatisfied();
    }

    @Test
    public void testWithMaxValidityPeriod() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-empty.json", 60 * 60);
        TestGenerator generator = new TestGenerator();
        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        PersonalAccessToken creation1 = tokenCreation("test owner", "test session", new Date(0), new Date(2 * 60 * 60 * 1000));
        PersonalAccessToken creation2 = tokenCreation("test owner", "test session", new Date(0), new Date(59 * 60 * 1000));

        PersonalAccessToken token1 = dao.createToken(creation1);
        PersonalAccessToken token2 = dao.createToken(creation2);

        assertEquals(token1.getValidFromDate(), creation1.getValidFromDate());
        assertEquals(token1.getValidToDate(), new Date(60 * 60 * 1000));

        assertEquals(token2.getValidFromDate(), creation2.getValidFromDate());
        assertEquals(token2.getValidToDate(), creation2.getValidToDate());
    }

    @Test
    public void testWithEmptyFile() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-empty.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        PersonalAccessToken tokenCreationA1 = tokenCreation("test owner", "test session A", new Date(5), new Date(10));
        PersonalAccessToken tokenCreationB1 = tokenCreation("test owner", "test session B", new Date(10), new Date(20));

        // create token A1
        PersonalAccessToken tokenA1 = dao.createToken(tokenCreationA1);
        assertToken(tokenA1, "token-1", tokenCreationA1);
        assertJsonFile("test-expected-1.json");

        // create token B1
        PersonalAccessToken tokenB1 = dao.createToken(tokenCreationB1);
        assertToken(tokenB1, "token-2", tokenCreationB1);
        assertJsonFile("test-expected-2.json");

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
        assertJsonFile("test-expected-3.json");

        PersonalAccessTokenSession sessionA2 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertSession(sessionA2, sessionA1, tokenA1.getValidFromDate(), tokenA2.getValidToDate());

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenA1, tokenA2, tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionA2, sessionB1);

        // delete token A1
        dao.deleteToken(tokenA1.getHash());
        assertJsonFile("test-expected-4.json");

        PersonalAccessTokenSession sessionA3 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertSession(sessionA3, sessionA2, tokenA2.getValidFromDate(), tokenA2.getValidToDate());

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenA2, tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionA3, sessionB1);

        // delete token A2
        dao.deleteToken(tokenA2.getHash());
        assertJsonFile("test-expected-5.json");

        AssertionUtil.assertCollectionContainsOnly(dao.listTokens(), tokenB1);
        AssertionUtil.assertCollectionContainsOnly(dao.listSessions(), sessionB1);

        PersonalAccessTokenSession sessionA4 = dao.getSessionByUserIdAndSessionName(tokenCreationA1.getOwnerId(), tokenCreationA1.getSessionName());
        assertNull(sessionA4);
    }

    @Test
    public void testWithNonExistentFile() throws IOException
    {
        IPersonalAccessTokenConfig config = config("i-do-not-exist.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);
    }

    @Test
    public void testWithFileWithIncorrectOwnerId() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-incorrect-owner-id.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);

        assertContains("ERROR OPERATION.PersonalAccessTokenDAO - Loading of personal access tokens file failed.",
                logRecorder.getLogContent());
        assertContains("Cannot deserialize value of type `java.lang.Long` from String \"I_SHOULD_BE_A_NUMBER\": not a valid Long value",
                logRecorder.getLogContent());
    }

    @Test
    public void testWithFileWithValidFromDateEqualToValidToDate() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-valid-from-date-equal-to-valid-to-date.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);

        assertContains("ERROR OPERATION.PersonalAccessTokenDAO - Loading of personal access tokens file failed.",
                logRecorder.getLogContent());
        assertContains("Valid to date has to be after valid from date",
                logRecorder.getLogContent());
    }

    @Test
    public void testWithFileWithValidFromDateAfterToValidToDate() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-valid-from-date-after-valid-to-date.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 0);
        assertEquals(dao.listSessions().size(), 0);

        assertContains("ERROR OPERATION.PersonalAccessTokenDAO - Loading of personal access tokens file failed.",
                logRecorder.getLogContent());
        assertContains("Valid to date has to be after valid from date",
                logRecorder.getLogContent());
    }

    @Test
    public void testWithFileWithMaxValidityPeriod() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-validity-period-longer-than-allowed-maximum.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 1);
        assertEquals(dao.listSessions().size(), 1);

        PersonalAccessToken token = dao.listTokens().get(0);

        assertEquals(token.getValidFromDate(), new Date(0));
        assertEquals(token.getValidToDate(), new Date(TEST_VALIDITY_PERIOD * 1000));

        assertJsonFile("test-validity-period-longer-than-allowed-maximum.json");
    }

    @Test
    public void testWithFileWithTokensOnly() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-tokens-only.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertEquals(dao.listTokens().size(), 3);
        assertEquals(dao.listSessions().size(), 2);

        assertJsonFile("test-tokens-only-with-generated-sessions.json");
    }

    @Test
    public void testWithFileWithUnknownUsers() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-unknown-users.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertNull(dao.getTokenByHash("token-1"));
        assertNotNull(dao.getTokenByHash("token-2"));

        assertNull(dao.getSessionByHash("session-1"));
        assertNotNull(dao.getSessionByHash("session-2"));

        assertEquals(dao.listTokens().size(), 1);
        assertEquals(dao.listSessions().size(), 1);

        assertJsonFile("test-unknown-users.json");
    }

    @Test
    public void testWithFileWithInactiveUsers() throws IOException
    {
        IPersonalAccessTokenConfig config = config("test-inactive-users.json", TEST_VALIDITY_PERIOD);
        TestGenerator generator = new TestGenerator();

        PersonalAccessTokenDAO dao = new PersonalAccessTokenDAO(config, personDAO, generator);

        assertNull(dao.getTokenByHash("token-1"));
        assertNotNull(dao.getTokenByHash("token-2"));

        assertNull(dao.getSessionByHash("session-1"));
        assertNotNull(dao.getSessionByHash("session-2"));

        assertEquals(dao.listTokens().size(), 1);
        assertEquals(dao.listSessions().size(), 1);

        assertJsonFile("test-inactive-users.json");
    }

    private IPersonalAccessTokenConfig config(String initialJsonFileName, long maxValidityPeriod) throws IOException
    {
        IPersonalAccessTokenConfig config = mockery.mock(IPersonalAccessTokenConfig.class);

        File initialFile = getFile(initialJsonFileName);
        File file = getFile("test.json");
        file.delete();
        if (initialFile.exists())
        {
            FileUtils.copyFile(initialFile, file);
        }

        mockery.checking(new Expectations()
        {
            {
                allowing(config).getPersonalAccessTokensFilePath();
                will(returnValue(file.getAbsolutePath()));

                allowing(config).getPersonalAccessTokensMaxValidityPeriod();
                will(returnValue(maxValidityPeriod));
            }
        });

        return config;
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

    private void assertJsonFile(String expectedFileName) throws IOException
    {
        File actualFile = getFile("test.json");
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
