/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ITableCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableColumn;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableDoubleCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableLongCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableStringCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author pkupczyk
 */
public class TableModelTranslator
{

    public TableModel translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel tableModel)
    {
        ArrayList<TableColumn> columns = new ArrayList<>();
        ArrayList<List<ITableCell>> translatedRows = new ArrayList<>();
        if (tableModel != null && tableModel.getHeader() != null && tableModel.getRows() != null)
        {
            List<TableModelColumnHeader> headers = tableModel.getHeader();
            columns.ensureCapacity(headers.size());
            for (TableModelColumnHeader header : headers)
            {
                columns.add(new TableColumn(header.getTitle()));
            }
            SimpleDateFormat format = new SimpleDateFormat(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN);
            List<TableModelRow> rows = tableModel.getRows();
            translatedRows.ensureCapacity(rows.size());
            for (TableModelRow row : rows)
            {
                List<ISerializableComparable> values = row.getValues();
                if (values != null)
                {
                    List<ITableCell> cells = new ArrayList<>(values.size());
                    for (ISerializableComparable value : values)
                    {
                        ITableCell cell = null;
                        if (value instanceof IntegerTableCell)
                        {
                            cell = new TableLongCell(((IntegerTableCell) value).getNumber());
                        } else if (value instanceof DoubleTableCell)
                        {
                            cell = new TableDoubleCell(((DoubleTableCell) value).getNumber());
                        } else if (value instanceof DateTableCell)
                        {
                            cell = new TableStringCell(format.format(((DateTableCell) value).getDateTime()));
                        } else if (value != null)
                        {
                            cell = new TableStringCell(value.toString());
                        }
                        cells.add(cell);
                    }
                    translatedRows.add(cells);
                }
            }
        }
        return new TableModel(columns, translatedRows);
    }

}
