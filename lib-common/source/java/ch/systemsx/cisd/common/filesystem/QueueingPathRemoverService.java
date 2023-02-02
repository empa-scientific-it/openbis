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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.collection.ExtendedLinkedBlockingQueue;
import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueDecorator;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.io.QueuePersister;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A service for removing (deep) paths. It provides a method {@link #removeRecursively(File)} that marks {@link File}s for deletion and queues them
 * up, using a separate thread to actually delete them.
 * <p>
 * Note that the service needs to be started via {@link #start(File, File, TimingParameters)}.
 * <p>
 * A file can be specified that keeps track of all the items that are to be deleted in order to persist program restart.
 * 
 * @author Bernd Rinn
 */
public class QueueingPathRemoverService
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            QueueingPathRemoverService.class);

    @Private
    final static String SHREDDER_PREFIX = ".SHREDDER_";

    private static final AtomicInteger counter = new AtomicInteger();

    private static IExtendedBlockingQueue<File> queue = null;

    private static Closeable queueCloseableOrNull = null;

    private static Thread thread = null;

    private static IFileRemover deepRemover = null;

    private static File shredderDir;

    /**
     * Initializes the shredder thread. Will not persist over program restart. <i>Needs to be called before this class is constructed for the first
     * time.</i>
     */
    public static final void start()
    {
        start(null, null);
    }

    /**
     * Initializes the shredder thread. <i>Needs to be called before this class is constructed for the first time.</i>
     * 
     * @param queueFileOrNull If not <code>null</code>, the file will be used to persist the items to be deleted over program restart.
     */
    public static final void start(File storeRootOrNull, File queueFileOrNull)
    {
        start(storeRootOrNull, queueFileOrNull, TimingParameters.getDefaultParameters());
    }

    /**
     * Initializes the shredder thread. <i>Needs to be called before this class is constructed for the first time.</i>
     * 
     * @param queueFileOrNull If not <code>null</code>, the file will be used to persist the items to be deleted over program restart.
     */
    public static synchronized final void start(final File storeRootOrNull, final File queueFileOrNull,
            TimingParameters parameters)
    {
        final ISimpleLogger logger = new Log4jSimpleLogger(operationLog);
        final IFileRemover monitoringProxy = FileOperations.createMonitoredInstance(parameters);
        deepRemover = new LoggingPathRemoverDecorator(monitoringProxy, logger, false);
        if (queueFileOrNull != null)
        {
            final PersistentExtendedBlockingQueueDecorator<File> persistentQueue =
                    PersistentExtendedBlockingQueueFactory.createSmartPersist(queueFileOrNull);
            queue = persistentQueue;
            queueCloseableOrNull = persistentQueue;
        } else
        {
            queue = new ExtendedLinkedBlockingQueue<File>();
        }

        if (storeRootOrNull != null)
        {
            shredderDir = new File(storeRootOrNull, ".SHREDDER");
            if (!shredderDir.exists())
            {
                shredderDir.mkdir();
            }
        }

        thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            final File fileToRemove = queue.peekWait();
                            deepRemover.removeRecursively(fileToRemove);
                            // Note: this is the only consumer of this queue.
                            queue.take();
                        }
                    } catch (InterruptedException ex)
                    {
                        // Exit thread.
                    } catch (InterruptedExceptionUnchecked ex)
                    {
                        // Exit thread.
                    }
                }
            }, "Shredder Queue");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Deletes <var>fileToRemove</var>, if necessary recursively. If it is a file, it will be deleted immediately, if it is a directory, it will be
     * queued up for asynchronous deletion.
     * <p>
     * <i>This method does not monitor file system operations for hangs. If you need this functionality, use
     * {@link IFileOperations#removeRecursivelyQueueing(File)} instead!</i>
     */
    public static boolean removeRecursively(File fileToRemove)
    {
        if (isRunning() == false)
        {
            throw new IllegalStateException(
                    "Cannot remove the file because the shreder is already stopped: "
                            + fileToRemove);
        }
        if (fileToRemove.isFile())
        {
            return fileToRemove.delete();
        } else
        {
            final String name =
                    SHREDDER_PREFIX + System.currentTimeMillis() + "-" + counter.incrementAndGet()
                            + "-" + fileToRemove.getName();
            final File shredderFile;
            if (shredderDir != null)
            {
                shredderFile = new File(shredderDir, name);
            } else
            {
                shredderFile = new File(fileToRemove.getParentFile(), name);
            }

            final boolean ok = fileToRemove.renameTo(shredderFile);
            if (ok)
            {
                queue.add(shredderFile);
            }
            return ok;
        }
    }

    private static final void close()
    {
        if (queueCloseableOrNull != null)
        {
            try
            {
                queueCloseableOrNull.close();
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
    }

    /**
     * Stop the service.
     */
    public static synchronized final void stop()
    {
        if (thread == null)
        {
            return;
        }
        thread.interrupt();
        close();
        thread = null;
        queue = null;
        queueCloseableOrNull = null;
        deepRemover = null;
    }

    /**
     * Stop the service and wait for it to finish, but at most <var>timeoutMillis</var> milli-seconds.
     * 
     * @return <code>true</code>, if stopping was successful, <code>false</code> otherwise.
     */
    public static synchronized final boolean stopAndWait(long timeoutMillis)
    {
        if (thread == null)
        {
            return true;
        }
        thread.interrupt();
        try
        {
            thread.join(timeoutMillis);
        } catch (InterruptedException ex)
        {
        }
        close();
        final boolean ok = (thread.isAlive() == false);
        thread = null;
        queue = null;
        queueCloseableOrNull = null;
        deepRemover = null;
        return ok;
    }

    /**
     * Returns <code>true</code>, if the service is currently running, <code>false</code> otherwise.
     */
    public static synchronized final boolean isRunning()
    {
        return deepRemover != null;
    }

    /**
     * Returns the list of currently queued up shredder items.
     */
    public static final List<File> listShredderItems(File queueFile)
    {
        return QueuePersister.list(File.class, queueFile);
    }

    private QueueingPathRemoverService()
    {
        // Cannot be instantiated.
    }

}
