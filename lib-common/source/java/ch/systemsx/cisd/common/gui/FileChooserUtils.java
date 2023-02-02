/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.gui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import ch.systemsx.cisd.base.utilities.OSUtilities;

/**
 * Utilities for choosing files and directories in the GUI.
 * 
 * @author Bernd Rinn
 */
public class FileChooserUtils
{

    /**
     * Let the user choose a file (<code>chooseDirectories=false</code>) or directory ( <code>chooseDirectories=true</code>). Start the selection
     * process in <var>initialDirectory</var>. The windows will be shown relative to <var>parentFrame</var>.
     * 
     * @return The new file or directory if the user approved the selection or <code>null</code> if the user cancelled the selection.
     */
    public static File tryChooseFile(Frame parentFrame, File initialDirectory,
            boolean chooseDirectories)
    {
        if (OSUtilities.isMacOS())
        {
            if (chooseDirectories)
            {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
            }
            final FileDialog fileChooser = new FileDialog(parentFrame, getTitle(chooseDirectories));
            fileChooser.setModal(true);
            fileChooser.setMode(FileDialog.LOAD);
            fileChooser.setDirectory(initialDirectory.getAbsolutePath());
            fileChooser.setVisible(true);
            final String newParent = fileChooser.getDirectory();
            final String newFile = fileChooser.getFile();
            if (chooseDirectories)
            {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
            if (newFile != null)
            {
                return new File(newParent, newFile);
            } else
            {
                return null;
            }
        } else
        {
            final JFileChooser fileChooser = new JFileChooser(initialDirectory);
            fileChooser.setFileSelectionMode(chooseDirectories ? JFileChooser.DIRECTORIES_ONLY
                    : JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(getTitle(chooseDirectories));
            final int returnVal = fileChooser.showOpenDialog(parentFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                return fileChooser.getSelectedFile();
            } else
            {
                return null;
            }
        }

    }

    /**
     * Let the user choose a file or directory. Start the selection process in <var>initialDirectoryOrNull</var> (defaulted to the home directory if
     * it is null). The windows will be shown relative to <var>parentFrame</var>.
     * 
     * @return The new file or directory if the user approved the selection or <code>null</code> if the user cancelled the selection.
     */
    public static File tryChooseFileOrDirectory(Frame parentFrame, File initialDirectoryOrNull)
    {

        // We can't use the awt file chooser to select files *or* directories (it only allows
        // selection of one or the other).
        File initialDirectory =
                (initialDirectoryOrNull != null) ? initialDirectoryOrNull : new File(
                        System.getProperty("user.home"));

        final JFileChooser fileChooser = new JFileChooser(initialDirectory);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle("Select a directory or file");
        final int returnVal = fileChooser.showOpenDialog(parentFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            return fileChooser.getSelectedFile();
        } else
        {
            return null;
        }

    }

    private static String getTitle(boolean chooseDirectories)
    {
        return "Select a " + (chooseDirectories ? "Directory" : "File");
    }
}
