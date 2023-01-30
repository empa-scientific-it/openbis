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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ShowResultSetCutInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedDataSetSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchWindow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.IDetailedSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid with detailed data set search results.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitGrid extends AbstractExternalDataGrid implements
        IDetailedSearchHitGrid
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "data-set-search-hit-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return create(viewContext, null, false);
    }

    public static DisposableEntityChooser<TableModelRowWithObject<AbstractExternalData>> createWithInitialSearchCriteria(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            DetailedSearchCriteria searchCriteria, boolean forChooser)
    {
        return create(viewContext, searchCriteria, forChooser);
    }

    private static DisposableEntityChooser<TableModelRowWithObject<AbstractExternalData>> create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            DetailedSearchCriteria searchCriteriaOrNull, boolean forChooser)
    {
        DataSetSearchHitGrid grid = new DataSetSearchHitGrid(viewContext, forChooser);
        final DetailedSearchWindow searchWindow =
                new DetailedSearchWindow(viewContext, EntityKind.DATA_SET);

        if (searchCriteriaOrNull != null)
        {
            grid.chosenSearchCriteria = searchCriteriaOrNull;
            // Set the initial search string before creating the toolbar
            // because the toolbar will use the initial search string in its own initialization.
            searchWindow.setInitialSearchCriteria(searchCriteriaOrNull);
        }

        final IDisposableComponent metadataComponent = grid.asDisposableWithoutToolbar();
        final LayoutContainerWithDisposableComponent containerHolder =
                createContainerHolder(metadataComponent);
        final IOnReportComponentGeneratedAction reportGeneratedAction =
                createReportGeneratedAction(containerHolder, grid);
        final ReportingPluginSelectionWidget reportSelectionWidget =
                new ReportingPluginSelectionWidget(viewContext, null);

        SelectionChangedListener<DatastoreServiceDescriptionModel> reportChangedListener =
                DataSetGridUtils.createReportSelectionChangedListener(viewContext,
                        grid.asDisposableWithoutToolbar(), reportGeneratedAction);
        reportSelectionWidget.addSelectionChangedListener(reportChangedListener);

        final DetailedSearchToolbar toolbar =
                new DetailedDataSetSearchToolbar(viewContext, grid,
                        viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY), searchWindow,
                        reportSelectionWidget, searchCriteriaOrNull != null);
        searchWindow.setUpdateListener(toolbar);

        return grid.asDisposableWithToolbar(containerHolder, toolbar);
    }

    private static LayoutContainerWithDisposableComponent createContainerHolder(
            IDisposableComponent metadataComponent)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        return new LayoutContainerWithDisposableComponent(container, metadataComponent);
    }

    private static IOnReportComponentGeneratedAction createReportGeneratedAction(
            final LayoutContainerWithDisposableComponent containerHolder,
            final DataSetSearchHitGrid metadataGrid)
    {
        return new IOnReportComponentGeneratedAction()
            {
                @Override
                public void execute(IDisposableComponent newGridComponent)
                {
                    final LayoutContainer container = containerHolder.getContainer();
                    final IDisposableComponent disposableComponent =
                            containerHolder.getDisposableComponent();

                    // remove second widget (first is the toolbar)
                    Widget widget = container.getWidget(1);
                    container.remove(widget);

                    // dispose if it wasn't the main grid component
                    if (disposableComponent != null
                            && disposableComponent.getComponent().equals(metadataGrid) == false)
                    {
                        disposableComponent.dispose();
                    }

                    // update new component
                    containerHolder.setDisposableComponent(newGridComponent);
                    container.add(newGridComponent.getComponent(), new RowData(1, 1));
                    container.layout();
                }
            };
    }

    private DetailedSearchCriteria chosenSearchCriteria;

    private final boolean forChooser;

    private DataSetSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean forChooser)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.DATA_SET_SEARCH_RESULT_GRID);
        this.forChooser = forChooser;
        if (forChooser)
        {
            disallowMultipleSelection();
        }
    }

    @Override
    protected ICellListenerAndLinkGenerator<AbstractExternalData> tryGetCellListenerAndLinkGenerator(
            String columnId)
    {
        if (forChooser)
        {
            return null;
        }
        return super.tryGetCellListenerAndLinkGenerator(columnId);
    }

    @Override
    protected boolean isEditable(BaseEntityModel<TableModelRowWithObject<AbstractExternalData>> model,
            String columnID)
    {
        return forChooser ? false : super.isEditable(model, columnID);
    }

    @Override
    protected void showNonEditableTableCellMessage(
            BaseEntityModel<TableModelRowWithObject<AbstractExternalData>> model, String columnID)
    {
        if (forChooser)
        {
            return;
        }
        super.showNonEditableTableCellMessage(model, columnID);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        callback.addOnSuccessAction(new ShowResultSetCutInfo<TypedTableResultSet<AbstractExternalData>>(
                viewContext));
        viewContext.getService().searchForDataSets(chosenSearchCriteria, resultSetConfig, callback);
    }

    @Override
    public void refresh(DetailedSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        chosenSearchCriteria = newCriteria;
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (chosenSearchCriteria == null)
        {
            return;
        }
        super.refresh();
    }

    /** @return this grid as a disposable component with a specified toolbar at the top. */
    private DisposableEntityChooser<TableModelRowWithObject<AbstractExternalData>> asDisposableWithToolbar(
            final LayoutContainerWithDisposableComponent containerHolder,
            final IDisposableComponent toolbar)
    {
        final LayoutContainer container = containerHolder.getContainer();
        container.add(toolbar.getComponent());
        container.add(this, new RowData(1, 1));
        IDisposableComponent dynamicComponentProvider =
                createDynamicComponentProvider(containerHolder);
        return asDisposableEntityChooser(container, toolbar, dynamicComponentProvider);
    }

    private IDisposableComponent createDynamicComponentProvider(
            final LayoutContainerWithDisposableComponent containerHolder)
    {
        // dynamically delegates to the disposable component currently stored in containerHolder
        return new IDisposableComponent()
            {

                private IDisposableComponent getDisposableComponent()
                {
                    return containerHolder.getDisposableComponent();
                }

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    getDisposableComponent().update(observedModifications);
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return getDisposableComponent().getRelevantModifications();
                }

                @Override
                public Component getComponent()
                {
                    return getDisposableComponent().getComponent();
                }

                @Override
                public void dispose()
                {
                    getDisposableComponent().dispose();
                }
            };
    }

    /** Holder of a {@link LayoutContainer} and a {@link IDisposableComponent} of a grid inside it. */
    private static class LayoutContainerWithDisposableComponent
    {
        private final LayoutContainer container;

        private IDisposableComponent disposableComponent;

        public LayoutContainerWithDisposableComponent(LayoutContainer container,
                IDisposableComponent component)
        {
            this.container = container;
            this.disposableComponent = component;
        }

        public IDisposableComponent getDisposableComponent()
        {
            return disposableComponent;
        }

        public void setDisposableComponent(IDisposableComponent disposableComponent)
        {
            this.disposableComponent = disposableComponent;
        }

        public LayoutContainer getContainer()
        {
            return container;
        }

    }

}
