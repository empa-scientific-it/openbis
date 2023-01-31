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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A file renamer that logs it's operations.
 * <p>
 * Renames and logs the file renaming process.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class FileRenamer
{
    private final static Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            FileRenamer.class);

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileRenamer.class);

    /**
     * Renames given <var>sourceFile</var> to given <var>destinationFile</var>.
     * <p>
     * Internally uses {@link FileOperations} and notifies the administrator if the process failed.
     * </p>
     */
    public static final boolean renameAndLog(final File sourceFile, final File destinationFile)
    {
        final String absoluteTargetPath = destinationFile.getAbsolutePath();
        if (destinationFile.exists())
        {
            notificationLog.error(String
                    .format("Destination file '%s' already exists. Won't overwrite it.",
                            absoluteTargetPath));
            return false;
        }
        boolean renamedOK =
                FileOperations.getMonitoredInstanceForCurrentThread().rename(sourceFile,
                        destinationFile);
        if (renamedOK)
        {
            if (operationLog.isInfoEnabled())
            {
                final String entity = sourceFile.isDirectory() ? "directory" : "file";
                final String name = sourceFile.getName();
                final String parent = sourceFile.getParent();
                final String path = destinationFile.getParent();
                operationLog.info(String.format("Moving %s '%s' from '%s' to '%s'.", entity, name,
                        parent, path));
            }
            return true;
        } else
        {
            notificationLog.error(String.format("Moving '%s' to '%s' failed, giving up.",
                    sourceFile.getAbsolutePath(), absoluteTargetPath));
            return false;
        }
    }

}
