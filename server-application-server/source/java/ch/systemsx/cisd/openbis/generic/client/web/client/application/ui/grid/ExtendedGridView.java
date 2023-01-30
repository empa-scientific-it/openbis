package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnHeader;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

/**
 * {@link GridView} with {@link ColumnHeader} allowing to define additional behavior for header click when SHIFT button is pressed at the same time.
 * By default the width of the column is adjusted.
 * 
 * @author Izabela Adamczyk
 */
public class ExtendedGridView extends GridView
{
    @Override
    protected ColumnHeader newColumnHeader()
    {
        header = new ColumnHeader(grid, cm)
            {
                @Override
                protected ComponentEvent createColumnEvent(ColumnHeader pHeader, int column,
                        Menu componentMenu)
                {
                    GridEvent<ModelData> event = new GridEvent<ModelData>(grid);
                    event.setColIndex(column);
                    event.setMenu(componentMenu);
                    return event;
                }

                @Override
                protected Menu getContextMenu(int column)
                {
                    return createContextMenu(column);
                }

                @Override
                protected void onColumnSplitterMoved(int colIndex, int pWidth)
                {
                    super.onColumnSplitterMoved(colIndex, pWidth);
                    ExtendedGridView.this.onColumnSplitterMoved(colIndex, pWidth);
                }

                @Override
                protected void onHeaderClick(ComponentEvent ce, int column)
                {
                    super.onHeaderClick(ce, column);
                    if (ce.isShiftKey())
                    {
                        ExtendedGridView.this.onHeaderClickWithShift(grid, column);
                    } else
                    {
                        ExtendedGridView.this.onHeaderClick(grid, column);
                    }
                }

            };
        header.setSplitterWidth(splitterWidth);
        header.setMinColumnWidth(grid.getMinColumnWidth());
        return header;
    }

    protected void onHeaderClickWithShift(Grid<ModelData> pGrid, int column)
    {
        int margin = 10;
        int newWidth = calculateWidthWithScroll(pGrid, column) + margin;
        cm.setColumnWidth(column, newWidth);
    }

    private int calculateWidthWithScroll(Grid<ModelData> pGrid, int column)
    {
        GridView view = pGrid.getView();
        Element headerCell = (Element) view.getHeaderCell(calculateHeaderCellIndex(pGrid, column));
        headerCell = (Element) headerCell.getFirstChildElement();
        headerCell.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
        int max = headerCell.getScrollWidth();
        headerCell.getStyle().setProperty("width", "auto");
        for (int i = 0; i < pGrid.getStore().getCount(); i++)
        {
            Element td = (Element) view.getCell(i, column);
            List<Node> nodes = extractNodes(td);
            for (Node n : nodes)
            {
                if (com.google.gwt.dom.client.Element.is(n))
                {
                    com.google.gwt.dom.client.Element e = com.google.gwt.dom.client.Element.as(n);
                    if (e.getTagName().equalsIgnoreCase("img") == false)
                    {
                        e.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
                    }
                }
            }
            Element element = (Element) td.getFirstChildElement();
            element.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
            int width = element.getScrollWidth();
            element.getStyle().setProperty("width", "auto");
            if (width > max)
            {
                max = width;
            }
        }
        return max;
    }

    private List<Node> extractNodes(Element element)
    {
        List<Node> visited = new ArrayList<Node>();
        List<Node> toVisit = new ArrayList<Node>();
        toVisit.add(element);
        while (toVisit.isEmpty() == false)
        {
            Node n = toVisit.get(0);
            toVisit.remove(n);
            if (visited.contains(n) == false)
            {
                visited.add(n);
                NodeList<Node> childNodes = n.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++)
                {
                    Node c = childNodes.getItem(j);
                    if (toVisit.contains(c) == false)
                    {
                        toVisit.add(c);
                    }
                }
            }
        }
        return visited;
    }

    private int calculateHeaderCellIndex(Grid<ModelData> pGrid, int column)
    {
        // WORKAROUND: getHeaderCell takes into account only visible columns
        int headerCellIndex = column;
        for (int i = 0; i < column; i++)
        {
            if (pGrid.getColumnModel().getColumn(i).isHidden())
            {
                headerCellIndex--;
            }
        }
        return headerCellIndex;
    }

    @Override
    // WORKAROUND extended rendering of the cells to include information about potentially outdated
    // values (see SP-198). Basically the method was copied from super class ant only part
    // responsible for rendering outdated cells was added.
    protected String doRender(List<ColumnData> cs, List<ModelData> rows, int startRow,
            int colCount, boolean stripe)
    {
        int last = colCount - 1;
        String tstyle = "width:" + getTotalWidth() + "px;";

        StringBuilder buf = new StringBuilder();

        for (int j = 0; j < rows.size(); j++)
        {
            @SuppressWarnings("cast")
            ModelData model = (ModelData) rows.get(j);

            model = prepareData(model);

            Record r = ds.hasRecord(model) ? ds.getRecord(model) : null;

            int rowBodyColSpanCount = colCount;
            if (enableRowBody)
            {
                if (grid.getSelectionModel() instanceof CheckBoxSelectionModel<?>)
                {
                    CheckBoxSelectionModel<?> sm =
                            (CheckBoxSelectionModel<?>) grid.getSelectionModel();
                    if (cm.getColumnById(sm.getColumn().getId()) != null)
                    {
                        rowBodyColSpanCount--;
                    }
                }
                for (ColumnConfig c : cm.getColumns())
                {
                    if (c instanceof RowExpander || c instanceof RowNumberer)
                    {
                        rowBodyColSpanCount--;
                    }
                }
            }
            int rowIndex = (j + startRow);

            if (GXT.isAriaEnabled())
            {
                buf.append("<div role=\"row\" aria-level=\"2\" class=\"x-grid3-row ");
            } else
            {
                buf.append("<div class=\"x-grid3-row ");
            }

            if (stripe && ((rowIndex + 1) % 2 == 0))
            {
                buf.append(" x-grid3-row-alt");
            }
            if (!selectable)
            {
                buf.append(" x-unselectable-single");
            }

            if (super.isShowDirtyCells() && r != null && r.isDirty())
            {
                buf.append(" x-grid3-dirty-row");
            }
            if (viewConfig != null)
            {
                buf.append(" ");
                buf.append(viewConfig.getRowStyle(model, rowIndex, ds));
            }
            buf.append("\" style=\"");
            buf.append(tstyle);
            buf.append("\" id=\"");
            buf.append(grid.getId());
            buf.append("_");
            buf.append(ds.getKeyProvider() != null ? ds.getKeyProvider().getKey(model) : XDOM
                    .getUniqueId());
            buf.append("\" unselectable=\"");
            buf.append(selectable ? "off" : "on");
            buf.append("\"><table class=x-grid3-row-table role=presentation border=0 cellspacing=0 cellpadding=0 style=\"");

            buf.append(tstyle);
            buf.append("\"><tbody role=presentation><tr role=presentation>");
            widgetList.add(rowIndex, new ArrayList<Widget>());
            for (int i = 0; i < colCount; i++)
            {
                ColumnData c = cs.get(i);
                c.css = c.css == null ? "" : c.css;
                String rv = getRenderedValue(c, rowIndex, i, model, c.name);
                String role = "gridcell";
                if (GXT.isAriaEnabled())
                {
                    ColumnConfig cc = cm.getColumn(i);
                    if (cc.isRowHeader())
                    {
                        role = "rowheader";
                    }
                }

                String attr = c.cellAttr != null ? c.cellAttr : "";
                String cellAttr = c.cellAttr != null ? c.cellAttr : "";

                buf.append("<td id=\"" + XDOM.getUniqueId() + "\" role=\"" + role
                        + "\" class=\"x-grid3-col x-grid3-cell x-grid3-td-");
                buf.append(c.id);
                buf.append(" ");
                buf.append(i == 0 ? "x-grid-cell-first " : (i == last ? "x-grid3-cell-last " : ""));
                if (c.css != null)
                {
                    buf.append(c.css);
                }
                if (super.isShowInvalidCells() && r != null && !r.isValid(c.name))
                {
                    buf.append(" x-grid3-invalid-cell");
                }
                if (super.isShowDirtyCells() && r != null && r.getChanges().containsKey(c.name))
                {
                    buf.append(" x-grid3-dirty-cell");
                } else
                { // rendering of the outdated cells
                    if (model instanceof BaseEntityModel<?>)
                    {
                        BaseEntityModel<?> baseEntityModel = (BaseEntityModel<?>) model;
                        if (baseEntityModel.isOutdated() && baseEntityModel.isOutdatable(c.name))
                        {
                            buf.append(" cisd-grid-outdated-cell");
                        }
                    } // and of the rendering of the outdated cells
                }

                buf.append("\" style=\"");
                buf.append(c.style);
                buf.append("\" ");
                buf.append(cellAttr);
                buf.append("><div unselectable=\"");
                buf.append(selectable ? "off" : "on");
                buf.append("\" class=\"x-grid3-cell-inner x-grid3-col-");
                buf.append(c.id);
                buf.append("\" ");
                buf.append(attr);
                buf.append(">");
                buf.append(rv);
                buf.append("</div></td>");
            }

            buf.append("</tr>");
            if (enableRowBody)
            {
                buf.append("<tr class=x-grid3-row-body-tr style=\"\"><td colspan=");
                buf.append(rowBodyColSpanCount);
                buf.append(" class=x-grid3-body-cell><div class=x-grid3-row-body>${body}</div></td></tr>");
            }
            buf.append("</tbody></table></div>");
        }

        return buf.toString();
    }
}