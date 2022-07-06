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
import java.util.Set;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ILogMessagePrefixGenerator;
import ch.systemsx.cisd.authentication.ISessionFactory;
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

    IDAOFactory daoFactory;

    private String userForAnonymousLogin;

    private int maxNumberOfSessionsPerUser;

    private Set<String> usersWithUnrestrictedNumberOfSessions = new HashSet<>();

    public OpenBisSessionManager(ISessionFactory<Session> sessionFactory, ILogMessagePrefixGenerator<Session> prefixGenerator,
            IAuthenticationService authenticationService, IRemoteHostProvider remoteHostProvider, int sessionExpirationPeriodMinutes,
            String sessionExpirationPeriodMinutesForNoLogin, boolean tryEmailAsUserName, IDAOFactory daoFactory)
    {
        super(sessionFactory, prefixGenerator, authenticationService, remoteHostProvider, sessionExpirationPeriodMinutes,
                parseAsIntOrReturnDefaultValue(sessionExpirationPeriodMinutesForNoLogin, DEFAULT_SESSION_EXPIRATION_PERIOD_FOR_NO_LOGIN),
                tryEmailAsUserName);
        this.daoFactory = daoFactory;
    }

    public OpenBisSessionManager(ISessionFactory<Session> sessionFactory, ILogMessagePrefixGenerator<Session> prefixGenerator,
            IAuthenticationService authenticationService, IRemoteHostProvider remoteHostProvider, int sessionExpirationPeriodMinutes,
            String sessionExpirationPeriodMinutesForNoLogin, IDAOFactory daoFactory)
    {
        this(sessionFactory, prefixGenerator, authenticationService, remoteHostProvider, sessionExpirationPeriodMinutes,
                sessionExpirationPeriodMinutesForNoLogin, false, daoFactory);
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
