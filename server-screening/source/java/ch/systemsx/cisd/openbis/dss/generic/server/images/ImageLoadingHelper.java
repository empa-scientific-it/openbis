/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.util.ArrayList;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.IImagingLoaderStrategy;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageTransformationParams;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * Helper class for loading images using an {@link IImagingLoaderStrategy}. Actually this class doesn't load images but it creates
 * {@link AbsoluteImageReference} instances which contain lazily loaded images and meta data from the imaging database.
 * <p>
 * This code has been refactored out of {@link ImageChannelsUtils} in autumn 2014.
 *
 * @author Franz-Josef Elmer
 */
class ImageLoadingHelper
{
    private static IImagingLoaderStrategy createLoaderStrategy(DatasetAcquiredImagesReference imageChannels,
            IHierarchicalContentProvider contentProvider)
    {
        String datasetCode = imageChannels.getDatasetCode();
        IHierarchicalContent dataSetRoot = contentProvider.asContent(datasetCode);
        IImagingDatasetLoader loader = HCSImageDatasetLoaderFactory.create(dataSetRoot, datasetCode);
        return ImagingLoaderStrategyFactory.createImageLoaderStrategy(loader);
    }

    private final IImagingLoaderStrategy imageLoaderStrategy;

    private final RequestedImageSize imageSizeLimit;

    private final String singleChannelTransformationCodeOrNull;

    @Private
    ImageLoadingHelper(IImagingLoaderStrategy imageLoaderStrategy,
            RequestedImageSize imageSizeLimit, String singleChannelTransformationCodeOrNull)
    {
        this.imageLoaderStrategy = imageLoaderStrategy;
        this.imageSizeLimit = imageSizeLimit;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
    }

    ImageLoadingHelper(IImagingLoaderStrategy imageLoaderStrategy, Size imageSizeLimitOrNull,
            String singleChannelTransformationCodeOrNull)
    {
        this(imageLoaderStrategy, new RequestedImageSize(imageSizeLimitOrNull, false),
                singleChannelTransformationCodeOrNull);
    }

    ImageLoadingHelper(DatasetAcquiredImagesReference imageChannels, IHierarchicalContentProvider contentProvider,
            RequestedImageSize imageSizeLimit, String singleChannelTransformationCodeOrNull)
    {
        this(createLoaderStrategy(imageChannels, contentProvider), imageSizeLimit, singleChannelTransformationCodeOrNull);
    }

    boolean isMergeAllChannels(DatasetAcquiredImagesReference imageChannels)
    {
        return imageChannels.isMergeAllChannels(getAllChannelCodes());
    }

    /**
     * @param skipNonExisting if true references to non-existing images are ignored, otherwise an exception is thrown
     * @param mergeAllChannels true if all existing channel images should be merged
     * @param transformationInfo
     */
    List<AbsoluteImageReference> fetchImageContents(
            DatasetAcquiredImagesReference imagesReference, boolean mergeAllChannels,
            boolean skipNonExisting, ImageTransformationParams transformationInfo)
    {
        List<String> channelCodes = imagesReference.getChannelCodes(getAllChannelCodes());
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();
        for (String channelCode : channelCodes)
        {
            ImageChannelStackReference channelStackReference = imagesReference.getChannelStackReference();
            AbsoluteImageReference image = imageLoaderStrategy.tryGetImage(channelCode, channelStackReference,
                    imageSizeLimit, singleChannelTransformationCodeOrNull);
            if (image == null && skipNonExisting == false)
            {
                String item = imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image";
                throw EnvironmentFailureException.fromTemplate("No %s found for channel stack %s and channel %s",
                        item, channelStackReference, channelCode);
            }
            if (image != null)
            {
                images.add(image);
            }
        }

        // Optimization for a case where all channels are on one image
        if (mergeAllChannels && (false == shouldApplySingleChannelsTransformations(transformationInfo)))
        {
            AbsoluteImageReference allChannelsImageReference = tryCreateAllChannelsImageReference(images);
            if (allChannelsImageReference != null)
            {
                images.clear();
                images.add(allChannelsImageReference);
            }
        }
        return images;
    }

    List<AbsoluteImageReference> getRepresentativeImageReferences(Location wellLocationOrNull)
    {
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        for (String chosenChannel : getAllChannelCodes())
        {
            images.add(getRepresentativeImageReference(chosenChannel, wellLocationOrNull));
        }
        return images;
    }

    private List<String> getAllChannelCodes()
    {
        return imageLoaderStrategy.getImageParameters().getChannelsCodes();
    }

    /**
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    private AbsoluteImageReference getRepresentativeImageReference(String channelCode,
            Location wellLocationOrNull)
    {
        AbsoluteImageReference image = imageLoaderStrategy.tryGetRepresentativeImage(channelCode,
                wellLocationOrNull, imageSizeLimit, singleChannelTransformationCodeOrNull);
        if (image != null)
        {
            return image;
        }
        String item = imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image";
        throw EnvironmentFailureException.fromTemplate("No representative %s found for well %s and channel %s",
                item, wellLocationOrNull, channelCode);
    }

    private boolean shouldApplySingleChannelsTransformations(ImageTransformationParams transformationInfo)
    {
        return transformationInfo != null
                && transformationInfo.tryGetTransformationCodeForChannels() != null
                && transformationInfo.tryGetTransformationCodeForChannels().size() > 0;
    }

    // Checks if all images differ only at the color component level and stem from the same page
    // of the same file. If that's the case any image from the collection contains the merged
    // channels image (if we erase the color component).
    private AbsoluteImageReference tryCreateAllChannelsImageReference(
            List<AbsoluteImageReference> imageReferences)
    {
        AbsoluteImageReference lastFound = null;
        for (AbsoluteImageReference image : imageReferences)
        {
            if (lastFound == null)
            {
                lastFound = image;
            } else
            {
                if (equals(image.tryGetImageID(), lastFound.tryGetImageID()) == false
                        || image.getUniqueId().equals(lastFound.getUniqueId()) == false)
                {
                    return null;
                }
            }
        }
        if (lastFound != null)
        {
            return lastFound.createWithoutColorComponent();
        } else
        {
            return null;
        }
    }

    private static boolean equals(String i1OrNull, String i2OrNull)
    {
        return (i1OrNull == null) ? (i2OrNull == null) : i1OrNull.equals(i2OrNull);
    }
}