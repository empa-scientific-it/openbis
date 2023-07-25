/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService;
import ch.systemsx.cisd.authentication.ldap.LDAPDirectoryConfiguration;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagementMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final Principal U1 = new Principal("u1", "Albert", "Einstein", "a.e@abc.de");

    private BufferedAppender logRecorder;

    private File configFile;

    private File auditLogFile;

    private File mappingFile;

    private Properties properties;

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        configFile = new File(workingDirectory, "config.json");
        auditLogFile = new File(workingDirectory, "audit_log.txt");
        mappingFile = new File(workingDirectory, "mapping-file.txt");
        properties = new Properties();
        properties.setProperty(UserManagementMaintenanceTask.CONFIGURATION_FILE_PATH_PROPERTY, configFile.getPath());
        properties.setProperty(UserManagementMaintenanceTask.AUDIT_LOG_FILE_PATH_PROPERTY, auditLogFile.getPath());
    }

    @Test
    public void testSetUpFailedBecauseConfigFileDoesNotExist()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        properties.remove(UserManagementMaintenanceTask.CONFIGURATION_FILE_PATH_PROPERTY);

        // When + Then
        assertConfigFailure(task, UserManagementMaintenanceTask.DEFAULT_CONFIGURATION_FILE_PATH + "' doesn't exist or is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseConfigFileIsADirectory()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        configFile.mkdirs();

        // When + Then
        assertConfigFailure(task, configFile.getPath() + "' doesn't exist or is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseAuditFileIsADirectory()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        auditLogFile.mkdirs();

        // When + Then
        assertConfigFailure(task, auditLogFile.getPath() + "' is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseMappingFileIsADirectory()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        properties.setProperty(UserManagementMaintenanceTask.SHARES_MAPPING_FILE_PATH_PROPERTY, mappingFile.getPath());
        mappingFile.mkdirs();

        // When + Then
        assertConfigFailure(task, "Share ids mapping file '" + mappingFile.getAbsolutePath() + "' is a directory.");
    }

    @Test
    public void testExecuteFailedBecauseOfConfigFileHasBeenDeleted()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        configFile.delete();

        // When
        task.execute();

        // Then
        assertContains(configFile + "' doesn't exist", logRecorder.getLogContent());
    }

    @Test
    public void testExecuteFailedBecauseOfInvalidConfigFile()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);

        // When
        task.execute();

        // Then
        assertContains("ERROR OPERATION.UserManagementMaintenanceTaskWithMocks - Invalid content of configuration file '"
                + configFile.getAbsolutePath(), logRecorder.getLogContent());
    }

    @Test
    public void testExecuteNoGroups()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath(), logRecorder.getLogContent());
    }

    @Test
    public void testExecuteFailedBecauseOfInvalidGroupKey()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": ["
                + "{\"key\":\"AB-c\"},"
                + "{\"key\":\"ab.C\"},"
                + "{\"key\":\"AB_C\"},"
                + "{\"key\":\"ab c\"},"
                + "]}");

        // When
        task.execute();

        // Then
        assertContains("Invalid group key: >AB_C<.", logRecorder.getLogContent());
    }

    @Test
    public void testExecuteWithoutLdapGroupKeysAndUsers()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks()
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\"}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'ABC' skipped because no users specified or found.\n" 
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteWithUsersButWithoutLdapGroupKeys()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks()
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\", \"users\":[\"alpha\", \"beta\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:null, enabled:true, ldapGroupKeys:null, users:[alpha, beta], admins:null] with users [alpha=alpha, beta=beta]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 2 users for group ABC\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteWithTwoUsersOneIgnored()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks()
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"usersToBeIgnored\":[\"beta\"], "
                + "\"groups\": [{\"key\":\"ABC\", \"users\":[\"alpha\", \"beta\"]}]}");
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:null, enabled:true, ldapGroupKeys:null, users:[alpha, beta], admins:null] with users [alpha=alpha]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for group ABC\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteEmptyLdapGroupKeys()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks()
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\",\"ldapGroupKeys\":[\"\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'ABC' has empty ldapGroupKey.\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'ABC' skipped because no users specified or found.\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteEmptyLdapGroups()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks()
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\",\"ldapGroupKeys\":[\"a1\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'ABC' has no users found for ldapGroupKey 'a1'.\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'ABC' skipped because no users specified or found.\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteMissingShareIds()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        properties.setProperty(UserManagementMaintenanceTask.SHARES_MAPPING_FILE_PATH_PROPERTY, mappingFile.getPath());
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"commonSpaces\":{\"USER\": [\"ALPHA\"]},"
                + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"],\"admins\": [\"u2\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {USER=[ALPHA]}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'SIS' skipped because no shareIds specified.\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteEmptyShareIds()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        properties.setProperty(UserManagementMaintenanceTask.SHARES_MAPPING_FILE_PATH_PROPERTY, mappingFile.getPath());
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"commonSpaces\":{\"USER\": [\"ALPHA\"]},"
                + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"],\"admins\": [\"u2\"],\"shareIds\":[]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {USER=[ALPHA]}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "WARN  OPERATION.UserManagementMaintenanceTaskWithMocks - Group 'SIS' skipped because no shareIds specified.\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteInvalidCommonSample()
    {
        // Given
        UserManagerReport report = new UserManagerReport(new MockTimeProvider(0, 1000));
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withUserManagerReport(report);
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"commonSpaces\":{\"USER\": [\"ALPHA\"]},\"commonSamples\":{\"A\":\"B\"},"
                + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {USER=[ALPHA]}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {A=B}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "ERROR NOTIFY.UserManagementMaintenanceTaskWithMocks - Identifier template 'A' is invalid "
                + "(reason: No common space for common sample). Template schema: <common space code>/<common sample code>\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group SIS[name:sis, enabled:true, ldapGroupKeys:[s], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for group SIS\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }


    public static final String testExecuteInvalidCommonExperimentsConfig = "{\"commonSpaces\":{\"USER\": [\"ALPHA\"]}, " + getCommonExperiments("ALPHA/B", "B") + ","
            + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"]}]}";

    @Test
    public void testExecuteInvalidCommonExperiments()
    {
        // Given
        UserManagerReport report = new UserManagerReport(new MockTimeProvider(0, 1000));
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withUserManagerReport(report);
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, testExecuteInvalidCommonExperimentsConfig);

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {USER=[ALPHA]}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: [{identifierTemplate=ALPHA/B, experimentType=B}]\n"
                + "ERROR NOTIFY.UserManagementMaintenanceTaskWithMocks - Identifier template 'ALPHA/B' is invalid. "
                + "Template schema: <common space code>/<common project code>/<common experiment code>\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group SIS[name:sis, enabled:true, ldapGroupKeys:[s], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for group SIS\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
    }

    public static final String testExecuteHappyCaseConfig = "{\"globalSpaces\":[\"ES\"],\"commonSpaces\":{\"USER\": [\"ALPHA\"]},"
            + "\"commonSamples\":{\"ALPHA/B\":\"B\"}," + getCommonExperiments("ALPHA/P/E", "E")
            + ",\"instanceAdmins\":[\"u3\"],"
            + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"],\"users\":[\"u2\"],\"admins\": [\"u2\"]},"
            + "{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"],\"enabled\": false}]}";

    @Test
    public void testExecuteHappyCase()
    {
        // Given
        UserManagerReport report = new UserManagerReport(new MockTimeProvider(0, 1000));
        report.addErrorMessage("This is a test error message");
        report.addGroup("blabla");
        report.addUser("a");
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withGroup("a", U1).withUserManagerReport(report);
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, testExecuteHappyCaseConfig);

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 2 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: [ES]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {USER=[ALPHA]}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {ALPHA/B=B}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: [{identifierTemplate=ALPHA/P/E, experimentType=E}]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group SIS[name:sis, enabled:true, ldapGroupKeys:[s], users:[u2], admins:[u2]] with users [u1=u1, u2=u2]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 2 users for group SIS\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:abc, enabled:false, ldapGroupKeys:[a], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for disabled group ABC\n"
                + "ERROR NOTIFY.UserManagementMaintenanceTaskWithMocks - User management failed for the following reason(s):\n\n"
                + "This is a test error message\n\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
        String lastModified = new SimpleDateFormat(UserManagerReport.DATE_FORMAT).format(new Date(configFile.lastModified()));
        assertEquals("1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] blabla\n"
                + "1970-01-01 01:00:01 [ADD-USER] a\n"
                + "1970-01-01 01:00:02 [CONFIG-UPDATE-START] Last modified: " + lastModified + "\n"
                + "{\"globalSpaces\":[\"ES\"],\"commonSpaces\":{\"USER\": [\"ALPHA\"]},\"commonSamples\":{\"ALPHA/B\":\"B\"},"
                + getCommonExperiments("ALPHA/P/E", "E") + ",\"instanceAdmins\":[\"u3\"],\"groups\": ["
                + "{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"],\"users\":[\"u2\"],\"admins\": [\"u2\"]},"
                + "{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"],\"enabled\": false}]}\n"
                + "1970-01-01 01:00:03 [CONFIG-UPDATE-END] \n"
                + "1970-01-01 01:00:04 [ADD-AUTHORIZATION-GROUP] dummy group, known users: [u2, u3]\n\n",
                FileUtilities.loadToString(auditLogFile));
    }

    @Test
    public void testExecuteThriceWithConfigChangedAfterTheSecondRun()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withGroup("a", U1);
        // 1. first run
        task.withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"],\"enabled\": false}]}");
        String lastModified1 = new SimpleDateFormat(UserManagerReport.DATE_FORMAT).format(new Date(configFile.lastModified()));
        task.execute();
        // 2. second run (same config)
        task.withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        task.execute();
        // 3. second run (changed config)
        task.withUserManagerReport(new UserManagerReport(new MockTimeProvider(0, 1000)));
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"]}]}");
        String lastModified2 = new SimpleDateFormat(UserManagerReport.DATE_FORMAT).format(new Date(configFile.lastModified()));

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:abc, enabled:false, ldapGroupKeys:[a], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for disabled group ABC\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:abc, enabled:false, ldapGroupKeys:[a], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for disabled group ABC\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - manage 1 groups\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Global spaces: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common spaces: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common samples: {}\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Common experiments: []\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - Add group ABC[name:abc, enabled:true, ldapGroupKeys:[a], users:null, admins:null] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - 1 users for group ABC\n"
                + "INFO  OPERATION.UserManagementMaintenanceTaskWithMocks - finished",
                logRecorder.getLogContent());
        assertEquals("1970-01-01 01:00:00 [CONFIG-UPDATE-START] Last modified: " + lastModified1 + "\n"
                + "{\"groups\": [{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"],\"enabled\": false}]}\n"
                + "1970-01-01 01:00:01 [CONFIG-UPDATE-END] \n"
                + "1970-01-01 01:00:02 [ADD-AUTHORIZATION-GROUP] dummy group, known users: []\n\n"
                + "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] dummy group, known users: []\n\n"
                + "1970-01-01 01:00:00 [CONFIG-UPDATE-START] Last modified: " + lastModified2 + "\n"
                + "{\"groups\": [{\"name\":\"abc\",\"key\":\"ABC\",\"ldapGroupKeys\": [\"a\"]}]}\n"
                + "1970-01-01 01:00:01 [CONFIG-UPDATE-END] \n"
                + "1970-01-01 01:00:02 [ADD-AUTHORIZATION-GROUP] dummy group, known users: []\n\n",
                FileUtilities.loadToString(auditLogFile));
    }

    private void assertConfigFailure(IMaintenanceTask task, String errorMessageSnippet)
    {
        try
        {
            task.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertContains(errorMessageSnippet, e.getMessage());
        }
    }


    private static String getCommonExperiments(String identifierTemplate, String experimentType) {
        return "\"commonExperiments\": [{ \"identifierTemplate\" : \"" + identifierTemplate + "\", \"experimentType\" : \""  + experimentType + "\" }]";
    }

//    public static void main(String[] args) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        UserManagerConfig config = mapper.readValue(testExecuteHappyCaseConfig, UserManagerConfig.class);
//    }

    private final class UserManagementMaintenanceTaskWithMocks extends UserManagementMaintenanceTask
    {
        private Map<String, List<Principal>> usersByGroup = new TreeMap<>();

        private LDAPDirectoryConfiguration config;

        private UserManagerReport report;

        private UserManagementMaintenanceTaskWithMocks()
        {
            config = new LDAPDirectoryConfiguration();
            config.setServerUrl("blabla");
            config.setSecurityPrincipalDistinguishedName("blabla");
            config.setSecurityPrincipalPassword("blabla");
        }

        @Override
        public void setUp(String pluginName, Properties properties)
        {
            properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, getClass().getName());
            super.setUp(pluginName, properties);
        }

        @Override
        protected List<Principal> getUsersOfGroup(String ldapGroupKey)
        {
            List<Principal> users = usersByGroup.get(ldapGroupKey);
            return users == null ? new LinkedList<>() : users;
        }

        @Override
        protected LDAPAuthenticationService getLdapAuthenticationService()
        {
            return new LDAPAuthenticationService(config);
        }

        @Override
        protected UserManagerReport createUserManagerReport()
        {
            return report == null ? super.createUserManagerReport() : report;
        }

        @Override
        protected UserManager createUserManager(Log4jSimpleLogger logger, UserManagerReport report)
        {
            return new MockUserManager(logger, report);
        }

        private UserManagementMaintenanceTaskWithMocks withGroup(String groupCode, Principal... users)
        {
            usersByGroup.put(groupCode, Arrays.asList(users));
            return this;
        }

        private UserManagementMaintenanceTaskWithMocks withUserManagerReport(UserManagerReport report)
        {
            this.report = report;
            return this;
        }

        private final class MockUserManager extends UserManager
        {
            private ISimpleLogger logger;

            MockUserManager(ISimpleLogger logger, UserManagerReport report)
            {
                super(null, null, null, logger, report);
                this.logger = logger;
            }

            @Override
            public void setGlobalSpaces(List<String> globalSpaces)
            {
                logger.log(LogLevel.INFO, "Global spaces: " + globalSpaces);
                super.setGlobalSpaces(globalSpaces);
            }

            @Override
            public void setCommon(Map<Role, List<String>> commonSpacesByRole, Map<String, String> commonSamples,
                    List<Map<String, String>> commonExperiments)
            {
                logger.log(LogLevel.INFO, "Common spaces: " + commonSpacesByRole);
                logger.log(LogLevel.INFO, "Common samples: " + commonSamples);
                logger.log(LogLevel.INFO, "Common experiments: " + commonExperiments);
                super.setCommon(commonSpacesByRole, commonSamples, commonExperiments);
            }

            @Override
            public void addGroup(UserGroup group, Map<String, Principal> principalsByUserId)
            {
                String renderedGroup = group.getKey() + "[name:" + group.getName() + ", enabled:" + group.isEnabled()
                        + ", ldapGroupKeys:" + group.getLdapGroupKeys() + ", users:" + group.getUsers()
                        + ", admins:" + group.getAdmins() + "]";
                CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                for (Entry<String, Principal> entry : principalsByUserId.entrySet())
                {
                    builder.append(entry.getKey() + "=" + entry.getValue().getUserId());
                }
                logger.log(LogLevel.INFO, "Add group " + renderedGroup + " with users [" + builder + "]");
                super.addGroup(group, principalsByUserId);
            }

            @Override
            public void manage(Set<String> knownUsers, Set<String> usersToBeIgnored)
            {
                report.addGroup("dummy group, known users: " + knownUsers);
            }

        }
    }

}
