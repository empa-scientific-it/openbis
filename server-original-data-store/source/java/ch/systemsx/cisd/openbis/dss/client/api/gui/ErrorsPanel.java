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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * @author Pawel Glyzewski
 */
public class ErrorsPanel extends JPanel
{
    private static final long serialVersionUID = 5932698456919803620L;

    private static final String VALIDATION_ERRORS_CARD = "Errors";

    private static final String VALIDATION_WAIT_CARD = "Wait";

    private static final String VALIDATION_SUCCESS_CARD = "Success";

    private static final ImageIcon OK_ICON =
            new ImageIcon(ErrorsPanel.class.getResource("/ok.png"));

    private static final ImageIcon WRONG_ICON =
            new ImageIcon(ErrorsPanel.class.getResource("/wrong.png"));

    private static final ImageIcon WAIT_ICON = new ImageIcon(
            ErrorsPanel.class.getResource("/wait.gif"));

    private final Set<String> errors = new LinkedHashSet<String>();

    private final ErrorMessageDialog errorMessageDialog;

    private final JScrollPane errorsAreaScroll;

    private final JPanel errorsPanel;

    public ErrorsPanel(JFrame mainWindow)
    {
        super(new CardLayout());

        errorMessageDialog = new ErrorMessageDialog(mainWindow);

        errorsPanel = new JPanel();
        errorsPanel.setLayout(new BoxLayout(errorsPanel, BoxLayout.Y_AXIS));
        errorsAreaScroll = new JScrollPane(errorsPanel);
        errorsAreaScroll.setBorder(BorderFactory.createEmptyBorder());
        errorsAreaScroll
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(errorsAreaScroll, VALIDATION_ERRORS_CARD);
        this.add(new JLabel("Validating, please wait...", WAIT_ICON, SwingConstants.LEFT),
                VALIDATION_WAIT_CARD);
        this.add(new JLabel("Validated successfully.", OK_ICON, SwingConstants.LEFT),
                VALIDATION_SUCCESS_CARD);
    }

    public void clear()
    {
        this.errors.clear();
        errorsPanel.removeAll();
        errorsPanel.invalidate();
        errorsPanel.getParent().validate();
    }

    public void reportError(ValidationError error)
    {
        errors.add(error.getErrorMessage());
        displayErrors();
    }

    private void displayErrors()
    {
        if (errors.isEmpty())
        {
            return;
        }
        errorsPanel.removeAll();
        for (final String error : errors)
        {
            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            String truncateErrorMessage = truncateErrorMessage(error);
            JLabel label = new JLabel(truncateErrorMessage, WRONG_ICON, SwingConstants.LEFT);
            line.add(label);
            if (truncateErrorMessage.length() < error.length())
            {
                JButton button = new JButton("...");
                button.setPreferredSize(new Dimension(25, label.getPreferredSize().height));
                button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            errorMessageDialog.showErrorMessage(error);
                        }
                    });
                line.add(button);
            }
            errorsPanel.add(line);
        }
        errorsPanel.invalidate();
        errorsPanel.getParent().validate();
    }

    private String truncateErrorMessage(String errorMessage)
    {
        int truncIndex = 80;

        int indexOfCR = errorMessage.indexOf('\r');
        if (indexOfCR > -1 && indexOfCR < truncIndex)
        {
            truncIndex = indexOfCR;
        }

        int indexOfLF = errorMessage.indexOf('\n');
        if (indexOfLF > -1 && indexOfLF < truncIndex)
        {
            truncIndex = indexOfLF;
        }

        return errorMessage.length() > truncIndex ? errorMessage.substring(0, truncIndex)
                : errorMessage;
    }

    public void waitCard()
    {
        ((CardLayout) this.getLayout()).show(this, VALIDATION_WAIT_CARD);
    }

    public void showResult()
    {
        if (errors.isEmpty())
        {
            ((CardLayout) this.getLayout()).show(this, VALIDATION_SUCCESS_CARD);
        } else
        {
            ((CardLayout) this.getLayout()).show(this, VALIDATION_ERRORS_CARD);
        }
    }
}
