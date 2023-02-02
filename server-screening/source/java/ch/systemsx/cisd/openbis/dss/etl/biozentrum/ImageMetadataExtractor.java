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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.geometry.SpatialPoint;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.TileGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;

/**
 * @author Kaloyan Enimanev
 */
public class ImageMetadataExtractor
{

    static final String POSITION_X_PROP = "stage-position-x";

    static final String POSITION_Y_PROP = "stage-position-y";

    public static Map<String, Object> extractMetadata(File imageFile)
    {
        IImageReader imageReader =
                ImageReaderFactory.tryGetReaderForFile(
                        ImageReaderConstants.BIOFORMATS_LIBRARY,
                        imageFile.getAbsolutePath());
        return imageReader.readMetaData(imageFile, ImageID.NULL, null);
    }

    /**
     * @param tileToMetadataMap mapping from tile number to image metadata
     */
    public static Map<Integer/* tile number */, Location> tryGetTileMapping(
            Map<Integer/* tile number */, Map<String/* name */, Object/* value */>> tileToMetadataMap,
            double epsilon)
    {
        Map<Integer, SpatialPoint> tileToSpatialPointMap = new HashMap<Integer, SpatialPoint>();
        for (Integer tile : tileToMetadataMap.keySet())
        {
            Map<String, Object> metadata = tileToMetadataMap.get(tile);
            SpatialPoint point = new SpatialPoint(extractXCoord(metadata), extractYCoord(metadata));
            tileToSpatialPointMap.put(tile, point);
        }
        return TileGeometryOracle.tryFigureLocations(tileToSpatialPointMap, epsilon);
    }

    private static Double extractXCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_X_PROP);
    }

    private static Double extractYCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_Y_PROP);
    }

    private static Double extractNumber(Map<String, Object> metadata, String propName)
    {
        String numberAsString = (String) metadata.get(propName);
        try
        {
            return Double.valueOf(numberAsString);
        } catch (NumberFormatException nfe)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot parse number for property %s with value %s", propName, numberAsString);
        }
    }

}
