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

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link Runnable} worker which allows to execute tasks from the specified queue until it is empty. Many workers can run in parallel using the same
 * queue.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
class ParallelizedWorker<T> implements Runnable
{
    @Private
    static final String PROGRESS_MSG_TEMPLATE = "Processing '%s'.";

    @Private
    static final String PROCESSING_FAILURE_MSG_TEMPLATE =
            "Exceptional condition when trying to process '%s'.";

    @Private
    static final String INTERRPTED_MSG = "Thread has been interrupted - exiting worker.";

    @Private
    static final String EXITING_MSG = "No more items to process - exiting worker.";

    @Private
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ParallelizedWorker.class);

    private final int retriesNumberWhenExecutionFails;

    private final boolean stopOnFirstFailure;

    private final Queue<T> workerQueue;

    private final Collection<FailureRecord<T>> failures;

    private final ITaskExecutor<T> taskExecutor;

    private final AtomicInteger activeWorkers;

    ParallelizedWorker(final Queue<T> incommingQueue, final Collection<FailureRecord<T>> failures,
            final ITaskExecutor<T> taskExecutor, final AtomicInteger activeWorkers,
            int retriesNumberWhenExecutionFails, boolean stopOnFirstFailure)
    {
        assert incommingQueue != null;
        assert failures != null;
        assert taskExecutor != null;
        assert activeWorkers != null;
        assert activeWorkers.get() > 0;

        this.workerQueue = incommingQueue;
        this.failures = failures;
        this.taskExecutor = taskExecutor;
        this.activeWorkers = activeWorkers;
        this.retriesNumberWhenExecutionFails = retriesNumberWhenExecutionFails;
        this.stopOnFirstFailure = stopOnFirstFailure;
    }

    @Override
    public void run()
    {
        try
        {
            do
            {
                if (Thread.interrupted())
                {
                    operationLog.info(INTERRPTED_MSG);
                    return;
                }
                final T taskOrNull = workerQueue.poll();
                if (taskOrNull == null)
                {
                    operationLog.debug(EXITING_MSG);
                    return;
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(PROGRESS_MSG_TEMPLATE, taskOrNull));
                }
                Status status = null;
                int count = 0;
                do
                {
                    if (Thread.interrupted())
                    {
                        operationLog.info(INTERRPTED_MSG);
                        return;
                    }
                    try
                    {
                        status = taskExecutor.execute(taskOrNull);
                    } catch (final InterruptedExceptionUnchecked iex)
                    {
                        operationLog.info(INTERRPTED_MSG);
                        return;
                    } catch (final Throwable th)
                    {
                        if (Thread.interrupted())
                        {
                            operationLog.info(INTERRPTED_MSG);
                            return;
                        }
                        operationLog.error(
                                String.format(PROCESSING_FAILURE_MSG_TEMPLATE, taskOrNull), th);
                        failures.add(new FailureRecord<T>(taskOrNull, th));
                        status = null;
                        break;
                    }
                    if (operationLog.isDebugEnabled())
                    {
                        logErrors(status);
                    }
                } while (StatusFlag.RETRIABLE_ERROR.equals(status.getFlag())
                        && ++count < retriesNumberWhenExecutionFails);
                if (status != null && Status.OK.equals(status) == false)
                {
                    logErrors(status);
                    failures.add(new FailureRecord<T>(taskOrNull, status));
                    if (stopOnFirstFailure)
                    {
                        return; // finish the thread
                    }
                }
            } while (true);
        } finally
        {
            // if there are no remaining threads working notify main executor thread that
            // is waiting for all failures.
            if (0 == activeWorkers.decrementAndGet())
            {
                synchronized (failures)
                {
                    failures.notify();
                }
            }
        }
    }

    private void logErrors(Status status)
    {
        if (status.isError())
        {
            operationLog.error(status);
        }
    }

}
