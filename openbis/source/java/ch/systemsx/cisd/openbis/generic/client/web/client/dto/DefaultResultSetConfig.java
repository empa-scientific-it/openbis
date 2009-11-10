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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;

/**
 * A default {@link IResultSetConfig} implementation.
 * 
 * @author Christian Ribeaud
 */
public class DefaultResultSetConfig<K, T> implements IResultSetConfig<K, T>, IsSerializable
{
    private int limit = NO_LIMIT;

    private int offset = 0;

    private SortInfo<T> sortInfo = new SortInfo<T>();

    private ResultSetFetchConfig<K> cacheConfig = ResultSetFetchConfig.createComputeAndCache();

    private Set<IColumnDefinition<T>> availableColumns;

    private GridFilters<T> filters = GridFilters.createEmptyFilter();

    // null if no custom columns are needed
    private String gridDisplayIdOrNull;

    public static <K, T> DefaultResultSetConfig<K, T> createFetchAll()
    {
        return new DefaultResultSetConfig<K, T>();
    }

    public final void setLimit(final int limit)
    {
        this.limit = limit;
    }

    public final void setOffset(final int offset)
    {
        this.offset = offset;
    }

    public final void setSortInfo(final SortInfo<T> sortInfo)
    {
        this.sortInfo = sortInfo;
    }

    public final void setCacheConfig(final ResultSetFetchConfig<K> cacheConfig)
    {
        this.cacheConfig = cacheConfig;
    }

    public GridFilters<T> getFilters()
    {
        return filters;
    }

    public final void setAvailableColumns(Set<IColumnDefinition<T>> availableColumns)
    {
        this.availableColumns = availableColumns;
    }

    public final void copyPagingConfig(DefaultResultSetConfig<K, T> resultSetConfig)
    {
        setLimit(resultSetConfig.getLimit());
        setOffset(resultSetConfig.getOffset());
        setAvailableColumns(resultSetConfig.getAvailableColumns());
        setSortInfo(resultSetConfig.getSortInfo());
        setFilters(resultSetConfig.getFilters());
        setCacheConfig(resultSetConfig.getCacheConfig());
        setGridDisplayId(resultSetConfig.tryGetGridDisplayId());
    }

    //
    // IResultSetConfig
    //

    public final int getLimit()
    {
        return limit;
    }

    public final int getOffset()
    {
        return offset;
    }

    public final Set<IColumnDefinition<T>> getAvailableColumns()
    {
        return availableColumns;
    }

    public final SortInfo<T> getSortInfo()
    {
        return sortInfo;
    }

    public final ResultSetFetchConfig<K> getCacheConfig()
    {
        return cacheConfig;
    }

    public void setFilters(GridFilters<T> filters)
    {
        this.filters = filters;
    }

    public void setGridDisplayId(String gridDisplayIdOrNull)
    {
        this.gridDisplayIdOrNull = gridDisplayIdOrNull;
    }

    public String tryGetGridDisplayId()
    {
        return gridDisplayIdOrNull;
    }
}
