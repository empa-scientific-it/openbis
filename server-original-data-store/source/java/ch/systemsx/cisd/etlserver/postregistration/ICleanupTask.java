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

import java.io.Serializable;

import ch.systemsx.cisd.common.logging.ISimpleLogger;

/**
 * Interface for cleanup tasks. Clean up tasks have to be {@link Serializable} because they are made persistent.
 * <p>
 * If the Data Store Server crashes the cleanup tasks are executed after start up in order to clean up what the {@link IPostRegistrationTask} didn't
 * finished before it does the job again.
 * <p>
 * Cleanup tasks are also performed if {@link IPostRegistrationTaskExecutor#execute()} throws an exception.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICleanupTask extends Serializable
{
    /**
     * Clean up stuff an interrupted {@link IPostRegistrationTaskExecutor#execute()} has created. This method should not throw an exception if there
     * is nothing to clean up or the clean up state is invalid.
     * 
     * @param logger Logger which can be used to log what has been done.
     */
    public void cleanup(ISimpleLogger logger);
}
