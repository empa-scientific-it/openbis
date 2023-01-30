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

import com.google.gwt.user.client.rpc.AsyncCallback;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * A resolver that takes search critera, executes a sample search, and displays the results.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleSearchLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SampleSearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public void openEntitySearch(DetailedSearchCriteria searchCriteria, String historyToken)
            throws UserFailureException
    {
        // Create a display criteria object for the search string
        ListSampleDisplayCriteria displayCriteria = ListSampleDisplayCriteria.createForSearch();
        displayCriteria.updateSearchCriteria(searchCriteria);

        // Do a search first and based on the results of the search, open either the details view or
        // the search view
        viewContext.getCommonService().listSamples(displayCriteria,
                new OpenEntitySearchTabCallback(displayCriteria, historyToken));
    }

    private class OpenEntitySearchTabCallback implements
            AsyncCallback<ResultSetWithEntityTypes<Sample>>
    {
        private final ListSampleDisplayCriteria displayCriteria;

        private final String historyToken;

        private OpenEntitySearchTabCallback(ListSampleDisplayCriteria displayCriteria,
                String historyToken)
        {
            this.displayCriteria = displayCriteria;
            this.historyToken = historyToken;
        }

        @Override
        public final void onFailure(Throwable caught)
        {
            // Error in the search -- notify the user
            GWTUtils.alert("Error", caught.getMessage());
        }

        @Override
        public final void onSuccess(ResultSetWithEntityTypes<Sample> result)
        {
            // If the search found just one sample, show it in the details view. If many samples
            // were found, open the search view.
            switch (result.getResultSet().getTotalLength())
            {
            // Nothing found -- notify the user
                case 0:
                    GWTUtils.alert("Error", "No " + viewContext.getMessage(Dict.SAMPLES).toLowerCase() + " matching criteria ["
                            + displayCriteria.getSearchCriteria().toString() + "] were found.");
                    break;
                // One result found -- show it in the details view
                case 1:
                    Sample sample = result.getResultSet().getList().get(0).getOriginalObject();
                    OpenEntityDetailsTabAction detailsAction =
                            new OpenEntityDetailsTabAction(sample, viewContext);
                    detailsAction.execute();
                    break;

                // Multiple results found -- show them in a grid
                default:
                    OpenEntitySearchGridTabAction searchAction =
                            new OpenEntitySearchGridTabAction(displayCriteria, viewContext,
                                    historyToken);
                    DispatcherHelper.dispatchNaviEvent(searchAction);

                    break;
            }
        }
    }

    private static class OpenEntitySearchGridTabAction extends AbstractTabItemFactory
    {
        private final ListSampleDisplayCriteria displayCriteria;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final String historyToken;

        private OpenEntitySearchGridTabAction(ListSampleDisplayCriteria displayCriteria,
                IViewContext<ICommonClientServiceAsync> viewContext, String historyToken)
        {
            this.displayCriteria = displayCriteria;
            this.viewContext = viewContext;
            this.historyToken = historyToken;
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
                    SampleSearchHitGrid.createWithInitialDisplayCriteria(viewContext,
                            displayCriteria);
            return createTab(Dict.SAMPLE_SEARCH, browser);
        }

        @Override
        public String getId()
        {
            return SampleSearchHitGrid.SEARCH_BROWSER_ID;
        }

        @Override
        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.SEARCH);
        }

        @Override
        public String getTabTitle()
        {
            return getMessage(Dict.SAMPLE_SEARCH);
        }

        @Override
        public String tryGetLink()
        {
            return historyToken;
        }
    }
}