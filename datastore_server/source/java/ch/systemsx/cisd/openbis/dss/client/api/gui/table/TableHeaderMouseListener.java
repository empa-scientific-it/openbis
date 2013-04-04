/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.table;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.SortableFilterableTableModel;

/**
 * @author Pawel Glyzewski
 */
public class TableHeaderMouseListener extends MouseAdapter
{
    private SortableFilterableTableModel model;

    public TableHeaderMouseListener(SortableFilterableTableModel model)
    {
        this.model = model;
    }

    @Override
    public void mouseClicked(MouseEvent evt)
    {
        JTable table = ((JTableHeader) evt.getSource()).getTable();
        TableColumnModel colModel = table.getColumnModel();

        // The index of the column whose header was clicked
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());
        int mColIndex = table.convertColumnIndexToModel(vColIndex);

        // Return if not clicked on any column header
        if (vColIndex == -1)
        {
            return;
        }

        // Determine if mouse was clicked between column heads
        Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
        if (vColIndex == 0)
        {
            headerRect.width -= 3; // Hard-coded constant
        } else
        {
            headerRect.grow(-3, 0); // Hard-coded constant
        }
        if (headerRect.contains(evt.getX(), evt.getY()))
        {
            model.toggleSort(mColIndex);
        }
    }
}
