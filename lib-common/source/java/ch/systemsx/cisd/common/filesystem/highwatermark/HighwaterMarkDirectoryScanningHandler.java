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

package ch.systemsx.cisd.common.filesystem.highwatermark;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.DirectoryScanningHandlerInterceptor;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.StoreItem;

/**
 * A <code>DirectoryScanningHandlerInterceptor</code> extension which bases its decision on the encapsulated {@link HighwaterMarkWatcher} and
 * {@link HostAwareFileWithHighwaterMark}s.
 * <p>
 * Note that the decision has precedence over encapsulated {@link IDirectoryScanningHandler} implementation and might short-circuit it.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HighwaterMarkDirectoryScanningHandler extends
        DirectoryScanningHandlerInterceptor
{
    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private final HostAwareFile[] files;

    public HighwaterMarkDirectoryScanningHandler(
            final IDirectoryScanningHandler directoryScanningHandler,
            final HighwaterMarkWatcher highwaterMarkWatcher, final File... files)
    {
        this(directoryScanningHandler, highwaterMarkWatcher, convertToHostAwareFiles(files));
    }

    public HighwaterMarkDirectoryScanningHandler(
            final IDirectoryScanningHandler directoryScanningHandler,
            final HighwaterMarkWatcher highwaterMarkWatcher, final HostAwareFile[] files)
    {
        super(directoryScanningHandler);
        assert directoryScanningHandler != null : "Unspecified IDirectoryScanningHandler";
        assert highwaterMarkWatcher != null : "Unspecified HighwaterMarkWatcher";
        assert files != null : "Unspecified files";
        this.highwaterMarkWatcher = highwaterMarkWatcher;
        this.files = files;
    }

    private static HostAwareFile[] convertToHostAwareFiles(final File... files)
    {
        HostAwareFile[] hostAwareFiles = new HostAwareFile[files.length];
        for (int i = 0; i < files.length; i++)
        {
            hostAwareFiles[i] = new HostAwareFile(files[i]);
        }
        return hostAwareFiles;
    }

    private final boolean mayHandle()
    {
        if (files.length < 1)
        {
            return isBelow() == false;
        }
        for (final HostAwareFile file : files)
        {
            if (isBelow(file))
            {
                return false;
            }
        }
        return true;
    }

    private final boolean isBelow()
    {
        // The path has probably been set before.
        highwaterMarkWatcher.run();
        return highwaterMarkWatcher.isBelow();
    }

    private final boolean isBelow(final HostAwareFile path)
    {
        highwaterMarkWatcher.setPathAndRun(path);
        return highwaterMarkWatcher.isBelow();
    }

    //
    // IDirectoryScanningHandler
    //

    @Override
    public HandleInstruction mayHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        return (mayHandle() == false) ? HandleInstruction.createError(
                "Not enough disk space on store '%s'.", scannedStore) : super.mayHandle(
                scannedStore, storeItem);
    }

}
