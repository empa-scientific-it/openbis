/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.mvc.Dispatcher;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * @author Kaloyan Enimanev
 */
public class GlobalSearchTabItemFactory
{

    /**
     * opens a new tab if there are search results.
     */
    public static void openTabIfEntitiesFound(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final SearchableEntity searchableEntity, final String queryText, final boolean useWildcardSearchMode)
    {

        openTab(viewContext, searchableEntity, queryText, false, useWildcardSearchMode);
    }

    /**
     * always opens a new tab, regardless if there were any search entities found.
     */
    public static void openTab(final IViewContext<ICommonClientServiceAsync> viewContext,
            final SearchableEntity searchableEntity, final String queryText, final boolean useWildcardSearchMode)
    {

        openTab(viewContext, searchableEntity, queryText, true, useWildcardSearchMode);
    }

    private static void openTab(final IViewContext<ICommonClientServiceAsync> viewContext,
            final SearchableEntity searchableEntity, final String queryText,
            final boolean openIfNoEntitiesFound, final boolean useWildcardSearchMode)
    {
        Dispatcher.get().fireEvent(AppEvents.GLOBAL_SEARCH_STARTED_EVENT);

        final MatchingEntitiesPanel matchingEntitiesGrid =
                new MatchingEntitiesPanel(viewContext, searchableEntity, queryText,
                        useWildcardSearchMode);

        String entityDescription =
                (searchableEntity != null) ? searchableEntity.getDescription() : null;
        String title = createTabTitle(viewContext, entityDescription, queryText);

        final AbstractTabItemFactory tabFactory =
                createTabFactory(matchingEntitiesGrid, title, viewContext);

        matchingEntitiesGrid.refresh(new IDataRefreshCallback()
            {
                private boolean firstCall = true;

                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    Dispatcher.get().fireEvent(AppEvents.GLOBAL_SEARCH_FINISHED_EVENT);

                    if (firstCall == false)
                    {
                        return;
                    }
                    firstCall = false;
                    if (matchingEntitiesGrid.getRowNumber() == 0)
                    {
                        Object[] msgParameters =
                                (useWildcardSearchMode == true) ? new String[] { queryText, "", "off", } : new String[] { queryText, "not", "on" };
                        GWTUtils.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                                viewContext.getMessage(Dict.NO_MATCH, msgParameters));

                        if (openIfNoEntitiesFound == false)
                        {
                            return;
                        }
                    }

                    DispatcherHelper.dispatchNaviEvent(tabFactory);
                }
            });
    }

    private static String createTabTitle(IViewContext<ICommonClientServiceAsync> viewContext,
            String chosenEntity, String queryText)
    {
        String entity = (chosenEntity != null) ? chosenEntity : "All";
        return viewContext.getMessage(Dict.GLOBAL_SEARCH, entity, queryText);
    }

    private static AbstractTabItemFactory createTabFactory(
            final MatchingEntitiesPanel matchingEntitiesPanel, final String title,
            IViewContext<?> viewContext)
    {
        final ITabItem tab =
                DefaultTabItem.create(title, matchingEntitiesPanel.asDisposableComponent(),
                        viewContext);
        // this tab cannot be opened for the second time, so we can create it outside of the
        // factory
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return tab;
                }

                @Override
                public String getId()
                {
                    return matchingEntitiesPanel.getId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SEARCH, HelpPageAction.ACTION);
                }

                @Override
                public String getTabTitle()
                {
                    return title;
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }
}
