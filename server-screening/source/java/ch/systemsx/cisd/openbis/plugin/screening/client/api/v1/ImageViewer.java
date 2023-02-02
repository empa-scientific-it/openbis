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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DataSetCodeAndWellPositions;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * @author Franz-Josef Elmer
 */
public class ImageViewer
{
    public static void main(String[] args) throws Exception
    {
        String serviceURL = args[0];
        String sessionToken = args[1];
        String channel = args[2];
        Map<String, DataSetCodeAndWellPositions> dataSets =
                new HashMap<String, DataSetCodeAndWellPositions>();
        for (int i = 3; i < args.length; i++)
        {
            DataSetCodeAndWellPositions dw = new DataSetCodeAndWellPositions(args[i]);
            dataSets.put(dw.getDataSetCode(), dw);
        }

        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        contentPane.add(content);
        ImageSize thumbnailSize = new ImageSize(100, 60);
        try
        {
            IScreeningOpenbisServiceFacade facade =
                    ScreeningOpenbisServiceFacadeFactory.INSTANCE.tryToCreate(sessionToken,
                            serviceURL);
            List<IDatasetIdentifier> dsIdentifiers =
                    facade.getDatasetIdentifiers(new ArrayList<String>(dataSets.keySet()));
            for (IDatasetIdentifier identifier : dsIdentifiers)
            {
                content.add(new JLabel("Images for data set " + identifier.getDatasetCode()));
                JPanel imagePanel = new JPanel();
                imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
                content.add(imagePanel);
                List<WellPosition> wellPositions =
                        dataSets.get(identifier.getDatasetCode()).getWellPositions();
                List<byte[]> imageBytes =
                        facade.loadImages(identifier, wellPositions, channel, thumbnailSize);
                for (byte[] bytes : imageBytes)
                {
                    imagePanel.add(new JLabel(new ImageIcon(bytes)));
                }
            }
            String pattern = channel.equals("Merged Channels") ? "brg" : "gbr";
            facade.saveImageTransformerFactory(dsIdentifiers, channel, new ExampleImageTransformerFactory(pattern));
            IImageTransformerFactory factory = facade.getImageTransformerFactoryOrNull(dsIdentifiers, channel);
            System.out.println("Image Transformer Factory: " + factory);
        } catch (Exception ex)
        {
            content.add(new JLabel(ex.toString()));
            throw ex;
        }

        frame.pack();
        frame.setVisible(true);
    }

}
