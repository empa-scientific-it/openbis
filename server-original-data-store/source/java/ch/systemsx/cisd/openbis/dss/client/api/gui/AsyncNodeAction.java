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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.IAsyncAction;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AsyncNodeAction<T> implements IAsyncAction<T>
{
    private final JTree tree;

    private final Timer scheduler;

    private final DefaultMutableTreeNode node;

    public AsyncNodeAction(JTree tree, DefaultMutableTreeNode node, Timer scheduler)
    {
        this.tree = tree;
        this.node = node;
        this.scheduler = scheduler;
    }

    @Override
    public void performAction(final T data)
    {
        EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handleData(data);
                    refreshTreeNode();
                }
            });
    }

    @Override
    public void handleException(final Throwable throwable)
    {
        UiUtilities.showException(tree, throwable);
    }

    public abstract void handleData(T data);

    private void refreshTreeNode()
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeStructureChanged(node);

        if (node.getChildCount() == 0)
        {
            scheduler.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        tree.collapsePath(new TreePath(node.getPath()));
                    }
                }, 1500l);
        }
    }

}
