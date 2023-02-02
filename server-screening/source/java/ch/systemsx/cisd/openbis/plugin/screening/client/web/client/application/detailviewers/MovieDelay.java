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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author pkupczyk
 */
public class MovieDelay extends Composite
{

    private static final int DEFAULT_DELAY_VALUE_IN_MILLIS = 500;

    private static final int DEFAULT_DELAY_INPUT_LENGTH = 4;

    private Label nameLabel;

    private Label unitLabel;

    private TextBox input;

    public MovieDelay()
    {
        nameLabel = new Label("delay:");
        unitLabel = new Label("ms");

        input = new TextBox();
        input.setValue(String.valueOf(DEFAULT_DELAY_VALUE_IN_MILLIS));
        input.setVisibleLength(DEFAULT_DELAY_INPUT_LENGTH);
        input.setMaxLength(DEFAULT_DELAY_INPUT_LENGTH);
        input.addChangeHandler(new ChangeHandler()
            {
                @Override
                public void onChange(ChangeEvent event)
                {
                    int delay = parseDelay(input.getValue());
                    input.setValue(String.valueOf(delay), false);
                }
            });

        Panel panel = new HorizontalPanel();
        panel.addStyleName("movieDelay");
        panel.add(nameLabel);
        panel.add(input);
        panel.add(unitLabel);

        initWidget(panel);
    }

    private int parseDelay(String delayString)
    {
        if (delayString == null || delayString.trim().length() == 0)
        {
            return DEFAULT_DELAY_VALUE_IN_MILLIS;
        } else
        {
            try
            {
                int delay = Integer.parseInt(delayString);

                if (delay >= 1)
                {
                    return delay;
                } else
                {
                    return DEFAULT_DELAY_VALUE_IN_MILLIS;
                }

            } catch (NumberFormatException e)
            {
                return DEFAULT_DELAY_VALUE_IN_MILLIS;
            }
        }
    }

    public int getDelay()
    {
        return parseDelay(input.getValue());
    }

    public void setDelay(int delay)
    {
        input.setValue(String.valueOf(delay));
    }

    public void addDelayChangeHandler(ChangeHandler handler)
    {
        input.addChangeHandler(handler);
    }

}
