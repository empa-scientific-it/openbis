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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

/**
 * A public class for setting rollback configuration variables (which are stored in package-visible classes).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RollbackConfigurator
{
    /**
     * These two variables determine together how long the rollback mechanism waits for a file system that has become unavailable and how often it
     * checks for the file system to become available.
     * <p>
     * The duration the rollback mechanism will wait before giving up equals waitTimeMS * waitCount;
     * <p>
     * Made public for testing.
     */
    public static void setFileSystemAvailabilityPollingWaitTimeAndWaitCount(int waitTimeMS,
            int waitCount)
    {
        ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.LiveTransactionState
                .setFileSystemAvailabilityPollingWaitTimeAndWaitCount(waitTimeMS, waitCount);
    }
}
