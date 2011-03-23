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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.StringUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor.FolderFileSizesReportGenerator;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier;

public final class RemoteDataSetFileOperationsExecutor implements IDataSetFileOperationsExecutor
{

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RemoteDataSetFileOperationsExecutor.class);

    private final ISshCommandExecutor executor;

    private final IPathCopier copier;

    private final String host;

    private final String rsyncModuleNameOrNull;

    private final String rsyncPasswordFileOrNull;

    private final File gfindExecutable;

    public RemoteDataSetFileOperationsExecutor(ISshCommandExecutor executor, IPathCopier copier,
            File gfindExecutable, String host, String rsyncModuleNameOrNull,
            String rsyncPasswordFileOrNull)
    {
        this.executor = executor;
        this.copier = copier;
        this.host = host;
        this.rsyncModuleNameOrNull = rsyncModuleNameOrNull;
        this.rsyncPasswordFileOrNull = rsyncPasswordFileOrNull;
        this.gfindExecutable = gfindExecutable;
    }

    public BooleanStatus exists(File file)
    {
        return executor.exists(file.getPath(), DataSetCopier.SSH_TIMEOUT_MILLIS);
    }

    public void deleteFolder(File folder)
    {
        ProcessResult result =
                executor.executeCommandRemotely("rm -rf " + folder.getPath(),
                        DataSetCopier.SSH_TIMEOUT_MILLIS);
        if (result.isOK() == false)
        {
            operationLog.error("Remote deletion of '" + folder + "' failed with exit value: "
                    + result.getExitValue());
            throw new ExceptionWithStatus(Status.createError("couldn't delete"));
        }
        List<String> output = result.getOutput();
        if (output.isEmpty() == false)
        {
            operationLog.error("Remote deletion of '" + folder
                    + "' seemed to be successful but produced following output:\n"
                    + StringUtilities.concatenateWithNewLine(output));
            throw new ExceptionWithStatus(Status.createError("deletion leads to a problem"));
        }
    }

    public void createFolder(File folder)
    {
        ProcessResult result =
                executor.executeCommandRemotely("mkdir -p " + folder.getPath(),
                        DataSetCopier.SSH_TIMEOUT_MILLIS);
        if (result.isOK() == false)
        {
            operationLog.error("Remote creation of '" + folder + "' failed with exit value: "
                    + result.getExitValue());
            throw new ExceptionWithStatus(
                    Status.createError("couldn't create destination directory"));
        }
        List<String> output = result.getOutput();
        if (output.isEmpty() == false)
        {
            operationLog.error("Remote creation of '" + folder
                    + "' seemed to be successful but produced following output:\n"
                    + StringUtilities.concatenateWithNewLine(output));
            throw new ExceptionWithStatus(
                    Status.createError("creation of destination directory leads to a problem"));
        }
    }

    public void copyDataSetToDestination(File dataSet, File destination)
    {
        Status result =
                copier.copyToRemote(dataSet, destination, host, rsyncModuleNameOrNull,
                        rsyncPasswordFileOrNull);
        if (result.isError())
        {
            throw new ExceptionWithStatus(result);
        }
    }

    public void retrieveDataSetFromDestination(File dataSet, File destination)
    {
        Status result =
                copier.copyFromRemote(destination, host, dataSet, rsyncModuleNameOrNull,
                        rsyncPasswordFileOrNull);
        if (result.isError())
        {
            throw new ExceptionWithStatus(result);
        }
    }

    public void renameTo(File newFile, File oldFile)
    {
        ProcessResult result =
                executor.executeCommandRemotely(
                        "mv " + oldFile.getPath() + " " + newFile.getPath(),
                        DataSetCopier.SSH_TIMEOUT_MILLIS);
        if (result.isOK() == false)
        {
            operationLog.error("Remote move of '" + oldFile + "' to '" + newFile
                    + "' failed with exit value: " + result.getExitValue());
            throw new ExceptionWithStatus(Status.createError("couldn't move"));
        }
        List<String> output = result.getOutput();
        if (output.isEmpty() == false)
        {
            operationLog.error("Remote move of '" + oldFile + "' to '" + newFile
                    + "' seemed to be successful but produced following output:\n"
                    + StringUtilities.concatenateWithNewLine(output));
            throw new ExceptionWithStatus(Status.createError("moving leads to a problem"));
        }
    }

    public void createMarkerFile(File markerFile)
    {
        ProcessResult result =
                executor.executeCommandRemotely("touch " + markerFile.getPath(),
                        DataSetCopier.SSH_TIMEOUT_MILLIS);
        if (result.isOK() == false)
        {
            operationLog.error("Creation of marker file '" + markerFile
                    + "' failed with exit value: " + result.getExitValue());
            throw new ExceptionWithStatus(Status.createError("creating a marker file failed"));
        }
        List<String> output = result.getOutput();
        if (output.isEmpty() == false)
        {
            operationLog.error("Creation of marker file '" + markerFile
                    + "' seemed to be successful but produced following output:\n"
                    + StringUtilities.concatenateWithNewLine(output));
            throw new ExceptionWithStatus(
                    Status.createError("creating a marker file leads to a problem"));
        }
    }

    public BooleanStatus checkSame(File dataSet, File destination)
    {
        if (false == dataSet.exists())
        {
            return BooleanStatus.createFalse("Data set location '" + dataSet + "' doesn't exist");
        }
        BooleanStatus existsStatus =
                executor.exists(destination.getPath(), DataSetCopier.SSH_TIMEOUT_MILLIS);
        if (false == existsStatus.isSuccess())
        {
            return existsStatus;
        }
        FileFilter nullFilter = null;
        List<File> storeFiles = FileUtilities.listFiles(dataSet, nullFilter, true);
        Map<String, Long> dataSetFileSizesByPaths =
                FolderFileSizesReportGenerator.extractSizesByPaths(storeFiles, dataSet);
        String cmd = createListFilesWithFileSizeCmd(destination.getPath(), gfindExecutable);
        ProcessResult result =
                executor.executeCommandRemotely(cmd, DataSetCopier.SSH_TIMEOUT_MILLIS);

        Map<String, Long> destinationFileSizesByPaths = new LinkedHashMap<String, Long>();
        if (result.isOK() && result.getOutput() != null)
        {
            List<String> output = result.getOutput();
            for (String line : output)
            {
                String split[] = line.split("\t");
                assert split.length == 2;
                destinationFileSizesByPaths.put(split[0], Long.parseLong(split[1]));
            }
        } else
        {
            String errorOutput = StringUtilities.concatenateWithNewLine(result.getErrorOutput());
            operationLog.error("Listing files in '" + destination + "' failed with exit value: "
                    + result.getExitValue() + "; error output: " + errorOutput);
            return BooleanStatus.createError("listing files failed");
        }

        String inconsistenciesReport =
                FolderFileSizesReportGenerator.findInconsistencies(dataSetFileSizesByPaths,
                        destinationFileSizesByPaths);
        if (StringUtils.isBlank(inconsistenciesReport))
        {
            return BooleanStatus.createTrue();
        } else
        {
            return BooleanStatus.createFalse("Inconsistencies:\n" + inconsistenciesReport);
        }
    }

    /**
     * Returns a bash command listing relative file paths of regular files with their sizes in
     * bytes.
     */
    private static String createListFilesWithFileSizeCmd(final String path, final File findExec)
    {
        return findExec + " " + path + " -type f -printf \"%p\\t%s\\n\"";
    }

}