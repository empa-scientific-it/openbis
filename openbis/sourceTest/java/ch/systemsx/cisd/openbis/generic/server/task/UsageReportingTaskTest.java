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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.activation.DataHandler;

import org.apache.log4j.Level;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class UsageReportingTaskTest extends AbstractFileSystemTestCase
{
    private static final String D = UsageReportingTask.DELIM;

    private BufferedAppender logRecorder;

    private File configFile;

    private Properties properties;

    private Mockery context;

    private IMailClient mailClient;

    private RecordingMatcher<String> subjectRecorder;

    private RecordingMatcher<String> contentRecorder;

    private RecordingMatcher<String> fileNameRecorder;

    private RecordingMatcher<DataHandler> attachmentRecorder;

    private RecordingMatcher<EMailAddress[]> recipientRecorder;

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        subjectRecorder = new RecordingMatcher<String>();
        contentRecorder = new RecordingMatcher<String>();
        fileNameRecorder = new RecordingMatcher<String>();
        attachmentRecorder = new RecordingMatcher<DataHandler>();
        recipientRecorder = new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    allowing(mailClient).sendEmailMessageWithAttachment(with(subjectRecorder), with(contentRecorder),
                            with(fileNameRecorder), with(attachmentRecorder), with(new IsNull<EMailAddress>()),
                            with(new IsNull<EMailAddress>()), with(recipientRecorder));
                }
            });
        configFile = new File(workingDirectory, "config.json");
        properties = new Properties();
        properties.setProperty(UsageReportingTask.CONFIGURATION_FILE_PATH_PROPERTY, configFile.getPath());
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a1@bc.de, a2@bc.de");
    }

    @DataProvider(name = "boolean")
    public java.lang.Object[][] provideBoolean()
    {
        return new java.lang.Object[][] { { true }, { false } };
    }

    @Test(dataProvider = "boolean")
    public void testMonthlyGroupsOnlyReport(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").space("A_X").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("A_Y").newSamples(7);
        task.user("u2").space("B_X").newExperiments(2);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B_Y").newExperiments(3);
        task.user("u4");
        task.user("u5");
        task.group("A", "u1", "u4");
        task.group("B", "u2", "u3");
        FileUtilities.writeToFile(configFile, "");
        setIntervalOrRunSchedule(properties, intervalSpecified, "30 d", "1. 1");
        properties.setProperty(UsageReportingTask.USER_REPORTING_KEY, UsageReportingTask.UserReportingType.NONE.name());
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}, {\"key\":\"A\"}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-01 until 1970-01-01\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1969-12-01 00:00:00, 1970-01-01 00:00:00]", task.periodListAsString());
        assertEquals("[[B, A]]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-01 until 1970-01-01", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-01_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets\n"
                + "1969-12-01 00:00:00" + D + "1970-01-01 00:00:00" + D + "" + D + "5" + D + "u4 u5" + D + "7" + D + "10" + D + "6\n"
                + "1969-12-01 00:00:00" + D + "1970-01-01 00:00:00" + D + "A" + D + "2" + D + "u4" + D + "2" + D + "0" + D + "1\n"
                + "1969-12-01 00:00:00" + D + "1970-01-01 00:00:00" + D + "B" + D + "2" + D + D + "5" + D + "0" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "boolean")
    public void testWeeklyGroupsAndAllUsersReport(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").space("A_X").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("A_Y").newSamples(4);
        task.user("u2").space("A_Z").newSamples(3);
        task.user("u2").space("B_X").newSamples(2);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B_Y").newSamples(3);
        task.user("u4");
        task.user("u5");
        task.group("A", "u1", "u4");
        task.group("B", "u2", "u3");
        FileUtilities.writeToFile(configFile, "");
        setIntervalOrRunSchedule(properties, intervalSpecified, "7 d", "Sa 21:42");
        properties.setProperty(UsageReportingTask.USER_REPORTING_KEY, UsageReportingTask.UserReportingType.ALL.name());
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}, {\"key\":\"A\"}]}");
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-21 until 1969-12-28\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1969-12-21 00:00:00, 1969-12-28 00:00:00]", task.periodListAsString());
        assertEquals("[[B, A]]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-21 until 1969-12-28", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-21_1969-12-28.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "" + D + "5" + D + "u4 u5" + D + "2" + D + "15" + D + "6\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "A" + D + "2" + D + "u4" + D + "2" + D + "0" + D + "1\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "B" + D + "2" + D + D + "0" + D + "5" + D + "0\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "u1" + D + "1" + D + D + "2" + D + "1" + D + "1\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "u2" + D + "1" + D + D + "0" + D + "11" + D + "5\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "u3" + D + "1" + D + D + "0" + D + "3" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "boolean")
    public void testWeeklyGroupsAndAllUsersReportCountEntities(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(1234567890));
        task.user("u1").space("A_X").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("A_Y").newSamples(4);
        task.user("u2").space("A_Z").newSamples(3);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B1").newSamples(3);
        task.user("u4");
        task.user("u5");
        task.group("A", "u1", "u4");
        task.group("B", "u2", "u3");
        task.next();
        task.user("u1").space("A_Y").newExperiments(5).newDataSets(6).newSamples(3);
        task.user("u2").space("B_X").newExperiments(3).newDataSets(7).newSamples(13);
        task.user("u3").space("B_Y").newSamples(3).newDataSets(11);
        task.user("u3").space("C").newExperiments(2).newDataSets(17);
        task.user("u4").space("A_Z").newExperiments(5).newDataSets(16);
        FileUtilities.writeToFile(configFile, "");
        setIntervalOrRunSchedule(properties, intervalSpecified, "7 d", "Mo 21:42");
        properties.setProperty(UsageReportingTask.USER_REPORTING_KEY, UsageReportingTask.UserReportingType.ALL.name());
        properties.setProperty(UsageReportingTask.COUNT_ALL_ENTITIES_KEY, Boolean.toString(true));
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}, {\"key\":\"A\"}]}");
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1970-01-04 until 1970-01-11\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1970-01-04 00:00:00, 1970-01-11 00:00:00], [1970-01-01 01:00:00, 1970-01-11 00:00:00]", task.periodListAsString());
        assertEquals("[[B, A], [B, A]]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1970-01-04 until 1970-01-11", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1970-01-04_1970-01-11.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets" + D + "total number of entities\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "" + D + "5" + D + "u4 u5" + D + "2" + D + "13" + D + "6" + D + "91\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "A" + D + "2" + D + "u4" + D + "2" + D + "0" + D + "1" + D + "35\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "B" + D + "2" + D + "u2 u3" + D + "0" + D + "0" + D + "0" + D + "37\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "u1" + D + "1" + D + D + "2" + D + "1" + D + "1" + D + "14\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "u2" + D + "1" + D + D + "0" + D + "9" + D + "5" + D + "23\n"
                + "1970-01-04 00:00:00" + D + "1970-01-11 00:00:00" + D + "u3" + D + "1" + D + D + "0" + D + "3" + D + "0" + D + "33\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "boolean")
    public void testDailyGroupsAndOutsideUsersReport(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").space("A_X").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("A_Y").newSamples(4);
        task.user("u2").space("A_Z").newSamples(3);
        task.user("u2").space("B_X").newDataSets(2);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B_Y").newDataSets(3);
        task.user("u4");
        task.user("u5");
        task.group("A", "u1", "u4");
        task.group("B", "u2", "u3");
        FileUtilities.writeToFile(configFile, "");
        setIntervalOrRunSchedule(properties, intervalSpecified, "1 d", "21:42");
        properties.setProperty(UsageReportingTask.USER_REPORTING_KEY, UsageReportingTask.UserReportingType.OUTSIDE_GROUP_ONLY.name());
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}, {\"key\":\"A\"}]}");
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-31 until 1970-01-01\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1969-12-31 00:00:00, 1970-01-01 00:00:00]", task.periodListAsString());
        assertEquals("[[B, A]]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-31 until 1970-01-01", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-31_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "" + D + "5" + D + "u4 u5" + D + "2" + D + "10" + D + "11\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "A" + D + "2" + D + "u4" + D + "2" + D + "0" + D + "1\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "B" + D + "2" + D + D + "0" + D + "0" + D + "5\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u1" + D + "1" + D + D + "0" + D + "1" + D + "0\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u2" + D + "1" + D + D + "0" + D + "9" + D + "5\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "boolean")
    public void testDailyReportWithoutGroups(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").space("A").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("B").newExperiments(2);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B").newExperiments(3);
        task.user("u4");
        setIntervalOrRunSchedule(properties, intervalSpecified, "1 d", "21:42");
        task.setUp("", properties);
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized.\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-31 until 1970-01-01\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1969-12-31 00:00:00, 1970-01-01 00:00:00]", task.periodListAsString());
        assertEquals("[null]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-31 until 1970-01-01", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-31_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(1).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "" + D + "4" + D + "u4" + D + "7" + D + "3" + D + "6\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u1" + D + "1" + D + D + "2" + D + "1" + D + "1\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u2" + D + "1" + D + D + "2" + D + "2" + D + "5\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u3" + D + "1" + D + D + "3" + D + "0" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "boolean")
    public void testDailyReportWithoutGroupsAndOutsideUsers(boolean intervalSpecified) throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").space("A").newExperiments(2).newDataSets(1);
        task.user("u1").space("C").newSamples(1);
        task.user("u2").space("B").newExperiments(2);
        task.user("u2").space("A1").newSamples(2).newDataSets(5);
        task.user("u3").space("B").newExperiments(3);
        task.user("u4");
        setIntervalOrRunSchedule(properties, intervalSpecified, "1 d", "21:42");
        properties.setProperty(UsageReportingTask.USER_REPORTING_KEY, UsageReportingTask.UserReportingType.OUTSIDE_GROUP_ONLY.name());
        task.setUp("", properties);
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized.\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-31 until 1970-01-01\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertEquals("[1969-12-31 00:00:00, 1970-01-01 00:00:00]", task.periodListAsString());
        assertEquals("[null]", task.groupsListAsString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-31 until 1970-01-01", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-31_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(1).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new collections" + D + "number of new objects" + D + "number of new data sets\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "" + D + "4" + D + "u4" + D + "7" + D + "3" + D + "6\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u1" + D + "1" + D + D + "2" + D + "1" + D + "1\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u2" + D + "1" + D + D + "2" + D + "2" + D + "5\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "u3" + D + "1" + D + D + "3" + D + "0" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    private void setIntervalOrRunSchedule(Properties properties, boolean intervalSpecified,
            String interval, String runSchedule)
    {
        if (intervalSpecified)
        {
            properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, interval);
        } else
        {
            properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, runSchedule);
        }
    }

    private void assertRecorders(RecordingMatcher<?> recorder, String expectedRecord, int expectedCount)
    {
        List<?> objects = recorder.getRecordedObjects();
        for (int i = 0; i < expectedCount; i++)
        {
            assertEquals("Object index " + i, expectedRecord, objects.get(i).toString());
        }
    }

    private static final class UsageReportingTaskWithMocks extends UsageReportingTask
    {
        private final IMailClient mailClient;

        private Date actualTimeStamp;

        private List<Map<String, Map<String, UsageInfo>>> listOfUsageByUsersAndSpaces = new ArrayList<>();
        
        private Map<String, Set<String>> usersByGroups = new TreeMap<>();

        private List<List<String>> groupsList = new ArrayList<>();

        private List<Period> periodList = new ArrayList<>();
        
        private int index;

        UsageReportingTaskWithMocks(IMailClient mailClient)
        {
            this.mailClient = mailClient;
            next();
        }
        
        @Override
        public void setUp(String pluginName, Properties properties)
        {
            properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, getClass().getName());
            super.setUp(pluginName, properties);
        }

        void next()
        {
            listOfUsageByUsersAndSpaces.add(0, new TreeMap<>());
            index = listOfUsageByUsersAndSpaces.size();
        }

        UsageReportingTaskWithMocks time(Date actualTimeStamp)
        {
            this.actualTimeStamp = actualTimeStamp;
            return this;
        }

        UsageBySpacesBuilder user(String user)
        {
            Map<String, UsageInfo> usageBySpaces = listOfUsageByUsersAndSpaces.get(0).get(user);
            if (usageBySpaces == null)
            {
                usageBySpaces = new TreeMap<>();
                listOfUsageByUsersAndSpaces.get(0).put(user, usageBySpaces);
            }
            return new UsageBySpacesBuilder(usageBySpaces);
        }
        
        UsageReportingTaskWithMocks group(String group, String...users)
        {
            usersByGroups.put(group, new HashSet<>(Arrays.asList(users)));
            return this;
        }

        @Override
        protected Date getActualTimeStamp()
        {
            return actualTimeStamp;
        }

        @Override
        protected UsageAndGroupsInfo gatherUsageAndGroups(List<String> groups, Period period)
        {
            groupsList.add(groups);
            periodList.add(period);
            return new UsageAndGroupsInfo(listOfUsageByUsersAndSpaces.get(--index), usersByGroups);
        }

        @Override
        protected IMailClient getMailClient()
        {
            return mailClient;
        }
        
        String groupsListAsString()
        {
            return groupsList.toString();
        }
        
        String periodListAsString()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (Period period : periodList)
            {
                builder.append("[" + dateFormat.format(period.getFrom()) + ", " + dateFormat.format(period.getUntil()) + "]");
            }
            return builder.toString();
        }
    }

    private static final class UsageBySpacesBuilder
    {
        private Map<String, UsageInfo> usageBySpaces;

        UsageBySpacesBuilder(Map<String, UsageInfo> usageBySpaces)
        {
            this.usageBySpaces = usageBySpaces;
        }

        UsageInfoBuilder space(String group)
        {
            UsageInfo usageInfo = new UsageInfo();
            usageBySpaces.put(group, usageInfo);
            return new UsageInfoBuilder(usageInfo);
        }
    }

    private static final class UsageInfoBuilder
    {
        private UsageInfo usageInfo;

        UsageInfoBuilder(UsageInfo usageInfo)
        {
            this.usageInfo = usageInfo;
        }

        UsageInfoBuilder newExperiments(int numberOfNewExperiments)
        {
            for (; numberOfNewExperiments > 0; numberOfNewExperiments--)
            {
                usageInfo.addNewExperiment();
            }
            return this;
        }

        UsageInfoBuilder newSamples(int numberOfNewSamples)
        {
            for (; numberOfNewSamples > 0; numberOfNewSamples--)
            {
                usageInfo.addNewSample();
            }
            return this;
        }

        UsageInfoBuilder newDataSets(int numberOfNewDataSets)
        {
            for (; numberOfNewDataSets > 0; numberOfNewDataSets--)
            {
                usageInfo.addNewDataSet();
            }
            return this;
        }

    }
}
