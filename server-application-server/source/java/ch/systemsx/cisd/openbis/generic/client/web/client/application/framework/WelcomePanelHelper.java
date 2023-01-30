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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.StandardPortletNames.HISTORY;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HistoryWidget;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * Helper methods for creating a panel with welcome information.
 * 
 * @author Piotr Buczek
 */
public class WelcomePanelHelper
{
    private static final String APPLICATION_VIEW_MODE_PAGE_BASE_NAME = "./custom/welcomePage";

    private static final String SIMPLE_VIEW_MODE_PAGE_BASE_NAME_GENERIC = "./custom/welcomePageSimpleGeneric";

    private static final String SIMPLE_VIEW_MODE_PAGE_BASE_NAME_SCREENING = "./custom/welcomePageSimpleScreening";

    public static final Component createWelcomePanel(IViewContext<?> viewContext, String idPrefix)
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new BorderLayout());
        layoutContainer.setStyleAttribute("background-color", "white");
        layoutContainer.setId(idPrefix + "welcome");
        layoutContainer.addStyleName("welcomePageContainer");
        DisplaySettingsManager displaySettingsManager = viewContext.getDisplaySettingsManager();
        if (displaySettingsManager.getPortletConfigurations().containsKey(HISTORY)
                && displaySettingsManager.getVisits().isEmpty() == false)
        {
            layoutContainer.add(new HistoryWidget(viewContext), new BorderLayoutData(
                    LayoutRegion.WEST, 0.3f));
        }
        HtmlPage welcomePage = new HtmlPage(getWelcomePageBaseName(viewContext));
        layoutContainer.add(welcomePage, new BorderLayoutData(LayoutRegion.CENTER, 1f));
        return layoutContainer;
    }

    private static final String getWelcomePageBaseName(IViewContext<?> viewContext)
    {
        return viewContext.isSimpleOrEmbeddedMode() ? getSimpleModeWelcomePageBaseName(viewContext)
                : APPLICATION_VIEW_MODE_PAGE_BASE_NAME;
    }

    private static final String getSimpleModeWelcomePageBaseName(IViewContext<?> viewContext)
    {
        if (viewContext.getModel().getApplicationInfo().isTechnologyEnabled("screening"))
        {
            return SIMPLE_VIEW_MODE_PAGE_BASE_NAME_SCREENING;
        } else
        {
            return SIMPLE_VIEW_MODE_PAGE_BASE_NAME_GENERIC;
        }
    }

}
