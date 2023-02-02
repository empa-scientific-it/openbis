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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * Channels of the basic images and overlay images.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageChannelsReference
{
    public static LogicalImageChannelsReference createWithoutOverlays(
            LogicalImageReference basicImage, List<String> channels,
            String imageTransformationCodeOrNull, Map<String, IntensityRange> rangesOrNull)
    {
        return new LogicalImageChannelsReference(basicImage, channels,
                imageTransformationCodeOrNull, rangesOrNull, new HashSet<ImageDatasetChannel>());
    }

    // ----

    private final LogicalImageReference basicImage;

    private final List<String> channels;

    private final String imageTransformationCodeOrNull;

    private final Map<String, IntensityRange> rangesOrNull;

    private final Set<ImageDatasetChannel> overlayChannels;

    public LogicalImageChannelsReference(LogicalImageReference basicImage, List<String> channels,
            String imageTransformationCodeOrNull, Map<String, IntensityRange> rangesOrNull,
            Set<ImageDatasetChannel> overlayChannels)
    {
        this.basicImage = basicImage;
        this.channels = channels;
        this.imageTransformationCodeOrNull = imageTransformationCodeOrNull;
        this.rangesOrNull = rangesOrNull;
        this.overlayChannels = overlayChannels;
    }

    public LogicalImageReference getBasicImage()
    {
        return basicImage;
    }

    public List<String> getChannelCodes()
    {
        return channels;
    }

    public Set<ImageDatasetChannel> getOverlayChannels()
    {
        return overlayChannels;
    }

    public String tryGetImageTransformationCode()
    {
        return imageTransformationCodeOrNull;
    }

    public Map<String, IntensityRange> tryGetIntensityRanges()
    {
        return rangesOrNull;
    }
}
