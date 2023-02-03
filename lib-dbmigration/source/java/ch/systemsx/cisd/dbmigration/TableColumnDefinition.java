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
import java.util.Iterator;
import java.util.List;

/**
 * Meta data of a database column. It contains
 * <ul>
 * <li>table to which it belongs ({@link #getTableDefinition()})
 * <li>name of the column ({@link #getColumnName()})
 * <li>name of its data type ({@link #getDataTypeName()})
 * <li>whether it a primary key or not ({@link #isPrimaryKey()})
 * <li>whether it a foreign key or not ({@link #getForeignKeyReference()})
 * <li>the value of the largest primary key found in a concrete data base ({@link #getLargestPrimaryKey()})
 * <li>all foreign keys (i.e. table and columns) pointing to this column ({@link #iterator()})
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public final class TableColumnDefinition implements Iterable<TableColumnDefinition>
{
    private final TableDefinition tableDefinition;

    private boolean primaryKey;

    private long largestPrimaryKey = -1;

    private TableColumnDefinition foreignKeyReference;

    private String columnName;

    private String dataTypeName;

    private List<TableColumnDefinition> connections = new ArrayList<TableColumnDefinition>();

    public TableColumnDefinition(TableDefinition tableDefinition)
    {
        this.tableDefinition = tableDefinition;
    }

    public final TableDefinition getTableDefinition()
    {
        return tableDefinition;
    }

    /**
     * Adds a connection defined by the specified foreign-key column. Sets also the foreign-key reference of the argument to this.
     * 
     * @throws IllegalStateException if this is not a primary-key column.
     */
    public void addConnection(TableColumnDefinition foreignKeyColumn)
    {
        if (isPrimaryKey() == false)
        {
            throw new IllegalStateException("Can not add connection '"
                    + foreignKeyColumn.getColumnName() + "' to column '" + columnName
                    + "'which is not a primary key.");
        }
        foreignKeyColumn.setForeignKeyReference(this);
        connections.add(foreignKeyColumn);
    }

    @Override
    public Iterator<TableColumnDefinition> iterator()
    {
        return connections.iterator();
    }

    public final long getLargestPrimaryKey()
    {
        return largestPrimaryKey;
    }

    public final void setLargestPrimaryKey(long nextFreePrimaryKey)
    {
        this.largestPrimaryKey = nextFreePrimaryKey;
    }

    public final TableColumnDefinition getForeignKeyReference()
    {
        return foreignKeyReference;
    }

    public final void setForeignKeyReference(TableColumnDefinition foreignKeyReference)
    {
        this.foreignKeyReference = foreignKeyReference;
    }

    public final boolean isPrimaryKey()
    {
        return primaryKey;
    }

    public final void setPrimaryKey(boolean primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public final String getColumnName()
    {
        return columnName;
    }

    public final void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public final String getDataTypeName()
    {
        return dataTypeName;
    }

    public final void setDataTypeName(String dataType)
    {
        this.dataTypeName = dataType;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof TableColumnDefinition == false)
        {
            return false;
        }
        TableColumnDefinition columnDefinition = (TableColumnDefinition) obj;
        return columnDefinition.columnName.equals(columnName);
    }

    @Override
    public int hashCode()
    {
        return columnName.hashCode();
    }

    @Override
    public String toString()
    {
        return (primaryKey ? "*" : " ") + columnName + " " + dataTypeName
                + (foreignKeyReference != null ? " (fk) " : " ")
                + (largestPrimaryKey < 0 ? "" : Long.toString(largestPrimaryKey) + " ")
                + (connections.isEmpty() ? "" : connections.toString());
    }
}