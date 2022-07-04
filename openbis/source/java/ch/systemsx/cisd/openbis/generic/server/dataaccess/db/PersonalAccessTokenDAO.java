package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.security.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PersonalAccessTokenDAO.class);

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH = "personal-access-tokens-file-path";

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT = "personal-access-tokens.json";

    private final Properties properties;

    private final HashGenerator generator;

    private Map<String, PersonalAccessToken> tokens = new HashMap<>();

    private Map<String, PersonalAccessTokenSession> sessions = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    private final List<Listener> listeners = new ArrayList<>();

    public PersonalAccessTokenDAO(final Properties properties)
    {
        this(properties, new HashGenerator()
        {

            private TokenGenerator generator = new TokenGenerator();

            @Override public String generateTokenHash(String user)
            {
                return generateSessionHash(user);
            }

            @Override public String generateSessionHash(String user)
            {
                return user + "-" + generator.getNewToken(System.currentTimeMillis(), 'x');
            }
        });
    }

    PersonalAccessTokenDAO(final Properties properties, final HashGenerator generator)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        if (generator == null)
        {
            throw new IllegalArgumentException("Hash generator cannot be null");
        }

        this.properties = properties;
        this.generator = generator;

        loadFromFile();
    }

    @Override public synchronized PersonalAccessToken createToken(final PersonalAccessToken creation)
    {
        PersonalAccessToken token = new PersonalAccessToken();
        token.setHash(generator.generateTokenHash(creation.getOwnerId()));
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

        recalculateSession(token.getOwnerId(), token.getSessionName());
        saveInFile();

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

            recalculateSession(token.getOwnerId(), token.getSessionName());
            saveInFile();
        }
    }

    @Override public synchronized void deleteToken(final String hash)
    {
        PersonalAccessToken token = tokens.remove(hash);

        if (token != null)
        {
            recalculateSession(token.getOwnerId(), token.getSessionName());
            saveInFile();
        }
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

    private synchronized void recalculateSession(String ownerId, String sessionName)
    {
        PersonalAccessTokenSession existingSession = getSessionByUserIdAndSessionName(ownerId, sessionName);
        PersonalAccessTokenSession newSession = null;

        for (PersonalAccessToken token : tokens.values())
        {
            if (token.getOwnerId().equals(ownerId) && token.getSessionName().equals(sessionName))
            {
                if (newSession == null)
                {
                    newSession = new PersonalAccessTokenSession();
                    if (existingSession != null)
                    {
                        newSession.setHash(existingSession.getHash());
                    } else
                    {
                        newSession.setHash(generator.generateSessionHash(ownerId));
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
            }
        }

        if (existingSession == null)
        {
            if (newSession != null)
            {
                sessions.put(newSession.getHash(), newSession);
                notifySessionCreated(newSession);
            }
        } else
        {
            if (newSession == null)
            {
                sessions.remove(existingSession.getHash());
                notifySessionDeleted(existingSession);
            } else if (!Objects.equals(existingSession.getValidFromDate(), newSession.getValidFromDate()) || !Objects.equals(
                    existingSession.getValidToDate(), newSession.getValidToDate()) || !Objects.equals(existingSession.getAccessDate(),
                    newSession.getAccessDate()))
            {
                sessions.put(newSession.getHash(), newSession);
                notifySessionUpdated(newSession);
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

    private String getFilePath()
    {
        return properties.getProperty(PERSONAL_ACCESS_TOKENS_FILE_PATH, PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT);
    }

    private void loadFromFile()
    {
        File file = new File(getFilePath());

        if (file.exists())
        {
            try
            {
                FileContent content = mapper.readValue(file, FileContent.class);
                tokens = content.tokens;
                sessions = content.sessions;
            } catch (IOException e)
            {
                operationLog.error("Loading of personal access tokens file failed. File path: " + file.getAbsolutePath(), e);
            }
        }
    }

    private void saveInFile()
    {
        File file = new File(getFilePath());

        try
        {
            FileContent content = new FileContent();
            content.tokens = tokens;
            content.sessions = sessions;
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, content);
        } catch (IOException e)
        {
            operationLog.error("Saving of personal access tokens file failed. File path: " + file.getAbsolutePath(), e);
        }
    }

    private static class FileContent
    {

        @JsonProperty
        public Map<String, PersonalAccessToken> tokens;

        @JsonProperty
        public Map<String, PersonalAccessTokenSession> sessions;

    }

    public interface HashGenerator
    {
        String generateTokenHash(String user);

        String generateSessionHash(String user);
    }

}
