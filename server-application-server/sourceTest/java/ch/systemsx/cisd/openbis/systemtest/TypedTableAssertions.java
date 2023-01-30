package ch.systemsx.cisd.openbis.systemtest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.AssertJUnit;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A utility class that can be used to simplify assertions in {@link TypedTableGrid}-related headless system tests.
 * 
 * @author Kaloyan Enimanev
 */
public class TypedTableAssertions<T extends Serializable> extends AssertJUnit
{

    /**
     * a convenience method, that can be used as a static import to achieve a fluent-API like programming style.
     */
    public static <S extends Serializable> TypedTableAssertions<S> assertTable(
            TypedTableResultSet<S> tableResultSet)
    {
        return new TypedTableAssertions<S>(tableResultSet);
    }

    /**
     * a convenience method, that can be used as a static import to achieve a fluent-API like programming style.
     */
    public static <S extends Serializable> TypedTableAssertions<S>.ColumnAssertions assertColumn(
            TypedTableResultSet<S> tableResultSet, String columnName)
    {
        return new TypedTableAssertions<S>(tableResultSet).hasColumn(columnName);
    }

    private TypedTableResultSet<T> tableResultSet;

    private Map<String, TableModelColumnHeader> headers;

    /**
     * the constructor is tentatively marked as "private" for now. We could make it public in the future if we decide it will suite our tests better.
     * 
     * @param tableResultSet the typed-table result set
     */
    private TypedTableAssertions(TypedTableResultSet<T> tableResultSet)
    {
        this.tableResultSet = tableResultSet;
        initializeHeaders();
    }

    /**
     * assertion for the total rows number of a table.
     */
    public TypedTableAssertions<T> hasNumberOfRows(int rowsNumber)
    {
        String error = null;
        if (rowsNumber != tableResultSet.getResultSet().getTotalLength())
        {
            // only build error message when the test is going to fail
            error = String.format("Invalid number of rows in %s", getTableContents());
        }
        assertEquals(error, rowsNumber, tableResultSet.getResultSet().getTotalLength());
        return this;
    }

    /**
     * asserts that the table contains a specified row.
     */
    public TypedTableAssertions<T> containsRow(Row row)
    {
        Map<String, Object> rowValues = row.getColumnIDValuesMap();
        // check all specified column names exist in the table
        for (String columnName : rowValues.keySet())
        {
            hasColumn(columnName);
        }

        boolean rowFound = false;
        GridRowModels<TableModelRowWithObject<T>> list = tableResultSet.getResultSet().getList();
        for (GridRowModel<TableModelRowWithObject<T>> rowModel : list)
        {
            if (rowsEqual(rowValues, rowModel))
            {
                rowFound = true;
                break;
            }
        }

        if (false == rowFound)
        {
            String errMessage =
                    String.format("Row with cells %s was not discovered in the "
                            + "table. Its contents were %s", rowValues, getTableContents());
            fail(errMessage);
        }

        return this;
    }

    public ColumnAssertions hasColumn(String columnId)
    {
        return new ColumnAssertions(columnId);
    }

    private void initializeHeaders()
    {
        headers = new HashMap<String, TableModelColumnHeader>();
        GridRowModels<TableModelRowWithObject<T>> list = tableResultSet.getResultSet().getList();

        for (TableModelColumnHeader tmch : list.getColumnHeaders())
        {
            headers.put(tmch.getId(), tmch);
        }
    }

    private boolean rowsEqual(Map<String, Object> rowValues,
            GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        List<ISerializableComparable> modelValues = rowModel.getOriginalObject().getValues();
        boolean rowsEqual = true;
        for (Entry<String, Object> rowCell : rowValues.entrySet())
        {
            int valueIndex = headers.get(rowCell.getKey()).getIndex();
            ISerializableComparable modelValue = modelValues.get(valueIndex);
            // the modelValue.toString() invocation here is tricky. We do it, since in most of
            // test cases we match the table contents with Strings. However, if someone wishes
            // to compare real ISerializableComparable objects we'll need to change the
            // way we compare our expectations with the model.
            if (false == rowCell.getValue().equals(modelValue.toString()))
            {
                rowsEqual = false;
                break;
            }
        }
        return rowsEqual;
    }

    private Collection<String> getHeaderNames()
    {
        return headers.keySet();
    }

    private List<List<String>> getTableContents()
    {
        GridRowModels<TableModelRowWithObject<T>> list = tableResultSet.getResultSet().getList();
        List<List<String>> result = new ArrayList<List<String>>();
        for (GridRowModel<TableModelRowWithObject<T>> rowModel : list)
        {
            List<String> rowContents = new ArrayList<String>();
            for (Object value : rowModel.getOriginalObject().getValues())
            {
                rowContents.add(value.toString());
            }
            result.add(rowContents);
        }
        return result;
    }

    /**
     * column-related assertions.
     */
    public class ColumnAssertions
    {
        private TableModelColumnHeader header = null;

        private ColumnAssertions(String columnId)
        {
            initTableHeader(columnId);
        }

        /**
         * prepares the column header for subsequent assertions.
         */
        private void initTableHeader(String columnId)
        {
            GridRowModels<TableModelRowWithObject<T>> list =
                    tableResultSet.getResultSet().getList();
            for (TableModelColumnHeader tmch : list.getColumnHeaders())
            {
                if (tmch.getId().equalsIgnoreCase(columnId))
                {
                    header = tmch;
                    return;
                }
            }

            String errMessage =
                    String.format(
                            "Table header with id '%s' was not discovered in the headers list: %s",
                            columnId, getHeaderNames());
            assertNotNull(errMessage, header);
        }

        /**
         * just a convenience method redirecting to the {@link #containsValue(String)} with a single parameter.
         */
        public void containsValues(String... values)
        {
            for (String value : values)
            {
                containsValue(value);
            }
        }

        /**
         * asserts that the column contains a specified value.
         * 
         * @param value a value to be searched for
         */
        public void containsValue(String value)
        {
            GridRowModels<TableModelRowWithObject<T>> list =
                    tableResultSet.getResultSet().getList();
            for (GridRowModel<TableModelRowWithObject<T>> rowModel : list)
            {
                List<ISerializableComparable> rowValues = rowModel.getOriginalObject().getValues();
                String cellValue = rowValues.get(header.getIndex()).toString();
                if (value.equals(cellValue))
                {
                    // the expected value has been found
                    return;
                }
            }
            String errMessage =
                    String.format(
                            "The value '%s' was not discovered in column %s. Its contents were : %s",
                            value, header.getId(), getColumnContents());
            fail(errMessage);

        }

        private List<String> getColumnContents()
        {
            GridRowModels<TableModelRowWithObject<T>> list =
                    tableResultSet.getResultSet().getList();
            List<String> result = new ArrayList<String>();
            for (GridRowModel<TableModelRowWithObject<T>> rowModel : list)
            {
                List<ISerializableComparable> rowValues = rowModel.getOriginalObject().getValues();
                String cellValue = rowValues.get(header.getIndex()).toString();
                result.add(cellValue);
            }
            return result;
        }
    }

}