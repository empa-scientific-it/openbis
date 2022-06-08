package ch.systemsx.cisd.authentication.pat;

import java.io.File;
import java.util.List;
import java.util.Map;

public class FileBasedPersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private File file;

    public FileBasedPersonalAccessTokenDAO(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("File cannot be null");
        }
        this.file = file;
    }

    @Override public List<PersonalAccessToken> listTokens()
    {
        return null;
    }

    @Override public PersonalAccessToken getTokenByHash(final String hash)
    {
        return null;
    }

    @Override public void createToken(final PersonalAccessToken creation)
    {

    }

    @Override public void updateToken(final PersonalAccessToken update)
    {

    }

    @Override public void deleteToken(final String hash)
    {

    }

    @Override public PersonalAccessTokenSession getSessionByHash(final String hash)
    {
        return null;
    }

    @Override public PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        return null;
    }

    @Override public void createSession(final PersonalAccessTokenSession creation)
    {

    }

    @Override public void updateSession(final PersonalAccessTokenSession update)
    {

    }

    @Override public void deleteSession(final String hash)
    {

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
