/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.logging;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link ConditionalNotificationLogger} class.
 * 
 * @author Christian Ribeaud
 */
public final class ConditionalNotificationLoggerTest
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ConditionalNotificationLoggerTest.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, ConditionalNotificationLoggerTest.class);

    private final ConditionalNotificationLogger createConditionalNotificationLogger(
            final int ignoredErrorCountBeforeNotification)
    {
        final ConditionalNotificationLogger logger =
                new ConditionalNotificationLogger(operationLog, Level.WARN, notificationLog,
                        ignoredErrorCountBeforeNotification);
        return logger;
    }

    private BufferedAppender operationLogRecorder;

    private BufferedAppender notificationLogRecorder;

    private BufferedAppender notificationInfoLogRecorder;

    @BeforeMethod
    public final void setUp()
    {
        operationLogRecorder = new BufferedAppender("%m", Level.WARN);
        notificationLogRecorder = new BufferedAppender("%m", Level.ERROR);
        notificationInfoLogRecorder = new BufferedAppender("%m", Level.INFO);
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new ConditionalNotificationLogger(operationLog, null, 100);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        try
        {
            new ConditionalNotificationLogger(operationLog, notificationLog, -1);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @DataProvider(name = "ignoredErrorCount")
    public final Object[][] getIgnoredErrorCount()
    {
        return new Object[][]
        {
                { 0 },
                { 1 },
                { 2 }, };
    }

    @Test(dataProvider = "ignoredErrorCount")
    public final void testLogWithErrorLevel(final int ignoredErrorCountBeforeNotification)
    {
        final ConditionalNotificationLogger logger =
                createConditionalNotificationLogger(ignoredErrorCountBeforeNotification);
        final String logMessage = "Some message";
        for (int i = 0; i <= ignoredErrorCountBeforeNotification; i++)
        {
            logger.log(LogLevel.ERROR, logMessage);
            assertEquals(StringUtils.repeat(logMessage, i + 1), operationLogRecorder
                    .getLogContent());
            if (i >= ignoredErrorCountBeforeNotification)
            {
                assertEquals(logMessage, notificationLogRecorder.getLogContent());
            }
        }
        logger.log(null, logMessage);
        assertEquals(logMessage, notificationLogRecorder.getLogContent());
    }

    @Test
    public final void testLogWithInfoLevel()
    {
        final ConditionalNotificationLogger logger = createConditionalNotificationLogger(0);
        final String logMessage = "Some message";
        logger.log(LogLevel.INFO, logMessage);
        assertEquals(logMessage, operationLogRecorder.getLogContent());
        assertEquals("", notificationLogRecorder.getLogContent());
    }

    @Test
    public final void testLogWithNullLevel()
    {
        final ConditionalNotificationLogger logger = createConditionalNotificationLogger(0);
        final String logMessage = "Some message";
        logger.log(null, logMessage);
        assertEquals("", operationLogRecorder.getLogContent());
        assertEquals("", notificationLogRecorder.getLogContent());
    }

    @Test
    public final void testReset()
    {
        final ConditionalNotificationLogger logger = createConditionalNotificationLogger(2);
        final String logMessage = "Some message";
        final String okLogMessage = "Green again";
        logger.log(LogLevel.ERROR, logMessage);
        logger.log(LogLevel.ERROR, logMessage);
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals(logMessage, notificationLogRecorder.getLogContent());
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals(logMessage, notificationLogRecorder.getLogContent());
        notificationInfoLogRecorder.resetLogContent();
        logger.reset(okLogMessage);
        assertEquals(okLogMessage, notificationInfoLogRecorder.getLogContent());
        notificationLogRecorder.resetLogContent();
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals("", notificationLogRecorder.getLogContent());
    }

    @Test
    public final void testResetWithoutMessage()
    {
        final ConditionalNotificationLogger logger = createConditionalNotificationLogger(2);
        final String logMessage = "Some message";
        logger.log(LogLevel.ERROR, logMessage);
        logger.log(LogLevel.ERROR, logMessage);
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals(logMessage, notificationLogRecorder.getLogContent());
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals(logMessage, notificationLogRecorder.getLogContent());
        notificationInfoLogRecorder.resetLogContent();
        logger.reset(null);
        assertEquals("", notificationInfoLogRecorder.getLogContent());
        notificationLogRecorder.resetLogContent();
        logger.log(LogLevel.ERROR, logMessage);
        assertEquals("", notificationLogRecorder.getLogContent());
    }
}
