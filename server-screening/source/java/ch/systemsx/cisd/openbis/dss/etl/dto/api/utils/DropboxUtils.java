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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.utils;

import java.util.Collection;
import java.util.Map;

import ch.systemsx.cisd.common.geometry.SpatialPoint;
import ch.systemsx.cisd.openbis.dss.etl.TileGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;

/**
 * @author Tomasz Pylak
 */
public class DropboxUtils
{
    /**
     * Translates a row number into letter code. Thus, 1 -> A, 2 -> B, 26 -> Z, 27 -> AA, 28 -> AB, etc.
     */
    public static String translateRowNumberIntoLetterCode(int rowNumber)
    {
        return PlateUtils.translateRowNumberIntoLetterCode(rowNumber);
    }

    /**
     * Figures the tile geometry based on all locations present in the it.
     */
    public static Geometry figureGeometry(Collection<Location> tileLocations)
    {
        return TileGeometryOracle.figureGeometry(tileLocations);
    }

    /**
     * Tries to figure out tile locations based on their spatial coordinates.
     * <p>
     * Two spatial points (x1, y1) and (x2, y2) are assumed lie in the same tile when abs(x1-x2) < epsilon and abs(y1-y2) < epsilon.
     * 
     * @param epsilon see the javadoc of the method
     */
    public static Map<Integer/* tile number */, Location> tryFigureLocations(
            Map<Integer/* tile number */, SpatialPoint> tileToSpatialPointMap, double epsilon)
    {
        return TileGeometryOracle.tryFigureLocations(tileToSpatialPointMap, epsilon);
    }
}
