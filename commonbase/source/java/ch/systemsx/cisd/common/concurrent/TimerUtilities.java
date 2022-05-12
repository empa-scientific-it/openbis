/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Utilities for {@link Timer}.
 * 
 * @author Bernd Rinn
 */
public class TimerUtilities
{
    static Thread tryGetTimerThread(String timerThreadName)
    {
        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            if (thread.getName().equals(timerThreadName))
            {
                return thread;
            }
        }
        return null;
    }

    /**
     * Tries to join the <var>thread</var> {@link Thread#join(long)}.
     * 
     * @param thread The {@link Thread} to join.
     * @param millis The time-out in milli-seconds to wait for the thread to die.
     * @return <code>true</code>, if the thread died in due time and <code>false</code> otherwise.
     */
    static boolean tryJoinThread(Thread thread, long millis)
    {
        try
        {
            thread.join(millis);
            return (thread.isAlive() == false);
        } catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Tries to interrupt the thread that the given <var>timer</var> uses for processing {@link TimerTask}s.
     * 
     * @return <code>true</code>, if the timer thread was successfully interrupted and <code>false</code> otherwise.
     */
    public static boolean tryInterruptTimerThread(Timer timer, String timerThreadName)
    {
        final Thread timerThreadOrNull = tryGetTimerThread(timerThreadName);
        if (timerThreadOrNull != null)
        {
            timerThreadOrNull.interrupt();
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * Tries to join the thread that <var>timer</var> is running its {@link TimerTask}s in (see {@link Thread#join(long)}.
     *
     * @param timerThreadName The name of the timer thread.
     * @param millis The time-out in milli-seconds to wait for the thread to die.
     * 
     * @return <code>true</code>, if the thread died in due time and <code>false</code> otherwise.
     */
    public static boolean tryJoinTimerThread(String timerThreadName, long millis)
    {
        final Thread timerThreadOrNull = tryGetTimerThread(timerThreadName);
        if (timerThreadOrNull != null)
        {
            return tryJoinThread(timerThreadOrNull, millis);
        }
        return false;
    }

    /**
     * Tries to shutdown the given <var>timer</var> by calling {@link Timer#cancel()}, interrupting the thread that it running its tasks and then
     * trying to join this thread.
     * 
     * @param timer The timer to shutdown.
     * @param millis The time-out in milli-seconds to wait for the thread to die. The total time-out of this method can be twice as high as the value
     *            specified.
     * @return <code>true</code>, if the thread died in due time and <code>false</code> otherwise.
     */
    public static boolean tryShutdownTimer(Timer timer, String timerThreadName, long millis)
    {
        Thread timerThread = tryGetTimerThread(timerThreadName);
        timer.cancel();
        if (timerThread == null)
        {
            return false;
        }
        timerThread.interrupt();
        final boolean joinOK = tryJoinThread(timerThread, millis);
        if (joinOK)
        {
            return true;
        }
        return false;
    }

}
