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

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

/**
 * Loades the requested image from the file system using the metadata from the database to find the image.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingDatasetLoader extends IImageDatasetLoader
{
    /**
     * @param channelCode The code for the channel to get the image for
     * @param imageSize required image size, specified so that an image with the closest size to the required is returned (e.g. a thumbnail version if
     *            available). Note that this method does no image resizing and the result will most probably not have the required size.
     * @param transformationCodeOrNull if transformed images are precomputed, they will be returned
     * @return image (with original file content, page and color)
     */
    AbsoluteImageReference tryGetImage(String channelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize,
            String transformationCodeOrNull);

    /**
     * Finds representative image of this data set. Returns <code>null</code> if no image was found.
     */
    AbsoluteImageReference tryFindAnyOriginalImage();

    /**
     * Finds representative image of this dataset in a given channel.
     * 
     * @param channelCode channel code for which representative image is requested
     * @param wellLocationOrNull if not null the returned images are restricted to one well. Otherwise the dataset is assumed to have no container and
     *            spots.
     * @param imageSize required image size, specified so that an image with the closest size to the required is returned (e.g. a thumbnail version if
     *            available). Note that this method does no image resizing and the result will most probably not have the required size.
     * @param transformationCodeOrNull if transformed images are precomputed, they will be returned
     */
    AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String transformationCodeOrNull);

    /**
     * Tries to find a representative thumbnail of this data set. Returns <code>null</code> if no thumbnail was found.
     */
    AbsoluteImageReference tryFindAnyThumbnail();

    /**
     * Tries to find a representative thumbnail of this dataset in a given channel. Returns NULL if no thumbnail was found.
     * 
     * @param channelCode channel code for which representative image is requested
     * @param wellLocationOrNull if not null the returned images are restricted to one well. Otherwise the dataset is assumed to have no container and
     *            spots.
     * @param imageSize
     * @param transformationCodeOrNull
     */
    AbsoluteImageReference tryGetRepresentativeThumbnail(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String transformationCodeOrNull);

    /**
     * Returns the stored thumbnail for the given parameters, or <code>null</code>, if no thumbnail has been stored.
     * <p>
     * This method will never do any calculations, it will not rescale, convert or merge any images. It will just plain give you the byte array that
     * has been stored for the thumbnail.
     * 
     * @param channelCode The code of the channel to get the thumbnail for.
     * @param channelStackReference Specifies well and tile of the thumbnail.
     * @param imageSize
     * @param transformationCodeOrNull if transformed images are precomputed, they will be returned
     */
    AbsoluteImageReference tryGetThumbnail(String channelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize,
            String transformationCodeOrNull);

}