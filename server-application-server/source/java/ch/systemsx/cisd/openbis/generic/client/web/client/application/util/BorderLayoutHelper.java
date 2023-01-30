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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.LayoutEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;

/**
 * @author pkupczyk
 */
public class BorderLayoutHelper
{

    private final static String LEFT_PANEL_PREFIX = "left_panel_";

    private final static int INITIAL_LEFT_PANEL_SIZE = 350;

    private IViewContext<?> viewContext;

    private BorderLayout layout;

    private String displayIdSuffix;

    public BorderLayoutHelper(IViewContext<?> viewContext, BorderLayout layout,
            String displayIdSuffix)
    {
        this.viewContext = viewContext;
        this.layout = layout;
        this.displayIdSuffix = displayIdSuffix;
    }

    /**
     * Creates {@link BorderLayoutData} for the left panel extracting initial size from display settings.
     */
    public BorderLayoutData createLeftBorderLayoutData()
    {
        final String panelId = getLeftPanelId();
        float initialSize = getLeftPanelInitialSize();
        viewContext.log(panelId + " initial size: " + initialSize);

        BorderLayoutData layoutData =
                BorderLayoutDataFactory.create(LayoutRegion.WEST, initialSize);
        layoutData.setCollapsible(true);
        return layoutData;
    }

    public static BorderLayoutData createRightBorderLayoutData()
    {
        return BorderLayoutDataFactory.create(LayoutRegion.CENTER);
    }

    /**
     * Adds listeners and sets up the initial left panel state.
     * <p>
     * Id for display settings needs to be initialized before this method is called.
     */
    public void configureLeftPanel(final Component panel)
    {
        // displayIdSuffix must be initialized first
        if (isLeftPanelInitiallyCollapsed())
        {
            viewContext.log(displayIdSuffix + " Initially Collapsed");
            // Without making the panel visible the collapse method is removing the panel
            // from DOM (nothing is shown, even the collapse/show button).
            panel.setVisible(true);
            getLayout().collapse(LayoutRegion.WEST);
        }

        // Add the listeners after configuring the panel, so as not to cause confusion
        addLeftPanelCollapseExpandListeners(panel);
    }

    private void addLeftPanelCollapseExpandListeners(final Component panel)
    {
        final String panelId = getLeftPanelId();
        getLayout().addListener(Events.Collapse, new Listener<BorderLayoutEvent>()
            {
                @Override
                public void handleEvent(BorderLayoutEvent be)
                {
                    viewContext.log(panelId + " Collapsed");
                    viewContext.getDisplaySettingsManager().updatePanelCollapsedSetting(panelId,
                            Boolean.TRUE);
                }

            });
        getLayout().addListener(Events.Expand, new Listener<BorderLayoutEvent>()
            {
                @Override
                public void handleEvent(BorderLayoutEvent be)
                {
                    viewContext.log(panelId + " Expand");
                    viewContext.getDisplaySettingsManager().updatePanelCollapsedSetting(panelId,
                            Boolean.FALSE);
                }

            });
        getLayout().addListener(Events.AfterLayout, new Listener<LayoutEvent>()
            {
                @Override
                public void handleEvent(LayoutEvent le)
                {
                    final Integer size = panel.getOffsetWidth();
                    viewContext.log(panelId + " AfterLayout, size: " + size);
                    // Left panel minimal size can't be less than 20 unless collapse button is used.
                    // We want to save size only if user dragged the splitter so that after restore
                    // from collapsed state the size before collapsing it will be used.
                    if (size > 0)
                    {
                        viewContext.getDisplaySettingsManager().updatePanelSizeSetting(panelId,
                                size);
                    }
                }
            });
    }

    private String getLeftPanelId()
    {
        return LEFT_PANEL_PREFIX + displayIdSuffix;
    }

    private boolean isLeftPanelInitiallyCollapsed()
    {
        Boolean collapsedOrNull =
                viewContext.getDisplaySettingsManager().tryGetPanelCollapsedSetting(
                        getLeftPanelId());
        return collapsedOrNull == null ? false : collapsedOrNull.booleanValue();
    }

    private int getLeftPanelInitialSize()
    {
        Integer sizeOrNull =
                viewContext.getDisplaySettingsManager().tryGetPanelSizeSetting(getLeftPanelId());
        return sizeOrNull == null ? INITIAL_LEFT_PANEL_SIZE : sizeOrNull.intValue();
    }

    private BorderLayout getLayout()
    {
        return layout;
    }

}
