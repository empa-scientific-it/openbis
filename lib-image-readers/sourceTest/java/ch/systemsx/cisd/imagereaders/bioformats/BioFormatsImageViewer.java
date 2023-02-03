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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ReadParams;

/**
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("deprecation")
public class BioFormatsImageViewer
{
    public static void main(String[] args)
    {
        String[] myArgs = args;
        if (myArgs.length == 0)
        {
            FileDialog fileDialog = new FileDialog((Frame) null);
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.setVisible(true);
            String dir = fileDialog.getDirectory();
            myArgs = new String[] { dir + "/" + fileDialog.getFile() };
        }
        for (String fileName : myArgs)
        {
            IImageReader reader = new BioFormatsReaderLibrary().tryGetReaderForFile(fileName);

            System.out.println("=========== File: " + fileName);
            System.out.println("Reader: " + reader);
            System.out.println("Library: " + reader.getLibraryName());
            System.out.println("Name: " + reader.getName());
            File file = new File(fileName);
            ReadParams readParams = new ReadParams();
            readParams.setIntensityRescalingChannel(0);
            List<ImageID> imageIDs = reader.getImageIDs(file);
            List<BufferedImage> images = new ArrayList<BufferedImage>();
            for (ImageID imageID : imageIDs)
            {
                System.out.println(imageID);
                BufferedImage image = reader.readImage(file, imageID, readParams);
                images.add(image);
            }
            showImages(images, fileName);
        }
    }

    private static void showImages(List<BufferedImage> images, String fileName)
    {
        final JFrame frame = new JFrame("Images: " + fileName);
        Container contentPane = frame.getContentPane();
        JPanel mainPanel = new JPanel(new BorderLayout());
        contentPane.add(mainPanel);
        int size = images.size();
        if (size > 1)
        {
            JTabbedPane tabbedPane = new JTabbedPane();
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
            for (int i = 0; i < size; i++)
            {
                BufferedImage image = images.get(i);
                String tabName = Integer.toString(i);
                tabbedPane.addTab(tabName, createImagePanel(image));
            }
        } else
        {
            mainPanel.add(createImagePanel(images.get(0)), BorderLayout.CENTER);
        }
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }

    private static Component createImagePanel(BufferedImage image)
    {
        return new JScrollPane(new JLabel(new ImageIcon(image)));
    }
}
