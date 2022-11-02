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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * @author pkupczyk
 */
public abstract class RetryCaller<T, E extends Throwable>
{
    private long waitingTime;

    private final long waitingTimeLimit;

    private final ISimpleLogger logger;

    public RetryCaller(long initialWaitingTime, long waitingTimeLimit, ISimpleLogger logger)
    {
        if (initialWaitingTime <= 0)
        {
            throw new IllegalArgumentException("Initial waiting time must be > 0");
        }
        if (waitingTimeLimit <= 0)
        {
            throw new IllegalArgumentException("Waiting time limit must be > 0");
        }
        if (waitingTimeLimit < initialWaitingTime)
        {
            throw new IllegalArgumentException("Waiting time limit must be >= initial waiting time");
        }

        this.waitingTime = initialWaitingTime;
        this.waitingTimeLimit = waitingTimeLimit;
        this.logger = logger;
    }

    protected abstract T call() throws E;

    public T callWithRetry() throws E
    {
        final long waitUntil = System.currentTimeMillis() + waitingTimeLimit;

        waitForRetry();

        while (true)
        {
            try
            {
                T result = call();
                return result;
            } catch (Exception e)
            {
                if (System.currentTimeMillis() < waitUntil)
                {
                    logger.log(LogLevel.WARN, "Call failed - will retry", e);
                    waitForRetry();
                } else
                {
                    logger.log(LogLevel.WARN, "Call failed - will NOT retry", e);
                    throw e;
                }
            }
        }
    }

    private void waitForRetry()
    {
        try
        {
            logger.log(LogLevel.INFO, "Going to wait " + DateTimeUtils.renderDuration(waitingTime));
            Thread.sleep(waitingTime);
            waitingTime = waitingTime * 2;
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

}
