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
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;

/**
 * A common model provider for column settings tab.
 * 
 * @author Tomasz Pylak
 */
public abstract class AbstractColumnSettingsDataModelProvider
{
    /**
     * Will be called passing the columns settings chosen by the user (with the right order) as a parameter when the Column Settings dialog closes.
     */
    protected abstract void onClose(List<ColumnDataModel> newColumnDataModels);

    private final List<ColumnDataModel> columnDataModels;

    private List<GridCustomColumn> lastLoadedCustomColumnsOrNull;

    public AbstractColumnSettingsDataModelProvider(List<ColumnDataModel> columnDataModels)
    {
        this.columnDataModels = columnDataModels;
        this.lastLoadedCustomColumnsOrNull = null;
    }

    /**
     * @return column data model including the current state of the custom columns. Note that the order may be different than the one chosen by the
     *         user.
     */
    public List<ColumnDataModel> getColumnDataModels()
    {
        return columnDataModels;
    }

    /**
     * Called when all custom columns definitions are loaded from the database. Some custom columns may have been added or deleted, it will be taken
     * into account when updating columnDataModels.
     * 
     * @param customColumns all custom columns for the grid
     */
    public void refreshCustomColumns(List<GridCustomColumn> customColumns)
    {
        // we assume that custom columns will be loaded once before any modifications will
        // happen
        if (lastLoadedCustomColumnsOrNull != null)
        {
            List<GridCustomColumn> addedColumns =
                    getFreshColumns(lastLoadedCustomColumnsOrNull, customColumns);
            addColumns(addedColumns);

            List<GridCustomColumn> deletedColumns =
                    getFreshColumns(customColumns, lastLoadedCustomColumnsOrNull);
            deleteColumns(deletedColumns);
        }
        lastLoadedCustomColumnsOrNull = customColumns;
    }

    /** @return current custom columns or null if custom columns have not finish to load. */
    public List<GridCustomColumnInfo> tryGetCustomColumnsInfo()
    {
        if (lastLoadedCustomColumnsOrNull == null)
        {
            return null;
        }
        List<GridCustomColumnInfo> result = new ArrayList<GridCustomColumnInfo>();
        for (GridCustomColumn column : lastLoadedCustomColumnsOrNull)
        {
            result.add(createCustomColumnInfo(column));
        }
        return result;
    }

    private GridCustomColumnInfo createCustomColumnInfo(GridCustomColumn column)
    {
        return new GridCustomColumnInfo(getId(column), column.getName(), column.getDescription(), column.getDataType());
    }

    private void addColumns(List<GridCustomColumn> columns)
    {
        for (GridCustomColumn column : columns)
        {
            ColumnDataModel columnDataModel = createColumnDataModel(column);
            columnDataModels.add(columnDataModel);
        }

    }

    private void deleteColumns(List<GridCustomColumn> columns)
    {
        for (GridCustomColumn column : columns)
        {
            int ix = tryFindColumnIx(getId(column));
            if (ix != -1)
            {
                columnDataModels.remove(ix);
            } else
            {
                throw new IllegalStateException("cannot remove a column " + getId(column));
            }
        }
    }

    // returns ids of columns which are pressent in 'current' and not present in 'previous'
    private static List<GridCustomColumn> getFreshColumns(List<GridCustomColumn> previous,
            List<GridCustomColumn> current)
    {
        List<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
        Set<String> previousIds = extractColumnIds(previous);
        for (GridCustomColumn column : current)
        {
            if (previousIds.contains(getId(column)) == false)
            {
                result.add(column);
            }
        }
        return result;
    }

    private static String getId(GridCustomColumn column)
    {
        return column.getCode();
    }

    private static Set<String> extractColumnIds(List<GridCustomColumn> columns)
    {
        Set<String> result = new HashSet<String>();
        for (GridCustomColumn column : columns)
        {
            result.add(getId(column));
        }
        return result;
    }

    private static ColumnDataModel createColumnDataModel(GridCustomColumn column)
    {
        return new ColumnDataModel(column.getName(), true, false, getId(column));
    }

    private int tryFindColumnIx(String columnId)
    {
        for (int i = 0; i < columnDataModels.size(); i++)
        {
            ColumnDataModel columnDataModel = columnDataModels.get(i);
            if (columnDataModel.getColumnID().equals(columnId))
            {
                return i;
            }
        }
        return -1;
    }
}