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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;

/**
 * @author Chandrasekhar Ramakrishnan
 * @author Kaloyan Enimanev
 */
public abstract class AbstractEntityPickerDialog extends JDialog
{

    private static final long serialVersionUID = 1L;

    /**
     * do not spawn one thread for each subclass.
     */
    protected static final Timer scheduler = new Timer();

    protected final JFrame mainWindow;

    protected final JButton refreshButton;

    protected final DataSetUploadClientModel clientModel;

    /**
     * @param mainWindow The parent window of thie dialog
     * @param title The title of the window
     * @param clientModel the client model used for connecting to the openBIS server and for caching.
     */
    public AbstractEntityPickerDialog(JFrame mainWindow, String title,
            DataSetUploadClientModel clientModel)
    {
        super(mainWindow, title, true);
        this.mainWindow = mainWindow;

        this.clientModel = clientModel;

        this.refreshButton = createRefreshButton();
    }

    /**
     * populates the dialog based on content cached in {@link #clientModel}.
     */
    protected abstract void setDialogData();

    protected JButton createRefreshButton()
    {
        final JButton button = new JButton();
        button.setText("Refresh");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    button.setEnabled(false);
                    refresh();
                }
            });
        return button;
    }

    protected void refresh()
    {
        new Thread()
            {
                @Override
                public void run()
                {
                    // reload data
                    clientModel.reloadDataFromServer();

                    // update UI
                    SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                setDialogData();
                                refreshButton.setEnabled(true);
                            }
                        });
                }
            }.start();
    }

    protected static JPanel createFilterAndRefreshButtonPanel(JTextField textField,
            JButton refreshButton)
    {
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setMaximumSize(new Dimension(9999, 30));

        refreshButton.setMargin(new Insets(refreshButton.getMargin().top, 2, refreshButton
                .getMargin().bottom, 2));

        innerPanel.add(textField, BorderLayout.CENTER);
        innerPanel.add(refreshButton, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.add(innerPanel, gbc);

        return outerPanel;
    }

}
