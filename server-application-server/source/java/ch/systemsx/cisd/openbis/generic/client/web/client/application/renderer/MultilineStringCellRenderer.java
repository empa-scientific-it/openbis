package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;

public class MultilineStringCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    @Override
    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        return new MultilineHTML(StringUtils.toStringEmptyIfNull(model.get(property))).toString();
    }
}
