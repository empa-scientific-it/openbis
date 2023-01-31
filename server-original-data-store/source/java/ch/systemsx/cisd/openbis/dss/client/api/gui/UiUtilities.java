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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.Identifier;
import ch.systemsx.cisd.openbis.dss.client.api.gui.tree.FilterableMutableTreeNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class UiUtilities
{

    public static void displayError(JLabel label, JComponent component,
            ErrorsPanel errorAreaOrNull, ValidationError error)
    {
        component.setToolTipText(error.getErrorMessage());
        label.setForeground(Color.RED);
        if (errorAreaOrNull != null)
        {
            errorAreaOrNull.reportError(error);
        }
    }

    public static void clearError(JLabel label, JComponent component, ErrorsPanel errorAreaOrNull)
    {
        component.setToolTipText(null);
        label.setForeground(Color.BLACK);
        if (errorAreaOrNull != null)
        {
            errorAreaOrNull.clear();
        }
    }

    static final String ROOT_SAMPLES = "Samples";
    static final String ROOT_EXPERIMENTS = "Experiments";
    static final String DATA_SETS = "Data Sets";
    private static final String WAITING_NODE_LABEL = "Loading data ...";
    
    private static final String[] SPECIAL_NODES = {ROOT_EXPERIMENTS, ROOT_SAMPLES, DATA_SETS, WAITING_NODE_LABEL};
    
    public static boolean isMatchingNode(Object node, Pattern pattern)
    {
        for (String specialNode : SPECIAL_NODES)
        {
            if (equals(specialNode, node))
            {
                return true;
            }
        }
        if (node instanceof FilterableMutableTreeNode)
        {
            Object userObject = ((FilterableMutableTreeNode) node).getUserObject();
            if (userObject instanceof Identifier)
            {
                if (((Identifier) userObject).getOwnerType() == DataSetOwnerType.DATA_SET)
                {
                    return true;
                }
            }
        }
        return pattern.matcher(node.toString()).find();
    }
    
    private static boolean equals(String label, Object object)
    {
        return object.toString().equals(label);
    }
    
    public static FilterableMutableTreeNode createWaitingNode()
    {
        return new FilterableMutableTreeNode(UiUtilities.WAITING_NODE_LABEL);
    }
    
    public static void showException(Component parentComponent, final Throwable throwable)
    {
        showMessageAndException(parentComponent, throwable, throwable.toString(), "Error");
    }

    public static void showMessageAndException(Component parentComponent, final Throwable throwable, String message, String title)
    {
        final JPanel panel = new JPanel(new BorderLayout(20, 5));
        panel.add(new JLabel(message), BorderLayout.WEST);
        JButton button = new JButton("Show Details");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    panel.removeAll();
                    JTextArea textArea = new JTextArea(20, 20);
                    textArea.setEditable(false);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    throwable.printStackTrace(new PrintStream(byteArrayOutputStream, true));
                    textArea.setText(byteArrayOutputStream.toString());
                    panel.add(textArea, BorderLayout.CENTER);
                    panel.validate();
                    SwingUtilities.getWindowAncestor(panel).pack();
                }
            });
        panel.add(button, BorderLayout.EAST);
        JOptionPane.showMessageDialog(parentComponent, panel, title, JOptionPane.ERROR_MESSAGE);
    }

}
