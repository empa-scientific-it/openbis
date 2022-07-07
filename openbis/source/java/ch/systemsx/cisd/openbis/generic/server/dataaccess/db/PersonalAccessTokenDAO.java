package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.authentication.SessionTokenHash;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenHash;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;

public class PersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PersonalAccessTokenDAO.class);

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH = "personal-access-tokens-file-path";

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT = "personal-access-tokens.json";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private final Properties properties;

    private final HashGenerator generator;

    private Map<String, PersonalAccessToken> tokens = new HashMap<>();

    private Map<String, PersonalAccessTokenSession> sessions = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public interface HashGenerator
    {
        String generateTokenHash(String user);

        String generateSessionHash(String user);
    }

    public PersonalAccessTokenDAO(final Properties properties)
    {
        this(properties, new HashGenerator()
        {
            @Override public String generateTokenHash(String user)
            {
                return PersonalAccessTokenHash.create(user, System.currentTimeMillis()).toString();
            }

            @Override public String generateSessionHash(String user)
            {
                return SessionTokenHash.create(user, System.currentTimeMillis()).toString();
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

        checkToken(token);
        tokens.put(token.getHash(), token);
        sessions = recalculateSessions(tokens.values(), sessions.values());
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

            checkToken(token);
            tokens.put(token.getHash(), token);
            sessions = recalculateSessions(tokens.values(), sessions.values());
            saveInFile();
        }
    }

    @Override public synchronized void deleteToken(final String hash)
    {
        PersonalAccessToken token = tokens.remove(hash);

        if (token != null)
        {
            sessions = recalculateSessions(tokens.values(), sessions.values());
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

    @Override public PersonalAccessTokenSession getSessionByHash(final String hash)
    {
        return sessions.get(hash);
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

    private void checkToken(PersonalAccessToken token)
    {
        if (StringUtils.isEmpty(token.getHash()))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }
        if (StringUtils.isEmpty(token.getSessionName()))
        {
            throw new UserFailureException("Session name cannot be empty.");
        }
        if (StringUtils.isEmpty(token.getOwnerId()))
        {
            throw new UserFailureException("Owner id cannot be empty.");
        }
        if (StringUtils.isEmpty(token.getRegistratorId()))
        {
            throw new UserFailureException("Registrator id cannot be empty.");
        }
        if (StringUtils.isEmpty(token.getModifierId()))
        {
            throw new UserFailureException("Modifier id cannot be empty.");
        }
        if (token.getValidFromDate() == null)
        {
            throw new UserFailureException("Valid from date cannot be null.");
        }
        if (token.getValidToDate() == null)
        {
            throw new UserFailureException("Valid to date cannot be null.");
        }
        if (token.getValidFromDate().after(token.getValidToDate()))
        {
            throw new UserFailureException("Valid from date cannot be after valid to date.");
        }
        if (token.getRegistrationDate() == null)
        {
            throw new UserFailureException("Registration date cannot be null.");
        }
        if (token.getModificationDate() == null)
        {
            throw new UserFailureException("Modification date cannot be null.");
        }
    }

    private void checkSession(PersonalAccessTokenSession session)
    {
        if (StringUtils.isEmpty(session.getOwnerId()))
        {
            throw new UserFailureException("Owner id cannot be empty.");
        }
        if (StringUtils.isEmpty(session.getName()))
        {
            throw new UserFailureException("Name cannot be empty.");
        }
        if (StringUtils.isEmpty(session.getHash()))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }
    }

    private synchronized Map<String, PersonalAccessTokenSession> recalculateSessions(Collection<PersonalAccessToken> tokens,
            Collection<PersonalAccessTokenSession> sessions)
    {
        Map<Pair<String, String>, PersonalAccessTokenSession> existingSessionsByOwnerAndName = new HashMap<>();
        Map<Pair<String, String>, PersonalAccessTokenSession> newSessionsByOwnerAndName = new HashMap<>();

        for (PersonalAccessTokenSession session : sessions)
        {
            existingSessionsByOwnerAndName.put(new ImmutablePair<>(session.getOwnerId(), session.getName()), session);
        }

        for (PersonalAccessToken token : tokens)
        {
            Pair<String, String> key = new ImmutablePair<>(token.getOwnerId(), token.getSessionName());
            PersonalAccessTokenSession existingSession = existingSessionsByOwnerAndName.get(key);
            PersonalAccessTokenSession newSession = newSessionsByOwnerAndName.get(key);

            if (newSession == null)
            {
                newSession = new PersonalAccessTokenSession();
                if (existingSession != null)
                {
                    newSession.setHash(existingSession.getHash());
                } else
                {
                    newSession.setHash(generator.generateSessionHash(token.getOwnerId()));
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

            newSessionsByOwnerAndName.put(key, newSession);
        }

        Map<String, PersonalAccessTokenSession> result = new HashMap<>();

        for (PersonalAccessTokenSession newSession : newSessionsByOwnerAndName.values())
        {
            PersonalAccessTokenSession existingSession =
                    existingSessionsByOwnerAndName.get(new ImmutablePair<>(newSession.getOwnerId(), newSession.getName()));

            if (existingSession == null || !EqualsBuilder.reflectionEquals(existingSession, newSession))
            {
                result.put(newSession.getHash(), newSession);
            } else
            {
                result.put(existingSession.getHash(), existingSession);
            }
        }

        return result;
    }

    private static Date getEarlierDate(Date date1, Date date2)
    {
        if (date1 != null && date2 != null)
        {
            return date1.before(date2) ? date1 : date2;
        } else
        {
            return date1 != null ? date1 : date2;
        }
    }

    private static Date getLaterDate(Date date1, Date date2)
    {
        if (date1 != null && date2 != null)
        {
            return date1.after(date2) ? date1 : date2;
        } else
        {
            return date1 != null ? date1 : date2;
        }
    }

    private static String formatDate(Date date)
    {
        if (date != null)
        {
            return DATE_FORMAT.format(date);
        }

        return null;
    }

    private static Date parseDate(String str) throws ParseException
    {
        if (str != null && str.trim().length() > 0)
        {
            return DATE_FORMAT.parse(str);
        }
        return null;
    }

    private String getFilePath()
    {
        return properties.getProperty(PERSONAL_ACCESS_TOKENS_FILE_PATH, PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT);
    }

    private synchronized void loadFromFile()
    {
        File file = new File(getFilePath());

        if (file.exists())
        {
            try
            {
                FileContent content = mapper.readValue(file, FileContent.class);

                Map<String, PersonalAccessToken> newTokens = new HashMap<>();
                if (content.tokens != null)
                {
                    for (FileToken fileToken : content.tokens)
                    {
                        PersonalAccessToken newToken = FileToken.create(fileToken);
                        newTokens.put(newToken.getHash(), newToken);
                    }
                }

                Map<String, PersonalAccessTokenSession> newSessions = new HashMap<>();
                if (content.sessions != null)
                {
                    for (FileSession fileSession : content.sessions)
                    {
                        PersonalAccessTokenSession newSession = FileSession.create(fileSession);
                        newSessions.put(newSession.getHash(), newSession);
                    }
                }

                newSessions = recalculateSessions(newTokens.values(), newSessions.values());

                for (PersonalAccessToken newToken : newTokens.values())
                {
                    checkToken(newToken);
                }

                for (PersonalAccessTokenSession newSession : newSessions.values())
                {
                    checkSession(newSession);
                }

                tokens = newTokens;
                sessions = newSessions;

                saveInFile();
            } catch (Exception e)
            {
                operationLog.error("Loading of personal access tokens file failed. File path: " + file.getAbsolutePath(), e);
                tokens = new HashMap<>();
                sessions = new HashMap<>();
            }
        }
    }

    private synchronized void saveInFile()
    {
        File file = new File(getFilePath());

        try
        {
            List<FileToken> fileTokens = new ArrayList<>();
            for (PersonalAccessToken token : tokens.values())
            {
                fileTokens.add(FileToken.create(token));
            }

            List<FileSession> fileSessions = new ArrayList<>();
            for (PersonalAccessTokenSession session : sessions.values())
            {
                fileSessions.add(FileSession.create(session));
            }

            FileContent content = new FileContent();
            content.tokens = fileTokens;
            content.sessions = fileSessions;

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, content);
        } catch (Exception e)
        {
            operationLog.error("Saving of personal access tokens file failed. File path: " + file.getAbsolutePath(), e);
        }
    }

    private static class FileContent
    {

        @JsonProperty
        public List<FileToken> tokens;

        @JsonProperty
        public List<FileSession> sessions;

    }

    @JsonObject("FileToken")
    private static class FileToken
    {
        public String ownerId;

        public String sessionName;

        public String hash;

        public String registratorId;

        public String modifierId;

        public String validFromDate;

        public String validToDate;

        public String registrationDate;

        public String modificationDate;

        public String accessDate;

        public static PersonalAccessToken create(FileToken fileToken) throws ParseException
        {
            PersonalAccessToken token = new PersonalAccessToken();
            token.setOwnerId(fileToken.ownerId);
            token.setSessionName(fileToken.sessionName);
            token.setHash(fileToken.hash);
            token.setRegistratorId(fileToken.registratorId);
            token.setModifierId(fileToken.modifierId);
            token.setValidFromDate(parseDate(fileToken.validFromDate));
            token.setValidToDate(parseDate(fileToken.validToDate));
            token.setRegistrationDate(parseDate(fileToken.registrationDate));
            token.setModificationDate(parseDate(fileToken.modificationDate));
            token.setAccessDate(parseDate(fileToken.accessDate));
            return token;
        }

        public static FileToken create(PersonalAccessToken token)
        {
            FileToken fileToken = new FileToken();
            fileToken.ownerId = token.getOwnerId();
            fileToken.sessionName = token.getSessionName();
            fileToken.hash = token.getHash();
            fileToken.registratorId = token.getRegistratorId();
            fileToken.modifierId = token.getModifierId();
            fileToken.validFromDate = formatDate(token.getValidFromDate());
            fileToken.validToDate = formatDate(token.getValidToDate());
            fileToken.registrationDate = formatDate(token.getRegistrationDate());
            fileToken.modificationDate = formatDate(token.getModificationDate());
            fileToken.accessDate = formatDate(token.getAccessDate());
            return fileToken;
        }
    }

    @JsonObject("FileSession")
    private static class FileSession
    {
        public String ownerId;

        public String name;

        public String hash;

        public static PersonalAccessTokenSession create(FileSession fileSession)
        {
            PersonalAccessTokenSession session = new PersonalAccessTokenSession();
            session.setOwnerId(fileSession.ownerId);
            session.setName(fileSession.name);
            session.setHash(fileSession.hash);
            return session;
        }

        public static FileSession create(PersonalAccessTokenSession session)
        {
            FileSession fileSession = new FileSession();
            fileSession.ownerId = session.getOwnerId();
            fileSession.name = session.getName();
            fileSession.hash = session.getHash();
            return fileSession;
        }
    }

}
