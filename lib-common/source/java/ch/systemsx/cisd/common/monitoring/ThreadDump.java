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

package ch.systemsx.cisd.common.monitoring;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.State;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Helper class which dumps all threads onto the console or a {@link PrintWriter}.
 *
 * @author Franz-Josef Elmer
 */
public class ThreadDump
{
    /**
     * Dumps all threads onto System.out.
     */
    public static void dumpAllThreads()
    {
        PrintWriter writer = new PrintWriter(System.out);
        dumpAllThreads(writer);
        writer.flush();
    }

    /**
     * Dumps all threads onto the specified logger as an INFO message.
     */
    public static void dumpAllThreads(Logger logger)
    {
        if (logger.isInfoEnabled() == false)
        {
            return;
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        dumpAllThreads(writer);
        writer.flush();
        logger.info("Thread dump:\n" + stringWriter.toString());
    }

    /**
     * Dumps all threads onto the specified writer.
     */
    public static void dumpAllThreads(PrintWriter writer)
    {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Collection<Entry<Thread, StackTraceElement[]>> allDumps = allStackTraces.entrySet();
        for (Entry<Thread, StackTraceElement[]> entry : allDumps)
        {
            Thread thread = entry.getKey();
            State state = thread.getState();
            writer.println(thread + ", state:" + State.valueOf(state.name()) + ", daemon:"
                    + thread.isDaemon() + ", alive:" + thread.isAlive() + ", interrupted:"
                    + thread.isInterrupted());
            StackTraceElement[] stackTraceElements = entry.getValue();
            for (StackTraceElement stackTraceElement : stackTraceElements)
            {
                writer.println("\tat " + stackTraceElement);
            }
            writer.println();
        }
    }
}
