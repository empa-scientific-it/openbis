package ch.systemsx.cisd.openbis.generic.server.pat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.IPrincipalProvider;
import ch.systemsx.cisd.authentication.ISessionActionListener;
import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

public class PersonalAccessTokenOpenBisSessionManagerDecorator implements IOpenBisSessionManager
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PersonalAccessTokenOpenBisSessionManagerDecorator.class);

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISessionFactory<Session> sessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Resource(name = ComponentNames.AUTHENTICATION_SERVICE)
    private IAuthenticationService authenticationService;

    private final IOpenBisSessionManager sessionManager;

    public PersonalAccessTokenOpenBisSessionManagerDecorator(IOpenBisSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Override public String tryToOpenSession(final String user, final String password)
    {
        return sessionManager.tryToOpenSession(user, password);
    }

    @Override public String tryToOpenSession(final String userID, final IPrincipalProvider principalProvider)
    {
        return sessionManager.tryToOpenSession(userID, principalProvider);
    }

    @Override public boolean isAWellFormedSessionToken(final String sessionTokenOrNull)
    {
        return sessionManager.isAWellFormedSessionToken(sessionTokenOrNull);
    }

    @Override public void addListener(final ISessionActionListener listener)
    {
        sessionManager.addListener(listener);
    }

    @Override public String getRemoteHost()
    {
        return sessionManager.getRemoteHost();
    }

    @Override public void updateAllSessions()
    {
        sessionManager.updateAllSessions();
    }

    @Override public String getUserForAnonymousLogin()
    {
        return sessionManager.getUserForAnonymousLogin();
    }

    @Override public boolean isSessionActive(final String sessionToken)
    {
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

    @Override public void expireSession(final String sessionToken) throws InvalidSessionException
    {
        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            sessionManager.expireSession(sessionToken);
        }
    }

    @Override public void closeSession(final String sessionToken) throws InvalidSessionException
    {
        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            sessionManager.closeSession(sessionToken);
        }
    }

    @Override public Session getSession(final String sessionToken) throws InvalidSessionException
    {
        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            return sessionManager.getSession(sessionToken);
        } else
        {
            Date now = new Date();
            if (now.after(patSession.getValidFromDate()) && now.before(patSession.getValidToDate()))
            {
                DefaultSessionManager.FullSession<Session> fullSession = createPersonalAccessSession(patSession);
                if (fullSession != null)
                {
                    return fullSession.getSession();
                }
            }
            throw new InvalidSessionException("Invalid personal access token session");
        }
    }

    @Override public Session tryGetSession(final String sessionToken)
    {
        PersonalAccessTokenSession patSession = daoFactory.getPersonalAccessTokenDAO().getSessionByHash(sessionToken);

        if (patSession == null)
        {
            return sessionManager.tryGetSession(sessionToken);
        } else
        {
            Date now = new Date();
            if (now.after(patSession.getValidFromDate()) && now.before(patSession.getValidToDate()))
            {
                DefaultSessionManager.FullSession<Session> fullSession = createPersonalAccessSession(patSession);
                if (fullSession != null)
                {
                    return fullSession.getSession();
                }
            }
            return null;
        }
    }

    @Override public List<Session> getSessions()
    {
        List<Session> sessions = new ArrayList<>(sessionManager.getSessions());

        for (PersonalAccessTokenSession patSession : daoFactory.getPersonalAccessTokenDAO().listSessions())
        {
            DefaultSessionManager.FullSession<Session> fullSession = createPersonalAccessSession(patSession);
            if (fullSession != null)
            {
                sessions.add(fullSession.getSession());
            }
        }

        return sessions;
    }

    private DefaultSessionManager.FullSession<Session> createPersonalAccessSession(PersonalAccessTokenSession patSession)
    {
        TransactionTemplate tmpl = new TransactionTemplate(transactionManager);
        return tmpl.execute(new TransactionCallback<DefaultSessionManager.FullSession<Session>>()
        {
            @Override
            public DefaultSessionManager.FullSession<Session> doInTransaction(TransactionStatus status)
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

                    final DefaultSessionManager.FullSession<Session> createdSession =
                            new DefaultSessionManager.FullSession<>(
                                    sessionFactory.create(patSession.getHash(), patSession.getOwnerId(), principal, getRemoteHost(),
                                            patSession.getValidFromDate().getTime(),
                                            (int) (patSession.getValidToDate().getTime() - patSession.getValidFromDate().getTime()),
                                            true, patSession.getName()));

                    HibernateUtils.initialize(person.getAllPersonRoles());
                    createdSession.getSession().setPerson(person);
                    createdSession.getSession().setCreatorPerson(person);

                    return createdSession;

                } catch (Exception e)
                {
                    operationLog.warn("Creation of a personal access token session (" + patSession + ") failed.", e);
                    return null;
                }
            }
        });
    }

}
