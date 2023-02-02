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

package ch.systemsx.cisd.common.process;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.io.CollectionIO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * Test cases for the {@link ProcessExecutionHelper}.
 * 
 * @author Bernd Rinn
 */
@Listeners(TestReportCleaner.class)
public class ProcessExecutionHelperTest
{

    private static final long WATCHDOG_WAIT_MILLIS = 1000L;

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            ProcessExecutionHelperTest.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProcessExecutionHelperTest.class);

    private static final File unitTestRootDirectory = new File("targets" + File.separator
            + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory,
            "ProcessExecutionHelperTest");

    private File createExecutable(String name, String... lines) throws IOException,
            InterruptedException
    {
        final File executable = new File(workingDirectory, name);
        executable.delete();
        CollectionIO.writeIterable(executable, Arrays.asList(lines));
        Thread.interrupted(); // Paranoia: clear interrupted state.
        Runtime.getRuntime().exec(String.format("/bin/chmod +x %s", executable.getPath()))
                .waitFor();
        executable.deleteOnExit();
        return executable;
    }

    private File createExecutable(String name, int exitValue) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "exit " + exitValue);
    }

    private final String sleepyMessage = "I am feeling sooo sleepy...";

    private File createSleepingExecutable(String name, long millisToSleep) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "echo " + sleepyMessage, "sleep "
                + (millisToSleep / 1000.0f), "exit 0");
    }

    private File createExecutableEndlessLoop(String name) throws IOException, InterruptedException
    {
        return createExecutable(name, "#! /bin/sh",
                "n=1; while [ 1 ]; do echo $n; n=$(($n+1)); done");
    }

    private File createExecutableEndlessDevURandom(String name) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "cat < /dev/urandom");
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        workingDirectory.delete();
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    @BeforeMethod
    @AfterClass
    public void clearThreadInterruptionState()
    {
        Thread.interrupted();
    }

    @Test(groups =
    { "requires_unix" })
    public void testExecutionOKWithoutTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyOKWithoutTimeOut.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog);
        assertTrue(ok);
    }

    @Test(groups =
    { "requires_unix" })
    public void testExecutionFailedWithoutTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyFailingWithoutTimeOut.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog);
        assertFalse(ok);
    }

    @Test(groups =
    { "requires_unix", "flaky" })
    public void testExecutionOKWithTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyOKWithTimeOut.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(ok);
    }

    @Test(groups =
    { "requires_unix" })
    public void testExecutionOKSameThreadWithTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyOKSameThreadWithTimeOut.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                        ProcessIOStrategy.TEXT_SAME_THREAD_IO_STRATEGY);
        assertTrue(ok);
    }

    @Test(groups =
    { "requires_unix", "flaky" })
    public void testExecutionEnvNoReplaceEnviron() throws Exception
    {
        final File dummyExec = createExecutable("printEnv.sh", "env");
        final Map<String, String> env = new HashMap<String, String>();
        env.put("bla", "blub");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), env,
                        false, operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                        ProcessIOStrategy.DEFAULT_IO_STRATEGY, true);
        assertTrue(result.isOK());
        boolean found = false;
        for (String ln : result.getOutput())
        {
            if (ln.equals("bla=blub"))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test(groups =
    { "requires_unix", "flaky" })
    public void testExecutionEnvReplaceEnviron() throws Exception
    {
        final File dummyExec = createExecutable("printEnv.sh", "env");
        final Map<String, String> env = new HashMap<String, String>();
        env.put("bla", "blub");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), env,
                        true, operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                        ProcessIOStrategy.DEFAULT_IO_STRATEGY, true);
        assertTrue(result.isOK());
        assertEquals(2, result.getOutput().size());
        boolean found = false;
        for (String ln : result.getOutput())
        {
            if (ln.equals("bla=blub"))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test(groups =
    { "requires_unix" })
    public void testExecutionFailedWithTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyFailingWithTimeOut.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertFalse(ok);
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testSleepyExecutionOKWithTimeOut() throws Exception
    {
        final File dummyExec =
                createSleepingExecutable("dummySleepyOKWithTimeOut.sh", WATCHDOG_WAIT_MILLIS / 2);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(result.isOK());
        assertEquals(1, result.getOutput().size());
        assertEquals(sleepyMessage, result.getOutput().get(0));
    }

    @Test(groups =
    { "requires_unix", "slow" }, expectedExceptions =
    { InterruptedExceptionUnchecked.class }, retryAnalyzer = RetryTen.class)
    public void testSleepyExecutionGetsStopped() throws Exception
    {
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        thisThread.interrupt();
                    }
                }, WATCHDOG_WAIT_MILLIS / 10);
            final File dummyExec =
                    createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                            2 * WATCHDOG_WAIT_MILLIS);
            ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), operationLog,
                    machineLog, WATCHDOG_WAIT_MILLIS);
        } finally
        {
            timer.cancel();
        }
    }

    @Test(groups =
    { "requires_unix", "slow" }, expectedExceptions =
    { InterruptedExceptionUnchecked.class })
    public void testSleepyExecutionGetsStoppedUsingRunUnblocking() throws Exception
    {
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        thisThread.interrupt();
                    }
                }, WATCHDOG_WAIT_MILLIS / 10);
            final File dummyExec =
                    createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                            2 * WATCHDOG_WAIT_MILLIS);
            final IProcessHandler processHandler =
                    ProcessExecutionHelper.runNonblocking(
                            Arrays.asList(dummyExec.getAbsolutePath()), operationLog, machineLog,
                            ProcessIOStrategy.DEFAULT_IO_STRATEGY, null, null);
            processHandler.getResult();
        } finally
        {
            timer.cancel();
        }
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testSleepyExecutionGetsInterrupted() throws Exception
    {
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        thisThread.interrupt();
                    }
                }, WATCHDOG_WAIT_MILLIS / 10);
            final File dummyExec =
                    createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                            2 * WATCHDOG_WAIT_MILLIS);
            final ProcessResult result =
                    ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                            operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                            ProcessIOStrategy.DEFAULT_IO_STRATEGY, false);
            assertTrue(result.isInterruped());
        } finally
        {
            timer.cancel();
        }
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testSleepyExecutionFailedWithTimeOut() throws Exception
    {
        final File dummyExec =
                createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                        2 * WATCHDOG_WAIT_MILLIS);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(result.isTimedOut());
        assertFalse(result.isOK());
        assertEquals(1, result.getOutput().size());
        assertEquals(sleepyMessage, result.getOutput().get(0));
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testSleepyExecutionFailedWithIOTimeOut() throws Exception
    {
        final File dummyExec =
                createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                        2 * WATCHDOG_WAIT_MILLIS);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS, false, true);
        assertTrue(result.isTimedOut());
        assertFalse(result.isOK());
        assertEquals(1, result.getOutput().size());
        assertEquals(sleepyMessage, result.getOutput().get(0));
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testHangingExecLotsOfOutputOnStdOut() throws Exception
    {
        final File dummyExec = createExecutableEndlessLoop("iHang.sh");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(result.isTimedOut());
        assertFalse(result.isOK());
        assertTrue(Integer.toString(result.getOutput().size()), result.getOutput().size() > 100);
        assertEquals(Integer.toString(result.getOutput().size()),
                result.getOutput().get(result.getOutput().size() - 1));
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testHangingExecLotsOfOutputOnStdOutWaitForNoIO() throws Exception
    {
        final File dummyExec = createExecutableEndlessLoop("iHang.sh");
        final Thread mainThread = Thread.currentThread();
        final Runnable interrupt = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(WATCHDOG_WAIT_MILLIS * 2);
                } catch (InterruptedException e)
                {
                }
                mainThread.interrupt();
            }
        };
        new Thread(interrupt).start();
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS, true, false);
        assertTrue(result.isInterruped());
        assertFalse(result.isTimedOut());
        assertFalse(result.isOK());
        assertTrue(Integer.toString(result.getOutput().size()), result.getOutput().size() > 100);
        assertEquals(Integer.toString(result.getOutput().size()),
                result.getOutput().get(result.getOutput().size() - 1));
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutput() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.DEFAULT_IO_STRATEGY, false);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertFalse(result.isBinaryOutput());
        assertEquals(4, result.getOutput().size());
        assertEquals(stdout1, result.getOutput().get(0));
        assertEquals(stderr1, result.getOutput().get(1));
        assertEquals(stdout2, result.getOutput().get(2));
        assertEquals(stderr2, result.getOutput().get(3));
        assertEquals(0, result.getExitValue());
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutputWithIOTimeout() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "sleep 0.5", "echo " + stderr1
                        + " > /dev/stderr", "sleep 0.5", "echo " + stdout2, "sleep 3", 
                        "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS, true,
                        ProcessIOStrategy.DEFAULT_IO_STRATEGY, false);
        result.log();
        assertFalse(result.isOK());
        assertTrue(result.isTimedOut());
        assertTrue(result.isOutputAvailable());
        assertFalse(result.isBinaryOutput());
        assertEquals(3, result.getOutput().size());
        assertEquals(stdout1, result.getOutput().get(0));
        assertEquals(stderr1, result.getOutput().get(1));
        assertEquals(stdout2, result.getOutput().get(2));
        assertTrue(result.getExitValue() != 0);
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutputDiscardStderr() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.TEXT_DISCARD_STDERR_IO_STRATEGY, false);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertFalse(result.isBinaryOutput());
        assertEquals(2, result.getOutput().size());
        assertEquals(stdout1, result.getOutput().get(0));
        assertEquals(stdout2, result.getOutput().get(1));
        assertEquals(0, result.getExitValue());
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutputDiscardStdout() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.DISCARD_STDOUT_IO_STRATEGY, false);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertFalse(result.isBinaryOutput());
        assertEquals(0, result.getOutput().size());
        assertEquals(2, result.getErrorOutput().size());
        assertEquals(stderr1, result.getErrorOutput().get(0));
        assertEquals(stderr2, result.getErrorOutput().get(1));
        assertEquals(0, result.getExitValue());
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutputDiscardStdoutSameThread() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.DISCARD_STDOUT_SAME_THREAD_IO_STRATEGY, false);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertFalse(result.isBinaryOutput());
        assertEquals(0, result.getOutput().size());
        assertEquals(2, result.getErrorOutput().size());
        assertEquals(stderr1, result.getErrorOutput().get(0));
        assertEquals(stderr2, result.getErrorOutput().get(1));
        assertEquals(0, result.getExitValue());
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadBinaryProcessOutput() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.BINARY_IO_STRATEGY, false);
        final int exitValue = result.getExitValue();
        assertEquals(0, exitValue);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertTrue(result.isBinaryOutput());
        assertEquals(2, result.getErrorOutput().size());
        assertEquals(stderr1, result.getErrorOutput().get(0));
        assertEquals(stderr2, result.getErrorOutput().get(1));
        final String stdout = new String(result.getBinaryOutput());
        assertEquals(stdout1 + "\n" + stdout2 + "\n", stdout);
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testTryExecutionReadBinaryProcessOutputSameThread() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1
                        + " > /dev/stderr", "echo " + stdout2, "echo " + stderr2 + " > /dev/stderr");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.BINARY_SAME_THREAD_IO_STRATEGY, false);
        final int exitValue = result.getExitValue();
        assertEquals(0, exitValue);
        result.log();
        assertTrue(result.isOK());
        assertTrue(result.isOutputAvailable());
        assertTrue(result.isBinaryOutput());
        assertEquals(2, result.getErrorOutput().size());
        assertEquals(stderr1, result.getErrorOutput().get(0));
        assertEquals(stderr2, result.getErrorOutput().get(1));
        final String stdout = new String(result.getBinaryOutput());
        assertEquals(stdout1 + "\n" + stdout2 + "\n", stdout);
    }

    @Test(groups =
    { "requires_unix", "slow" })
    public void testHangingExecLotsOfBinaryOutputOnStdOut() throws Exception
    {
        final File dummyExec = createExecutableEndlessDevURandom("iHangOnUrandom.sh");
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                        ProcessIOStrategy.BINARY_SAME_THREAD_IO_STRATEGY, false);
        assertTrue(result.isTimedOut());
        assertFalse(result.isOK());
        assertTrue(result.getBinaryOutput().length > 1000);
    }

    @Test(groups = "requires_unix")
    public void testStartupFailed()
    {
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList("some_non_existent_executable"),
                        operationLog, machineLog);
        result.log();
        assertFalse(result.isRun());
        assertTrue(result.getStartupFailureMessage(),
                result.getStartupFailureMessage().indexOf("some_non_existent_executable") >= 0);
    }

    @Test(groups =
    { "requires_unix", "slow", "flaky" })
    public void testSleepyExecutionWithTermination() throws Exception
    {
        final File dummyExec = createSleepingExecutable("sleep.sh", 2 * WATCHDOG_WAIT_MILLIS);
        final IProcessHandler handler =
                ProcessExecutionHelper.runNonblocking(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ProcessIOStrategy.DEFAULT_IO_STRATEGY, null, null);
        final AtomicReference<ProcessResult> result = new AtomicReference<ProcessResult>(null);
        final Runnable resultGetter = new Runnable()
            {
                @Override
                public void run()
                {
                    result.set(handler.getResult());
                }
            };
        new Thread(resultGetter).start();
        Thread.sleep(WATCHDOG_WAIT_MILLIS / 2);
        boolean terminated = handler.terminate();
        assertTrue(terminated);
        Thread.sleep(WATCHDOG_WAIT_MILLIS / 20);
        // Now resultGetter should be done with obtaining the result.
        ProcessResult processResult = result.get();
        assertTrue(processResult != null);
        assertFalse(processResult.isOK()); // process terminated unsuccessfully
        assertTrue(processResult.isInterruped() || processResult.isTerminated());
    }
}
