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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Class that keeps around the result of running an Operating System process.
 * <p>
 * Since the process output can only ever be read once from a process, it need to be kept around if it is needed more than once. This is what this
 * class is good for.
 */
public final class ProcessResult
{
    /**
     * The value indicating the process execution went OK.
     */
    public static final int EXIT_VALUE_OK = 0;

    /**
     * The value indicating that there is no exit value available for a process execution.
     */
    public static final int NO_EXIT_VALUE = -1;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by {@link Process#destroy()} on a MS Windows machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_WINDOWS = 1;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by {@link Process#destroy()} on a UNIX machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_UNIX = 143;

    private final ExecutionStatus status;

    private final String startupFailureMessage;

    private final int exitValue;

    private final List<String> commandLine;

    private final String commandName;

    private final int processNumber;

    private final Logger operationLog;

    private final Logger machineLog;

    private final boolean outputAvailable;

    private final List<String> output;

    private final byte[] binaryOutput;

    private final List<String> errorOutput;

    private final boolean isBinaryOutput;

    private final ExecutionResult<?> processIOResult;

    /**
     * Returns <code>true</code> if the <var>exitValue</var> indicates that the process has been terminated on the Operating System level.
     */
    public static boolean isProcessTerminated(final int exitValue)
    {
        if (OSUtilities.isWindows())
        {
            return exitValue == ProcessResult.EXIT_VALUE_FOR_TERMINATION_WINDOWS;
        } else
        {
            return exitValue == ProcessResult.EXIT_VALUE_FOR_TERMINATION_UNIX;
        }
    }

    public static boolean isProcessOK(final int exitValue)
    {
        return (exitValue == EXIT_VALUE_OK);
    }

    public ProcessResult(final List<String> commandLine, final int processNumber,
            final ExecutionStatus status, ExecutionResult<?> processIOResult,
            final String startupFailureMessageOrNull, final int exitValue,
            final List<String> processOutputOrNull, final List<String> processErrorOutputOrNull,
            final Logger operationLog, final Logger machineLog)
    {
        this.commandLine = commandLine;
        this.commandName = ProcessExecutionHelper.getCommandName(commandLine);
        this.processNumber = processNumber;
        this.status = status;
        this.processIOResult = processIOResult;
        this.startupFailureMessage =
                (startupFailureMessageOrNull == null) ? "" : startupFailureMessageOrNull;
        this.exitValue = exitValue;
        this.isBinaryOutput = false;
        this.outputAvailable = (processOutputOrNull != null);
        if (outputAvailable)
        {
            this.output = Collections.unmodifiableList(processOutputOrNull);

        } else
        {
            this.output = Collections.emptyList();

        }
        this.errorOutput = processErrorOutputOrNull;
        this.binaryOutput = null;
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    public ProcessResult(final List<String> commandLine, final int processNumber,
            final ExecutionStatus status, ExecutionResult<?> processIOResult,
            final String startupFailureMessageOrNull, final int exitValue,
            final byte[] processBinaryOutputOrNull, final List<String> processErrorOutputOrNull,
            final Logger operationLog, final Logger machineLog)
    {
        this.commandLine = commandLine;
        this.commandName = ProcessExecutionHelper.getCommandName(commandLine);
        this.processNumber = processNumber;
        this.status = status;
        this.processIOResult = processIOResult;
        this.startupFailureMessage =
                (startupFailureMessageOrNull == null) ? "" : startupFailureMessageOrNull;
        this.exitValue = exitValue;
        this.isBinaryOutput = true;
        this.outputAvailable = (processBinaryOutputOrNull != null);
        if (outputAvailable)
        {
            this.errorOutput =
                    (processErrorOutputOrNull == null) ? Collections.<String> emptyList()
                            : Collections.unmodifiableList(processErrorOutputOrNull);
            this.binaryOutput = processBinaryOutputOrNull;

        } else
        {
            this.errorOutput = Collections.emptyList();
            binaryOutput = new byte[0];

        }
        this.output = null;
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    /**
     * Returns the command line that belongs to this process.
     */
    public List<String> getCommandLine()
    {
        return commandLine;
    }

    /**
     * Returns the name of the command that belongs to this process.
     */
    public String getCommandName()
    {
        return commandName;
    }

    /**
     * Returns a number identifying the process that this result is for.
     */
    public int getProcessNumber()
    {
        return processNumber;
    }

    /**
     * Returns <code>true</code> if the output (<code>stdout</code> and <code>stderr</code>) is available (note that even if it available it may still
     * be empty).
     */
    public boolean isOutputAvailable()
    {
        return outputAvailable;
    }

    /**
     * Returns <code>true</code>, if the output of the method is binary and <code>false</code>, if it is text. If the output is binary, the error
     * output is available separately, otherwise it is merged with the regular output.
     * <p>
     * If this method returns <code>true</code>, you are supposed to call {@link #getBinaryOutput()} and {@link #getErrorOutput()}. If it is
     * <code>false</code>, you are supposed to call {@link #getOutput()}.
     */
    public boolean isBinaryOutput()
    {
        return isBinaryOutput;
    }

    /**
     * Returns the binary output of the process (<code>stdout</code>). If it not available (see {@link #isOutputAvailable()}, an empty array is
     * returned.
     * <p>
     * <i>Only call this method, if {@link #isBinaryOutput()} is <code>true</code>. Otherwise this method will return <code>null</code></i>.
     */
    public byte[] getBinaryOutput()
    {
        return binaryOutput;
    }

    /**
     * Returns the text error output of the process (<code>stderr</code>). If it not available (see {@link #isOutputAvailable()}, an empty array is
     * returned.
     * <p>
     * <i>Only call this method, if {@link #isBinaryOutput()} is <code>true</code>. Otherwise this method will return <code>null</code></i>.
     */
    public List<String> getErrorOutput()
    {
        return errorOutput;
    }

    /**
     * Returns this result as a {@link Status}.
     */
    public Status toStatus()
    {
        if (isOK())
        {
            return Status.OK;
        } else
        {
            if (isTimedOut())
            {
                return Status.createRetriableError("Process timed out");
            }
            if (isInterruped())
            {
                return Status.createRetriableError("Process got interrupted");
            }
            if (StringUtils.isBlank(getStartupFailureMessage()) == false)
            {
                return Status.createError(getStartupFailureMessage());
            }
            List<String> statusOutput = getErrorOutput();
            if (statusOutput.isEmpty())
            {
                statusOutput = getOutput();
            }
            return Status.createError((StringUtils.join(getCommandLine(), " ").trim() + "\n  "
                    + "Exit Value: " + getExitValue() + "\n  " + StringUtils.join(
                    statusOutput, "\n")).trim());
        }

    }

    /**
     * Returns the text output of the process (<code>stdout</code> and <code>stderr</code>). If it not available (see {@link #isOutputAvailable()}, an
     * empty list is returned.
     * <p>
     * <i>Only call this method, if {@link #isBinaryOutput()} is <code>false</code>. Otherwise this method will return <code>null</code></i>.
     */
    public List<String> getOutput()
    {
        return output;
    }

    /**
     * Returns the process' I/O result which tells whether there was an error condition on doing process <code>stdin / stdout / stderr</code> I/O.
     * <p>
     * <b>Note that {@link ExecutionResult#isOK()} does <i>not</i> necessarily mean that all output has been read from the process. Check
     * {@link ProcessResult#isOK()} instead which also considers the exit value of the process.</b>
     */
    public ExecutionResult<?> getProcessIOResult()
    {
        return processIOResult;
    }

    /**
     * Returns the exit value of the process, or {@link #NO_EXIT_VALUE}, if the value is not available.
     */
    public int getExitValue()
    {
        return exitValue;
    }

    /**
     * Returns the message that was given when the process failed to startup. If the process didn't fail on startup, an empty String is returned.
     */
    public String getStartupFailureMessage()
    {
        return startupFailureMessage;
    }

    /**
     * Returns <code>true</code>, if the process has completed successfully.
     */
    public boolean isOK()
    {
        return isProcessOK(exitValue) && processIOResult.isOK();
    }

    /**
     * Returns <code>true</code>, if the process has completed successfully. Do not consider the state of {@link #getProcessIOResult()} for the check.
     */
    public boolean isOKIgnoreIO()
    {
        return isProcessOK(exitValue);
    }

    /**
     * Returns <code>true</code> if the process has been run at all.
     */
    public boolean isRun()
    {
        return StringUtils.isBlank(startupFailureMessage);
    }

    /**
     * Returns <code>true</code> if the process has been terminated on the Operating System level.
     */
    public boolean isTerminated()
    {
        return ProcessResult.isProcessTerminated(getExitValue());
    }

    /**
     * Returns <code>true</code>, if the process has timed out on the Java level.
     */
    public boolean isTimedOut()
    {
        return ExecutionStatus.TIMED_OUT.equals(status);
    }

    /**
     * Returns <code>true</code>, if the Java thread that the process was running in got interrupted.
     */
    public boolean isInterruped()
    {
        return ExecutionStatus.INTERRUPTED.equals(status);
    }

    /**
     * Logs the outcome of the process execution.
     */
    public void log()
    {

        if (isOK() == false)
        {
            log(Level.WARN);
        } else if (operationLog.isDebugEnabled())
        {
            logProcessExitValue(Level.DEBUG);
            logProcessOutput(Level.DEBUG);
        }
    }

    /**
     * Logs command, exit value and outputs as INFO events.
     */
    public void logAsInfo()
    {
        log(Level.INFO);
    }

    private void log(Level level)
    {
        logCommandLine(level);
        logProcessExitValue(level);
        logProcessOutput(level);
    }

    private void logCommandLine(final Level logLevel)
    {
        operationLog.log(logLevel, String.format("P%d-{%s} had command line: %s", processNumber,
                commandName, getCommandLine()));
    }

    private void logProcessExitValue(final Level logLevel)
    {
        if (isRun() == false)
        {
            operationLog.log(logLevel, String.format("P%d-{%s} process has not started up: '%s'.",
                    processNumber, commandName, startupFailureMessage));
        } else if (isTimedOut())
        {
            operationLog.log(logLevel,
                    String.format("P%d-{%s} process has timed out.", processNumber, commandName));
        } else if (isInterruped())
        {
            operationLog.log(logLevel,
                    String.format("P%d-{%s} thread was interrupted.", processNumber, commandName));
        } else if (isTerminated())
        {
            operationLog.log(logLevel,
                    String.format("P%d-{%s} process was terminated.", processNumber, commandName));
        } else
        {
            operationLog.log(logLevel, String.format(
                    "P%d-{%s} process returned with exit value %d.", processNumber, commandName,
                    getExitValue()));
        }
    }

    private void logProcessOutput(final Level logLevel)
    {
        assert logLevel != null;

        if (isBinaryOutput)
        {
            if (getBinaryOutput().length != 0)
            {
                machineLog.log(logLevel, String.format("[%s] output: %d bytes", commandName,
                        getBinaryOutput().length));
            }
            final List<String> processErrorOutputLines = getErrorOutput();
            if (processErrorOutputLines.size() > 0)
            {
                machineLog.log(logLevel, String.format("[%s] error output:", commandName));
                for (final String ln : processErrorOutputLines)
                {
                    if (ln.trim().length() > 0)
                    {
                        machineLog.log(logLevel, String.format("\"%s\"", ln));
                    }
                }
            }

        } else
        {
            final List<String> processOutputLines = getOutput();
            if (processOutputLines.size() == 0)
            {
                return;
            }
            machineLog.log(logLevel, String.format("[%s] output:", commandName));
            for (final String ln : processOutputLines)
            {
                if (ln.trim().length() > 0)
                {
                    machineLog.log(logLevel, String.format("\"%s\"", ln));
                }
            }
        }
    }

}