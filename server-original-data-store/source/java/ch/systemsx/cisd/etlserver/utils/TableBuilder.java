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

package ch.systemsx.cisd.etlserver.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for a bunch of table columns.
 *
 * @author Franz-Josef Elmer
 */
public class TableBuilder
{
    private final List<Column> columns;

    /**
     * Creates an instance for the specified list of column headers.
     */
    public TableBuilder(List<String> headers)
    {
        this(headers.toArray(new String[headers.size()]));
    }

    /**
     * Creates an instance for the specified array of column headers.
     */
    public TableBuilder(String... headers)
    {
        columns = new ArrayList<Column>(headers.length);
        for (String header : headers)
        {
            columns.add(new Column(header));
        }
    }

    /**
     * Adds the specified values as a new row to the table.
     */
    public void addRow(List<String> values)
    {
        addRow(values.toArray(new String[values.size()]));
    }

    /**
     * Adds the specified values as a new row to the table.
     */
    public void addRow(String... values)
    {
        for (int i = 0, n = columns.size(); i < n; i++)
        {
            Column column = columns.get(i);
            column.add(i < values.length ? values[i] : "");
        }
    }

    /**
     * Returns the columns after table has been built.
     */
    public List<Column> getColumns()
    {
        return columns;
    }
}
