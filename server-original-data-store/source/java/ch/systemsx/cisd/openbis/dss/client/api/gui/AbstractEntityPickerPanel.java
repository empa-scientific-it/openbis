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
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractEntityPickerPanel extends JPanel implements ActionListener
{

    private static final long serialVersionUID = 1L;

    static class JTextFieldFireActionPerformedExposed extends JTextField
    {
        private static final long serialVersionUID = -7656053161479466883L;

        @Override
        public void fireActionPerformed()
        {
            super.fireActionPerformed();
        }
    }

    protected final JTextFieldFireActionPerformedExposed textField =
            new JTextFieldFireActionPerformedExposed();

    private final JButton button = new JButton("...");

    public AbstractEntityPickerPanel(final JFrame mainWindow)
    {
        super(new BorderLayout());

        button.setMargin(new Insets(button.getMargin().top, 2, button.getMargin().bottom, 2));
        button.addActionListener(this);
        button.setToolTipText(getButtonToolTipText());

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
        button.addFocusListener(focusListener);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        textField.setEditable(enabled);
        textField.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(String tooltip)
    {
        textField.setToolTipText(tooltip);
    }

    protected abstract String getButtonToolTipText();
}
