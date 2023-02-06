/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.TableModel")
public class TableModel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<TableColumn> columns;

    private List<List<ITableCell>> rows;

    public TableModel(List<TableColumn> columns, List<List<ITableCell>> rows)
    {
        if (columns == null)
        {
            throw new IllegalArgumentException("Unspecified table columns.");
        }
        if (columns.isEmpty())
        {
            throw new IllegalArgumentException("No table column specified.");
        }
        this.columns = columns;
        if (rows == null)
        {
            throw new IllegalArgumentException("Unspecified table rows.");
        }
        for (int i = 0; i < rows.size(); i++)
        {
            List<ITableCell> row = rows.get(i);
            if (row == null)
            {
                throw new IllegalArgumentException((i + 1) + ". row is not specified.");
            }
            if (row.size() != columns.size())
            {
                throw new IllegalArgumentException((i + 1) + ". row has " + row.size()
                        + " cells instead of " + columns.size() + ".");
            }
        }
        this.rows = rows;
    }

    public List<TableColumn> getColumns()
    {
        return columns;
    }

    public List<List<ITableCell>> getRows()
    {
        return rows;
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private TableModel()
    {
    }

    @SuppressWarnings("unused")
    private void setColumns(List<TableColumn> columns)
    {
        this.columns = columns;
    }

    @SuppressWarnings("unused")
    private void setRows(List<List<ITableCell>> rows)
    {
        this.rows = rows;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("columns", columns).append("rowCount", rows != null ? rows.size() : 0).toString();
    }

}
