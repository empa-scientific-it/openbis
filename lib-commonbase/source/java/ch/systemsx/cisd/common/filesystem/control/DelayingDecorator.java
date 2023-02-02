/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.control;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator that can be used to limit the amount of getNewEvents() calls on the decorated event feed.
 * 
 * @author anttil
 */
public class DelayingDecorator implements IEventFeed
{
    private final IEventFeed eventFeed;

    private IClock clock;

    private final long interval;

    private long lastCall;

    public DelayingDecorator(long interval, IEventFeed eventFeed)
    {
        this(interval, eventFeed, new SystemClock());
    }

    DelayingDecorator(long interval, IEventFeed eventFeed, IClock clock)
    {
        this.eventFeed = eventFeed;
        this.clock = clock;
        this.interval = interval;
        this.lastCall = 0;
    }

    @Override
    public List<String> getNewEvents(IEventFilter filter)
    {
        long currentTime = clock.getTime();
        if (currentTime - lastCall > interval)
        {
            lastCall = currentTime;
            return eventFeed.getNewEvents(filter);
        } else
        {
            return new ArrayList<String>();
        }
    }
}
