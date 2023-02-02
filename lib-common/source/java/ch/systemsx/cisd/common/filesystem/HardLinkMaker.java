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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A class for creating hard links based on the Unix 'ln' program.
 * 
 * @author Bernd Rinn
 */
public class HardLinkMaker implements IFileImmutableCopier
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HardLinkMaker.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            HardLinkMaker.class);

    private static final String HARD_LINK_EXEC = "ln";

    private final String linkExecPath;

    private final TimingParameters timingParameters;

    //
    // Factory methods
    //

    /**
     * Creates copier trying to find the path to the <code>ln</code> executable.
     * 
     * @param timingParameters The timing parameters to use when monitoring the call to 'ln'.
     * @return <code>null</code> if the <code>ln</code> executable was not found.
     */
    public static final IFileImmutableCopier tryCreate(TimingParameters timingParameters)
    {
        final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
        {
            return null;
        }
        return create(lnExec, timingParameters);
    }

    /**
     * Creates copier trying to find the path to the <code>ln</code> executable. Uses default timing parameters.
     * 
     * @return <code>null</code> if the <code>ln</code> executable was not found.
     * @see TimingParameters#getDefaultParameters()
     */
    public static final IFileImmutableCopier tryCreate()
    {
        final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
        {
            return null;
        }
        return create(lnExec);
    }

    /**
     * Creates copier which is able to retry the operation of creating the hard link of a file if it does not complete after a specified timeout.
     * 
     * @param lnExec The executable of the 'ln' program.
     * @param timingParameters The timing parameters to use when monitoring the call to 'ln'.
     */
    public static final IFileImmutableCopier create(final File lnExec,
            final TimingParameters timingParameters)
    {
        return new HardLinkMaker(lnExec.getAbsolutePath(), timingParameters);
    }

    /**
     * Creates copier which is able to retry the operation of creating the hard link of a file if it does not complete after a specified timeout. Uses
     * default timing parameters.
     * 
     * @param lnExec The executable of the 'ln' program.
     * @see TimingParameters#getDefaultParameters()
     */
    public static final IFileImmutableCopier create(final File lnExec)
    {
        return new HardLinkMaker(lnExec.getAbsolutePath(), TimingParameters.getDefaultParameters());
    }

    private HardLinkMaker(final String linkExecPath, TimingParameters timingParameters)
    {
        this.linkExecPath = linkExecPath;
        this.timingParameters = timingParameters;
    }

    //
    // IFileImutableCopier
    //

    @Override
    public Status copyFileImmutably(final File source, final File destinationDirectory,
            final String nameOrNull)
    {
        return copyFileImmutably(source, destinationDirectory, nameOrNull, CopyModeExisting.ERROR);
    }

    @Override
    public Status copyFileImmutably(final File source, final File destinationDirectory,
            final String nameOrNull, final CopyModeExisting mode)
    {
        assert source.isFile() : String
                .format("Given file '%s' must be a file and is not.", source);
        final File destFile =
                new File(destinationDirectory, nameOrNull == null ? source.getName() : nameOrNull);
        if (destFile.exists())
        {
            switch (mode)
            {
                case OVERWRITE:
                    destFile.delete();
                    break;
                case IGNORE:
                    return Status.OK;
                default:
                    return Status.createError("File '" + destFile + "' already exists.");
            }
        }
        final List<String> cmd = createLnCmdLine(source, destFile);
        final Callable<Status> processTask = new Callable<Status>()
            {
                @Override
                public final Status call()
                {
                    final ProcessResult result =
                            ProcessExecutionHelper.run(cmd, operationLog, machineLog,
                                    timingParameters.getTimeoutMillis());
                    ProcessExecutionHelper.log(result);
                    // NOTE: we have noticed that in some environments sometimes the result is
                    // false although the file have been copied
                    if (result.isOK() == false && destFile.exists()
                            && checkIfIdenticalContent(source, destFile))
                    {
                        machineLog
                                .warn("Link creator reported failure, but the exact copy of the file '"
                                        + source.getPath()
                                        + "' seems to exist in '"
                                        + destFile.getPath() + "'. Error will be ignored.");
                        return Status.OK;
                    }
                    return result.toStatus();
                }
            };
        final Status ok =
                runRepeatableProcess(processTask, timingParameters.getMaxRetriesOnFailure(),
                        timingParameters.getIntervalToWaitAfterFailureMillis());
        if (ok.isError())
        {
            final String errorMsg = ok.tryGetErrorMessage();
            if (errorMsg != null && errorMsg.endsWith("Operation not supported"))
            {
                try
                {
                    FileCopyUtils.copy(source, destFile);
                    return Status.OK;
                } catch (IOException ex)
                {
                    return Status.createError(ex.getMessage());
                }
            }
        }
        return ok;
    }

    private static boolean checkIfIdenticalContent(final File file1, final File file2)
    {
        InterruptedExceptionUnchecked.check();
        try
        {
            return FileUtils.contentEquals(file1, file2);
        } catch (IOException e)
        {
            machineLog.warn("Error when comparing the content of a file and its link: "
                    + e.getMessage());
        }
        return false;
    }

    private final List<String> createLnCmdLine(final File srcFile, final File destFile)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }

    private static Status runRepeatableProcess(final Callable<Status> task,
            final int maxRetryOnFailure, final long millisToSleepOnFailure)
    {
        return new CallableExecutor(maxRetryOnFailure, millisToSleepOnFailure)
                .executeCallable(task);
    }
}
