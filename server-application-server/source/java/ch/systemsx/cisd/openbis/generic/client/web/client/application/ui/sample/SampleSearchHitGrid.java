/*
 * Copyright 2009 ETH Zuerich, CISD
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
 * 
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ShowResultSetCutInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchWindow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.IDetailedSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid with detailed sample search results.
 * 
 * @author Piotr Buczek
 */
public class SampleSearchHitGrid extends SampleBrowserGrid implements IDetailedSearchHitGrid
{
    // browser consists of the grid and the paging toolbar
    public static final String SEARCH_BROWSER_ID = GenericConstants.ID_PREFIX
            + "sample-search-hit-browser";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, ListSampleDisplayCriteria.createForSearch());
        SampleSearchHitGrid grid = new SampleSearchHitGrid(viewContext, criteriaProvider);
        final DetailedSearchWindow searchWindow =
                new DetailedSearchWindow(viewContext, EntityKind.SAMPLE);
        final DetailedSearchToolbar toolbar =
                new DetailedSearchToolbar(grid, viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY),
                        searchWindow);
        searchWindow.setUpdateListener(toolbar);
        return grid.asDisposableWithToolbar(toolbar);
    }

    /**
     * Create a search hit grid and initialize the search with the display criteria.
     */
    public static IDisposableComponent createWithInitialDisplayCriteria(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            ListSampleDisplayCriteria displayCriteria)
    {
        // Use the caller-provided display criteria
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, displayCriteria);
        SampleSearchHitGrid grid = new SampleSearchHitGrid(viewContext, criteriaProvider);
        final DetailedSearchWindow searchWindow =
                new DetailedSearchWindow(viewContext, EntityKind.SAMPLE);

        // Set the initial search string before creating the toolbar because the toolbar will use
        // the initial search string in its own initialization.
        searchWindow.setInitialSearchCriteria(displayCriteria.getSearchCriteria());

        final DetailedSearchToolbar toolbar =
                new DetailedSearchToolbar(grid, viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY),
                        searchWindow, true);
        searchWindow.setUpdateListener(toolbar);

        return grid.asDisposableWithToolbar(toolbar);
    }

    private SampleSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ISampleCriteriaProvider criteriaProvider)
    {
        super(viewContext, criteriaProvider, SEARCH_BROWSER_ID, false,
                DisplayTypeIDGenerator.SAMPLE_SEARCH_RESULT_GRID,
                DUMMY_DIRECTLY_CONNECTED_CONTROLLER);
        updateCriteriaProviderAndRefresh();
        extendBottomToolbar();
    }

    @Override
    protected void addEntityOperationButtons()
    {
        String showRelatedDatasetsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_RELATED_DATASETS);
        Button showRelatedDatasetsButton =
                new Button(showRelatedDatasetsTitle, new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            // TODO, 2010-12-13, FJE, show related data sets isn't easy because
                            // TableModelRowWithObject doesn't implement IEntityInformationHolder.
                            // Changing the code is relatively easy but the method
                            // showRelatedDataSets()
                            // is also used by MatchingEntitiesPanel.
                            showRelatedDataSets(viewContext, SampleSearchHitGrid.this);
                        }
                    });
        addButton(showRelatedDatasetsButton);

        super.addEntityOperationButtons();
    }

    @Override
    public void refresh(DetailedSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        ListSampleDisplayCriteria criteriaOrNull = tryGetDisplayCriteria();
        assert criteriaOrNull != null;
        criteriaOrNull.updateSearchCriteria(newCriteria);
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (isAnySearchCriteriaSet())
        {
            super.refresh();
        }
    }

    private boolean isAnySearchCriteriaSet()
    {
        ListSampleDisplayCriteria criteriaOrNull = tryGetDisplayCriteria();
        return criteriaOrNull != null && (criteriaOrNull.getSearchCriteria().isEmpty() == false);
    }

    private ListSampleDisplayCriteria tryGetDisplayCriteria()
    {
        return getCriteriaProvider().tryGetCriteria();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        callback.addOnSuccessAction(new ShowResultSetCutInfo<TypedTableResultSet<Sample>>(
                viewContext));
        super.listTableRows(resultSetConfig, callback);
    }

    @Override
    public IDelegatedActionWithResult<SelectedAndDisplayedItems> getSelectedAndDisplayedItemsAction()
    {
        throw new UnsupportedOperationException();
    }
}
