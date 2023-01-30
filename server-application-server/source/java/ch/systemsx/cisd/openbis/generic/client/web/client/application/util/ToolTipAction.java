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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;

/**
 * Helper class for setting up a delegated action to be invoked when user puts mouse over a {@link Component}. Implementation of delayed invocation of
 * the action is based on delayed showing of a {@link ToolTip} (with the same naming conventions).
 */
public class ToolTipAction
{

    private final Component target;

    private final Listener<ComponentEvent> listener;

    private IDelegatedAction action; // shouldn't be null

    private Timer invokeTimer;

    // private boolean scheduled = false; // not needed

    public ToolTipAction(final Component target)
    {
        this.target = target;
        this.listener = createListener(target);
    }

    private Listener<ComponentEvent> createListener(final Component aTarget)
    {
        return new Listener<ComponentEvent>()
            {
                @Override
                public void handleEvent(ComponentEvent ce)
                {
                    Element source = aTarget.getElement();
                    EventType type = ce.getType();
                    if (type == Events.OnMouseOver)
                    {
                        EventTarget from = ce.getEvent().getRelatedEventTarget();
                        if (from == null
                                || (com.google.gwt.dom.client.Element.is(source) && com.google.gwt.dom.client.Element.is(from) && !DOM.isOrHasChild(
                                        source, (Element) com.google.gwt.dom.client.Element.as(from))))
                        {
                            onTargetOver(ce);
                        }
                    } else if (type == Events.OnMouseOut)
                    {
                        EventTarget to = ce.getEvent().getRelatedEventTarget();
                        if (to == null
                                || (com.google.gwt.dom.client.Element.is(source) && com.google.gwt.dom.client.Element.is(to) && !DOM.isOrHasChild(
                                        source, (Element) com.google.gwt.dom.client.Element.as(to))))
                        {
                            onTargetOut(ce);
                        }
                    } else if (type == Events.Hide || type == Events.Detach)
                    {
                        hide();
                    }
                }
            };
    }

    public void setAction(IDelegatedAction action)
    {
        this.action = action;
        initListener(this.target, this.listener); // reinitialize
    }

    private void onTargetOver(ComponentEvent ce)
    {
        // if (scheduled == false && !ce.within(target.getElement()))
        if (!ce.within(target.getElement()))
        {
            return;
        }
        delayInvoke();
    }

    private void onTargetOut(ComponentEvent ce)
    {
        cancelInvocation();
    }

    private void hide()
    {
        cancelInvocation();
    }

    private void delayInvoke()
    {
        // scheduled = true;
        invokeTimer = new Timer()
            {
                @Override
                public void run()
                {
                    invoke();
                }
            };
        invokeTimer.schedule(500); // delay 500ms (the same as for showing loading)
    }

    private void invoke()
    {
        if (action != null)
        {
            this.action.execute();
        }
        if (this.target != null)
        {
            removeListener(target, listener);
        }
    }

    private void cancelInvocation()
    {
        // scheduled = false;
        if (invokeTimer != null)
        {
            invokeTimer.cancel();
            invokeTimer = null;
        }
    }

    private static void initListener(final Component target, Listener<ComponentEvent> listener)
    {
        target.addListener(Events.OnMouseOver, listener);
        target.addListener(Events.OnMouseOut, listener);
        target.addListener(Events.Hide, listener);
        target.addListener(Events.Detach, listener);
        target.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
    }

    private static void removeListener(final Component target, Listener<ComponentEvent> listener)
    {
        target.removeListener(Events.OnMouseOver, listener);
        target.removeListener(Events.OnMouseOut, listener);
        target.removeListener(Events.OnMouseMove, listener);
        target.removeListener(Events.Hide, listener);
        target.removeListener(Events.Detach, listener);
    }

}