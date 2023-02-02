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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.HCSChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.MicroscopyChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * DTO which describes which image channels and which overlays should be used to generate an image.
 * 
 * @author Tomasz Pylak
 */
public class ImageGenerationDescription
{
    private final DatasetAcquiredImagesReference imageChannelsOrNull;

    private final String singleChannelTransformationCodeOrNull;

    private final Map<String, String> transformationsPerChannelOrNull;

    private final List<DatasetAcquiredImagesReference> overlayChannels;

    private final String sessionId;

    // original size if null
    private final Size thumbnailSizeOrNull;

    public ImageGenerationDescription(DatasetAcquiredImagesReference imageChannelsOrNull,
            String singleChannelTransformationCodeOrNull,
            Map<String, String> transformationsPerChannelOrNull,
            List<DatasetAcquiredImagesReference> overlayChannels, String sessionId,
            Size thumbnailSizeOrNull)
    {
        this.imageChannelsOrNull = imageChannelsOrNull;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
        this.transformationsPerChannelOrNull = transformationsPerChannelOrNull;
        this.overlayChannels = overlayChannels;
        this.sessionId = sessionId;
        this.thumbnailSizeOrNull = thumbnailSizeOrNull;
    }

    public DatasetAcquiredImagesReference tryGetImageChannels()
    {
        return imageChannelsOrNull;
    }

    public String tryGetSingleChannelTransformationCode()
    {
        return singleChannelTransformationCodeOrNull;
    }

    public Map<String, String> tryGetTransformationsPerChannel()
    {
        return transformationsPerChannelOrNull;
    }

    public List<DatasetAcquiredImagesReference> getOverlayChannels()
    {
        return overlayChannels;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public Size tryGetThumbnailSize()
    {
        return thumbnailSizeOrNull;
    }

    @Override
    public String toString()
    {
        return "ImageGenerationDescription [image channels="
                + imageChannelsOrNull
                + (overlayChannels == null || overlayChannels.size() == 0 ? ""
                        : ", overlay channels=" + overlayChannels) + ", "
                + (thumbnailSizeOrNull == null ? "original size" : "size=" + thumbnailSizeOrNull)
                + "]";
    }

    /**
     * Creates description of the parameters which can be used e.g. to give a name to the generated image file.
     */
    public String getShortDescription()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("image");
        if (imageChannelsOrNull != null)
        {
            appendShortDescription(sb, imageChannelsOrNull);
        }
        for (DatasetAcquiredImagesReference overlay : overlayChannels)
        {
            sb.append("_");
            appendChannels(sb, overlay);
        }
        if (thumbnailSizeOrNull != null)
        {
            sb.append("_");
            sb.append(thumbnailSizeOrNull.getWidth());
            sb.append("x");
            sb.append(thumbnailSizeOrNull.getHeight());
        }
        return sb.toString();
    }

    private static void appendShortDescription(StringBuffer sb,
            DatasetAcquiredImagesReference imagesRef)
    {
        appendChannels(sb, imagesRef);
        appendStackRef(sb, imagesRef.getChannelStackReference());
    }

    private static void appendChannels(StringBuffer sb, DatasetAcquiredImagesReference imagesRef)
    {
        List<String> channelCodes = imagesRef.getChannelCodes(null);
        if (channelCodes == null)
        {
            sb.append("merged");
        } else
        {
            for (String channel : channelCodes)
            {
                sb.append("_");
                sb.append(channel);
            }
        }
    }

    private static void appendStackRef(StringBuffer sb, ImageChannelStackReference stackRef)
    {
        HCSChannelStackByLocationReference hcsChannelStack = stackRef.tryGetHCSChannelStack();
        if (hcsChannelStack != null)
        {
            sb.append("_well");
            sb.append(hcsChannelStack.getWellLocation().createMatrixCoordinateFromLocation());
            appendTileLocation(sb, hcsChannelStack.getTileLocation());
        }
        MicroscopyChannelStackByLocationReference microscopyChannelStack =
                stackRef.tryGetMicroscopyChannelStack();
        if (microscopyChannelStack != null)
        {
            appendTileLocation(sb, microscopyChannelStack.getTileLocation());
        }
        Long channelStackId = stackRef.tryGetChannelStackId();
        if (channelStackId != null)
        {
            sb.append("_");
            sb.append(channelStackId);
        }
    }

    private static void appendTileLocation(StringBuffer sb, Location tileLocation)
    {
        sb.append("_tile");
        sb.append(tileLocation.getY());
        sb.append("x");
        sb.append(tileLocation.getX());
    }
}
