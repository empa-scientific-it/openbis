/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor.FolderFileSizesReportGenerator;

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

    private final long timeoutInMillis;

    public RemoteDataSetFileOperationsExecutor(ISshCommandExecutor executor, IPathCopier copier,
            File gfindExecutable, String host, String rsyncModuleNameOrNull,
            String rsyncPasswordFileOrNull, long timeoutInMillis)
    {
        this.executor = executor;
        this.copier = copier;
        this.host = host;
        this.rsyncModuleNameOrNull = rsyncModuleNameOrNull;
        this.rsyncPasswordFileOrNull = rsyncPasswordFileOrNull;
        this.gfindExecutable = gfindExecutable;
        this.timeoutInMillis = timeoutInMillis;
    }

    @Override
    public BooleanStatus exists(File file)
    {
        return executor.exists(file.getPath(), timeoutInMillis);
    }

    @Override
    public void deleteFolder(File folder)
    {
        ProcessResult result =
                executor.executeCommandRemotely("rm -rf " + folder.getPath(), timeoutInMillis);
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

    @Override
    public void createFolder(File folder)
    {
        ProcessResult result =
                executor.executeCommandRemotely("mkdir -p " + folder.getPath(), timeoutInMillis);
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

    @Override
    public void copyDataSetToDestination(File dataSet, File destination)
    {
        Status result =
                copier.copyToRemote(dataSet, destination.getPath(), host, rsyncModuleNameOrNull,
                        rsyncPasswordFileOrNull, null, null);
        if (result.isError())
        {
            throw new ExceptionWithStatus(result);
        }
    }

    @Override
    public void syncDataSetWithDestination(File dataSet, File destination)
    {
        copyDataSetToDestination(dataSet, destination);
    }

    @Override
    public void retrieveDataSetFromDestination(File dataSet, File destination)
    {
        Status result =
                copier.copyFromRemote(destination.getPath(), host, dataSet, rsyncModuleNameOrNull,
                        rsyncPasswordFileOrNull, null, null);
        if (result.isError())
        {
            throw new ExceptionWithStatus(result);
        }
    }

    @Override
    public void renameTo(File newFile, File oldFile)
    {
        ProcessResult result =
                executor.executeCommandRemotely(
                        "mv " + oldFile.getPath() + " " + newFile.getPath(), timeoutInMillis);
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

    @Override
    public void createMarkerFile(File markerFile)
    {
        ProcessResult result =
                executor.executeCommandRemotely("touch " + markerFile.getPath(), timeoutInMillis);
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

    @Override
    public BooleanStatus checkSame(File dataSet, File destination)
    {
        if (false == dataSet.exists())
        {
            return BooleanStatus.createFalse("Data set location '" + dataSet + "' doesn't exist");
        }
        BooleanStatus existsStatus = executor.exists(destination.getPath(), timeoutInMillis);
        if (false == existsStatus.isSuccess())
        {
            return existsStatus;
        }
        FileFilter nullFilter = null;
        List<File> storeFiles = FileUtilities.listFiles(dataSet, nullFilter, true);
        Map<String, Long> dataSetFileSizesByPaths =
                FolderFileSizesReportGenerator.extractSizesByPaths(storeFiles, dataSet);
        String cmd = createListFilesWithFileSizeCmd(destination.getPath(), gfindExecutable);
        ProcessResult result = executor.executeCommandRemotely(cmd, timeoutInMillis);

        if (result.isOK() == false)
        {
            String errorOutput = StringUtilities.concatenateWithNewLine(result.getOutput());
            operationLog.error("Listing files in '" + destination + "' failed with exit value: "
                    + result.getExitValue() + "; error output: " + errorOutput);
            return BooleanStatus.createError("listing files failed");
        }
        Map<String, Long> destinationFileSizesByPaths =
                extractDestinationFileSizesByPaths(result.getOutput(), destination);

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

    @Override
    public long freeSpaceKb(String path)
    {
        throw new UnsupportedOperationException();
    }

    private Map<String, Long> extractDestinationFileSizesByPaths(List<String> output,
            File destination)
    {
        Map<String, Long> destinationFileSizesByPaths = new LinkedHashMap<String, Long>();
        for (String line : output)
        {
            String split[] = line.split("\t");
            if (split.length != 2)
            {
                throw new ExceptionWithStatus(Status.createError(String.format(
                        "Unexpected output from find in line: '%s'. "
                                + "Got %d tokens instead of 2.", line, split.length)));
            }
            String filePath = FileUtilities.getRelativeFilePath(destination, new File(split[0]));
            String fileSizeAsString = split[1];
            try
            {
                Long fileSize = Long.parseLong(fileSizeAsString);
                destinationFileSizesByPaths.put(filePath, fileSize);
            } catch (NumberFormatException ex)
            {
                throw new ExceptionWithStatus(Status.createError(String.format(
                        "Unexpected output from find in line: '%s'. "
                                + "Expected file size, got '%s'.", line, fileSizeAsString)));
            }
        }
        return destinationFileSizesByPaths;
    }

    /**
     * Returns a bash command listing relative file paths of regular files with their sizes in bytes.
     */
    private static String createListFilesWithFileSizeCmd(final String path, final File findExec)
    {
        return findExec + " " + path + " -type f -printf \"%p\\t%s\\n\"";
    }

}