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

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class ControlDirectoryEventFeedTest
{
    File controlDir;

    private IEventFeed eventFeed;

    private IEventFilter filter;

    private Mockery context;

    @BeforeMethod
    public void fixture()
    {
        controlDir = new File("/tmp/" + UUID.randomUUID().toString());
        controlDir.mkdir();
        eventFeed = new ControlDirectoryEventFeed(controlDir);

        context = new Mockery();
        filter = context.mock(IEventFilter.class);

    }

    @Test
    public void eventsAreReturnedOnlyOnce() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(filter).accepts("event");
                    will(returnValue(true));
                }
            });
        createEvent("event");

        assertThat(eventFeed.getNewEvents(filter).isEmpty(), is(false));
        assertThat(eventFeed.getNewEvents(filter).isEmpty(), is(true));
    }

    @Test
    public void eventsAreReturnedInCorrectOrder() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(filter).accepts(with(any(String.class)));
                    will(returnValue(true));
                }
            });

        createEvent("this_event", 1000);
        createEvent("that_event", 10000);
        createEvent("this_event2", 100000);

        List<String> events = eventFeed.getNewEvents(filter);

        assertThat(events.size(), is(3));
        assertThat(events.get(0), is("this_event"));
        assertThat(events.get(1), is("that_event"));
        assertThat(events.get(2), is("this_event2"));
    }

    @Test
    public void filesAreCleaned() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(filter).accepts("event");
                    will(returnValue(true));
                }
            });

        File event = new File(controlDir, "event");
        event.createNewFile();

        eventFeed.getNewEvents(filter);

        assertThat(event.exists(), is(false));
    }

    @Test
    public void unrelatedFilesAreNotCleaned() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(filter).accepts("other_event");
                    will(returnValue(false));
                }
            });

        File event = new File(controlDir, "other_event");
        event.createNewFile();

        List<String> events = eventFeed.getNewEvents(filter);

        assertThat(events.isEmpty(), is(true));
        assertThat(event.exists(), is(true));
    }

    private void createEvent(String event) throws Exception
    {
        createEvent(event, System.currentTimeMillis());
    }

    private void createEvent(String event, long timestamp) throws Exception
    {
        File f = new File(controlDir, event);
        f.createNewFile();
        f.setLastModified(timestamp);
    }
}
