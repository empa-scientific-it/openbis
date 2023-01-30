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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Tomasz Pylak
 */
public class ColumnDefsAndConfigs<T>
{
    private final List<ColumnConfig> columnConfigs;

    private final Set<IColumnDefinition<T>> columnDefs;

    public static <T> ColumnDefsAndConfigs<T> create(
            List<? extends IColumnDefinitionUI<T>> columnsSchema, IViewContext<?> viewContext)
    {
        ColumnDefsAndConfigs<T> result = new ColumnDefsAndConfigs<T>();
        result.addColumns(columnsSchema, viewContext);
        return result;
    }

    private ColumnDefsAndConfigs()
    {
        this.columnConfigs = new ArrayList<ColumnConfig>();
        this.columnDefs = new HashSet<IColumnDefinition<T>>();
    }

    public void addColumns(List<? extends IColumnDefinitionUI<T>> columnsSchema,
            IViewContext<?> viewContext)
    {
        for (IColumnDefinitionUI<T> column : columnsSchema)
        {
            addColumn(column, viewContext);
        }
    }

    public void addColumn(IColumnDefinitionUI<T> column,
            GridCellRenderer<BaseEntityModel<?>> rendererOrNull, IViewContext<?> viewContext)
    {
        ColumnConfig columnConfig = createColumn(column, viewContext);
        if (rendererOrNull != null)
        {
            columnConfig.setRenderer(rendererOrNull);
        }
        addColumn(column, columnConfig);
    }

    private void addColumn(IColumnDefinitionUI<T> column, IViewContext<?> viewContext)
    {
        addColumn(column, createColumn(column, viewContext));
    }

    private void addColumn(IColumnDefinitionUI<T> column, ColumnConfig columnConfig)
    {
        columnConfigs.add(columnConfig);
        columnDefs.add(column);
    }

    public void setGridCellRendererFor(String columnID, GridCellRenderer<BaseEntityModel<?>> render)
    {
        for (ColumnConfig columnConfig : columnConfigs)
        {
            if (columnConfig.getDataIndex().equals(columnID))
            {
                columnConfig.setRenderer(render);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static <T> ColumnConfig createColumn(IColumnDefinitionUI<T> column,
            IViewContext<?> viewContext)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setSortable(true);
        columnConfig.setId(column.getIdentifier());
        final String headerTitle = column.getHeader();
        columnConfig.setHeader(headerTitle);
        if (column.isNumeric())
        {
            columnConfig.setAlignment(HorizontalAlignment.RIGHT);
        }
        columnConfig.setWidth(column.getWidth());
        columnConfig.setHidden(column.isHidden());
        String toolTip =
                "[Click] to sort, [SHIFT+Click] to adjust the width, "
                        + "[Drag & Drop] to change order.";
        if (column.isEditable() && ClientStaticState.isSimpleMode() == false)
        {
            toolTip += " This is an editibale column. Just double-click on a cell.";
            CellEditor editor = ColumnUtils.createCellEditor(column, viewContext);
            columnConfig.setEditor(editor);
        }
        columnConfig.setToolTip(toolTip);
        return columnConfig;
    }

    public Set<IColumnDefinition<T>> getColumnDefs()
    {
        return columnDefs;
    }

    public List<ColumnConfig> getColumnConfigs()
    {
        return columnConfigs;
    }
}
