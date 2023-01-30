/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

class TableData<K, T>
{
    private final K dataKey;

    private final List<T> originalData;

    @IgnoreSizeOf
    private final ICustomColumnsProvider customColumnsProvider;

    @IgnoreSizeOf
    private final IColumnCalculator columnCalculator;

    @IgnoreSizeOf
    private final List<TableModelColumnHeader> headers;

    @IgnoreSizeOf
    private final List<IColumnDefinition<T>> headerColumnDefinitions =
            new ArrayList<IColumnDefinition<T>>();

    @IgnoreSizeOf
    private Map<String, Column> calculatedColumns = new HashMap<String, Column>();

    TableData(K dataKey, List<T> originalData, List<TableModelColumnHeader> headers,
            ICustomColumnsProvider customColumnsProvider, IColumnCalculator columnCalculator)
    {
        this.dataKey = dataKey;
        this.originalData = originalData;
        this.headers = headers;
        for (final TableModelColumnHeader header : headers)
        {
            headerColumnDefinitions.add(new IColumnDefinition<T>()
                {

                    @Override
                    public String getValue(GridRowModel<T> rowModel)
                    {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel)
                    {
                        T originalObject = rowModel.getOriginalObject();
                        if (originalObject instanceof TableModelRow)
                        {
                            TableModelRow row = (TableModelRow) originalObject;
                            return row.getValues().get(header.getIndex());
                        }
                        return null;
                    }

                    @Override
                    public String getHeader()
                    {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String getIdentifier()
                    {
                        return header.getId();
                    }

                    @Override
                    public DataTypeCode tryToGetDataType()
                    {
                        return null;
                    }

                    @Override
                    public String tryToGetProperty(String key)
                    {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isCustom()
                    {
                        return false;
                    }
                });
        }
        this.customColumnsProvider = customColumnsProvider;
        this.columnCalculator = columnCalculator;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("TableData%").append(dataKey);
        builder.append("[columns: ").append(headers.size());
        builder.append(", rows: ").append(originalData.size());
        if (originalData.isEmpty() == false)
        {
            T firstRow = originalData.get(0);
            Class<? extends Object> rowClass = firstRow.getClass();
            if (firstRow instanceof TableModelRowWithObject)
            {
                @SuppressWarnings("rawtypes")
                Serializable objectOrNull = ((TableModelRowWithObject) firstRow).getObjectOrNull();
                if (objectOrNull != null)
                {
                    rowClass = objectOrNull.getClass();
                }
            }
            builder.append(", row type: ").append(rowClass.getSimpleName());
        }
        return builder.append("]").toString();
    }

    GridRowModels<T> getRows(String sessionToken, IResultSetConfig<?, T> config)
    {
        Set<Entry<String, Column>> cachedColumns = calculatedColumns.entrySet();
        List<GridCustomColumn> allCustomColumnDefinitions =
                loadAllCustomColumnDefinitions(sessionToken, config);
        List<GridCustomColumn> neededCustomColumns =
                filterNeededCustomColumnDefinitions(allCustomColumnDefinitions, config);
        removedColumnsNoLongerNeeded(cachedColumns, neededCustomColumns);

        for (GridCustomColumn customColumn : neededCustomColumns)
        {
            String code = customColumn.getCode();
            Column column = calculatedColumns.get(code);
            if (column == null)
            {
                Set<IColumnDefinition<T>> availableColumns = getAvailableColumns(config);
                boolean errorMessageLong = config.isCustomColumnErrorMessageLong();
                long time = System.currentTimeMillis();
                List<PrimitiveValue> values =
                        columnCalculator.evalCustomColumn(originalData, customColumn,
                                availableColumns, errorMessageLong);
                CachedResultSetManager.operationLog.info((System.currentTimeMillis() - time)
                        + "ms for calculating column '" + customColumn.getName() + "' over "
                        + originalData.size() + " rows.");
                column = new Column(customColumn);
                column.setValues(values);
                calculatedColumns.put(code, column);
            }
        }

        List<GridRowModel<T>> rows = new ArrayList<GridRowModel<T>>();
        for (int i = 0; i < originalData.size(); i++)
        {
            T rowData = originalData.get(i);
            HashMap<String, PrimitiveValue> customColumnValues =
                    new HashMap<String, PrimitiveValue>();
            for (Entry<String, Column> entry : cachedColumns)
            {
                String columnCode = entry.getKey();
                customColumnValues.put(columnCode, entry.getValue().getValues().get(i));
            }
            rows.add(new GridRowModel<T>(rowData, customColumnValues));
        }

        List<ColumnDistinctValues> columnDistinctValues =
                calculateColumnDistinctValues(rows, config.getFilters());
        List<GridCustomColumnInfo> customColumnInfos =
                extractColumnInfos(allCustomColumnDefinitions);

        return new GridRowModels<T>(rows, headers, customColumnInfos, columnDistinctValues);
    }

    private Set<IColumnDefinition<T>> getAvailableColumns(IResultSetConfig<?, T> config)
    {
        Set<IColumnDefinition<T>> columns = new HashSet<IColumnDefinition<T>>();
        HashSet<String> columnIDs = new HashSet<String>();
        for (IColumnDefinition<T> definition : headerColumnDefinitions)
        {
            columns.add(definition);
            columnIDs.add(definition.getIdentifier());
        }
        Set<IColumnDefinition<T>> columnsFromConfig = config.getAvailableColumns();
        for (IColumnDefinition<T> definition : columnsFromConfig)
        {
            if (columnIDs.contains(definition.getIdentifier()) == false)
            {
                columns.add(definition);
            }
        }
        return columns;
    }

    private List<GridCustomColumn> loadAllCustomColumnDefinitions(String sessionToken,
            IResultSetConfig<?, T> resultConfig)
    {
        String gridId = resultConfig.tryGetGridDisplayId();
        ArrayList<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
        if (gridId != null)
        {
            List<GridCustomColumn> columns =
                    customColumnsProvider.getGridCustomColumn(sessionToken, gridId);
            for (GridCustomColumn column : columns)
            {
                result.add(column);
            }
        }
        return result;
    }

    private List<GridCustomColumn> filterNeededCustomColumnDefinitions(
            List<GridCustomColumn> allDefinitions, IResultSetConfig<?, T> resultConfig)
    {
        Set<String> ids = gatherAllColumnIDs(resultConfig);
        ArrayList<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
        for (GridCustomColumn column : allDefinitions)
        {
            if (ids.contains(column.getCode()))
            {
                result.add(column);
            }
        }
        return result;
    }

    private Set<String> gatherAllColumnIDs(final IResultSetConfig<?, T> resultConfig)
    {
        Set<String> ids = new HashSet<String>();
        Set<String> idsOfPresentedColumns = resultConfig.getIDsOfPresentedColumns();
        if (idsOfPresentedColumns != null)
        {
            ids.addAll(idsOfPresentedColumns);
        }
        String sortField = resultConfig.getSortInfo().getSortField();
        if (sortField != null)
        {
            ids.add(sortField);
        }
        GridFilters<T> filters = resultConfig.getFilters();
        List<GridColumnFilterInfo<T>> filterInfos = filters.tryGetFilterInfos();
        if (filterInfos != null)
        {
            for (GridColumnFilterInfo<T> filterInfo : filterInfos)
            {
                ids.add(filterInfo.getFilteredField().getIdentifier());
            }
        }
        CustomFilterInfo<T> customFilterInfo = filters.tryGetCustomFilterInfo();
        if (customFilterInfo != null)
        {
            String expression = customFilterInfo.getExpression();
            Set<IColumnDefinition<T>> availableColumns = resultConfig.getAvailableColumns();
            for (IColumnDefinition<T> columnDefinition : availableColumns)
            {
                String identifier = columnDefinition.getIdentifier();
                if (expression.indexOf(identifier) >= 0)
                {
                    ids.add(identifier);
                }
            }
        }
        return ids;
    }

    private void removedColumnsNoLongerNeeded(Set<Entry<String, Column>> cachedColumns,
            List<GridCustomColumn> neededCustomColumns)
    {
        Map<String, GridCustomColumn> map = new HashMap<String, GridCustomColumn>();
        for (GridCustomColumn customColumn : neededCustomColumns)
        {
            map.put(customColumn.getCode(), customColumn);
        }

        for (Iterator<Entry<String, Column>> iterator = cachedColumns.iterator(); iterator
                .hasNext();)
        {
            Entry<String, Column> entry = iterator.next();
            String code = entry.getKey();
            GridCustomColumn customColumn = map.get(code);
            if (customColumn == null)
            {
                iterator.remove();
            } else if (entry.getValue().hasSameExpression(customColumn) == false)
            {
                iterator.remove();
            }
        }
    }

    private List<ColumnDistinctValues> calculateColumnDistinctValues(
            List<GridRowModel<T>> rowModels, GridFilters<T> gridFilters)
    {
        List<ColumnDistinctValues> result = new ArrayList<ColumnDistinctValues>();
        List<GridColumnFilterInfo<T>> filterInfos = gridFilters.tryGetFilterInfos();
        if (filterInfos == null)
        {
            return result;
        }
        for (GridColumnFilterInfo<T> column : filterInfos)
        {
            ColumnDistinctValues distinctValues =
                    tryCalculateColumnDistinctValues(rowModels, column.getFilteredField());
            if (distinctValues != null)
            {
                result.add(distinctValues);
            }
        }
        return result;
    }

    /** @return null if values are not from a small set */
    private ColumnDistinctValues tryCalculateColumnDistinctValues(
            List<GridRowModel<T>> rowModels, IColumnDefinition<T> column)
    {
        Set<String> distinctValues = new LinkedHashSet<String>();
        for (GridRowModel<T> rowModel : rowModels)
        {
            String value = CachedResultSetManager.getOriginalValue(column, rowModel);
            distinctValues.add(value);
            if (distinctValues.size() > CachedResultSetManager.MAX_DISTINCT_COLUMN_VALUES_SIZE)
            {
                return null;
            }
        }
        ArrayList<String> distinctValuesList = new ArrayList<String>(distinctValues);
        return new ColumnDistinctValues(column.getIdentifier(), distinctValuesList);
    }

    private List<GridCustomColumnInfo> extractColumnInfos(
            List<GridCustomColumn> allCustomColumnDefinitions)
    {
        List<GridCustomColumnInfo> result = new ArrayList<GridCustomColumnInfo>();
        for (GridCustomColumn definition : allCustomColumnDefinitions)
        {
            Column column = calculatedColumns.get(definition.getCode());
            if (column != null)
            {
                result.add(column.getInfo());
            } else
            {
                result.add(CachedResultSetManager.translate(definition));
            }
        }
        return result;
    }
}