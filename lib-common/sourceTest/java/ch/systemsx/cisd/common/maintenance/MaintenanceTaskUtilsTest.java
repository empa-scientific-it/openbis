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

package ch.systemsx.cisd.common.maintenance;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class MaintenanceTaskUtilsTest extends AssertJUnit
{
    private static final int NUM_CONTENDING_TASKS = 5;

    private static final AtomicBoolean executedParallely = new AtomicBoolean(false);

    private static final CountDownLatch latch = new CountDownLatch(NUM_CONTENDING_TASKS);

    public static class ResourceContendingTask implements IDataStoreLockingMaintenanceTask
    {
        private static final AtomicInteger numberActive = new AtomicInteger(0);

        @Override
        public void setUp(String pluginName, Properties properties)
        {
        }

        @Override
        public void execute()
        {
            if (numberActive.incrementAndGet() > 1)
            {
                executedParallely.set(true);
            }
            try
            {
                // simulate some activity
                Thread.sleep(300);
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
            } finally
            {
                numberActive.decrementAndGet();
                latch.countDown();
            }

        }

        /**
         * @see IDataStoreLockingMaintenanceTask#requiresDataStoreLock()
         */
        @Override
        public boolean requiresDataStoreLock()
        {
            return true;
        }
    }

    @Test
    public void testStartupMaintenancePlugins() throws Exception
    {
        List<MaintenancePlugin> plugins = null;

        try
        {
            MaintenanceTaskParameters[] tasks = new MaintenanceTaskParameters[NUM_CONTENDING_TASKS];
            for (int i = 0; i < tasks.length; i++)
            {
                tasks[i] = createTaskParameters("Task-" + i);
            }

            plugins = MaintenanceTaskUtils.startupMaintenancePlugins(tasks);

            // wait for all maintenance tasks to finish
            latch.await();

            assertFalse("Tasks competing for the same system resource should "
                    + "not be executed in parallel", executedParallely.get());
        } finally
        {
            if (plugins != null)
            {
                MaintenanceTaskUtils.shutdownMaintenancePlugins(plugins);
            }
        }
    }

    private MaintenanceTaskParameters createTaskParameters(String pluginName)
    {
        Properties props = new Properties();
        props.put(MaintenanceTaskParameters.CLASS_KEY, ResourceContendingTask.class.getName());
        props.put(MaintenanceTaskParameters.ONE_TIME_EXECUTION_KEY, true);
        props.put(MaintenanceTaskParameters.START_KEY, System.currentTimeMillis());
        return new MaintenanceTaskParameters(props, pluginName);
    }

}
