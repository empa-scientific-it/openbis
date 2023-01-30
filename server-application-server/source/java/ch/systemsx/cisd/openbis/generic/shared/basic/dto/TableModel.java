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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ITableModel;

/**
 * A table with a list of rows and columns specification. Each column has header and type.
 * 
 * @author Tomasz Pylak
 */
public class TableModel implements Serializable, ITableModel
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String messageOrNull;

    private List<TableModelRow> rows;

    private List<TableModelColumnHeader> header;

    public TableModel(List<TableModelColumnHeader> header, List<TableModelRow> rows)
    {
        this(header, rows, null);
    }

    public TableModel(List<TableModelColumnHeader> header, List<TableModelRow> rows,
            String messageOrNull)
    {
        this.rows = rows;
        this.header = header;
        this.messageOrNull = messageOrNull;
        validate();
    }

    private void validate()
    {
        int columnsNo = header.size();
        for (TableModelRow row : rows)
        {
            assert row.getValues().size() == columnsNo : "row has a different number of columns than the table header";
        }
    }

    public List<TableModelRow> getRows()
    {
        return rows;
    }

    public List<TableModelColumnHeader> getHeader()
    {
        return header;
    }

    public String tryGetMessage()
    {
        return messageOrNull;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModel()
    {
    }
}
