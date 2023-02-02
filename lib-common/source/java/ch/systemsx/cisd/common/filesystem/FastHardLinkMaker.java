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

import org.apache.commons.io.FileCopyUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A {@link IFileImmutableCopier} that uses a native method to create hard links.
 * 
 * @author Bernd Rinn
 */
public class FastHardLinkMaker implements IFileImmutableCopier
{

    private final static IFileImmutableCopier nativeCopier = new IFileImmutableCopier()
        {
            @Override
            public Status copyFileImmutably(File source, File destinationDirectory,
                    String nameOrNull)
            {
                return copyFileImmutably(source, destinationDirectory, nameOrNull,
                        CopyModeExisting.ERROR);
            }

            @Override
            public Status copyFileImmutably(File source, File destinationDirectory,
                    String nameOrNull, CopyModeExisting mode)
            {
                final File destination =
                        new File(destinationDirectory, (nameOrNull == null) ? source.getName()
                                : nameOrNull);
                if (destination.exists())
                {
                    switch (mode)
                    {
                        case OVERWRITE:
                            destination.delete();
                            break;
                        case IGNORE:
                            return Status.OK;
                        default:
                            return Status.createError("File '" + destination + "' already exists.");
                    }
                }
                try
                {
                    Unix.createHardLink(source.getAbsolutePath(), destination.getAbsolutePath());
                    return Status.OK;
                } catch (IOExceptionUnchecked ex)
                {
                    final String errorMsg = ex.getCause().getMessage();
                    if (errorMsg.endsWith("Operation not supported"))
                    {
                        try
                        {
                            FileCopyUtils.copyFile(source, destination);
                            return Status.OK;
                        } catch (IOException ex2)
                        {
                            return Status.createError(ex2.getMessage());
                        }
                    }
                    return Status.createError(errorMsg);
                }
            }
        };

    /**
     * Returns <code>true</code>, if the native library could be initialized successfully and thus this class is operational, or <code>false</code>
     * otherwise.
     */
    public final static boolean isOperational()
    {
        return Unix.isOperational();
    }

    /**
     * Creates an {@link IFileImmutableCopier}.
     * 
     * @param timingParameters The timing parameters used to monitor and potentially retry the hard link creation.
     * @return The copier, if the native library could be initialized successfully, or <code>null</code> otherwise.
     */
    public final static IFileImmutableCopier tryCreate(final TimingParameters timingParameters)
    {
        if (Unix.isOperational() == false)
        {
            return null;
        }
        return new FastHardLinkMaker(timingParameters);

    }

    /**
     * Creates an {@link IFileImmutableCopier} with default timing parameters (uses {@link TimingParameters#getDefaultParameters()}.
     * 
     * @return The copier, if the native library could be initialized successfully, or <code>null</code> otherwise.
     */
    public final static IFileImmutableCopier tryCreate()
    {
        return tryCreate(TimingParameters.getDefaultParameters());
    }

    private final IFileImmutableCopier monitoringProxy;

    private FastHardLinkMaker(final TimingParameters timingParameters)
    {
        monitoringProxy =
                MonitoringProxy.create(IFileImmutableCopier.class, nativeCopier)
                        .timing(timingParameters).get();
    }

    @Override
    public Status copyFileImmutably(final File source, final File destinationDirectory,
            final String nameOrNull)
    {
        return monitoringProxy.copyFileImmutably(source, destinationDirectory, nameOrNull);
    }

    @Override
    public Status copyFileImmutably(File source, File destinationDirectory, String nameOrNull,
            CopyModeExisting mode)
    {
        return monitoringProxy.copyFileImmutably(source, destinationDirectory, nameOrNull, mode);
    }

}
