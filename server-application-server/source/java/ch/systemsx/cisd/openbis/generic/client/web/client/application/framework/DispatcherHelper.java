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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents.OpenUrlEvent;

/**
 * A helper for using {@link Dispatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class DispatcherHelper
{
    private DispatcherHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Creates and dispatches an event of type {@link OpenUrlEvent#OPEN_URL_EVENT} which opens a new window.
     */
    public final static void dispatchOpenUrlEvent(String url)
    {
        dispatch(new OpenUrlEvent(url, null));
    }

    /**
     * Creates and dispatches an event of type {@link OpenUrlEvent#OPEN_URL_EVENT} which changes the url of the current window.
     */
    public final static void dispatchRedirectUrlEvent(String url, String target)
    {
        dispatch(new OpenUrlEvent(url, target));
    }

    /**
     * Creates and dispatches an event of type {@link AppEvents#NAVI_EVENT}. The event opens a new tab.
     */
    public final static void dispatchNaviEvent(final AbstractTabItemFactory tabItemFactory)
    {
        dispatch(createEvent(AppEvents.NAVI_EVENT, tabItemFactory));
    }

    private final static AppEvent createEvent(EventType eventType, Object data)
    {
        final AppEvent event = new AppEvent(eventType, data);
        return event;
    }

    private static void dispatch(AppEvent event)
    {
        Dispatcher.get().dispatch(event);
    }
}
