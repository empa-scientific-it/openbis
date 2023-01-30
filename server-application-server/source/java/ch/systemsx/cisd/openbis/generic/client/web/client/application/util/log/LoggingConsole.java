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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * @author Tomasz Pylak
 */
public class LoggingConsole
{
    public static final String ID = GenericConstants.ID_PREFIX + "LoggingConsole";

    public static Component create(final IProfilingTable profilingTable)
    {
        final ContentPanel mainPanel = new ContentPanel();
        mainPanel.setScrollMode(Scroll.AUTO);

        mainPanel.add(createContent(profilingTable));
        Button clearButton = new Button("Clear");
        clearButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    profilingTable.clearLog();
                    mainPanel.removeAll();
                }
            });
        mainPanel.addButton(clearButton);

        Button refreshButton = new Button("Refresh");
        refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    mainPanel.removeAll();
                    mainPanel.add(createContent(profilingTable));
                    mainPanel.layout();
                }
            });
        mainPanel.addButton(refreshButton);

        return mainPanel;
    }

    private static Widget createContent(IProfilingTable profilingTable)
    {
        final VerticalPanel panel = new VerticalPanel();
        List<String> events = profilingTable.getLoggedEvents();
        panel.add(new Label("The header is: start-timestamp [stop-timestamp: duration] description"));
        for (String event : events)
        {
            panel.add(new Label(event));
        }
        return panel;
    }
}
