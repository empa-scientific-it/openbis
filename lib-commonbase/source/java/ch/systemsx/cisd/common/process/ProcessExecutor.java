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

package ch.systemsx.cisd.common.process;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamedCallable;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.IActivitySensor;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.utilities.AddToListTextHandler;
import ch.systemsx.cisd.common.utilities.DelegatingTextHandler;
import ch.systemsx.cisd.common.utilities.ITextHandler;

/**
 * Utility class to execute a command from a command line and deal with possible process results and the process I/O.
 * 
 * @author Bernd Rinn
 */
class ProcessExecutor
{
    private final class ProcessOutput
    {
        private final List<String> processTextOutput;

        private final List<String> processErrorOutput;

        private final ByteArrayOutputStream processBinaryOutput;

        private final ITextHandler processErrorOutputHandler;

        private final ITextHandler processTextOutputHandler;

        ProcessOutput(ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            if (processIOStrategy.isBinaryOutput())
            {
                this.processTextOutput = null;
                processTextOutputHandler = null;
                this.processBinaryOutput = new ByteArrayOutputStream();
            } else
            {
                this.processTextOutput = new ArrayList<String>();
                processTextOutputHandler = new DelegatingTextHandler(new AddToListTextHandler(processTextOutput),
                        stdoutHandlerOrNull);
                this.processBinaryOutput = null;
            }
            processErrorOutput = new ArrayList<String>();
            processErrorOutputHandler = new DelegatingTextHandler(new AddToListTextHandler(processErrorOutput),
                    stderrHandlerOrNull);
        }

        ITextHandler getProcessTextOutputHandler()
        {
            return processTextOutputHandler;
        }

        ITextHandler getProcessErrorOutputHandler()
        {
            return processErrorOutputHandler;
        }

        List<String> getTextProcessOutput()
        {
            return processTextOutput;
        }

        ByteArrayOutputStream getBinaryProcessOutput()
        {
            return processBinaryOutput;
        }

        List<String> getErrorProcessOutput()
        {
            return processErrorOutput;
        }
    }

    /**
     * Record to store process and its output.
     */
    private final class ProcessRecord
    {
        private final Process process;

        private final Future<?> processIOFutureOrNull;

        private final AtomicBoolean processRunning;

        ProcessRecord(final Process process, final Future<?> processIOFutureOrNull,
                final AtomicBoolean processRunning)
        {
            this.process = process;
            this.processRunning = processRunning;
            this.processIOFutureOrNull = processIOFutureOrNull;
        }

        Future<?> tryGetProcessIOFuture()
        {
            return processIOFutureOrNull;
        }

        AtomicBoolean isProcessRunning()
        {
            return processRunning;
        }

        Process getProcess()
        {
            return process;
        }
    }

    /** Corresponds to a short timeout of 1/5 s. */
    private static final long SHORT_TIMEOUT = 200;

    /**
     * The fraction of the timeout that should be applied when reading the process output.
     */
    private static final double OUTPUT_READING_TIMEOUT_FRACTION = 0.1;

    /** The minimum timeout to wait for the process output to complete. */
    private static final long OUTPUT_READING_TIMEOUT_MILLIS = 250;

    /** Corresponds to a short timeout of 1/100 s. */
    private static final long PAUSE_MILLIS = 10;

    /** The executor service handling the threads that OS processes are spawned in. */
    private static final ExecutorService executor = new NamingThreadPoolExecutor("osproc")
            .corePoolSize(10).daemonize();

    /** The counter to draw the <var>processNumber</var> from. */
    private static final AtomicInteger processCounter = new AtomicInteger();

    private final Logger operationLog;

    private final Logger machineLog;

    /** Read-only! */
    private final List<String> commandLine;

    private final boolean replaceEnvironment;

    /** Read-only! */
    private final Map<String, String> environment;

    private final long millisToWaitForCompletion;

    private final boolean doNotTimeoutWhenIO;

    private final long millisToWaitForIOCompletion;

    private final ProcessIOStrategy processIOStrategy;

    private final ProcessOutput processOutput;

    private final IProcessIOHandler processIOHandlerOrNull;

    /** The number used in thread names to distinguish the process. */
    private final int processNumber;

    /** The name of the thread that the {@link ProcessExecutionHelper} is instantiated from. */
    private final String callingThreadName;

    // Use this reference to make sure the process is as dead as you can get it to be.
    private final AtomicReference<ProcessRecord> processWrapper;

    private final RecordingActivityObserverSensor activityObserverSensor;

    /**
     * Returns the command represented by <var>commandLine</var>.
     */
    private final static String getCommand(final List<String> commandLine)
    {
        return StringUtils.join(commandLine, ' ');
    }

    //
    // Implementation
    //

    private final int getExitValue(final Process process)
    {
        try
        {
            return process.exitValue();
        } catch (final IllegalThreadStateException ex)
        {
            return ProcessResult.NO_EXIT_VALUE;
        }
    }

    /**
     * Reads the <code>stdout</code> and <code>stderr</code> of <var>process</var>.
     */
    private final void readProcessOutput(final ProcessRecord processRecord, final byte[] buffer)
    {
        readProcessOutput(processRecord, buffer, -1);
    }

    /**
     * Reads the <code>stdout</code> and <code>stderr</code> of <var>process</var>. If <code>maxBytes > 0</code>, read not more than so many bytes.
     */
    private final void readProcessOutput(final ProcessRecord processRecord, final byte[] buffer,
            final long maxBytes)
    {
        assert processRecord != null;
        assert machineLog != null;

        final Process process = processRecord.getProcess();
        final List<String> errorOutput = processOutput.getErrorProcessOutput();
        final BufferedReader errorReader =
                new BufferedReader(new InputStreamReader(process.getErrorStream()));
        if (processIOStrategy.isBinaryOutput())
        {
            try
            {
                ProcessExecutionHelper.readBytesIfAvailable(process.getInputStream(),
                        processOutput.getBinaryProcessOutput(), buffer, maxBytes,
                        processIOStrategy.isDiscardStandardOutput());
            } catch (final IOException e)
            {
                machineLog.warn(String.format("IOException when reading stdout, msg='%s'.",
                        e.getMessage()));
            }
            readProcessOutputLines(errorOutput, errorReader,
                    processIOStrategy.isDiscardStandardError());
        } else
        {
            final List<String> stdoutLines = processOutput.getTextProcessOutput();
            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            readProcessOutputLines(stdoutLines, reader, processIOStrategy.isDiscardStandardOutput());
            readProcessOutputLines(errorOutput, errorReader,
                    processIOStrategy.isDiscardStandardError());
        }
    }

    /**
     * Returns the <code>stdout</code> and <code>stderr</code> of the <var>process</var> in <var>processRecord.getProcessOutput()</var>.
     */
    private final void readProcessOutputLines(final List<String> stdoutLines,
            final BufferedReader reader, final boolean discard)
    {
        assert stdoutLines != null;
        assert reader != null;
        assert machineLog != null;

        try
        {
            ProcessExecutionHelper.readTextIfAvailable(reader, stdoutLines, discard);
        } catch (final IOException e)
        {
            machineLog.warn(String.format("IOException when reading stdout/stderr, msg='%s'.",
                    e.getMessage()));
        }
    }

    /**
     * The class that performs the actual calling and interaction with the Operating System process. Since we observed hangs of several
     * process-related methods we call all of this in a separate thread.
     * <p>
     * Spawns a separate thread for the handling of the process I/O.
     */
    private class ProcessRunnerWithIOHandler implements NamedCallable<ProcessResult>
    {
        private final Process launch() throws IOException
        {
            final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            if (replaceEnvironment)
            {
                processBuilder.environment().clear();
            }
            if (environment != null)
            {
                processBuilder.environment().putAll(environment);
            }
            if (processIOStrategy.isMergeStderr())
            {
                processBuilder.redirectErrorStream(true);
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Running command: " + getCommand(commandLine)
                        + " (I/O in separate thread)");
            }
            return processBuilder.start();
        }

        //
        // NamedCallable
        //

        @Override
        public final ProcessResult call() throws Exception
        {
            try
            {
                final Process process = launch();
                int exitValue = ProcessResult.NO_EXIT_VALUE;
                final AtomicBoolean processRunning = new AtomicBoolean(true);
                final Future<?> ioFuture = executor.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                processIOHandlerOrNull.handle(processRunning,
                                        activityObserverSensor, process.getOutputStream(),
                                        process.getInputStream(), process.getErrorStream());
                            } catch (IOException ex)
                            {
                                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                            } finally
                            {
                                IOUtils.closeQuietly(process.getErrorStream());
                                IOUtils.closeQuietly(process.getInputStream());
                                IOUtils.closeQuietly(process.getOutputStream());
                            }
                        }
                    });
                final ProcessRecord processRecord =
                        new ProcessRecord(process, ioFuture, processRunning);

                processWrapper.set(processRecord);
                exitValue = process.waitFor();
                // Give the I/O handler time to finish up.
                final ExecutionResult<?> processIOResult =
                        getAndLogProcessIOResult(processRecord, millisToWaitForIOCompletion);
                processWrapper.set(null);

                if (processIOStrategy.isBinaryOutput())
                {
                    return new ProcessResult(commandLine, processNumber, ExecutionStatus.COMPLETE,
                            processIOResult, "", exitValue, processOutput.getBinaryProcessOutput()
                                    .toByteArray(),
                            processOutput.getErrorProcessOutput(),
                            operationLog, machineLog);
                } else
                {
                    return new ProcessResult(commandLine, processNumber, ExecutionStatus.COMPLETE,
                            processIOResult, "", exitValue, processOutput.getTextProcessOutput(),
                            processOutput.getErrorProcessOutput(), operationLog, machineLog);
                }
            } catch (final Exception ex)
            {
                machineLog
                        .error(String.format("Exception when launching: [%s, %s]", ex.getClass()
                                .getSimpleName(), StringUtils.defaultIfEmpty(ex.getMessage(),
                                        "NO MESSAGE")));
                throw ex;
            }
        }

        @Override
        public String getCallableName()
        {
            return callingThreadName + "::run-P" + processNumber + "-{"
                    + ProcessExecutionHelper.getCommandName(commandLine) + "}";
        }

    }

    /**
     * The class that performs the actual calling and interaction with the Operating System process. Since we observed hangs of several
     * process-related methods we call all of this in a separate thread.
     * <p>
     * Performs the process I/O in the same thread.
     */
    private class ProcessRunnerIOInSameThread implements NamedCallable<ProcessResult>
    {
        private final Process launch() throws IOException
        {
            final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            if (replaceEnvironment)
            {
                processBuilder.environment().clear();
            }
            if (environment != null)
            {
                processBuilder.environment().putAll(environment);
            }
            if (processIOStrategy.isMergeStderr())
            {
                processBuilder.redirectErrorStream(true);
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Running command: " + getCommand(commandLine)
                        + " (I/O in same thread)");
            }
            return processBuilder.start();
        }

        //
        // NamedCallable
        //

        @Override
        public final ProcessResult call() throws Exception
        {
            try
            {
                final Process process = launch();
                try
                {
                    int exitValue = ProcessResult.NO_EXIT_VALUE;
                    final ProcessRecord processRecord = new ProcessRecord(process, null, null);

                    processWrapper.set(processRecord);
                    final byte[] buffer = new byte[ProcessExecutionHelper.RECOMMENDED_BUFFER_SIZE];
                    while (exitValue == ProcessResult.NO_EXIT_VALUE)
                    {
                        readProcessOutput(processRecord, buffer);
                        exitValue = getExitValue(process);
                        if (exitValue == ProcessResult.NO_EXIT_VALUE)
                        {
                            ConcurrencyUtilities.sleep(PAUSE_MILLIS);
                        }
                    }
                    readProcessOutput(processRecord, buffer);
                    processWrapper.set(null);

                    if (processIOStrategy.isBinaryOutput())
                    {
                        return new ProcessResult(commandLine, processNumber,
                                ExecutionStatus.COMPLETE, ExecutionResult.create(null), "",
                                exitValue, processOutput.getBinaryProcessOutput().toByteArray(),
                                processOutput.getErrorProcessOutput(), operationLog, machineLog);
                    } else
                    {
                        return new ProcessResult(commandLine, processNumber,
                                ExecutionStatus.COMPLETE, ExecutionResult.create(null), "",
                                exitValue, processOutput.getTextProcessOutput(),
                                processOutput.getErrorProcessOutput(), operationLog, machineLog);
                    }
                } finally
                {
                    IOUtils.closeQuietly(process.getErrorStream());
                    IOUtils.closeQuietly(process.getInputStream());
                    IOUtils.closeQuietly(process.getOutputStream());
                }
            } catch (final Exception ex)
            {
                machineLog
                        .error(String.format("Exception when launching: [%s, %s]", ex.getClass()
                                .getSimpleName(), StringUtils.defaultIfEmpty(ex.getMessage(),
                                        "NO MESSAGE")));
                throw ex;
            }
        }

        @Override
        public String getCallableName()
        {
            return callingThreadName + "::run-P" + processNumber + "-{"
                    + ProcessExecutionHelper.getCommandName(commandLine) + "}";
        }

    }

    /**
     * The class that performs the destruction of a process that has timed-out. We do this in a separate thread because we have observed that,
     * depending on the operating system and Java version, processes can hang indefinitely on launching.
     */
    private final class ProcessKiller implements NamedCallable<ProcessResult>
    {
        private final ExecutionStatus status;

        private ProcessKiller(ExecutionStatus status)
        {
            this.status = status;
        }

        //
        // NamedCallable
        //

        @Override
        public final ProcessResult call()
        {
            final ProcessRecord processRecord = processWrapper.getAndSet(null);
            if (processRecord != null)
            {
                final ExecutionResult<?> processIOResultOrNull =
                        getAndLogProcessIOResult(processRecord, millisToWaitForIOCompletion);
                final Process process = processRecord.getProcess();
                process.destroy(); // Note: this also closes the I/O streams.
                if (machineLog.isInfoEnabled())
                {
                    machineLog.info(String.format("Killed '%s'.", getCommand(commandLine)));
                }
                final int exitValue = getExitValue(processRecord.getProcess());
                if (processIOStrategy.isBinaryOutput())
                {
                    return new ProcessResult(commandLine, processNumber, status,
                            processIOResultOrNull, "", exitValue, processOutput
                                    .getBinaryProcessOutput().toByteArray(),
                            processOutput.getErrorProcessOutput(), operationLog, machineLog);
                } else
                {
                    return new ProcessResult(commandLine, processNumber, status,
                            processIOResultOrNull, "", exitValue,
                            processOutput.getTextProcessOutput(),
                            processOutput.getErrorProcessOutput(), operationLog, machineLog);
                }
            } else
            {
                return null; // Value signals that the ProcessRunner got us.
            }
        }

        @Override
        public final String getCallableName()
        {
            return callingThreadName + "::kill-P" + processNumber + "-{"
                    + ProcessExecutionHelper.getCommandName(commandLine) + "}";
        }
    }

    /**
     * An {@link IProcessIOHandler} that reads bytes from stdout.
     */
    private class BinaryProcessIOHandler implements IProcessIOHandler
    {
        @Override
        public void handle(AtomicBoolean processRunning, IActivityObserver activityObserver, OutputStream stdin,
                InputStream stdout, InputStream stderr) throws IOException
        {
            final BufferedReader bufStderr = new BufferedReader(new InputStreamReader(stderr));
            final byte[] buf = new byte[ProcessExecutionHelper.RECOMMENDED_BUFFER_SIZE];
            ITextHandler processErrorOutputHandler = processOutput.getProcessErrorOutputHandler();
            while (processRunning.get())
            {
                final long stdOutRead = ProcessExecutionHelper.readBytesIfAvailable(stdout,
                        processOutput.processBinaryOutput, buf, -1,
                        processIOStrategy.isDiscardStandardOutput());
                final boolean stdErrRead = ProcessExecutionHelper.readTextIfAvailable(bufStderr,
                        processErrorOutputHandler,
                        processIOStrategy.isDiscardStandardError());
                if (stdOutRead > 0 || stdErrRead)
                {
                    activityObserver.update();
                }
                ConcurrencyUtilities.sleep(200);
            }
            ProcessExecutionHelper.readBytesIfAvailable(stdout, processOutput.processBinaryOutput,
                    buf, -1, processIOStrategy.isDiscardStandardOutput());
            ProcessExecutionHelper.readTextIfAvailable(bufStderr, processErrorOutputHandler,
                    processIOStrategy.isDiscardStandardError());
        }
    }

    /**
     * An {@link IProcessIOHandler} that reads text from stdout.
     */
    private class TextProcessIOHandler implements IProcessIOHandler
    {
        @Override
        public void handle(AtomicBoolean processRunning, IActivityObserver activityObserver, OutputStream stdin,
                InputStream stdout, InputStream stderr) throws IOException
        {
            final BufferedReader bufStdout = new BufferedReader(new InputStreamReader(stdout));
            final BufferedReader bufStderr = new BufferedReader(new InputStreamReader(stderr));
            ITextHandler processErrorOutputHandler = processOutput.getProcessErrorOutputHandler();
            ITextHandler processTextOutputHandler = processOutput.getProcessTextOutputHandler();
            while (processRunning.get())
            {
                final boolean stdOutRead = ProcessExecutionHelper.readTextIfAvailable(bufStdout,
                        processTextOutputHandler,
                        processIOStrategy.isDiscardStandardOutput());
                final boolean stdErrRead = ProcessExecutionHelper.readTextIfAvailable(bufStderr,
                        processErrorOutputHandler,
                        processIOStrategy.isDiscardStandardError());
                if (stdOutRead || stdErrRead)
                {
                    activityObserver.update();
                }
                ConcurrencyUtilities.sleep(200);
            }
            ProcessExecutionHelper.readTextIfAvailable(bufStdout, processTextOutputHandler,
                    processIOStrategy.isDiscardStandardOutput());
            ProcessExecutionHelper.readTextIfAvailable(bufStderr, processErrorOutputHandler,
                    processIOStrategy.isDiscardStandardError());
        }
    }

    ProcessExecutor(final List<String> commandLine, final Map<String, String> environment,
            final boolean replaceEnvironment, final long millisToWaitForCompletion,
            final boolean doNotTimeoutWhenIO, final ProcessIOStrategy ioStrategy,
            final Logger operationLog, final Logger machineLog,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
    {
        this.processNumber = processCounter.getAndIncrement();
        this.callingThreadName = Thread.currentThread().getName();
        this.operationLog = operationLog;
        this.machineLog = machineLog;
        // Backward compatibility. Do not remove this!!!
        if (millisToWaitForCompletion == ConcurrencyUtilities.IMMEDIATE_TIMEOUT)
        {
            this.millisToWaitForCompletion = ConcurrencyUtilities.NO_TIMEOUT;
        } else
        {
            this.millisToWaitForCompletion = millisToWaitForCompletion;
        }
        this.millisToWaitForIOCompletion =
                Math.round((millisToWaitForCompletion == ConcurrencyUtilities.NO_TIMEOUT) ? ConcurrencyUtilities.NO_TIMEOUT
                        : Math.max(OUTPUT_READING_TIMEOUT_MILLIS, this.millisToWaitForCompletion
                                * OUTPUT_READING_TIMEOUT_FRACTION));
        this.doNotTimeoutWhenIO = doNotTimeoutWhenIO;
        this.processIOStrategy = ioStrategy;
        this.commandLine = Collections.unmodifiableList(commandLine);
        this.environment = environment;
        this.replaceEnvironment = replaceEnvironment;
        this.processOutput = new ProcessOutput(stdoutHandlerOrNull, stderrHandlerOrNull);
        this.processWrapper = new AtomicReference<ProcessRecord>();
        if (ioStrategy.isUseNoIOHandler() == false && ioStrategy.tryGetCustomIOHandler() == null)
        {
            this.processIOHandlerOrNull =
                    processIOStrategy.isBinaryOutput() ? new BinaryProcessIOHandler()
                            : new TextProcessIOHandler();
        } else
        {
            this.processIOHandlerOrNull = ioStrategy.tryGetCustomIOHandler();
        }
        this.activityObserverSensor = new RecordingActivityObserverSensor();
    }

    final IProcessHandler runUnblocking()
    {
        final Future<ProcessResult> runnerFuture = launchProcessExecutor();
        return new IProcessHandler()
            {
                private final boolean stopOnInterruption = true;

                @Override
                public boolean terminate()
                {
                    ExecutionResult<ProcessResult> executionResult =
                            killProcess(ExecutionStatus.TIMED_OUT, stopOnInterruption);
                    return (executionResult.tryGetResult() != null);
                }

                @Override
                public ProcessResult getResult()
                {
                    return getProcessResult(stopOnInterruption, runnerFuture,
                            millisToWaitForCompletion, doNotTimeoutWhenIO);
                }

                @Override
                public ProcessResult getResult(final long millisToWaitForCompletion, boolean doNotTimeOutWhenIO)
                {
                    return getProcessResult(stopOnInterruption, runnerFuture,
                            millisToWaitForCompletion, doNotTimeOutWhenIO);
                }
            };
    }

    private ExecutionResult<?> getAndLogProcessIOResult(ProcessRecord record, long timeout)
    {
        if (processIOStrategy.isUseNoIOHandler())
        {
            return ExecutionResult.create(null);
        }
        if (record.tryGetProcessIOFuture() == null)
        {
            return ExecutionResult.createExceptional(new Error("Missing IO result."));
        }
        record.isProcessRunning().set(false);
        final ExecutionResult<?> processIOResult =
                ConcurrencyUtilities.getResult(record.tryGetProcessIOFuture(), timeout);
        logProcessIOFailures(processIOResult);
        return processIOResult;
    }

    private void logProcessIOFailures(final ExecutionResult<?> processIOResult)
    {
        switch (processIOResult.getStatus())
        {
            case COMPLETE:
                break;
            case EXCEPTION:
                final Throwable th = processIOResult.tryGetException();
                final Throwable cause =
                        (th == null) ? new RuntimeException("Unknown exception.")
                                : (th instanceof Error) ? (Error) th : CheckedExceptionTunnel
                                        .unwrapIfNecessary((Exception) th);
                machineLog.warn(String.format(
                        "Exception when doing process I/O, type='%s', msg='%s'.", cause.getClass()
                                .getSimpleName(),
                        cause.getMessage()));
                break;
            case INTERRUPTED:
                machineLog.warn("Interrupted when doing process I/O.");
                break;
            case TIMED_OUT:
                machineLog.warn("Timeout when doing process I/O.");
                break;
        }
    }

    final ProcessResult run(final boolean stopOnInterrupt)
    {
        final Future<ProcessResult> runnerFuture = launchProcessExecutor();
        return getProcessResult(stopOnInterrupt, runnerFuture, millisToWaitForCompletion, doNotTimeoutWhenIO);
    }

    private ProcessResult getProcessResult(final boolean stopOnInterrupt,
            final Future<ProcessResult> runnerFuture, final long millisToWaitForCompletionOverride, 
            final boolean doNotTimeOutOnIOOverride)
    {
        // when runUnblocking is used it is possible that we are hanging here while other thread
        // runs the killer. We will get COMPLETE status and null as the ProcessResult. We have to
        // change that status.
        ExecutionResult<ProcessResult> executionResult =
                getExecutionResult(runnerFuture, millisToWaitForCompletionOverride, 
                        doNotTimeOutOnIOOverride ? activityObserverSensor : null);
        if (executionResult.getStatus() != ExecutionStatus.COMPLETE)
        {
            executionResult = killProcess(executionResult.getStatus(), stopOnInterrupt);

            // If the process killer did not find anything to kill, then try to get the original
            // process result. We could have had a raise condition here.
            if (executionResult.tryGetResult() == null)
            {
                executionResult =
                        ConcurrencyUtilities.getResult(runnerFuture,
                                ConcurrencyUtilities.IMMEDIATE_TIMEOUT);
            }
        }
        checkStop(executionResult.getStatus(), stopOnInterrupt);
        final ProcessResult result = executionResult.tryGetResult();
        if (result != null)
        {
            return result;
        } else
        {
            ExecutionStatus status = executionResult.getStatus();
            if (status == ExecutionStatus.COMPLETE)
            {
                // see the note above about termination from other thread
                status = ExecutionStatus.INTERRUPTED;
            }
            if (processIOStrategy.isBinaryOutput())
            {
                return new ProcessResult(commandLine, processNumber, status, executionResult,
                        tryGetStartupFailureMessage(executionResult.tryGetException()),
                        ProcessResult.NO_EXIT_VALUE, (byte[]) null, null, operationLog, machineLog);
            } else
            {
                return new ProcessResult(commandLine, processNumber, status, executionResult,
                        tryGetStartupFailureMessage(executionResult.tryGetException()),
                        ProcessResult.NO_EXIT_VALUE, (List<String>) null, null, operationLog,
                        machineLog);
            }
        }
    }

    private ExecutionResult<ProcessResult> killProcess(ExecutionStatus executionStatus,
            boolean stopOnInterrupt)
    {
        final Future<ProcessResult> killerFuture =
                executor.submit(new ProcessKiller(executionStatus));
        checkStop(executionStatus, stopOnInterrupt);
        return ConcurrencyUtilities.getResult(killerFuture, SHORT_TIMEOUT);
    }

    private static ExecutionResult<ProcessResult> getExecutionResult(
            final Future<ProcessResult> runnerFuture, long millisToWaitForCompletion, IActivitySensor activitySensor)
    {
        return ConcurrencyUtilities.getResult(runnerFuture, millisToWaitForCompletion, false, null, null, activitySensor);
    }

    /**
     * Returns <code>true</code>, if an I/O handler for the process I/O is available.
     */
    private boolean hasIOHandler()
    {
        return processIOHandlerOrNull != null;
    }

    private Future<ProcessResult> launchProcessExecutor()
    {
        if (hasIOHandler())
        {
            return executor.submit(new ProcessRunnerWithIOHandler());
        } else
        {
            return executor.submit(new ProcessRunnerIOInSameThread());
        }
    }

    private final static void checkStop(ExecutionStatus executionStatus, boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        if (stopOnInterrupt && ExecutionStatus.INTERRUPTED.equals(executionStatus))
        {
            throw new InterruptedExceptionUnchecked();
        }
    }

    private final static String tryGetStartupFailureMessage(final Throwable throwableOrNull)
    {
        if (throwableOrNull != null && throwableOrNull instanceof IOException)
        {
            return throwableOrNull.getMessage();
        } else
        {
            return null;
        }
    }

}
