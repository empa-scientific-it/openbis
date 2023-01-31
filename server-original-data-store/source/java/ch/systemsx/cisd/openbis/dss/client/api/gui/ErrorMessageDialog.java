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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Pawel Glyzewski
 */
public class ErrorMessageDialog extends JDialog
{

    private static final long serialVersionUID = 2915724575615063627L;

    private final JTextArea errorArea = new JTextArea();

    private final JFrame mainWindow;

    public JPopupMenu popup;

    // An inner class to check whether mouse events are the popup trigger
    class MousePopupListener extends MouseAdapter
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            checkPopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            checkPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(ErrorMessageDialog.this.errorArea, e.getX(), e.getY());
            }
        }
    }

    public ErrorMessageDialog(JFrame mainWindow)
    {
        super(mainWindow, true);

        this.mainWindow = mainWindow;
        JScrollPane scrollPane = new JScrollPane(this.errorArea);
        Object[] objects =
        { "Validation error occured: ", scrollPane };

        final JOptionPane optionPane =
                new JOptionPane(objects, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION,
                        null);

        this.setContentPane(optionPane);
        this.errorArea.setEditable(false);

        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Copy");
        popup.add(menuItem);
        menuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    errorArea.copy();
                }
            });

        optionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {

                    if (evt.getPropertyName() != null
                            && evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))
                    {

                        if (evt.getNewValue() != null)
                        {
                            if (((Integer) evt.getNewValue()) == JOptionPane.OK_OPTION)
                            {
                                optionPane.setValue(optionPane.getInitialValue());

                                ErrorMessageDialog.this.dispose();
                            }
                        }
                    }
                }

            });
        errorArea.addMouseListener(new MousePopupListener());
    }

    public void showErrorMessage(String errorMessage)
    {
        errorArea.setText(errorMessage);
        this.pack();

        int height = this.getHeight() > 500 ? 500 : this.getHeight();
        int width = this.getWidth() > 600 ? 600 : this.getWidth();
        this.setSize(width, height);

        Point mwLocation = mainWindow.getLocationOnScreen();
        int x = mwLocation.x + (mainWindow.getWidth() / 2) - (this.getWidth() / 2);
        int y = mwLocation.y + (mainWindow.getHeight() / 2) - (this.getHeight() / 2);

        this.setLocation(x > 0 ? x : 0, y > 0 ? y : 0);

        this.setVisible(true);
    }
}
