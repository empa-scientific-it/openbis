/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem.ssh;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * Executor of commands on remote machine through SSH.
 * 
 * @author Piotr Buczek
 */
public class SshCommandExecutor implements ISshCommandExecutor, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SshCommandExecutor.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, SshCommandExecutor.class);

    private final ISshCommandBuilder sshCommandBuilder;

    private final String host;

    public SshCommandExecutor(File sshExecutable, final String host)
    {
        this(createSshCommandBuilder(sshExecutable), host);
    }

    public SshCommandExecutor(final ISshCommandBuilder sshCommandBuilder, final String host)
    {
        this.host = host;
        this.sshCommandBuilder = sshCommandBuilder;
    }

    private String getHost()
    {
        assert host != null : "host cannot be null";
        return host;
    }

    // -- common bash commands -------------

    // Creates bash command. The command returns 0 and its output is empty if the path is a readable
    // and writable directory
    private static String mkCheckDirectoryFullyAccessibleCommand(final String path)
    {
        // %1$s references always the first argument
        return String.format("if [ -d %1$s -a -w %1$s -a -r %1$s -a -x %1$s ]; then "
                + "exit 0; else echo false; fi", path);
    }

    // Creates bash command. The command returns 0 and its output is empty if the path is an
    // existing file or directory
    private static String mkCheckFileExistsCommand(final String path)
    {
        return String.format("if [ -e %s ]; then exit 0; else echo false; fi", path);
    }

    private boolean isSuccessfulCheck(final ProcessResult result)
    {
        return result.getOutput().size() == 0;
    }

    @Override
    public final BooleanStatus exists(final String pathString, final long timeOutMillis)
    {
        final String cmd = mkCheckFileExistsCommand(pathString);
        final ProcessResult result = executeCommandRemotely(cmd, timeOutMillis);
        if (result.isOK())
        {
            return BooleanStatus.createFromBoolean(isSuccessfulCheck(result));
        } else
        {
            return BooleanStatus.createError("Remote command '" + cmd
                    + "' failed with exit value: " + result.getExitValue());
        }
    }

    @Override
    public BooleanStatus checkDirectoryAccessible(final String pathString, final long timeOutMillis)
    {
        final String cmd = mkCheckDirectoryFullyAccessibleCommand(pathString);
        final ProcessResult result = executeCommandRemotely(cmd, timeOutMillis);
        String dirDesc = "'" + getHost() + ":" + pathString + "'";
        if (result.isOK())
        {
            if (isSuccessfulCheck(result))
            {
                return BooleanStatus.createTrue();
            } else
            {
                String msg =
                        "Directory not accessible: " + dirDesc
                                + ". Check that it exists and that you have read and write "
                                + "permissions to it.";
                if (result.getOutput().size() > 0
                        && "false".equals(result.getOutput().get(0)) == false)
                {
                    msg += " [check says: " + StringUtils.join(result.getOutput(), '\n') + "]";
                }
                return BooleanStatus.createFalse(msg);
            }
        } else
        {
            if (result.isTimedOut())
            {
                return BooleanStatus.createError("Error when checking if directory " + dirDesc
                        + " is accessible: TIMEOUT");
            } else
            {
                return BooleanStatus.createError("Error when checking if directory " + dirDesc
                        + " is accessible: " + result.getOutput());
            }
        }
    }

    @Override
    public ProcessResult executeCommandRemotely(final String localCmd, final long timeOutMillis)
    {
        return executeCommandRemotely(localCmd, timeOutMillis, true);
    }

    @Override
    public ProcessResult executeCommandRemotely(final String localCmd, final long timeOutMillis,
            final boolean logResult)
    {
        final List<String> cmdLine = sshCommandBuilder.createSshCommand(localCmd, getHost());
        final ProcessResult result =
                ProcessExecutionHelper.run(cmdLine, operationLog, machineLog, timeOutMillis,
                        ProcessIOStrategy.DEFAULT_IO_STRATEGY, false);
        if (logResult)
        {
            result.log();
        }
        return result;
    }

    //

    public static ISshCommandBuilder createSshCommandBuilder(final File sshExecutable)
    {
        return new ISshCommandBuilder()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public List<String> createSshCommand(final String cmd, final String host)
                {
                    return SshCommandExecutor.createSshCommand(cmd, sshExecutable, host);
                }
            };
    }

    private final static List<String> createSshCommand(final String command,
            final File sshExecutable, final String host)
    {
        final ArrayList<String> wrappedCmd = new ArrayList<String>();
        final List<String> sshCommand = Arrays.asList(sshExecutable.getPath(), "-T", host);
        wrappedCmd.addAll(sshCommand);
        wrappedCmd.add(command);
        return wrappedCmd;
    }

}
