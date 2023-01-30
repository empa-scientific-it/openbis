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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * Helps in building a {@link TableModel}
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class SimpleTableModelBuilder
{
    private final List<TableModelRow> rows;

    private final List<TableModelColumnHeader> headers;

    private final Counters<String> counters = new Counters<String>();

    private final Map<String, Integer> titleToIndexMap;

    private final boolean uniqueHeaderTitles;

    private String messageOrNull;

    /**
     * Creates a new instance with non-unique header titles allowed.
     */
    public SimpleTableModelBuilder()
    {
        this(false);
    }

    /**
     * Creates a new instance.
     * 
     * @param uniqueHeaderTitles If <code>true</code> header title must be unique
     */
    public SimpleTableModelBuilder(boolean uniqueHeaderTitles)
    {
        this.uniqueHeaderTitles = uniqueHeaderTitles;
        this.rows = new ArrayList<TableModelRow>();
        this.headers = new ArrayList<TableModelColumnHeader>();
        titleToIndexMap = new HashMap<String, Integer>();
    }

    /**
     * Adds header with specified title and default column width 150.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with same title has already been added.
     */
    public void addHeader(String title)
    {
        addHeader(title, 150);
    }

    /**
     * Adds header with specified title, specified code and default column width 150.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with same title has already been added.
     */
    public void addHeader(String title, String code)
    {
        addHeader(title, code, 150);
    }

    /**
     * Adds header with specified title and specified default column width.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with same title has already been added.
     */
    public void addHeader(String title, int defaultColumnWidth)
    {
        addHeader(title, title, defaultColumnWidth);
    }

    /**
     * Adds header with specified title, specified code and specified default column width.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with same title has already been added.
     */
    private void addHeader(String title, String code, int defaultColumnWidth)
    {
        String id = createUniqueID(code);
        TableModelColumnHeader header = new TableModelColumnHeader(title, id, headers.size());
        header.setDefaultColumnWidth(defaultColumnWidth);
        Integer replacedValue = titleToIndexMap.put(title, headers.size());
        if (uniqueHeaderTitles && replacedValue != null)
        {
            throw new UserFailureException("There is already a header with title '" + title + "'.");
        }
        headers.add(header);
    }

    private String createUniqueID(String code)
    {
        int count = counters.count(code);
        return count == 1 ? code : code + count;
    }

    /**
     * Adds an empty row and returns a row builder for setting values of this row.
     * 
     * @throws UnsupportedOperationException if header titles are not forced to be unique
     */
    public IRowBuilder addRow()
    {
        if (uniqueHeaderTitles == false)
        {
            throw new UnsupportedOperationException(
                    "Method only supported for unique header titles.");
        }
        final List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
        StringTableCell emptyCell = new StringTableCell("");
        for (int i = 0; i < headers.size(); i++)
        {
            values.add(emptyCell);
        }
        rows.add(new TableModelRow(values));
        return new IRowBuilder()
            {
                @Override
                public void setCell(String headerTitle, String value)
                {
                    setCell(headerTitle, asText(value));
                }

                @Override
                public void setCell(String headerTitle, long value)
                {
                    setCell(headerTitle, asInteger(value));
                }

                @Override
                public void setCell(String headerTitle, double value)
                {
                    setCell(headerTitle, asDouble(value));
                }

                @Override
                public void setCell(String headerTitle, Date value)
                {
                    setCell(headerTitle, asDate(value));
                }

                @Override
                public void setCell(String headerTitle, ISerializableComparable value)
                {
                    Integer index = titleToIndexMap.get(headerTitle);
                    if (index == null)
                    {
                        throw new UserFailureException("Unkown column header title: " + headerTitle);
                    }
                    values.set(index, value);
                    setColumnDataType(index, value);
                    setColumnEntityKind(index, value);
                }

            };
    }

    /**
     * Adds specified list of values as a row.
     * 
     * @throws UserFailureException if more or less values than headers added.
     */
    public void addRow(List<ISerializableComparable> values)
    {
        if (values.size() != headers.size())
        {
            throw new UserFailureException(headers.size() + " row values expected instead of "
                    + values.size() + ".");
        }
        for (int i = 0; i < values.size(); i++)
        {
            ISerializableComparable value = values.get(i);
            setColumnDataType(i, value);
            setColumnEntityKind(i, value);
        }
        rows.add(new TableModelRow(values));
    }

    private void setColumnDataType(int index, ISerializableComparable value)
    {
        TableModelColumnHeader header = headers.get(index);
        DataTypeCode headerDataType = header.getDataType();
        DataTypeCode dataType = getDataTypeCodeFor(value);
        if (value != null && StringUtils.isBlank(value.toString()) == false)
        {
            DataTypeCode compatibleDataType =
                    DataTypeUtils.getCompatibleDataType(headerDataType, dataType);
            header.setDataType(compatibleDataType);
        }
    }

    private void setColumnEntityKind(Integer index, ISerializableComparable value)
    {
        TableModelColumnHeader header = headers.get(index);
        EntityKind headerEntityKind = header.tryGetEntityKind();
        EntityKind dataEntityKind = getEntityKindFor(value);
        if (dataEntityKind != null)
        {
            if (headerEntityKind == null || dataEntityKind.equals(headerEntityKind))
            {
                header.setEntityKind(dataEntityKind);
            }
        }
    }

    private EntityKind getEntityKindFor(ISerializableComparable value)
    {
        if (value instanceof EntityTableCell)
        {
            return ((EntityTableCell) value).getEntityKind();
        }
        return null;
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

    public SimpleTableModelBuilder addFullHeader(String... titles)
    {
        for (String title : titles)
        {
            addHeader(title);
        }

        return this;
    }

    public SimpleTableModelBuilder addFullRow(String... values)
    {
        List<ISerializableComparable> rowValues = new ArrayList<ISerializableComparable>();
        for (String value : values)
        {
            rowValues.add(SimpleTableModelBuilder.asText(value));
        }
        addRow(rowValues);
        return this;
    }

    public String tryGetMessage()
    {
        return messageOrNull;
    }

    public void setMessage(String message)
    {
        this.messageOrNull = message;
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

    public static ISerializableComparable asInteger(long num)
    {
        return new IntegerTableCell(num);
    }

    public static ISerializableComparable asDouble(double num)
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

    static ISerializableComparable createNullCell()
    {
        return new StringTableCell("");
    }

}
