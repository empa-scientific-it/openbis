/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ILogMessagePrefixGenerator;
import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Jakub Straszewski
 */
public class OpenBisSessionManager extends DefaultSessionManager<Session> implements IOpenBisSessionManager
{
    private static final int DEFAULT_SESSION_EXPIRATION_PERIOD_FOR_NO_LOGIN = 10;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            OpenBisSessionManager.class);

    private static final int parseAsIntOrReturnDefaultValue(String string, int defaultValue)
    {
        try
        {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex)
        {
            return defaultValue;
        }
    }

    private final IDAOFactory daoFactory;

    private final ISessionFactory<Session> sessionFactory;

    private final IAuthenticationService authenticationService;

    private String userForAnonymousLogin;

    private int maxNumberOfSessionsPerUser;

    private final Set<String> usersWithUnrestrictedNumberOfSessions = new HashSet<>();

    public OpenBisSessionManager(ISessionFactory<Session> sessionFactory, ILogMessagePrefixGenerator<Session> prefixGenerator,
            IAuthenticationService authenticationService, IRemoteHostProvider remoteHostProvider, int sessionExpirationPeriodMinutes,
            String sessionExpirationPeriodMinutesForNoLogin, boolean tryEmailAsUserName, IDAOFactory daoFactory)
    {
        super(sessionFactory, prefixGenerator, authenticationService, remoteHostProvider, sessionExpirationPeriodMinutes,
                parseAsIntOrReturnDefaultValue(sessionExpirationPeriodMinutesForNoLogin, DEFAULT_SESSION_EXPIRATION_PERIOD_FOR_NO_LOGIN),
                tryEmailAsUserName, daoFactory.getPersonalAccessTokenDAO());
        this.daoFactory = daoFactory;
        this.sessionFactory = sessionFactory;
        this.authenticationService = authenticationService;
    }

    public OpenBisSessionManager(ISessionFactory<Session> sessionFactory, ILogMessagePrefixGenerator<Session> prefixGenerator,
            IAuthenticationService authenticationService, IRemoteHostProvider remoteHostProvider, int sessionExpirationPeriodMinutes,
            String sessionExpirationPeriodMinutesForNoLogin, IDAOFactory daoFactory)
    {
        this(sessionFactory, prefixGenerator, authenticationService, remoteHostProvider, sessionExpirationPeriodMinutes,
                sessionExpirationPeriodMinutesForNoLogin, false, daoFactory);
    }

    @PostConstruct
    private void init()
    {
        List<PersonalAccessTokenSession> patSessions = daoFactory.getPersonalAccessTokenDAO().listSessions();

        synchronized (sessions)
        {
            for (PersonalAccessTokenSession patSession : patSessions)
            {
                try
                {
                    final Principal principal = authenticationService.getPrincipal(patSession.getUserId());

                    if (principal == null)
                    {
                        sessions.remove(patSession.getSessionHash());
                        operationLog.warn(
                                String.format(
                                        "Ignoring a personal access token session because the session's user '%s' was not found by the authentication service.",
                                        patSession.getUserId()));
                        continue;
                    } else
                    {
                        principal.setAuthenticated(true);
                    }

                    PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(patSession.getUserId());

                    if (person == null)
                    {
                        operationLog.warn(
                                String.format(
                                        "Ignoring a personal access token session because the session's user '%s' was not found in the openBIS database.",
                                        patSession.getUserId()));
                        continue;
                    }

                    final FullSession<Session> createdSession =
                            new FullSession<>(sessionFactory.create(patSession.getSessionHash(), patSession.getUserId(), principal, getRemoteHost(),
                                    patSession.getValidFrom().getTime(), (int) (patSession.getValidUntil().getTime() - System.currentTimeMillis()),
                                    true, patSession.getSessionName()));

                    HibernateUtils.initialize(person.getAllPersonRoles());
                    createdSession.getSession().setPerson(person);
                    createdSession.getSession().setCreatorPerson(person);

                    sessions.put(createdSession.getSession().getSessionToken(), createdSession);
                } catch (Exception e)
                {
                    operationLog.warn(String.format(
                            "Loading of a personal access token session defined for user '%s' and session name '%s' failed. Ignoring it.",
                            patSession.getUserId(),
                            patSession.getSessionName()), e);
                }
            }
        }
    }

    @Override
    protected int getMaxNumberOfSessionsFor(String user)
    {
        if (usersWithUnrestrictedNumberOfSessions.contains(user))
        {
            return 0;
        }
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user);
        if (person != null && person.isSystemUser())
        {
            return 0;
        }
        return maxNumberOfSessionsPerUser;
    }

    public void setMaxNumberOfSessionsPerUser(String number)
    {
        maxNumberOfSessionsPerUser = parseAsIntOrReturnDefaultValue(number, 5);
    }

    public void setUsersWithUnrestrictedNumberOfSessions(String users)
    {
        if (users.startsWith("${") == false)
        {
            for (String user : users.split(","))
            {
                usersWithUnrestrictedNumberOfSessions.add(user.trim());
            }
        }
    }

    @Override
    public void updateAllSessions()
    {
        synchronized (sessions)
        {
            for (FullSession<Session> fullSession : sessions.values())
            {
                Session session = fullSession.getSession();
                synchronized (session) // synchronized with updateDisplaySettings() and saveDisplaySettings() in AbstractServer
                {
                    PersonPE oldPerson = session.tryGetPerson();
                    if (oldPerson != null
                            && oldPerson.isSystemUser() == false)
                    {
                        IPersonDAO personDAO = daoFactory.getPersonDAO();
                        PersonPE person = personDAO.tryGetByTechId(new TechId(oldPerson.getId()));
                        if (person != null)
                        {
                            HibernateUtils.initialize(person.getAllPersonRoles());
                            session.setPerson(person);
                            session.setCreatorPerson(person);
                        }
                    }
                }
            }
        }
    }

    public final void setUserForAnonymousLogin(String userID)
    {
        userForAnonymousLogin = AbstractServer.isResolved(userID) ? userID : null;
    }

    @Override
    public String getUserForAnonymousLogin()
    {
        return userForAnonymousLogin;
    }
}
