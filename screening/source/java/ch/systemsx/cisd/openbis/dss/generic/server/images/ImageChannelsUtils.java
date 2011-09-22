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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.IImagingLoaderStrategy;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.generic.server.ResponseContentStream;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageTransformationParams;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelColor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Utility classes to create an image of a specified size containing one channel or a subset of all
 * channels.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelsUtils
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ImageChannelsUtils.class);

    // MIME type of the images which are produced by this class
    public static final String IMAGES_CONTENT_TYPE = "image/png";

    public static interface IDatasetDirectoryProvider
    {
        /** directory where dataset can be found in DSS store */
        File getDatasetRoot(String datasetCode);
    }

    private final IImagingLoaderStrategy imageLoaderStrategy;

    private final RequestedImageSize imageSizeLimit;

    @Private
    ImageChannelsUtils(IImagingLoaderStrategy imageLoaderStrategy, RequestedImageSize imageSizeLimit)
    {
        this.imageLoaderStrategy = imageLoaderStrategy;
        this.imageSizeLimit = imageSizeLimit;
    }

    @Private
    ImageChannelsUtils(IImagingLoaderStrategy imageLoaderStrategy, Size imageSizeLimitOrNull)
    {
        this(imageLoaderStrategy, new RequestedImageSize(imageSizeLimitOrNull, false));
    }

    /**
     * Returns content of image for the specified tile in the specified size and for the requested
     * channel or with all channels merged.
     * 
     * @param datasetDirectoryProvider
     */
    public static ResponseContentStream getImageStream(ImageGenerationDescription params,
            IDatasetDirectoryProvider datasetDirectoryProvider)
    {
        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();

        BufferedImage image = null;
        DatasetAcquiredImagesReference imageChannels = params.tryGetImageChannels();
        if (imageChannels != null)
        {
            RequestedImageSize imageSize = new RequestedImageSize(thumbnailSizeOrNull, false);
            image =
                    calculateBufferedImage(imageChannels,
                            params.tryGetSingleChannelTransformationCode(),
                            datasetDirectoryProvider, imageSize);
        }

        RequestedImageSize overlaySize = calcOverlaySize(image, thumbnailSizeOrNull);
        for (DatasetAcquiredImagesReference overlayChannels : params.getOverlayChannels())
        {
            // NOTE: never merges the overlays, draws each channel separately (merging looses
            // transparency and is slower)
            List<ImageWithReference> overlayImages =
                    getSingleImagesSkipNonExisting(overlayChannels, overlaySize,
                            datasetDirectoryProvider);
            for (ImageWithReference overlayImage : overlayImages)
            {
                if (image != null)
                {
                    drawOverlay(image, overlayImage);
                } else
                {
                    image = overlayImage.getBufferedImage();
                }
            }
        }
        if (image == null)
        {
            throw new UserFailureException("No image is available for parameters: " + params);
        }
        return createResponseContentStream(image, null);
    }

    private static List<ImageWithReference> getSingleImagesSkipNonExisting(
            DatasetAcquiredImagesReference imagesReference, RequestedImageSize imageSize,
            IDatasetDirectoryProvider datasetDirectoryProvider)
    {
        ImageChannelsUtils utils =
                createImageChannelsUtils(imagesReference, datasetDirectoryProvider, imageSize);
        boolean mergeAllChannels = utils.isMergeAllChannels(imagesReference);
        List<AbsoluteImageReference> imageContents =
                utils.fetchImageContents(imagesReference, mergeAllChannels, true);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(true, mergeAllChannels, null);
        return calculateSingleImagesForDisplay(imageContents, transformationInfo);
    }

    private static RequestedImageSize getSize(BufferedImage img, boolean highQuality)
    {
        return new RequestedImageSize(new Size(img.getWidth(), img.getHeight()), true, highQuality);
    }

    private static RequestedImageSize calcOverlaySize(BufferedImage imageOrNull,
            Size thumbnailSizeOrNull)
    {
        if (thumbnailSizeOrNull == null)
        {
            // thumbnais are not used, so use the original size even if it does not match
            return RequestedImageSize.createOriginal();
        } else
        {
            // we want higher quality only if thumbnail overlays are generated
            boolean highQuality = true;
            if (imageOrNull != null)
            {
                // all overlays have to be of the same size as the basic image
                return getSize(imageOrNull, highQuality);
            } else
            {
                // if there is no basic image yet, enlarging too small images is not necessary
                return new RequestedImageSize(thumbnailSizeOrNull, false, highQuality);
            }
        }
    }

    private static ResponseContentStream createResponseContentStream(BufferedImage image,
            String nameOrNull)
    {
        IContent imageContent = createPngContent(image, nameOrNull);
        return asResponseContentStream(imageContent);
    }

    private static BufferedImage calculateBufferedImage(
            DatasetAcquiredImagesReference imageChannels,
            String singleChannelTransformationCodeOrNull,
            IDatasetDirectoryProvider datasetDirectoryProvider, RequestedImageSize imageSizeLimit)
    {
        ImageChannelsUtils imageChannelsUtils =
                createImageChannelsUtils(imageChannels, datasetDirectoryProvider, imageSizeLimit);

        boolean useMergedChannelsTransformation =
                imageChannelsUtils.isMergeAllChannels(imageChannels);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(true, useMergedChannelsTransformation,
                        singleChannelTransformationCodeOrNull);

        return imageChannelsUtils.calculateBufferedImage(imageChannels, transformationInfo);
    }

    private static ImageChannelsUtils createImageChannelsUtils(
            DatasetAcquiredImagesReference imageChannels,
            IDatasetDirectoryProvider datasetDirectoryProvider, RequestedImageSize imageSizeLimit)
    {
        IImagingDatasetLoader imageAccessor =
                createImageAccessor(imageChannels, datasetDirectoryProvider);
        return new ImageChannelsUtils(
                ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                imageSizeLimit);
    }

    @Private
    BufferedImage calculateBufferedImage(DatasetAcquiredImagesReference imageChannels,
            ImageTransformationParams transformationInfo)
    {
        boolean mergeAllChannels = isMergeAllChannels(imageChannels);
        List<AbsoluteImageReference> imageContents =
                fetchImageContents(imageChannels, mergeAllChannels, false);
        return calculateBufferedImage(imageContents, transformationInfo);
    }

    private boolean isMergeAllChannels(DatasetAcquiredImagesReference imageChannels)
    {
        return imageChannels.isMergeAllChannels(getAllChannelCodes());
    }

    /**
     * @param skipNonExisting if true references to non-existing images are ignored, otherwise an
     *            exception is thrown
     * @param mergeAllChannels true if all existing channel images should be merged
     */
    private List<AbsoluteImageReference> fetchImageContents(
            DatasetAcquiredImagesReference imagesReference, boolean mergeAllChannels,
            boolean skipNonExisting)
    {
        List<String> channelCodes = imagesReference.getChannelCodes(getAllChannelCodes());
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();
        for (String channelCode : channelCodes)
        {
            ImageChannelStackReference channelStackReference =
                    imagesReference.getChannelStackReference();
            AbsoluteImageReference image =
                    imageLoaderStrategy.tryGetImage(channelCode, channelStackReference,
                            imageSizeLimit);
            if (image == null && skipNonExisting == false)
            {
                throw createImageNotFoundException(channelStackReference, channelCode);
            }
            if (image != null)
            {
                images.add(image);
            }
        }

        // Optimization for a case where all channels are on one image
        if (mergeAllChannels)
        {
            AbsoluteImageReference allChannelsImageReference =
                    tryCreateAllChannelsImageReference(images);
            if (allChannelsImageReference != null)
            {
                images.clear();
                images.add(allChannelsImageReference);
            }
        }
        return images;
    }

    private static IImagingDatasetLoader createImageAccessor(
            DatasetAcquiredImagesReference imagesReference,
            IDatasetDirectoryProvider datasetDirectoryProvider)
    {
        String datasetCode = imagesReference.getDatasetCode();
        File datasetRoot = datasetDirectoryProvider.getDatasetRoot(datasetCode);
        return HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
    }

    /**
     * Returns content of the image which is representative for the given dataset.
     */
    public static ResponseContentStream getRepresentativeImageStream(File datasetRoot,
            String datasetCode, Location wellLocationOrNull, Size imageSizeLimitOrNull)
    {
        IImagingDatasetLoader imageAccessor =
                HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
        List<AbsoluteImageReference> imageReferences =
                new ImageChannelsUtils(
                        ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                        imageSizeLimitOrNull).getRepresentativeImageReferences(wellLocationOrNull);
        BufferedImage image =
                calculateBufferedImage(imageReferences, new ImageTransformationParams(true, true,
                        null));
        String name = createFileName(datasetCode, wellLocationOrNull, imageSizeLimitOrNull);
        return createResponseContentStream(image, name);
    }

    private static String createFileName(String datasetCode, Location wellLocationOrNull,
            Size imageSizeLimitOrNull)
    {
        String name = "dataset_" + datasetCode;
        if (wellLocationOrNull != null)
        {
            name += "_row" + wellLocationOrNull.getY();
            name += "_col" + wellLocationOrNull.getX();
        }
        if (imageSizeLimitOrNull != null)
        {
            name += "_small";
        }
        name += ".png";
        return name;
    }

    private static ResponseContentStream asResponseContentStream(IContent image)
    {
        return ResponseContentStream.create(image.getInputStream(), image.getSize(),
                ImageChannelsUtils.IMAGES_CONTENT_TYPE, image.tryGetName());
    }

    /**
     * @return an image for the specified tile in the specified size and for the requested channel.
     */
    public static IContent getImage(IImagingLoaderStrategy imageLoaderStrategy,
            ImageChannelStackReference channelStackReference, String chosenChannelCode,
            Size imageSizeLimitOrNull, String singleChannelImageTransformationCodeOrNull,
            boolean convertToPng, boolean transform)
    {
        DatasetAcquiredImagesReference imagesReference =
                createDatasetAcquiredImagesReference(imageLoaderStrategy, channelStackReference,
                        chosenChannelCode == null ? ScreeningConstants.MERGED_CHANNELS
                                : chosenChannelCode);

        ImageChannelsUtils imageChannelsUtils =
                new ImageChannelsUtils(imageLoaderStrategy, imageSizeLimitOrNull);
        boolean mergeAllChannels = imageChannelsUtils.isMergeAllChannels(imagesReference);
        List<AbsoluteImageReference> imageContents =
                imageChannelsUtils.fetchImageContents(imagesReference, mergeAllChannels, false);

        IContent rawContent = tryGetRawContent(convertToPng, imageContents);
        if (rawContent != null)
        {
            return rawContent;
        }
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(transform, mergeAllChannels,
                        singleChannelImageTransformationCodeOrNull);
        BufferedImage image = calculateBufferedImage(imageContents, transformationInfo);
        return createPngContent(image, null);
    }

    private static DatasetAcquiredImagesReference createDatasetAcquiredImagesReference(
            IImagingLoaderStrategy imageLoaderStrategy,
            ImageChannelStackReference channelStackReference, String chosenChannelCode)
    {
        String datasetCode = imageLoaderStrategy.getImageParameters().getDatasetCode();
        boolean isMergedChannels =
                ScreeningConstants.MERGED_CHANNELS.equalsIgnoreCase(chosenChannelCode);
        if (isMergedChannels)
        {
            return DatasetAcquiredImagesReference.createForMergedChannels(datasetCode,
                    channelStackReference);
        } else
        {
            return DatasetAcquiredImagesReference.createForSingleChannel(datasetCode,
                    channelStackReference, chosenChannelCode);
        }
    }

    // optimization: if there is exactly one image reference, maybe its original raw content is the
    // appropriate answer?
    private static IContent tryGetRawContent(boolean convertToPng,
            List<AbsoluteImageReference> imageContents)
    {
        if (imageContents.size() == 1 && convertToPng == false)
        {
            AbsoluteImageReference imageReference = imageContents.get(0);
            return imageReference.tryGetRawContent();
        }
        return null;
    }

    private List<AbsoluteImageReference> getRepresentativeImageReferences(
            Location wellLocationOrNull)
    {
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        for (String chosenChannel : getAllChannelCodes())
        {
            AbsoluteImageReference image =
                    getRepresentativeImageReference(chosenChannel, wellLocationOrNull);
            images.add(image);
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
        AbsoluteImageReference image =
                imageLoaderStrategy.tryGetRepresentativeImage(channelCode, wellLocationOrNull,
                        imageSizeLimit);
        if (image != null)
        {
            return image;
        } else
        {
            throw EnvironmentFailureException.fromTemplate(
                    "No representative "
                            + (imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image")
                            + " found for well %s and channel %s", wellLocationOrNull, channelCode);
        }
    }

    /**
     * @param useMergedChannelsTransformation sometimes we can have a single image which contain all
     *            channels merged. In this case a different transformation will be applied to it.
     */
    private static BufferedImage calculateAndTransformSingleImageForDisplay(
            AbsoluteImageReference imageReference, ImageTransformationParams transformationInfo)
    {
        BufferedImage image = calculateSingleImage(imageReference);
        image = transform(image, imageReference, transformationInfo);
        image = ImageUtil.convertForDisplayIfNecessary(image);
        return image;
    }

    private static BufferedImage calculateSingleImage(AbsoluteImageReference imageReference)
    {
        long start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
        BufferedImage image = imageReference.getUnchangedImage();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Load original image: " + (System.currentTimeMillis() - start));
        }

        // resized the image if necessary
        RequestedImageSize requestedSize = imageReference.getRequestedSize();

        Size size = requestedSize.tryGetThumbnailSize();
        if (size != null)
        {
            start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            image =
                    ImageUtil.rescale(image, size.getWidth(), size.getHeight(),
                            requestedSize.enlargeIfNecessary(),
                            requestedSize.isHighQualityRescalingRequired());
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Create thumbnail: " + (System.currentTimeMillis() - start));
            }
        }

        // choose color component if necessary
        final ColorComponent colorComponentOrNull = imageReference.tryGetColorComponent();
        if (colorComponentOrNull != null)
        {
            start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            image = transformToChannel(image, colorComponentOrNull);
            if (operationLog.isDebugEnabled())
            {
                operationLog
                        .debug("Select single channel: " + (System.currentTimeMillis() - start));
            }
        }
        return image;
    }

    /**
     * @param allChannelsMerged if true then we use one special transformation on the merged images
     *            instead of transforming every single image.
     */
    private static BufferedImage calculateBufferedImage(
            List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo)
    {
        AbsoluteImageReference singleImageReference = imageReferences.get(0);
        if (imageReferences.size() == 1)
        {
            return calculateAndTransformSingleImageForDisplay(singleImageReference,
                    transformationInfo);
        } else
        {
            return mergeChannels(imageReferences, transformationInfo, singleImageReference);
        }
    }

    private static BufferedImage mergeChannels(List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo,
            AbsoluteImageReference singleImageReference)
    {
        // We do not transform single images here.
        // The 'merged channels' transformation will be applied later.
        List<ImageWithReference> images = calculateSingleImagesForDisplay(imageReferences, null);
        BufferedImage mergedImage = mergeImages(images);
        // NOTE: even if we are not merging all the channels but just few of them we use the
        // merged-channel transformation

        // TODO 2011-09-13, Tomasz Pylak: it looks like we are applying image level
        // transformation from a random single channel to merged images. Replace with following
        // code after testing:

        // if (transformationInfo.isApplyNonImageLevelTransformation())
        // {
        // IImageTransformerFactory transformationOrNull =
        // singleImageReference.getImageTransfomationFactories().tryGetForMerged();
        // mergedImage =
        // applyImageTransformation(mergedImage, transformationOrNull);
        // }
        // return mergedImage;

        return transform(mergedImage, singleImageReference,
                transformationInfo.cloneAndSetUseMergedChannelsTransformation());
    }

    private static BufferedImage transform(BufferedImage image,
            AbsoluteImageReference imageReference, ImageTransformationParams transformationInfo)
    {
        BufferedImage resultImage = image;
        ImageTransfomationFactories transfomations =
                imageReference.getImageTransfomationFactories();
        // image level transformation is applied always, as it cannot be applied or changed in
        // external image viewer
        resultImage = applyImageTransformation(resultImage, transfomations.tryGetForImage());

        if (transformationInfo.isApplyNonImageLevelTransformation() == false)
        {
            return resultImage;
        }
        IImageTransformerFactory channelLevelTransformationOrNull;
        if (transformationInfo.isUseMergedChannelsTransformation())
        {
            channelLevelTransformationOrNull = transfomations.tryGetForMerged();
        } else
        {
            channelLevelTransformationOrNull =
                    transfomations.tryGetForChannel(transformationInfo
                            .tryGetSingleChannelTransformationCode());
        }
        return applyImageTransformation(resultImage, channelLevelTransformationOrNull);
    }

    private static BufferedImage applyImageTransformation(BufferedImage image,
            IImageTransformerFactory transformerFactoryOrNull)
    {
        if (transformerFactoryOrNull == null)
        {
            return image;
        }
        return transformerFactoryOrNull.createTransformer().transform(image);
    }

    private static class ImageWithReference
    {
        private final BufferedImage image;

        private final AbsoluteImageReference reference;

        public ImageWithReference(BufferedImage image, AbsoluteImageReference reference)
        {
            this.image = image;
            this.reference = reference;
        }

        public BufferedImage getBufferedImage()
        {
            return image;
        }

        public AbsoluteImageReference getReference()
        {
            return reference;
        }
    }

    /**
     * @param transformationInfoOrNull if null all transformations (including image-level) will be
     *            skipped
     */
    private static List<ImageWithReference> calculateSingleImagesForDisplay(
            List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfoOrNull)
    {
        List<ImageWithReference> images = new ArrayList<ImageWithReference>();
        for (AbsoluteImageReference imageRef : imageReferences)
        {
            BufferedImage image;
            if (transformationInfoOrNull != null)
            {
                image =
                        calculateAndTransformSingleImageForDisplay(imageRef,
                                transformationInfoOrNull);
            } else
            {
                // NOTE: here we skip image level transformations as well
                image = calculateSingleImage(imageRef);
                image = ImageUtil.convertForDisplayIfNecessary(image);
            }
            images.add(new ImageWithReference(image, imageRef));
        }
        return images;
    }

    // Checks if all images differ only at the color component level and stem from the same page
    // of the same file. If that's the case any image from the collection contains the merged
    // channels image (if we erase the color component).
    private static AbsoluteImageReference tryCreateAllChannelsImageReference(
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

    // this method always returns RGB images, even if the input was in grayscale
    private static BufferedImage mergeImages(List<ImageWithReference> images)
    {
        assert images.size() > 1 : "more than 1 image expected, but found: " + images.size();

        BufferedImage newImage = createNewRGBImage(images.get(0).getBufferedImage());
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        int colorBuffer[] = new int[4];
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int mergedRGB = mergeRGBColor(images, x, y, colorBuffer);
                newImage.setRGB(x, y, mergedRGB);
            }
        }
        return newImage;
    }

    private static void drawOverlay(BufferedImage image, ImageWithReference overlayImage)
    {
        BufferedImage overlayBufferedImage = overlayImage.getBufferedImage();
        if (supportsTransparency(overlayImage))
        {
            drawTransparentOverlayFast(image, overlayBufferedImage);
        } else
        {
            drawOverlaySlow(image, overlayBufferedImage);
        }
    }

    private static void drawTransparentOverlayFast(BufferedImage image,
            BufferedImage overlayBufferedImage)
    {
        Graphics2D graphics = image.createGraphics();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        graphics.setComposite(ac);
        graphics.drawImage(overlayBufferedImage, null, null);
    }

    /**
     * Draws overlay by computing the maximal color components (r,g,b) for each pixel. In this way
     * both transparent and black pixels are not drawn. Useful when overlays are saved in a format
     * which does not support transparency.
     */
    private static void drawOverlaySlow(BufferedImage image, BufferedImage overlayImage)
    {
        int width = Math.min(image.getWidth(), overlayImage.getWidth());
        int height = Math.min(image.getHeight(), overlayImage.getHeight());

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int imageRGB = image.getRGB(x, y);
                int overlayRGB = overlayImage.getRGB(x, y);
                int overlayedRGB = overlayRGBColor(imageRGB, overlayRGB);
                image.setRGB(x, y, overlayedRGB);
            }
        }
    }

    // creates a color with a maximum value of each component
    private static int overlayRGBColor(int imageRGB, int overlayRGB)
    {
        Color imageColor = new Color(imageRGB);
        Color overlayColor = new Color(overlayRGB, true);

        if (overlayColor.getAlpha() == 0)
        {
            return imageRGB; // overlay is transparent, return the original pixel
        } else
        {
            int r = Math.max(imageColor.getRed(), overlayColor.getRed());
            int g = Math.max(imageColor.getGreen(), overlayColor.getGreen());
            int b = Math.max(imageColor.getBlue(), overlayColor.getBlue());
            return new Color(r, g, b).getRGB();
        }
    }

    private static boolean supportsTransparency(ImageWithReference image)
    {
        return image.getBufferedImage().getColorModel().hasAlpha();
    }

    private static int mergeRGBColor(List<ImageWithReference> images, int x, int y,
            int colorBuffer[])
    {
        Arrays.fill(colorBuffer, 0);
        for (int index = 0; index < images.size(); index++)
        {
            ImageWithReference image = images.get(index);
            int rgb = image.getBufferedImage().getRGB(x, y);
            Color singleColor = new Color(rgb, true);
            AbsoluteImageReference imageReference = image.getReference();
            setColorComponents(colorBuffer, singleColor, imageReference);
            // merge alpha channel
            colorBuffer[3] = Math.max(colorBuffer[3], singleColor.getAlpha());
        }
        return asRGB(colorBuffer);
    }

    private static void setColorComponents(int[] colorBuffer, Color singleColor,
            AbsoluteImageReference imageReference)
    {
        ColorComponent colorComponent = imageReference.tryGetColorComponent();
        if (colorComponent != null)
        {
            int index = getColorComponentIndex(colorComponent);
            colorBuffer[index] = colorComponent.getComponent(singleColor);
        } else
        {
            ImageChannelColor channelColor = imageReference.getChannelColor();
            setColorComponentsForChannelIndex(colorBuffer, singleColor, channelColor);
        }
    }

    private static int getColorComponentIndex(ColorComponent colorComponent)
    {
        switch (colorComponent)
        {
            case RED:
                return 0;
            case GREEN:
                return 1;
            case BLUE:
                return 2;
            default:
                throw new IllegalStateException("Unknown color " + colorComponent);
        }
    }

    private static void setColorComponentsForChannelIndex(int[] colorBuffer, Color singleColor,
            ImageChannelColor channelColor)
    {
        int maxIngredient = extractMaxColorIngredient(singleColor);
        for (int i : getRGBColorIndexes(channelColor))
        {
            colorBuffer[i] = Math.max(colorBuffer[i], maxIngredient);
        }
    }

    // --------- common

    private EnvironmentFailureException createImageNotFoundException(
            ImageChannelStackReference channelStackReference, String chosenChannelCode)
    {
        return EnvironmentFailureException.fromTemplate(
                "No " + (imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image")
                        + " found for channel stack %s and channel %s", channelStackReference,
                chosenChannelCode);
    }

    /**
     * Transforms the given <var>bufferedImage</var> by selecting a single channel from it.
     */
    private static BufferedImage transformToChannel(BufferedImage bufferedImage,
            ColorComponent colorComponent)
    {
        BufferedImage newImage = createNewRGBImage(bufferedImage);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int rgb = bufferedImage.getRGB(x, y);
                int channelColor = extractSingleComponent(rgb, colorComponent);
                newImage.setRGB(x, y, channelColor);
            }
        }
        return newImage;
    }

    // NOTE: drawing on this image will not preserve transparency - but we do not need it and the
    // image is smaller
    private static BufferedImage createNewRGBImage(RenderedImage bufferedImage)
    {
        BufferedImage newImage =
                new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        return newImage;
    }

    // 0=R, 1=G, 2=B, 3=RG, 4=RB, 5=GB
    private static int[] getRGBColorIndexes(ImageChannelColor channelColor)
    {
        switch (channelColor)
        {
            case RED:
                return new int[]
                    { 0 };
            case GREEN:
                return new int[]
                    { 1 };
            case BLUE:
                return new int[]
                    { 2 };
            case RED_GREEN:
                return new int[]
                    { 0, 1 };
            case RED_BLUE:
                return new int[]
                    { 0, 2 };
            case GREEN_BLUE:
                return new int[]
                    { 1, 2 };
            default:
                throw new IllegalStateException("not possible");
        }
    }

    // We reset all ingredients besides the one which is specified by color component.
    // The result is the rgb value with only one component which is non-zero.
    private static int extractSingleComponent(int rgb, ColorComponent colorComponent)
    {
        return colorComponent.extractSingleComponent(rgb).getRGB();
    }

    // returns the max ingredient for the color
    private static int extractMaxColorIngredient(Color c)
    {
        return Math.max(Math.max(c.getBlue(), c.getGreen()), c.getRed());
    }

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2], rgb[3]).getRGB();
    }

    private static IContent createPngContent(BufferedImage image, String nameOrNull)
    {
        final byte[] output = ImageUtil.imageToPngFast(image);
        return new ByteArrayBasedContent(output, nameOrNull);
    }

}
