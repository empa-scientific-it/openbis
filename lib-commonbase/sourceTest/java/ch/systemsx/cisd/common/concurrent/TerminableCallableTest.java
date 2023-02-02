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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.TerminableCallable.CanceledException;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.FinishCause;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallableCleaner;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;

/**
 * Test cases for {@link TerminableCallable}.
 * 
 * @author Bernd Rinn
 */
public class TerminableCallableTest
{

    enum Strategy
    {
        COMPLETE_IMMEDIATELY, SLEEP_FOREVER, KEEP_SPINNING, KEEP_SPINNING_STOPPABLE,
        THROW_EXCEPTION
    }

    private static class TestRunnable implements ICallableCleaner<Object>
    {
        final private CountDownLatch launchLatch;

        final private CountDownLatch milestoneLatch;

        final private CountDownLatch finishLatchOrNull;

        final private Strategy strategy;

        volatile FinishCause cause;

        volatile int cleanUpCount;

        public TestRunnable(CountDownLatch launchLatch, CountDownLatch milestoneLatch,
                Strategy strategy)
        {
            this(launchLatch, milestoneLatch, strategy, null);
        }

        public TestRunnable(CountDownLatch launchLatch, CountDownLatch milestoneLatch,
                Strategy strategy, CountDownLatch finishLatchOrNull)
        {
            this.launchLatch = launchLatch;
            this.milestoneLatch = milestoneLatch;
            this.strategy = strategy;
            this.finishLatchOrNull = finishLatchOrNull;
        }

        @Override
        public Object call(IStoppableExecutor<Object> executorForStoppableCode) throws Exception
        {
            launchLatch.countDown();
            switch (strategy)
            {
                case COMPLETE_IMMEDIATELY:
                {
                    milestoneLatch.countDown();
                    break;
                }
                case SLEEP_FOREVER:
                {
                    milestoneLatch.countDown();
                    Thread.sleep(Long.MAX_VALUE);
                    break;
                }
                case KEEP_SPINNING:
                {
                    milestoneLatch.countDown();
                    while (true)
                    {
                    }
                }
                case KEEP_SPINNING_STOPPABLE:
                {
                    executorForStoppableCode.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                milestoneLatch.countDown();
                                while (true)
                                {
                                }
                            }
                        });
                    break;
                }
                case THROW_EXCEPTION:
                {
                    milestoneLatch.countDown();
                    throw new RuntimeException("Something is wrong!");
                }
            }
            return null;
        }

        @Override
        public void cleanUp(FinishCause myCause)
        {
            ++cleanUpCount;
            this.cause = myCause;
            if (finishLatchOrNull != null)
            {
                finishLatchOrNull.countDown();
            }
        }

    }

    @Test
    public void testComplete() throws Exception
    {
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final CountDownLatch milestoneLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final TestRunnable sensor =
                new TestRunnable(launchLatch, milestoneLatch, Strategy.COMPLETE_IMMEDIATELY,
                        finishLatch);
        final TerminableCallable<Object> callableUnderTest = TerminableCallable.create(sensor);
        new Thread(callableUnderTest.asRunnable(), "complete").start();
        finishLatch.await(200L, TimeUnit.MILLISECONDS);
        assertTrue(milestoneLatch.await(0, TimeUnit.MILLISECONDS));
        assertTrue(describe(sensor.cause), FinishCause.COMPLETED.equals(sensor.cause));
        assertEquals(1, sensor.cleanUpCount);
    }

    @Test
    public void testCancel() throws Exception
    {
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final CountDownLatch milestoneLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final TestRunnable sensor =
                new TestRunnable(launchLatch, milestoneLatch, Strategy.COMPLETE_IMMEDIATELY,
                        finishLatch);
        final TerminableCallable<Object> callableUnderTest = TerminableCallable.create(sensor);
        callableUnderTest.cancel(false);
        final Thread t = new Thread(callableUnderTest.asRunnable(), "cancel");
        final AtomicReference<Throwable> uncaughtException = new AtomicReference<Throwable>(null);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread t2, Throwable e)
                {
                    uncaughtException.set(e);
                }
            });
        t.start();
        finishLatch.await(200L, TimeUnit.MILLISECONDS);
        assertFalse(milestoneLatch.await(0, TimeUnit.MILLISECONDS));
        assertNull(sensor.cause);
        assertEquals(0, sensor.cleanUpCount);
        assertEquals(CanceledException.class, uncaughtException.get().getClass());
    }

    @Test
    public void testInterrupt() throws Exception
    {
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final CountDownLatch milestoneLatch = new CountDownLatch(1);
        final TestRunnable sensor =
                new TestRunnable(launchLatch, milestoneLatch, Strategy.SLEEP_FOREVER);
        final TerminableCallable<Object> callableUnderTest = TerminableCallable.create(sensor);
        new Thread(callableUnderTest.asRunnable(), "interrupt").start();
        launchLatch.await();
        assertTrue(callableUnderTest.terminate(200L));
        assertTrue(milestoneLatch.await(0, TimeUnit.MILLISECONDS));
        assertTrue(describe(sensor.cause), FinishCause.INTERRUPTED.equals(sensor.cause));
        assertEquals(1, sensor.cleanUpCount);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTerminateFailed() throws Exception
    {
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final CountDownLatch milestoneLatch = new CountDownLatch(1);
        final TestRunnable sensor =
                new TestRunnable(launchLatch, milestoneLatch, Strategy.KEEP_SPINNING);
        final TerminableCallable<Object> callableUnderTest = TerminableCallable.create(sensor);
        final Thread t = new Thread(callableUnderTest.asRunnable(), "terminate failed");
        t.start();
        launchLatch.await();
        assertFalse(callableUnderTest.terminate(200L));
        assertTrue(milestoneLatch.await(0, TimeUnit.MILLISECONDS));
        assertNull(sensor.cause);
        assertEquals(0, sensor.cleanUpCount);
        t.stop();
    }

    @Test(invocationCount = 10)
    public void testThrowException() throws Exception
    {
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final CountDownLatch milestoneLatch = new CountDownLatch(1);
        final TestRunnable sensor =
                new TestRunnable(launchLatch, milestoneLatch, Strategy.THROW_EXCEPTION);
        final TerminableCallable<Object> callableUnderTest = TerminableCallable.create(sensor);
        final Thread t = new Thread(callableUnderTest.asRunnable(), "throw exception");
        t.start();
        launchLatch.await();
        milestoneLatch.await();
        assertTrue(callableUnderTest.terminate(200L));
        assertTrue(milestoneLatch.await(0, TimeUnit.MILLISECONDS));
        assertTrue(describe(sensor.cause), FinishCause.EXCEPTION.equals(sensor.cause));
        assertEquals(1, sensor.cleanUpCount);
    }

    private String describe(final FinishCause causeOrNull)
    {
        if (causeOrNull == null)
        {
            return "cleanUp() was not called";
        } else
        {
            return "cleanUp() called with cause: " + causeOrNull.toString();
        }
    }

}
