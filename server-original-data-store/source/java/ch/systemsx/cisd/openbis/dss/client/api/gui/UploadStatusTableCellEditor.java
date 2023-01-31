/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadTableModel;

/**
 * Creates a clickable button which allows the user to "edit" the status.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class UploadStatusTableCellEditor extends AbstractCellEditor implements TableCellEditor
{

    private static final long serialVersionUID = 1L;

    final JButton button = new JButton("Upload");

    final DataSetUploadTableModel tableModel;

    UploadStatusTableCellEditor(DataSetUploadTableModel tableModel)
    {
        super();
        this.tableModel = tableModel;

        button.setOpaque(true);
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    fireEditingStopped();
                }
            });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column)
    {
        if (isSelected)
        {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else
        {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        return button;
    }

    @Override
    public Object getCellEditorValue()
    {
        return tableModel.getSelectedNewDataSetOrNull();
    }
}
