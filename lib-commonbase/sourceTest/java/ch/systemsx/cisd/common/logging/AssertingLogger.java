/*
 * Copyright 2011 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A logger useful for testing.
 * 
 * @author Bernd Rinn
 */
public class AssertingLogger implements ISimpleLogger
{
    private final List<LogRecord> records = new ArrayList<LogRecord>();

    @Override
    public void log(final LogLevel level, final String message)
    {
        log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwableOrNull)
    {
        records.add(new LogRecord(level, message, throwableOrNull));
    }

    public void reset()
    {
        records.clear();
    }

    public void assertNumberOfMessage(final int expectedNumberOfMessages)
    {
        assertEquals(expectedNumberOfMessages, records.size());
    }

    public void assertEq(final int i, final LogLevel expectedLevel, final String expectedMessage)
    {
        assertEquals(expectedLevel, records.get(i).level);
        assertEquals(expectedMessage, records.get(i).message);
    }

    public void assertMatches(final int i, final LogLevel expectedLevel, final String pattern)
    {
        assertEquals(expectedLevel, records.get(i).level);
        final String message = records.get(i).message;
        final String assertError = String.format("Log message '%s' does not matches speficied pattern '%s'",
                message, pattern);
        assertTrue(assertError, message.matches(pattern));
    }

    public void assertThrowable(int i, LogLevel expectedLevel,
            Class<? extends Throwable> throwableClass, String throwableMessagePattern)
    {
        LogRecord record = records.get(i);
        assertEquals(expectedLevel, record.level);
        Throwable throwable = record.throwableOrNull;
        assertNotNull(throwable);
        assertEquals(throwableClass.getName(), throwable.getClass().getName());
        String message = throwable.getMessage();
        String assertError =
                String.format("Throwable message '%s' does not matches speficied pattern '%s'",
                        message, throwableMessagePattern);
        assertTrue(assertError, message.matches(throwableMessagePattern));
    }

    public int getNumberOfRecords()
    {
        return records.size();
    }

    public void print(PrintStream out)
    {
        for (LogRecord record : records)
        {
            out.println(record);
        }
    }

    private static class LogRecord
    {
        final LogLevel level;

        final String message;

        final Throwable throwableOrNull;

        LogRecord(final LogLevel level, final String message, Throwable throwableOrNull)
        {
            this.level = level;
            this.message = message;
            this.throwableOrNull = throwableOrNull;
        }

        @Override
        public String toString()
        {
            if (throwableOrNull != null)
            {
                return "LogRecord [level=" + level + ", message=" + message + ", throwable="
                        + throwableOrNull + "]";

            } else
            {
                return "LogRecord [level=" + level + ", message=" + message + "]";
            }
        }
    }
}