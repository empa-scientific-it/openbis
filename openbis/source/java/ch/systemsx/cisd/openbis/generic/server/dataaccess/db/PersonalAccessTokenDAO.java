package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
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
            if (update.getSessionName() != null)
            {
                token.setSessionName(update.getSessionName());
            }
            if (update.getValidFromDate() != null)
            {
                token.setValidFromDate(update.getValidFromDate());
            }
            if (update.getValidToDate() != null)
            {
                token.setValidToDate(update.getValidToDate());
            }
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
        for (PersonalAccessTokenSession session : sessions.values())
        {
            if (session.getOwnerId().equals(userId) && session.getName().equals(sessionName))
            {
                return session;
            }
        }
        return null;
    }

    private synchronized void recalculateSessions()
    {
        Map<String, PersonalAccessTokenSession> newSessions = new HashMap<>();

        for (PersonalAccessToken token : tokens.values())
        {
            PersonalAccessTokenSession session = getSessionByUserIdAndSessionName(token.getOwnerId(), token.getSessionName());

            if (session == null)
            {
                Date now = new Date();

                session = new PersonalAccessTokenSession();
                session.setHash(new TokenGenerator().getNewToken(now.getTime()));
                session.setName(token.getSessionName());
                session.setOwnerId(token.getOwnerId());
                session.setValidFromDate(token.getValidFromDate());
                session.setValidToDate(token.getValidToDate());
                session.setAccessDate(token.getAccessDate());
            } else
            {
                session.setValidFromDate(getEarlierDate(token.getValidFromDate(), session.getValidFromDate()));
                session.setValidToDate(getLaterDate(token.getValidToDate(), session.getValidToDate()));
                session.setAccessDate(getLaterDate(token.getAccessDate(), session.getAccessDate()));
            }

            newSessions.put(session.getHash(), session);
        }

        sessions.clear();
        sessions.putAll(newSessions);
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
