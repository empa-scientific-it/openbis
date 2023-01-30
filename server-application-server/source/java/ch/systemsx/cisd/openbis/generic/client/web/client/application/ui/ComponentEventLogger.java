/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.IProfilingTable;

public class ComponentEventLogger
{

    public static enum EventPair
    {
        RENDER(Events.BeforeRender, Events.Render), LAYOUT(Events.BeforeLayout, Events.AfterLayout);

        private final EventType beforeEvent;

        private final EventType afterEvent;

        private EventPair(EventType beforeEvent, EventType afterEvent)
        {
            this.beforeEvent = beforeEvent;
            this.afterEvent = afterEvent;
        }
    }

    private final Map<Object, Integer> logIDs = new HashMap<Object, Integer>();

    private final IProfilingTable profilingTable;

    private final String viewId;

    public ComponentEventLogger(IProfilingTable profilingTable, String viewId)
    {
        this.profilingTable = profilingTable;
        this.viewId = viewId;
    }

    public void prepareLoggingBetweenEvents(final Component component, final EventPair eventPair)
    {
        final Object dummySource = new Object();
        component.addListener(eventPair.beforeEvent, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    String id = component.getId();
                    if (id.startsWith("x-"))
                    {
                        id = component.getClass().getName();
                        int lastIndex = id.lastIndexOf('.');
                        if (lastIndex >= 0)
                        {
                            id = id.substring(lastIndex + 1);
                        }
                    }
                    Object key = be.getSource();
                    if (key == null)
                    {
                        key = dummySource;
                    }
                    logIDs.put(key, log("event: " + eventPair + " (" + id + ")"));
                }
            });
        component.addListener(eventPair.afterEvent, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    Object key = be.getSource();
                    if (key == null)
                    {
                        key = dummySource;
                    }
                    profilingTable.logStop(logIDs.get(key));
                }
            });
    }

    private int log(String message)
    {
        return profilingTable.log(message + " [" + viewId + "]");
    }

}
