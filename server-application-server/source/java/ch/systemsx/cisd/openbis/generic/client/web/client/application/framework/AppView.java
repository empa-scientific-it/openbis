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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SimpleModeHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main application view.
 * 
 * @author Izabela Adamczyk
 */
final class AppView extends View
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private Viewport viewport;

    private IMainPanel mainPanel;

    private ComponentProvider componentProvider;

    AppView(final Controller controller, final CommonViewContext viewContext)
    {
        super(controller);
        this.viewContext = viewContext;
    }

    private final AbstractTabItemFactory getData(final AppEvent event)
    {
        return event.getData();
    }

    private final void activate(final AbstractTabItemFactory tabItemFactory)
    {
        mainPanel.open(tabItemFactory);
    }

    private final void initUI()
    {
        viewport = new Viewport();
        viewport.setLayout(new BorderLayout());
        createNorth();
        createCenter();
        createSouth();
        RootPanel.get().clear();
        RootPanel.get().add(viewport);
    }

    private final void createNorth()
    {
        LayoutContainer north;
        ViewMode viewMode = getViewMode();
        if (viewMode == ViewMode.SIMPLE)
        {
            north = new SimpleModeHeader(viewContext, componentProvider);
        } else if (viewMode == ViewMode.NORMAL)
        {
            north = new TopMenu(viewContext, componentProvider);
        } else if (viewMode == ViewMode.EMBEDDED || viewMode == ViewMode.GRID)
        {
            north = null;
        } else
        {
            throw new IllegalStateException("Unknown view mode " + viewMode);
        }
        if (north != null)
        {
            final BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 30);
            viewport.add(north, data);
        }
    }

    private ViewMode getViewMode()
    {
        return viewContext.getModel().getViewMode();
    }

    private final void createCenter()
    {
        mainPanel = createMainPanel(viewContext);
        componentProvider.setMainPanel(mainPanel);
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        viewport.add(mainPanel.asWidget(), data);
    }

    private final void createSouth()
    {
        if (viewContext.isDebuggingEnabled())
        {
            final BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 200);
            data.setSplit(true);
            final ContentPanel panel = new ContentPanel();
            panel.setScrollMode(Scroll.AUTO);
            panel.setHeaderVisible(false);
            panel.add(DebugPanelManager.createDebugPanel());
            viewport.add(panel, data);
        }
    }

    //
    // View
    //

    @Override
    protected final void initialize()
    {
        componentProvider = new ComponentProvider(viewContext);
    }

    @Override
    protected final void handleEvent(final AppEvent event)
    {
        if (event.getType() == AppEvents.INIT)
        {
            initUI();
        } else if (event.getType() == AppEvents.NAVI_EVENT)
        {
            activate(getData(event));
        }
    }

    private static IMainPanel createMainPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return new MainPagePanel(viewContext);
        } else
        {
            return new MainTabPanel(viewContext);
        }
    }
}
