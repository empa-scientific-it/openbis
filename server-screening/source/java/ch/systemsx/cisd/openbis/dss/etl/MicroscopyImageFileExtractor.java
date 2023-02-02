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

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * {@link IImageFileExtractor} implementation for microscopy images.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyImageFileExtractor extends AbstractImageFileExtractor
{

    public MicroscopyImageFileExtractor(Properties properties)
    {
        super(properties, true);
        if (this.tileMapperOrNull == null)
        {
            throw new ConfigurationFailureException("Tile mapping property not defined: "
                    + TILE_MAPPING_PROPERTY);
        }
    }

    @Override
    protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
            SampleIdentifier datasetSample)
    {
        UnparsedImageFileInfo unparsedInfo =
                UnparsedImageFileInfoLexer.tryExtractMicroscopyImageFileInfo(imageFile,
                        incomingDataSetDirectory);
        if (unparsedInfo == null)
        {
            return null;
        }

        // extract tile
        Location tileLocation = null;
        Integer tileNumber = tryAsInt(unparsedInfo.getTileLocationToken());
        if (tileNumber != null)
        {
            tileLocation = tryGetTileLocation(tileNumber);
        }
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

        Float timepointOrNull = tryAsFloat(unparsedInfo.getTimepointToken());
        info.setTimepoint(timepointOrNull);
        Float depthOrNull = tryAsFloat(unparsedInfo.getDepthToken());
        info.setDepth(depthOrNull);
        Integer seriesNumberOrNull = tryAsInt(unparsedInfo.getSeriesNumberToken());
        info.setSeriesNumber(seriesNumberOrNull);

        return info;

    }

}
