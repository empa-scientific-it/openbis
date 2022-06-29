package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.security.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private final Map<String, PersonalAccessToken> tokens = new HashMap<>();

    private final Map<String, PersonalAccessTokenSession> sessions = new HashMap<>();

    private final List<Listener> listeners = new ArrayList<>();

    @Override public synchronized PersonalAccessToken createToken(final PersonalAccessToken creation)
    {
        Date now = new Date();

        PersonalAccessToken token = new PersonalAccessToken();
        token.setHash(new TokenGenerator().getNewToken(now.getTime()));
        token.setSessionName(creation.getSessionName());
        token.setOwnerId(creation.getOwnerId());
        token.setRegistratorId(creation.getRegistratorId());
        token.setModifierId(creation.getRegistratorId());
        token.setValidFromDate(creation.getValidFromDate());
        token.setValidToDate(creation.getValidToDate());
        token.setRegistrationDate(creation.getRegistrationDate());
        token.setModificationDate(creation.getModificationDate());
        token.setAccessDate(creation.getAccessDate());

        tokens.put(token.getHash(), token);

        recalculateSessions();

        return token;
    }

    @Override public synchronized void updateToken(final PersonalAccessToken update)
    {
        PersonalAccessToken token = getTokenByHash(update.getHash());

        if (token != null)
        {
            if (update.getAccessDate() != null)
            {
                token.setAccessDate(update.getAccessDate());
            }
            tokens.put(token.getHash(), token);

            recalculateSessions();
        }
    }

    @Override public synchronized void deleteToken(final String hash)
    {
        tokens.remove(hash);

        recalculateSessions();
    }

    @Override public synchronized List<PersonalAccessToken> listTokens()
    {
        return new ArrayList<>(tokens.values());
    }

    @Override public synchronized PersonalAccessToken getTokenByHash(final String hash)
    {
        return tokens.get(hash);
    }

    @Override public synchronized List<PersonalAccessTokenSession> listSessions()
    {
        return new ArrayList<>(sessions.values());
    }

    @Override public synchronized PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        return getSessionByUserIdAndSessionName(sessions.values(), userId, sessionName);
    }

    private PersonalAccessTokenSession getSessionByUserIdAndSessionName(final Collection<PersonalAccessTokenSession> sessions, final String userId,
            final String sessionName)
    {
        for (PersonalAccessTokenSession session : sessions)
        {
            if (session.getOwnerId().equals(userId) && session.getName().equals(sessionName))
            {
                return session;
            }
        }
        return null;
    }

    @Override public synchronized void addListener(final Listener listener)
    {
        listeners.add(listener);
    }

    private void notifySessionCreated(final PersonalAccessTokenSession session)
    {
        for (Listener listener : listeners)
        {
            listener.onSessionCreated(session);
        }
    }

    private void notifySessionUpdated(final PersonalAccessTokenSession session)
    {
        for (Listener listener : listeners)
        {
            listener.onSessionUpdated(session);
        }
    }

    private void notifySessionDeleted(final PersonalAccessTokenSession session)
    {
        for (Listener listener : listeners)
        {
            listener.onSessionDeleted(session);
        }
    }

    private synchronized void recalculateSessions()
    {
        Map<String, PersonalAccessTokenSession> existingSessions = new HashMap<>(sessions);
        Map<String, PersonalAccessTokenSession> newSessions = new HashMap<>();

        for (PersonalAccessToken token : tokens.values())
        {
            PersonalAccessTokenSession newSession =
                    getSessionByUserIdAndSessionName(newSessions.values(), token.getOwnerId(), token.getSessionName());

            if (newSession == null)
            {
                PersonalAccessTokenSession existingSession =
                        getSessionByUserIdAndSessionName(existingSessions.values(), token.getOwnerId(), token.getSessionName());

                newSession = new PersonalAccessTokenSession();
                if (existingSession != null)
                {
                    newSession.setHash(existingSession.getHash());
                } else
                {
                    newSession.setHash(new TokenGenerator().getNewToken(new Date().getTime()));
                }
                newSession.setName(token.getSessionName());
                newSession.setOwnerId(token.getOwnerId());
                newSession.setValidFromDate(token.getValidFromDate());
                newSession.setValidToDate(token.getValidToDate());
                newSession.setAccessDate(token.getAccessDate());
            } else
            {
                newSession.setValidFromDate(getEarlierDate(token.getValidFromDate(), newSession.getValidFromDate()));
                newSession.setValidToDate(getLaterDate(token.getValidToDate(), newSession.getValidToDate()));
                newSession.setAccessDate(getLaterDate(token.getAccessDate(), newSession.getAccessDate()));
            }

            newSessions.put(newSession.getHash(), newSession);
        }

        sessions.clear();
        sessions.putAll(newSessions);

        for (PersonalAccessTokenSession newSession : newSessions.values())
        {
            PersonalAccessTokenSession existingSession = existingSessions.get(newSession.getHash());

            if (existingSession != null)
            {
                if (!existingSession.getValidFromDate().equals(newSession.getValidFromDate()) || !existingSession.getValidToDate()
                        .equals(newSession.getValidToDate()))
                {
                    notifySessionUpdated(newSession);
                }
            } else
            {
                notifySessionCreated(newSession);
            }
        }

        for (PersonalAccessTokenSession session : existingSessions.values())
        {
            if (!newSessions.containsKey(session.getHash()))
            {
                notifySessionDeleted(session);
            }
        }
    }

    private Date getEarlierDate(Date date1, Date date2)
    {
        if (date1 != null && date2 != null)
        {
            return date1.before(date2) ? date1 : date2;
        } else
        {
            return date1 != null ? date1 : date2;
        }
    }

    private Date getLaterDate(Date date1, Date date2)
    {
        if (date1 != null && date2 != null)
        {
            return date1.after(date2) ? date1 : date2;
        } else
        {
            return date1 != null ? date1 : date2;
        }
    }

}
