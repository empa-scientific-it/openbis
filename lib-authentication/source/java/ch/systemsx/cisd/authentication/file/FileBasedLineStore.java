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

package ch.systemsx.cisd.authentication.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An implementation of a {@line ILineStore} that is based on a file.
 * 
 * @author Bernd Rinn
 */
final class FileBasedLineStore implements ILineStore
{

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileBasedLineStore.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileBasedLineStore.class);

    private final File file;

    private final File oldFile;

    private final File newFile;

    private final String fileDescription;

    private long lastReadTimestamp;

    FileBasedLineStore(File file, String fileDescription)
    {
        this.file = file;
        this.oldFile = new File(file.getPath() + ".sv");
        this.newFile = new File(file.getPath() + ".tmp");
        this.fileDescription = fileDescription;
    }

    @Override
    public void check() throws ConfigurationFailureException
    {
        try
        {
            checkWritable();
        } catch (EnvironmentFailureException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
        if (file.canRead() == false)
        {
            final String msg =
                    String.format(file.exists() ? "%s '%s' is not readable."
                            : "%s '%s' does not exist.", fileDescription, file.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
    }

    private static void checkWritable(File file, String fileDescription)
            throws EnvironmentFailureException
    {
        if (file.exists() == false)
        {
            try
            {
                FileUtils.touch(file);
            } catch (IOException ex)
            {
                final String msg =
                        String.format("%s '%s' is not writable.", fileDescription, file
                                .getAbsolutePath());
                operationLog.error(msg);
                throw new EnvironmentFailureException(msg);
            }
        }
        if (file.canWrite() == false)
        {
            final String msg =
                    String.format("%s '%s' is not writable.", fileDescription, file
                            .getAbsolutePath());
            operationLog.error(msg);
            throw new EnvironmentFailureException(msg);
        }
    }

    @Override
    public String getId()
    {
        return file.getPath();
    }

    @SuppressWarnings("unchecked")
    private static List<String> primReadLines(File file) throws IOException
    {
        return FileUtils.readLines(file);
    }

    @Override
    public List<String> readLines() throws ConfigurationFailureException
    {
        if (file.exists() == false)
        {
            return new ArrayList<String>();
        }
        if (file.canRead() == false)
        {
            final String msg =
                    String.format(file.exists() ? "File '%s' cannot be read."
                            : "File '%s' does not exist.", file.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        try
        {
            final List<String> lines = primReadLines(file);
            lastReadTimestamp = file.lastModified();
            return lines;
        } catch (IOException ex)
        {
            final String msg =
                    String.format("Error when reading file '%s'.", file.getAbsolutePath());
            machineLog.error(msg, ex);
            throw new EnvironmentFailureException(msg, ex);
        }
    }

    private static void primWriteLines(File file, List<String> lines)
    {
        if (file.canWrite() == false)
        {
            final String msg =
                    String.format(file.exists() ? "File '%s' cannot be written."
                            : "File '%s' does not exist.", file.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        try
        {
            FileUtils.writeLines(file, lines);
        } catch (IOException ex)
        {
            final String msg =
                    String.format("Error when writing file '%s'.", file.getAbsolutePath());
            machineLog.error(msg, ex);
            throw new EnvironmentFailureException(msg, ex);
        }
    }

    private void checkWritable() throws EnvironmentFailureException
    {
        checkWritable(file, fileDescription);
        checkWritable(oldFile, fileDescription);
        checkWritable(newFile, fileDescription);
    }

    @Override
    public void writeLines(List<String> lines)
    {
        checkWritable();
        primWriteLines(newFile, lines);
        oldFile.delete();
        file.renameTo(oldFile);
        newFile.renameTo(file);
        lastReadTimestamp = file.lastModified();
    }

    @Override
    public boolean hasChanged()
    {
        return file.lastModified() != lastReadTimestamp;
    }

}
