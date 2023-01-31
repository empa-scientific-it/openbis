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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.IDatasetImageOverviewPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.ResponseContentStream;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageResolutionKind;

/**
 * Demo {@link IDatasetImageOverviewPlugin} implementation showing always the same images dependent on resolution.
 * 
 * @author Piotr Buczek
 */
public class DemoOverviewPlugin implements IDatasetImageOverviewPlugin
{
    private static final String LABEL_PROPERTY_KEY = "label";

    private static final String SMALL_IMAGE_FILE = "resource/overview-small.png";

    private static final String NORMAL_IMAGE_FILE = "resource/overview-normal.png";

    private final String label;

    public DemoOverviewPlugin(Properties properties)
    {
        label = PropertyUtils.getProperty(properties, LABEL_PROPERTY_KEY, "(no label)");
    }

    @Override
    public ResponseContentStream createImageOverview(String datasetCode, String datasetTypeCode,
            IHierarchicalContent datasetRoot, ImageResolutionKind resolution)
    {
        System.out.println(String.format("%s (%s): create image for\n"
                + "\tdataset code: %s\n\tdataset type: %s\n\tresolution: %s", this.getClass()
                .getSimpleName(), label, datasetCode, datasetTypeCode, resolution));
        String imageFilePath;
        switch (resolution)
        {
            case SMALL:
                imageFilePath = SMALL_IMAGE_FILE;
                break;
            case NORMAL:
                imageFilePath = NORMAL_IMAGE_FILE;
                break;
            default:
                throw new UnsupportedOperationException(resolution + " resolution is not supported");
        }
        try
        {
            BufferedImage image =
                    createImage(imageFilePath, datasetCode, datasetTypeCode, resolution);
            return ResponseContentStream.createPNG(image, imageFilePath);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private BufferedImage createImage(String imageFilePath, String datasetCode, String datasetType,
            ImageResolutionKind resolution) throws IOException
    {
        BufferedImage image = ImageIO.read(new File(imageFilePath));
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD));
        switch (resolution)
        {
            case SMALL:
                graphics.drawString("" + datasetCode.charAt(datasetCode.length() - 1), 11, 21);
                break;
            case NORMAL:
                graphics.drawString(this.getClass().getSimpleName(), 10, 20);
                graphics.drawString("Plugin Label: " + label, 10, 40);
                graphics.drawString("Data Set Code: " + datasetCode, 10, 60);
                graphics.drawString("Data Set Type: " + datasetType, 10, 80);
                break;
        }
        return image;
    }

}
