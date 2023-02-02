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

package ch.systemsx.cisd.openbis.dss.etl;

import java.awt.image.BufferedImage;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Reference to the image with an absolute path.
 * 
 * @author Tomasz Pylak
 */
// TODO 2010-12-23, Tomasz Pylak: rename to ImageContent
public class AbsoluteImageReference extends AbstractImageReference
{
    private final IHierarchicalContentNode contentNode;

    private final String uniqueId;

    private final RequestedImageSize imageSize;

    private final ImageTransfomationFactories imageTransformationFactories;

    private final ImageLibraryInfo imageLibraryOrNull;

    private BufferedImage image;

    private ChannelColorRGB channelColor;

    private String singleChannelTransformationCodeOrNull;

    private final String channelCodeOrNull;

    /**
     * @param contentNode is the original content before choosing the color component and the image ID
     */
    public AbsoluteImageReference(IHierarchicalContentNode contentNode, String uniqueId,
            String imageIdOrNull, ColorComponent colorComponentOrNull,
            RequestedImageSize imageSize, ChannelColorRGB channelColor,
            ImageTransfomationFactories imageTransfomationFactories,
            ImageLibraryInfo imageLibraryOrNull, String singleChannelTransformationCodeOrNull,
            String channelCodeOrNull)
    {
        super(imageIdOrNull, colorComponentOrNull);
        assert imageSize != null : "image size is null";
        assert imageTransfomationFactories != null : "imageTransfomationFactories is null";

        this.contentNode = contentNode;
        this.uniqueId = uniqueId;
        this.imageSize = imageSize;
        this.channelColor = channelColor;
        this.imageTransformationFactories = imageTransfomationFactories;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
        this.channelCodeOrNull = channelCodeOrNull;
    }

    /**
     * Returns id of the content which uniquely identifies the source of it and distinguishes from other sources. Example: for a file-system-based
     * content the absolute path is the correct id.
     */
    public String getUniqueId()
    {
        return uniqueId;
    }

    /**
     * @return unchanged image content to allow the fastest possible access to any stored image.
     */
    public IHierarchicalContentNode tryGetRawContent()
    {
        if (tryGetColorComponent() == null && tryGetImageID() == null)
        {
            return contentNode;
        }
        return null;
    }

    /**
     * @return unchanged image content if the image does not have to be extracted from the original content. This method is provided to allow the
     *         fastest possible access to original images.
     */
    public IHierarchicalContentNode tryGetRawContentForOriginalImage()
    {
        if (tryGetColorComponent() == null && tryGetImageID() == null
                && getRequestedSize().isThumbnailRequired() == false)
        {
            return contentNode;
        } else
        {
            return null;
        }
    }

    public BufferedImage getUnchangedImage()
    {
        if (image == null)
        {
            image = Utils.loadUnchangedImage(contentNode, tryGetImageID(), imageLibraryOrNull);
        }
        return image;
    }

    /**
     * Returns the image size. Preferred method if only image size is needed because only the header of an image file might be read to get the size.
     */
    public Size getUnchangedImageSize()
    {
        if (image != null)
        {
            return new Size(image.getWidth(), image.getHeight());
        }
        return Utils.loadUnchangedImageSize(contentNode, tryGetImageID(), imageLibraryOrNull);
    }

    public Integer getColorDepth()
    {
        if (image != null)
        {
            return image.getColorModel().getPixelSize();
        }
        return Utils.loadUnchangedImageColorDepth(contentNode, tryGetImageID(), imageLibraryOrNull);
    }

    public RequestedImageSize getRequestedSize()
    {
        return imageSize;
    }

    public ImageTransfomationFactories getImageTransformationFactories()
    {
        return imageTransformationFactories;
    }

    public ChannelColorRGB getChannelColor()
    {
        return channelColor;
    }

    /**
     * Returns the applied transformation code
     */
    public String tryGetSingleChannelTransformationCode()
    {
        return singleChannelTransformationCodeOrNull;
    }

    public String tryGetChannelCode()
    {
        return channelCodeOrNull;
    }

    public AbsoluteImageReference createWithoutColorComponent()
    {
        ColorComponent colorComponent = null;
        return new AbsoluteImageReference(contentNode, uniqueId, tryGetImageID(), colorComponent,
                imageSize, channelColor, imageTransformationFactories, imageLibraryOrNull,
                singleChannelTransformationCodeOrNull, null);
    }
}