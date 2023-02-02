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

package ch.systemsx.cisd.common.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.utilities.AddToListTextHandler;
import ch.systemsx.cisd.common.utilities.ITextHandler;

/**
 * Utility methods to execute a command from a command line and log all events.
 * 
 * @author Bernd Rinn
 */
public final class ProcessExecutionHelper
{
    /**
     * A good size for I/O buffers.
     */
    public static final int RECOMMENDED_BUFFER_SIZE = 4096;

    /**
     * Log the <var>result</var> and return the {@link ProcessResult#isOK()} flag.
     * 
     * @throws InterruptedExceptionUnchecked If the <var>result</var> has the {@link ProcessResult#isInterruped()} flag set.
     */
    public static final boolean log(ProcessResult result) throws InterruptedExceptionUnchecked
    {
        result.log();
        if (result.isInterruped())
        {
            throw new InterruptedExceptionUnchecked();
        }
        return result.isOK();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, null, false, ConcurrencyUtilities.NO_TIMEOUT, false,
                ProcessIOStrategy.DEFAULT_IO_STRATEGY, operationLog, machineLog, null, null)
                        .run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws InterruptedExceptionUnchecked
    {
        return run(cmd, operationLog, machineLog, true);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli-seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog. Use {@link ConcurrencyUtilities#NO_TIMEOUT} if you do not want any timeout.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion)
            throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, millisToWaitForCompletion, false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, null, false, millisToWaitForCompletion, false,
                ProcessIOStrategy.DEFAULT_IO_STRATEGY, operationLog, machineLog, null, null)
                        .run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param doNotTimeoutWhenIO If <code>true</code>, do not consider it a timeout as long as there is I/O activity on either <code>stdout</code> or
     *            <code>stderr</code>, i.e. <code>millisToWaitForCompletion</code> will be considered the time to wait for I/O rather than the time to
     *            wait for the process to complete.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion, boolean doNotTimeoutWhenIO,
            final boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, null, false, millisToWaitForCompletion, doNotTimeoutWhenIO,
                ProcessIOStrategy.DEFAULT_IO_STRATEGY, operationLog, machineLog, null, null)
                        .run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param ioStrategy The strategy to handle process I/O.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final ProcessIOStrategy ioStrategy, final boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, null, false, millisToWaitForCompletion, false,
                ioStrategy, operationLog, machineLog, null, null).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param doNotTimeoutWhenIO If <code>true</code>, do not consider it a timeout as long as there is I/O activity on either <code>stdout</code> or
     *            <code>stderr</code>, i.e. <code>millisToWaitForCompletion</code> will be considered the time to wait for I/O rather than the time to
     *            wait for the process to complete.
     * @param ioStrategy The strategy to handle process I/O.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion, boolean doNotTimeoutWhenIO,
            final ProcessIOStrategy ioStrategy, final boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, null, false, millisToWaitForCompletion, doNotTimeoutWhenIO, 
                ioStrategy, operationLog, machineLog, null, null).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param environment The environment of the process to start.
     * @param replaceEnvironment If <code>true</code>, the environment will be cleared before addinng the keys from <var>environment</var>, if it is
     *            <code>false</code>, the keys in <var>environment</var> will be added without clearing the environment before.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param ioStrategy The strategy to handle process I/O.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Map<String, String> environment,
            boolean replaceEnvironment, final Logger operationLog, final Logger machineLog,
            final long millisToWaitForCompletion, final ProcessIOStrategy ioStrategy,
            final boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, environment, replaceEnvironment, millisToWaitForCompletion,
                false, ioStrategy, operationLog, machineLog, null, null).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param environment The environment of the process to start.
     * @param replaceEnvironment If <code>true</code>, the environment will be cleared before addinng the keys from <var>environment</var>, if it is
     *            <code>false</code>, the keys in <var>environment</var> will be added without clearing the environment before.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param doNotTimeoutWhenIO If <code>true</code>, do not consider it a timeout as long as there is I/O activity on either <code>stdout</code> or
     *            <code>stderr</code>, i.e. <code>millisToWaitForCompletion</code> will be considered the time to wait for I/O rather than the time to
     *            wait for the process to complete.
     * @param ioStrategy The strategy to handle process I/O.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be thrown if the thread got interrupted, otherwise,
     *            the {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Map<String, String> environment,
            boolean replaceEnvironment, final Logger operationLog, final Logger machineLog,
            final long millisToWaitForCompletion, final boolean doNotTimeoutWhenIO,
            final ProcessIOStrategy ioStrategy, final boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutor(cmd, environment, replaceEnvironment, millisToWaitForCompletion, doNotTimeoutWhenIO,
                ioStrategy, operationLog, machineLog, null, null).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion)
            throws InterruptedExceptionUnchecked
    {
        return run(cmd, operationLog, machineLog, millisToWaitForCompletion, true);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli-seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param processIOStrategy The strategy of how to handle the process I/O.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final ProcessIOStrategy processIOStrategy) throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, millisToWaitForCompletion, processIOStrategy,
                false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param environment The environment of the process to start.
     * @param replaceEnvironment If <code>true</code>, the environment will be cleared before addinng the keys from <var>environment</var>, if it is
     *            <code>false</code>, the keys in <var>environment</var> will be added without clearing the environment before.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli-seconds. If the process is not finished after that time,
     *            it will be terminated by a watch dog.
     * @param processIOStrategy The strategy of how to handle the process I/O.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Map<String, String> environment,
            boolean replaceEnvironment, final Logger operationLog, final Logger machineLog,
            final long millisToWaitForCompletion, final ProcessIOStrategy processIOStrategy)
            throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, environment, replaceEnvironment, operationLog, machineLog,
                millisToWaitForCompletion, processIOStrategy, false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>. Does not block waiting for a result.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param processIOStrategy The strategy on how to deal with process I/O.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The handler which allows to wait for the result or terminate the process.
     */
    public static IProcessHandler runNonblocking(final List<String> cmd, Logger operationLog,
            final Logger machineLog, final ProcessIOStrategy processIOStrategy,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
    {
        return new ProcessExecutor(cmd, null, false, ConcurrencyUtilities.NO_TIMEOUT, false,
                processIOStrategy, operationLog, machineLog, stdoutHandlerOrNull, stderrHandlerOrNull).runUnblocking();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>. Does not block waiting for a result.
     * 
     * @param cmd The command line to run.
     * @param environment The environment of the process to start.
     * @param replaceEnvironment If <code>true</code>, the environment will be cleared before addinng the keys from <var>environment</var>, if it is
     *            <code>false</code>, the keys in <var>environment</var> will be added without clearing the environment before.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @param processIOStrategy The strategy on how to deal with process I/O.
     * @return The handler which allows to wait for the result or terminate the process.
     */
    public static IProcessHandler runNonblocking(final List<String> cmd,
            final Map<String, String> environment, final boolean replaceEnvironment,
            Logger operationLog, final Logger machineLog, final ProcessIOStrategy processIOStrategy,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
    {
        return new ProcessExecutor(cmd, environment, replaceEnvironment,
                ConcurrencyUtilities.NO_TIMEOUT, false, processIOStrategy, operationLog, 
                machineLog, stdoutHandlerOrNull, stderrHandlerOrNull)
                        .runUnblocking();
    }

    /**
     * Helper method for non-blocking reading text from a reader and add it to a list of strings, or, alternatively, discard it.
     * 
     * @param outputOrNull Must not be <code>null</code> if <var>discard</var> is <code>false</code> .
     * @return <code>true</code> if reader has read some text and <code>false</code> otherwise.
     */
    public static boolean readTextIfAvailable(final BufferedReader reader,
            final List<String> outputOrNull, final boolean discard) throws IOException
    {
        return readTextIfAvailable(reader, new AddToListTextHandler(outputOrNull), discard);
    }

    static boolean readTextIfAvailable(final BufferedReader reader,
            final ITextHandler textHandler, final boolean discard) throws IOException
    {
        boolean readText = false;
        while (reader.ready())
        {
            final String line = reader.readLine();
            if (line == null)
            {
                return readText;
            }
            readText = true;
            if (discard == false)
            {
                textHandler.handle(line);
            }
        }
        return readText;
    }

    /**
     * Helper method for non-blocking reading of bytes from an input stream, if available, and writing them to an output stream, or, alternatively,
     * discard them.
     * 
     * @param outputOrNull Must not be <code>null</code> if <var>discard</var> is <code>false</code> .
     * @return The number of bytes being read in this call.
     */
    public static long readBytesIfAvailable(final InputStream input,
            final OutputStream outputOrNull, final byte[] buffer, final long maxBytes,
            final boolean discard) throws IOException
    {
        long count = 0;
        int n = 0;
        while ((maxBytes <= 0 || count < maxBytes) && input.available() > 0
                && -1 != (n = input.read(buffer)))
        {
            if (discard == false)
            {
                outputOrNull.write(buffer, 0, n);
            }
            count += n;
        }
        return count;
    }

    /**
     * Returns the name of the command represented by <var>commandLine</var>.
     */
    final static String getCommandName(final List<String> commandLine)
    {
        return new File(commandLine.get(0)).getName();
    }

}
