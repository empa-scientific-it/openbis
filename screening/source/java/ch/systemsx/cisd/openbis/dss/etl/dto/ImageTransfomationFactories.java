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

package ch.systemsx.cisd.openbis.dss.etl.dto;

import java.util.Map;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * Stores transformations defined for the image on different levels.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransfomationFactories
{
    // transformation with a specified code can be applied when a single channel is displayed
    private Map<String/* transformation code */, IImageTransformerFactory> singleChannelMap;

    // applied when all channels of the image are merged
    private IImageTransformerFactory mergedChannels;

    // individual transformation for the image
    private IImageTransformerFactory image;

    public IImageTransformerFactory tryGetForChannel(String transformationCodeOrNull)
    {
        if (transformationCodeOrNull == null)
        {
            return null;
        }
        return singleChannelMap.get(transformationCodeOrNull);
    }

    public IImageTransformerFactory tryGetForMerged()
    {
        return mergedChannels;
    }

    public void setForChannel(
            Map<String/* transformation code */, IImageTransformerFactory> singleChannelMap)
    {
        this.singleChannelMap = singleChannelMap;
    }

    public void setForMergedChannels(IImageTransformerFactory transformerFactoryForMergedChannels)
    {
        this.mergedChannels = transformerFactoryForMergedChannels;
    }

    public IImageTransformerFactory tryGetForImage()
    {
        return image;
    }

    public void setForImage(IImageTransformerFactory transformerFactoryForImage)
    {
        this.image = transformerFactoryForImage;
    }

}
