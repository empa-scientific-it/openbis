package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;

/**
 * Represents a column of the table.
 * 
 * @author Izabela Adamczyk
 */
public class ColumnDataModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    static final String COLUMN_ID = "column_id";

    public static final String ADDRESS = "address";

    static final String HAS_FILTER = "HAS_FILTER";

    static final String IS_VISIBLE = "IS_VISIBLE";

    public static final String HEADER = "header";

    public ColumnDataModel(String header, boolean isVisible, boolean hasFilter, String columnID)
    {
        setHeader(header);
        setIsVisible(isVisible);
        setHasFilter(hasFilter);
        setColumnID(columnID);
        setAddress("row.col('" + columnID + "')");
    }

    private void setAddress(String address)
    {
        set(ADDRESS, address);
    }

    private void setColumnID(String columnID)
    {
        set(COLUMN_ID, columnID);
    }

    private void setIsVisible(boolean isVisible)
    {
        set(IS_VISIBLE, isVisible);
    }

    private void setHasFilter(boolean hasFilter)
    {
        set(HAS_FILTER, hasFilter);
    }

    private void setHeader(String header)
    {
        set(HEADER, header);
    }

    public String getColumnID()
    {
        return get(COLUMN_ID);
    }

    public boolean isVisible()
    {
        return get(IS_VISIBLE);
    }

    public boolean hasFilter()
    {
        return get(HAS_FILTER);
    }

    public String getHeader()
    {
        return get(HEADER);
    }

    public String getAddress()
    {
        return get(ADDRESS);
    }

}
