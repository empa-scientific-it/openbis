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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.base.tests.Retry50;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy.IMonitorCommunicator;
import ch.systemsx.cisd.common.logging.AssertingLogger;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * Test cases for the {@link MonitoringProxy}.
 * 
 * @author Bernd Rinn
 */
@Test(groups = "flaky")
public class MonitoringProxyTest
{

    private static final String THREAD_NAME = "Some Flaky Stuff";

    private static final String THE_STRING = "some string";

    private static final boolean THE_BOOLEAN = true;

    private static final int THE_INTEGER = 17;

    private static final Status THE_STATUS = Status.TWO;

    private static final long TIMEOUT_MILLIS = 2000L;

    private volatile Thread threadToStop;

    private RecordingActivityObserverSensor observerSensor;

    private ITest defaultReturningProxy;

    private ITest exceptionThrowingProxy;

    private AssertingLogger asyncLogger;

    private ITest asynchronouslyExecutingProxy;

    private ITest retryingOnceExceptionThrowingProxy;

    private ITest retryingTwiceExceptionThrowingProxy;

    private ITest nonDefaultExecutorServiceProxy;

    private static class SignalException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    private static class RetryItException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    enum Status
    {
        ONE, TWO, THREE, UUUPS, SPECIAL_UUUPS
    }

    interface ITest
    {
        void idle(boolean hang);

        void idle(long[] idleTimes);

        void busyUpdatingActivity();

        void busyUpdatingActivity(IMonitorCommunicator communicator);

        String getString(boolean hang);

        String getThreadName();

        boolean getBoolean(boolean hang);

        int getInteger(boolean hang);

        Status getStatus(boolean hang);

        Status getSpecialStatus(boolean hang);

        void throwSignalException() throws SignalException;

        void worksOnSecondInvocation() throws RetryItException;

        void resetInvocationsCancelled();

        int getInvocationsCancelled();

        void worksOnSecondInvocation(IMonitorCommunicator communicator) throws RetryItException;

        void worksOnThirdInvocation() throws RetryItException;

    }

    private final static Pattern THREAD_NAME_PATTERN = Pattern
            .compile("Monitoring Proxy-T[0-9]+::main::" + THREAD_NAME);

    private final static Pattern MY_SPECIAL_THREAD_NAME_PATTERN = Pattern
            .compile("My Special Monitoring Proxy-T[0-9]+::main::" + THREAD_NAME);

    private class TestImpl implements ITest
    {
        private final IActivityObserver observer;

        private final boolean checkThreadName;

        TestImpl(IActivityObserver observer, boolean checkThreadName)
        {
            this.observer = observer;
            this.checkThreadName = checkThreadName;
        }

        private void hang(boolean hang)
        {
            if (hang)
            {
                threadToStop = Thread.currentThread();
                try
                {
                    while (true)
                    {
                    }
                } finally
                {
                    threadToStop = null;
                }
            }
        }

        private void checkThreadName()
        {
            if (checkThreadName == false)
            {
                return;
            }
            final String name = Thread.currentThread().getName();
            assertTrue(name, THREAD_NAME_PATTERN.matcher(name).matches());
        }

        @Override
        public void idle(boolean hang)
        {
            checkThreadName();
            hang(hang);
        }

        int invocationCount4 = 0;

        @Override
        public void idle(long[] idleTimes)
        {
            checkThreadName();
            ConcurrencyUtilities.sleep(idleTimes[invocationCount4++]);
        }

        @Override
        public void busyUpdatingActivity()
        {
            checkThreadName();
            threadToStop = Thread.currentThread();
            try
            {
                final long timeToHangMillis = (long) (TIMEOUT_MILLIS * 1.5);
                final long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < timeToHangMillis)
                {
                    observer.update();
                }
            } finally
            {
                threadToStop = null;
            }
        }

        @Override
        public void busyUpdatingActivity(IMonitorCommunicator communicator)
        {
            checkThreadName();
            threadToStop = Thread.currentThread();
            try
            {
                final long timeToHangMillis = (long) (TIMEOUT_MILLIS * 1.5);
                final long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < timeToHangMillis)
                {
                    if (communicator.isCancelled())
                    {
                        return;
                    }
                    communicator.update();
                }
            } finally
            {
                threadToStop = null;
            }
        }

        @Override
        public boolean getBoolean(boolean hang)
        {
            checkThreadName();
            hang(hang);
            return THE_BOOLEAN;
        }

        @Override
        public int getInteger(boolean hang)
        {
            checkThreadName();
            hang(hang);
            return THE_INTEGER;
        }

        @Override
        public String getString(boolean hang)
        {
            checkThreadName();
            hang(hang);
            return THE_STRING;
        }

        @Override
        public Status getStatus(boolean hang)
        {
            checkThreadName();
            hang(hang);
            return THE_STATUS;
        }

        @Override
        public Status getSpecialStatus(boolean hang)
        {
            checkThreadName();
            hang(hang);
            return THE_STATUS;
        }

        @Override
        public void throwSignalException() throws SignalException
        {
            checkThreadName();
            throw new SignalException();
        }

        int invocationCount1 = 0;

        @Override
        public void worksOnSecondInvocation() throws RetryItException
        {
            checkThreadName();
            if (++invocationCount1 < 2)
            {
                throw new RetryItException();
            }
        }

        int invocationCount2 = 0;

        AtomicInteger invocationsCancelled = new AtomicInteger();

        @Override
        public int getInvocationsCancelled()
        {
            return invocationsCancelled.get();
        }

        @Override
        public void resetInvocationsCancelled()
        {
            invocationsCancelled.set(0);
        }

        @Override
        public void worksOnSecondInvocation(IMonitorCommunicator communicator)
                throws RetryItException
        {
            checkThreadName();
            if (++invocationCount2 < 2)
            {
                try
                {
                    ConcurrencyUtilities.sleep(TIMEOUT_MILLIS * 3);
                } finally
                {
                    if (communicator.isCancelled())
                    {
                        invocationsCancelled.incrementAndGet();
                    }
                }
            }
        }

        int invocationCount3 = 0;

        @Override
        public void worksOnThirdInvocation() throws RetryItException
        {
            checkThreadName();
            if (++invocationCount3 < 3)
            {
                throw new RetryItException();
            }
        }

        @Override
        public String getThreadName()
        {
            return Thread.currentThread().getName();
        }

    }

    @BeforeClass
    public void createMonitoringProxies() throws NoSuchMethodException
    {
        final ISimpleLogger logger = new ConsoleLogger();
        observerSensor = new RecordingActivityObserverSensor();
        defaultReturningProxy =
                MonitoringProxy
                        .create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.createNoRetries(TIMEOUT_MILLIS))
                        .errorValueOnTimeout()
                        .name(THREAD_NAME)
                        .errorTypeValueMapping(Status.class, Status.UUUPS)
                        .errorMethodValueMapping(
                                ITest.class.getMethod("getSpecialStatus", new Class<?>[]
                                { Boolean.TYPE }), Status.SPECIAL_UUUPS).sensor(observerSensor)
                        .errorLog(logger).get();
        final ExecutorService executorService =
                new NamingThreadPoolExecutor("My Special Monitoring Proxy").corePoolSize(1)
                        .daemonize();

        nonDefaultExecutorServiceProxy =
                MonitoringProxy
                        .create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.createNoRetries(TIMEOUT_MILLIS))
                        .errorValueOnTimeout()
                        .name(THREAD_NAME)
                        .errorTypeValueMapping(Status.class, Status.UUUPS)
                        .errorMethodValueMapping(
                                ITest.class.getMethod("getSpecialStatus", new Class<?>[]
                                { Boolean.TYPE }), Status.SPECIAL_UUUPS).sensor(observerSensor)
                        .errorLog(logger)
                        .executorService(executorService)
                        .get();
        exceptionThrowingProxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.createNoRetries(TIMEOUT_MILLIS)).name(THREAD_NAME)
                        .sensor(observerSensor).errorLog(logger)
                        .get();
        asyncLogger = new AssertingLogger();
        asynchronouslyExecutingProxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, false))
                        .name(THREAD_NAME)
                        .sensor(observerSensor)
                        .errorLog(asyncLogger)
                        .logLevelForSuccessfulCalls(LogLevel.INFO)
                        .timing(TimingParameters.create(TIMEOUT_MILLIS, 1, 0L))
                        .exceptionClassSuitableForRetrying(RetryItException.class)
                        .callAsynchronously(ITest.class.getMethod("idle", new Class<?>[]
                        { long[].class }))
                        .callAsynchronously(ITest.class.getMethod("throwSignalException"))
                        .callAsynchronously(ITest.class.getMethod("worksOnSecondInvocation"))
                        .callAsynchronously(ITest.class.getMethod("getInteger", new Class<?>[]
                        { Boolean.TYPE }))
                        .get();
        retryingOnceExceptionThrowingProxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.create(TIMEOUT_MILLIS, 1, 0L)).name(THREAD_NAME)
                        .sensor(observerSensor)
                        .exceptionClassSuitableForRetrying(RetryItException.class).errorLog(logger)
                        .get();
        retryingTwiceExceptionThrowingProxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.create(TIMEOUT_MILLIS, 2, 0L)).name(THREAD_NAME)
                        .sensor(observerSensor)
                        .exceptionClassSuitableForRetrying(RetryItException.class).errorLog(logger)
                        .get();
    }

    @SuppressWarnings("deprecation")
    @AfterMethod
    public void stopThread()
    {
        final Thread t = threadToStop;
        if (t != null)
        {
            t.stop();
        }
    }

    @BeforeMethod
    @AfterClass
    public void clearThreadInterruptionState()
    {
        Thread.interrupted();
    }

    @Test
    public void testVoid()
    {
        defaultReturningProxy.idle(false);
    }

    @Test
    public void testVoidTimeoutNoException()
    {
        defaultReturningProxy.idle(true);
    }

    @Test
    public void testNonDefaultExecutorService()
    {
        final String threadName = nonDefaultExecutorServiceProxy.getThreadName();
        assertTrue(threadName, MY_SPECIAL_THREAD_NAME_PATTERN.matcher(threadName).matches());
    }

    @Test(expectedExceptions = SignalException.class, retryAnalyzer = Retry50.class)
    public void testThrowExceptionNullReturningPolicy()
    {
        defaultReturningProxy.throwSignalException();
    }

    @Test
    public void testWorksOnSecondInvocationOnAsynchronouslyCalledMethod()
    {
        asyncLogger.reset();
        asynchronouslyExecutingProxy.worksOnSecondInvocation();
        for (int i = 0; i < 10; ++i)
        {
            if (asyncLogger.getNumberOfRecords() > 1)
            {
                break;
            }
            ConcurrencyUtilities.sleep(100L);
        }
        asyncLogger.assertNumberOfMessage(2);
        asyncLogger
                .assertEq(
                        0,
                        LogLevel.ERROR,
                        "Call to method 'ITest.worksOnSecondInvocation()'[Some Flaky Stuff]: exception: <no message> [RetryItException].");
        asyncLogger
                .assertEq(
                        1,
                        LogLevel.INFO,
                        "Call to method 'ITest.worksOnSecondInvocation()'[Some Flaky Stuff]: call returns null.");
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetIntegerAsynchronouslyCalledMethod()
    {
        asyncLogger.reset();
        assertEquals(1, asynchronouslyExecutingProxy.getInteger(false));
        for (int i = 0; i < 20; ++i)
        {
            if (asyncLogger.getNumberOfRecords() > 0)
            {
                break;
            }
            ConcurrencyUtilities.sleep(100L);
        }
        asyncLogger.assertNumberOfMessage(1);
        asyncLogger.assertEq(0, LogLevel.INFO,
                "Call to method 'ITest.getInteger(boolean)'[Some Flaky Stuff]: call returns 17.");
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testTimeoutOnAsynchronouslyCalledMethod()
    {
        asyncLogger.reset();
        final long start = System.currentTimeMillis();
        asynchronouslyExecutingProxy.idle(new long[]
        { 3000L, 0L });
        final long stop = System.currentTimeMillis();
        assertTrue(Long.toString(stop - start), (stop - start) < 20);
        for (int i = 0; i < 30; ++i)
        {
            if (asyncLogger.getNumberOfRecords() > 1)
            {
                break;
            }
            ConcurrencyUtilities.sleep(100L);
        }
        asyncLogger.assertNumberOfMessage(2);
        asyncLogger
                .assertEq(0, LogLevel.ERROR,
                        "Call to method 'ITest.idle(long[])'[Some Flaky Stuff]: timeout of 2.00 s exceeded, cancelled.");
        asyncLogger.assertEq(1, LogLevel.INFO,
                "Call to method 'ITest.idle(long[])'[Some Flaky Stuff]: call returns null.");
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testThrowExceptionOnAsynchronouslyCalledMethod()
    {
        asyncLogger.reset();
        asynchronouslyExecutingProxy.throwSignalException();
        for (int i = 0; i < 30; ++i)
        {
            if (asyncLogger.getNumberOfRecords() > 0)
            {
                break;
            }
            ConcurrencyUtilities.sleep(100L);
        }
        asyncLogger.assertNumberOfMessage(1);
        asyncLogger.assertEq(0, LogLevel.ERROR,
                "Call to method 'ITest.throwSignalException()'[Some Flaky Stuff]:" +
                        " exception: <no message> [SignalException].");
    }

    @Test(expectedExceptions = SignalException.class, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testThrowExceptionExceptionThrowsPolicy()
    {
        exceptionThrowingProxy.throwSignalException();
    }

    @Test(groups = "slow", expectedExceptions = TimeoutExceptionUnchecked.class, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testVoidTimeoutWithException()
    {
        exceptionThrowingProxy.idle(true);
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testNoTimeoutDueToSensorUpdate()
    {
        exceptionThrowingProxy.busyUpdatingActivity();
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testNoTimeoutDueToCommunicatorUpdate()
    {
        exceptionThrowingProxy.busyUpdatingActivity(null);
    }

    @Test(retryAnalyzer = Retry50.class)
    public void testGetStringNullReturningPolicy()
    {
        assertEquals(THE_STRING, defaultReturningProxy.getString(false));
    }

    @Test(retryAnalyzer = Retry50.class)
    public void testGetStringExceptionThrowingPolicy()
    {
        assertEquals(THE_STRING, exceptionThrowingProxy.getString(false));
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class)
    public void testGetStringTimeoutNoException()
    {
        assertNull(defaultReturningProxy.getString(true));
    }

    @Test(groups = "slow", expectedExceptions = TimeoutExceptionUnchecked.class, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetStringTimeoutWithException()
    {
        exceptionThrowingProxy.getString(true);
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetIntNullReturningPolicy()
    {
        assertEquals(THE_INTEGER, defaultReturningProxy.getInteger(false));
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetIntExceptionThrowingPolicy()
    {
        assertEquals(THE_INTEGER, exceptionThrowingProxy.getInteger(false));
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetBoolTimeoutReturnsDefault()
    {
        assertEquals(false, defaultReturningProxy.getBoolean(true));
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetStatus()
    {
        assertEquals(THE_STATUS, defaultReturningProxy.getStatus(false));
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetStatusTimeoutReturnsDefault()
    {
        assertEquals(Status.UUUPS, defaultReturningProxy.getStatus(true));
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetSpecialStatusTimeoutReturnsMethodDefault()
    {
        assertEquals(Status.SPECIAL_UUUPS, defaultReturningProxy.getSpecialStatus(true));
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetIntTimeoutReturnsDefault()
    {
        assertEquals(0, defaultReturningProxy.getInteger(true));
    }

    @Test(groups = "slow", expectedExceptions = TimeoutExceptionUnchecked.class, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testGetIntTimeoutWithException()
    {
        exceptionThrowingProxy.getInteger(true);
    }

    @Test(groups = "slow", expectedExceptions = InterruptedExceptionUnchecked.class, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testInterruptTheUninterruptableThrowsException()
    {
        final ITest proxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.create(1000L)).name(THREAD_NAME).get();
        final Thread currentThread = Thread.currentThread();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    currentThread.interrupt();
                }
            }, 50L);
        // This call would not be interruptible if it wasn't proxied, but we get a StopException
        // from the proxy.
        proxy.idle(true);
        timer.cancel();
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testInterruptTheUninterruptableReturnsDefaultValue()
    {
        final String defaultReturnValue = "That's the default return value.";
        final ITest proxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.create(1000L)).name(THREAD_NAME)
                        .errorValueOnInterrupt()
                        .errorTypeValueMapping(String.class, defaultReturnValue).get();
        final Thread currentThread = Thread.currentThread();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    currentThread.interrupt();
                }
            }, 50L);
        // This call would not be interruptable if it wasn't proxied, but we get the default return
        // value for Strings here.
        assertEquals(defaultReturnValue, proxy.getString(true));
        timer.cancel();
    }

    @Test(expectedExceptions = RetryItException.class)
    public void testNoRetryFailOnce()
    {
        exceptionThrowingProxy.worksOnSecondInvocation();
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testRetryOnceFailOnce()
    {
        retryingOnceExceptionThrowingProxy.worksOnSecondInvocation();
    }

    @Test(groups =
    { "slow" }, retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testRetryOnceFailOnceWithCommunicator()
    {
        retryingOnceExceptionThrowingProxy.resetInvocationsCancelled();
        retryingOnceExceptionThrowingProxy
                .worksOnSecondInvocation(MonitoringProxy.MONITOR_COMMUNICATOR);
        ConcurrencyUtilities.sleep(TIMEOUT_MILLIS);
        assertEquals(1, retryingOnceExceptionThrowingProxy.getInvocationsCancelled());
    }

    @Test(expectedExceptions = RetryItException.class, retryAnalyzer = Retry50.class, groups = "broken")
    public void testRetryOnceFailTwice()
    {
        retryingOnceExceptionThrowingProxy.worksOnThirdInvocation();
    }

    @Test(groups = "slow", retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testRetryTwiceFailTwice()
    {
        retryingTwiceExceptionThrowingProxy.worksOnThirdInvocation();
    }

    @Test(retryAnalyzer = Retry50.class, successPercentage = 2)
    public void testInvocationLog()
    {
        final List<ExecutionResult<Object>> results = new ArrayList<ExecutionResult<Object>>();
        final List<Boolean> retryFlags = new ArrayList<Boolean>();
        final ITest proxy =
                MonitoringProxy.create(ITest.class, new TestImpl(observerSensor, true))
                        .timing(TimingParameters.create(TIMEOUT_MILLIS, 2, 0L)).name(THREAD_NAME)
                        .sensor(observerSensor)
                        .exceptionClassSuitableForRetrying(RetryItException.class)
                        .invocationLog(new IMonitoringProxyLogger()
                            {
                                @Override
                                public void log(Method method, ExecutionResult<Object> result,
                                        boolean willRetry)
                                {
                                    results.add(result);
                                    retryFlags.add(willRetry);
                                }
                            }).get();
        proxy.worksOnThirdInvocation();
        assertEquals(Arrays.asList(true, true, false), retryFlags);
        assertEquals(3, results.size());
        assertEquals(ExecutionStatus.EXCEPTION, results.get(0).getStatus());
        assertTrue(results.get(0).tryGetException() instanceof RetryItException);
        assertEquals(ExecutionStatus.EXCEPTION, results.get(1).getStatus());
        assertTrue(results.get(1).tryGetException() instanceof RetryItException);
        assertEquals(ExecutionStatus.COMPLETE, results.get(2).getStatus());
    }

}
