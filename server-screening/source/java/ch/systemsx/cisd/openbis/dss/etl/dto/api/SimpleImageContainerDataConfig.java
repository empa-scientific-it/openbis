/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.util.List;

/**
 * Subclass of {@link SimpleImageDataConfig} for container image files which creates {@link ImageMetadata} based on {@link ImageIdentifier} using the
 * following simple mapping:
 * <ul>
 * <li>ImageMetadata.seriesNumber = ImageIdentifier.seriesIndex + 1
 * <li>ImageMetadata.timePoint = ImageIdentifier.timeSeriesIndex
 * <li>ImageMetadata.depth = ImageIdentifier.focalPlaneIndex
 * <li>ImageMetadata.channelCode = 'CHANNEL-' + (ImageIdentifier.colorChannelIndex + 1)
 * <li>ImageMetadata.tileNumber = 1
 * </ul>
 * ImageMetadata.well will be filled with return value of method {@link #tryToExtractWell(ImageIdentifier)}.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleImageContainerDataConfig extends SimpleImageDataConfig
{

    @Override
    public ImageMetadata[] extractImagesMetadata(String imagePath,
            List<ImageIdentifier> imageIdentifiers)
    {
        ImageMetadata[] metaData = new ImageMetadata[imageIdentifiers.size()];
        for (int i = 0, n = imageIdentifiers.size(); i < n; i++)
        {
            ImageIdentifier imageIdentifier = imageIdentifiers.get(i);
            ImageMetadata imageMetadata = new ImageMetadata();
            imageMetadata.setImageIdentifier(imageIdentifier);
            imageMetadata.setSeriesNumber(imageIdentifier.getSeriesIndex() + 1);
            imageMetadata.setTimepoint(Float.valueOf(imageIdentifier.getTimeSeriesIndex()));
            imageMetadata.setDepth(Float.valueOf(imageIdentifier.getFocalPlaneIndex()));
            imageMetadata.setChannelCode("CHANNEL-" + (imageIdentifier.getColorChannelIndex() + 1));
            imageMetadata.setWell(tryToExtractWell(imageIdentifier));
            imageMetadata.setTileNumber(1);
            metaData[i] = imageMetadata;
        }
        return metaData;
    }

    /**
     * Tries to extract well from the specified image identifier. Default implementation returns <code>null</code>. In case of screening this method
     * can be overridden.
     */
    public String tryToExtractWell(ImageIdentifier imageIdentifier)
    {
        return null;
    }

}
