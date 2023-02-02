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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.PixelHistogram;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.dss.etl.IImageProvider;
import ch.systemsx.cisd.openbis.dss.etl.Utils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageMetadata;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IntensityRange;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleOverviewImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformationBuffer;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Allows to prepare the image dataset which should be registered easily using the specified {@link SimpleImageDataConfig}.
 * 
 * @author Tomasz Pylak
 */
public class SimpleImageDataSetRegistrator
{
    public static final String OPTIMAL_DATASET_INTENSITY_RESCALING_DESCRIPTION =
            "Optimal intensity rescaling for a series of images. "
                    + "It allows to compare images of one plate's dataset to each other."
                    + "At the same time it causes that the conversion to 8 bit color depth looses less information, "
                    + "especially when images use only a small part of available intensities range.";

    @Private
    static interface IImageReaderFactory
    {
        IImageReader tryGetReader(String libraryName, String readerName);

        IImageReader tryGetReaderForFile(String libraryName, String fileName);
    }

    private static class ImageTokensWithPath extends ImageMetadata
    {
        /** path relative to the incoming dataset directory */
        private String imageRelativePath;

        public ImageTokensWithPath(ImageMetadata imageTokens, String imageRelativePath)
        {
            setWell(imageTokens.getWell());
            setChannelCode(imageTokens.getChannelCode());
            setTileNumber(imageTokens.getTileNumber());
            setDepth(imageTokens.tryGetDepth());
            setTimepoint(imageTokens.tryGetTimepoint());
            setSeriesNumber(imageTokens.tryGetSeriesNumber());
            setImageIdentifier(imageTokens.tryGetImageIdentifier());
            this.imageRelativePath = imageRelativePath;
        }

        public String getImagePath()
        {
            return imageRelativePath;
        }
    }

    public static DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            SimpleImageDataConfig simpleImageConfig, File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory, IImageProvider imageProvider)
    {
        return createImageDatasetDetails(simpleImageConfig, incoming, factory, imageProvider,
                new IImageReaderFactory()
                    {
                        @Override
                        public IImageReader tryGetReaderForFile(String libraryName, String fileName)
                        {
                            return ImageReaderFactory.tryGetReaderForFile(libraryName, fileName);
                        }

                        @Override
                        public IImageReader tryGetReader(String libraryName, String readerName)
                        {
                            return ImageReaderFactory.tryGetReader(libraryName, readerName);
                        }
                    });
    }

    private static DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            SimpleImageDataConfig simpleImageConfig, File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory, IImageProvider imageProvider,
            IImageReaderFactory readerFactory)
    {
        SimpleImageDataSetRegistrator registrator =
                new SimpleImageDataSetRegistrator(simpleImageConfig, imageProvider, readerFactory);
        return registrator.createImageDatasetDetails(incoming, factory);
    }

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SimpleImageDataSetRegistrator.class);

    private final SimpleImageDataConfig simpleImageConfig;

    private final IImageReaderFactory readerFactory;

    private final IImageProvider imageProvider;

    private SimpleImageDataSetRegistrator(SimpleImageDataConfig simpleImageConfig, IImageProvider imageProvider,
            IImageReaderFactory readerFactory)
    {
        this.simpleImageConfig = simpleImageConfig;
        this.imageProvider = imageProvider;
        this.readerFactory = readerFactory;
    }

    private DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails =
                imageDatasetFactory.createDataSetRegistrationDetails();
        ImageDataSetInformation imageDataset = registrationDetails.getDataSetInformation();

        if (simpleImageConfig instanceof SimpleOverviewImageDataConfig)
        {
            SimpleOverviewImageDataConfig simpleOverviewImageConfig =
                    (SimpleOverviewImageDataConfig) simpleImageConfig;

            imageDataset.setContainerDatasetPermId(simpleOverviewImageConfig
                    .getContainerDataSetCode());
            imageDataset.setGenerateOverviewImagesFromRegisteredImages(simpleOverviewImageConfig
                    .isGenerateOverviewImagesFromRegisteredImages());
            imageDataset.setRegisterAsOverviewImageDataSet(true);
        }

        setImageDataset(incoming, imageDataset);
        List<Channel> channels = simpleImageConfig.getChannels();
        if (channels != null)
        {
            List<ChannelColorComponent> channelColorComponentsOrNull =
                    simpleImageConfig.getChannelColorComponentsOrNull();
            if (channelColorComponentsOrNull == null)
            {
                imageDataset.setChannels(channels);
            } else
            {
                imageDataset.setChannels(channels, channelColorComponentsOrNull);
            }
        }

        imageDataset.setColorDepth(simpleImageConfig.getColorDepth());

        setRegistrationDetails(registrationDetails, imageDataset);
        registrationDetails.getDataSetInformation().setImageGenerationAlgorithm(simpleImageConfig.getImageGenerationAlgorithm());
        return registrationDetails;
    }

    /**
     * Finds all images in the directory.
     */
    private List<File> listImageFiles(final File incoming)
    {
        String[] extensions = simpleImageConfig.getRecognizedImageExtensions();
        if (incoming.isFile())
        {
            List<File> list = new ArrayList<File>();
            if (extensionMatches(incoming, extensions))
            {
                list.add(incoming);
            }
            return list;
        } else
        {
            return FileOperations.getInstance().listFiles(incoming, extensions, true);
        }
    }

    private boolean extensionMatches(final File incoming, String[] extensions)
    {
        if (extensions == null || extensions.length == 0)
        {
            return true;
        }
        String fileExt = FilenameUtils.getExtension(incoming.getName());
        if (fileExt == null)
        {
            fileExt = "";
        }
        for (String ext : extensions)
        {
            if (ext.equalsIgnoreCase(fileExt))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tokenizes file names of all images in the directory.
     */
    private List<ImageTokensWithPath> parseImageTokens(List<File> imageFiles,
            File incomingDirectory, IImageReader imageReaderOrNull)
    {
        List<ImageTokensWithPath> imageTokensList = new ArrayList<ImageTokensWithPath>();

        for (File imageFile : imageFiles)
        {
            try
            {
                File file = new File(imageFile.getPath());
                List<ImageIdentifier> identifiers = imageProvider.getImageIdentifiers(imageReaderOrNull, file);
                String imageRelativePath = FileUtilities.getRelativeFilePath(incomingDirectory, file);
                ImageMetadata[] imageTokens =
                        simpleImageConfig.extractImagesMetadata(imageRelativePath, identifiers);
                if (imageTokens != null)
                {
                    for (ImageMetadata imageToken : imageTokens)
                    {
                        imageToken.ensureValid(simpleImageConfig.isMicroscopyData());
                        imageTokensList.add(new ImageTokensWithPath(imageToken, imageRelativePath));
                    }
                }
            } catch (Exception ex)
            {
                throw new UserFailureException("Error ocured when processing image " + imageFile.getPath(), ex);
            }
        }
        if (imageTokensList.isEmpty())
        {
            throw UserFailureException
                    .fromTemplate(
                            "No image tokens could be parsed from incoming directory '%s' for extensions %s!\n Method extractImagesMetadata did not return any image tokens for any of the input image files",
                            incomingDirectory.getPath(),
                            CollectionUtils.abbreviate(
                                    simpleImageConfig.getRecognizedImageExtensions(), -1));
        }
        return imageTokensList;
    }

    private List<File> extractImageFiles(File incoming)
    {
        List<File> imageFiles = listImageFiles(incoming);
        if (imageFiles.isEmpty())
        {
            throw UserFailureException.fromTemplate(
                    "Incoming directory '%s' contains no images with extensions %s!", incoming
                            .getPath(), CollectionUtils.abbreviate(
                            simpleImageConfig.getRecognizedImageExtensions(), -1));
        }
        return imageFiles;
    }

    // this method can have a side effect - it autodetects the image reader if only the library is
    // specified
    private IImageReader tryCreateAndSaveImageReader(List<File> imageFiles)
    {
        IImageReader readerOrNull = null;
        ImageLibraryInfo imageLibraryInfoOrNull = tryGetImageLibrary();
        if (imageLibraryInfoOrNull != null)
        {
            readerOrNull = tryCreateImageReader(imageFiles, imageLibraryInfoOrNull);
            if (readerOrNull != null)
            {
                // NOTE: ugly side effect which is used later on
                // if (null == imageLibraryInfoOrNull.getReaderName())
                // {
                imageLibraryInfoOrNull.setReaderName(readerOrNull.getName());
                // }
            } else
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Cannot find any reader for '%s' library.", imageLibraryInfoOrNull);
            }
        }
        return readerOrNull;
    }

    private IImageReader tryCreateImageReader(List<File> imageFiles,
            ImageLibraryInfo imageLibraryInfo)
    {
        if (imageFiles.isEmpty())
        {
            return null;
        }
        File imageFile = imageFiles.get(0);
        String libraryName = imageLibraryInfo.getName();
        String readerNameOrNull = imageLibraryInfo.getReaderName();
        if (readerNameOrNull != null)
        {
            return readerFactory.tryGetReader(libraryName, readerNameOrNull);
        } else
        {
            return readerFactory.tryGetReaderForFile(libraryName, imageFile.getPath());
        }
    }

    private ImageLibraryInfo tryGetImageLibrary()
    {
        return simpleImageConfig.getImageStorageConfiguration().tryGetImageLibrary();
    }

    /**
     * Creates ImageFileInfo for a given path to an image.
     */
    protected ImageFileInfo createImageInfo(ImageTokensWithPath imageTokens, Geometry tileGeometry)
    {
        Location tileCoords =
                simpleImageConfig.getTileCoordinates(imageTokens.getTileNumber(), tileGeometry);
        ImageFileInfo img =
                new ImageFileInfo(imageTokens.getChannelCode(), tileCoords.getRow(),
                        tileCoords.getColumn(), imageTokens.getImagePath());
        img.setTimepoint(imageTokens.tryGetTimepoint());
        img.setDepth(imageTokens.tryGetDepth());
        img.setSeriesNumber(imageTokens.tryGetSeriesNumber());
        img.setWell(imageTokens.getWell());
        img.setImageIdentifier(imageTokens.tryGetImageIdentifier());
        return img;
    }

    /**
     * @param imageTokensList list of ImageTokens for each image
     * @param tileGeometry describes the matrix of tiles (aka fields or sides) in the well
     */
    protected List<ImageFileInfo> createImageInfos(List<ImageTokensWithPath> imageTokensList,
            Geometry tileGeometry)
    {
        List<ImageFileInfo> images = new ArrayList<ImageFileInfo>();
        for (ImageTokensWithPath imageTokens : imageTokensList)
        {
            ImageFileInfo image = createImageInfo(imageTokens, tileGeometry);
            images.add(image);
        }
        return images;
    }

    private List<Channel> getAvailableChannels(List<ImageFileInfo> images)
    {
        Set<String> channelCodes = new LinkedHashSet<String>();
        for (ImageFileInfo image : images)
        {
            channelCodes.add(image.getChannelCode());
        }
        List<Channel> channels = new ArrayList<Channel>();
        for (String channelCode : channelCodes)
        {
            Channel channel = simpleImageConfig.createChannel(channelCode);
            channels.add(channel);
        }
        return channels;
    }

    private static int getMaxTileNumber(List<ImageTokensWithPath> imageTokensList)
    {
        int max = 0;
        for (ImageMetadata imageTokens : imageTokensList)
        {
            max = Math.max(max, imageTokens.getTileNumber());
        }
        return max;
    }

    // -------------------

    private void computeAndAppendCommonFixedIntensityRangeTransformation(
            List<ImageFileInfo> images, File incomingDir, List<Channel> channels,
            IImageReader readerOrNull)
    {
        final List<Channel> channelsForComputation =
                findChannelsToComputeFixedCommonIntensityRange(channels);
        final Map<String, IntensityRange> map =
                simpleImageConfig.getFixedIntensityRangeForAllImages();
        final IntensityRange defaultLevelsOrNull = map.get(null);
        for (Channel channel : channelsForComputation)
        {
            final IntensityRange levelsOrNull = map.get(channel.getCode());
            final Levels intensityRange =
                    levelsOrNull == null ? new Levels(defaultLevelsOrNull.getBlackPoint(),
                            defaultLevelsOrNull.getWhitePoint()) : new Levels(
                            levelsOrNull.getBlackPoint(), levelsOrNull.getWhitePoint());
            operationLog.info(String.format(
                    "Set intensity range for channel '%s' to fixed value: %s "
                            + "(incoming directory '%s').", channel.getCode(),
                    intensityRange.toString(), incomingDir.getName()));
            appendCommonIntensityRangeTransformation(channel, intensityRange);
        }
    }

    private List<Channel> findChannelsToComputeFixedCommonIntensityRange(List<Channel> channels)
    {
        final Map<String, ch.systemsx.cisd.openbis.dss.etl.dto.api.IntensityRange> map =
                simpleImageConfig.getFixedIntensityRangeForAllImages();
        final ch.systemsx.cisd.openbis.dss.etl.dto.api.IntensityRange defaultLevelOrNull =
                map.get(null);
        if (defaultLevelOrNull != null)
        {
            return channels;
        }
        final Set<String> channelCodesSet = map.keySet();
        List<Channel> chosenChannels = new ArrayList<Channel>();
        for (Channel channel : channels)
        {
            if (channelCodesSet.contains(channel.getCode()))
            {
                chosenChannels.add(channel);
            }
        }
        return chosenChannels;
    }

    private void computeAndAppendCommonIntensityRangeTransformation(List<ImageFileInfo> images,
            File incomingDir, List<Channel> channels, IImageReader readerOrNull)
    {
        final List<Channel> channelsForComputation =
                tryFindChannelsToComputeCommonIntensityRange(channels);
        if (channelsForComputation == null)
        {
            return;
        }
        for (Channel channel : channelsForComputation)
        {
            final String channelCode = channel.getCode();
            List<ImageFileInfo> channelImages = chooseChannelImages(images, channelCode);
            operationLog.info(String.format("Computing intensity range for channel '%s'. "
                    + "Found %d images for the channel in incoming directory '%s'.", channelCode,
                    channelImages.size(), incomingDir.getName()));
            final Levels intensityRange = tryComputeCommonIntensityRange(readerOrNull, channelImages,
                    incomingDir, simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesThreshold());
            if (intensityRange != null)
            {
                operationLog.info(String.format(
                        "Computed intensity range for channel '%s': %s (incoming directory '%s').",
                        channelCode, intensityRange.toString(), incomingDir.getName()));
                appendCommonIntensityRangeTransformation(channel, intensityRange);
            } else
            {
                operationLog
                        .warn(String
                                .format("Transformation cannot be generated for channel '%s' (incoming directory '%s').",
                                        channelCode, incomingDir.getName()));
            }
        }
    }

    private void appendCommonIntensityRangeTransformation(Channel channel, Levels intensityRange)
    {
        ImageTransformationBuffer buffer = new ImageTransformationBuffer();
        // append first
        String label = simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesLabel();
        ImageTransformation imageTransformation =
                buffer.appendRescaleGrayscaleIntensity(intensityRange.getMinLevel(),
                        intensityRange.getMaxLevel(), label);
        imageTransformation.setDescription(OPTIMAL_DATASET_INTENSITY_RESCALING_DESCRIPTION);
        boolean isDefault = simpleImageConfig.isComputeCommonIntensityRangeOfAllImagesDefault();
        imageTransformation.setDefault(isDefault);
        buffer.appendAll(channel.getAvailableTransformations());

        channel.setAvailableTransformations(buffer.getTransformations());
    }

    private static List<ImageFileInfo> chooseChannelImages(List<ImageFileInfo> images,
            String channelCode)
    {
        String normalizedChannelCode = CodeNormalizer.normalize(channelCode);
        List<ImageFileInfo> channelImages = new ArrayList<ImageFileInfo>();
        for (ImageFileInfo imageFileInfo : images)
        {
            String imageChannelCode = CodeNormalizer.normalize(imageFileInfo.getChannelCode());
            if (imageChannelCode.equals(normalizedChannelCode))
            {
                channelImages.add(imageFileInfo);
            }
        }
        return channelImages;
    }

    private List<Channel> tryFindChannelsToComputeCommonIntensityRange(List<Channel> channels)
    {
        List<String> channelCodes =
                simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesForChannels();
        if (channelCodes == null)
        {
            return null;
        }
        if (channelCodes.isEmpty())
        {
            return channels;
        }

        Set<String> channelCodesSet = createNormalizedCodesSet(channelCodes);
        List<Channel> chosenChannels = new ArrayList<Channel>();
        for (Channel channel : channels)
        {
            if (channelCodesSet.contains(channel.getCode()))
            {
                chosenChannels.add(channel);
            }
        }
        return chosenChannels;
    }

    private static Set<String> createNormalizedCodesSet(Collection<String> channelCodes)
    {
        final Set<String> normalizedCodes = new HashSet<String>();
        for (String code : channelCodes)
        {
            normalizedCodes.add(CodeNormalizer.normalize(code));
        }
        return normalizedCodes;
    }

    /**
     * Computes common intensity range for a list of files.
     * 
     * @return calculated levels or null if calculation couldn't succeed because some images where not in gray scale
     */
    private Levels tryComputeCommonIntensityRange(IImageReader readerOrNull,
            List<ImageFileInfo> channelImages, File incomingDir, float threshold)
    {
        String libraryName = (readerOrNull == null) ? null : readerOrNull.getLibraryName();
        String readerName = (readerOrNull == null) ? null : readerOrNull.getName();
        ImageLibraryInfo libraryInfo = new ImageLibraryInfo(libraryName, readerName);
        PixelHistogram histogram = new PixelHistogram();

        for (ImageFileInfo imageFileInfo : channelImages)
        {
            String imageId = imageFileInfo.tryGetUniqueStringIdentifier();
            File imageFile = new File(incomingDir, imageFileInfo.getImageRelativePath());
            String humanReadableImageId = String.format("image '%s' of image file '%s'", imageId, imageFile);
            try
            {
                FileBasedContentNode contentNode = new FileBasedContentNode(imageFile);
                BufferedImage image = imageProvider.getImage(contentNode, imageId, libraryInfo);
                if (IntensityRescaling.isNotGrayscale(image))
                {
                    operationLog.warn("Intensity range cannot be computed because " + humanReadableImageId
                            + " is not in grayscale.");
                    return null;
                }
                IntensityRescaling.addToLevelStats(histogram, DssScreeningUtils.createPixels(image),
                        ch.systemsx.cisd.common.image.IntensityRescaling.Channel.RED);
            } catch (Exception ex)
            {
                throw new UserFailureException("Error ocured when processing " + humanReadableImageId, ex);
            }
        }
        return IntensityRescaling.computeLevels(histogram, threshold);
    }

    // -------------------
    /**
     * Extracts all images from the incoming directory.
     * 
     * @param incoming - folder with images
     * @param dataset - here the result will be stored
     */
    private void setImageDataset(File incoming, ImageDataSetInformation dataset)
    {
        dataset.setDatasetTypeCode(simpleImageConfig.getDataSetType());
        dataset.setDataSetKind(DataSetKind.PHYSICAL);
        dataset.setFileFormatCode(simpleImageConfig.getFileFormatType());
        dataset.setMeasured(simpleImageConfig.isMeasuredData());

        dataset.setSample(simpleImageConfig.getPlateSpace(), simpleImageConfig.getProjectCode(), simpleImageConfig.getPlateCode());
        dataset.setIncomingDirectory(incoming);

        ImageDataSetStructure imageStruct = createImageDataSetStructure(incoming, dataset);
        dataset.setImageDataSetStructure(imageStruct);
    }

    private ImageDataSetStructure createImageDataSetStructure(File incoming,
            ImageDataSetInformation dataset)
    {
        List<ImageFileInfo> images = null;
        List<Channel> channels = null;
        Geometry tileGeometry = null;
        if (dataset.isGenerateOverviewImagesFromRegisteredImages())
        {
            images = new ArrayList<ImageFileInfo>();
            channels = new ArrayList<Channel>();
            String permId = dataset.tryGetContainerDatasetPermId();
            tileGeometry = Utils.gatherImageFileInfos(DssScreeningUtils.getQuery(), permId, images, channels);
        } else
        {
            List<File> imageFiles = extractImageFiles(incoming);
            IImageReader imageReaderOrNull = tryCreateAndSaveImageReader(imageFiles);
            List<ImageTokensWithPath> imageTokensList =
                    parseImageTokens(imageFiles, incoming, imageReaderOrNull);

            int maxTileNumber = getMaxTileNumber(imageTokensList);
            tileGeometry = simpleImageConfig.getTileGeometry(imageTokensList, maxTileNumber);

            images = createImageInfos(imageTokensList, tileGeometry);
            channels = getAvailableChannels(images);

            if (simpleImageConfig.isFixedIntensityRangeForAllImagesDefined())
            {
                computeAndAppendCommonFixedIntensityRangeTransformation(images, incoming, channels,
                        imageReaderOrNull);
            } else
            {
                computeAndAppendCommonIntensityRangeTransformation(images, incoming, channels,
                        imageReaderOrNull);
            }
        }

        ImageDataSetStructure imageStruct = new ImageDataSetStructure();
        imageStruct.setImages(images);
        imageStruct.setChannels(channels);
        imageStruct.setTileGeometry(tileGeometry.getNumberOfRows(),
                tileGeometry.getNumberOfColumns());

        imageStruct.setImageStorageConfiguraton(simpleImageConfig.getImageStorageConfiguration());
        return imageStruct;
    }

    private <T extends DataSetInformation> void setRegistrationDetails(
            DataSetRegistrationDetails<T> registrationDetails, T dataset)
    {
        registrationDetails.setDataSetInformation(dataset);
        registrationDetails.setFileFormatType(new FileFormatType(simpleImageConfig
                .getFileFormatType()));
        registrationDetails.setDataSetType(new DataSetType(simpleImageConfig.getDataSetType()));
        registrationDetails.setDataSetKind(DataSetKind.PHYSICAL);
        registrationDetails.setMeasuredData(simpleImageConfig.isMeasuredData());

    }

}
