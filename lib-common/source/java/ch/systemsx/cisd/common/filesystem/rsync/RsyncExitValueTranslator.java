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

package ch.systemsx.cisd.common.filesystem.rsync;

import ch.systemsx.cisd.common.exceptions.StatusFlag;

/**
 * A class that the return value of the <a href="http://rsync.samba.org">rsync</a> program into a meaningful status and message.
 * 
 * @author Bernd Rinn
 */
final class RsyncExitValueTranslator
{

    /**
     * Returns the error message of rsync as indicated by the <var>exitValue</var>. Must <i>not</i> be called for <code>exitValue=0</code>.
     */
    public static String getMessage(final int exitValue)
    {
        assert exitValue > 0;

        return String.format("rsync: %s (%d)", getRawMessage(exitValue), exitValue);
    }

    /**
     * Returns the raw error message of rsync as indicated by the <var>exitValue</var>. Must <i>not</i> be called for <code>exitValue=0</code>.
     */
    private static String getRawMessage(final int exitValue)
    {
        if (exitValue < 0)
        {
            throw new IllegalArgumentException("Exit value must be > 0 but is: " + exitValue);
        }

        switch (exitValue)
        {
            case 1:
                return "syntax or usage error";
            case 2:
                return "protocol incompatibility";
            case 3:
                return "errors selecting input/output files, dirs";
            case 4:
                return "requested action not supported";
            case 5:
                return "error starting client-server protocol";
            case 10:
                return "error in socket IO";
            case 11:
                return "error in file IO";
            case 12:
                return "error in rsync protocol data stream";
            case 13:
                return "errors with program diagnostics";
            case 14:
                return "error in IPC code";
            case 20:
                return "status returned when sent SIGUSR1, SIGINT";
            case 21:
                return "some error returned by waitpid()";
            case 22:
                return "error allocating core memory buffers";
            case 23:
                return "partial transfer";
            case 24:
                return "file vanished on sender";
            case 25:
                return "--max-delete stopped deletions";
            case 30:
                return "timeout in data send/receive";
            case 124:
                return "remote shell failed";
            case 125:
                return "remote shell killed";
            case 126:
                return "command could not be run";
            case 127:
                return "command not found";
            default:
                return "unknown error";
        }
    }

    /** Returns the {@link StatusFlag} of the rsync operation as indicated by the <var>exitValue</var>. */
    public static StatusFlag getStatus(final int exitValue)
    {
        if (exitValue < 0)
        {
            throw new IllegalArgumentException("Negative exit value: " + exitValue);
        }

        switch (exitValue)
        {
            case 0:
                return StatusFlag.OK;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 30:
            case 125:
            case 255:
                return StatusFlag.RETRIABLE_ERROR;
            default:
                return StatusFlag.ERROR;
        }
    }

}
