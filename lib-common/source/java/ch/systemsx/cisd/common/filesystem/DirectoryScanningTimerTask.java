/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ITimerTaskStatusProvider;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler.HandleInstruction;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler.HandleInstructionFlag;
import ch.systemsx.cisd.common.logging.ConditionalNotificationLogger;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link TimerTask} that scans a source directory for entries that are accepted by some {@link FileFilter} and handles the accepted entries by some
 * {@link IPathHandler}. It maintains a list of faulty paths that failed to be handled OK in the past. Clearing the list will make the class to retry
 * handling the paths.
 * <p>
 * The class should be constructed in the start-up phase and as part of the system's self-test in order to reveal problems with incorrect paths
 * timely.
 * </p>
 * 
 * @author Bernd Rinn
 */
public final class DirectoryScanningTimerTask extends TimerTask implements ITimerTaskStatusProvider
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DirectoryScanningTimerTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DirectoryScanningTimerTask.class);

    private final IStoreHandler storeHandler;

    private final IScannedStore sourceDirectory;

    private final IDirectoryScanningHandler directoryScanningHandler;

    private final ConditionalNotificationLogger notificationLogger;

    private final Map<StoreItem, String> errorLog;

    private boolean didSomeWork;

    private String threadNameOrNull;

    private File activityLogDirectoryOrNull;

    /**
     * Indicates that we should try to exit the {@link #run()} method as soon as possible.
     * <p>
     * This could be set asynchronously via {@link #stopRun()}.
     * </p>
     */
    private volatile boolean stopRun;

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     * @param directoryScanningHandler A directory scanning handler.
     * @param threadName The name of the thread
     * @param activityLogDirectory The directory to log activity to.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler handler, final IDirectoryScanningHandler directoryScanningHandler,
            final String threadName, final File activityLogDirectory)
    {
        this(asScannedStore(sourceDirectory, filter), directoryScanningHandler, PathHandlerAdapter
                .asScanningHandler(sourceDirectory, handler), 0, threadName, activityLogDirectory);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     * @param directoryScanningHandler A directory scanning handler.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler handler, final IDirectoryScanningHandler directoryScanningHandler)
    {
        this(asScannedStore(sourceDirectory, filter), directoryScanningHandler, PathHandlerAdapter
                .asScanningHandler(sourceDirectory, handler), 0, null, null);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param scannedStore The store which is scan for entries.
     * @param directoryScanningHandler A directory scanning handler.
     * @param storeHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the next error is logged (can be
     *            used to suppress error when the directory is on a remote share and the server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(final IScannedStore scannedStore,
            final IDirectoryScanningHandler directoryScanningHandler,
            final IStoreHandler storeHandler, final int ignoredErrorCount)
    {
        this(scannedStore, directoryScanningHandler, storeHandler, ignoredErrorCount, null, null);

    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param storeHandler The handler that is used for treating the matching paths.
     * @param directoryScanningHandler A directory scanning handler.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IStoreHandler storeHandler,
            final IDirectoryScanningHandler directoryScanningHandler)
    {
        this(asScannedStore(sourceDirectory, filter), directoryScanningHandler, storeHandler, 0,
                null, null);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler pathHandler)
    {
        this(sourceDirectory, filter, pathHandler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     * @param threadName The name of the thread
     * @param activityLogDirectory The directory to log activity to.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler pathHandler, String threadName, File activityLogDirectory)
    {
        this(sourceDirectory, filter, pathHandler, 0, threadName, activityLogDirectory);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param scannedStore The store which is scan for entries.
     * @param directoryScanningHandler A directory scanning handler.
     * @param storeHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the next error is logged (can be
     *            used to suppress error when the directory is on a remote share and the server is flaky sometimes)
     * @param threadName The name of the thread
     * @param activityLogDirectory The directory to log activity to.
     */
    public DirectoryScanningTimerTask(final IScannedStore scannedStore,
            final IDirectoryScanningHandler directoryScanningHandler,
            final IStoreHandler storeHandler, final int ignoredErrorCount, final String threadName,
            final File activityLogDirectory)
    {
        assert scannedStore != null;
        assert storeHandler != null;
        assert directoryScanningHandler != null : "Unspecified IDirectoryScanningHandler implementation";
        assert ignoredErrorCount >= 0;

        this.sourceDirectory = scannedStore;
        this.storeHandler = storeHandler;
        this.directoryScanningHandler = directoryScanningHandler;
        this.notificationLogger =
                new ConditionalNotificationLogger(operationLog, Level.WARN, notificationLog,
                        ignoredErrorCount);
        this.errorLog = new LinkedHashMap<StoreItem, String>();
        this.threadNameOrNull = threadName;
        this.activityLogDirectoryOrNull = activityLogDirectory;
        if (activityLogDirectory != null && activityLogDirectory.isDirectory() == false)
        {
            activityLogDirectory.mkdirs();
            if (activityLogDirectory.isDirectory() == false)
            {
                operationLog.error("Cannot create activityLogDirectory " + activityLogDirectory
                        + " - activity logging disabled.");
            }
        }
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param fileFilter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the next error is logged (can be
     *            used to suppress error when the directory is on a remote share and the server is flaky sometimes)
     */
    DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter fileFilter,
            final IPathHandler pathHandler, final int ignoredErrorCount)
    {
        this(asScannedStore(sourceDirectory, fileFilter), new FaultyPathDirectoryScanningHandler(
                sourceDirectory, pathHandler, null), PathHandlerAdapter.asScanningHandler(
                sourceDirectory, pathHandler), ignoredErrorCount, null, null);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param fileFilter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the next error is logged (can be
     *            used to suppress error when the directory is on a remote share and the server is flaky sometimes)
     * @param threadName The name of the thread
     * @param activityLogDirectory The directory to log activity to.
     */
    DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter fileFilter,
            final IPathHandler pathHandler, final int ignoredErrorCount, final String threadName,
            final File activityLogDirectory)
    {
        this(asScannedStore(sourceDirectory, fileFilter), new FaultyPathDirectoryScanningHandler(
                sourceDirectory, pathHandler, null), PathHandlerAdapter.asScanningHandler(
                sourceDirectory, pathHandler), ignoredErrorCount, threadName, activityLogDirectory);
    }

    private final static IScannedStore asScannedStore(final File directory, final FileFilter filter)
    {
        return new DirectoryScannedStore(filter, directory);
    }

    private final void printNotification(final Exception ex)
    {
        if (ex instanceof TimeoutExceptionUnchecked)
        {
            notificationLog.error("Timeout while scanning directory: " + ex.getMessage(), ex);
        } else
        {
            notificationLog.error("An exception has occurred. (thread still running)",
                    CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    private final StoreItem[] listStoreItems()
    {
        final StoreItem[] storeItems = sourceDirectory.tryListSorted(notificationLogger);
        if (storeItems != null)
        {
            notificationLogger.reset(String.format("Directory '%s' is available again.",
                    sourceDirectory));
        }
        return (storeItems == null) ? StoreItem.EMPTY_ARRAY : storeItems;
    }

    /**
     * Tries to stop {@link #run()} method before it exists.
     */
    public final void stopRun()
    {
        stopRun = true;
    }

    //
    // TimerTask
    //

    /**
     * Handles all entries in the source directory that are picked by the filter.
     */
    @Override
    public final void run()
    {
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Start scanning directory '%s'.", sourceDirectory));
        }
        if (threadNameOrNull == null)
        {
            threadNameOrNull = Thread.currentThread().getName();
        }
        logActivity();
        try
        {
            didSomeWork = false;
            int numberOfItemsProcessedInLastRound;
            directoryScanningHandler.init(sourceDirectory);
            do
            {
                numberOfItemsProcessedInLastRound = 0;
                final StoreItem[] allStoreItems = listStoreItems();
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format("Found %d store items in total.",
                            allStoreItems.length));
                }
                cleanseErrorLog(allStoreItems);
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format(
                            "Found %d store items that are not in error state.",
                            allStoreItems.length));
                }
                final StoreItem[] storeItemsOrNull =
                        sourceDirectory.tryFilterReadyToProcess(allStoreItems, notificationLogger);
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format("Found %d store items ready to be processed.",
                            (storeItemsOrNull == null) ? 0 : storeItemsOrNull.length));
                }
                if (storeItemsOrNull == null)
                {
                    break;
                }
                final int numberOfItems = storeItemsOrNull.length;
                directoryScanningHandler.beforeHandle(sourceDirectory);
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace("beforeHandle() completed.");
                }
                for (int i = 0; i < numberOfItems; i++)
                {
                    final StoreItem storeItem = storeItemsOrNull[i];
                    if (operationLog.isTraceEnabled())
                    {
                        operationLog.trace(String
                                .format("Looping over store item '%s'.", storeItem));
                    }
                    if (stopRun || Thread.interrupted() || storeHandler.isStopped())
                    {
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog
                                    .debug(String.format("Scan of store '%s' has been cancelled. "
                                            + "Following items have NOT been handled: %s.",
                                            sourceDirectory, CollectionUtils.abbreviate(ArrayUtils
                                                    .subarray(storeItemsOrNull, i + 1,
                                                            numberOfItems), 10)));
                        }
                        return;
                    }
                    final HandleInstruction instruction =
                            directoryScanningHandler.mayHandle(sourceDirectory, storeItem);
                    if (operationLog.isTraceEnabled())
                    {
                        if (instruction.tryGetMessage() == null)
                        {
                            operationLog.trace(String.format("Handle instruction: %s.",
                                    instruction.getFlag()));
                        } else
                        {
                            operationLog.trace(String.format("Handle instruction: %s [%s].",
                                    instruction.getFlag(), instruction.tryGetMessage()));
                        }
                    }
                    if (HandleInstructionFlag.PROCESS.equals(instruction.getFlag()))
                    {
                        try
                        {
                            storeHandler.handle(storeItem);
                            if (operationLog.isTraceEnabled())
                            {
                                operationLog.trace(String.format(
                                        "Store item '%s' has been handled.", storeItem));
                            }
                            didSomeWork = true;
                            ++numberOfItemsProcessedInLastRound;
                        } catch (final Exception ex)
                        {
                            if (ex instanceof InterruptedExceptionUnchecked)
                            {
                                break;
                            }
                            // Do not stop when processing of one file has failed,
                            // continue with other files.
                            errorLog.put(storeItem, String.format(
                                    "Exception when processing item '%s': %s (%s)", storeItem, ex
                                            .getClass().getSimpleName(), StringUtils.defaultString(
                                            ex.getMessage(), "no message")));
                            printNotification(ex);
                        } finally
                        {
                            final Status status =
                                    directoryScanningHandler.finishItemHandle(sourceDirectory,
                                            storeItem);
                            if (status.isError())
                            {
                                final String msgOrNull = status.tryGetErrorMessage();
                                errorLog.put(storeItem, StringUtils.defaultIfEmpty(msgOrNull,
                                        getDefaultErrorMessage(storeItem)));
                            }
                        }
                    } else
                    {
                        if (HandleInstructionFlag.ERROR.equals(instruction.getFlag()))
                        {
                            final String msgOrNull = instruction.tryGetMessage();
                            errorLog.put(storeItem, StringUtils.defaultIfEmpty(msgOrNull,
                                    getDefaultErrorMessage(storeItem)));
                        }
                        if (operationLog.isTraceEnabled())
                        {
                            operationLog.trace(String.format(
                                    "Following store item '%s' has NOT been handled (%s).",
                                    storeItem, instruction));
                        }
                    }
                }
            } while (numberOfItemsProcessedInLastRound > 0);
        } catch (final Exception ex)
        {
            if (ex instanceof InterruptedExceptionUnchecked == false)
            {
                printNotification(ex);
            }
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Finished scanning directory '%s'.", sourceDirectory));
        }
    }

    private void logActivity()
    {
        if (activityLogDirectoryOrNull != null)
        {
            final File activityFile =
                    new File(activityLogDirectoryOrNull, threadNameOrNull.replace(' ', '_'));
            try
            {
                FileUtils.touch(activityFile);
            } catch (IOException ex)
            {
                operationLog.warn("Cannot touch activity file " + activityFile);
            }
        }
    }

    private void cleanseErrorLog(StoreItem[] allStoreItemsOrNull)
    {
        if (allStoreItemsOrNull == null)
        {
            return;
        }
        final Set<StoreItem> itemSet = new HashSet<StoreItem>(Arrays.asList(allStoreItemsOrNull));
        for (StoreItem errorItem : new ArrayList<StoreItem>(errorLog.keySet()))
        {
            if (itemSet.contains(errorItem) == false)
            {
                errorLog.remove(errorItem);
            }
        }
    }

    private String getDefaultErrorMessage(final StoreItem storeItem)
    {
        return String.format("Error processing item '%s'.", storeItem);
    }

    //
    // ITimerTaskStatusProvider
    //

    @Override
    public boolean hasErrors()
    {
        return errorLog.size() > 0;
    }

    @Override
    public boolean hasPerformedMeaningfulWork()
    {
        return didSomeWork;
    }

    @Override
    public String tryGetErrorLog()
    {
        if (hasErrors())
        {
            return String.format("  [%s]\n  %s",
                    StringUtils.defaultIfEmpty(threadNameOrNull, "UNKNOWN"),
                    StringUtils.join(errorLog.values(), "\n  "));
        } else
        {
            return null;
        }
    }

    //
    // Helper classes
    //

    public static interface IScannedStore
    {
        /**
         * List <i>all</i> items (not just the ones who are ready to be processed) in the scanned store in order in which they should be handled.
         * 
         * @return <code>null</code> if it was no able to access the items of this scanned store.
         */
        StoreItem[] tryListSorted(ISimpleLogger loggerOrNull);

        /**
         * Performs a filtering on the items.
         * 
         * @returns Only those <var>items</var> which are ready to be processed right now, or <code>null</code>, if the filtering step produced an
         *          exception.
         */
        StoreItem[] tryFilterReadyToProcess(final StoreItem[] items, ISimpleLogger loggerOrNull);

        /**
         * Returns <code>true</code>, if the <var>item</var> either still exists or is in an error state.
         */
        boolean existsOrError(StoreItem item);

        /**
         * returned description should give the user the idea about file location. You should not use the result for something else than printing it
         * for user. It should not be especially assumed that the result is the path which could be used in java.io.File constructor.
         */
        String getLocationDescription(StoreItem item);

        /**
         * Convert the store item to a file.
         * 
         * @throws UnsupportedOperationException Thrown if the store item cannot be represented as a file object.
         */
        File asFile(StoreItem item) throws UnsupportedOperationException;

        /**
         * Constructs a {@link StoreItem} from a location description as created by {@link #getLocationDescription(StoreItem)}.
         */
        StoreItem asStoreItem(String locationDescription);
    }

}
