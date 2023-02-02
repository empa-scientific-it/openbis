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

package ch.systemsx.cisd.common.api.retry.config;

/**
 * @author pkupczyk
 */
public class StaticRetryConfiguration implements RetryConfiguration
{

    private int maximumNumberOfRetries;

    private int waitingTimeBetweenRetries;

    private float waitingTimeBetweenRetriesIncreasingFactor;

    @Override
    public int getMaximumNumberOfRetries()
    {
        return maximumNumberOfRetries;
    }

    public void setMaximumNumberOfRetries(int maximumNumberOfRetries)
    {
        this.maximumNumberOfRetries = maximumNumberOfRetries;
    }

    @Override
    public int getWaitingTimeBetweenRetries()
    {
        return waitingTimeBetweenRetries;
    }

    public void setWaitingTimeBetweenRetries(int waitingTimeBetweenRetries)
    {
        this.waitingTimeBetweenRetries = waitingTimeBetweenRetries;
    }

    @Override
    public float getWaitingTimeBetweenRetriesIncreasingFactor()
    {
        return waitingTimeBetweenRetriesIncreasingFactor;
    }

    public void setWaitingTimeBetweenRetriesIncreasingFactor(
            float waitingTimeBetweenRetriesIncreasingFactor)
    {
        this.waitingTimeBetweenRetriesIncreasingFactor = waitingTimeBetweenRetriesIncreasingFactor;
    }

}
