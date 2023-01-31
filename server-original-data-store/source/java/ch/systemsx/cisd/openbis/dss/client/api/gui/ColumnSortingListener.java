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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadTableModel;

/**
 * Internal class for triggering the sorting of columns.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ColumnSortingListener extends MouseAdapter
{
    private final DataSetUploadTableModel model;

    private final JTable table;

    public ColumnSortingListener(DataSetUploadTableModel model, JTable table)
    {
        this.model = model;
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        TableColumnModel colModel = table.getColumnModel();

        int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

        if (modelIndex < 0)
        {
            return;
        } else if (modelIndex == DataSetUploadTableModel.DATA_SET_METADATA_COLUMN)
        {
            // Don't offer sorting on metadata -- it doesn't make sense.
            return;
        } else if (model.getSortColumnIndex() == modelIndex)
        {
            model.setSortAscending(model.isSortAscending() == false);
        } else
        {
            model.setSortColumnIndex(modelIndex);
            model.setSortAscending(true);
        }

        for (int i = 0; i < colModel.getColumnCount(); i++)
        {
            TableColumn column = colModel.getColumn(i);
            column.setHeaderValue(table.getModel().getColumnName(column.getModelIndex()));
        }

        table.getTableHeader().repaint();

        model.syncNewDataSetInfoListView();
    }
}