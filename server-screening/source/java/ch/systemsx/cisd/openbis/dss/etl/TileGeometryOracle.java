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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.systemsx.cisd.common.geometry.SpatialPoint;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Utility methods that figure out tile locations/tile geometry based on spatial coordinates of the tiles.
 * 
 * @author Kaloyan Enimanev
 */
public class TileGeometryOracle
{
    /**
     * Figures the tile geometry based on all locations present in the it.
     */
    public static Geometry figureGeometry(Collection<Location> tileLocations)
    {
        int maxRow = 0;
        int maxCol = 0;
        for (Location location : tileLocations)
        {
            if (location.getRow() > maxRow)
            {
                maxRow = location.getRow();
            }
            if (location.getColumn() > maxCol)
            {
                maxCol = location.getColumn();
            }
        }

        return Geometry.createFromRowColDimensions(maxRow, maxCol);
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
        List<Double> xCoords = new ArrayList<Double>();
        List<Double> yCoords = new ArrayList<Double>();

        for (SpatialPoint point : tileToSpatialPointMap.values())
        {
            addIfNotPresent(xCoords, point.getX(), epsilon);
            addIfNotPresent(yCoords, point.getY(), epsilon);
        }

        Collections.sort(xCoords);
        Collections.sort(yCoords);

        Map<Integer, Location> result = new HashMap<Integer, Location>();
        for (Entry<Integer, SpatialPoint> entry : tileToSpatialPointMap.entrySet())
        {
            Integer tileNumber = entry.getKey();
            Location location = extractLocation(entry.getValue(), xCoords, yCoords, epsilon);
            result.put(tileNumber, location);
        }
        return result;

    }

    private static void addIfNotPresent(List<Double> values, Double value, double epsilon)
    {
        int idx = findIdxByEpsilon(values, value, epsilon);
        if (idx < 0)
        {
            values.add(value);
        }
    }

    private static int findIdxByEpsilon(List<Double> values, double toFind, double epsilon)
    {
        for (int idx = 0; idx < values.size(); idx++)
        {
            if (Math.abs(values.get(idx) - toFind) < epsilon)
            {
                return idx;
            }
        }
        return -1;
    }

    private static Location extractLocation(SpatialPoint point, List<Double> xCoords,
            List<Double> yCoords, double epsilon)
    {

        int locationX = findIdxByEpsilon(xCoords, point.getX(), epsilon);
        int locationY = findIdxByEpsilon(yCoords, point.getY(), epsilon);

        int row = yCoords.size() - locationY;
        int column = locationX + 1;
        Location location = new Location(row, column);
        return location;
    }

}
