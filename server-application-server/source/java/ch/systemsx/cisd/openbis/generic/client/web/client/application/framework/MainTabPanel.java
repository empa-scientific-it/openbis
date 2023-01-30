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

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * Main panel - where the tabs will open.
 * 
 * @author Izabela Adamczyk
 */
public class MainTabPanel extends TabPanel implements IMainPanel
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "main-tab-panel_";

    public static final String TAB_SUFFIX = "_tab";

    public static final String BLANK_TAB_TITLE = "&nbsp;";

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
        setPlain(true);
        setBodyBorder(false);
        setBorders(false);
        setBorderStyle(false);
        add(createWelcomePanel());
    }

    private final MainTabItem createWelcomePanel()
    {
        final LayoutContainer mainComponent = new LayoutContainer(new FitLayout());
        ITabItem tabItem =
                DefaultTabItem.createWithUpdater(BLANK_TAB_TITLE, mainComponent,
                        new IDelegatedAction()
                            {
                                @Override
                                public void execute()
                                {
                                    mainComponent.removeAll();
                                    mainComponent.add(WelcomePanelHelper.createWelcomePanel(
                                            viewContext, PREFIX));
                                    mainComponent.layout();
                                }
                            }, viewContext);
        final MainTabItem intro = new MainTabItem(tabItem, mainComponent.getId(), null, null);
        intro.setIcon(AbstractImagePrototype.create(viewContext.getImageBundle().getHomeIcon()));
        intro.setClosable(false);
        return intro;
    }

    private final MainTabItem tryFindTab(final AbstractTabItemFactory tabItemFactory)
    {
        return openTabs.get(tabItemFactory.getId());
    }

    private final int getTabPosition(final MainTabItem tabItem)
    {
        return getItems().indexOf(tabItem);
    }

    /**
     * Set the currently selected tab to the given <i>tabItem</i>.
     * <p>
     * If the tab could not be found (meaning that it has not been created yet), then a new tab will be generated out of given {@link ITabItem}.
     * </p>
     */
    @Override
    public final void open(final AbstractTabItemFactory tabItemFactory)
    {
        boolean inBackground = tabItemFactory.isInBackground();
        final MainTabItem tab = tryFindTab(tabItemFactory);
        if (tab != null && tabItemFactory.isForceReopen() == false)
        {
            maybeActivate(tab, inBackground);
        } else
        {
            final String tabId = tabItemFactory.getId();
            // Note that if not set, is then automatically generated. So this is why we test for
            // 'ID_PREFIX'. We want the user to set an unique id.
            assert tabId.startsWith(GenericConstants.ID_PREFIX) : "Unspecified component id.";
            final HelpPageIdentifier helpId = tabItemFactory.getHelpPageIdentifier();
            final String linkOrNull = tabItemFactory.tryGetLink();
            assert helpId != null : "Unspecified help identifier";
            final MainTabItem newTab =
                    new MainTabItem(tabItemFactory.create(), tabId, helpId, linkOrNull);
            // WORKAROUND to fix problems when paging toolbar's layout is performed in a hidden tab
            newTab.setHideMode(HideMode.OFFSETS);
            if (tab != null)
            {
                // reopen tab in the same position
                int oldPosition = getTabPosition(tab);
                tab.close(); // e.g. removes from openTabs
                insert(newTab, oldPosition);
            } else
            {
                add(newTab);
            }
            openTabs.put(tabId, newTab);
            maybeActivate(newTab, inBackground);
        }
    }

    private void maybeActivate(MainTabItem tab, boolean inBackground)
    {
        if (inBackground == false)
        {
            setSelection(tab);
        }
    }

    /** closes all opened tabs */
    @Override
    public final void reset()
    {
        for (TabItem openTab : new ArrayList<TabItem>(openTabs.values()))
        {
            openTab.close();
        }
    }

    // context menu

    private MenuItem helpMenuItem;

    private MenuItem bookmarkMenuItem;

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
            bookmarkMenuItem = createBookmarkMenuItem();
            closeContextMenu.add(bookmarkMenuItem);
            helpMenuItem = createHelpMenuItem();
            closeContextMenu.add(helpMenuItem);
            super.onItemContextMenu(item, x, y);
        }
        boolean bookmarkNotAvailable = ((MainTabItem) item).getLinkOrNull() == null;
        bookmarkMenuItem.setEnabled(bookmarkNotAvailable == false);
    }

    private MenuItem createHelpMenuItem()
    {
        return new MenuItem("Help", new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    MainTabItem selectedTab = (MainTabItem) ce.getContainer().getData("tab");
                    URLMethodWithParameters url =
                            new URLMethodWithParameters(GenericConstants.HELP_REDIRECT_SERVLET_NAME);
                    HelpPageIdentifier helpPageId = selectedTab.getHelpPageIdentifier();
                    url.addParameter(GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY,
                            helpPageId.getHelpPageTitle(viewContext));
                    url.addParameter(GenericConstants.HELP_REDIRECT_SPECIFIC_KEY,
                            Boolean.toString(helpPageId.isSpecific()));
                    WindowUtils.openWindow(URL.encode(url.toString()));
                }
            });
    }

    private MenuItem createBookmarkMenuItem()
    {
        return new MenuItem(viewContext.getMessage(Dict.TAB_LINK),
                new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce)
                        {
                            MainTabItem selectedTab =
                                    (MainTabItem) ce.getContainer().getData("tab");
                            String linkToken = selectedTab.getLinkOrNull();
                            assert linkToken != null;
                            String link =
                                    LinkRenderer.renderAsLinkWithAnchor(
                                            viewContext.getMessage(Dict.URL), "#" + linkToken,
                                            false);
                            MessageBox.info(viewContext.getMessage(Dict.TAB_LINK),
                                    viewContext.getMessage(Dict.TAB_LINK_MESSAGE, link), null);
                        }
                    });
    }

    //
    // Helper classes
    //
    private final class MainTabItem extends TabItem
    {
        private final ITabItem tabItem;

        private final String idPrefix;

        private final String linkOrNull;

        private final HelpPageIdentifier helpPageIdentifier;

        public MainTabItem(final ITabItem tabItem, final String idPrefix,
                final HelpPageIdentifier helpPageIdentifier, String linkOrNull)
        {
            this.tabItem = tabItem;
            this.idPrefix = idPrefix;
            this.helpPageIdentifier = helpPageIdentifier;
            this.linkOrNull = linkOrNull;
            setId(idPrefix + TAB_SUFFIX);
            setClosable(true);
            setLayout(new FitLayout());
            addStyleName("pad-text");
            add(tabItem.getComponent());
            tabItem.getComponent().addListener(AppEvents.CloseViewer, createCloseViewerListener());
            tabItem.getTabTitleUpdater().bind(this);
            addListener(Events.BeforeClose, createBeforeCloseListener());
            addListener(Events.Close, createCloseTabListener());
            addListener(Events.BeforeSelect, createRefreshTabListener());
            addListener(Events.Select, createActivateTabListener());
        }

        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return helpPageIdentifier;
        }

        public String getLinkOrNull()
        {
            return linkOrNull;
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
            if (openTabs.size() == 0)
            {
                MainTabPanel.this.syncSize();
            }
        }

        private Listener<ComponentEvent> createCloseViewerListener()
        {
            return new Listener<ComponentEvent>()
                {
                    @Override
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
                    @Override
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
                    @Override
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.getType().equals(Events.Select))
                        {
                            tabItem.onActivate(linkOrNull);
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createRefreshTabListener()
        {
            return new Listener<TabPanelEvent>()
                {
                    @Override
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.getType().equals(Events.BeforeSelect))
                        {
                            tabItem.onRefresh(linkOrNull);
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createBeforeCloseListener()
        {
            return new Listener<TabPanelEvent>()
                {
                    private boolean closeConfirmed = false;

                    @Override
                    public void handleEvent(final TabPanelEvent be)
                    {
                        if (tabItem.isCloseConfirmationNeeded() && closeConfirmed == false)
                        {
                            be.setCancelled(true);
                            new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE),
                                    viewContext.getMessage(Dict.CONFIRM_CLOSE_MSG))
                                {
                                    @Override
                                    protected void onYes()
                                    {
                                        closeConfirmed = true;
                                        MainTabItem.this.close();
                                    }
                                }.show();
                        }
                    }
                };
        }
    }

    @Override
    public Widget asWidget()
    {
        return this;
    }
}
