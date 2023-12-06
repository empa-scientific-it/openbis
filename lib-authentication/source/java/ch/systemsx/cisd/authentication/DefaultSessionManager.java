/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.authentication;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * Default session manager. Needs
 * <ul>
 * <li>a {@link ISessionFactory} for creating new session objects,
 * <li>a {@link ILogMessagePrefixGenerator} for generating log messages which are logged by a logger with category {@link LogCategory#AUTH},
 * <li>a {@link IAuthenticationService} for authenticating users,
 * <li>a {@link IRemoteHostProvider} for providing the remote host of the user client.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DefaultSessionManager<T extends BasicSession> implements ISessionManager<T>
{
    private enum SessionClosingReason
    {
        LOGOUT, SESSION_EXPIRATION, SESSIONS_LIMIT
    }

    public static final File NO_LOGIN_FILE = new File("./etc/nologin.html");

    private static final String LOGOUT_PREFIX = "LOGOUT: ";

    private static final String LOGIN_PREFIX_TEMPLATE = "(%dms) LOGIN: ";

    private static final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH,
            DefaultSessionManager.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DefaultSessionManager.class);

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DefaultSessionManager.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    protected static final class FullSession<S extends BasicSession>
    {
        /**
         * Session data.
         */
        private final S session;

        /**
         * The last time when this session has been used (in milliseconds since 1970-01-01).
         */
        private long lastActiveTime;

        public FullSession(final S session)
        {
            assert session != null : "Undefined session";

            this.session = session;
            touch();
        }

        /**
         * Returns the session.
         */
        public S getSession()
        {
            return session;
        }

        /**
         * Sets the time of last activity (used to determine whether the session {@link #hasExpired(Long)}.
         */
        void touch()
        {
            this.lastActiveTime = System.currentTimeMillis();
        }

        /**
         * Returns <code>true</code> if the session has expired.
         */
        boolean hasExpired(Long sessionExpirationTimeOrNull)
        {
                long sessionExpirationTime = sessionExpirationTimeOrNull == null ? session.getSessionExpirationTime() : sessionExpirationTimeOrNull;
                return System.currentTimeMillis() - lastActiveTime > sessionExpirationTime;
            }
        }

    private final ISessionFactory<T> sessionFactory;

    private final ILogMessagePrefixGenerator<T> prefixGenerator;

    /**
     * The map of session tokens to sessions. Access to this data structure needs to be synchronized.
     */
    protected final Map<String, FullSession<T>> sessions =
            new LinkedHashMap<String, FullSession<T>>();

    private final IAuthenticationService authenticationService;

    private final IRemoteHostProvider remoteHostProvider;

    /**
     * The time after which an inactive session will be expired (in milliseconds).
     */
    private final long sessionExpirationPeriodMillis;

    private final long sessionExpirationPeriodMillisNoLogin;

    private final boolean tryEmailAsUserName;

    private final Set<ISessionActionListener> listeners = new LinkedHashSet<>();

    public DefaultSessionManager(final ISessionFactory<T> sessionFactory,
            final ILogMessagePrefixGenerator<T> prefixGenerator,
            final IAuthenticationService authenticationService,
            final IRemoteHostProvider remoteHostProvider, final int sessionExpirationPeriodMinutes)
    {
        this(sessionFactory, prefixGenerator, authenticationService, remoteHostProvider,
                sessionExpirationPeriodMinutes, 0, false);
    }

    public DefaultSessionManager(final ISessionFactory<T> sessionFactory,
            final ILogMessagePrefixGenerator<T> prefixGenerator,
            final IAuthenticationService authenticationService,
            final IRemoteHostProvider remoteHostProvider, final int sessionExpirationPeriodMinutes,
            final int sessionExpirationPeriodMinutesNoLogin,
            final boolean tryEmailAsUserName)
    {

        assert sessionFactory != null : "Missing session factory.";
        assert prefixGenerator != null : "Missing prefix generator";
        assert authenticationService != null : "Missing authentication service.";
        assert remoteHostProvider != null : "Missing remote host provider.";
        assert sessionExpirationPeriodMinutes >= 0 : "Session experation time has to be a positive value: "
                + sessionExpirationPeriodMinutes; // == 0 is for unit test

        this.sessionFactory = sessionFactory;
        this.prefixGenerator = prefixGenerator;
        this.authenticationService = authenticationService;
        this.remoteHostProvider = remoteHostProvider;
        this.sessionExpirationPeriodMillis = sessionExpirationPeriodMinutes * DateUtils.MILLIS_PER_MINUTE;
        if (sessionExpirationPeriodMinutesNoLogin > 0)
        {
            this.sessionExpirationPeriodMillisNoLogin = sessionExpirationPeriodMinutesNoLogin * DateUtils.MILLIS_PER_MINUTE;
        } else
        {
            this.sessionExpirationPeriodMillisNoLogin = sessionExpirationPeriodMillis;
        }
        this.tryEmailAsUserName = tryEmailAsUserName;

        operationLog.info(String.format("Authentication service: '%s'", authenticationService
                .getClass().getName()));
        operationLog.info(String.format("Session expiration period: %s",
                DurationFormatUtils.formatDuration(sessionExpirationPeriodMillis, "H:mm:ss.SSS")));
        try
        {
            authenticationService.check();
        } catch (EnvironmentFailureException ex)
        {
            if (authenticationService.isRemote())
            {
                operationLog.warn("Remote authentication service check failed.", ex);
            } else
            {
                throw ex;
            }
        }
    }

    private final T createAndStoreSession(final String user, final Principal principal,
            final long now)
    {
        final String sessionToken = SessionTokenHash.create(user, now).toString();

        synchronized (SessionManagerLock.getInstance())
        {
            int maxNumberOfSessions = getMaxNumberOfSessionsFor(user);
            if (maxNumberOfSessions > 0)
            {
                List<FullSession<T>> openSessions = getOpenSessionsFor(user);
                while (openSessions.size() >= maxNumberOfSessions)
                {
                    FullSession<T> session = openSessions.remove(0);
                    closeSession(session.getSession(), SessionClosingReason.SESSIONS_LIMIT);
                }
            }

            final T session =
                    sessionFactory.create(sessionToken, user, principal, getRemoteHost(), now,
                            sessionExpirationPeriodMillis);
            final FullSession<T> createdSession = new FullSession<T>(session);
            sessions.put(createdSession.getSession().getSessionToken(), createdSession);

            getSessionMonitor().logSessionMonitoringInfo();

            return session;
        }
    }

    private List<FullSession<T>> getOpenSessionsFor(String user)
    {
        List<FullSession<T>> userSessions = new ArrayList<>();
        for (FullSession<T> session : sessions.values())
        {
            if (session.getSession().getUserName().equals(user))
            {
                userSessions.add(session);
            }
        }
        Collections.sort(userSessions, new SimpleComparator<FullSession<T>, Long>()
            {
                @Override
                public Long evaluate(FullSession<T> session)
                {
                    return session.lastActiveTime;
                }
            });
        return userSessions;
    }

    protected int getMaxNumberOfSessionsFor(String user)
    {
        return 0;
    }

    private ISessionMonitor getSessionMonitor()
    {
        if (sessionMonitor == null)
        {
            synchronized (SessionManagerLock.getInstance())
            {
                if (sessionMonitor == null)
                {
                    sessionMonitor = createSessionMonitor();
                }
            }
        }
        return sessionMonitor;
    }

    private volatile ISessionMonitor sessionMonitor;

    private ISessionMonitor createSessionMonitor()
    {
        Properties properties =
                configurer == null ? new Properties() : configurer.getResolvedProps();
        int sessionNotifyThreshold =
                PropertyUtils.getInt(properties, SessionMonitor.SESSION_NOTIFY_THRESHOLD_KEY,
                        SessionMonitor.SESSION_NOTIFY_THRESHOLD_DEFAULT);
        int notificationDelayPeriod =
                PropertyUtils.getInt(properties, SessionMonitor.SESSION_NOTIFY_DELAY_PERDIOD_KEY,
                        SessionMonitor.SESSION_NOTIFY_DELAY_PERDIOD_DEFAULT);

        if (sessionNotifyThreshold != 0)
        {
            operationLog.info("Create session monitor with threshold " + sessionNotifyThreshold);
            return new SessionMonitor(sessionNotifyThreshold, notificationDelayPeriod);
        } else
        {
            operationLog.info("Create dummy session monitor");
            return new ISessionMonitor()
                {
                    @Override
                    public void logSessionMonitoringInfo()
                    {
                    }
                };
        }
    }

    private interface ISessionMonitor
    {
        void logSessionMonitoringInfo();
    }

    private class SessionMonitor implements ISessionMonitor
    {
        private static final String SESSION_NOTIFY_THRESHOLD_KEY = "session-notification-threshold";

        private static final int SESSION_NOTIFY_THRESHOLD_DEFAULT = 0;

        private static final String SESSION_NOTIFY_DELAY_PERDIOD_KEY =
                "session-notification-delay-period";

        private static final int SESSION_NOTIFY_DELAY_PERDIOD_DEFAULT = 30 * 60;

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final long lastNotification = 0;

        /**
         * The delay beetween sending two notifications (in miliseconds)
         */
        final int notificationDelayPeriod;

        final int sessionNotifyThreshold;

        public SessionMonitor(int sessionNotifyThreshold, int notificationDelayPeriodInSeconds)
        {
            this.notificationDelayPeriod = notificationDelayPeriodInSeconds * 1000;
            this.sessionNotifyThreshold = sessionNotifyThreshold;

            if (sessionNotifyThreshold <= 0)
            {
                throw new IllegalArgumentException("Sessions threshold must be a positive integer");
            }
        }

        @Override
        public void logSessionMonitoringInfo()
        {
            int sessionsSize = sessions.size();

            operationLog.info("Currently active sessions: " + sessionsSize);

            if (sessionsSize > sessionNotifyThreshold)
            {
                long now = System.currentTimeMillis();
                if (lastNotification + notificationDelayPeriod > now)
                    return;

                notifyLog.info("Number of active sessions has exceeded the threshold ("
                        + sessionNotifyThreshold + ").");
                for (FullSession<T> fullSession : sessions.values())
                {
                    T session = fullSession.getSession();
                    session.getSessionStart();
                    session.getUserName();
                    session.getRemoteHost();
                    session.isAnonymous();
                    String message =
                            String.format(
                                    "Session %s:\n  User %s%s from %s\n  Started at %s, will expire in %d seconds.",
                                    session.getSessionToken(), session.getUserName(),
                                    session.isAnonymous() ? "(anonymous)" : "",
                                    session.getRemoteHost(),
                                    df.format(new Date(session.getSessionStart())),
                                    session.getSessionExpirationTime());
                    notifyLog.info(message);
                }
            }
        }
    }

    private static void checkIfNotBlank(final String object, final String name)
            throws UserFailureException
    {
        if (StringUtils.isBlank(object))
        {
            throw UserFailureException.fromTemplate("No '%s' specified.", name);
        }
    }

    public boolean isSessionActive(final String sessionToken)
    {
        synchronized (SessionManagerLock.getInstance())
        {
            final FullSession<T> session = sessions.get(sessionToken);
            if (session != null)
            {
                return !doSessionExpiration(session);
            } else
            {
                return false;
            }
        }
    }

    private boolean isSessionUnavailable(final FullSession<T> session)
    {
        return session == null || doSessionExpiration(session);
    }

    private boolean doSessionExpiration(final FullSession<T> session)
    {
        Long expTimeOrNull = NO_LOGIN_FILE.exists() ? (long) sessionExpirationPeriodMillisNoLogin : null;
        return session != null && session.hasExpired(expTimeOrNull);
    }

    private void logAuthenticed(final T session, final long timeToLoginMillis)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(LOGIN_PREFIX_TEMPLATE, timeToLoginMillis)
                    + (session.isAnonymous() ? "Anonymous user" : "User")
                    + " '" + session.getUserName()
                    + "' has been successfully authenticated from host '" + getRemoteHost()
                    + "'. Session token: '" + session.getSessionToken() + "'.");
        }
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": login");
    }

    private void logFailedAuthentication(final String user, final long timeToLoginMillis)
    {
        operationLog.warn(String.format(LOGIN_PREFIX_TEMPLATE, timeToLoginMillis) + "User '" + user
                + "' failed to authenticate from host '"
                + getRemoteHost() + "'.");
        logAuthenticationFailure(user);
    }

    private void logSessionFailure(final String user, final RuntimeException ex,
            final long timeToLoginMillis)
    {
        logAuthenticationFailure(user);
        operationLog.error(String.format(LOGIN_PREFIX_TEMPLATE, timeToLoginMillis)
                + "Error when trying to authenticate user '" + user + "'.",
                ex);
    }

    private void logAuthenticationFailure(final String user)
    {
        final String prefix = prefixGenerator.createPrefix(user, getRemoteHost());
        authenticationLog.info(prefix + ": login   ...FAILED");
    }

    private void logSessionExpired(final T session)
    {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("%sExpiring session '%s' for user '%s' "
                                + "after %d minutes of inactivity.", LOGOUT_PREFIX, session.getSessionToken(),
                        session.getUserName(), sessionExpirationPeriodMillis
                                / DateUtils.MILLIS_PER_MINUTE));
            }
            final String prefix = prefixGenerator.createPrefix(session);
            authenticationLog.info(prefix + ": session_expired  [inactive "
                    + DurationFormatUtils.formatDuration(sessionExpirationPeriodMillis, "H:mm:ss.SSS") + "]");
        }

    private void logLogout(final T session)
    {
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": logout");
        if (operationLog.isInfoEnabled())
        {
            final String user = session.getUserName();
            operationLog.info(LOGOUT_PREFIX + "Session '" + session.getSessionToken()
                    + "' of user '" + user + "' has been closed.");
        }
    }

    private void logLimitedNumberOfSessions(final T session)
    {
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": session closed because limit of open session has been reached.");
        if (operationLog.isInfoEnabled())
        {
            final String user = session.getUserName();
            operationLog.info(LOGOUT_PREFIX + "Session '" + session.getSessionToken()
                    + "' of user '" + user + "' has been closed because limit of open session has been reached.");
        }
    }

    @Override
    public boolean isAWellFormedSessionToken(String sessionTokenOrNull)
    {
        return SessionTokenHash.isValid(sessionTokenOrNull);
    }

    @Override
    public T getSession(final String sessionToken) throws InvalidSessionException
    {
        return getSession(sessionToken, true);
    }

    @Override public List<T> getSessions()
    {
        synchronized (SessionManagerLock.getInstance())
        {
            List<T> result = new ArrayList<>();
            for (FullSession<T> session : sessions.values())
            {
                result.add(session.getSession());
            }
            return result;
        }
    }

    @Override
    public T tryGetSession(String sessionToken)
    {
        synchronized (SessionManagerLock.getInstance())
        {
            final FullSession<T> session = sessions.get(sessionToken);
            return (session == null) ? null : session.getSession();
        }
    }

    private T getSession(final String sessionToken, boolean checkAndTouch)
            throws InvalidSessionException
    {
        checkIfNotBlank(sessionToken, "sessionToken");

        synchronized (SessionManagerLock.getInstance())
        {
            final FullSession<T> session = sessions.get(sessionToken);
            
            if (session == null)
            {
                final String msg =
                        "Session token '" + sessionToken + "' is invalid: user is not logged in.";
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(msg);
                }
                throw new InvalidSessionException(msg);
            }
            if (sessionToken.equals(session.getSession().getSessionToken()) == false)
            {
                final String msg =
                        "Session token '" + sessionToken
                                + "' is invalid: wrong token. Please login again.";
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(msg);
                }
                throw new InvalidSessionException(msg);
            }
            if (checkAndTouch && doSessionExpiration(session))
            {
                closeSession(session.getSession(), SessionClosingReason.SESSION_EXPIRATION);
            }
            if (checkAndTouch && isSessionUnavailable(session))
            {
                throw new InvalidSessionException(
                        "Session no longer available. Please login again.");
            }
            // This is where we know for sure we have a session.
            if (checkAndTouch)
            {
                session.touch();
            }
            return session.getSession();
        }
    }

    @Override
    public String tryToOpenSession(final String user, final String password)
    {
        checkIfNotBlank(password, "password");
        String sessionToken = tryToOpenSession(user, new IPrincipalProvider()
        {
            @Override
            public Principal tryToGetPrincipal(String userID)
            {
                return tryGetAndAuthenticateUser(user, password);
            }
        });
        return sessionToken;
    }

    @Override
    public String tryToOpenSession(String userID, IPrincipalProvider principalProvider)
    {
        checkIfNotBlank(userID, "user");
        final long now = System.currentTimeMillis();
        try
        {
            String sessionToken = null;
            final Principal principalOrNull = principalProvider.tryToGetPrincipal(userID);
            final long timeToLogin = System.currentTimeMillis() - now;
            final boolean isAuthenticated = Principal.isAuthenticated(principalOrNull);
            if (isAuthenticated)
            {
                try
                {
                    final T session =
                            createAndStoreSession(principalOrNull.getUserId(), principalOrNull, now);
                    sessionToken = session.getSessionToken();
                    logAuthenticed(session, timeToLogin);
                } catch (final IllegalArgumentException ex)
                {
                    // getPrincipal() of an authenticated user should not fail, if it does, this
                    // is an environment failure.
                    throw new EnvironmentFailureException(ex.getMessage(), ex);
                }
            } else
            {
                logFailedAuthentication(userID, timeToLogin);
            }
            return sessionToken;
        } catch (final RuntimeException ex)
        {
            logSessionFailure(userID, ex, System.currentTimeMillis() - now);
            throw ex;
        }

    }

    @Override
    public void closeSession(final String sessionToken) throws InvalidSessionException
    {
        synchronized (SessionManagerLock.getInstance())
        {
            final T session = getSession(sessionToken, false);
            closeSession(session, SessionClosingReason.LOGOUT);
        }
    }

    @Override
    public void expireSession(String sessionToken) throws InvalidSessionException
    {
        final T session = getSession(sessionToken, false);
        closeSession(session, SessionClosingReason.SESSION_EXPIRATION);
    }

    private void closeSession(final T session, SessionClosingReason reason)
            throws InvalidSessionException
    {
        synchronized (SessionManagerLock.getInstance())
        {
            session.cleanup();
            String sessionToken = session.getSessionToken();
            sessions.remove(sessionToken);
            switch (reason)
            {
                case LOGOUT:
                    logLogout(session);
                    break;
                case SESSION_EXPIRATION:
                    logSessionExpired(session);
                    break;
                case SESSIONS_LIMIT:
                    logLimitedNumberOfSessions(session);
                    break;
            }
            for (ISessionActionListener listener : listeners)
            {
                listener.sessionClosed(sessionToken);
            }
        }
    }

    @Override
    public String getRemoteHost()
    {
        return remoteHostProvider.getRemoteHost();
    }

    private Principal tryGetAndAuthenticateUser(final String user, final String password)
    {
        final Principal p = authenticationService.tryGetAndAuthenticateUser(user, password);
        if (p == null && tryEmailAsUserName && user.contains("@")
                && authenticationService.supportsAuthenticatingByEmail())
        {
            return authenticationService.tryGetAndAuthenticateUserByEmail(user, password);
        }
        return p;
    }

    @Override
    public void addListener(ISessionActionListener listener)
    {
        listeners.add(listener);
    }

}
