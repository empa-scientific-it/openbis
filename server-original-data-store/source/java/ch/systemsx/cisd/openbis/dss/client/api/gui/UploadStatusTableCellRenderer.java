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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadTableModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.NewDataSetInfo;

/**
 * Displays a button or download status, depending on whether the file has been downloaded or not.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class UploadStatusTableCellRenderer implements TableCellRenderer
{
    private final JButton uploadButton = new JButton("Upload");

    private final JPanel uploadPanel = new JPanel();

    private final JLabel fixErrorsLabel = new JLabel(
            "<html>Incomplete<br>(see fields in red)</html>");

    private final JPanel fixErrorsPanel = new JPanel();

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    private final JLabel progressLabel = new JLabel();

    private final JPanel progressPanel = new JPanel();

    private final JButton retryButton = new JButton("Retry Upload");

    private final JLabel retryLabel = new JLabel("<html><i>Upload Failed</i></html>");

    private final JPanel retryPanel = new JPanel();

    private final JLabel completedLabel = new JLabel("Registered");

    private final JPanel completedPanel = new JPanel();

    public UploadStatusTableCellRenderer(DataSetUploadTableModel tableModel)
    {
        super();
        createFixErrorsPanel();
        createDownloadPanel();
        createProgressPanel();
        createRetryPanel();
        createCompletedPanel();
    }

    private void createFixErrorsPanel()
    {
        fixErrorsPanel.setLayout(new GridLayout(1, 0));
        fixErrorsLabel.setFont(fixErrorsLabel.getFont().deriveFont(Font.PLAIN));
        fixErrorsLabel.setForeground(Color.RED);
        fixErrorsLabel
                .setToolTipText("Fields marked in red have not been specified correctly. Place the mouse over the field for a description of the error.");
        fixErrorsPanel.add(fixErrorsLabel);
        fixErrorsPanel.setOpaque(true);
    }

    private void createCompletedPanel()
    {
        completedPanel.setLayout(new GridLayout(1, 0));
        completedLabel.setFont(completedLabel.getFont().deriveFont(Font.PLAIN));
        completedPanel.add(completedLabel);
        completedPanel.setOpaque(true);
    }

    private void createRetryPanel()
    {
        retryPanel.setLayout(new GridLayout(2, 0));
        retryLabel.setFont(retryLabel.getFont().deriveFont(Font.PLAIN));
        retryPanel.add(retryLabel);
        retryPanel.add(retryButton);
        retryPanel.setOpaque(true);
    }

    private void createProgressPanel()
    {
        progressPanel.setLayout(new GridLayout(2, 0));
        progressPanel.add(progressBar);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN));
        progressPanel.add(progressLabel);
        progressPanel.setOpaque(true);
    }

    private void createDownloadPanel()
    {
        uploadPanel.setLayout(new GridLayout(1, 0));
        uploadPanel.add(uploadButton);
        uploadPanel.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        final Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();

        final JPanel panel;
        final NewDataSetInfo dataSetInfo = (NewDataSetInfo) value;
        switch (dataSetInfo.getStatus())
        {
            case TO_UPLOAD:
                if (dataSetInfo.hasErrors())
                {
                    panel = fixErrorsPanel;
                } else
                {
                    panel = uploadPanel;
                }
                break;
            case QUEUED_FOR_UPLOAD:
            case UPLOADING:
            	panel = progressPanel;
            	progressBar.setValue(dataSetInfo.getPercentageDownloaded());
            	break;
            case STALLED:
                panel = progressPanel;
                progressBar.setValue(0);
                progressLabel.setText("uploading...");
                break;
            case COMPLETED_UPLOAD:
                panel = completedPanel;
                break;
            case FAILED:
                panel = retryPanel;
                break;
            default:
                throw new RuntimeException("Unknown status ");
        }

        panel.setBackground(backgroundColor);
        return panel;
    }
}
