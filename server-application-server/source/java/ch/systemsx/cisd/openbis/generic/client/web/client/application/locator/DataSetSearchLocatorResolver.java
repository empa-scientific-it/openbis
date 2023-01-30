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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * A resolver that takes search critera, executes a dataset search, and displays the results.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetSearchLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public DataSetSearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public void openEntitySearch(DetailedSearchCriteria searchCriteria, String historyToken)
            throws UserFailureException
    {
        // Open the search view using the provided criteria
        OpenEntitySearchGridTabAction searchAction =
                new OpenEntitySearchGridTabAction(searchCriteria, historyToken, viewContext);
        DispatcherHelper.dispatchNaviEvent(searchAction);
    }

    private static class OpenEntitySearchGridTabAction extends AbstractTabItemFactory
    {
        private final DetailedSearchCriteria searchCriteria;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final String historyToken;

        private OpenEntitySearchGridTabAction(DetailedSearchCriteria searchCriteria,
                String historyToken, IViewContext<ICommonClientServiceAsync> viewContext)
        {
            this.searchCriteria = searchCriteria;
            this.historyToken = historyToken;
            this.viewContext = viewContext;
        }

        private String getMessage(String key)
        {
            return viewContext.getMessage(key);
        }

        private ITabItem createTab(String dictionaryMsgKey, IDisposableComponent component)
        {
            String title = getMessage(dictionaryMsgKey);
            return DefaultTabItem.create(title, component, viewContext);
        }

        @Override
        public ITabItem create()
        {
            IDisposableComponent browser =
                    DataSetSearchHitGrid.createWithInitialSearchCriteria(viewContext,
                            searchCriteria, false);
            return createTab(Dict.DATA_SET_SEARCH, browser);
        }

        @Override
        public String getId()
        {
            return DataSetSearchHitGrid.BROWSER_ID;
        }

        @Override
        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.SEARCH);
        }

        @Override
        public String getTabTitle()
        {
            return getMessage(Dict.DATA_SET_SEARCH);
        }

        @Override
        public String tryGetLink()
        {
            return historyToken;
        }
    }
}