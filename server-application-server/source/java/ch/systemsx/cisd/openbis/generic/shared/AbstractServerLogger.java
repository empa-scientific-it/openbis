/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ILogMessagePrefixGenerator;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * An <i>abstract</i> server logger.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServerLogger implements IServer
{
    private static final String RESULT_SUCCESS = "";

    private static final String RESULT_FAILURE = " ...FAILED";

    private final Logger authLog;

    private final Logger accessLog;

    private final Logger trackingLog;

    protected final ILogMessagePrefixGenerator<Session> logMessagePrefixGenerator;

    private final ISessionManager<Session> sessionManagerOrNull;

    private final IInvocationLoggerContext context;

    private final String prefixOrNull;

    public AbstractServerLogger(final ISessionManager<Session> sessionManagerNull,
            IInvocationLoggerContext context)
    {
        this.sessionManagerOrNull = sessionManagerNull;
        this.context = context;
        logMessagePrefixGenerator = new LogMessagePrefixGenerator();
        String sessionTokenOrNull = context.tryToGetSessionToken();
        prefixOrNull = tryToCreatePrefix(sessionTokenOrNull);

        authLog = LogFactory.getLogger(LogCategory.AUTH, getClass());
        accessLog = LogFactory.getLogger(LogCategory.ACCESS, getClass());
        trackingLog = LogFactory.getLogger(LogCategory.TRACKING, getClass());
    }

    // helper methods for logging collections and arrays

    protected String abbreviate(Collection<?> c)
    {
        if (c == null)
        {
            return "null";
        }
        return CollectionUtils.abbreviate(c, 10);
    }

    protected String abbreviate(Object[] object)
    {
        return CollectionUtils.abbreviate(object, 10);
    }

    protected int size(Collection<?> c)
    {
        if (c == null)
        {
            return 0;
        }
        return c.size();
    }

    protected int size(Object[] t)
    {
        if (t == null)
        {
            return 0;
        }
        return t.length;
    }

    //

    private String tryToCreatePrefix(String sessionTokenOrNull)
    {
        if (sessionTokenOrNull == null)
        {
            return null;
        }
        return tryToCreatePrefixFromSession(sessionTokenOrNull);
    }

    private String tryToCreatePrefixSecondTime(String sessionToken)
    {
        if (prefixOrNull != null)
        {
            return prefixOrNull;
        }
        return tryToCreatePrefixFromSession(sessionToken);
    }

    private String tryToCreatePrefixFromSession(String sessionToken)
    {
        if (sessionManagerOrNull == null
                || sessionManagerOrNull.isAWellFormedSessionToken(sessionToken) == false)
        {
            return "[SESSION:" + sessionToken + "]";
        }
        // Do not trigger any session expiration at this point, this might lead to leaking database
        // connections, see BIS-205
        final Session session = sessionManagerOrNull.tryGetSession(sessionToken);
        return (session == null) ? "[NO SESSION]" : logMessagePrefixGenerator.createPrefix(session);
    }

    protected final void logAuth(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(authLog, Level.INFO, sessionToken, commandName, parameterDisplayFormat,
                parameters);
    }

    protected final void logAccess(final String sessionToken, final String commandName)
    {
        logAccess(sessionToken, commandName, "");
    }

    protected final void logAccess(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(accessLog, Level.INFO, sessionToken, commandName, parameterDisplayFormat,
                parameters);
    }

    protected final void logAccess(final Level level, final String sessionToken,
            final String commandName, final String parameterDisplayFormat,
            final Object... parameters)
    {
        logMessage(accessLog, level, sessionToken, commandName, parameterDisplayFormat, parameters);
    }

    protected final void logTracking(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(trackingLog, Level.INFO, sessionToken, commandName, parameterDisplayFormat,
                parameters);
    }

    private final void logMessage(final Logger logger, final Level level,
            final String sessionToken, final String commandName,
            String parameterDisplayFormat, Object[] parameters)
    {
        if (logger.isEnabledFor(level) == false)
        {
            return;
        }

        for (int i = 0; i < parameters.length; i++)
        {
            final Object parameter = parameters[i];
            if (parameter == null)
            {
                parameters[i] = LogMessagePrefixGenerator.UNDEFINED;
            } else
            {
                Object unquotedParameter = parameter;
                if (parameter instanceof Collection)
                {
                    unquotedParameter = CollectionUtils.abbreviate((Collection<?>) unquotedParameter, 20);
                }
                parameters[i] = "'" + unquotedParameter + "'";
            }
        }

        String theParameterDisplayFormat = parameterDisplayFormat;
        Object[] theParameters = parameters;

        if (sessionManagerOrNull != null && sessionManagerOrNull.isAWellFormedSessionToken(sessionToken))
        {
            Session session = sessionManagerOrNull.tryGetSession(sessionToken);

            if (session != null && session.isOnBehalfSession())
            {
                String creatorUserId = session.tryGetCreatorPerson() != null ? session.tryGetCreatorPerson().getUserId() : null;
                String userId = session.tryGetPerson() != null ? session.tryGetPerson().getUserId() : null;

                theParameters = new Object[parameters.length + 2];
                theParameters[0] = creatorUserId;
                theParameters[1] = userId;

                for (int i = 0; i < parameters.length; i++)
                {
                    theParameters[i + 2] = parameters[i];
                }

                theParameterDisplayFormat = "USER_(%s)_ON_BEHALF_OF_(%s) " + parameterDisplayFormat;
            }
        }

        final String message = String.format(theParameterDisplayFormat, theParameters);

        if (context.invocationFinished())
        {
            final String invocationStatusMessage = getInvocationStatusMessage();
            final String elapsedTimeMessage = getElapsedTimeMessage();

            // We put on purpose 2 spaces between the command and the message derived from the
            // parameters.
            logger.log(
                    level,
                    tryToCreatePrefixSecondTime(sessionToken)
                            + String.format(": (%s) %s  %s%s", elapsedTimeMessage, commandName,
                                    message, invocationStatusMessage));
        } else
        {
            // We put on purpose 2 spaces between the command and the message derived from the
            // parameters.
            logger.log(
                    level,
                    tryToCreatePrefixSecondTime(sessionToken)
                            + String.format(": (START) %s  %s", commandName,
                                    message));
        }
    }

    private String getInvocationStatusMessage()
    {
        return context.invocationWasSuccessful() ? RESULT_SUCCESS : RESULT_FAILURE;
    }

    private String getElapsedTimeMessage()
    {
        return context.getElapsedTime() + "ms";
    }

    //
    // ISessionProvider
    //

    @Override
    public final IAuthSession getAuthSession(final String sessionToken) throws UserFailureException
    {
        return null;
    }

    //
    // IServer
    //

    @Override
    public final int getVersion()
    {
        return IServer.VERSION;
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        logAccess(sessionToken, "getServerInformation", "SESSION(%s)", sessionToken);
        return null;
    }

    @Override
    public final SessionContextDTO tryAuthenticate(final String user, final String password)
    {
        // No logging because already done by the session manager
        return null;
    }

    @Override
    public SessionContextDTO tryAuthenticateAs(String user, String password, String asUser)
    {
        // No logging because already done by the session manager
        return null;
    }

    @Override
    public SessionContextDTO tryAuthenticateAnonymously()
    {
        return null;
    }

    @Override
    public SessionContextDTO tryToAuthenticate(String sessionToken)
    {
        return null;
    }

    @Override
    public SessionContextDTO tryGetSession(String sessionToken)
    {
        logAccess(sessionToken, "tryGetSession", "SESSION(%s)", sessionToken);
        return null;
    }

    @Override
    public void checkSession(String sessionToken) throws InvalidSessionException
    {
        logAccess(sessionToken, "checkSession", "SESSION(%s)", sessionToken);
    }

    @Override
    public final void logout(final String sessionToken) throws UserFailureException
    {
        // No logging because already done by the session manager
    }

    @Override
    public void expireSession(String sessionToken) throws UserFailureException
    {
        // No logging because already done by the session manager
    }

    @Override
    public void deactivatePersons(String sessionToken, List<String> personsCodes)
    {
        logTracking(sessionToken, "deactivatePersons", "PERSONS(%s)", abbreviate(personsCodes));
    }

    @Override
    public int countActivePersons(String sessionToken)
    {
        // do not log that
        return 0;
    }

    @Override
    public boolean isArchivingConfigured(String sessionToken)
    {
        // Do not log that
        return false;
    }

    @Override
    public boolean isProjectSamplesEnabled(String sessionToken)
    {
        // Do not log that
        return false;
    }

    @Override
    public boolean isProjectLevelAuthorizationEnabled(String sessionToken)
    {
        // Do not log that
        return false;
    }

    @Override
    public boolean isProjectLevelAuthorizationUser(String sessionToken)
    {
        // Do not log that
        return false;
    }

    @Override
    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings,
            int maxEntityVisits)
    {
        logTracking(sessionToken, "save_display_settings", "");
    }

    @Override
    public void updateDisplaySettings(String sessionToken,
            IDisplaySettingsUpdate displaySettingsUpdate)
    {
        logTracking(sessionToken, "update_display_settings", "UPDATE (%s)", displaySettingsUpdate);
    }

    @Override
    public DisplaySettings getDefaultDisplaySettings(String sessionToken)
    {
        logTracking(sessionToken, "get_default_display_settings", "");
        return null;
    }

    @Override
    public void changeUserHomeSpace(String sessionToken, TechId spaceIdOrNull)
    {
        String spaceId = spaceIdOrNull == null ? "null" : spaceIdOrNull.toString();
        logTracking(sessionToken, "change_user_home_space", "SPACE_ID (%s)", spaceId);
    }

    @Override
    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridId)
    {
        logAccess(sessionToken, "listGridCustomColumns", "GRID_ID(%s)", gridId);
        return null;
    }

    @Override
    public void setBaseIndexURL(String sessionToken, String baseURL)
    {
        logAccess(sessionToken, "set_base_url", "BASE_URL(%s)", baseURL);
    }

    @Override
    public String getBaseIndexURL(String sessionToken)
    {
        logAccess(sessionToken, "get_base_url", "");
        return null;
    }

    @Override
    public void setSessionUser(String sessionToken, String userID)
    {
        logMessage(authLog, Level.INFO, sessionToken, "set_session_user", "USER(%s)", new Object[] { userID });
    }

    public int unarchiveDatasets(String sessionToken, List<String> datasetCodes)
    {
        logTracking(sessionToken, "unarchiveDatasets", "DATASETS(%s)", abbreviate(datasetCodes));
        return 0;
    }

    public int archiveDatasets(String sessionToken, List<String> datasetCodes,
            boolean removeFromDataStore, Map<String, String> options)
    {
        logTracking(sessionToken, "archiveDatasets", "DATASETS(%s), REMOVE_FROM_DATA_STORE(%s)",
                abbreviate(datasetCodes), removeFromDataStore);
        return 0;
    }
}
