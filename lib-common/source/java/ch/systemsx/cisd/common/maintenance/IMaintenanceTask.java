/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.Properties;

/**
 * The interface that should be implemented by all maintenance tasks. Task instances are reusable.
 * <p>
 * If your maintenance task needs cleanup, additionally implement the {@link java.io.Closeable} interface and put the cleanup tasks in
 * {@link java.io.Closeable#close()}.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaintenanceTask
{
    /**
     * Prepares the task for execution and checks that it has been configured correctly. It will be called exactly once immediately after the task is
     * created.
     * 
     * @param pluginName Name of the plugin. Useful for creating messages.
     * @param properties Properties to set up the task.
     */
    public void setUp(String pluginName, Properties properties);

    /**
     * Performs the maintenance task. This method is reusable - it will be called many times every certain time period.
     */
    public void execute();
}
