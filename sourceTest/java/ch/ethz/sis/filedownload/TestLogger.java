/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filedownload;

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

    private static final int TIMEOUT = 1000;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");

    private List<Entry> entries = new ArrayList<Entry>();

    @Override
    public boolean isEnabled(LogLevel level)
    {
        return level.ordinal() >= LogLevel.DEBUG.ordinal();
    }

    @Override
    public synchronized void log(Class<?> clazz, LogLevel level, String message)
    {
        log(clazz, level, message, null);
    }

    @Override
    public synchronized void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        Entry entry = new Entry(clazz, level, message, throwable);
        System.out.println(entry.toString());
        entries.add(entry);
        notifyAll();
    }

    public synchronized void awaitLogs(String... logs)
    {
        try
        {
            long timeoutTime = System.currentTimeMillis() + TIMEOUT;

            while (false == containsLogs(logs))
            {
                long timeLeft = timeoutTime - System.currentTimeMillis();

                if (timeLeft > 0)
                {
                    wait(timeLeft);
                } else
                {
                    break;
                }
            }

            if (false == containsLogs(logs))
            {
                StringBuilder sb = new StringBuilder();
                for (Entry entry : entries)
                {
                    sb.append(">>> " + entry.getLog() + "\n");
                }
                fail("Expected to contain logs: " + Arrays.toString(logs) + ", but the logs were:\n" + sb.toString());
            }

        } catch (Throwable e)
        {
            throw new RuntimeException(e);
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
