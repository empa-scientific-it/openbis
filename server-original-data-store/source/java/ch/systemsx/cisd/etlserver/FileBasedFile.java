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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;

/**
 * Adapter of {@link File}. Files are copies by creating hard links (if possible) if the parameter <var>hardLinkInsteadOfCopy</var> of the constructor
 * is set to <code>true</code>. Otherwise files are always copied.
 * 
 * @author Franz-Josef Elmer
 */
public class FileBasedFile implements IFile
{
    private final IImmutableCopier hardLinkCopierOrNull;

    private final File file;

    /**
     * Creates a new instance for the specified file with the specified copy policy.
     * 
     * @param file Real file wrapped by this adapter.
     * @param hardLinkCopierOrNull If specified, will be used instead of the normal file system copier for copying files and directories.
     */
    public FileBasedFile(final File file, final IImmutableCopier hardLinkCopierOrNull)
    {
        assert file != null : "Unspecified file.";
        this.file = file;
        this.hardLinkCopierOrNull = hardLinkCopierOrNull;
    }

    @Override
    public void copyFrom(final File sourceFile)
    {
        copy(sourceFile, file);
    }

    @Override
    public void copyTo(final File destinationFile)
    {
        copy(file, destinationFile);
    }

    private void copy(final File sourceFile, final File destinationFile)
    {
        if (sourceFile.isDirectory())
        {
            copyDirectory(sourceFile, destinationFile);
        } else
        {
            copyFile(sourceFile, destinationFile);
        }
    }

    private void copyFile(final File sourceFile, final File destinationFile)
    {
        if (hardLinkCopierOrNull != null)
        {
            final File destinationDirectory = destinationFile.getParentFile();
            final Status status =
                    hardLinkCopierOrNull.copyImmutably(sourceFile, destinationDirectory,
                            destinationFile.getName());
            if (status.isError())
            {
                throw new EnvironmentFailureException("Couldn't copy '"
                        + sourceFile.getAbsolutePath() + "' using hard links to '"
                        + destinationFile.getAbsolutePath()
                        + "': " + status);
            }
        } else
        {
            try
            {
                InterruptedExceptionUnchecked.check();
                FileUtils.copyFile(sourceFile, destinationFile, true);
            } catch (final IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

    private void copyDirectory(final File sourceDirectory, final File destinationDirectory)
    {
        if (hardLinkCopierOrNull != null)
        {
            final File destinationParentDirectory = destinationDirectory.getParentFile();
            final Status status =
                    hardLinkCopierOrNull.copyImmutably(sourceDirectory,
                            destinationParentDirectory, destinationDirectory.getName());
            if (status.isError())
            {
                throw new EnvironmentFailureException("Couldn't copy '"
                        + sourceDirectory.getAbsolutePath() + "' using hard links to '"
                        + destinationDirectory.getAbsolutePath()
                        + "': " + status);
            }
        } else
        {
            try
            {
                InterruptedExceptionUnchecked.check();
                FileUtils.copyDirectory(sourceDirectory, destinationDirectory, true);
            } catch (final IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

    @Override
    public void delete()
    {
        if (FileOperations.getMonitoredInstanceForCurrentThread().removeRecursivelyQueueing(file))
        {
            throw new EnvironmentFailureException("Can not delete file '"
                    + file.getAbsolutePath() + "'.");
        }
    }

    @Override
    public String getAbsolutePath()
    {
        return file.getAbsolutePath();
    }

    @Override
    public byte[] read()
    {
        try
        {
            return FileUtils.readFileToByteArray(file);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void write(final byte[] data)
    {
        try
        {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final String response = FileUtilities.checkDirectoryFullyAccessible(file, "");
        if (response != null)
        {
            throw new ConfigurationFailureException(response);
        }
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }
}
