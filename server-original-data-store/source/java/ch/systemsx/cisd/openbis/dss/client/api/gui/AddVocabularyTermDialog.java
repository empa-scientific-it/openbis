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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.dss.client.api.gui.VocabularyTermsComboBoxPanel.VocabularyTermAdaptor;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * @author Pawel Glyzewski
 */
public class AddVocabularyTermDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private JPanel gridPanel;

    private final JTextField codeField = new JTextField();

    private final JTextField labelField = new JTextField();

    private JTextArea descriptionField = new JTextArea();

    private JComboBox vocabularyTermsField = new JComboBox();

    private final Vocabulary vocabulary;

    private final DataSetUploadClientModel clientModel;

    public AddVocabularyTermDialog(JFrame mainWindow, ComboBoxModel comboBoxModel,
            Vocabulary vocabulary, DataSetUploadClientModel clientModel)
    {
        super(mainWindow, "Add Ad Hoc vocabulary term", true);

        this.vocabulary = vocabulary;
        this.clientModel = clientModel;
        mainPanel = new JPanel(new BorderLayout());

        this.gridPanel = createMainPanel(comboBoxModel);
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        mainPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

        this.setContentPane(mainPanel);

        this.setSize(500, 300);

        Point mwLocation = mainWindow.getLocationOnScreen();
        int x = mwLocation.x + (mainWindow.getWidth() / 2) - (this.getWidth() / 2);
        int y = mwLocation.y + (mainWindow.getHeight() / 2) - (this.getHeight() / 2);

        this.setLocation(x > 0 ? x : 0, y > 0 ? y : 0);
    }

    private JPanel createButtonsPanel()
    {
        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    clientModel.addUnofficialVocabularyTerm(vocabulary, codeField.getText(),
                            labelField.getText().trim(), descriptionField.getText(),
                            extractPreviousTermOrdinal());
                    AddVocabularyTermDialog.this.dispose();
                }
            });
        buttonsPanel.add(addButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AddVocabularyTermDialog.this.dispose();
                }
            });
        buttonsPanel.add(cancelButton);

        return buttonsPanel;
    }

    private Long extractPreviousTermOrdinal()
    {
        // - 0 if nothing is selected (move to the beginning),
        // - (otherwise) selected term's ordinal
        VocabularyTermAdaptor selectedItem =
                (VocabularyTermAdaptor) vocabularyTermsField.getSelectedItem();
        return selectedItem != null ? selectedItem.getOrdinal() : 0;
    }

    private JPanel createMainPanel(ComboBoxModel comboBoxModel)
    {
        JPanel panel = new JPanel(new GridBagLayout());

        codeField.setEnabled(false);
        labelField.requestFocus();
        labelField.addKeyListener(new KeyListener()
            {
                @Override
                public void keyPressed(KeyEvent arg0)
                {
                    handleEvent();
                }

                @Override
                public void keyReleased(KeyEvent arg0)
                {
                    handleEvent();
                }

                @Override
                public void keyTyped(KeyEvent arg0)
                {
                    handleEvent();
                }

                private void handleEvent()
                {
                    codeField.setText(CodeNormalizer.normalize(labelField.getText()));
                }
            });
        for (int i = 0; i < comboBoxModel.getSize(); i++)
        {
            vocabularyTermsField.addItem(comboBoxModel.getElementAt(i));
        }
        selectMaxOrdinal(vocabularyTermsField);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Code:"), c);

        c.gridy = 0;
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(codeField, c);

        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Label:"), c);

        c.gridy = 1;
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(labelField, c);

        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Description:"), c);

        c.gridy = 2;
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(new JScrollPane(descriptionField), c);

        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Position after:"), c);

        c.gridy = 3;
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(vocabularyTermsField, c);

        return panel;
    }

    private static void selectMaxOrdinal(JComboBox comboBox)
    {
        ComboBoxModel model = comboBox.getModel();
        long maxOrdinal = Long.MIN_VALUE;
        int maxItemIndex = -1;
        for (int i = 0; i < model.getSize(); i++)
        {
            Long ordinal = ((VocabularyTermAdaptor) model.getElementAt(i)).getOrdinal();
            if (maxOrdinal < ordinal)
            {
                maxOrdinal = ordinal;
                maxItemIndex = i;
            }
        }
        if (maxItemIndex > -1)
        {
            comboBox.setSelectedIndex(maxItemIndex);
        }
    }

}
