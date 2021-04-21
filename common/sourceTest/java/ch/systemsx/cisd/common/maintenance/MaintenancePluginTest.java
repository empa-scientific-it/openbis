/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.maintenance;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * @author Franz-Josef Elmer
 */
public class MaintenancePluginTest extends AbstractFileSystemTestCase
{
    private static final int TOLERANCE = 20;

    private static final String CRON = MaintenanceTaskParameters.CRON_PREFIX;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUpLogRecorder()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
    }

    @DataProvider(name = "properties")
    public static Object[][] properties()
    {
        return new Object[][] {
                new Object[] { MaintenanceTaskParameters.INTERVAL_KEY, "2 sec" },
                new Object[] { MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/2 * * * * *" }
        };
    }

    @Test(dataProvider = "properties")
    public void test(String property, String value)
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        properties.setProperty(property, value);
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        MaintenancePlugin plugin = new MaintenancePlugin(task, parameters);
        Date start = new Date();

        // When
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
        }
        plugin.shutdown();

        // Then
        assertEquals(3, executionDates.size());
        int expectedDifference = 2000;
        for (int i = 1; i < executionDates.size(); i++)
        {
            assertTimestampDifference(executionDates, i, expectedDifference);
        }
        if (property.equals(MaintenanceTaskParameters.RUN_SCHEDULE_KEY))
        {
            assertTimestamps(executionDates, start, 2000, true);
        }
    }

    @Test
    public void testMissedNext()
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/5 * * * * *");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        MaintenancePlugin plugin = new MaintenancePlugin(task, parameters);
        Date now = new Date();
        Date missedNext = new Date(parameters.getNextTimestampProvider().getNextTimestamp(now).getTime() - 5000);
        System.out.println("now:" + now + ", missedNext:" + missedNext);
        FileUtilities.writeToFile(getPersistentNextDateFile(),
                new SimpleDateFormat(MaintenancePlugin.TIME_STAMP_FORMAT).format(missedNext));

        // When
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
        }
        plugin.shutdown();

        // Then
        assertEquals(3, executionDates.size());
        System.out.println("Execution dates: " + executionDates);
        assertTimestamps(executionDates, now, 5000, false);
    }

    @Test
    public void testNoTimestampFileAndShutdownBeforeFirstExecution() throws Exception
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/5 * * * * *");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        MaintenancePlugin plugin = new MaintenancePlugin(task, new MaintenanceTaskParameters(properties, "test"));

        // When
        // Simulate server start up
        plugin.start();
        // Simulate server shut down
        plugin.shutdown();
        // Wait until first scheduled run passed
        Thread.sleep(5500);
        Date start = new Date();
        // Simulate 2. server start up
        executionDates = new ArrayList<>();
        task = new TestMaintenanceTask(messageChannel, executionDates);
        plugin = new MaintenancePlugin(task, new MaintenanceTaskParameters(properties, "test"));
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
        }
        plugin.shutdown();

        // Then
        assertEquals(3, executionDates.size());
        System.out.println("Execution dates 2: " + executionDates);
        assertTimestamps(executionDates, start, 5000, false);
    }

    @Test
    public void testStopAfterExecutionAndRestartAfterMissed() throws Exception
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/5 * * * * *");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        MaintenancePlugin plugin = new MaintenancePlugin(task, new MaintenanceTaskParameters(properties, "test"));

        // When
        plugin.start();
        messageChannel.assertNextMessage("1. execution");
        Thread.sleep(500);
        // Stop after first execution
        plugin.shutdown();
        assertEquals(executionDates.size(), 1);
        Date firstExecutionDate = executionDates.get(0);
        // Wait until next scheduled run passed
        Thread.sleep(6000);
        Date start = new Date();
        System.out.println("1. execution date:" + firstExecutionDate + " " + start);
        // Simulate 2. server start up
        executionDates = new ArrayList<>();
        task = new TestMaintenanceTask(messageChannel, executionDates);
        plugin = new MaintenancePlugin(task, new MaintenanceTaskParameters(properties, "test"));
        plugin.start();
        List<Date> savedDates = new ArrayList<>();
        for (int i = 1; i <= 4; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
            Thread.sleep(200);
            savedDates.add(getSavedNext());
        }
        plugin.shutdown();

        // Then
        assertEquals(4, executionDates.size());
        System.out.println("Execution dates 3: " + executionDates);
        assertTimestamps(executionDates, start, 5000, false);
        System.out.println("Saved dates 3:" + savedDates);
        assertTimestamps(savedDates, start, 5000, true);
    }

    @Test
    public void testRetry() throws Exception
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates, 2);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/5 * * * * *");
        properties.setProperty(MaintenanceTaskParameters.RETRY_INTERVALS_AFTER_FAILURE_KEY, "1 sec, 1500 msec");
        MaintenancePlugin plugin = new MaintenancePlugin(task, new MaintenanceTaskParameters(properties, "test"));

        // When
        Date start = new Date();
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
            if (i == 1)
            {
                start = new Date();
            }
        }
        plugin.shutdown();

        // Then
        String prefix = "WARN  OPERATION.MaintenancePlugin - Execution of maintenance task '"
                + "ch.systemsx.cisd.common.maintenance.MaintenancePluginTest.TestMaintenanceTask' failed. ";
        assertEquals(prefix + "1. retry in 1000 msec.\n"
                + prefix + "2. retry in 1500 msec.", logRecorder.getLogContent());
        assertEquals(3, executionDates.size());
        System.out.println("Execution dates 4: " + executionDates);
        assertTimestamps(executionDates, start, 5000, false);
    }

    private void assertTimestamps(List<Date> dates, Date start, long period, boolean startOnRaster)
    {
        long t0 = (start.getTime() / period) * period;
        if (startOnRaster)
        {
            t0 += period;
        }
        for (int i = 0; i < dates.size(); i++)
        {
            long expectedTime = i == 0 && startOnRaster == false ? start.getTime() : t0 + i * period;
            if (Math.abs(expectedTime - dates.get(i).getTime()) > TOLERANCE)
            {
                fail((i + 1) + ". execution time stamp was " + dates.get(i).getTime() + ". Expected time stamp: "
                        + expectedTime + " +- " + TOLERANCE + ".");

            }
        }
    }

    private void assertTimestampDifference(List<Date> dates, int i, long expectedDifference)
    {
        long tim1 = dates.get(i - 1).getTime();
        long ti = dates.get(i).getTime();
        if (Math.abs(ti - tim1 - expectedDifference) > TOLERANCE)
        {
            fail("Unexpected execution time stamp difference between "
                    + "timestamp[" + (i - 1) + "] = " + tim1 + " and timestamp[" + i + "] = " + ti
                    + ". Expected " + expectedDifference + " +- " + TOLERANCE + ".");
        }
    }

    private Date getSavedNext() throws Exception
    {
        String savedNext = FileUtilities.loadToString(getPersistentNextDateFile());
        return new SimpleDateFormat(MaintenancePlugin.TIME_STAMP_FORMAT).parse(savedNext);
    }

    private File getPersistentNextDateFile()
    {
        return new File(workingDirectory, "nextDate.txt");
    }

    public static class TestMaintenanceTask implements IMaintenanceTask
    {
        private MessageChannel messageChannel;

        private List<Date> executionDates;

        private int counter;

        private int failureCount;

        TestMaintenanceTask(MessageChannel messageChannel, List<Date> executionDates)
        {
            this(messageChannel, executionDates, 0);
        }

        TestMaintenanceTask(MessageChannel messageChannel, List<Date> executionDates, int failureCount)
        {
            this.messageChannel = messageChannel;
            this.executionDates = executionDates;
            this.failureCount = failureCount;
        }

        @Override
        public void setUp(String pluginName, Properties properties)
        {
        }

        @Override
        public void execute()
        {
            if (failureCount > 0)
            {
                failureCount--;
                throw new RuntimeException("Failed");
            }
            executionDates.add(new Date());
            messageChannel.send(++counter + ". execution");
        }

    }
}
