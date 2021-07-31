/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Tests of {@link ParallelizedExecutor}.
 * 
 * @author Tomasz Pylak
 */
public class ParallelizedExecutorTest extends AssertJUnit
{
    @Test
    public void testAllExecuted()
    {
        int itemsNum = 20;
        final long mainThreadId = getCurrentThreadId();
        final boolean executed[] = new boolean[itemsNum];
        List<Integer> items = createTaskItems(itemsNum);
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
            {
                @Override
                public Status execute(Integer item)
                {
                    if (executed[item])
                    {
                        fail("Invalid attempt to perform job on the same item twice: item " + item);
                    }
                    if (mainThreadId == getCurrentThreadId())
                    {
                        fail("Task is executed in the same thread");
                    }
                    work(item, 10);
                    executed[item] = true;
                    return Status.OK;
                }
            };
        Collection<FailureRecord<Integer>> errors = process(items, taskExecutor);
        assertEquals(0, errors.size());
        assertAllExecuted(executed);
    }

    private static class MyExecutor implements ITaskExecutor<Integer>
    {
        private ThreadLocal<Integer> executionCounter = new ThreadLocal<Integer>();

        public boolean executedToManyTimes = false; // this is shared among treads

        @Override
        public Status execute(Integer item)
        {
            Integer counter = executionCounter.get();
            if (counter == null)
            {
                counter = 0;
            }
            executionCounter.set(counter + 1);
            System.out.println("executed " + executionCounter);
            if (executionCounter.get() > 3)
            {
                executedToManyTimes = true;
            }
            return Status.createRetriableError();
        }

    }

    @Test
    public void testStopOnFailure()
    {
        List<Integer> items = createTaskItems(100);
        final int retriesNumberWhenExecutionFails = 3;
        MyExecutor taskExecutor = new MyExecutor();

        ParallelizedExecutor.process(items, taskExecutor, 1, 3, "test",
                retriesNumberWhenExecutionFails, true);
        assertFalse("Each thread should be called at most 3 times "
                + "(we are stopping on the first item which cannot be processed after retries)",
                taskExecutor.executedToManyTimes);
    }

    @Test
    public void testFailuresReported()
    {
        List<Integer> items = createTaskItems(100);
        items.add(0); // item 0 occurs twice
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
            {
                @Override
                public Status execute(Integer item)
                {
                    work(item, 10);
                    if (item.intValue() == 0)
                    {
                        return Status.createError();
                    } else
                    {
                        return Status.OK;
                    }
                }
            };
        Collection<FailureRecord<Integer>> errors = process(items, taskExecutor);
        assertEquals(2, errors.size());
        for (FailureRecord<Integer> error : errors)
        {
            assertEquals(0, error.getFailedItem().intValue());
        }
    }

    @Test
    public void testExecutedInTheSameThread()
    {
        final int numberOfTries = 3;
        List<Integer> items = createTaskItems(1);
        final long mainThreadId = getCurrentThreadId();
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
            {
                int tryNumber = 1;

                @Override
                public Status execute(Integer item)
                {
                    assertEquals(mainThreadId, getCurrentThreadId());
                    Status status = (tryNumber == 1) ? Status.createError() : Status.OK;
                    if (tryNumber > 2)
                    {
                        fail("To many retries");
                    }
                    tryNumber++;
                    return status;
                }
            };
        // there is one item to process and maxThreads is 1, so the operation should be performed in
        // the same thread
        Collection<FailureRecord<Integer>> errors =
                ParallelizedExecutor.process(items, taskExecutor, 1, 1, "test", numberOfTries,
                        false);
        assertEquals(0, errors.size());
    }

    @Test
    public void testExecutedInTheSameThreadWithFailure()
    {
        final int numberOfTries = 3;
        List<Integer> items = createTaskItems(5);
        final long mainThreadId = getCurrentThreadId();
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
        {
            @Override
            public Status execute(Integer item)
            {
                assertEquals(mainThreadId, getCurrentThreadId());
                work(item, 10);
                if (item.intValue() % 2 == 0)
                {
                    return Status.createError();
                } else
                {
                    return Status.OK;
                }
            }
        };
        Collection<FailureRecord<Integer>> errors =
                ParallelizedExecutor.process(items, taskExecutor, 1, 1, "test", numberOfTries, false);
        assertEquals(3, errors.size());
    }
    
    private long getCurrentThreadId()
    {
        return Thread.currentThread().getId();
    }

    private static void work(Integer item, int timeMsec)
    {
        try
        {
            synchronized (item)
            {
                // System.out.println("working on "+item);
                item.wait(timeMsec);
            }
        } catch (InterruptedException ex)
        {
        }
    }

    private Collection<FailureRecord<Integer>> process(List<Integer> items,
            ITaskExecutor<Integer> taskExecutor)
    {
        return ParallelizedExecutor.process(items, taskExecutor, 10, 10, "test", 1, false);
    }

    private static void assertAllExecuted(final boolean[] executed)
    {
        for (int i = 0; i < executed.length; i++)
        {
            if (executed[i] == false)
            {
                fail("Job not executed for item " + i);
            }
        }
    }

    private static List<Integer> createTaskItems(int itemsNum)
    {
        List<Integer> items = new ArrayList<Integer>();
        for (int i = 0; i < itemsNum; i++)
        {
            items.add(i);
        }
        return items;
    }

}
