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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * Class storing personalised display settings for a table column. This class implements {@link Serializable} not only for transferring it's content
 * remotely but also to store it in the database. Thus, CHANGES IN THIS CLASS MIGHT LEAD TO A LOST OF PERSONAL SETTINGS.
 * 
 * @author Franz-Josef Elmer
 */
public class ColumnSetting implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String columnID;

    private boolean hidden;

    private boolean hasFilter;

    private int width;

    private SortDir sortDir;

    public SortDir getSortDir()
    {
        return sortDir;
    }

    public void setSortDir(SortDir sortDir)
    {
        this.sortDir = sortDir;
    }

    public final String getColumnID()
    {
        return columnID;
    }

    public final void setColumnID(String columnID)
    {
        this.columnID = columnID;
    }

    public final boolean isHidden()
    {
        return hidden;
    }

    public final void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public final int getWidth()
    {
        return width;
    }

    public final void setWidth(int width)
    {
        this.width = width;
    }

    public boolean hasFilter()
    {
        return hasFilter;
    }

    public void setHasFilter(boolean hasFilter)
    {
        this.hasFilter = hasFilter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof ColumnSetting == false)
        {
            return false;
        }
        ColumnSetting setting = (ColumnSetting) obj;
        return setting.columnID.equals(columnID);
    }

    @Override
    public int hashCode()
    {
        return columnID.hashCode();
    }

    @Override
    public String toString()
    {
        return columnID + "( hidden = " + hidden + ", width = " + width + ", hasFilter = "
                + hasFilter + " )";
    }

}
