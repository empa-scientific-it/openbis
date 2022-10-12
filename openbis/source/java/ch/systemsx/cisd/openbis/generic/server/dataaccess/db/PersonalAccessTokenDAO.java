package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.authentication.SessionTokenHash;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenHash;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;

@Component
public class PersonalAccessTokenDAO implements IPersonalAccessTokenDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PersonalAccessTokenDAO.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";

    private final IPersonalAccessTokenConfig config;

    private final IPersonDAO personDAO;

    private final HashGenerator generator;

    private final ITimeProvider timeProvider;

    private Map<String, FileToken> fileTokens = new LinkedHashMap<>();

    private Map<String, FileSession> fileSessions = new LinkedHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public interface HashGenerator
    {
        String generateTokenHash(String user);

        String generateSessionHash(String user);
    }

    @Autowired
    public PersonalAccessTokenDAO(final IPersonalAccessTokenConfig config, final IDAOFactory daoFactory)
    {
        this(config, daoFactory.getPersonDAO(), new HashGenerator()
        {
            @Override public String generateTokenHash(String user)
            {
                return PersonalAccessTokenHash.create(user, System.currentTimeMillis()).toString();
            }

            @Override public String generateSessionHash(String user)
            {
                return SessionTokenHash.create(user, System.currentTimeMillis()).toString();
            }
        }, new ITimeProvider()
        {
            @Override public long getTimeInMilliseconds()
            {
                return System.currentTimeMillis();
            }
        });
    }

    PersonalAccessTokenDAO(final IPersonalAccessTokenConfig config, final IPersonDAO personDAO,
            final HashGenerator generator, final ITimeProvider timeProvider)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        if (personDAO == null)
        {
            throw new IllegalArgumentException("Person DAO cannot be null");
        }

        if (generator == null)
        {
            throw new IllegalArgumentException("Hash generator cannot be null");
        }

        if (timeProvider == null)
        {
            throw new IllegalArgumentException("Time provider cannot be null");
        }

        this.config = config;
        this.personDAO = personDAO;
        this.generator = generator;
        this.timeProvider = timeProvider;

        if (config.arePersonalAccessTokensEnabled())
        {
            loadFromFile();
        }
    }

    private void checkPersonalAccessTokensEnabled()
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            throw new UserFailureException("Personal access tokens are disabled");
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized PersonalAccessToken createToken(final PersonalAccessToken creation)
    {
        checkPersonalAccessTokensEnabled();

        if (StringUtils.isEmpty(creation.getSessionName()))
        {
            throw new UserFailureException("Session name cannot be empty.");
        }

        if (StringUtils.isEmpty(creation.getOwnerId()))
        {
            throw new UserFailureException("Owner id cannot be empty.");
        }

        PersonPE owner = getUser(creation.getOwnerId());
        if (owner == null)
        {
            throw new UserFailureException("Owner with id: " + creation.getOwnerId() + " does not exist.");
        }

        if (StringUtils.isEmpty(creation.getRegistratorId()))
        {
            throw new UserFailureException("Registrator id cannot be empty.");
        }

        PersonPE registrator = getUser(creation.getRegistratorId());

        if (registrator == null)
        {
            throw new UserFailureException("Registrator with id: " + creation.getRegistratorId() + " does not exist.");
        }

        if (StringUtils.isEmpty(creation.getModifierId()))
        {
            throw new UserFailureException("Modifier id cannot be empty.");
        }

        PersonPE modifier = getUser(creation.getModifierId());

        if (modifier == null)
        {
            throw new UserFailureException("Modifier with id: " + creation.getModifierId() + " does not exist.");
        }

        if (creation.getValidFromDate() == null)
        {
            throw new UserFailureException("Valid from date cannot be null.");
        }

        if (creation.getValidToDate() == null)
        {
            throw new UserFailureException("Valid to date cannot be null.");
        }

        if (creation.getValidToDate().getTime() < timeProvider.getTimeInMilliseconds())
        {
            throw new UserFailureException("Valid to date cannot be in the past.");
        }

        if (creation.getValidFromDate().getTime() >= creation.getValidToDate().getTime())
        {
            throw new UserFailureException("Valid to date has to be after valid from date.");
        }

        if (creation.getRegistrationDate() == null)
        {
            throw new UserFailureException("Registration date cannot be null.");
        }

        if (creation.getModificationDate() == null)
        {
            throw new UserFailureException("Modification date cannot be null.");
        }

        FileToken fileToken = new FileToken();
        fileToken.hash = generator.generateTokenHash(creation.getOwnerId());
        fileToken.sessionName = creation.getSessionName();
        fileToken.ownerId = owner.getId();
        fileToken.registratorId = registrator.getId();
        fileToken.modifierId = modifier.getId();
        fileToken.validFromDate = creation.getValidFromDate();

        long maxValidityPeriod = config.getPersonalAccessTokensMaxValidityPeriod() * 1000;
        long validityPeriod = creation.getValidToDate().getTime() - creation.getValidFromDate().getTime();

        if (validityPeriod < maxValidityPeriod)
        {
            fileToken.validToDate = creation.getValidToDate();
        } else
        {
            fileToken.validToDate = new Date(creation.getValidFromDate().getTime() + maxValidityPeriod);
        }

        fileToken.registrationDate = creation.getRegistrationDate();
        fileToken.modificationDate = creation.getModificationDate();

        fileTokens.put(fileToken.hash, fileToken);
        fileSessions = calculateSessions(fileTokens.values(), fileSessions.values());
        saveInFile();

        operationLog.info(
                "Created personal access token - owner: " + fileToken.ownerId + ", sessionName: " + fileToken.sessionName + ", validFrom: "
                        + fileToken.validFromDate + ", validTo: " + fileToken.validToDate);

        return getTokenByHash(fileToken.hash);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized void updateToken(final PersonalAccessToken update)
    {
        checkPersonalAccessTokensEnabled();

        if (update == null)
        {
            throw new UserFailureException("Update cannot be null.");
        }

        if (StringUtils.isEmpty(update.getHash()))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }

        if (StringUtils.isEmpty(update.getModifierId()))
        {
            throw new UserFailureException("Modifier id cannot be empty.");
        }

        PersonPE modifier = getUser(update.getModifierId());

        if (modifier == null)
        {
            throw new UserFailureException("Modifier with id: " + update.getModifierId() + " does not exist.");
        }

        if (update.getModificationDate() == null)
        {
            throw new UserFailureException("Modification date cannot be null.");
        }

        FileToken fileToken = fileTokens.get(update.getHash());

        if (fileToken != null)
        {
            if (update.getAccessDate() != null)
            {
                fileToken.accessDate = update.getAccessDate();
            }
            fileToken.modifierId = modifier.getId();
            fileToken.modificationDate = update.getModificationDate();

            fileTokens.put(fileToken.hash, fileToken);
            fileSessions = calculateSessions(fileTokens.values(), fileSessions.values());
            saveInFile();
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized void deleteToken(final String hash)
    {
        checkPersonalAccessTokensEnabled();

        if (StringUtils.isEmpty(hash))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }

        FileToken fileToken = fileTokens.remove(hash);

        if (fileToken != null)
        {
            fileSessions = calculateSessions(fileTokens.values(), fileSessions.values());
            saveInFile();

            operationLog.info(
                    "Deleted personal access token - owner: " + fileToken.ownerId + ", sessionName: " + fileToken.sessionName + ", validFrom: "
                            + fileToken.validFromDate + ", validTo: " + fileToken.validToDate);
        }
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized List<PersonalAccessToken> listTokens()
    {
        checkPersonalAccessTokensEnabled();

        List<PersonalAccessToken> tokens = new ArrayList<>();

        for (FileToken fileToken : fileTokens.values())
        {
            PersonalAccessToken token = convert(fileToken);

            if (token != null)
            {
                tokens.add(token);
            }
        }

        return tokens;
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized PersonalAccessToken getTokenByHash(final String hash)
    {
        checkPersonalAccessTokensEnabled();

        if (StringUtils.isEmpty(hash))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }

        FileToken fileToken = fileTokens.get(hash);

        if (fileToken == null)
        {
            return null;
        }

        return convert(fileToken);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized List<PersonalAccessTokenSession> listSessions()
    {
        checkPersonalAccessTokensEnabled();

        List<PersonalAccessTokenSession> sessions = new ArrayList<>();

        for (FileSession fileSession : fileSessions.values())
        {
            PersonalAccessTokenSession session = convert(fileSession);

            if (session != null)
            {
                sessions.add(session);
            }
        }

        return sessions;
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public PersonalAccessTokenSession getSessionByHash(final String hash)
    {
        checkPersonalAccessTokensEnabled();

        if (StringUtils.isEmpty(hash))
        {
            throw new UserFailureException("Hash cannot be empty.");
        }

        FileSession fileSession = fileSessions.get(hash);

        if (fileSession == null)
        {
            return null;
        }

        return convert(fileSession);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override public synchronized PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        checkPersonalAccessTokensEnabled();
        
        if (StringUtils.isEmpty(userId))
        {
            throw new UserFailureException("User id cannot be empty.");
        }

        if (StringUtils.isEmpty(sessionName))
        {
            throw new UserFailureException("Session name cannot be empty.");
        }

        PersonPE user = getUser(userId);

        if (user == null)
        {
            return null;
        }

        for (FileSession fileSession : fileSessions.values())
        {
            if (fileSession.ownerId.equals(user.getId()) && fileSession.name.equals(sessionName))
            {
                return convert(fileSession);
            }
        }

        return null;
    }

    private synchronized Map<String, FileSession> calculateSessions(Collection<FileToken> tokens,
            Collection<FileSession> sessions)
    {
        Map<Pair<Long, String>, FileSession> existingSessionsByOwnerAndName = new HashMap<>();
        Map<Pair<Long, String>, FileSession> newSessionsByOwnerAndName = new HashMap<>();

        for (FileSession session : sessions)
        {
            existingSessionsByOwnerAndName.put(new ImmutablePair<>(session.ownerId, session.name), session);
        }

        for (FileToken token : tokens)
        {
            Pair<Long, String> key = new ImmutablePair<>(token.ownerId, token.sessionName);
            FileSession existingSession = existingSessionsByOwnerAndName.get(key);
            FileSession newSession = newSessionsByOwnerAndName.get(key);

            if (newSession == null)
            {
                newSession = new FileSession();
                if (existingSession != null)
                {
                    newSession.hash = existingSession.hash;
                } else
                {
                    PersonPE owner = getUser(token.ownerId);

                    if (owner == null)
                    {
                        continue;
                    } else
                    {
                        newSession.hash = generator.generateSessionHash(owner.getUserId());
                    }
                }
                newSession.name = token.sessionName;
                newSession.ownerId = token.ownerId;
                newSession.validFromDate = token.validFromDate;
                newSession.validToDate = token.validToDate;
                newSession.accessDate = token.accessDate;
            } else
            {
                newSession.validFromDate = getEarlierDate(token.validFromDate, newSession.validFromDate);
                newSession.validToDate = getLaterDate(token.validToDate, newSession.validToDate);
                newSession.accessDate = getLaterDate(token.accessDate, newSession.accessDate);
            }

            newSessionsByOwnerAndName.put(key, newSession);
        }

        Map<String, FileSession> result = new LinkedHashMap<>();

        for (FileSession newSession : newSessionsByOwnerAndName.values())
        {
            result.put(newSession.hash, newSession);
        }

        return result;
    }

    private PersonalAccessToken convert(FileToken fileToken)
    {
        PersonPE owner = getUser(fileToken.ownerId);

        if (owner == null)
        {
            return null;
        }

        PersonPE registrator = getUser(fileToken.registratorId);
        PersonPE modifier = getUser(fileToken.modifierId);

        PersonalAccessToken token = new PersonalAccessToken();
        token.setHash(fileToken.hash);
        token.setSessionName(fileToken.sessionName);
        token.setOwnerId(owner.getUserId());
        token.setRegistratorId(registrator != null ? registrator.getUserId() : null);
        token.setModifierId(modifier != null ? modifier.getUserId() : null);
        token.setValidFromDate(fileToken.validFromDate);
        token.setValidToDate(fileToken.validToDate);
        token.setRegistrationDate(fileToken.registrationDate);
        token.setModificationDate(fileToken.modificationDate);
        token.setAccessDate(fileToken.accessDate);

        return token;
    }

    private PersonalAccessTokenSession convert(FileSession fileSession)
    {
        PersonPE owner = getUser(fileSession.ownerId);

        if (owner == null)
        {
            return null;
        }

        PersonalAccessTokenSession session = new PersonalAccessTokenSession();
        session.setOwnerId(owner.getUserId());
        session.setName(fileSession.name);
        session.setHash(fileSession.hash);
        session.setValidFromDate(fileSession.validFromDate);
        session.setValidToDate(fileSession.validToDate);
        session.setAccessDate(fileSession.accessDate);

        return session;
    }

    private PersonPE getUser(Long userTechId)
    {
        if (userTechId != null)
        {
            PersonPE user = personDAO.tryGetByTechId(new TechId(userTechId));
            return user != null && user.isActive() ? user : null;
        }

        return null;
    }

    private PersonPE getUser(String userId)
    {
        if (userId != null)
        {
            PersonPE user = personDAO.tryFindPersonByUserId(userId);
            return user != null && user.isActive() ? user : null;
        }

        return null;
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

    private synchronized void loadFromFile()
    {
        File file = new File(config.getPersonalAccessTokensFilePath());

        if (file.exists())
        {
            try
            {
                FileContent content = mapper.readValue(file, FileContent.class);

                Map<String, FileToken> newTokens = new LinkedHashMap<>();
                if (content.tokens != null)
                {
                    for (FileToken fileToken : content.tokens)
                    {
                        if (fileToken.ownerId == null)
                        {
                            throw new UserFailureException("Owner id cannot be null.");
                        }

                        if (StringUtils.isEmpty(fileToken.sessionName))
                        {
                            throw new UserFailureException("Session name cannot be empty.");
                        }

                        if (StringUtils.isEmpty(fileToken.hash))
                        {
                            throw new UserFailureException("Hash cannot be empty.");
                        }

                        if (fileToken.registratorId == null)
                        {
                            throw new UserFailureException("Registrator id cannot be null.");
                        }

                        if (fileToken.modifierId == null)
                        {
                            throw new UserFailureException("Modifier id cannot be null.");
                        }

                        if (fileToken.validFromDate == null)
                        {
                            throw new UserFailureException("Valid from date cannot be null.");
                        }

                        if (fileToken.validToDate == null)
                        {
                            throw new UserFailureException("Valid to date cannot be null.");
                        }

                        if (fileToken.validFromDate.getTime() >= fileToken.validToDate.getTime())
                        {
                            throw new UserFailureException("Valid to date has to be after valid from date.");
                        }

                        if (fileToken.registrationDate == null)
                        {
                            throw new UserFailureException("Registration date cannot be null.");
                        }

                        if (fileToken.modificationDate == null)
                        {
                            throw new UserFailureException("Modification date cannot be null.");
                        }

                        newTokens.put(fileToken.hash, fileToken);
                    }
                }

                Map<String, FileSession> newSessions = new LinkedHashMap<>();
                if (content.sessions != null)
                {
                    for (FileSession fileSession : content.sessions)
                    {
                        if (fileSession.ownerId == null)
                        {
                            throw new UserFailureException("Owner id cannot be null.");
                        }
                        if (StringUtils.isEmpty(fileSession.name))
                        {
                            throw new UserFailureException("Name cannot be empty.");
                        }
                        if (StringUtils.isEmpty(fileSession.hash))
                        {
                            throw new UserFailureException("Hash cannot be empty.");
                        }
                        newSessions.put(fileSession.hash, fileSession);
                    }
                }

                newSessions = calculateSessions(newTokens.values(), newSessions.values());

                fileTokens = newTokens;
                fileSessions = newSessions;

                saveInFile();

                operationLog.info("Personal access tokens file successfully loaded. Number of tokens: " + newTokens.size()
                        + ". Number of sessions: " + newSessions.size() + ". File path: " + file.getAbsolutePath());
            } catch (Exception e)
            {
                operationLog.error("Loading of personal access tokens file failed. File path: " + file.getAbsolutePath(), e);
                fileTokens = new HashMap<>();
                fileSessions = new HashMap<>();
            }
        }
    }

    private synchronized void saveInFile()
    {
        File file = new File(config.getPersonalAccessTokensFilePath());

        try
        {
            FileContent content = new FileContent();
            content.tokens = new ArrayList<>(fileTokens.values());
            content.sessions = new ArrayList<>(fileSessions.values());

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
        public Long ownerId;

        public String sessionName;

        public String hash;

        public Long registratorId;

        public Long modifierId;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        public Date validFromDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        public Date validToDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        public Date registrationDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        public Date modificationDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        public Date accessDate;

    }

    @JsonObject("FileSession")
    private static class FileSession
    {
        public Long ownerId;

        public String name;

        public String hash;

        @JsonIgnore
        public Date validFromDate;

        @JsonIgnore
        public Date validToDate;

        @JsonIgnore
        public Date accessDate;
    }

}
