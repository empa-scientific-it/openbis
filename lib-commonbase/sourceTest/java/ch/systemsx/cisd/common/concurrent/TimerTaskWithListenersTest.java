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
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class TimerTaskWithListenersTest
{
    private static final class MockTimerTask extends TimerTask
    {
        private final List<String> recorder;

        private final boolean throwException;

        MockTimerTask(boolean throwException, List<String> recorder)
        {
            this.throwException = throwException;
            this.recorder = recorder;
        }

        @Override
        public void run()
        {
            recorder.add("run");
            if (throwException)
            {
                throw new RuntimeException("exception");
            }
        }

        @Override
        public boolean cancel()
        {
            recorder.add("cancel");
            return false;
        }
    }

    private static final class MockTimerTaskListener implements ITimerTaskListener
    {
        private final List<String> recorder;

        private final String name;

        MockTimerTaskListener(String name, List<String> recorder)
        {
            this.name = name;
            this.recorder = recorder;
        }

        @Override
        public void canceling()
        {
            recorder.add(name + ".canceling");
        }

        @Override
        public void finishRunning(ITimerTaskStatusProvider statusProviderOrNull)
        {
            recorder.add(name + ".finishRunning");
        }

        @Override
        public void startRunning()
        {
            recorder.add(name + ".startRunning");
        }
    }

    @Test
    public void testRun()
    {
        ArrayList<String> recorder = new ArrayList<String>();
        TimerTaskWithListeners task = new TimerTaskWithListeners(new MockTimerTask(false, recorder));
        task.addListener(new MockTimerTaskListener("a", recorder));
        task.addListener(new MockTimerTaskListener("b", recorder));

        task.run();

        assertEquals("[a.startRunning, b.startRunning, run, a.finishRunning, b.finishRunning]",
                recorder.toString());
    }

    @Test
    public void testRunWithException()
    {
        ArrayList<String> recorder = new ArrayList<String>();
        TimerTaskWithListeners task = new TimerTaskWithListeners(new MockTimerTask(true, recorder));
        task.addListener(new MockTimerTaskListener("a", recorder));
        task.addListener(new MockTimerTaskListener("b", recorder));

        try
        {
            task.run();
            fail("RuntimeException expected");
        } catch (RuntimeException e)
        {
            assertEquals("exception", e.getMessage());
        }

        assertEquals("[a.startRunning, b.startRunning, run, a.finishRunning, b.finishRunning]",
                recorder.toString());
    }

    @Test
    public void testCancel()
    {
        ArrayList<String> recorder = new ArrayList<String>();
        TimerTaskWithListeners task = new TimerTaskWithListeners(new MockTimerTask(false, recorder));
        task.addListener(new MockTimerTaskListener("a", recorder));
        task.addListener(new MockTimerTaskListener("b", recorder));

        task.cancel();

        assertEquals("[a.canceling, b.canceling, cancel]", recorder.toString());
    }

}
