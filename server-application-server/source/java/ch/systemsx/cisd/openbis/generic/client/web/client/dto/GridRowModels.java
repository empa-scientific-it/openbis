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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;

/**
 * A list of {@link GridRowModel} together with metadata about custom columns.
 * 
 * @author Tomasz Pylak
 */
public class GridRowModels<T> extends ArrayList<GridRowModel<T>> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<TableModelColumnHeader> columnHeaders;

    /**
     * Metadata of available custom columns for all rows.
     */
    private List<GridCustomColumnInfo> customColumnsMetadata;

    /**
     * Information about distinct values in the columns. Only those columns which user wanted to filter with column filters and which at the same time
     * had values belonging to a small set are included.<br>
     * Note that all rows are taken into account to compute distinct values, not only those from this grid row model.<br>
     */
    private List<ColumnDistinctValues> columnDistinctValues;

    /** Creates a new instance with the specified list of data and previous values for other fields */
    public GridRowModels<T> cloneWithData(List<GridRowModel<T>> list)
    {
        return new GridRowModels<T>(list, this.getColumnHeaders(), this.getCustomColumnsMetadata(),
                this.getColumnDistinctValues());
    }

    public GridRowModels(List<GridRowModel<T>> list, List<TableModelColumnHeader> headers,
            List<GridCustomColumnInfo> customColumnsMetadata, List<ColumnDistinctValues> arrayList)
    {
        super(list);
        columnHeaders = headers;
        this.customColumnsMetadata = customColumnsMetadata;
        this.columnDistinctValues = arrayList;
    }

    public List<TableModelColumnHeader> getColumnHeaders()
    {
        return columnHeaders;
    }

    /** Used when items are not displayed in a grid (usually we need the values for comboboxes) */
    // TODO 2009-10-08, Tomasz Pylak: this method is a source of many anti-patterns where the
    // existance of server cache is simply forgotten and cache is never cleared.
    // Possible solution is to add an
    // option to a {@link IResultSetConfig} not to use cache when it will not be used.
    public List<T> extractOriginalObjects()
    {
        List<T> result = new ArrayList<T>();
        for (GridRowModel<T> item : this)
        {
            result.add(item.getOriginalObject());
        }
        return result;
    }

    public List<GridCustomColumnInfo> getCustomColumnsMetadata()
    {
        return customColumnsMetadata;
    }

    public List<ColumnDistinctValues> getColumnDistinctValues()
    {
        return columnDistinctValues;
    }

    // GWT only
    @SuppressWarnings("unused")
    private GridRowModels()
    {
    }

}
