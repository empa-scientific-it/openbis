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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Client;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Footer;

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

    private TopMenu north;

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
        Client.clientSysout("AppView.initUI.start");
        viewport = new Viewport();
        viewport.setLayout(new BorderLayout());
        if (viewContext.isSimpleMode() == false)
        {
            createNorth();
        }
        Client.clientSysout("AppView.initUI.center");
        createCenter();
        Client.clientSysout("AppView.initUI.south");
        createSouth();
        Client.clientSysout("AppView.initUI.clear");
        RootPanel.get().clear();
        Client.clientSysout("AppView.initUI.viewport");
        RootPanel.get().add(viewport);
        Client.clientSysout("AppView.initUI.finish");
    }

    private final void createNorth()
    {
        north = new TopMenu(viewContext, componentProvider);
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 30);
        viewport.add(north, data);
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
        final Footer footer = new Footer(viewContext);
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 20);
        viewport.add(footer, data);
    }

    //
    // View
    //

    @Override
    protected final void initialize()
    {
        componentProvider = new ComponentProvider(viewContext);
        // categoriesBuilder = new CategoriesBuilder(componentProvider);
    }

    @Override
    protected final void handleEvent(final AppEvent event)
    {
        if (event.getType() == AppEvents.INIT)
        {
            Client.clientSysout("AppView.initUI" + event.getType());
            initUI();
        } else if (event.getType() == AppEvents.NAVI_EVENT)
        {
            activate(getData(event));
        }
    }

    private static IMainPanel createMainPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        if (viewContext.isSimpleMode())
        {
            return new MainPagePanel(viewContext);
        } else
        {
            return new MainTabPanel(viewContext);
        }
    }
}
