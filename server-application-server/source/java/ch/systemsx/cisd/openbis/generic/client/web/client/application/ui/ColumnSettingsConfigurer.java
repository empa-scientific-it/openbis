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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnSettingsDataModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnSettingsDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.CustomColumnsMetadataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A class that manages the configuring of column settings in the AbstractBrowserGrid.
 * <p>
 * Considered a friend class to AbstractBrowserGried, i.e., it is allowed to use methods that are otherwise private.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ColumnSettingsConfigurer<T extends Serializable>
{
    public static final int DEFAULT_COLUMN_WIDTH = 150;

    private final TypedTableGrid<T> browserGrid;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final FilterToolbar<TableModelRowWithObject<T>> filterToolbar;

    private final CustomColumnsMetadataProvider customColumnsMetadataProvider;

    // result set key of the last refreshed data
    private final String resultSetKeyOrNull;

    // not null only if there is a pending request. No new request can be issued until this one is
    // finished.
    private final ResultSetFetchConfig<String> pendingFetchConfigOrNull;

    public ColumnSettingsConfigurer(TypedTableGrid<T> browserGrid,
            IViewContext<ICommonClientServiceAsync> viewContext,
            FilterToolbar<TableModelRowWithObject<T>> filterToolbar,
            CustomColumnsMetadataProvider customColumnsMetadataProvider, String resultSetKeyOrNull,
            ResultSetFetchConfig<String> pendingFetchConfigOrNull)
    {
        this.browserGrid = browserGrid;
        this.viewContext = viewContext;
        this.filterToolbar = filterToolbar;
        this.customColumnsMetadataProvider = customColumnsMetadataProvider;
        this.resultSetKeyOrNull = resultSetKeyOrNull;
        this.pendingFetchConfigOrNull = pendingFetchConfigOrNull;
    }

    public void showDialog()
    {
        List<ColumnDataModel> settingsModel =
                TypedTableGrid.createColumnsSettingsModel(browserGrid.getFullColumnModel(),
                        filterToolbar.extractFilteredColumnIds());
        AbstractColumnSettingsDataModelProvider provider =
                new AbstractColumnSettingsDataModelProvider(settingsModel)
                    {
                        @Override
                        public void onClose(List<ColumnDataModel> newColumnDataModels)
                        {
                            final ColumnModel newColumnModel =
                                    createNewColumnModel(newColumnDataModels);
                            final List<String> filteredColumnIds =
                                    getFilteredColumnIds(newColumnDataModels);
                            String gridDisplayTypeID = browserGrid.getGridDisplayTypeID();
                            DisplaySettingsManager displaySettingsManager =
                                    viewContext.getDisplaySettingsManager();
                            displaySettingsManager.storeSettings(gridDisplayTypeID,
                                    new IDisplaySettingsGetter()
                                        {
                                            @Override
                                            public Object getModifier()
                                            {
                                                return browserGrid;
                                            }

                                            @Override
                                            public List<String> getFilteredColumnIds()
                                            {
                                                return filteredColumnIds;
                                            }

                                            @Override
                                            public ColumnModel getColumnModel()
                                            {
                                                return newColumnModel;
                                            }

                                            @Override
                                            public SortInfo getSortState()
                                            {
                                                return browserGrid.getGridSortInfo();
                                            }
                                        }, false);

                            // refresh the whole grid if custom columns changed
                            List<GridCustomColumnInfo> newCustomColumns = tryGetCustomColumnsInfo();
                            if (newCustomColumns != null)
                            {
                                customColumnsMetadataProvider
                                        .setCustomColumnsMetadata(newCustomColumns);
                            }
                            boolean customColumnsChanged =
                                    customColumnsMetadataProvider.getHasChangedAndSetFalse();
                            browserGrid.recreateColumnModelAndRefreshColumnsWithFilters();

                            boolean columnFiltersChanged =
                                    browserGrid.rebuildFiltersFromIds(filteredColumnIds);

                            if (customColumnsChanged || columnFiltersChanged)
                            {
                                browserGrid
                                        .debug("refreshing custom columns and/or filter distinct value in "
                                                + pendingFetchConfigOrNull + " mode");
                                // we do not need to reload data if custom filters changed (we do
                                // not need distinct column values)
                                browserGrid.reloadData(createRefreshSettingsFetchConfig());
                            }
                            // settings will be automatically stored because of event handling
                            browserGrid.refreshColumnsSettings();
                            filterToolbar.refresh();
                        }

                        private ResultSetFetchConfig<String> createRefreshSettingsFetchConfig()
                        {
                            if (pendingFetchConfigOrNull == null)
                            {
                                if (resultSetKeyOrNull == null)
                                {
                                    return ResultSetFetchConfig.createComputeAndCache();
                                } else
                                {
                                    return ResultSetFetchConfig
                                            .createFetchFromCacheAndRecompute(resultSetKeyOrNull);
                                }
                            } else
                            {
                                return pendingFetchConfigOrNull;
                            }
                        }
                    };
        ColumnSettingsDialog.show(viewContext, provider, browserGrid.getGridDisplayTypeID());
    }

    /**
     * Creates a new column model based on the specified column data models and the old full column model as provided by
     * {@link TypedTableGrid#getFullColumnModel()}.
     */
    private ColumnModel createNewColumnModel(List<ColumnDataModel> newColumnDataModels)
    {
        List<ColumnConfig> oldColumnsList = getOldColumnsList();
        Map<String, ColumnConfig> oldColumnsMap = getOldColumnsMap();

        List<ColumnConfig> columnsList = new LinkedList<ColumnConfig>();
        Map<String, ColumnConfig> columnsMap = new HashMap<String, ColumnConfig>();

        for (ColumnDataModel columnDataModel : newColumnDataModels)
        {
            String columnID = columnDataModel.getColumnID();
            ColumnConfig column = oldColumnsMap.get(columnID);
            if (column == null)
            {
                String header = columnDataModel.getHeader();
                column =
                        new ColumnConfig(columnID, header,
                                ColumnSettingsConfigurer.DEFAULT_COLUMN_WIDTH);
            }
            column.setHidden(columnDataModel.isVisible() == false);
            columnsList.add(column);
            columnsMap.put(column.getId(), column);
        }

        // LMS-2711
        // Do not loose columns that are not available in a model of the grid but are in the
        // settings. We have to remember their position for other views that may use the same
        // settings but have a different model structure.

        int index = 0;
        for (ColumnConfig oldColumn : oldColumnsList)
        {
            if (!columnsMap.containsKey(oldColumn.getId()))
            {
                columnsList.add(index, oldColumn);
            }
            index++;
        }

        return new ColumnModel(columnsList);
    }

    private List<ColumnConfig> getOldColumnsList()
    {
        return browserGrid.getFullColumnModel().getColumns();
    }

    private Map<String, ColumnConfig> getOldColumnsMap()
    {
        List<ColumnConfig> columns = getOldColumnsList();
        HashMap<String, ColumnConfig> map = new HashMap<String, ColumnConfig>();
        for (ColumnConfig columnConfig : columns)
        {
            map.put(columnConfig.getId(), columnConfig);
        }
        return map;
    }

    private static List<String> getFilteredColumnIds(List<ColumnDataModel> result)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (ColumnDataModel model : result)
        {
            if (model.hasFilter() && filteredColumnsIds.size() < FilterToolbar.MAX_FILTER_FIELDS)
            {
                filteredColumnsIds.add(model.getColumnID());
            }
        }
        return filteredColumnsIds;
    }

}
