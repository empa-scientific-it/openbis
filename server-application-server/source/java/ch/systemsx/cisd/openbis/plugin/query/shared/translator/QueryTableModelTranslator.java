/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.translator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Translates TableModel objects into QueryTableModel objects
 * 
 * @author cramakri
 */
public class QueryTableModelTranslator
{
    private final TableModel originalTableModel;

    public QueryTableModelTranslator(TableModel tableModel)
    {
        this.originalTableModel = tableModel;
    }

    public QueryTableModel translate()
    {
        return translate(originalTableModel);
    }

    private QueryTableModel translate(TableModel result)
    {
        List<TableModelColumnHeader> headers = result.getHeader();
        ArrayList<QueryTableColumn> translatedHeaders = new ArrayList<QueryTableColumn>();
        for (TableModelColumnHeader header : headers)
        {
            String title = header.getTitle();
            QueryTableColumnDataType dataType = Util.translate(header.getDataType());
            translatedHeaders.add(new QueryTableColumn(title, dataType));
        }
        QueryTableModel tableModel = new QueryTableModel(translatedHeaders);
        List<TableModelRow> rows = result.getRows();
        SimpleDateFormat format = new SimpleDateFormat(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN);
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            Serializable[] translatedValues = new Serializable[values.size()];
            for (int i = 0, n = values.size(); i < n; i++)
            {
                ISerializableComparable value = values.get(i);
                Serializable translatedValue = null;
                if (value instanceof IntegerTableCell)
                {
                    translatedValue = ((IntegerTableCell) value).getNumber();
                } else if (value instanceof DoubleTableCell)
                {
                    translatedValue = ((DoubleTableCell) value).getNumber();
                } else if (value instanceof DateTableCell)
                {
                    translatedValue = format.format(((DateTableCell) value).getDateTime());
                } else if (value != null)
                {
                    translatedValue = value.toString();
                }
                translatedValues[i] = translatedValue;
            }
            tableModel.addRow(translatedValues);
        }
        return tableModel;
    }

}
