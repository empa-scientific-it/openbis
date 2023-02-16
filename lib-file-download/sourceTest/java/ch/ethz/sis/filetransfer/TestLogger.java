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
package ch.ethz.sis.filetransfer;

import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author pkupczyk
 */
public class TestLogger implements ILogger
{

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");

    private static final LogLevel LOG_LEVEL = LogLevel.INFO;

    private List<Entry> entries = new ArrayList<Entry>();

    @Override
    public boolean isEnabled(LogLevel level)
    {
        return level.ordinal() >= LOG_LEVEL.ordinal();
    }

    @Override
    public synchronized void log(Class<?> clazz, LogLevel level, String message)
    {
        log(clazz, level, message, null);
    }

    @Override
    public synchronized void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        if (isEnabled(level))
        {
            Entry entry = new Entry(clazz, level, message, throwable);
            System.out.println(entry.toString());
            entries.add(entry);
            notifyAll();
        }
    }

    public synchronized void awaitLogs(int timeout, String... logs)
    {
        try
        {
            long timeoutTime = System.currentTimeMillis() + timeout;

            while (true)
            {
                if (containsLogs(logs))
                {
                    return;
                } else
                {
                    long timeLeft = timeoutTime - System.currentTimeMillis();

                    if (timeLeft > 0)
                    {
                        wait(timeLeft);
                    } else
                    {
                        StringBuilder sb = new StringBuilder();
                        for (Entry entry : entries)
                        {
                            sb.append(">>> " + entry.getLog() + "\n");
                        }
                        fail("Expected to contain logs: " + Arrays.toString(logs) + ", but the logs were:\n" + sb.toString());
                    }
                }
            }

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public synchronized void assertLogsWithLevels(LogLevel... logLevels)
    {
        for (LogLevel logLevel : logLevels)
        {
            boolean found = false;

            for (Entry entry : entries)
            {
                if (logLevel.equals(entry.getLevel()))
                {
                    found = true;
                    break;
                }
            }

            if (false == found)
            {
                fail("Expected to find logs with level: " + logLevel);
            }
        }
    }

    public synchronized void assertNoLogsWithLevels(LogLevel... logLevels)
    {
        for (LogLevel logLevel : logLevels)
        {
            for (Entry entry : entries)
            {
                if (logLevel.equals(entry.getLevel()))
                {
                    fail("Unexpected log found with level: " + logLevel);
                }
            }
        }
    }

    private boolean containsLogs(String... logs)
    {
        int lastEntryIndex = -1;

        for (int l = 0; l < logs.length; l++)
        {
            boolean found = false;

            for (int e = lastEntryIndex + 1; e < entries.size(); e++)
            {
                Entry entry = entries.get(e);
                if (entry.getLog().contains(logs[l]))
                {
                    found = true;
                    lastEntryIndex = e;
                    break;
                }
            }

            if (false == found)
            {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    private class Entry
    {

        private Class<?> clazz;

        private LogLevel level;

        private String message;

        private Throwable throwable;

        private String log;

        public Entry(Class<?> clazz, LogLevel level, String message, Throwable throwable)
        {
            this.clazz = clazz;
            this.level = level;
            this.message = message;
            this.throwable = throwable;

            StringBuilder sb = new StringBuilder();

            sb.append("(" + DATE_FORMAT.format(new Date()) + ") ");
            sb.append(level);
            sb.append(" [" + Thread.currentThread().getName() + "] ");
            sb.append(clazz.getSimpleName());
            sb.append(" - ");
            sb.append(message);

            if (throwable != null)
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(os, true);
                throwable.printStackTrace(ps);
                ps.close();
                sb.append("\n" + new String(os.toByteArray()));
            }

            this.log = sb.toString();
        }

        public Class<?> getClazz()
        {
            return clazz;
        }

        public LogLevel getLevel()
        {
            return level;
        }

        public String getMessage()
        {
            return message;
        }

        public Throwable getThrowable()
        {
            return throwable;
        }

        public String getLog()
        {
            return log;
        }

        @Override
        public String toString()
        {
            return log;
        }

    }

}
