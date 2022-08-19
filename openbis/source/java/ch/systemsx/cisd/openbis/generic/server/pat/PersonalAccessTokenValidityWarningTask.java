package ch.systemsx.cisd.openbis.generic.server.pat;

import static java.util.stream.Collectors.groupingBy;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;

public class PersonalAccessTokenValidityWarningTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "personal-access-token-validity-warning-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 24 * 60 * 60;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private IPersonalAccessTokenConfig personalAccessTokenConfig;

    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    private IPersonDAO personDAO;

    private IMailClient mailClient;

    private ITimeProvider timeProvider;

    PersonalAccessTokenValidityWarningTask(IPersonalAccessTokenConfig personalAccessTokenConfig, IPersonalAccessTokenDAO personalAccessTokenDAO,
            IPersonDAO personDAO, IMailClient mailClient, ITimeProvider timeProvider)
    {
        this.personalAccessTokenConfig = personalAccessTokenConfig;
        this.personalAccessTokenDAO = personalAccessTokenDAO;
        this.personDAO = personDAO;
        this.mailClient = mailClient;
        this.timeProvider = timeProvider;
    }

    @Override public void setUp(final String pluginName, final Properties properties)
    {
        personalAccessTokenConfig = CommonServiceProvider.getApplicationContext().getBean(IPersonalAccessTokenConfig.class);
        personalAccessTokenDAO = CommonServiceProvider.getApplicationContext().getBean(IPersonalAccessTokenDAO.class);
        personDAO = CommonServiceProvider.getApplicationContext().getBean(IDAOFactory.class).getPersonDAO();
        final MailClientParameters mailClientParameters = CommonServiceProvider.getApplicationContext().getBean(MailClientParameters.class);
        mailClient = new MailClient(mailClientParameters);
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        operationLog.info("Task " + pluginName + " initialized.");
    }

    @Override public void execute()
    {
        if (!personalAccessTokenConfig.arePersonalAccessTokensEnabled())
        {
            return;
        }

        Map<String, List<PersonalAccessToken>> userTokensMap = personalAccessTokenDAO.listTokens().stream()
                .filter(this::isValidityPeriodWarningNeeded)
                .sorted(Comparator.comparing(this::getValidityPeriodLeft))
                .collect(groupingBy(PersonalAccessToken::getOwnerId));

        for (String userId : userTokensMap.keySet())
        {
            sendEmail(userId, userTokensMap.get(userId));
        }
    }

    private void sendEmail(final String userId, final List<PersonalAccessToken> userTokens)
    {
        if (userTokens.isEmpty())
        {
            return;
        }

        PersonPE user = personDAO.tryFindPersonByUserId(userId);

        if (user == null)
        {
            return;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty())
        {
            operationLog.info("Personal access tokens expiration warning email could not be sent to user: " + user.getUserId()
                    + " because the user's email address is empty.");
            return;
        }

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("The following personal access tokens are going to expire soon:\n");

        for (PersonalAccessToken userToken : userTokens)
        {
            emailContent.append("- hash: ").append(userToken.getHash())
                    .append(", session name: ").append(userToken.getSessionName())
                    .append(", valid from: ").append(DATE_FORMAT.format(userToken.getValidFromDate()))
                    .append(", valid to: ").append(DATE_FORMAT.format(userToken.getValidToDate()));

            long validityPeriodLeft = getValidityPeriodLeft(userToken);

            if (validityPeriodLeft > 0)
            {
                emailContent.append(", expires in: ").append(DateTimeUtils.renderDuration(validityPeriodLeft)).append("\n");
            } else
            {
                emailContent.append(", already expired").append("\n");
            }
        }

        try
        {
            mailClient.sendEmailMessage("openBIS personal access tokens expiration warning", emailContent.toString(), null, null,
                    new EMailAddress(user.getEmail()));
        } catch (Exception e)
        {
            operationLog.warn(
                    "Personal access tokens expiration warning email could not be sent to user: " + user.getUserId() + " with email: "
                            + user.getEmail(),
                    e);
        }
    }

    private boolean isValidityPeriodWarningNeeded(PersonalAccessToken token)
    {
        return getValidityPeriodLeft(token) < personalAccessTokenConfig.getPersonalAccessTokensValidityWarningPeriod() * 1000;
    }

    private long getValidityPeriodLeft(PersonalAccessToken token)
    {
        return Math.max(0, token.getValidToDate().getTime() - timeProvider.getTimeInMilliseconds());
    }

}
