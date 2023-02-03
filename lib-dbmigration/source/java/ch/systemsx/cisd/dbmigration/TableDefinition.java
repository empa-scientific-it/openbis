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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Meta data of a database table. Contains
 * <ul>
 * <li>Name of the table ({@link #getTableName()})
 * <li>Meta data for all columns ({@link #getColumnDefinition(String)}, {@link #iterator()})
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public final class TableDefinition implements Iterable<TableColumnDefinition>,
        Comparable<TableDefinition>
{
    private final String tableName;

    private final Map<String, TableColumnDefinition> columns =
            new TreeMap<String, TableColumnDefinition>();

    public TableDefinition(String tableName)
    {
        this.tableName = tableName;
    }

    public final String getTableName()
    {
        return tableName;
    }

    public void add(TableColumnDefinition columnDefinition)
    {
        columns.put(columnDefinition.getColumnName(), columnDefinition);
    }

    public void defineColumnAsPrimaryKey(String columnName)
    {
        getColumnDefinition(columnName).setPrimaryKey(true);
    }

    /**
     * Returns the definition for the specified column.
     * 
     * @throws IllegalArgumentException if no definition found.
     */
    public TableColumnDefinition getColumnDefinition(String columnName)
    {
        TableColumnDefinition columnDefinition = columns.get(columnName);
        if (columnDefinition == null)
        {
            throw new IllegalArgumentException("No column '" + columnName
                    + "' defined in table '" + tableName + "'.");
        }
        return columnDefinition;
    }

    @Override
    public Iterator<TableColumnDefinition> iterator()
    {
        return columns.values().iterator();
    }

    @Override
    public int compareTo(TableDefinition tableDefinition)
    {
        return tableName.compareTo(tableDefinition.getTableName());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(tableName).append(":");
        for (TableColumnDefinition columnDefinition : columns.values())
        {
            builder.append("\n  ").append(columnDefinition);
        }
        return builder.toString();
    }

}