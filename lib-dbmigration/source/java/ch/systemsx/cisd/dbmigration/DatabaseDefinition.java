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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Meta data of a database necessary to import another database with the same schema.
 * 
 * @author Franz-Josef Elmer
 */
public final class DatabaseDefinition implements Iterable<TableDefinition>
{
    private final Map<String, TableDefinition> tableDefinitions =
            new TreeMap<String, TableDefinition>();

    /**
     * Adds the specified table definition.
     */
    public void add(TableDefinition tableDefinition)
    {
        tableDefinitions.put(tableDefinition.getTableName(), tableDefinition);
    }

    /**
     * Returns the definition for the specified table.
     * 
     * @throws IllegalArgumentException if table not found.
     */
    public TableDefinition getTableDefinition(String tableName)
    {
        TableDefinition tableDefinition = tableDefinitions.get(tableName);
        if (tableDefinition == null)
        {
            throw new IllegalArgumentException("Unknown table '" + tableName + "'.");
        }
        return tableDefinition;
    }

    /**
     * Connects two tables via a foreign key relation.
     * 
     * @param pkTableName Name of the table with primary key.
     * @param pkColumnName Name of the primary key column.
     * @param fkTableName Name of the table with foreign key.
     * @param fkColumnName Name of the foreign key column.
     * @throws IllegalArgumentException if one of the tables or columns do not exist.
     */
    public void connect(String pkTableName, String pkColumnName, String fkTableName,
            String fkColumnName)
    {
        TableDefinition pkTableDefinition = getTableDefinition(pkTableName);
        TableColumnDefinition pkColumnDefinition =
                pkTableDefinition.getColumnDefinition(pkColumnName);
        TableDefinition fkTableDefinition = getTableDefinition(fkTableName);
        TableColumnDefinition fkColumnDefinition =
                fkTableDefinition.getColumnDefinition(fkColumnName);
        pkColumnDefinition.addConnection(fkColumnDefinition);
    }

    /**
     * Creates an iterator over all table definitions.
     */
    @Override
    public Iterator<TableDefinition> iterator()
    {
        return tableDefinitions.values().iterator();
    }

    /**
     * Returns the definitions of all tables depending directly or indirectly on the specified tables.
     */
    public Set<TableDefinition> getTablesDependingOn(String... tableNames)
    {
        Set<TableDefinition> collection = new TreeSet<TableDefinition>();
        Set<String> visitedTableDefinitions = new HashSet<String>();
        for (String tableName : tableNames)
        {
            collectTablesDependingOn(getTableDefinition(tableName), collection,
                    visitedTableDefinitions);
        }

        return collection;
    }

    private void collectTablesDependingOn(TableDefinition tableDefinition,
            Set<TableDefinition> collection, Set<String> visitedTableDefinitions)
    {
        if (visitedTableDefinitions.contains(tableDefinition.getTableName()))
        {
            return;
        }
        visitedTableDefinitions.add(tableDefinition.getTableName());
        for (TableColumnDefinition tableColumnDefinition : tableDefinition)
        {
            for (TableColumnDefinition tableColumnDefinition2 : tableColumnDefinition)
            {
                TableDefinition dependentTable = tableColumnDefinition2.getTableDefinition();
                collection.add(dependentTable);
                collectTablesDependingOn(dependentTable, collection, visitedTableDefinitions);
            }
        }
    }

    /**
     * Returns a textual description of the meta data.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (TableDefinition tableDefinition : tableDefinitions.values())
        {
            builder.append(tableDefinition).append('\n');
        }
        return builder.toString();
    }

}