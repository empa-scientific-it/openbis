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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class DelayingDecoratorTest
{
    Mockery context;

    private MockClock clock;

    private IEventFeed provider;

    private IEventFeed decorator;

    private IEventFilter filter;

    private static final long INTERVAL = 500;

    @BeforeMethod
    public void fixture()
    {
        clock = new MockClock();
        context = new Mockery();
        provider = context.mock(IEventFeed.class);
        filter = context.mock(IEventFilter.class);
        decorator = new DelayingDecorator(INTERVAL, provider, clock);
    }

    @Test
    public void callsToDecoratedEventFeedAreDelayedByTheGivenInterval() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    List<String> events = new ArrayList<String>();
                    events.add("event-1");
                    events.add("event-2");
                    exactly(2).of(provider).getNewEvents(filter);
                    will(returnValue(events));
                }
            });

        long firstTime = System.currentTimeMillis();
        clock.setTime(firstTime);
        assertThat(decorator.getNewEvents(filter).size(), is(2));

        clock.setTime(firstTime + INTERVAL);
        assertThat(decorator.getNewEvents(filter).size(), is(0));

        clock.setTime(firstTime + INTERVAL + 1);
        assertThat(decorator.getNewEvents(filter).size(), is(2));

        context.assertIsSatisfied();
    }
}
