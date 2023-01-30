/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * @author pkupczyk
 */
public class ButtonWithConfirmations extends Button
{

    private ConfirmationChain confirmationChain;

    public ButtonWithConfirmations()
    {
        confirmationChain = new ConfirmationChain(new IConfirmation()
            {
                @Override
                public void confirm(IConfirmationChain chain)
                {
                    fireEvent(Events.Select);
                }
            });
        addBeforeSelectListener();
    }

    public void addConfirmation(IConfirmation confirmation)
    {
        confirmationChain.add(confirmation);
    }

    public void clearConfirmations()
    {
        confirmationChain.clear();
    }

    @Override
    public void removeAllListeners()
    {
        super.removeAllListeners();
        addBeforeSelectListener();
    }

    private void addBeforeSelectListener()
    {
        addListener(Events.BeforeSelect, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    be.setCancelled(true);
                    confirmationChain.reset();
                    confirmationChain.next();
                }
            });
    }

    public static interface IConfirmation
    {

        public void confirm(IConfirmationChain chain);

    }

    public static interface IConfirmationChain
    {

        public void next();

    }

    private static class ConfirmationChain implements IConfirmationChain
    {

        private List<IConfirmation> confirmations = new ArrayList<IConfirmation>();

        private IConfirmation lastConfirmation;

        private int currentConfirmationIndex;

        public ConfirmationChain(IConfirmation lastConfirmation)
        {
            this.lastConfirmation = lastConfirmation;
        }

        public void add(IConfirmation confirmation)
        {
            confirmations.add(confirmation);
        }

        public void clear()
        {
            confirmations.clear();
            reset();
        }

        public void reset()
        {
            currentConfirmationIndex = 0;
        }

        @Override
        public void next()
        {
            if (currentConfirmationIndex < confirmations.size())
            {
                confirmations.get(currentConfirmationIndex++).confirm(this);
            } else
            {
                lastConfirmation.confirm(this);
            }
        }
    }

}
