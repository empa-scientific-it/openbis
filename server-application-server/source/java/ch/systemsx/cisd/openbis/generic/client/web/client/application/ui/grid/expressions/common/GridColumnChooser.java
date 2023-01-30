package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.common;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnSettingsDataModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Allows to select grid columns which should be used in the grid custom filter or column expression. Chosen value is a string that can be used in an
 * expression to address selected grid columns.
 * 
 * @author Izabela Adamczyk
 */
class GridColumnChooser
{

    private final Grid<ColumnDataModel> grid;

    public GridColumnChooser(AbstractColumnSettingsDataModelProvider columnDataModelProvider,
            IMessageProvider messageProvider)
    {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig addressColumn =
                new ColumnConfig(ColumnDataModel.HEADER, messageProvider
                        .getMessage(Dict.GRID_COLUMN_NAME_HEADER), 300);
        ColumnConfig idColumn =
                new ColumnConfig(ColumnDataModel.ADDRESS, messageProvider
                        .getMessage(Dict.HOW_TO_ADDRESS), 300);

        configs.add(addressColumn);
        configs.add(idColumn);
        for (ColumnConfig column : configs)
        {
            column.setSortable(false);
            column.setMenuDisabled(true);
        }

        grid =
                new Grid<ColumnDataModel>(
                        createStore(columnDataModelProvider.getColumnDataModels()),
                        new ColumnModel(configs));
        grid.setHideHeaders(false);
        grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
    }

    private static ListStore<ColumnDataModel> createStore(List<ColumnDataModel> list)
    {
        ListStore<ColumnDataModel> store = new ListStore<ColumnDataModel>();
        store.add(list);
        return store;
    }

    public Component getComponent()
    {
        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        grid.setAutoWidth(true);
        grid.setAutoHeight(true);
        cp.add(grid);
        cp.setScrollMode(Scroll.AUTOY);
        return cp;
    }

    public List<String> getSelectedItems()
    {
        List<String> result = new ArrayList<String>();
        List<ColumnDataModel> items = grid.getSelectionModel().getSelectedItems();
        for (ColumnDataModel item : items)
        {
            result.add(item.getAddress());
        }
        return result;
    }

}
