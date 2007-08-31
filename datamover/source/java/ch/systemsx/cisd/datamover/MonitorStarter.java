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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.util.Timer;

import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.datamover.helper.CopyFinishedMarker;
import ch.systemsx.cisd.datamover.helper.FileSystemHelper;
import ch.systemsx.cisd.datamover.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.intf.IPathCopier;
import ch.systemsx.cisd.datamover.intf.IPathImmutableCopier;
import ch.systemsx.cisd.datamover.intf.IPathRemover;
import ch.systemsx.cisd.datamover.intf.IReadPathOperations;

/**
 * A class that starts up the processing pipeline and its monitoring, based on the parameters provided.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak on Aug 24, 2007
 */
public class MonitorStarter
{
    private final static String LOCAL_COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private final static String LOCAL_COPY_COMPLETE_DIR = "copy-complete";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private final static String LOCAL_TEMP_DIR = "tmp";

    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final LocalBufferDirs bufferDirs;

    /**
     * starts the process of moving data and monitoring it
     * 
     * @return object which can be used to terminate the process and all its threads
     */
    public static final ITerminable start(Parameters parameters, IFileSysOperationsFactory factory)
    {
        LocalBufferDirs localBufferDirs =
                new LocalBufferDirs(parameters, LOCAL_COPY_IN_PROGRESS_DIR, LOCAL_COPY_COMPLETE_DIR,
                        LOCAL_READY_TO_MOVE_DIR, LOCAL_TEMP_DIR);
        return start(parameters, factory, localBufferDirs);
    }

    /** Allows to specify buffer directories. Exposed for testing purposes. */
    public static final ITerminable start(Parameters parameters, IFileSysOperationsFactory factory,
            LocalBufferDirs localBufferDirs)
    {
        return new MonitorStarter(parameters, factory, localBufferDirs).start();
    }

    private MonitorStarter(Parameters parameters, IFileSysOperationsFactory factory, LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.factory = factory;
        this.bufferDirs = bufferDirs;
    }

    private ITerminable start()
    {
        final LazyPathHandler outgoingProcessor = startupOutgoingMovingProcess(parameters.getOutgoingStore());
        final LazyPathHandler localProcessor = startupLocalProcessing(outgoingProcessor);
        final Timer incomingProcessor = startupIncomingMovingProcess(parameters.getIncomingStore(), localProcessor);
        return createTerminable(outgoingProcessor, localProcessor, incomingProcessor);
    }

    private static ITerminable createTerminable(final LazyPathHandler outgoingProcessor,
            final LazyPathHandler localProcessor, final Timer incomingProcessor)
    {
        return new ITerminable()
            {
                public boolean terminate()
                {
                    incomingProcessor.cancel();
                    boolean ok = localProcessor.terminate();
                    ok = ok && outgoingProcessor.terminate();
                    return ok;
                }
            };
    }

    private LazyPathHandler startupLocalProcessing(LazyPathHandler outgoingHandler)
    {
        IPathImmutableCopier copier = factory.getImmutableCopier();
        IPathHandler localProcesingHandler =
                LocalProcessorHandler.createAndRecover(parameters, bufferDirs.getCopyCompleteDir(), bufferDirs
                        .getReadyToMoveDir(), bufferDirs.getTempDir(), outgoingHandler, copier);
        return LazyPathHandler.create(localProcesingHandler, "Local Processor");
    }

    // --- Incoming data processing -----------------------

    private Timer startupIncomingMovingProcess(FileStore incomingStore, LazyPathHandler localProcessor)
    {
        IReadPathOperations readOperations = factory.getReadAccessor();
        boolean isIncomingRemote = parameters.getTreatIncomingAsRemote();

        recoverIncomingAfterShutdown(incomingStore, readOperations, isIncomingRemote, localProcessor);
        IPathHandler pathHandler =
                createIncomingMovingPathHandler(incomingStore.getHost(), localProcessor, isIncomingRemote);

        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(incomingStore.getPath(), new QuietPeriodFileFilter(parameters,
                        readOperations), pathHandler);
        final Timer movingTimer = new Timer("Mover of Incomming Data");
        // The moving task is scheduled at fixed rate. It makes sense especially if the task is moving data from the
        // remote share. The rationale behind this is that if new items are
        // added to the source directory while the remote timer task has been running for a long time, busy moving data,
        // the task shoulnd't sit idle for the check time when there is actually work to do.
        movingTimer.scheduleAtFixedRate(movingTask, 0, parameters.getCheckIntervalMillis());
        return movingTimer;
    }

    private void recoverIncomingAfterShutdown(FileStore incomingStore, IReadPathOperations incomingReadOperations,
            boolean isIncomingRemote, LazyPathHandler localProcessor)
    {
        if (isIncomingRemote == false)
            return; // no recovery is needed

        recoverIncomingInProgress(incomingStore, incomingReadOperations, bufferDirs.getCopyInProgressDir(), bufferDirs
                .getCopyCompleteDir());
        recoverIncomingCopyComplete(bufferDirs.getCopyCompleteDir(), localProcessor);
    }

    private static void recoverIncomingInProgress(FileStore incomingStore, IReadPathOperations incomingReadOperations,
            File copyInProgressDir, File copyCompleteDir)
    {
        File[] files = FileSystemHelper.listFiles(copyInProgressDir);
        if (files == null || files.length == 0)
            return; // directory is empty, no recovery is needed

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            recoverIncomingAfterShutdown(file, incomingStore, incomingReadOperations, copyCompleteDir);
        }
    }

    private static void recoverIncomingAfterShutdown(File unfinishedFile, FileStore incomingStore,
            IReadPathOperations incomingReadOperations, File copyCompleteDir)
    {
        if (CopyFinishedMarker.isMarker(unfinishedFile))
        {
            File markerFile = unfinishedFile;
            File localCopy = CopyFinishedMarker.extractOriginal(markerFile);
            if (localCopy.exists())
            {
                // copy and marker exist - do nothing, recovery will be done for copied resource
            } else
            {
                // copy finished, resource moved, but marker was not deleted
                markerFile.delete();
            }
        } else
        // handle local copy
        {
            File localCopy = unfinishedFile;
            File markerFile = CopyFinishedMarker.extractMarker(localCopy);
            if (markerFile.exists())
            {
                // copy and marker exist - copy finished, but copied resource not moved
                tryMoveFromInProgressToFinished(localCopy, markerFile, copyCompleteDir);
            } else
            // no marker
            {
                File incomingDir = incomingStore.getPath();
                File originalInIncoming = new File(incomingDir, localCopy.getName());
                if (incomingReadOperations.exists(originalInIncoming))
                {
                    // partial copy - nothing to do, will be copied again
                } else
                {
                    // move finished, but marker not created
                    tryMoveFromInProgressToFinished(localCopy, null, copyCompleteDir);
                }
            }
        }
    }

    // schedule processing of all resources which were previously copied
    private static void recoverIncomingCopyComplete(File copyCompleteDir, LazyPathHandler localProcessor)
    {
        File[] files = FileSystemHelper.listFiles(copyCompleteDir);
        if (files == null || files.length == 0)
            return; // directory is empty, no recovery is needed

        for (int i = 0; i < files.length; i++)
        {
            localProcessor.handle(files[i]);
        }
    }

    private IPathHandler createIncomingMovingPathHandler(final String sourceHostOrNull,
            final LazyPathHandler localProcessor, final boolean isIncomingRemote)
    {
        return new IPathHandler()
            {
                public boolean handle(File sourceFile)
                {
                    if (isIncomingRemote)
                    {
                        return moveFromRemoteIncoming(sourceFile, sourceHostOrNull, localProcessor);
                    } else
                    {
                        return moveFromLocalIncoming(sourceFile, localProcessor);
                    }
                }
            };
    }

    private boolean moveFromLocalIncoming(File source, LazyPathHandler localProcessor)
    {
        File finalFile = tryMoveLocal(source, bufferDirs.getCopyCompleteDir());
        if (finalFile == null)
        {
            return false;
        }
        return localProcessor.handle(finalFile);
    }

    private boolean moveFromRemoteIncoming(File source, String sourceHostOrNull, LazyPathHandler localProcessor)
    {
        // 1. move from incoming: copy, delete, create copy-finished-marker
        File copyInProgressDir = bufferDirs.getCopyInProgressDir();
        boolean ok = moveFromRemoteToLocal(source, sourceHostOrNull, copyInProgressDir);
        if (ok == false)
        {
            return false;
        }
        File copiedFile = new File(copyInProgressDir, source.getName());
        assert copiedFile.exists();
        File markerFile = CopyFinishedMarker.extractMarker(copiedFile);
        assert markerFile.exists();

        // 2. Move to final directory, delete marker
        File finalFile = tryMoveFromInProgressToFinished(copiedFile, markerFile, bufferDirs.getCopyCompleteDir());
        if (finalFile == null)
        {
            return false;
        }

        // 3. schedule local processing, always successful
        localProcessor.handle(finalFile);
        return true;
    }

    private static File tryMoveFromInProgressToFinished(File copiedFile, File markerFileOrNull, File copyCompleteDir)
    {
        File finalFile = tryMoveLocal(copiedFile, copyCompleteDir);
        if (finalFile != null)
        {
            if (markerFileOrNull != null)
            {
                markerFileOrNull.delete(); // process even if marker file could not be deleted
            }
            return finalFile;
        } else
        {
            return null;
        }
    }

    private boolean moveFromRemoteToLocal(File source, String sourceHostOrNull, File localDestDir)
    {
        return createRemotePathMover(sourceHostOrNull, localDestDir, null).handle(source);
    }

    private static File tryMoveLocal(File sourceFile, File destinationDir)
    {
        return FileSystemHelper.tryMoveLocal(sourceFile, destinationDir);
    }

    // --------------------------

    private LazyPathHandler startupOutgoingMovingProcess(FileStore outputDir)
    {
        final File outgoingDirectory = outputDir.getPath();
        final String outgoingHost = outputDir.getHost();
        final IPathHandler remoteMover = createRemotePathMover(null, outgoingDirectory, outgoingHost);
        return LazyPathHandler.create(remoteMover, "Final Destination Mover");
    }

    private IPathHandler createRemotePathMover(String sourceHost, File destinationDirectory, String destinationHost)
    {
        IPathCopier copier = factory.getCopier(destinationDirectory);
        CopyActivityMonitor monitor =
                new CopyActivityMonitor(destinationDirectory, factory.getReadAccessor(), copier, parameters);
        IPathRemover remover = factory.getRemover();
        return new RemotePathMover(destinationDirectory, destinationHost, monitor, remover, copier, sourceHost,
                parameters);
    }
}
