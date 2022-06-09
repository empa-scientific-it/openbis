package ch.systemsx.cisd.authentication.pat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;

public class FileBasedPersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private final Map<String, PersonalAccessToken> tokens;

    private final Map<String, PersonalAccessTokenSession> sessions;

    public FileBasedPersonalAccessTokenDAO()
    {
        try
        {
            // TEST DATA
            PersonalAccessToken token1 = new PersonalAccessToken();
            token1.setUserId("admin");
            token1.setSessionName("app1");
            token1.setHash("admin-111111111111111x11111111111111111111111111111111");
            token1.setValidFrom(DateUtils.parseDate("2022-01-01 10:00", "YYYY-MM-dd HH:mm"));
            token1.setValidUntil(DateUtils.parseDate("2022-06-10 12:00", "YYYY-MM-dd HH:mm"));

            PersonalAccessToken token2 = new PersonalAccessToken();
            token2.setUserId("admin");
            token2.setSessionName("app1");
            token2.setHash("admin-222222222222222x22222222222222222222222222222222");
            token2.setValidFrom(DateUtils.parseDate("2022-06-01 10:00", "YYYY-MM-dd HH:mm"));
            token2.setValidUntil(DateUtils.parseDate("2022-10-10 12:00", "YYYY-MM-dd HH:mm"));

            tokens = new HashMap<>();
            tokens.put(token1.getHash(), token1);
            tokens.put(token2.getHash(), token2);

            PersonalAccessTokenSession session1 = new PersonalAccessTokenSession();
            session1.setUserId("admin");
            session1.setSessionName("app1");
            session1.setHash("admin-999999999999999x99999999999999999999999999999999");
            session1.setValidFrom(token1.getValidFrom());
            session1.setValidUntil(token2.getValidUntil());

            sessions = new HashMap<>();
            sessions.put(session1.getHash(), session1);

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override public List<PersonalAccessToken> listTokens()
    {
        return new ArrayList<>(tokens.values());
    }

    @Override public PersonalAccessToken getTokenByHash(final String hash)
    {
        return tokens.get(hash);
    }

    @Override public void updateToken(final PersonalAccessToken patToken)
    {
        tokens.put(patToken.getHash(), patToken);
    }

    @Override public List<PersonalAccessTokenSession> listSessions()
    {
        return new ArrayList<>(sessions.values());
    }

    @Override public PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        for (PersonalAccessTokenSession session : sessions.values())
        {
            if (session.getUserId().equals(userId) && session.getSessionName().equals(sessionName))
            {
                return session;
            }
        }
        return null;
    }

    private static class FileContent
    {
        private Map<String, PersonalAccessToken> tokens;

        private Map<String, PersonalAccessTokenSession> sessions;

        public Map<String, PersonalAccessToken> getTokens()
        {
            return tokens;
        }

        public void setTokens(final Map<String, PersonalAccessToken> tokens)
        {
            this.tokens = tokens;
        }

        public Map<String, PersonalAccessTokenSession> getSessions()
        {
            return sessions;
        }

        public void setSessions(final Map<String, PersonalAccessTokenSession> sessions)
        {
            this.sessions = sessions;
        }
    }
}
