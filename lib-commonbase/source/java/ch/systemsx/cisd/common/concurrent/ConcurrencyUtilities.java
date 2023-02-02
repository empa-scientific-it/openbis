/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * Concurrency related utility methods.
 * 
 * @author Bernd Rinn
 */
public final class ConcurrencyUtilities
{

    /** Corresponds to no timeout at all when waiting for the future. */
    public static final long NO_TIMEOUT = -1L;

    /** Corresponds to an immediate timeout when waiting for the future. */
    public static final long IMMEDIATE_TIMEOUT = 0L;

    /**
     * Provides settings for error logging.
     */
    public interface ILogSettings
    {
        /**
         * Returns the logger to be used for errors.
         */
        ISimpleLogger getLogger();

        /**
         * Returns the operation name to be used in error logging.
         */
        String getOperationName();

        /**
         * Returns the log level to be used in error logging.
         */
        LogLevel getLogLevelForError();

        /**
         * Returns the log level to be used in successful operation (return {@link LogLevel#OFF} to switch off logging for successful operation).
         */
        LogLevel getLogLevelForSuccess();
    }

    /**
     * A role for notifying the caller that a future will be cancelled <em>before</em> it is actually cancelled.
     */
    public interface ICancellationNotifier
    {
        /**
         * Called immediately before the future is cancelled.
         */
        void willCancel();
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. Any
     * {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @return The result of the future, or <code>null</code>, if the result does not become available within <var>timeoutMillis</var> ms.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis)
            throws InterruptedExceptionUnchecked
    {
        return tryGetResult(future, timeoutMillis, null, true);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. Any
     * {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @return The result of the future, or <code>null</code>, if the result does not become available within <var>timeoutMillis</var> ms.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis, boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        return tryGetResult(future, timeoutMillis, null, stopOnInterrupt);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. Any
     * {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown (wrapped in a {@link CheckedExceptionTunnel} if
     * necessary.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @param logSettingsOrNull The settings for error logging, or <code>null</code>, if error conditions should not be logged.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link InterruptedExceptionUnchecked} if the thread gets interrupted while waiting on the
     *            future.
     * @return The result of the future, or <code>null</code>, if the result does not become available within <var>timeoutMillis</var> ms or if the
     *         waiting thread gets interrupted.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis,
            ILogSettings logSettingsOrNull, boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        final ExecutionResult<T> result = getResult(future, timeoutMillis, logSettingsOrNull);
        return tryDealWithResult(result, stopOnInterrupt);
    }

    /**
     * Convenience wrapper for {@link #tryDealWithResult(ExecutionResult, boolean)} with <var>stopOnInterrupt</var> set to <code>true</code>.
     */
    public static <T> T tryDealWithResult(ExecutionResult<T> result)
    {
        return tryDealWithResult(result, true);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. Any
     * {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown (wrapped in a {@link CheckedExceptionTunnel} if
     * necessary.
     * <p>
     * This method is meant to be used if the an {@link ExecutionResult} should <i>mostly</i> be treated the way
     * {@link #tryGetResult(Future, long, boolean)} does it but not quite. Just deal with the deviant cases yourself, then call this method to deal
     * with the rest.
     * 
     * @param result A
     * @param stopOnInterrupt If <code>true</code>, throw a {@link InterruptedExceptionUnchecked} if the thread gets interrupted while waiting on the
     *            future.
     * @return The value of the <var>result</var> of the future, or <code>null</code>, if the result status is {@link ExecutionStatus#TIMED_OUT} or
     *         {@link ExecutionStatus#INTERRUPTED} and <var>stopOnInterrupt</var> is <code>false</code>.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     * @throws RuntimeException If the result status is {@link ExecutionStatus#EXCEPTION} and the exception is derived from {@link RuntimeException}.
     * @throws CheckedExceptionTunnel If the result status is {@link ExecutionStatus#EXCEPTION} and the exception is not derived from
     *             {@link RuntimeException}.
     */
    public static <T> T tryDealWithResult(ExecutionResult<T> result, boolean stopOnInterrupt)
    {
        switch (result.getStatus())
        {
            case COMPLETE:
            {
                return result.tryGetResult();
            }
            case EXCEPTION:
            {
                final Throwable cause = result.tryGetException();
                assert cause != null;
                throw CheckedExceptionTunnel.wrapIfNecessary(cause);
            }
            case INTERRUPTED:
            {
                if (stopOnInterrupt)
                {
                    throw new InterruptedExceptionUnchecked();
                } else
                {
                    return null;
                }
            }
            default:
            {
                return null;
            }
        }
    }

    /**
     * Returns the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. The return value is
     * never <code>null</code>, but always a {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis)
    {
        return getResult(future, timeoutMillis, null);
    }

    /**
     * Returns the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to become available. The return value is
     * never <code>null</code>, but always a {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @param logSettingsOrNull The settings for error logging, or <code>null</code>, if error conditions should not be logged.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis,
            ILogSettings logSettingsOrNull)
    {
        return getResult(future, timeoutMillis, true, logSettingsOrNull);
    }

    /**
     * Returns the result of a <var>future</var>, at most waiting <var>timeoutMillis</var> for the result to become available. The return value is
     * never <code>null</code>, but always a {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @param cancelOnTimeout If <code>true</code>, the <var>future</var> will be canceled on time-out.
     * @param logSettingsOrNull The settings for error logging, or <code>null</code>, if error conditions should not be logged.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis,
            boolean cancelOnTimeout, ILogSettings logSettingsOrNull)
    {
        return getResult(future, timeoutMillis, cancelOnTimeout, logSettingsOrNull, null, null);
    }

    private static boolean isActive(IActivitySensor sensorOrNull, long timeoutMillis)
    {
        return (sensorOrNull != null) && sensorOrNull.hasActivityMoreRecentThan(timeoutMillis);
    }

    /**
     * Returns the result of a <var>future</var>, at most waiting <var>timeoutMillis</var> for the result to become available. The return value is
     * never <code>null</code>, but always a {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If it is smaller than 0, no time-out will apply.
     * @param cancelOnTimeout If <code>true</code>, the <var>future</var> will be canceled on time-out.
     * @param logSettingsOrNull The settings for error logging, or <code>null</code>, if error conditions should not be logged.
     * @param cancellationNotifierOrNull The notifier for cancellation of the future or <code>null</code>
     * @param sensorOrNull A sensor that can prevent the method from timing out by showing activity.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis,
            boolean cancelOnTimeout, ILogSettings logSettingsOrNull,
            ICancellationNotifier cancellationNotifierOrNull, IActivitySensor sensorOrNull)
    {
        try
        {
            ExecutionResult<T> result = null;
            do
            {
                try
                {
                    result =
                            ExecutionResult.create(future.get(transform(timeoutMillis),
                                    TimeUnit.MILLISECONDS));
                } catch (TimeoutException ex)
                {
                    // result is still null
                }
            } while (result == null && isActive(sensorOrNull, timeoutMillis));
            if (result == null)
            {
                if (cancelOnTimeout)
                {
                    if (cancellationNotifierOrNull != null)
                    {
                        cancellationNotifierOrNull.willCancel();
                    }
                    future.cancel(true);
                }
                if (logSettingsOrNull != null)
                {
                    logSettingsOrNull.getLogger().log(
                            logSettingsOrNull.getLogLevelForError(),
                            String.format("%s: timeout of %.2f s exceeded%s.", logSettingsOrNull
                                    .getOperationName(), timeoutMillis / 1000f,
                                    cancelOnTimeout ? ", cancelled" : ""));
                }
                return ExecutionResult.createTimedOut();
            } else
            {
                if (logSettingsOrNull != null
                        && logSettingsOrNull.getLogLevelForSuccess() != LogLevel.OFF)
                {
                    logSettingsOrNull.getLogger().log(
                            logSettingsOrNull.getLogLevelForSuccess(),
                            String.format("%s: call returns %s.", logSettingsOrNull
                                    .getOperationName(), result.tryGetResult()));
                }
                return result;
            }
        } catch (InterruptedException ex)
        {
            if (cancellationNotifierOrNull != null)
            {
                cancellationNotifierOrNull.willCancel();
            }
            future.cancel(true);
            if (logSettingsOrNull != null)
            {
                logSettingsOrNull.getLogger().log(logSettingsOrNull.getLogLevelForError(),
                        String.format("%s: interrupted.", logSettingsOrNull.getOperationName()));
            }
            return ExecutionResult.createInterrupted();
        } catch (InterruptedExceptionUnchecked ex)
        {
            // Happens when Thread.stop(new StopException()) is called.
            if (cancellationNotifierOrNull != null)
            {
                cancellationNotifierOrNull.willCancel();
            }
            future.cancel(true);
            if (logSettingsOrNull != null)
            {
                logSettingsOrNull.getLogger().log(logSettingsOrNull.getLogLevelForError(),
                        String.format("%s: stopped.", logSettingsOrNull.getOperationName()));
            }
            return ExecutionResult.createInterrupted();
        } catch (ThreadDeath ex)
        {
            if (cancellationNotifierOrNull != null)
            {
                cancellationNotifierOrNull.willCancel();
            }
            future.cancel(true);
            if (logSettingsOrNull != null)
            {
                logSettingsOrNull.getLogger().log(logSettingsOrNull.getLogLevelForError(),
                        String.format("%s: stopped.", logSettingsOrNull.getOperationName()));
            }
            return ExecutionResult.createInterrupted();
        } catch (CancellationException ex)
        {
            if (logSettingsOrNull != null)
            {
                logSettingsOrNull.getLogger().log(logSettingsOrNull.getLogLevelForError(),
                        String.format("%s: cancelled.", logSettingsOrNull.getOperationName()));
            }
            return ExecutionResult.createInterrupted();
        } catch (ExecutionException ex)
        {
            final Throwable cause = ex.getCause();
            if (cause instanceof InterruptedExceptionUnchecked)
            {
                if (cancellationNotifierOrNull != null)
                {
                    cancellationNotifierOrNull.willCancel();
                }
                future.cancel(true);
                if (logSettingsOrNull != null)
                {
                    logSettingsOrNull.getLogger()
                            .log(
                                    logSettingsOrNull.getLogLevelForError(),
                                    String.format("%s: interrupted.", logSettingsOrNull
                                            .getOperationName()));
                }
                return ExecutionResult.createInterrupted();
            }
            if (logSettingsOrNull != null)
            {
                final String message =
                        (cause == null || cause.getMessage() == null) ? "<no message>" : cause
                                .getMessage();
                final String className =
                        (cause == null) ? "<unknown class>" : cause.getClass().getSimpleName();
                logSettingsOrNull.getLogger().log(
                        logSettingsOrNull.getLogLevelForError(),
                        String.format("%s: exception: %s [%s].", logSettingsOrNull
                                .getOperationName(), message, className));
            }
            return ExecutionResult.createExceptional(cause == null ? ex : cause);
        }
    }

    /**
     * Submits the <var>callable</var> to the <var>executor</var>.
     * 
     * @return A future which allows to terminate the task even when running.
     * @see ExecutorService#submit(Callable)
     */
    public static <V> ITerminableFuture<V> submit(ExecutorService executor,
            TerminableCallable<V> callable)
    {
        final Future<V> future = executor.submit(callable);
        return new TerminableFuture<V>(future, callable);
    }

    /**
     * Submits the <var>callableWithCleaner</var> to the <var>executor</var>.
     * 
     * @return A future which allows to terminate the task even when running.
     * @see ExecutorService#submit(Callable)
     */
    public static <V> ITerminableFuture<V> submit(ExecutorService executor,
            TerminableCallable.ICallableCleaner<V> callableWithCleaner)
    {
        return submit(executor, TerminableCallable.create(callableWithCleaner));
    }

    /**
     * Submits the <var>callable</var> to the <var>executor</var>.
     * 
     * @return A future which allows to terminate the task even when running.
     * @see ExecutorService#submit(Callable)
     */
    public static <V> ITerminableFuture<V> submit(ExecutorService executor,
            TerminableCallable.ICallable<V> callable)
    {
        return submit(executor, TerminableCallable.create(callable));
    }

    /**
     * Submits the <var>stoppableCallable</var> to the <var>executor</var>.
     * <p>
     * <strong>Note: Code executed in the <var>stoppableCallable</var> must <i>not</i> change variables or data structures used by several threads or
     * else the problems described in <a href="http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html">"Why is
     * <code>Thread.stop()</code> deprecated?"</a> apply to your code! Watch out for static thread-safe variables like e.g. the ones of type
     * {@link ThreadLocal}!</strong>
     * 
     * @return A future which allows to terminate the task even when running.
     * @see TerminableCallable#createStoppable(Callable)
     * @see ExecutorService#submit(Callable)
     */
    public static <V> ITerminableFuture<V> submitAsStoppable(ExecutorService executor,
            Callable<V> stoppableCallable)
    {
        return submit(executor, TerminableCallable.createStoppable(stoppableCallable));
    }

    /**
     * The same as {@link Thread#sleep(long)} but throws a {@link InterruptedExceptionUnchecked} on interruption rather than a
     * {@link InterruptedException}.
     */
    public static void sleep(long millis) throws InterruptedExceptionUnchecked
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

    /**
     * The same as {@link Thread#join()} but throws a {@link InterruptedExceptionUnchecked} on interruption rather than a {@link InterruptedException}
     * .
     */
    public static void join(Thread thread) throws InterruptedExceptionUnchecked
    {
        try
        {
            thread.join();
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

    /**
     * The same as {@link Thread#join(long)} but throws a {@link InterruptedExceptionUnchecked} on interruption rather than a
     * {@link InterruptedException}.
     */
    public static void join(Thread thread, long millis) throws InterruptedExceptionUnchecked
    {
        try
        {
            thread.join(millis);
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

    /**
     * The same as {@link Object#wait()} but throws a {@link InterruptedExceptionUnchecked} on interruption rather than a {@link InterruptedException}
     * .
     */
    public static void wait(Object obj) throws InterruptedExceptionUnchecked
    {
        try
        {
            obj.wait();
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

    /**
     * The same as {@link Object#wait(long)} but throws a {@link InterruptedExceptionUnchecked} on interruption rather than a
     * {@link InterruptedException}.
     */
    public static void wait(Object obj, long millis) throws InterruptedExceptionUnchecked
    {
        try
        {
            obj.wait(millis);
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

    private static long transform(long timeoutMillis)
    {
        return (timeoutMillis < 0) ? Long.MAX_VALUE : timeoutMillis;
    }
}
