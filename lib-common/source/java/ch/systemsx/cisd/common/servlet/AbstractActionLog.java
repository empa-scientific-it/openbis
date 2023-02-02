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

package ch.systemsx.cisd.common.servlet;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * Abstract super class of action logs. Action logs are logged by one of the following loggers: Authentication logger, access logger, and tracking
 * logger.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractActionLog implements IActionLog
{
    private static final String FAILED = "FAILED";

    private static final String OK = "OK";

    private static final String USER_SESSION_TEMPLATE = "{USER: %s, WEBSESSION: %s} logout%s";

    private static final String USER_HOST_SESSION_TEMPLATE =
            "{USER: %s, HOST: %s, WEBSESSION: %s} ";

    /** Authentication logger. Name is specified by {@link LogCategory#AUTH}. */
    protected final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH);

    /** Access logger. Name is specified by {@link LogCategory#ACCESS}. */
    protected final Logger accessLog = LogFactory.getLogger(LogCategory.ACCESS);

    /** Tracking logger. Name is specified by {@link LogCategory#TRACKING}. */
    protected final Logger trackingLog = LogFactory.getLogger(LogCategory.TRACKING);

    protected final IRequestContextProvider requestContextProvider;

    protected final IRemoteHostProvider remoteHostProvider;

    protected static String getSuccessString(final boolean success)
    {
        return success ? OK : FAILED;
    }

    /**
     * Creates an instance for the specified request context provider. It is used to provide {@link HttpSession} in all methods except
     * {@link #logLogout(HttpSession)}.
     * 
     * @param requestContextProvider
     */
    public AbstractActionLog(final IRequestContextProvider requestContextProvider)
    {
        this.requestContextProvider = requestContextProvider;
        this.remoteHostProvider = new RequestContextProviderAdapter(requestContextProvider);
    }

    @Override
    public void logFailedLoginAttempt(final String userCode)
    {
        if (authenticationLog.isInfoEnabled())
        {
            final String logMessage =
                    String.format("{USER: %s, HOST: %s} login: FAILED", userCode,
                            remoteHostProvider.getRemoteHost());
            authenticationLog.info(logMessage);
        }
    }

    @Override
    public void logSuccessfulLogin()
    {
        if (authenticationLog.isInfoEnabled())
        {
            final String userHostSessionDescription = getUserHostSessionDescription();
            authenticationLog.info(userHostSessionDescription + "login: OK");
        }
    }

    @Override
    public void logLogout(final HttpSession httpSession)
    {
        if (authenticationLog.isInfoEnabled())
        {
            final String userName = getUserCode(httpSession);
            final String id = httpSession.getId();
            final long diff = System.currentTimeMillis() - httpSession.getLastAccessedTime();
            final boolean timedOut = diff / 1000.0 >= httpSession.getMaxInactiveInterval();
            final String logoutMsg =
                    String.format(USER_SESSION_TEMPLATE, userName, id,
                            timedOut ? LogoutReason.SESSION_TIMEOUT.getLogText()
                                    : LogoutReason.SESSION_LOGOUT.getLogText());
            authenticationLog.info(logoutMsg);
        }
    }

    @Override
    public void logSetSessionUser(String oldUserCode, String newUserCode, final boolean success)
    {
        if (authenticationLog.isInfoEnabled())
        {
            authenticationLog.info(getUserHostSessionDescription()
                    + String.format("set_user_code to '%s': %s", newUserCode,
                            getSuccessString(success)));
        }
    }

    /**
     * Returns a short description which contains user code, client host, and session id.
     */
    protected String getUserHostSessionDescription()
    {
        final HttpSession httpSession = getHttpSession();
        final String remoteHost = remoteHostProvider.getRemoteHost();
        final String userName;
        final String id;
        if (httpSession == null)
        {
            userName = "UNKNOWN";
            id = "UNKNOWN";
        } else
        {
            userName = getUserCode(httpSession);
            id = httpSession.getId();
        }
        return String.format(USER_HOST_SESSION_TEMPLATE, userName, remoteHost, id);
    }

    /**
     * Extracts the user code from the specified session.
     */
    protected abstract String getUserCode(HttpSession httpSession);

    protected HttpSession getHttpSession()
    {
        try
        {
            return requestContextProvider.getHttpServletRequest().getSession();
        } catch (RuntimeException ex)
        {
            return null;
        }
    }

}
