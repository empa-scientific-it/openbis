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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.SortableFilterableTableModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.table.SortButtonRenderer;
import ch.systemsx.cisd.openbis.dss.client.api.gui.table.TableHeaderMouseListener;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Pawel Glyzewski
 */
public class ExperimentPickerDialog extends AbstractEntityPickerDialog
{
    private static final long serialVersionUID = 6688336042860619854L;

    private static String[] HEADERS = new String[]
    { "Space code", "Project code", "Experiment code", "Experiment identifier" };

    private final JTable table;

    private final JTextField filterField;

    private final JOptionPane optionPane;

    public ExperimentPickerDialog(JFrame mainWindow, DataSetUploadClientModel clientModel)
    {
        super(mainWindow, "Pick an experiment", clientModel);

        table = createTable();
        filterField = createFilterField(table);

        JPanel northPanel = createFilterAndRefreshButtonPanel(filterField, refreshButton);
        optionPane = createOptionPane(northPanel, table, this);
        createTableListener(table, optionPane);
        setDialogData();

        this.setContentPane(optionPane);
    }

    private static JOptionPane createOptionPane(JPanel northPanel, final JTable table,
            final JDialog parent)
    {
        final JScrollPane scrollPane = new JScrollPane(table);

        Object[] objects = new Object[]
        { "Filter experiments: ", northPanel, "Select Experiment:", scrollPane };
        final JOptionPane optionPane =
                new JOptionPane(objects, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                            && evt.getNewValue() != null)
                    {
                        if (((Integer) evt.getNewValue()).intValue() == JOptionPane.OK_OPTION
                                && table.getSelectedRow() == -1)
                        {
                            JOptionPane.showMessageDialog(parent,
                                    "Experiment needs to be selected!", "No experiment selected!",
                                    JOptionPane.WARNING_MESSAGE);
                            optionPane.setValue(optionPane.getInitialValue());
                        } else
                        {
                            parent.setVisible(false);
                        }
                    }
                }
            });
        return optionPane;
    }

    private static List<String[]> prepareData(List<Experiment> experiments)
    {
        List<String[]> data = new ArrayList<String[]>(experiments.size());
        for (Experiment experiment : experiments)
        {
            ExperimentIdentifier expId =
                    ExperimentIdentifierFactory.parse(experiment.getIdentifier());

            data.add(new String[]
            { expId.getSpaceCode(), expId.getProjectCode(), expId.getExperimentCode(),
                    experiment.getIdentifier() });
        }

        return data;
    }

    private static JTable createTable()
    {
        final JTable table = new JTable();
        table.setPreferredScrollableViewportSize(new Dimension(500, 150));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        for (int i = 0; i < table.getColumnModel().getColumnCount() - 1; i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(110);
        }
        header.setDefaultRenderer(new SortButtonRenderer());

        return table;
    }

    private static void createTableListener(final JTable table, final JOptionPane optionPane)
    {
        table.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        if (table.getSelectedRow() != -1)
                        {
                            optionPane.setValue(JOptionPane.OK_OPTION);
                        }
                    }
                }
            });
    }

    private static JTextField createFilterField(final JTable table)
    {
        final JTextField filterField = new JTextField();
        filterField.setEditable(true);
        filterField.getDocument().addDocumentListener(new DocumentListener()
            {
                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    SortableFilterableTableModel model =
                            (SortableFilterableTableModel) table.getModel();
                    model.filter(filterField.getText());
                }

                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    SortableFilterableTableModel model =
                            (SortableFilterableTableModel) table.getModel();
                    model.filter(filterField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    SortableFilterableTableModel model =
                            (SortableFilterableTableModel) table.getModel();
                    model.filter(filterField.getText());
                }
            });

        return filterField;
    }

    @Override
    protected void setDialogData()
    {
        List<Experiment> experiments = clientModel.getExperiments();
        final SortableFilterableTableModel model =
                new SortableFilterableTableModel(prepareData(experiments), HEADERS);
        table.setModel(model);
        table.getTableHeader().addMouseListener(new TableHeaderMouseListener(model));
        model.filter(filterField.getText());
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
    }

    public String pickExperiment()
    {
        this.pack();

        int height = this.getHeight() > 500 ? 500 : this.getHeight();
        int width = this.getWidth() > 600 ? 600 : this.getWidth();
        this.setSize(width, height);

        Point mwLocation = mainWindow.getLocationOnScreen();
        int x = mwLocation.x + (mainWindow.getWidth() / 2) - (this.getWidth() / 2);
        int y = mwLocation.y + (mainWindow.getHeight() / 2) - (this.getHeight() / 2);

        this.setLocation(x > 0 ? x : 0, y > 0 ? y : 0);

        this.setVisible(true);

        Object value = optionPane.getValue();
        optionPane.setValue(optionPane.getInitialValue());
        if (value == null || ((Integer) value).intValue() == JOptionPane.CANCEL_OPTION)
        {
            return null;
        } else
        {
            return table.getValueAt(table.getSelectedRow(), 3).toString();
        }
    }
}
