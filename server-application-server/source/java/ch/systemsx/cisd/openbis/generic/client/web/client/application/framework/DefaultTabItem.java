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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * A default {@link ITabItem} implementation with various factory methods.
 * 
 * @author Christian Ribeaud
 */
public class DefaultTabItem implements ITabItem
{
    private final TabTitleUpdater titleUpdater;

    private final Component component;

    private final IDelegatedAction disposerActionOrNull;

    private final LastModificationStateUpdater lastModificationStateUpdaterOrNull;

    private final LastHistoryTokenUpdater historyTokenUpdater;

    private final IDelegatedAction updaterOrNull;

    private final boolean isCloseConfirmationNeeded;

    /**
     * Creates a tab with the specified {@link Component}. The tab is unaware of database modifications and will not be automatically refreshed if
     * changes occur.
     */
    public static ITabItem createUnaware(final String title, final Component component,
            boolean isCloseConfirmationNeeded, IViewContext<?> viewContext)
    {
        return new DefaultTabItem(viewContext, title, component, null, null, null,
                isCloseConfirmationNeeded);
    }

    /**
     * Creates a tab with the specified {@link Component} and updater action. The updater action will be invoked when this tab is selected. Note, that
     * this tab is unaware of database modifications and will not be automatically refreshed if changes occur.
     */
    public static ITabItem createWithUpdater(final String title, final Component component,
            IDelegatedAction updater, IViewContext<?> viewContext)
    {
        return new DefaultTabItem(viewContext, title, component, updater, null, null, false);
    }

    /**
     * Creates a tab with the specified {@link ContentPanel}. The tab is unaware of database modifications and will not be automatically refreshed if
     * changes occur.
     */
    public static ITabItem createUnaware(final ContentPanel component,
            boolean isCloseConfirmationNeeded, IViewContext<?> viewContext)
    {
        String title = getTabTitle(component);
        return new DefaultTabItem(viewContext, title, component, null, null, null,
                isCloseConfirmationNeeded);
    }

    private static String getTabTitle(ContentPanel contentPanel)
    {
        final Header header = contentPanel.getHeader();
        return header != null ? header.getText() : contentPanel.getId();
    }

    /**
     * Creates a tab with the specified component. The tab is aware of database modifications and will be automatically refreshed if relevant changes
     * take place.
     */
    public static ITabItem create(final String title,
            final DatabaseModificationAwareComponent component, IViewContext<?> viewContext,
            boolean isCloseConfirmationNeeded)
    {
        IDelegatedAction disposer = null;
        IDatabaseModificationObserver modificationObserver = component.getModificationObserver();
        if (modificationObserver instanceof IDisposableComponent)
        {
            disposer = createDisposer((IDisposableComponent) modificationObserver);
        }
        return create(viewContext, title, component.get(), null, component, disposer,
                isCloseConfirmationNeeded);
    }

    public static ITabItem create(final String title, final IDisposableComponent component,
            IViewContext<?> viewContext)
    {
        boolean isCloseConfirmationNeeded = false;
        IDelegatedAction disposer = createDisposer(component);
        return create(viewContext, title, component.getComponent(), null, component, disposer,
                isCloseConfirmationNeeded);
    }

    private static DefaultTabItem create(IViewContext<?> viewContext, final String title,
            final Component component, IDelegatedAction updaterOrNull,
            IDatabaseModificationObserver modificationObserver,
            IDelegatedAction disposerActionOrNull, boolean isCloseConfirmationNeeded)
    {
        LastModificationStateUpdater stateUpdater =
                new LastModificationStateUpdater(viewContext, modificationObserver);
        return new DefaultTabItem(viewContext, title, component, updaterOrNull, stateUpdater,
                disposerActionOrNull, isCloseConfirmationNeeded);
    }

    private static IDelegatedAction createDisposer(final IDisposableComponent disposableComponent)
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    disposableComponent.dispose();
                }
            };
    }

    private DefaultTabItem(IViewContext<?> viewContext, final String initialTitle,
            final Component component, IDelegatedAction updaterOrNull,
            LastModificationStateUpdater lastModificationStateUpdaterOrNull,
            IDelegatedAction disposerActionOrNull, boolean isCloseConfirmationNeeded)
    {
        assert initialTitle != null : "Unspecified title.";
        assert component != null : "Unspecified component.";
        this.updaterOrNull = updaterOrNull;
        this.historyTokenUpdater = new LastHistoryTokenUpdater(viewContext);
        this.titleUpdater = new TabTitleUpdater(initialTitle);
        this.component = component;
        this.lastModificationStateUpdaterOrNull = lastModificationStateUpdaterOrNull;
        this.disposerActionOrNull = disposerActionOrNull;
        this.isCloseConfirmationNeeded = isCloseConfirmationNeeded;
    }

    //
    // ITabItem
    //
    @Override
    public final Component getComponent()
    {
        return component;
    }

    @Override
    public final TabTitleUpdater getTabTitleUpdater()
    {
        return titleUpdater;
    }

    @Override
    public boolean isCloseConfirmationNeeded()
    {
        if (isCloseConfirmationNeeded)
        {
            return ComponentWithCloseConfirmationUtil.shouldAskForCloseConfirmation(component);
        } else
        {
            return false;
        }
    }

    @Override
    public void onActivate(String linkOrNull)
    {
        if (linkOrNull != null)
        {
            historyTokenUpdater.update(linkOrNull);
        }
        if (updaterOrNull != null)
        {
            updaterOrNull.execute();
        }
        if (lastModificationStateUpdaterOrNull != null)
        {
            lastModificationStateUpdaterOrNull.update();
        }

        GWTUtils.updatePageTitle(getTabTitleUpdater().getCurrentTitle());
    }

    @Override
    public void onRefresh(String linkOrNull)
    {
        if (component instanceof IComponentWithRefresh)
        {
            ((IComponentWithRefresh) component).refresh();
        }
    }

    @Override
    public void onClose()
    {
        if (disposerActionOrNull != null)
        {
            disposerActionOrNull.execute();
        }
    }
}
