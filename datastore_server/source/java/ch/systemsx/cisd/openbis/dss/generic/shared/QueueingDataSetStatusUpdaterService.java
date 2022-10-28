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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueDecorator;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.io.QueuePersister;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;

/**
 * A service for updating data set status in openBIS. It provides a method {@link #update(DataSetCodesWithStatus)} that queues updates using a
 * separate thread to actually perform update.
 * <p>
 * Note that the service needs to be started via {@link #start(File, TimingParameters)}.
 * <p>
 * A file that keeps track of all the data sets that are to be updated needs to be specified in order to persist program restart.
 * 
 * @author Piotr Buczek
 */
public class QueueingDataSetStatusUpdaterService
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            QueueingDataSetStatusUpdaterService.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            QueueingDataSetStatusUpdaterService.class);

    private static IExtendedBlockingQueue<DataSetCodesWithStatus> queue = null;

    private static Closeable queueCloseableOrNull = null;

    private static Thread thread = null;

    private static IDataSetStatusUpdater updater = null;

    /**
     * Initializes the updater thread. <i>Needs to be called before this class is constructed for the first time.</i>
     * 
     * @param queueFile the file that will be used to persist the items to be deleted over program restart.
     */
    public static final void start(File queueFile)
    {
        start(queueFile, TimingParameters.getDefaultParameters());
    }

    /**
     * Initializes the updater thread. <i>Needs to be called before this class is constructed for the first time.</i>
     * 
     * @param queueFile the file that will be used to persist the items to be deleted over program restart.
     */
    public static synchronized final void start(final File queueFile, TimingParameters parameters)
    {
        final PersistentExtendedBlockingQueueDecorator<DataSetCodesWithStatus> persistentQueue =
                PersistentExtendedBlockingQueueFactory.createSmartPersist(queueFile);
        queue = persistentQueue;
        queueCloseableOrNull = persistentQueue;
        updater = createDataSetStatusUpdater();
        thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            final DataSetCodesWithStatus dataSets = queue.peekWait();
                            try
                            {
                                updater.updateDataSetStatuses(dataSets.getDataSetCodes(),
                                        dataSets.getStatus(), dataSets.isPresentInArchive());
                                // Note: this is the only consumer of this queue.
                                queue.take();
                                // If update succeeded than it is possible that other updates
                                // that failed before will work too so we can reduce sleep time
                                // for next failures.
                                Sleeper.resetSleepTime();
                            } catch (RemoteAccessException ex)
                            {
                                // If connection with openBIS fails it is possible that
                                // the same problem will occur for other updates in the queue,
                                // so we just retry after increasing time.
                                notifyUpdateFailure(dataSets, ex);
                                Sleeper.sleepAndIncreaseSleepTime();
                            } catch (UserFailureException ex)
                            {
                                // OpenBIS failure occurred - the problem may be connected with
                                // certain data set so move this item to the end of the queue and
                                // try to update other data sets before retrying.
                                notifyUpdateFailure(dataSets, ex);
                                Sleeper.sleepAndIncreaseSleepTime();
                                queue.add(dataSets);
                                queue.remove();
                            }
                        }
                    } catch (InterruptedException ex)
                    {
                        // Exit thread.
                    } catch (InterruptedExceptionUnchecked ex)
                    {
                        // Exit thread.
                    }
                }

                private void notifyUpdateFailure(final DataSetCodesWithStatus dataSets, Exception ex)
                {
                    notificationLog.error(
                            "Update of data sets "
                                    + CollectionUtils.abbreviate(dataSets.getDataSetCodes(), 10)
                                    + " status to '" + dataSets.getStatus()
                                    + "' has failed.\nRetry will occur not sooner than "
                                    + Sleeper.getCurrentSleepTime() + ".",
                            ex);
                }
            }, "Updater Queue");
        thread.setDaemon(true);
        thread.start();
    }

    private static IDataSetStatusUpdater createDataSetStatusUpdater()
    {
        return new IDataSetStatusUpdater()
            {
                @Override
                public void updateDataSetStatuses(List<String> dataSetCodes,
                        DataSetArchivingStatus newStatus, boolean presentInArchive)
                {
                    ServiceProvider.getOpenBISService().updateDataSetStatuses(dataSetCodes,
                            newStatus, presentInArchive);
                    operationLog.info("Data Sets " + CollectionUtils.abbreviate(dataSetCodes, 10)
                            + " changed status to " + newStatus);
                }

            };
    }

    /**
     * Schedules update of given data sets.
     */
    public static void update(DataSetCodesWithStatus dataSets)
    {
        if (dataSets.getDataSetCodes().isEmpty() == false)
        {
            queue.add(dataSets);
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
        updater = null;
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
        updater = null;
        return ok;
    }

    /**
     * Returns <code>true</code>, if the service is currently running, <code>false</code> otherwise.
     */
    public static synchronized final boolean isRunning()
    {
        return updater != null;
    }

    /**
     * Returns the list of currently queued up items.
     */
    public static final List<DataSetCodesWithStatus> listItems(File queueFile)
    {
        return QueuePersister.list(DataSetCodesWithStatus.class, queueFile);
    }

    private QueueingDataSetStatusUpdaterService()
    {
        // Cannot be instantiated.
    }

    /**
     * Helper class with a {@link #sleepAndIncreaseSleepTime()} method that invokes {@link Thread#sleep(long)} with an increasing amount time.
     */
    private static class Sleeper
    {

        private static final long SECOND_IN_MILIS = 1000;

        /** start from 1 minute */
        private static final long INITIAL_SLEEP_TIME = 60 * SECOND_IN_MILIS;

        /** 1 day */
        private static final long MAX_SLEEP_TIME = 86400 * SECOND_IN_MILIS;

        /** after each sleep increase the sleep time using this factor */
        private static final int FACTOR = 2;

        /** current sleep time in milliseconds */
        private static long sleepTime = INITIAL_SLEEP_TIME;

        public static String getCurrentSleepTime()
        {
            long seconds = sleepTime / SECOND_IN_MILIS;
            return seconds + "s";
        }

        public static void sleepAndIncreaseSleepTime() throws InterruptedException
        {
            operationLog.info("Going to sleep for " + getCurrentSleepTime());
            Thread.sleep(sleepTime);
            sleepTime *= FACTOR;
            if (sleepTime > MAX_SLEEP_TIME)
            {
                sleepTime = MAX_SLEEP_TIME;
            }
        }

        public static void resetSleepTime()
        {
            sleepTime = INITIAL_SLEEP_TIME;
        }
    }

    /**
     * A role that can update data set status.
     * 
     * @author Piotr Buczek
     */
    public interface IDataSetStatusUpdater
    {
        /**
         * Updates status of data sets with given codes.
         * 
         * @param dataSetCodes codes of data sets to be updated
         * @param newStatus status to be set
         * @param presentInArchive the present in archive flag to set
         */
        public void updateDataSetStatuses(List<String> dataSetCodes,
                DataSetArchivingStatus newStatus, boolean presentInArchive);
    }

}
