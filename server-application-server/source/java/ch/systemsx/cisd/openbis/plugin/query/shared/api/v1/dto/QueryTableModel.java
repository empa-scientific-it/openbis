/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Model of query data.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("QueryTableModel")
public class QueryTableModel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<QueryTableColumn> columns;

    private List<Serializable[]> rows;

    /**
     * Creates an instance for the specified columns.
     */
    public QueryTableModel(List<QueryTableColumn> columns)
    {
        this.columns = columns;
        rows = new ArrayList<Serializable[]>();
    }

    /**
     * Returns columns as specified in the constructor.
     */
    public List<QueryTableColumn> getColumns()
    {
        return columns;
    }

    /**
     * Adds a row of values.
     * 
     * @throws IllegalArgumentException if the number of values is not the same as the number of columns.
     */
    public void addRow(Serializable[] values)
    {
        if (values == null)
        {
            throw new IllegalArgumentException("Unspecified row.");
        }
        if (values.length != columns.size())
        {
            throw new IllegalArgumentException("Row has " + values.length + " instead of "
                    + columns.size() + ".");
        }
        rows.add(values);
    }

    /**
     * Gets all rows.
     */
    @JsonIgnore
    public List<Serializable[]> getRows()
    {
        return rows;
    }

    // JSON-RPC Serialization
    private QueryTableModel()
    {
        columns = new ArrayList<QueryTableColumn>();
        rows = new ArrayList<Serializable[]>();
    }

    private void setColumns(List<QueryTableColumn> columns)
    {
        this.columns = columns;
    }

    /**
     * Jackson cannot deserialize things typed as Serializable because it has no idea what the correct type might be.
     * <p>
     * Thus we convert the values to strings and add type information.
     */
    @JsonProperty(value = "rows")
    private List<TypedStringValue[]> getTypedRows()
    {
        ArrayList<TypedStringValue[]> typedRows = new ArrayList<TypedStringValue[]>(rows.size());
        for (Serializable[] row : rows)
        {
            TypedStringValue[] typedRow = new TypedStringValue[row.length];
            typedRows.add(typedRow);
            for (int i = 0; i < row.length; ++i)
            {
                Serializable value = row[i];
                if (value instanceof Long)
                {
                    typedRow[i] =
                            new TypedStringValue(QueryTableColumnDataType.LONG, value.toString());
                } else if (value instanceof Double)
                {
                    typedRow[i] =
                            new TypedStringValue(QueryTableColumnDataType.DOUBLE, value.toString());
                } else if (value instanceof String)
                {
                    typedRow[i] =
                            new TypedStringValue(QueryTableColumnDataType.STRING, value.toString());
                } else
                {
                    throw new IllegalArgumentException("Cannot convert " + value
                            + " to a long, double, or String.");
                }
            }
        }
        return typedRows;
    }

    @JsonProperty(value = "rows")
    private void setTypedRows(List<TypedStringValue[]> typedRows)
    {
        rows = new ArrayList<Serializable[]>(typedRows.size());
        for (TypedStringValue[] typedRow : typedRows)
        {
            Serializable[] row = new Serializable[typedRow.length];
            rows.add(row);
            for (int i = 0; i < row.length; ++i)
            {
                row[i] = typedRow[i].toSerializable();
            }
        }
    }
}
