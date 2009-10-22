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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * A {@link IResultSetManager} implementation which caches the full data retrieved using
 * {@link IOriginalDataProvider}.
 * 
 * @author Christian Ribeaud
 */
public final class CachedResultSetManager<K> implements IResultSetManager<K>, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CachedResultSetManager.class);

    /** how many values can one column have to consider it reasonably distinct */
    @Private
    static final int MAX_DISTINCT_COLUMN_VALUES_SIZE = 50;

    private final IResultSetKeyGenerator<K> resultSetKeyProvider;

    private final ICustomColumnsProvider customColumnsProvider;

    @Private
    final Map<K, List<?>> results = new HashMap<K, List<?>>();

    public interface ICustomColumnsProvider
    {
        // used to fetch grid custom columns definition for the specified grid id
        List<GridCustomColumn> getGridCustomColumn(String sessionToken, String gridDisplayId);
    }

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider,
            ICustomColumnsProvider customColumnsProvider)
    {
        this.resultSetKeyProvider = resultSetKeyProvider;
        this.customColumnsProvider = customColumnsProvider;
    }

    @SuppressWarnings("unchecked")
    private final <T> T cast(final Object object)
    {
        return (T) object;
    }

    /**
     * Server-side filter info object.
     */
    private static class FilterInfo<T>
    {
        private final IColumnDefinition<T> filteredField;

        private final String[] filterExpressionAlternatives;

        private FilterInfo(GridFilterInfo<T> gridFilterInfo)
        {
            this.filteredField = gridFilterInfo.getFilteredField();

            // - each token is used as an alternative
            // - tokens are separated with whitespace
            // - quotes (both double and single quote) wrap data into tokens
            StrTokenizer tokenizer =
                    new StrTokenizer(gridFilterInfo.tryGetFilterPattern().toLowerCase());
            tokenizer.setQuoteMatcher(StrMatcher.quoteMatcher());
            this.filterExpressionAlternatives = tokenizer.getTokenArray();
        }

        static <T> FilterInfo<T> tryCreate(GridFilterInfo<T> filterInfo)
        {
            if (filterInfo.tryGetFilterPattern() == null)
            {
                return null;
            } else
            {
                return new FilterInfo<T>(filterInfo);
            }
        }

        final IColumnDefinition<T> getFilteredField()
        {
            return filteredField;
        }

        final String[] getFilterExpressionAlternatives()
        {
            return filterExpressionAlternatives;
        }
    }

    private static final <T> GridRowModels<T> filterData(final GridRowModels<T> rows,
            Set<IColumnDefinition<T>> availableColumns, final List<GridFilterInfo<T>> filterInfos,
            CustomFilterInfo<T> customFilterInfo)
    {
        List<GridRowModel<T>> filteredRows;
        if (customFilterInfo != null)
        {
            filteredRows =
                    GridExpressionUtils.applyCustomFilter(rows, availableColumns, customFilterInfo);
        } else
        {
            filteredRows = applyStandardColumnFilter(rows, filterInfos);
        }
        return rows.cloneWithData(filteredRows);
    }

    private static <T> List<GridRowModel<T>> applyStandardColumnFilter(final GridRowModels<T> rows,
            final List<GridFilterInfo<T>> filterInfos)
    {
        List<GridRowModel<T>> filtered = new ArrayList<GridRowModel<T>>();
        List<FilterInfo<T>> appliedFilterInfos = processAppliedFilters(filterInfos);
        for (GridRowModel<T> row : rows)
        {
            if (isMatching(row, appliedFilterInfos))
            {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static <T> List<FilterInfo<T>> processAppliedFilters(
            final List<GridFilterInfo<T>> filterInfos)
    {
        List<FilterInfo<T>> serverFilterInfos = new ArrayList<FilterInfo<T>>(filterInfos.size());
        for (GridFilterInfo<T> filterInfo : filterInfos)
        {
            FilterInfo<T> info = FilterInfo.tryCreate(filterInfo);
            if (info != null)
            {
                serverFilterInfos.add(info);
            }
        }
        return serverFilterInfos;
    }

    // returns true if a row matches all the filters
    private static final <T> boolean isMatching(final GridRowModel<T> row,
            final List<FilterInfo<T>> serverFilterInfos)
    {
        for (FilterInfo<T> filter : serverFilterInfos)
        {
            if (isMatching(row, filter) == false)
            {
                return false;
            }
        }
        return true;
    }

    private static final <T> boolean isMatching(final GridRowModel<T> row,
            final FilterInfo<T> filterInfo)
    {
        IColumnDefinition<T> filteredField = filterInfo.getFilteredField();
        String value = filteredField.getValue(row).toLowerCase();
        return isMatching(value, filterInfo.getFilterExpressionAlternatives());
    }

    private static boolean isMatching(String value, String[] filterPatternAlternatives)
    {
        for (String pattern : filterPatternAlternatives)
        {
            if (value.contains(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private final <T> void sortData(final GridRowModels<T> data, final SortInfo<T> sortInfo)
    {
        assert data != null : "Unspecified data.";
        assert sortInfo != null : "Unspecified sort information.";
        if (data.size() == 0)
        {
            return;
        }
        final SortDir sortDir = sortInfo.getSortDir();
        final IColumnDefinition<T> sortField = sortInfo.getSortField();
        if (sortDir == SortDir.NONE || sortField == null)
        {
            return;
        }
        Comparator<GridRowModel<T>> comparator = createComparator(sortDir, sortField);
        Collections.sort(data, comparator);
    }

    private static <T> Comparator<GridRowModel<T>> createComparator(final SortDir sortDir,
            final IColumnDefinition<T> sortField)
    {
        Comparator<GridRowModel<T>> comparator = new Comparator<GridRowModel<T>>()
            {

                @SuppressWarnings("unchecked")
                public int compare(GridRowModel<T> o1, GridRowModel<T> o2)
                {
                    Comparable v1 = sortField.getComparableValue(o1);
                    Comparable v2 = sortField.getComparableValue(o2);
                    return v1.compareTo(v2);
                }
            };
        return applySortDir(sortDir, comparator);
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> applySortDir(final SortDir sortDir, Comparator<T> comparator)
    {
        if (sortDir == SortDir.DESC)
        {
            return new ReverseComparator(comparator);
        } else
        {
            return comparator;
        }
    }

    @Private
    final static int getLimit(final int size, final int limit, final int offset)
    {
        assert size > -1 : "Size can not be negative";
        if (limit < 0)
        {
            return size - offset;
        }
        return Math.min(size - offset, limit);
    }

    @Private
    final static int getOffset(final int size, final int offset)
    {
        assert size > -1 : "Size can not be negative";
        if (size == 0)
        {
            return 0;
        }
        return Math.min(size - 1, Math.max(offset, 0));
    }

    /**
     * Encapsulates list returned by {@link List#subList(int, int)} in a new <code>List</code> as
     * <i>GWT</i> complains because of a serialization concern.
     */
    private final static <T> GridRowModels<T> subList(final GridRowModels<T> data,
            final int offset, final int limit)
    {
        final int toIndex = offset + limit;
        return data.cloneWithData(data.subList(offset, toIndex));
    }

    //
    // IDataManager
    //

    public final synchronized <T> IResultSet<K, T> getResultSet(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataProvider != null : "Unspecified data retriever";
        GridRowModels<T> data;
        K dataKey = resultConfig.getResultSetKey();
        if (dataKey == null)
        {
            dataKey = resultSetKeyProvider.createKey();
            debug("Unknown result set key: retrieving the data with a new key " + dataKey);
            List<T> rows = dataProvider.getOriginalData();
            data = calculateRowModels(sessionToken, rows, resultConfig);
            results.put(dataKey, data);
        } else
        {
            debug(String.format("Fetching the result from the specifed result set key '%s'.",
                    dataKey));
            data = cast(results.get(dataKey));
            if (data == null)
            {
                debug(String
                        .format("Invalid result set key '%s'. This should not happen.", dataKey));
            }
        }
        assert data != null : "Unspecified data";
        data =
                filterData(data, resultConfig.getAvailableColumns(), resultConfig.getFilterInfos(),
                        resultConfig.tryGetCustomFilterInfo());
        final int size = data.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo<T> sortInfo = resultConfig.getSortInfo();
        sortData(data, sortInfo);
        final GridRowModels<T> list = subList(data, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, size);
    }

    private <T> GridRowModels<T> calculateRowModels(String sessionToken, List<T> rows,
            IResultSetConfig<?, T> resultConfig)
    {
        List<GridCustomColumn> customColumns =
                fetchCustomColumnsMetadata(sessionToken, resultConfig);
        List<GridRowModel<T>> rowModels =
                GridExpressionUtils.evalCustomColumns(rows, customColumns, resultConfig
                        .getAvailableColumns());

        List<ColumnDistinctValues> columnDistinctValues =
                calculateColumnDistinctValues(rowModels, resultConfig.getFilterInfos());

        List<GridCustomColumnInfo> customColumnInfos =
                GridExpressionUtils.extractColumnInfos(customColumns);

        return new GridRowModels<T>(rowModels, customColumnInfos, columnDistinctValues);
    }

    @Private
    static <T> List<ColumnDistinctValues> calculateColumnDistinctValues(
            List<GridRowModel<T>> rowModels, List<GridFilterInfo<T>> columns)
    {
        List<ColumnDistinctValues> result = new ArrayList<ColumnDistinctValues>();
        for (GridFilterInfo<T> column : columns)
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
    private static <T> ColumnDistinctValues tryCalculateColumnDistinctValues(
            List<GridRowModel<T>> rowModels, IColumnDefinition<T> column)
    {
        Set<String> distinctValues = new HashSet<String>();
        for (GridRowModel<T> rowModel : rowModels)
        {
            String value = column.getValue(rowModel);
            distinctValues.add(value);
            if (distinctValues.size() > MAX_DISTINCT_COLUMN_VALUES_SIZE)
            {
                return null;
            }
        }
        ArrayList<String> distinctValuesList = new ArrayList<String>(distinctValues);
        return new ColumnDistinctValues(column.getIdentifier(), distinctValuesList);
    }

    private List<GridCustomColumn> fetchCustomColumnsMetadata(String sessionToken,
            IResultSetConfig<?, ?> resultConfig)
    {
        String gridId = resultConfig.tryGetGridDisplayId();
        if (gridId == null)
        {
            return new ArrayList<GridCustomColumn>();
        } else
        {
            return customColumnsProvider.getGridCustomColumn(sessionToken, gridId);
        }
    }

    public final synchronized void removeResultSet(final K resultSetKey)
    {
        assert resultSetKey != null : "Unspecified data key holder.";
        if (results.remove(resultSetKey) != null)
        {
            debug(String.format("Result set for key '%s' has been removed.", resultSetKey));
        } else
        {
            debug(String.format("No result set for key '%s' could be found.", resultSetKey));
        }
    }

    private void debug(String msg)
    {
        operationLog.debug(msg);
    }
}
