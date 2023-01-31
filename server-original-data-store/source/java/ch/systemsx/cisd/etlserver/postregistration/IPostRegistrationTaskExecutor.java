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

package ch.systemsx.cisd.etlserver.postregistration;

/**
 * Executor of a post registration task. {@link PostRegistrationMaintenanceTask} calls first {@link #createCleanupTask()} before {@link #execute()} is
 * invoked.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPostRegistrationTaskExecutor
{
    /**
     * Creates a cleanup task.
     */
    public ICleanupTask createCleanupTask();

    /**
     * Performs post registration task.
     */
    public void execute();
}
