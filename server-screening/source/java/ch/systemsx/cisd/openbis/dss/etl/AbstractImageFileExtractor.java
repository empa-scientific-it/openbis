/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.hcs.WellGeometry;
import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Abstract superclass for {@link IImageFileExtractor} implementations.<br>
 * <br>
 * Assumes that images names have a file extension present in {@link ImageFileExtractorUtils#IMAGE_EXTENSIONS} constant. <br>
 * <br>
 * If 'extract-single-image-channels' property is specified for storage processor then the channels are extracted from the color components and the
 * channel in the image file name is ignored.
 * <p>
 * Deprecated, use python dropboxes instead! Image datasets registered with the extractor 1. cannot have any thumbnails and 2. will not be saved in
 * 'original' directory!
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractImageFileExtractor implements IImageFileExtractor
{
    /**
     * Extracts {@link ImageFileInfo} for a given image file. Should log all the syntax problems in image names.
     */
    abstract protected ImageFileInfo tryExtractImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample);

    // -----------

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractImageFileExtractor.class);

    protected static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Information about the image could not be extracted for the file '%s'.";

    protected static final String IMAGE_FILE_NOT_ENOUGH_ENTITIES =
            "The name of image file '%s' could not be splitted into enough entities.";

    protected static final String IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE =
            "Image file '%s' belongs to the wrong sample [expected=%s,found=%s].";

    protected static final String IMAGE_FILE_ACCEPTED = "Image file '%s' was accepted: %s.";

    // --------

    // tiles geometry, e.g. 3x4 if the well is divided into 12 tiles (3 rows, 4 columns)
    public static final String TILE_GEOMETRY_PROPERTY = "well_geometry";

    // comma separated list of channel names, order matters
    @Deprecated
    public static final String CHANNEL_NAMES = "channel-names";

    // comma separated list of channel codes, order matters
    public static final String CHANNEL_CODES = "channel-codes";

    // comma separated list of channel labels, order matters
    public static final String CHANNEL_LABELS = "channel-labels";

    // Allows to create channels from single color components of the image.
    // Optional, type: list of the color components (RED, GREEN, BLUE),
    // in the same order as corresponding channel names.
    protected static final String EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY =
            "extract-single-image-channels";

    protected static final String TILE_MAPPING_PROPERTY = "tile_mapping";

    protected static final char TOKEN_SEPARATOR = '_';

    // -----------------------------------------

    private final List<ChannelDescription> channelDescriptionsOrNull;

    private final List<ColorComponent> channelColorComponentsOrNull;

    private final boolean skipChannelsWithoutImages;

    protected final TileMapper tileMapperOrNull;

    protected final Geometry tileGeometry;

    protected AbstractImageFileExtractor(Properties properties, boolean skipChannelsWithoutImages)
    {
        this(tryExtractChannelDescriptions(properties), getWellGeometry(properties),
                skipChannelsWithoutImages, properties);
    }

    /**
     * @param skipChannelsWithoutImages if true channel names are derived from a set of channel codes in the extracted images. In this way we do not
     *            restrict available channel codes and each channel has at least one image. Channel labels are taken from channel descriptions anyway.
     *            Should be set to true only for microscopy, in HCS each dataset of one experiment should have the same set of channels even if they
     *            are not present in some datasets (exceptions: image overlays, test screens).
     */
    protected AbstractImageFileExtractor(List<ChannelDescription> channelDescriptionsOrNull,
            Geometry tileGeometry, boolean skipChannelsWithoutImages, Properties properties)
    {
        assert tileGeometry != null : "wel geometry is null";

        this.tileGeometry = tileGeometry;

        this.channelDescriptionsOrNull = channelDescriptionsOrNull;
        this.channelColorComponentsOrNull = tryGetChannelComponents(properties);
        this.skipChannelsWithoutImages = skipChannelsWithoutImages;
        checkChannelsAndColorComponents();

        this.tileMapperOrNull =
                TileMapper.tryCreate(properties.getProperty(TILE_MAPPING_PROPERTY), tileGeometry);
    }

    protected final static Geometry getMandatoryTileGeometry(Properties properties)
    {
        String spotGeometryText =
                PropertyUtils.getMandatoryProperty(properties, TILE_GEOMETRY_PROPERTY);
        return Geometry.createFromString(spotGeometryText);
    }

    private static Geometry getWellGeometry(final Properties properties)
    {
        final String property = properties.getProperty(WellGeometry.WELL_GEOMETRY);
        if (property == null)
        {
            throw new ConfigurationFailureException(String.format(
                    "No '%s' property has been specified.", WellGeometry.WELL_GEOMETRY));
        }
        final Geometry geometry = WellGeometry.createFromString(property);
        if (geometry == null)
        {
            throw new ConfigurationFailureException(String.format(
                    "Could not create a geometry from property value '%s'.", property));
        }
        return geometry;
    }

    protected final Location tryGetTileLocation(int tileNumber)
    {
        if (tileMapperOrNull != null)
        {
            return tileMapperOrNull.tryGetLocation(tileNumber);
        } else
        {
            return null;
        }
    }

    private void checkChannelsAndColorComponents()
    {
        if (skipChannelsWithoutImages == false && channelDescriptionsOrNull == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate("Expected channels are not specified and extraction of channels from images is switched off!");
        }
        if (channelColorComponentsOrNull != null && channelDescriptionsOrNull == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate("Channels are not specified although color components are given!");
        }

        if (channelColorComponentsOrNull != null && channelDescriptionsOrNull != null
                && channelColorComponentsOrNull.size() != channelDescriptionsOrNull.size())
        {
            throw ConfigurationFailureException.fromTemplate(
                    "There should be exactly one color component for each channel name."
                            + " Correct the list of values for '%s' property.",
                    AbstractImageFileExtractor.EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY);
        }
    }

    @Override
    public ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractionResult extract(
            File incomingDataSetDirectory, DataSetInformation dataSetInformation)
    {
        List<File> invalidFiles = new LinkedList<File>();
        List<AcquiredSingleImage> acquiredImages = new ArrayList<AcquiredSingleImage>();
        List<File> imageFiles = ImageFileExtractorUtils.listImageFiles(incomingDataSetDirectory);
        for (final File imageFile : imageFiles)
        {
            InterruptedExceptionUnchecked.check();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Processing image file '%s'", imageFile));
            }
            ImageFileInfo imageInfo =
                    tryExtractImageInfo(imageFile, incomingDataSetDirectory,
                            dataSetInformation.getSampleIdentifier());

            if (imageInfo != null)
            {
                List<AcquiredSingleImage> newImages = getImages(imageInfo);
                acquiredImages.addAll(newImages);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_ACCEPTED, imageFile, imageInfo));
                }
            } else
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(IMAGE_FILE_NOT_STANDARDIZABLE, imageFile));
                }
                invalidFiles.add(imageFile);
            }
        }
        File datasetRelativeImagesFolderPath =
                new File(ScreeningConstants.ORIGINAL_DATA_DIR + File.separator
                        + incomingDataSetDirectory.getName());
        return new ImageFileExtractionResult(acquiredImages, datasetRelativeImagesFolderPath,
                Collections.unmodifiableList(invalidFiles), getAllChannels(acquiredImages),
                tileGeometry, null, null);

    }

    private List<AcquiredSingleImage> getImages(ImageFileInfo imageInfo)
    {
        checkChannelsAndColorComponents();

        if (channelColorComponentsOrNull != null && channelDescriptionsOrNull != null)
        {
            List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                ChannelDescription channelDescription = channelDescriptionsOrNull.get(i);
                images.add(createImage(imageInfo, channelDescription.getCode(), colorComponent,
                        null));
            }
            return images;
        } else
        {
            ensureChannelExist(channelDescriptionsOrNull, imageInfo.getChannelCode());
            return createImagesWithNoColorComponent(imageInfo, null);
        }
    }

    private List<Channel> getAllChannels(List<AcquiredSingleImage> acquiredImages)
    {
        if (channelDescriptionsOrNull != null && skipChannelsWithoutImages == false)
        {
            return createChannels(channelDescriptionsOrNull);
        } else
        {
            if (skipChannelsWithoutImages)
            {
                return createChannels(extractChannelDescriptions(acquiredImages,
                        channelDescriptionsOrNull));
            } else
            {
                throw new IllegalStateException(
                        "extractChannelsFromImages is false, channelDescriptionsOrNull is null");
            }
        }
    }

    private static List<ChannelDescription> extractChannelDescriptions(
            List<AcquiredSingleImage> acquiredImages,
            List<ChannelDescription> channelDescriptionsOrNull)
    {
        Map<String, String> channelCodeToLabel =
                createChannelCodeToLabelMap(channelDescriptionsOrNull);
        Set<String> channelCodes = new HashSet<String>();
        for (AcquiredSingleImage image : acquiredImages)
        {
            channelCodes.add(image.getChannelCode());
        }

        List<ChannelDescription> descs = new ArrayList<ChannelDescription>();
        for (String channelCode : channelCodes)
        {
            String label = channelCodeToLabel.get(channelCode);
            ChannelDescription desc;
            if (label != null)
            {
                desc = new ChannelDescription(channelCode, label);
            } else
            {
                desc = new ChannelDescription(channelCode);
            }
            descs.add(desc);
        }
        return descs;
    }

    private static Map<String, String> createChannelCodeToLabelMap(
            List<ChannelDescription> channelDescriptionsOrNull)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (channelDescriptionsOrNull == null)
        {
            return map;
        }
        for (ChannelDescription desc : channelDescriptionsOrNull)
        {
            map.put(desc.getCode(), desc.getLabel());
        }
        return map;
    }

    // ------- static helper methods

    protected final static String getRelativeImagePath(File incomingDataSetDirectory,
            final File imageFile)
    {
        String imageRelativePath =
                FileUtilities.getRelativeFilePath(incomingDataSetDirectory,
                        new File(imageFile.getPath()));
        assert imageRelativePath != null : "Image relative path should not be null.";
        return imageRelativePath;
    }

    protected final static List<ColorComponent> tryGetChannelComponents(final Properties properties)
    {
        List<String> componentNames =
                PropertyUtils.tryGetList(properties, EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY);
        if (componentNames == null)
        {
            return null;
        }
        List<ColorComponent> components = new ArrayList<ColorComponent>();
        for (String name : componentNames)
        {
            components.add(ColorComponent.valueOf(name));
        }
        return components;
    }

    protected final static List<Channel> createChannels(List<ChannelDescription> channelDescriptions)
    {
        assert channelDescriptions != null : "channelDescriptions is null";

        List<Channel> channels = new ArrayList<Channel>();
        for (ChannelDescription channelDescription : channelDescriptions)
        {
            channels.add(new Channel(channelDescription.getCode(), channelDescription.getLabel()));
        }
        return channels;
    }

    private final static List<ChannelDescription> tryExtractChannelDescriptions(
            final Properties properties)
    {
        List<String> names = PropertyUtils.tryGetList(properties, CHANNEL_NAMES);
        List<String> codes = PropertyUtils.tryGetList(properties, CHANNEL_CODES);
        List<String> labels = tryGetListOfLabels(properties, CHANNEL_LABELS);
        if (names != null && (codes != null || labels != null))
        {
            throw new ConfigurationFailureException(String.format(
                    "Configure either '%s' or ('%s','%s') but not both.", CHANNEL_NAMES,
                    CHANNEL_CODES, CHANNEL_LABELS));
        }
        if (names != null)
        {
            List<ChannelDescription> descriptions = new ArrayList<ChannelDescription>();
            for (String name : names)
            {
                descriptions.add(new ChannelDescription(name));
            }
            return descriptions;
        }
        if (codes == null || labels == null)
        {
            return null;
        }
        if (codes.size() != labels.size())
        {
            throw new ConfigurationFailureException(String.format(
                    "Number of configured '%s' should be the same as number of '%s'.",
                    CHANNEL_CODES, CHANNEL_LABELS));
        }
        List<ChannelDescription> descriptions = new ArrayList<ChannelDescription>();
        for (int i = 0; i < codes.size(); i++)
        {
            descriptions.add(new ChannelDescription(codes.get(i), labels.get(i)));
        }
        return descriptions;
    }

    private final static List<String> tryGetListOfLabels(Properties properties, String propertyKey)
    {
        String itemsList = PropertyUtils.getProperty(properties, propertyKey);
        if (itemsList == null)
        {
            return null;
        }
        String[] items = itemsList.split(",");
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim();
        }
        return Arrays.asList(items);
    }

    protected final static void ensureChannelExist(
            List<ChannelDescription> channelDescriptionsOrNull, String channelCode)
    {
        if (channelDescriptionsOrNull == null)
        {
            return;
        }
        for (ChannelDescription channelDescription : channelDescriptionsOrNull)
        {
            if (channelDescription.getCode().equalsIgnoreCase(channelCode))
            {
                return;
            }
        }
        throw UserFailureException.fromTemplate(
                "Channel '%s' is not one of: %s. Change the configuration.", channelCode,
                channelDescriptionsOrNull);
    }

    public final static List<AcquiredSingleImage> createImagesWithNoColorComponent(
            ImageFileInfo imageInfo, ThumbnailsInfo thumbnailFilePathsOrNull)
    {
        List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
        images.add(createImage(imageInfo, imageInfo.getChannelCode(), null,
                thumbnailFilePathsOrNull));
        return images;
    }

    public final static AcquiredSingleImage createImage(ImageFileInfo imageInfo,
            String channelCode, ColorComponent colorComponentOrNull,
            ThumbnailsInfo thumbnailFilePathsOrNull)
    {
        RelativeImageReference relativeImageRef =
                new RelativeImageReference(imageInfo.getImageRelativePath(),
                        imageInfo.tryGetUniqueStringIdentifier(), colorComponentOrNull);

        RelativeImageReference relativeThumbnailRef = null;
        if (thumbnailFilePathsOrNull != null)
        {
            String relativeThumbnailPath =
                    thumbnailFilePathsOrNull.getThumbnailPath(RelativeImageFile.create(imageInfo),
                            colorComponentOrNull);
            relativeThumbnailRef = new RelativeImageReference(relativeThumbnailPath, null, null);
        }

        Location wellLoc = null;
        if (imageInfo.hasWellLocation())
        {
            wellLoc =
                    Location.createLocationFromRowAndColumn(imageInfo.tryGetWellRow(),
                            imageInfo.tryGetWellColumn());
        }
        Location tileLoc =
                Location.createLocationFromRowAndColumn(imageInfo.getTileRow(),
                        imageInfo.getTileColumn());
        return new AcquiredSingleImage(wellLoc, tileLoc, channelCode, imageInfo.tryGetTimepoint(),
                imageInfo.tryGetDepth(), imageInfo.tryGetSeriesNumber(), relativeImageRef,
                relativeThumbnailRef);
    }

    protected static Integer tryAsInt(String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return null;
        }
        try
        {
            return Integer.parseInt(valueOrNull);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    protected static Float tryAsFloat(String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return null;
        }
        try
        {
            return Float.parseFloat(valueOrNull);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
