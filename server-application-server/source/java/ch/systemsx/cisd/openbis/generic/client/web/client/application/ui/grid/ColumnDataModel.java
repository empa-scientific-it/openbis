/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;

/**
 * Represents a column of the table.
 * 
 * @author Izabela Adamczyk
 */
public class ColumnDataModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    static final String COLUMN_ID = "column_id";

    public static final String ADDRESS = "address";

    static final String HAS_FILTER = "HAS_FILTER";

    static final String IS_VISIBLE = "IS_VISIBLE";

    public static final String HEADER = "header";

    public ColumnDataModel(String header, boolean isVisible, boolean hasFilter, String columnID)
    {
        setHeader(header);
        setIsVisible(isVisible);
        setHasFilter(hasFilter);
        setColumnID(columnID);
        setAddress("row.col('" + columnID + "')");
    }

    private void setAddress(String address)
    {
        set(ADDRESS, address);
    }

    private void setColumnID(String columnID)
    {
        set(COLUMN_ID, columnID);
    }

    private void setIsVisible(boolean isVisible)
    {
        set(IS_VISIBLE, isVisible);
    }

    private void setHasFilter(boolean hasFilter)
    {
        set(HAS_FILTER, hasFilter);
    }

    private void setHeader(String header)
    {
        set(HEADER, header);
    }

    public String getColumnID()
    {
        return get(COLUMN_ID);
    }

    public boolean isVisible()
    {
        return get(IS_VISIBLE);
    }

    public boolean hasFilter()
    {
        return get(HAS_FILTER);
    }

    public String getHeader()
    {
        return get(HEADER);
    }

    public String getAddress()
    {
        return get(ADDRESS);
    }

}
