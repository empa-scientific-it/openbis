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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that provides the framework for guarding a {@link Thread} such that it can be stopped safely. It will guard the current thread of whoever
 * runs {@link #startGuard()}.
 * <p>
 * A {@link ThreadGuard} instance can only guard a thread once, it can not be reset.
 * 
 * @author Bernd Rinn
 */
final class ThreadGuard
{
    private enum State
    {
        INITIAL, CANCELED, TERMINATING, RUNNING, FINISHING
    }

    /** The lock that guards stopping the thread. */
    private final Lock stopLock = new ReentrantLock();

    /** This latch signals that the task is finished. */
    private final CountDownLatch finishedLatch = new CountDownLatch(1);

    private Thread thread;

    private ThreadGuard.State state = State.INITIAL;

    private volatile boolean cancelled = false;

    private synchronized Thread tryInterruptAndGetThread()
    {
        if (state == State.RUNNING)
        {
            final Thread t = thread;
            thread = null;
            state = State.TERMINATING;
            t.interrupt();
            return t;
        } else
        {
            return null;
        }
    }

    // No need to synchronize these.

    /**
     * Call this method to ensure the thread can't be stopped.
     */
    void preventStopping()
    {
        stopLock.lock();
    }

    /**
     * Call this method to ensure the thread can be stopped.
     */
    void allowStopping()
    {
        stopLock.unlock();
    }

    /**
     * Mark the guard as being finished. Stopping must not be allowed when calling this method.
     */
    void markFinished()
    {
        finishedLatch.countDown();
        stopLock.unlock(); // Just in case we happen to hang in stop()
    }

    /**
     * Wait for the guard to be marked finished.
     * 
     * @return <code>true</code>, if the guard was marked finished in due time and <code>false</code>, if the wait timed out.
     * @throws InterruptedException If this thread got interrupted.
     */
    boolean waitForFinished(long timeoutMillis) throws InterruptedException
    {
        return finishedLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    // Whatever manipulates state or thread needs to be synchronized.

    /**
     * Start up the guard. The current thread when running this method will be guarded from now on. Implies {@link #preventStopping()}.
     * 
     * @return <code>true</code>, if the guard was successfully started and <code>false</code>, if the guard had been canceled before.
     */
    synchronized boolean startGuard()
    {
        if (state != State.INITIAL)
        {
            return false;
        }
        stopLock.lock();
        state = State.RUNNING;
        thread = Thread.currentThread();
        return true;
    }

    /**
     * Shut down the guard. Does not yet set it to finished.
     */
    synchronized void shutdownGuard()
    {
        state = State.FINISHING;
        thread = null;
        Thread.interrupted(); // Clear interrupted flag in case we are in mode TERMINATING.
    }

    /**
     * Returns <code>true</code>, if the guard has been started.
     */
    synchronized boolean hasStarted()
    {
        return (state != State.INITIAL && state != State.CANCELED);
    }

    /**
     * Returns <code>true</code> if the guard is in state <code>RUNNING</code>.
     */
    synchronized boolean isRunning()
    {
        return (state == State.RUNNING);
    }

    /**
     * Returns <code>true</code> if {@link #cancel(boolean)} has been called on the guard successfully, or when {@link #terminateAndWait(long, long)}
     * has been called on the guard.
     */
    boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Tries to cancel the guard, i.e. prevent it from running if it doesn't run yet. If canceling is successful, it implies marking the guard as
     * finished.
     * 
     * @param mayInterruptIfRunning If <code>true</code> and the guard is in state <code>RUNNING</code>, interrupt the thread. Otherwise, do nothing.
     * @return <code>true</code>, if the guard has been canceled successfully.
     */
    synchronized boolean cancel(boolean mayInterruptIfRunning)
    {
        if (state == State.INITIAL)
        {
            state = State.CANCELED;
            // Do not call markFinished() as the stopLock is not yet initialized.
            finishedLatch.countDown();
            cancelled = true;
        } else
        {
            cancelled = mayInterruptIfRunning ? (tryInterruptAndGetThread() != null) : false;
        }
        return cancelled;
    }

    /**
     * Tries to terminate task running in the thread. Note that this is a synchronous call that returns only when either the guard is marked finished
     * or when a timeout has occurred. Note also that even when providing <var>timeoutMillis</var> as 0, this method may wait up to
     * <var>waitInterruptMillis</var> milli-seconds for the {@link Thread#interrupt()} call to work.
     * <p>
     * The following steps are performed:
     * <ol>
     * <li>If the guard got canceled, return with cod<code>true</code>.</li>
     * <li>If the guard is already in state finishing, wait for the guard to be set to finished and return <code>true</code>, if that happens in due
     * time and <code>false</code> otherwise.</li>
     * <li>Call <code>Thread.terminate()</code> on the thread.</li>
     * <li>Wait for the guard to be marked finished in response to the call above (wait for <var>timeoutInterruptMillis</var> milli-seconds.</li>
     * <li>If the guard didn't get marked as finished, try to call <code>Thread.stop()</code> on the thread. This can only succeed, if stopping
     * becomes allowed in due time.</li>
     * <li>If either interrupt or stop took effect in due time, wait for guard to be marked as finished.</li>
     * <li>If the guard is marked finished in due time, return <code>true</code>, otherwise <code>false</code>.</li>
     * </ol>
     * 
     * @param waitInterruptMillis The time to wait for <code>interrupt()</code> to terminate the task.
     * @param timeoutMillis The method will wait for at most this time for the task to stop.
     * @return <code>true</code>, if the guard has been marked finished, or <code>false</code>, if a timeout has occurred.
     * @throws InterruptedException If the current thread is interrupted.
     */
    // Do not synchronize this or things will stop working!
    boolean terminateAndWait(long waitInterruptMillis, long timeoutMillis)
            throws InterruptedException
    {
        if (cancel(false))
        {
            return true;
        }
        final long start = System.currentTimeMillis();
        final Thread t = tryInterruptAndGetThread();
        if (t != null)
        {
            cancelled = true;
            if (waitForFinished(waitInterruptMillis))
            {
                return true;
            } else
            {
                return false;
            }
        }
        return waitForFinished(timeoutMillis - (System.currentTimeMillis() - start));
    }

}