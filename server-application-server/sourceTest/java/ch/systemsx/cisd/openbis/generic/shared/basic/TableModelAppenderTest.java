/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithDifferentColumnCountException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithDifferentColumnIdsException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithIncompatibleColumnTypesException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author pkupczyk
 */
public class TableModelAppenderTest
{

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAppendNullModel()
    {
        TableModelAppender appender = new TableModelAppender();
        appender.append(null);
    }

    @Test
    public void testAppendEmptyModel()
    {
        TableModelAppender appender = new TableModelAppender();
        appender.append(new TableModel(Collections.<TableModelColumnHeader> emptyList(), Collections.<TableModelRow> emptyList()));
        TableModel result = appender.toTableModel();

        Assert.assertEquals(0, result.getHeader().size());
        Assert.assertEquals(0, result.getRows().size());
    }

    @Test
    public void testAppendModelWithSameColumns()
    {
        SimpleTableModelBuilder builder1 = new SimpleTableModelBuilder();
        builder1.addHeader("column1");
        builder1.addHeader("column2");
        builder1.addFullRow("row1_column1", "row1_column2");
        builder1.addFullRow("row2_column1", "row2_column2");

        SimpleTableModelBuilder builder2 = new SimpleTableModelBuilder();
        builder2.addHeader("column1");
        builder2.addHeader("column2");
        builder2.addFullRow("row3_column1", "row3_column2");

        TableModelAppender appender = new TableModelAppender();
        appender.append(builder1.getTableModel());
        appender.append(builder2.getTableModel());
        TableModel result = appender.toTableModel();

        Assert.assertEquals(2, result.getHeader().size());
        Assert.assertEquals(3, result.getRows().size());

        assertHeaders(result, "column1", "column2");
        assertRow(result, 0, "row1_column1", "row1_column2");
        assertRow(result, 1, "row2_column1", "row2_column2");
        assertRow(result, 2, "row3_column1", "row3_column2");
    }

    @Test
    public void testAppendModelWithDifferentNumberOfColumns()
    {
        SimpleTableModelBuilder builder1 = new SimpleTableModelBuilder();
        builder1.addHeader("column1");
        builder1.addHeader("column2");
        builder1.addFullRow("row1_column1", "row1_column2");
        builder1.addFullRow("row2_column1", "row2_column2");

        SimpleTableModelBuilder builder2 = new SimpleTableModelBuilder();
        builder2.addHeader("column1");
        builder2.addFullRow("row3_column1");

        try
        {
            TableModelAppender appender = new TableModelAppender();
            appender.append(builder1.getTableModel());
            appender.append(builder2.getTableModel());
            Assert.fail();
        } catch (TableModelWithDifferentColumnCountException e)
        {
            Assert.assertEquals(2, e.getExpectedColumnCount());
            Assert.assertEquals(1, e.getAppendedColumnCount());
        }
    }

    @Test
    public void testAppendModelWithSameIdsAndCompatibleTypesOfColumns()
    {
        SimpleTableModelBuilder builder1 = new SimpleTableModelBuilder();
        builder1.addHeader("column1");
        builder1.addHeader("column2");
        builder1.addRow(Arrays.asList(SimpleTableModelBuilder.asText("row1_column1"), SimpleTableModelBuilder.asText("row1_column2")));
        builder1.addRow(Arrays.asList(SimpleTableModelBuilder.asText("row2_column1"), SimpleTableModelBuilder.asText("row2_column2")));

        SimpleTableModelBuilder builder2 = new SimpleTableModelBuilder();
        builder2.addHeader("column1");
        builder2.addHeader("column2");
        builder2.addRow(Arrays.asList(null, SimpleTableModelBuilder.asText("row3_column2")));

        TableModelAppender appender = new TableModelAppender();
        appender.append(builder1.getTableModel());
        appender.append(builder2.getTableModel());
        TableModel result = appender.toTableModel();

        Assert.assertEquals(2, result.getHeader().size());
        Assert.assertEquals(3, result.getRows().size());

        assertHeaders(result, "column1", "column2");
        assertRow(result, 0, "row1_column1", "row1_column2");
        assertRow(result, 1, "row2_column1", "row2_column2");
        assertRow(result, 2, null, "row3_column2");
    }

    @Test
    public void testAppendModelWithSameIdsAndIncompatibleTypesOfColumns()
    {
        SimpleTableModelBuilder builder1 = new SimpleTableModelBuilder();
        builder1.addHeader("column1");
        builder1.addHeader("column2");
        builder1.addRow(Arrays.asList(SimpleTableModelBuilder.asText("row1_column1"), SimpleTableModelBuilder.asText("row1_column2")));
        builder1.addRow(Arrays.asList(SimpleTableModelBuilder.asText("row2_column1"), SimpleTableModelBuilder.asText("row2_column2")));

        SimpleTableModelBuilder builder2 = new SimpleTableModelBuilder();
        builder2.addHeader("column1");
        builder2.addHeader("column2");
        builder2.addRow(Arrays.asList(SimpleTableModelBuilder.asText("row3_column1"), SimpleTableModelBuilder.asInteger(3)));

        try
        {
            TableModelAppender appender = new TableModelAppender();
            appender.append(builder1.getTableModel());
            appender.append(builder2.getTableModel());
            Assert.fail();
        } catch (TableModelWithIncompatibleColumnTypesException e)
        {
            Assert.assertEquals(Arrays.asList(DataTypeCode.VARCHAR, DataTypeCode.VARCHAR), e.getExpectedColumnTypes());
            Assert.assertEquals(Arrays.asList(DataTypeCode.VARCHAR, DataTypeCode.INTEGER), e.getAppendedColumnTypes());
        }
    }

    @Test
    public void testAppendModelWithDifferentIdsOfColumns()
    {
        SimpleTableModelBuilder builder1 = new SimpleTableModelBuilder();
        builder1.addHeader("column1");
        builder1.addHeader("column2");
        builder1.addFullRow("row1_column1", "row1_column2");
        builder1.addFullRow("row2_column1", "row2_column2");

        SimpleTableModelBuilder builder2 = new SimpleTableModelBuilder();
        builder2.addHeader("column1");
        builder2.addHeader("column2a");
        builder2.addFullRow("row3_column1", "row3_column2a");

        try
        {
            TableModelAppender appender = new TableModelAppender();
            appender.append(builder1.getTableModel());
            appender.append(builder2.getTableModel());
            Assert.fail();
        } catch (TableModelWithDifferentColumnIdsException e)
        {
            Assert.assertEquals(Arrays.asList("column1", "column2"), e.getExpectedColumnIds());
            Assert.assertEquals(Arrays.asList("column1", "column2a"), e.getAppendedColumnIds());
        }
    }

    private void assertHeaders(TableModel tableModel, String... headers)
    {
        int columnIndex = 0;
        for (TableModelColumnHeader header : tableModel.getHeader())
        {
            Assert.assertEquals(headers[columnIndex++], header.getId());
        }
    }

    private void assertRow(TableModel tableModel, int rowIndex, String... rowValues)
    {
        TableModelRow row = tableModel.getRows().get(rowIndex);
        int columnIndex = 0;

        for (ISerializableComparable value : row.getValues())
        {
            StringTableCell cell = (StringTableCell) value;
            Assert.assertEquals(rowValues[columnIndex++], cell != null ? cell.toString() : null);
        }
    }
}
