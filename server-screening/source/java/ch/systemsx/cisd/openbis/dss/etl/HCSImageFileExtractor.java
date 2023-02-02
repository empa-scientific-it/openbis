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
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Each image name should adhere to the schema:<br>
 * 
 * <pre>
 * &lt;any-text&gt;_&lt;plate-code&gt;_&lt;well-code&gt;_&lt;tile-code&gt;_&lt;channel-name&gt;.&lt;allowed-image-extension&gt;
 * </pre>
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractImageFileExtractor
{
    // boolean property, if true the names of the plate in file name and directory name have to
    // match.
    // True by default.
    private static final String CHECK_PLATE_NAME_FLAG_PROPERTY_NAME = "validate-plate-name";

    private final boolean shouldValidatePlateName;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties, extractSkipChannelsWithoutImages(properties));
        this.shouldValidatePlateName =
                PropertyUtils.getBoolean(properties, CHECK_PLATE_NAME_FLAG_PROPERTY_NAME, true);
    }

    private static boolean extractSkipChannelsWithoutImages(Properties properties)
    {
        return PropertyUtils.getBoolean(properties,
                PlateStorageProcessor.CHANNELS_PER_EXPERIMENT_PROPERTY, true) == false;
    }

    /**
     * Extracts the plate location from argument. Returns <code>null</code> if the operation fails.
     */
    protected static Location tryGetWellLocation(final String plateLocation)
    {
        return Location.tryCreateLocationFromTransposedMatrixCoordinate(plateLocation);
    }

    /**
     * Extracts the well location from given token. Returns <code>null</code> if the operation fails.<br>
     * Can be overwritten in the subclasses if they use {@link #tryExtractHCSImageInfo(UnparsedImageFileInfo, File, File)} internally.
     */
    protected Location tryGetTileLocation(final String wellLocation)
    {
        Integer tileNumber = tryAsInt(wellLocation);
        if (tileNumber == null)
        {
            return null;
        }
        Location tileLoc = tryGetTileLocation(tileNumber);
        if (tileLoc == null)
        {
            tileLoc = Location.tryCreateLocationFromRowwisePosition(tileNumber, tileGeometry);
        }
        return tileLoc;
    }

    /**
     * Splits specified image file name into at least four tokens. Only the last four tokens will be considered. They are sample code, plate location,
     * well location, and channel. Note, that sample code could be <code>null</code>.
     * 
     * @param shouldValidatePlateName if true it will be checked if the plate code in the file name matches the datasetSample plate code.
     * @return <code>null</code> if the argument could not be splitted into tokens.
     */
    private final static UnparsedImageFileInfo tryExtractImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample,
            boolean shouldValidatePlateName)
    {
        final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
        final String[] tokens = StringUtils.split(baseName, TOKEN_SEPARATOR);
        if (tokens == null || tokens.length < 4)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(IMAGE_FILE_NOT_ENOUGH_ENTITIES, imageFile));
            }
            return null;
        }
        final String sampleCode = tokens[tokens.length - 4];
        if (shouldValidatePlateName && sampleCode != null
                && sampleCode.equalsIgnoreCase(datasetSample.getSampleCode()) == false)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE, imageFile,
                        datasetSample, sampleCode));
            }
            return null;
        }
        String channelToken = tokens[tokens.length - 1];
        if (StringUtils.isBlank(channelToken))
        {
            operationLog.info("Channel token is empty for image: " + imageFile);
            return null;
        }

        UnparsedImageFileInfo info = new UnparsedImageFileInfo();
        info.setWellLocationToken(tokens[tokens.length - 3]);
        info.setTileLocationToken(tokens[tokens.length - 2]);
        info.setChannelToken(channelToken);
        info.setTimepointToken(null);
        info.setDepthToken(null);

        return info;
    }

    @Override
    /** Default implementation, can be overwritten in subclasses. */
    protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
            SampleIdentifier datasetSample)
    {
        UnparsedImageFileInfo unparsedInfo =
                tryExtractImageInfo(imageFile, incomingDataSetDirectory, datasetSample,
                        shouldValidatePlateName);
        if (unparsedInfo == null)
        {
            return null;
        }
        return tryExtractHCSImageInfo(unparsedInfo, imageFile, incomingDataSetDirectory);
    }

    protected final ImageFileInfo tryExtractHCSImageInfo(UnparsedImageFileInfo unparsedInfo,
            File imageFile, File incomingDataSetDirectory)
    {
        assert unparsedInfo != null;

        Location tileLocation = tryGetTileLocation(unparsedInfo.getTileLocationToken());
        if (tileLocation == null)
        {
            operationLog.info("Cannot extract tile location (a.k.a. tile/field/side) from token "
                    + unparsedInfo.getTileLocationToken());
            return null;
        }
        String channelCode = CodeNormalizer.normalize(unparsedInfo.getChannelToken());
        String imageRelativePath = getRelativeImagePath(incomingDataSetDirectory, imageFile);

        ImageFileInfo info =
                new ImageFileInfo(channelCode, tileLocation.getY(), tileLocation.getX(),
                        imageRelativePath);

        boolean ok = info.setWell(unparsedInfo.getWellLocationToken());
        if (ok == false)
        {
            operationLog.info("Cannot extract well location from token "
                    + unparsedInfo.getWellLocationToken());
            return null;
        }

        Float timepointOrNull = tryAsFloat(unparsedInfo.getTimepointToken());
        info.setTimepoint(timepointOrNull);
        Float depthOrNull = tryAsFloat(unparsedInfo.getDepthToken());
        info.setDepth(depthOrNull);
        Integer seriesNumberOrNull = tryAsInt(unparsedInfo.getSeriesNumberToken());
        info.setSeriesNumber(seriesNumberOrNull);

        return info;
    }
}
