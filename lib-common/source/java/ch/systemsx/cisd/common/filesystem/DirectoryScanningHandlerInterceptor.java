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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;

/**
 * An <code>IDirectoryScanningHandler</code> implementation which simply delegates the method calls to the encapsulated implementation.
 * 
 * @author Christian Ribeaud
 */
public class DirectoryScanningHandlerInterceptor implements IDirectoryScanningHandler
{
    private final IDirectoryScanningHandler directoryScanningHandler;

    protected DirectoryScanningHandlerInterceptor(
            final IDirectoryScanningHandler directoryScanningHandler)
    {
        assert directoryScanningHandler != null : "Unspecified IDirectoryScanningHandler implementation";
        this.directoryScanningHandler = directoryScanningHandler;
    }

    //
    // IDirectoryScanningHandler
    //

    @Override
    public void init(IScannedStore scannedStore)
    {
        directoryScanningHandler.init(scannedStore);
    }

    @Override
    public void beforeHandle(IScannedStore scannedStore)
    {
        directoryScanningHandler.beforeHandle(scannedStore);
    }

    @Override
    public HandleInstruction mayHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        return directoryScanningHandler.mayHandle(scannedStore, storeItem);
    }

    @Override
    public Status finishItemHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        return directoryScanningHandler.finishItemHandle(scannedStore, storeItem);
    }
}
