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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

/**
 * @author Pawel Glyzewski
 */
public interface IImagingLoaderStrategy extends IImageDatasetLoader
{
    /**
     * @param channelCode The code fo the channel to get the image for
     * @param imageSize required image size, specified so that an image with the closest size to the required is returned (e.g. a thumbnail version if
     *            available). Note that this method does no image resizing and the result will most probably not have the required size.
     * @return image (with original file content, page and color)
     */
    AbsoluteImageReference tryGetImage(String channelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize,
            String transformationCodeOrNull);

    /**
     * Finds representative image of this dataset in a given channel.
     * 
     * @param channelCode channel code for which representative image is requested
     * @param wellLocationOrNull if not null the returned images are restricted to one well. Otherwise the dataset is assumed to have no container and
     *            spots.
     * @param imageSize required image size, specified so that an image with the closest size to the required is returned (e.g. a thumbnail version if
     *            available). Note that this method does no image resizing and the result will most probably not have the required size.
     */
    AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String transformationCodeOrNull);
}
