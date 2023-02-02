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

/**
 * An implementation of {@link IActivityObserver} that just records the last event.
 * 
 * @author Bernd Rinn
 */
public class RecordingActivityObserverSensor implements IActivityObserver, IActivitySensor
{

    private volatile long lastActivityMillis;

    public RecordingActivityObserverSensor()
    {
        update(); // Avoid initial 'false alarm'.
    }

    protected long getLastActivityMillis()
    {
        return lastActivityMillis;
    }

    //
    // IActivityObserver
    //

    @Override
    public void update()
    {
        lastActivityMillis = System.currentTimeMillis();
    }

    //
    // IActivitySensor
    //

    @Override
    public long getLastActivityMillisMoreRecentThan(long thresholdMillis)
    {
        return lastActivityMillis;
    }

    @Override
    public boolean hasActivityMoreRecentThan(long thresholdMillis)
    {
        return (System.currentTimeMillis() - lastActivityMillis) < thresholdMillis;
    }

}
