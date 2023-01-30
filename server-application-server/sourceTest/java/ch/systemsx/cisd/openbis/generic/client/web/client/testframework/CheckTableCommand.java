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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.shared.basic.Row;

/**
 * Command for checking the content (i.e. <code>ListStore</code>) of a table (i.e. <code>Grid</code> ). It uses a fluent API approach for its methods
 * to prepare expectations.
 * 
 * @author Franz-Josef Elmer
 */
public class CheckTableCommand extends AbstractDefaultTestCommand
{
    private abstract static class ColumnModelExpectation
    {
        private final String columnID;

        ColumnModelExpectation(String columnID)
        {
            this.columnID = columnID;
        }

        protected ColumnConfig getColumn(ColumnModel columnModel)
        {
            ColumnConfig column = columnModel.getColumnById(columnID);
            if (column == null)
            {
                fail("Unknown column '" + columnID + "'.");
            }
            return column;
        }

        protected String createFailureMessage()
        {
            return "Column '" + columnID + "':";
        }

        public abstract void check(ColumnModel columnModel);
    }

    private static class ColumnHiddenExpectation extends ColumnModelExpectation
    {
        private final boolean hidden;

        ColumnHiddenExpectation(String columnID, boolean hidden)
        {
            super(columnID);
            this.hidden = hidden;
        }

        @Override
        public void check(ColumnModel columnModel)
        {
            assertEquals(createFailureMessage(), hidden, getColumn(columnModel).isHidden());
        }
    }

    private static class ColumnWidthExpectation extends ColumnModelExpectation
    {
        private final int width;

        ColumnWidthExpectation(String columnID, int width)
        {
            super(columnID);
            this.width = width;
        }

        @Override
        public void check(ColumnModel columnModel)
        {
            assertEquals(createFailureMessage(), width, getColumn(columnModel).getWidth());
        }
    }

    private final String gridID;

    private int expectedNumberOfRows = -1;

    private int expectedTotalNumberOfColumns = -1; // including hidden columns

    private final List<ColumnModelExpectation> columnModelExpectations =
            new ArrayList<ColumnModelExpectation>();

    private final List<Row> expectedRows = new ArrayList<Row>();

    private final List<Row> unexpectedRows = new ArrayList<Row>();

    /**
     * Creates an instance for the specified table or grid ID.
     */
    public CheckTableCommand(final String gridId)
    {
        this.gridID = gridId;
    }

    public CheckTableCommand expectedColumnHidden(String columnID, boolean hidden)
    {
        columnModelExpectations.add(new ColumnHiddenExpectation(columnID, hidden));
        return this;
    }

    public CheckTableCommand expectedColumnWidth(String columnID, int width)
    {
        columnModelExpectations.add(new ColumnWidthExpectation(columnID, width));
        return this;
    }

    /**
     * Prepares this with the expectation upon the number of table rows.
     */
    public CheckTableCommand expectedSize(final int numberOfRows)
    {
        this.expectedNumberOfRows = numberOfRows;
        return this;
    }

    /**
     * Prepares this with the expectation upon the number of table columns, including hidden columns.
     */
    public CheckTableCommand expectedColumnsNumber(final int expectedNumberOfColumns)
    {
        this.expectedTotalNumberOfColumns = expectedNumberOfColumns;
        return this;
    }

    public CheckTableCommand unexpectedRow(final Row row)
    {
        unexpectedRows.add(row);
        return this;
    }

    /**
     * Prepares this with the expectation upon a certain column in a row to appear in the table.
     */

    public CheckTableCommand expectedColumn(String columnId, String columnValue)
    {
        expectedRow(new Row().withCell(columnId, columnValue));
        return this;
    }

    /**
     * Prepares this with the expectation upon a certain row to appear in the table.
     */
    public CheckTableCommand expectedRow(final Row row)
    {
        expectedRows.add(row);
        return this;
    }

    @Override
    public void execute()
    {
        Grid<ModelData> grid = checkColumnModelExpectations();
        final ListStore<ModelData> store = grid.getStore();
        final List<Row> matchedRows = new ArrayList<Row>();
        for (final Row expectedRow : expectedRows)
        {
            for (int i = 0; i < store.getCount(); i++)
            {
                final ModelData row = store.getAt(i);
                if (match(expectedRow, row))
                {
                    matchedRows.add(expectedRow);
                    break;
                }
            }
        }
        expectedRows.removeAll(matchedRows);
        if (expectedRows.isEmpty() == false)
        {
            final StringBuffer buffer = new StringBuffer("Unmatched expected rows:");
            for (final Row expectedRow : expectedRows)
            {
                buffer.append('\n').append(expectedRow);
            }
            buffer.append("\nActual rows:");
            for (int i = 0; i < store.getCount(); i++)
            {
                final ModelData row = store.getAt(i);
                buffer.append("\n").append(row.getProperties());
            }
            fail(buffer.toString());
        }
        assertExpectedNumberOfRows(store);
        assertExpectedNumberOfColumns(grid);

        for (final Row unexpectedRow : unexpectedRows)
        {
            for (int i = 0; i < store.getCount(); i++)
            {
                final ModelData row = store.getAt(i);
                assertFalse(match(unexpectedRow, row));
            }
        }
    }

    private void assertExpectedNumberOfRows(final ListStore<ModelData> store)
    {
        if (expectedNumberOfRows >= 0)
        {
            assertEquals(expectedNumberOfRows, store.getCount());
        }
    }

    private void assertExpectedNumberOfColumns(Grid<ModelData> grid)
    {
        if (expectedTotalNumberOfColumns >= 0)
        {
            assertEquals(expectedTotalNumberOfColumns, grid.getColumnModel().getColumnCount());
        }
    }

    private Grid<ModelData> checkColumnModelExpectations()
    {
        Grid<ModelData> grid = GWTTestUtil.getGridWithID(gridID);
        ColumnModel columnModel = grid.getColumnModel();
        for (ColumnModelExpectation expectation : columnModelExpectations)
        {
            expectation.check(columnModel);
        }
        return grid;
    }

    private boolean match(final Row expectedRow, final ModelData row)
    {
        for (final Map.Entry<String, Object> entry : expectedRow.getColumnIDValuesMap().entrySet())
        {
            String key = entry.getKey();
            Object rowColumnValue = row.get(key);
            Object expectedColumnValue = wrapNull(entry.getValue());
            if (TestUtil.isEqual(rowColumnValue, expectedColumnValue) == false)
            {
                return false;
            }
        }
        return true;
    }

    private Object wrapNull(Object value)
    {
        if (value == null)
        {
            return "";
        } else
        {
            return value;
        }
    }

}
