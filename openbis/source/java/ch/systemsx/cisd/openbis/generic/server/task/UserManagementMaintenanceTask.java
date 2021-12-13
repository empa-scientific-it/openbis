/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagementMaintenanceTask extends AbstractMaintenanceTask
{
    static final String DEACTIVATE_UNKOWN_USERS_PROPERTY = "deactivate-unknown-users";

    static final String AUDIT_LOG_FILE_PATH_PROPERTY = "audit-log-file-path";

    static final String DEFAULT_AUDIT_LOG_FILE_PATH = "logs/user-management-audit_log.txt";

    static final String SHARES_MAPPING_FILE_PATH_PROPERTY = "shares-mapping-file-path";

    static final String LDAP_FILTER_KEY_PROPERTY = "filter-key";

    static final String DEFAULT_LDAP_FILTER_KEY = "ou";
    
    static final String LDAP_GROUP_QUERY_TEMPLATE = "ldap-group-query-template";

    private File auditLogFile;

    private LDAPAuthenticationService ldapService;

    private File shareIdsMappingFile;

    private String filterKey;

    private boolean deactivateUnknownUsers;

    private BufferedAppender bufferedAppender;

    private int executionId;

    private final Map<Integer, UserManagerReport> reportsById = new HashMap<>();

    private String groupQueryTemplateOrNull;

    public UserManagementMaintenanceTask()
    {
        super(true);
        bufferedAppender = new BufferedAppender("%d{HH:mm:ss,SSS} %-5p [%t] %c - %m%n", Level.INFO, "OPERATION.UserManagementMaintenanceTask");
    }

    @Override
    protected void setUpSpecific(Properties properties)
    {
        deactivateUnknownUsers = PropertyUtils.getBoolean(properties, DEACTIVATE_UNKOWN_USERS_PROPERTY, true);
        auditLogFile = new File(properties.getProperty(AUDIT_LOG_FILE_PATH_PROPERTY, DEFAULT_AUDIT_LOG_FILE_PATH));
        if (auditLogFile.isDirectory())
        {
            throw new ConfigurationFailureException("Audit log file '" + auditLogFile.getAbsolutePath() + "' is a directory.");
        }
        ldapService = getLdapAuthenticationService();
        filterKey = properties.getProperty(LDAP_FILTER_KEY_PROPERTY, DEFAULT_LDAP_FILTER_KEY);
        groupQueryTemplateOrNull = properties.getProperty(LDAP_GROUP_QUERY_TEMPLATE);
        String shareIdsMappingFilePath = properties.getProperty(SHARES_MAPPING_FILE_PATH_PROPERTY);
        if (shareIdsMappingFilePath != null)
        {
            shareIdsMappingFile = new File(shareIdsMappingFilePath);
            if (shareIdsMappingFile.isDirectory())
            {
                throw new ConfigurationFailureException("Share ids mapping file '" + shareIdsMappingFile.getAbsolutePath() + "' is a directory.");
            }
        }
    }

    @Override
    public void execute()
    {
        UserManagerReport report = createUserManagerReport();
        execute(report);
    }

    public int executeAsync()
    {
        executionId++;
        UserManagerReport report = createUserManagerReport();
        reportsById.put(executionId, report);
        operationLog.info(reportsById.size() + " tasks are running.");
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    execute(report);
                }
            }).start();
        return executionId;
    }

    public UserManagerReport getReportById(int executionId)
    {
        return reportsById.get(executionId);
    }

    public void removeReport(int executionId)
    {
        reportsById.remove(executionId);
    }

    public synchronized void execute(UserManagerReport report)
    {
        bufferedAppender.resetLogContent();
        UserManagerConfig config = readGroupDefinitions(report);
        if (config == null || config.getGroups() == null)
        {
            return;
        }
        operationLog.info("manage " + config.getGroups().size() + " groups");
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        Set<String> knownUsers = new HashSet<>();
        UserManager userManager = createUserManager(config, logger, report);
        for (UserGroup group : config.getGroups())
        {
            addGroup(userManager, group);
            if (group.getUsers() != null)
            {
                knownUsers.addAll(group.getUsers());
            }
        }
        userManager.manage(knownUsers);
        handleReport(report);
        operationLog.info("finished");
    }

    private void addGroup(UserManager userManager, UserGroup group)
    {
        String key = group.getKey();
        if (shareIdsMappingFile != null)
        {
            List<String> shareIds = group.getShareIds();
            if (shareIds == null || shareIds.isEmpty())
            {
                operationLog.warn("Group '" + key + "' skipped because no shareIds specified.");
                return;
            }
        }
        Map<String, Principal> principalsByUserId = new TreeMap<>();
        List<String> users = group.getUsers();
        if (users != null && users.isEmpty() == false)
        {
            for (String user : users)
            {
                principalsByUserId.put(user, new Principal(user, "", "", ""));
            }
        }
        List<String> ldapGroupKeys = group.getLdapGroupKeys();
        if (ldapGroupKeys != null && ldapGroupKeys.isEmpty() == false)
        {
            for (String ldapGroupKey : ldapGroupKeys)
            {
                if (StringUtils.isBlank(ldapGroupKey))
                {
                    operationLog.warn("Group '" + key + "' has empty ldapGroupKey.");
                } else
                {
                    try
                    {
                        List<Principal> principals = getUsersOfGroup(ldapGroupKey);
                        if (group.isEnabled() && principals.isEmpty())
                        {
                            operationLog.warn("Group '" + key + "' has no users found for ldapGroupKey '" + ldapGroupKey + "'.");
                        }
                        for (Principal principal : principals)
                        {
                            principalsByUserId.put(principal.getUserId(), principal);
                        }
                    } catch (Throwable e)
                    {
                        operationLog.error("Group '" + key + "' leads to error for ldapGroupKey '" + ldapGroupKey + "': " + e, e);
                    }
                }
            }
        }
        if (principalsByUserId.isEmpty())
        {
            operationLog.warn("Group '" + key + "' skipped because no users specified or found.");
        } else
        {
            userManager.addGroup(group, principalsByUserId);
        }
    }

    private void handleReport(UserManagerReport report)
    {
        String errorReport = report.getErrorReport();
        if (StringUtils.isNotBlank(errorReport))
        {
            notificationLog.error("User management failed for the following reason(s):\n\n" + errorReport);
        }
        String auditLog = report.getAuditLog();
        if (StringUtils.isNotBlank(auditLog))
        {
            FileUtilities.appendToFile(auditLogFile, auditLog, true);
        }
    }

    protected List<Principal> getUsersOfGroup(String ldapGroupKey)
    {
        if (ldapService.isConfigured() == false)
        {
            throw new ConfigurationFailureException("There is no LDAP authentication service configured. "
                    + "At least 'ldap.server.url', 'ldap.security.principal.distinguished.name', "
                    + "'ldap.security.principal.password' have to be specified in 'service.properties'.");
        }
        String query = groupQueryTemplateOrNull == null ? null : String.format(groupQueryTemplateOrNull, ldapGroupKey);
        return ldapService.listPrincipalsByKeyValueOrQuery(filterKey, ldapGroupKey, query);
    }

    protected LDAPAuthenticationService getLdapAuthenticationService()
    {
        return (LDAPAuthenticationService) CommonServiceProvider.tryToGetBean("ldap-authentication-service");
    }

    protected UserManagerReport createUserManagerReport()
    {
        return new UserManagerReport(SystemTimeProvider.SYSTEM_TIME_PROVIDER, bufferedAppender);
    }

    private UserManager createUserManager(UserManagerConfig config, Log4jSimpleLogger logger, UserManagerReport report)
    {
        UserManager userManager = createUserManager(logger, report);
        userManager.setGlobalSpaces(config.getGlobalSpaces());
        try
        {
            userManager.setCommon(config.getCommonSpaces(), config.getCommonSamples(), config.getCommonExperiments());
        } catch (ConfigurationFailureException e)
        {
            notificationLog.error(e.getMessage());
        }
        return userManager;
    }

    protected UserManager createUserManager(Log4jSimpleLogger logger, UserManagerReport report)
    {
        IAuthenticationService authenticationService = (IAuthenticationService) CommonServiceProvider.tryToGetBean("authentication-service");
        UserManager userManager = new UserManager(authenticationService, CommonServiceProvider.getApplicationServerApi(),
                shareIdsMappingFile, logger, report);
        userManager.setDeactivateUnknwonUsers(deactivateUnknownUsers);
        return userManager;
    }
}
