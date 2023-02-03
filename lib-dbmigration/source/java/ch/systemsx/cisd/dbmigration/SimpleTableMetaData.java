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

package ch.systemsx.cisd.dbmigration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.reflection.AbstractHashable;

/**
 * Immutable class which holds some meta data needed for handling tables stored in delimiter-separated files.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleTableMetaData extends AbstractHashable
{

    private final String tableName;

    private final String tableFileName;

    private final List<String> columnNames;

    /**
     * Creates an instance for the specified table name, file name, and list of column names. Note that table name and column names are converted to
     * upper case.
     */
    public SimpleTableMetaData(String tableName, String tableFileName, List<String> columnNames)
    {
        this.tableName = tableName.toUpperCase();
        this.tableFileName = tableFileName;
        this.columnNames = Collections.unmodifiableList(convertToUpperCase(columnNames));
    }

    private List<String> convertToUpperCase(List<String> names)
    {
        List<String> list = new ArrayList<String>();
        for (String columnName : names)
        {
            list.add(columnName.toUpperCase());
        }
        return list;
    }

    /**
     * Returns the name of the table.
     */
    public final String getTableName()
    {
        return tableName;
    }

    /**
     * Returns the name of the delimiter-separated file.
     */
    public final String getTableFileName()
    {
        return tableFileName;
    }

    /**
     * Returns the name of the columns in the order the data is stored in the delemiter-separated file.
     */
    public final List<String> getColumnNames()
    {
        return columnNames;
    }

    /**
     * Returns the index of the specified column.
     * 
     * @return -1 if not found.
     */
    public int getIndexOfColumn(String columnName)
    {
        return columnNames.indexOf(columnName.toUpperCase());
    }

}
