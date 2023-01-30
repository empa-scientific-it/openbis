package ch.systemsx.cisd.openbis.generic.server.pat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;

public class PersonalAccessTokenValidityWarningTaskTest
{

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final PersonPE USER_1;

    private static final PersonPE USER_2;

    private static final PersonPE USER_WITHOUT_EMAIL;

    private static final PersonPE NON_EXISTENT_USER;

    static
    {
        USER_1 = new PersonPE();
        USER_1.setUserId("user_1");
        USER_1.setEmail("user_1@email.com");

        USER_2 = new PersonPE();
        USER_2.setUserId("user_2");
        USER_2.setEmail("user_2@email.com");

        USER_WITHOUT_EMAIL = new PersonPE();
        USER_WITHOUT_EMAIL.setUserId("i_dont_have_email");

        NON_EXISTENT_USER = new PersonPE();
        NON_EXISTENT_USER.setUserId("i_don_exist");
    }

    @Test
    public void test() throws Exception
    {
        Mockery mockery = new Mockery();

        IPersonalAccessTokenConfig personalAccessTokenConfig = mockery.mock(IPersonalAccessTokenConfig.class);
        IPersonalAccessTokenDAO personalAccessTokenDAO = mockery.mock(IPersonalAccessTokenDAO.class);
        IPersonDAO personDAO = mockery.mock(IPersonDAO.class);
        IMailClient mailClient = mockery.mock(IMailClient.class);
        ITimeProvider timeProvider = mockery.mock(ITimeProvider.class);

        PersonalAccessToken user1TokenExpired = new PersonalAccessToken();
        user1TokenExpired.setHash("token-1");
        user1TokenExpired.setSessionName("session-A");
        user1TokenExpired.setOwnerId(USER_1.getUserId());
        user1TokenExpired.setValidFromDate(parseDate("2020-01-01 00:00:00"));
        user1TokenExpired.setValidToDate(parseDate("2021-01-01 00:00:00"));

        PersonalAccessToken user1TokenSoonToBeExpired = new PersonalAccessToken();
        user1TokenSoonToBeExpired.setHash("token-2");
        user1TokenSoonToBeExpired.setSessionName("session-A");
        user1TokenSoonToBeExpired.setOwnerId(USER_1.getUserId());
        user1TokenSoonToBeExpired.setValidFromDate(parseDate("2022-01-01 00:00:00"));
        user1TokenSoonToBeExpired.setValidToDate(parseDate("2022-01-02 00:00:00"));

        PersonalAccessToken user1TokenSoonToBeExpired2 = new PersonalAccessToken();
        user1TokenSoonToBeExpired2.setHash("token-2b");
        user1TokenSoonToBeExpired2.setSessionName("session-A");
        user1TokenSoonToBeExpired2.setOwnerId(USER_1.getUserId());
        user1TokenSoonToBeExpired2.setValidFromDate(parseDate("2022-01-01 01:00:00"));
        user1TokenSoonToBeExpired2.setValidToDate(parseDate("2022-01-02 01:00:00"));

        PersonalAccessToken user1TokenStillValidForALongTime = new PersonalAccessToken();
        user1TokenStillValidForALongTime.setHash("token-3");
        user1TokenStillValidForALongTime.setSessionName("session-B");
        user1TokenStillValidForALongTime.setOwnerId(USER_1.getUserId());
        user1TokenStillValidForALongTime.setValidFromDate(parseDate("2022-01-01 00:00:00"));
        user1TokenStillValidForALongTime.setValidToDate(parseDate("2023-01-01 00:00:00"));

        PersonalAccessToken user2TokenSoonToBeExpired = new PersonalAccessToken();
        user2TokenSoonToBeExpired.setHash("token-4");
        user2TokenSoonToBeExpired.setSessionName("session-C");
        user2TokenSoonToBeExpired.setOwnerId(USER_2.getUserId());
        user2TokenSoonToBeExpired.setValidFromDate(parseDate("2022-01-01 00:00:00"));
        user2TokenSoonToBeExpired.setValidToDate(parseDate("2022-01-02 00:00:00"));

        PersonalAccessToken userWithoutEmailToken = new PersonalAccessToken();
        userWithoutEmailToken.setHash("token-5");
        userWithoutEmailToken.setSessionName("session-D");
        userWithoutEmailToken.setOwnerId(USER_WITHOUT_EMAIL.getUserId());
        userWithoutEmailToken.setValidFromDate(parseDate("2022-01-01 00:00:00"));
        userWithoutEmailToken.setValidToDate(parseDate("2022-01-02 00:00:00"));

        PersonalAccessToken nonExistentUserToken = new PersonalAccessToken();
        nonExistentUserToken.setHash("token-6");
        nonExistentUserToken.setSessionName("session-E");
        nonExistentUserToken.setOwnerId(NON_EXISTENT_USER.getUserId());
        nonExistentUserToken.setValidFromDate(parseDate("2022-01-01 00:00:00"));
        nonExistentUserToken.setValidToDate(parseDate("2022-01-02 00:00:00"));

        String emailUser1 = readEmail("emailUser1.txt");
        String emailUser2 = readEmail("emailUser2.txt");

        mockery.checking(new Expectations()
        {
            {
                allowing(personalAccessTokenConfig).arePersonalAccessTokensEnabled();
                will(returnValue(true));

                allowing(personalAccessTokenConfig).getPersonalAccessTokensValidityWarningPeriod();
                will(returnValue(36 * 3600L));

                allowing(personalAccessTokenDAO).listTokens();
                will(returnValue(Arrays.asList(user1TokenExpired, user1TokenSoonToBeExpired, user1TokenSoonToBeExpired2,
                        user1TokenStillValidForALongTime, user2TokenSoonToBeExpired, userWithoutEmailToken, nonExistentUserToken)));

                one(personalAccessTokenDAO).deleteToken(user1TokenExpired.getHash());

                allowing(personDAO).tryFindPersonByUserId(with(USER_1.getUserId()));
                will(returnValue(USER_1));

                allowing(personDAO).tryFindPersonByUserId(with(USER_2.getUserId()));
                will(returnValue(USER_2));

                allowing(personDAO).tryFindPersonByUserId(with(USER_WITHOUT_EMAIL.getUserId()));
                will(returnValue(USER_WITHOUT_EMAIL));

                allowing(personDAO).tryFindPersonByUserId(with(NON_EXISTENT_USER.getUserId()));
                will(returnValue(null));

                one(mailClient).sendEmailMessage(
                        with("openBIS personal access tokens expiration warning"),
                        with(emailUser1),
                        with((EMailAddress) null),
                        with((EMailAddress) null),
                        with(new EMailAddress[] { new EMailAddress(USER_1.getEmail()) }));

                one(mailClient).sendEmailMessage(
                        with("openBIS personal access tokens expiration warning"),
                        with(emailUser2),
                        with((EMailAddress) null),
                        with((EMailAddress) null),
                        with(new EMailAddress[] { new EMailAddress(USER_2.getEmail()) }));

                allowing(timeProvider).getTimeInMilliseconds();
                will(returnValue(parseDate("2022-01-01 00:00:00").getTime()));
            }
        });

        PersonalAccessTokenValidityWarningTask task =
                new PersonalAccessTokenValidityWarningTask(personalAccessTokenConfig, personalAccessTokenDAO, personDAO, mailClient, timeProvider);

        task.execute();
    }

    private String readEmail(String emailFileName) throws IOException
    {
        TestResources resources = new TestResources(getClass());
        File file = resources.getResourceFile(emailFileName);
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    private Date parseDate(String dateString) throws ParseException
    {
        return DATE_FORMAT.parse(dateString);
    }

}
