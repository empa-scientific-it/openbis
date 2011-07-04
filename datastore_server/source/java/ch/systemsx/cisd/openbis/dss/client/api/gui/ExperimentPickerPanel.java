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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * @author Pawel Glyzewski
 */
public class ExperimentPickerPanel extends JPanel
{
    private static final long serialVersionUID = -8093481985680332715L;

    private final JTextField textField = new JTextField();

    private final JButton button = new JButton("...");

    private final ExperimentPickerDialog dialog;

    public ExperimentPickerPanel(final JFrame mainWindow, final List<Experiment> experiments)
    {
        super(new BorderLayout());

        dialog = new ExperimentPickerDialog(mainWindow, experiments);

        button.setMargin(new Insets(button.getMargin().top, 2, button.getMargin().bottom, 2));

        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    String experimentId = dialog.pickExperiment();
                    if (experimentId != null)
                    {
                        textField.setText(experimentId);
                    }
                }
            });

        add(textField, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
    }

    public String getText()
    {
        return textField.getText();
    }

    public void setText(String text)
    {
        textField.setText(text);
    }

    public void addActionListener(ActionListener actionListener)
    {
        textField.addActionListener(actionListener);
    }

    @Override
    public void addFocusListener(FocusListener focusListener)
    {
        textField.addFocusListener(focusListener);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        textField.setEditable(enabled);
        textField.setEnabled(enabled);
        button.setEnabled(enabled);
    }
}
