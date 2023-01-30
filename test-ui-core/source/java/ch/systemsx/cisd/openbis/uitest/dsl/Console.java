/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author anttil
 */
public class Console extends Writer
{
    private static long DEFAULT_TIMEOUT = 60000;

    private volatile LinkedBlockingQueue<String> queue;

    private volatile boolean buffering;

    private long startTime;

    private long timeout;

    private String error;

    public Console()
    {
        buffering = false;
        timeout = DEFAULT_TIMEOUT;
        this.error = null;
    }

    @Override
    public void close() throws IOException
    {
    }

    @Override
    public void flush() throws IOException
    {
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {

        char[] buf = new char[len];
        for (int i = off; i < off + len; i++)
        {
            buf[i - off] = cbuf[i];
        }

        if (buffering)
        {
            queue.offer(new String(buf));
        }
    }

    public void startBuffering()
    {
        if (buffering == false)
        {
            this.queue = new LinkedBlockingQueue<String>();
            this.buffering = true;
        }
        this.startTime = System.currentTimeMillis();
    }

    public void setTimeOut(long timeout)
    {
        this.timeout = timeout;
    }

    public void reset()
    {
        this.timeout = DEFAULT_TIMEOUT;
        this.error = null;
        this.buffering = false;
    }

    public void waitFor(String... text)
    {
        while (true)
        {
            String line = null;
            try
            {
                line = queue.poll(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
            if (line != null && containsAll(line, text))
            {
                break;
            }

            if (line != null && error != null && containsAll(line, new String[]
            { error }))
            {

                throw new CommandNotSuccessful("Failed: " + line);
            }

            if (System.currentTimeMillis() - startTime > timeout)
            {
                throw new RuntimeException(
                        "Timeout - could not find log line containing substrings: "
                                + Arrays.asList(text));
            }
        }
    }

    public boolean containsAll(String line, String[] text)
    {
        for (String token : text)
        {
            if (line.contains(token))
            {
                continue;
            }
            return false;
        }
        return true;
    }

    public void setError(String error)
    {
        this.error = error;
    }
}
