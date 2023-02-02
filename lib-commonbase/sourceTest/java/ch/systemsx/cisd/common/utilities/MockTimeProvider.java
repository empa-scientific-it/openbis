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

package ch.systemsx.cisd.common.utilities;

/**
 * A time provider which increases time from a start time by specified steps.
 *
 * @author Franz-Josef Elmer
 */
public class MockTimeProvider implements ITimeAndWaitingProvider
{
    private long time;

    private final long[] timeSteps;

    private int index;

    /**
     * Creates an instance which increases from 0 in steps of one second.
     */
    public MockTimeProvider()
    {
        this(0, 1000);
    }

    public MockTimeProvider(long startTime, long... timeSteps)
    {
        time = startTime;
        this.timeSteps = timeSteps;
    }

    @Override
    public long getTimeInMilliseconds()
    {
        long result = time;
        time += timeSteps[index++ % timeSteps.length];
        return result;
    }

    @Override
    public void sleep(long milliseconds)
    {
        time += milliseconds;
    }
}
