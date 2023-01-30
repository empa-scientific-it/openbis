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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The filters (column filters or custom filter) which should be applied to the grid rows.
 * 
 * @author Tomasz Pylak
 */
public class GridFilters<T> implements IsSerializable
{
    public static <T> GridFilters<T> createEmptyFilter()
    {
        return new GridFilters<T>(null, null);
    }

    public static <T> GridFilters<T> createColumnFilter(List<GridColumnFilterInfo<T>> filterInfos)
    {
        return new GridFilters<T>(filterInfos, null);
    }

    public static <T> GridFilters<T> createCustomFilter(CustomFilterInfo<T> customFilterInfo)
    {
        return new GridFilters<T>(null, customFilterInfo);
    }

    // ----

    private List<GridColumnFilterInfo<T>> filterInfosOrNull =
            new ArrayList<GridColumnFilterInfo<T>>();

    private CustomFilterInfo<T> customFilterInfoOrNull;

    private GridFilters(List<GridColumnFilterInfo<T>> filterInfosOrNull,
            CustomFilterInfo<T> customFilterInfoOrNull)
    {
        this.filterInfosOrNull = filterInfosOrNull;
        this.customFilterInfoOrNull = customFilterInfoOrNull;
    }

    // GWT only
    private GridFilters()
    {
    }

    /**
     * @return null if custom filter should be applied. Otherwise returns the column filters which are visible for the user, they should be applied
     *         for the result if they are not empty. They will be also used to calculate distinct values in columns for more convenient filtering.
     */
    public List<GridColumnFilterInfo<T>> tryGetFilterInfos()
    {
        return filterInfosOrNull;
    }

    /**
     * The custom filter which should be applied for the result or null if no custom filter should be applied.
     */
    public CustomFilterInfo<T> tryGetCustomFilterInfo()
    {
        return customFilterInfoOrNull;
    }
}
