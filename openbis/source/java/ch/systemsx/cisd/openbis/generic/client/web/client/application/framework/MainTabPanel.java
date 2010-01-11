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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * Main panel - where the tabs will open.
 * 
 * @author Izabela Adamczyk
 */
public class MainTabPanel extends TabPanel
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "main-tab-panel_";

    public static final String TAB_SUFFIX = "_tab";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static final String ID = PREFIX.substring(0, PREFIX.length() - 1);

    private final Map<String/* tab id */, MainTabItem> openTabs =
            new HashMap<String, MainTabItem>();

    MainTabPanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        // setLayout(new FitLayout()); - for some reason this results in JavaScriptException:
        // "com.google.gwt.core.client.JavaScriptException: (TypeError): Result of expression 'c' [null] is not an object."
        setTabScroll(true);
        setId(ID);
        setCloseContextMenu(true);
        add(createWelcomePanel());
    }

    private final MainTabItem createWelcomePanel()
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
        String layoutContainerId = PREFIX + "welcome";
        layoutContainer.setId(layoutContainerId);
        layoutContainer.addText(createWelcomeText());
        final MainTabItem intro =
                new MainTabItem(DefaultTabItem.createUnaware("&nbsp;", layoutContainer, false),
                        layoutContainerId);
        intro.setClosable(false);
        return intro;
    }

    private final String createWelcomeText()
    {
        final Element div = DOM.createDiv();
        div.setClassName("intro-tab");
        div.setInnerText(viewContext.getMessage(Dict.WELCOME));
        return div.getString();
    }

    private final MainTabItem tryFindTab(final ITabItemFactory tabItemFactory)
    {
        return openTabs.get(tabItemFactory.getId());
    }

    /**
     * Set the currently selected tab to the given <i>tabItem</i>.
     * <p>
     * If the tab could not be found (meaning that it has not been created yet), then a new tab will
     * be generated out of given {@link ITabItem}.
     * </p>
     */
    public final void openTab(final ITabItemFactory tabItemFactory)
    {
        final MainTabItem tab = tryFindTab(tabItemFactory);
        if (tab != null)
        {
            setSelection(tab);
        } else
        {
            String tabId = tabItemFactory.getId();
            // Note that if not set, is then automatically generated. So this is why we test for
            // 'ID_PREFIX'. We want the user to set an unique id.
            assert tabId.startsWith(GenericConstants.ID_PREFIX) : "Unspecified component id.";
            final MainTabItem newTab = new MainTabItem(tabItemFactory.create(), tabId);
            add(newTab);
            openTabs.put(tabId, newTab);
            setSelection(newTab);
        }
    }

    /** closes all opened tabs */
    public final void reset()
    {
        for (TabItem openTab : new ArrayList<TabItem>(openTabs.values()))
        {
            openTab.close();
        }
    }

    @Override
    protected void onItemContextMenu(TabItem item, int x, int y)
    {
        // WORKAROUND -- GXT does not provide a mechanism to extend the context menu. This is a
        // workaround for this problem.

        // Check if the menu has not been initialized yet
        boolean shouldInitializeContextMenu = (closeContextMenu == null);

        // call the super
        super.onItemContextMenu(item, x, y);

        // If the menu was not initialized, we can now add menu items to the context menu and
        // refresh the menu
        if (shouldInitializeContextMenu)
        {
            MenuItem menuItem = new MenuItem("Help", new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce)
                    {
                        MainTabItem selectedTab = (MainTabItem) ce.getContainer().getData("tab");
                        URLMethodWithParameters url =
                                new URLMethodWithParameters(
                                        GenericConstants.HELP_REDIRECT_SERVLET_NAME);
                        HelpPageIdentifier helpPageId = selectedTab.getHelpPageIdentifier();
                        url.addParameter(GenericConstants.HELP_REDIRECT_DOMAIN_KEY, helpPageId
                                .getHelpPageDomain());
                        url.addParameter(GenericConstants.HELP_REDIRECT_ACTION_KEY, helpPageId
                                .getHelpPageAction());
                        WindowUtils.openWindow(URL.encode(url.toString()));
                    }
                });
            closeContextMenu.add(menuItem);
            super.onItemContextMenu(item, x, y);
        }

    }

    //
    // Helper classes
    //
    private final class MainTabItem extends TabItem
    {
        private final ITabItem tabItem;

        private final String idPrefix;

        public MainTabItem(final ITabItem tabItem, final String idPrefix)
        {
            this.tabItem = tabItem;
            this.idPrefix = idPrefix;
            setId(idPrefix + TAB_SUFFIX);
            setClosable(true);
            setLayout(new FitLayout());
            addStyleName("pad-text");
            add(tabItem.getComponent());
            tabItem.getComponent().addListener(AppEvents.CloseViewer, createCloseViewerListener());
            tabItem.getTabTitleUpdater().bind(this);
            if (tabItem.isCloseConfirmationNeeded())
            {
                addListener(Events.BeforeClose, createBeforeCloseListener());
            }
            addListener(Events.Close, createCloseTabListener());
            addListener(Events.Select, createActivateTabListener());
        }

        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return tabItem.getHelpPageIdentifier();
        }

        @Override
        public void close()
        {
            super.close();
            cleanup();
        }

        private void cleanup()
        {
            tabItem.onClose();
            openTabs.remove(idPrefix);
        }

        private Listener<ComponentEvent> createCloseViewerListener()
        {
            return new Listener<ComponentEvent>()
                {
                    public final void handleEvent(final ComponentEvent be)
                    {
                        if (be.getType() == AppEvents.CloseViewer)
                        {
                            MainTabItem.this.close();
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createCloseTabListener()
        {
            return new Listener<TabPanelEvent>()
                {
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.getType().equals(Events.Close))
                        {
                            cleanup();
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createActivateTabListener()
        {
            return new Listener<TabPanelEvent>()
                {
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.getType().equals(Events.Select))
                        {
                            tabItem.onActivate();
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createBeforeCloseListener()
        {
            return new Listener<TabPanelEvent>()
                {
                    public void handleEvent(final TabPanelEvent be)
                    {
                        be.setCancelled(true);
                        new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE),
                                viewContext.getMessage(Dict.CONFIRM_CLOSE_MSG))
                            {
                                @Override
                                protected void onYes()
                                {
                                    MainTabItem.this.close();
                                }
                            }.show();
                    }
                };
        }
    }
}
