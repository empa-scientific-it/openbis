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

package ch.systemsx.cisd.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * The base class for running many tasks in parallel.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public class ParallelizedExecutor
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ParallelizedExecutor.class);

    private static final int NUMBER_OF_CPU_CORES = Runtime.getRuntime().availableProcessors();

    private static <T> Queue<T> tryFillWorkerQueue(List<T> itemsToProcessOrNull)
            throws EnvironmentFailureException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Found %d items to process.",
                    itemsToProcessOrNull.size()));
        }
        if (itemsToProcessOrNull.isEmpty())
        {
            return null;
        }
        return new ArrayBlockingQueue<T>(itemsToProcessOrNull.size(), false, itemsToProcessOrNull);
    }

    @Private
    static int getInitialNumberOfWorkers(double machineLoad, int maxThreads, int numberOfTaskItems)
    {
        long threads = Math.round(NUMBER_OF_CPU_CORES * machineLoad);
        threads = Math.min(threads, maxThreads);
        threads = Math.min(threads, numberOfTaskItems);
        return (int) Math.max(1, threads);
    }

    private static <T> Thread[] startUpWorkerThreads(AtomicInteger workersCounter,
            Queue<T> workerQueue, Collection<FailureRecord<T>> failed,
            ITaskExecutor<T> taskExecutor, int retriesNumberWhenExecutionFails,
            boolean stopOnFirstFailure)
    {
        int counter = workersCounter.get();
        final Thread[] workerThreads = new Thread[counter];
        for (int i = 0; i < counter; ++i)
        {
            ParallelizedWorker<T> worker =
                    new ParallelizedWorker<T>(workerQueue, failed, taskExecutor, workersCounter,
                            retriesNumberWhenExecutionFails, stopOnFirstFailure);
            workerThreads[i] = new Thread(worker, "Worker " + i);
            workerThreads[i].start();
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Started up %d worker threads.", counter));
        }
        return workerThreads;
    }

    /**
     * Processes all items with the specified task executor.
     * <p>
     * Uses #cores * <var>machineLoad</var> threads for the processing, but not more than <var>maxThreads</var>.
     * 
     * @param stopOnFirstFailure if true and processing of any item fails (after the specified number of retries), then next items are not processed
     *            at all.
     */
    public static <T> Collection<FailureRecord<T>> process(List<T> itemsToProcessOrNull,
            ITaskExecutor<T> taskExecutor, double machineLoad, int maxThreads,
            String processDescription, int retriesNumberWhenExecutionFails,
            boolean stopOnFirstFailure) throws InterruptedExceptionUnchecked,
            EnvironmentFailureException
    {
        long start = System.currentTimeMillis();
        final Queue<T> workerQueue = tryFillWorkerQueue(itemsToProcessOrNull);
        final Collection<FailureRecord<T>> failed =
                Collections.synchronizedCollection(new ArrayList<FailureRecord<T>>());
        if (workerQueue == null || workerQueue.isEmpty())
        {
            return failed;
        }
        int numberOfWorkers =
                getInitialNumberOfWorkers(machineLoad, maxThreads, itemsToProcessOrNull.size());
        if (numberOfWorkers == 1)
        {
            processInTheSameThread(itemsToProcessOrNull, taskExecutor,
                    retriesNumberWhenExecutionFails, failed);
        } else
        {
            processInParallel(taskExecutor, retriesNumberWhenExecutionFails, workerQueue, failed,
                    numberOfWorkers, stopOnFirstFailure);
        }
        logFinished(failed, processDescription, start);
        return failed;
    }

    private static <T> void processInTheSameThread(List<T> itemsToProcess,
            ITaskExecutor<T> taskExecutor, int retriesNumberWhenExecutionFails, Collection<FailureRecord<T>> failed)
    {
        for (T item : itemsToProcess)
        {
            int counter = retriesNumberWhenExecutionFails;
            Status status;
            do
            {
                status = taskExecutor.execute(item);
                counter--;
            } while (counter > 0 && status.isError());
            if (status.isError())
            {
                failed.add(new FailureRecord<T>(item, status));
            }
        }
    }

    private static <T> void processInParallel(ITaskExecutor<T> taskExecutor,
            int retriesNumberWhenExecutionFails, final Queue<T> workerQueue,
            final Collection<FailureRecord<T>> failed, int numberOfWorkers,
            boolean stopOnFirstFailure)
    {
        final AtomicInteger workersCounter = new AtomicInteger(numberOfWorkers);
        final Thread[] threads =
                startUpWorkerThreads(workersCounter, workerQueue, failed, taskExecutor,
                        retriesNumberWhenExecutionFails, stopOnFirstFailure);
        synchronized (failed)
        {
            while (workersCounter.get() > 0)
            {
                try
                {
                    failed.wait();
                } catch (InterruptedException ex)
                {
                    workerQueue.clear();
                    for (Thread t : threads)
                    {
                        t.interrupt();
                    }
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }

    private static <T> void logFinished(Collection<FailureRecord<T>> failureReport,
            String processDescription, long startTimeMsec)
    {
        int errorsNumber = failureReport.size();
        String time = "[" + (System.currentTimeMillis() - startTimeMsec) + " msec.] ";
        operationLog.info(processDescription + " finished "
                + (errorsNumber == 0 ? "successfully" : "with " + errorsNumber + " errors") + " "
                + time + ".");
    }

    /**
     * Converts the <var>failureRecord</var> to an error string. If <code>failureRecords.isEmpty()</code>, then return <code>null</code>.
     */
    public static <T> String tryFailuresToString(Collection<FailureRecord<T>> failureRecords)
    {
        assert failureRecords != null;

        if (failureRecords.size() > 0)
        {
            final StringBuilder errorMsgBuilder = new StringBuilder();
            errorMsgBuilder.append("The following items could not be successfully processed:\n");
            for (FailureRecord<T> r : failureRecords)
            {
                errorMsgBuilder.append(String.format("%s (%s)\n", r.getFailedItem().toString(), r
                        .getFailureStatus()));
            }
            return errorMsgBuilder.toString();
        }
        return null;
    }

    private ParallelizedExecutor()
    {
        // Do not instantiate.
    }
}
