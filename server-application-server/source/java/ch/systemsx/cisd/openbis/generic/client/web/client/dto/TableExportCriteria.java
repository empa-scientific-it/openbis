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

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;

/**
 * Describes which entities should be exported, in which order and which columns should be present. The result of the export is a table with header.
 * 
 * @author Tomasz Pylak
 */
public class TableExportCriteria<T/* exported entity */> implements IsSerializable
{
    // a key at which data are stored in the server cache
    private String resultSetKey;

    private SortInfo sortInfo = new SortInfo();

    /** @see IResultSetConfig#getFilters() */
    private GridFilters<T> filters;

    private EntityKind entityKindForUpdateOrNull;

    // which columns should be exported
    private List<IColumnDefinition<T>> columnDefs;

    // all available columns in the grid which is exported. Used to calculate expressions in
    // custom filters and columns.
    private Set<IColumnDefinition<T>> availableColumns;

    // This field would be used only if the exported data could not be found in the
    // cache and custom columns would have to be computed. In fact it should be always a case that
    // data are in a cache.
    private String gridDisplayId;

    // GWT only
    public TableExportCriteria()
    {
    }

    public TableExportCriteria(String resultSetKey, SortInfo sortInfo, GridFilters<T> filters,
            EntityKind entityKindForUpdateOrNull, List<IColumnDefinition<T>> columnDefs,
            Set<IColumnDefinition<T>> availableColumns, String gridDisplayId)
    {
        this.resultSetKey = resultSetKey;
        this.sortInfo = sortInfo;
        this.filters = filters;
        this.entityKindForUpdateOrNull = entityKindForUpdateOrNull;
        this.columnDefs = columnDefs;
        this.availableColumns = availableColumns;
        this.gridDisplayId = gridDisplayId;
    }

    public EntityKind getEntityKindForUpdateOrNull()
    {
        return entityKindForUpdateOrNull;
    }

    /**
     * Uniquely identifies a result set on the server side.
     */
    public String getResultSetKey()
    {
        return resultSetKey;
    }

    public SortInfo getSortInfo()
    {
        return sortInfo;
    }

    public GridFilters<T> getFilters()
    {
        return filters;
    }

    public List<IColumnDefinition<T>> getColumnDefs()
    {
        return columnDefs;
    }

    public Set<IColumnDefinition<T>> getAvailableColumns()
    {
        return availableColumns;
    }

    public String getGridDisplayId()
    {
        return gridDisplayId;
    }
}
