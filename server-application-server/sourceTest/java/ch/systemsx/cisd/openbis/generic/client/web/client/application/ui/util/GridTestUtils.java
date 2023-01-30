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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.TestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Utility methods to test subclasses of {@link TypedTableGrid}
 * 
 * @author Tomasz Pylak
 */
public class GridTestUtils
{

    /** simulates pressing the refresh button for the currently opened grid */
    public static final void pressRefreshGridButton()
    {
        final Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
        Assert.assertTrue(refresh.isEnabled());
        GWTTestUtil.clickButtonWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
    }

    /**
     * Fires a double click event on a first row which contains given value in a column with a specified id.
     */
    public static <T extends ModelData> void fireDoubleClick(final Grid<T> table, String columnId,
            String columnValue)
    {
        GridEvent<T> event = createGridEvent(table, columnId, columnValue);
        table.fireEvent(Events.CellDoubleClick, event);
    }

    /**
     * Fires a single click event on a first row which contains given value in a column with a specified id.
     */
    public static <T extends ModelData> void fireSingleClick(final Grid<T> table, String columnId,
            String columnValue)
    {
        GridEvent<T> event = createGridEvent(table, columnId, columnValue);
        table.fireEvent(Events.CellClick, event);
    }

    /**
     * Fires a selection of a first row in given table which contains given value in a column with a specified id.
     */
    public static <T extends ModelData> void fireSelectRow(final Grid<T> table, String columnId,
            String columnValue)
    {
        T row = getFirstRowWithColumnValue(table, columnId, columnValue);
        table.getSelectionModel().select(row, false);
    }

    private static <T extends ModelData> GridEvent<T> createGridEvent(final Grid<T> table,
            String columnId, String columnValue)
    {
        int rowIndex = getFirstRowIndexWithColumnValue(table, columnId, columnValue);

        final GridEvent<T> gridEvent = new GridEvent<T>(table);
        gridEvent.setRowIndex(rowIndex);
        gridEvent.setColIndex(table.getColumnModel().findColumnIndex(columnId));
        return gridEvent;
    }

    private static <T extends ModelData> T getFirstRowWithColumnValue(final Grid<T> table,
            String columnId, String columnValue)
    {
        int rowIndex = getFirstRowIndexWithColumnValue(table, columnId, columnValue);
        final ListStore<T> store = table.getStore();
        final T row = store.getAt(rowIndex);
        return row;
    }

    private static <T extends ModelData> int getFirstRowIndexWithColumnValue(final Grid<T> table,
            String columnId, String columnValue)
    {
        final ListStore<T> store = table.getStore();
        String codes = "";
        for (int i = 0; i < store.getCount(); i++)
        {
            final T row = store.getAt(i);
            String rowCode = TestUtil.normalize(row.get(columnId));
            if (columnValue.equalsIgnoreCase(rowCode))
            {
                return i;
            }
            codes += rowCode;
            if (i < store.getCount() - 1)
            {
                codes += ", ";
            }
        }
        Assert.fail("The column with id '" + columnId + "' has never the value '" + columnValue
                + "'. Following values were found: " + codes);
        return -1; // just to make the compiler happy
    }

    public static String getPropertyColumnIdentifier(final String propertyCode,
            boolean managedInternally)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, managedInternally);
        return "property-" + (managedInternally ? "INTERN" : "USER") + "-"
                + propertyType.getSimpleCode();
    }

    public final static PropertyType createPropertyType(final String propertyCode,
            final boolean managedInternally)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setManagedInternally(managedInternally);
        propertyType.setSimpleCode(propertyCode.toUpperCase());
        return propertyType;
    }

    public static String getDataSetPropertyColumnIdentifier(final String propertyCode,
            boolean managedInternally)
    {
        return getPropertyColumnIdentifier(propertyCode, managedInternally);
    }
}
