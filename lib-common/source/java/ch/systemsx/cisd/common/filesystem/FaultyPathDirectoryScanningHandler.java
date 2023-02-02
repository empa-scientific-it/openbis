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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.common.io.CollectionIO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An <code>IDirectoryScanningHandler</code> which manages faulty paths.
 * <p>
 * A faulty path is a {@link StoreItem} which was <i>not handled</i> (still exists in the {@link IScannedStore}).
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class FaultyPathDirectoryScanningHandler implements IDirectoryScanningHandler
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FaultyPathDirectoryScanningHandler.class);

    private final Set<StoreItem> faultyPaths;

    private final File faultyPathsFile;

    private long faultyPathsLastChanged;

    private IStopSignaler stopSignaler;

    public static interface IFaultyPathDirectoryScanningHandlerDelegate
    {
        /**
         * return true if the given path should NOT end in faulty paths
         */
        boolean shouldNotAddToFaultyPathsOrNull(File file);
    }

    private final IFaultyPathDirectoryScanningHandlerDelegate delegate;

    public FaultyPathDirectoryScanningHandler(final File faultyPathDirectory,
            final IStopSignaler stopSignaler)
    {
        this(faultyPathDirectory, stopSignaler, null);
    }

    public FaultyPathDirectoryScanningHandler(final File faultyPathDirectory,
            final IStopSignaler stopSignaler, IFaultyPathDirectoryScanningHandlerDelegate delegate)
    {
        this.faultyPaths = new HashSet<StoreItem>();
        this.faultyPathsFile = new File(faultyPathDirectory, FileConstants.FAULTY_PATH_FILENAME);
        this.stopSignaler = stopSignaler;

        this.delegate = delegate;
    }

    private Set<String> faultyPathsAsStrings(IScannedStore scannedStore)
    {
        final Set<String> faultyPathStrings = new HashSet<String>(faultyPaths.size());
        for (StoreItem item : faultyPaths)
        {
            faultyPathStrings.add(scannedStore.getLocationDescription(item));
        }
        return faultyPathStrings;
    }

    private void setFaultyPathsFromStrings(IScannedStore scannedStore, Set<String> faultyPathStrings)
    {
        faultyPaths.clear();
        for (String faultyPath : faultyPathStrings)
        {
            faultyPaths.add(scannedStore.asStoreItem(faultyPath));
        }
    }

    private final void checkForFaultyPathsFileChanged(IScannedStore scannedStore)
    {
        if (faultyPathsFile.exists())
        {
            // Handles manual manipulation.
            if (faultyPathsFile.lastModified() > faultyPathsLastChanged)
            {
                final Set<String> faultyPathStrings = new HashSet<String>();
                CollectionIO.readCollection(faultyPathsFile, faultyPathStrings);
                final Set<StoreItem> faultyPathsCopy =
                        operationLog.isInfoEnabled() ? new HashSet<StoreItem>(faultyPaths) : null;
                setFaultyPathsFromStrings(scannedStore, faultyPathStrings);
                faultyPathsLastChanged = faultyPathsFile.lastModified();
                if (operationLog.isInfoEnabled())
                {
                    if (faultyPaths.equals(faultyPathsCopy) == false)
                    {
                        operationLog.info(String.format(
                                "Reread faulty paths file '%s'. New entries are '%s'.",
                                getLocationDescription(faultyPathsFile),
                                CollectionUtils.abbreviate(faultyPaths, 10)));
                    }
                }
            }
        } else if (false == faultyPaths.isEmpty())
        {
            // Handles manual removal.
            faultyPaths.clear();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(
                        "Faulty paths file '%s' has been removed manually.",
                        getLocationDescription(faultyPathsFile)));
            }
        }
    }

    private final static String getLocationDescription(final File file)
    {
        return file.getPath();
    }

    private final boolean isFaultyPathsFile(final IScannedStore scannedStore,
            final StoreItem storeItem)
    {
        final String itemLocation = scannedStore.getLocationDescription(storeItem);
        final String faultyPathsLocation = getLocationDescription(faultyPathsFile);
        return itemLocation.equals(faultyPathsLocation);
    }

    private final void addToFaultyPaths(final IScannedStore scannedStore, final StoreItem item)
    {
        if (operationLog.isDebugEnabled())
        {
            final String path = scannedStore.getLocationDescription(item);
            operationLog.debug(String.format("Path '%s' has been added to faulty paths file '%s'",
                    path, faultyPathsFile.getAbsolutePath()));
        }
        faultyPaths.add(item);
        refreshFaultyPathsFile(scannedStore);
    }

    private final void refreshFaultyPathsFile(IScannedStore scannedStore)
    {
        CollectionIO.writeIterable(faultyPathsFile, faultyPathsAsStrings(scannedStore));
        faultyPathsLastChanged = faultyPathsFile.lastModified();
    }

    //
    // IDirectoryScanningHandler
    //

    @Override
    public void init(IScannedStore scannedStore)
    {
        final Iterator<StoreItem> it = faultyPaths.iterator();
        boolean changedFaultyPaths = false;
        while (it.hasNext())
        {
            final StoreItem item = it.next();
            if (scannedStore.existsOrError(item) == false)
            {
                it.remove();
                changedFaultyPaths = true;
            }
        }
        if (changedFaultyPaths)
        {
            refreshFaultyPathsFile(scannedStore);
        }
    }

    @Override
    public final void beforeHandle(IScannedStore scannedStore)
    {
        checkForFaultyPathsFileChanged(scannedStore);
    }

    @Override
    public final HandleInstruction mayHandle(final IScannedStore scannedStore,
            final StoreItem storeItem)
    {
        if (isFaultyPathsFile(scannedStore, storeItem))
        {
            return HandleInstruction.IGNORE;
        } else if (faultyPaths.contains(storeItem))
        {
            return HandleInstruction.createError("Known bad item '%s'.", storeItem);
        } else
        {
            return HandleInstruction.PROCESS;
        }
    }

    @Override
    public final Status finishItemHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        // if the external provided predicate says we should not add it to faulty paths, we won't
        if (delegate != null
                && delegate.shouldNotAddToFaultyPathsOrNull(scannedStore.asFile(storeItem)))
        {
            operationLog
                    .info("File "
                            + storeItem
                            + " not written to faulty paths. It will be reprocessed during the next iteration.");
            return Status.OK;
        }

        // If the item still exists, we assume that it has not been handled. So it
        // should be added to the faulty paths.
        if (scannedStore.existsOrError(storeItem) && stopSignaler.isStopped() == false)
        {
            addToFaultyPaths(scannedStore, storeItem);
            return Status.createError("Failed to move item '%s'.", storeItem);
        } else
        {
            return Status.OK;
        }
    }

}
