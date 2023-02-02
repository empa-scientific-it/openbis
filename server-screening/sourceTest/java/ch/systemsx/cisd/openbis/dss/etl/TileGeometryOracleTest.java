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

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.geometry.SpatialPoint;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * @author Kaloyan Enimanev
 */
public class TileGeometryOracleTest extends AssertJUnit
{

    @Test
    public void testSimple()
    {
        Map<Integer, SpatialPoint> tileToXYMap = new HashMap<Integer, SpatialPoint>();
        tileToXYMap.put(0, new SpatialPoint(4.44E-4, 6.61E-4));
        tileToXYMap.put(1, new SpatialPoint(-4.44E-4, 6.61E-4));
        tileToXYMap.put(2, new SpatialPoint(-4.44E-4, -6.61E-4));
        tileToXYMap.put(3, new SpatialPoint(4.44E-4, -6.61E-4));

        Map<Integer, Location> locationMap =
                TileGeometryOracle.tryFigureLocations(tileToXYMap, 1e-7);

        assertEquals(new Location(1, 2), locationMap.get(0));
        assertEquals(new Location(1, 1), locationMap.get(1));
        assertEquals(new Location(2, 1), locationMap.get(2));
        assertEquals(new Location(2, 2), locationMap.get(3));

        Geometry expectedGeometry = Geometry.createFromRowColDimensions(2, 2);
        Geometry geometry = TileGeometryOracle.figureGeometry(locationMap.values());
        assertEquals(expectedGeometry, geometry);
    }

}
