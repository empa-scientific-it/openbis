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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Stores metadata about grid custom columns.
 * 
 * @author Tomasz Pylak
 */
public class CustomColumnsMetadataProvider
{
    // what custom columns are present in the grid. Used to build column definitions.
    private List<GridCustomColumnInfo> customColumnsMetadata =
            new ArrayList<GridCustomColumnInfo>();

    // has the last setter call changed the matadata?
    private boolean hasChanged;

    /** true if the value has changed. Subsequent calls will return false. */
    public boolean getHasChangedAndSetFalse()
    {
        boolean result = hasChanged;
        this.hasChanged = false;
        return result;
    }

    public List<GridCustomColumnInfo> getCustomColumnsMetadata()
    {
        return customColumnsMetadata;
    }

    public void setCustomColumnsMetadata(List<GridCustomColumnInfo> columns)
    {
        // create old columns map by code
        Map<String, GridCustomColumnInfo> oldColumns = new HashMap<String, GridCustomColumnInfo>();
        for (GridCustomColumnInfo oldColumn : this.customColumnsMetadata)
        {
            oldColumns.put(oldColumn.getCode(), oldColumn);
        }

        // copy data types whenever data type is missing in a new column but was set in the old one
        List<GridCustomColumnInfo> newColumns = new ArrayList<GridCustomColumnInfo>();
        if (columns != null)
        {
            for (GridCustomColumnInfo column : columns)
            {
                if (column.getDataType() == null)
                {
                    GridCustomColumnInfo oldColumn = oldColumns.get(column.getCode());
                    if (oldColumn != null)
                    {
                        DataTypeCode dataType = oldColumn.getDataType();
                        newColumns.add(new GridCustomColumnInfo(column.getCode(),
                                column.getLabel(), column.getDescription(), dataType));
                    } else
                    {
                        newColumns.add(column);
                    }
                } else
                {
                    newColumns.add(column);
                }
            }
        }

        this.hasChanged = this.customColumnsMetadata.equals(newColumns) == false;
        this.customColumnsMetadata = newColumns;
    }
}
