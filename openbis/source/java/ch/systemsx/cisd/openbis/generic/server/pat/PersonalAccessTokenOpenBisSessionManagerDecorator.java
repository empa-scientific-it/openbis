package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.IPrincipalProvider;
import ch.systemsx.cisd.authentication.ISessionActionListener;
import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.OpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

public class PersonalAccessTokenOpenBisSessionManagerDecorator implements IOpenBisSessionManager
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PersonalAccessTokenOpenBisSessionManagerDecorator.class);

    @Autowired
    private IPersonalAccessTokenConfig config;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISessionFactory<Session> sessionFactory;

    @Resource(name = ComponentNames.AUTHENTICATION_SERVICE)
    private IAuthenticationService authenticationService;

    private final IOpenBisSessionManager sessionManager;

    private final Map<String, Session> sessions = new HashMap<>();

    public PersonalAccessTokenOpenBisSessionManagerDecorator(IOpenBisSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public String tryToOpenSession(final String user, final String password)
    {
        return sessionManager.tryToOpenSession(user, password);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public String tryToOpenSession(final String userID, final IPrincipalProvider principalProvider)
    {
        return sessionManager.tryToOpenSession(userID, principalProvider);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public boolean isAWellFormedSessionToken(final String sessionTokenOrNull)
    {
        return sessionManager.isAWellFormedSessionToken(sessionTokenOrNull);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public void addListener(final ISessionActionListener listener)
    {
        sessionManager.addListener(listener);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public String getRemoteHost()
    {
        return sessionManager.getRemoteHost();
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public void updateAllSessions()
    {
        sessionManager.updateAllSessions();

        synchronized (sessions)
        {
            OpenBisSessionManager.updateSessions(daoFactory, sessions.values());
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public String getUserForAnonymousLogin()
    {
        return sessionManager.getUserForAnonymousLogin();
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public boolean isSessionActive(final String sessionToken)
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            return sessionManager.isSessionActive(sessionToken);
        }

        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            return sessionManager.isSessionActive(sessionToken);
        } else
        {
            Date now = new Date();
            return now.after(patSession.getValidFromDate()) && now.before(patSession.getValidToDate());
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public void expireSession(final String sessionToken) throws InvalidSessionException
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            sessionManager.expireSession(sessionToken);
        }

        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            sessionManager.expireSession(sessionToken);
        } else
        {
            closeSession(sessionToken);
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public void closeSession(final String sessionToken) throws InvalidSessionException
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            sessionManager.closeSession(sessionToken);
        }

        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            sessionManager.closeSession(sessionToken);
        } else
        {
            synchronized (sessions)
            {
                Date now = new Date();

                if (now.after(patSession.getValidToDate()))
                {
                    Session session = getSessionForPATSession(patSession);
                    if (session != null)
                    {
                        session.cleanup();
                        sessions.remove(session.getSessionToken());
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public Session getSession(final String sessionToken) throws InvalidSessionException
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            return sessionManager.getSession(sessionToken);
        }

        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            return sessionManager.getSession(sessionToken);
        } else
        {
            synchronized (sessions)
            {
                Session session = tryGetSession(sessionToken);
                if (session != null)
                {
                    return session;
                } else
                {
                    throw new InvalidSessionException("Invalid personal access token session");
                }
            }
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public Session tryGetSession(final String sessionToken)
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            return sessionManager.tryGetSession(sessionToken);
        }

        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            return sessionManager.tryGetSession(sessionToken);
        } else
        {
            synchronized (sessions)
            {
                if (isSessionActive(sessionToken))
                {
                    return getOrCreateSessionForPATSession(patSession);
                } else
                {
                    return null;
                }
            }
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public List<Session> getSessions()
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            return sessionManager.getSessions();
        }

        List<Session> allSessions = new ArrayList<>(sessionManager.getSessions());

        synchronized (sessions)
        {
            for (PersonalAccessTokenSession patSession : daoFactory.getPersonalAccessTokenDAO().listSessions())
            {
                if (isSessionActive(patSession.getHash()))
                {
                    Session session = getOrCreateSessionForPATSession(patSession);
                    if (session != null)
                    {
                        allSessions.add(session);
                    }
                }
            }
        }

        return allSessions;
    }

    private Session getSessionForPATSession(PersonalAccessTokenSession patSession)
    {
        return sessions.get(patSession.getHash());
    }

    private Session getOrCreateSessionForPATSession(PersonalAccessTokenSession patSession)
    {
        Session session = getSessionForPATSession(patSession);
        if (session == null)
        {
            session = createSessionForPATSession(patSession);
            if (session != null)
            {
                sessions.put(session.getSessionToken(), session);
            }
        }
        return session;
    }

    private Session createSessionForPATSession(PersonalAccessTokenSession patSession)
    {
        try
        {
            final Principal principal = authenticationService.getPrincipal(patSession.getOwnerId());

            if (principal == null)
            {
                operationLog.warn("Ignoring a personal access token session (" + patSession
                        + ") because the session's owner was not found by the authentication service.");
                return null;
            } else
            {
                principal.setAuthenticated(true);
            }

            PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(patSession.getOwnerId());

            if (person == null)
            {
                operationLog.warn("Ignoring a personal access token session (" + patSession
                        + ") because the session's owner was not found in the openBIS database.");
                return null;
            }

            final Session session = sessionFactory.create(patSession.getHash(), patSession.getOwnerId(), principal, getRemoteHost(),
                    patSession.getValidFromDate().getTime(),
                    (int) (patSession.getValidToDate().getTime() - patSession.getValidFromDate().getTime()),
                    true, patSession.getName());

            HibernateUtils.initialize(person.getAllPersonRoles());
            session.setPerson(person);
            session.setCreatorPerson(person);

            return session;

        } catch (Exception e)
        {
            operationLog.warn("Creation of a personal access token session (" + patSession + ") failed.", e);
            return null;
        }
    }

}
