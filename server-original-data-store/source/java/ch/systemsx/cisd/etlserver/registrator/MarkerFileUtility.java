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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.IStoreRootDirectoryHolder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class MarkerFileUtility
{
    private final Logger operationLog;

    private final Logger notificationLog;

    private final IFileOperations fileOperations;

    private final File storeRootDirectory;

    public MarkerFileUtility(Logger operationLog, Logger notificationLog,
            IFileOperations fileOperations, IStoreRootDirectoryHolder storeRootDirectoryHolder)
    {
        this(operationLog, notificationLog, fileOperations, storeRootDirectoryHolder
                .getStoreRootDirectory());
    }

    public MarkerFileUtility(Logger operationLog, Logger notificationLog,
            IFileOperations fileOperations, File storeRootDirectory)
    {
        this.operationLog = operationLog;
        this.notificationLog = notificationLog;
        this.fileOperations = fileOperations;
        this.storeRootDirectory = storeRootDirectory;
    }

    public static File getMarkerFileFromIncoming(final File incoming)
    {
        return new File(incoming.getParentFile(), FileConstants.IS_FINISHED_PREFIX + incoming.getName());
    }

    /**
     * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
     * 
     * @return <code>null</code> if a problem has happened. Otherwise a useful and usable incoming data set path is returned.
     */
    public final File getIncomingDataSetPathFromMarker(final File isFinishedPath)
    {
        final File incomingDataSetPath = getIncomingFromMarkerFile(isFinishedPath);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Getting incoming data set path '%s' from is-finished path '%s'",
                    incomingDataSetPath, isFinishedPath));
        }
        final String errorMsg =
                fileOperations.checkPathFullyAccessible(incomingDataSetPath, "incoming data set");
        if (errorMsg != null)
        {
            fileOperations.delete(isFinishedPath);
            throw EnvironmentFailureException.fromTemplate(String.format(
                    "Error moving path '%s' from '%s' to '%s': %s", incomingDataSetPath.getName(),
                    incomingDataSetPath.getParent(), storeRootDirectory, errorMsg));
        }
        return incomingDataSetPath;
    }

    static File getIncomingFromMarkerFile(final File isFinishedPath)
    {
        final File incomingDataSetPath =
                FileUtilities.removePrefixFromFileName(isFinishedPath, FileConstants.IS_FINISHED_PREFIX);
        return incomingDataSetPath;
    }

    public boolean deleteAndLogIsFinishedMarkerFile(File isFinishedFile)
    {
        if (fileOperations.exists(isFinishedFile) == false)
        {
            return false;
        }
        final boolean ok = fileOperations.delete(isFinishedFile);
        final String absolutePath = isFinishedFile.getAbsolutePath();
        if (ok == false)
        {
            notificationLog.error(String.format("Removing marker file '%s' failed.", absolutePath));
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String
                        .format("Marker file '%s' has been removed.", absolutePath));
            }
        }
        return ok;
    }
}
