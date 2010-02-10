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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;

/**
 * Helps in building a {@link TableModel}
 * 
 * @author Tomasz Pylak
 */
public class SimpleTableModelBuilder
{
    private List<TableModelRow> rows;

    private List<TableModelColumnHeader> headers;

    public SimpleTableModelBuilder()
    {
        this.rows = new ArrayList<TableModelRow>();
        this.headers = new ArrayList<TableModelColumnHeader>();
    }

    public void addHeader(String title)
    {
        headers.add(new TableModelColumnHeader(title, headers.size()));
    }

    public void addRow(List<ISerializableComparable> values)
    {
        assert values.size() == headers.size() : "header has different number of columns than a row";
        for (int i = 0; i < values.size(); i++)
        {
            ISerializableComparable value = values.get(i);
            TableModelColumnHeader header = headers.get(i);
            DataTypeCode headerDataType = header.getDataType();
            DataTypeCode dataType = getDataTypeCodeFor(value);
            header.setDataType(DataTypeUtils.getCompatibleDataType(headerDataType, dataType));
        }
        rows.add(new TableModelRow(values));
    }
    
    private DataTypeCode getDataTypeCodeFor(ISerializableComparable value)
    {
        if (value instanceof IntegerTableCell)
        {
            return DataTypeCode.INTEGER;
        } 
        if (value instanceof DoubleTableCell)
        {
            return DataTypeCode.REAL;
        } 
        if (value instanceof DateTableCell)
        {
            return DataTypeCode.TIMESTAMP;
        }
        return DataTypeCode.VARCHAR;
    }

    public TableModel getTableModel()
    {
        return new TableModel(headers, rows);
    }

    public static ISerializableComparable asText(String textOrNull)
    {
        if (textOrNull == null)
        {
            return createNullCell();
        }
        return new StringTableCell(textOrNull);
    }

    public static ISerializableComparable asNum(int num)
    {
        return new IntegerTableCell(num);
    }

    public static ISerializableComparable asNum(double num)
    {
        return new DoubleTableCell(num);
    }

    public static ISerializableComparable asDate(Date dateOrNull)
    {
        if (dateOrNull == null)
        {
            return createNullCell();
        }
        return new DateTableCell(dateOrNull);
    }

    private static ISerializableComparable createNullCell()
    {
        return new StringTableCell("");
    }
}
