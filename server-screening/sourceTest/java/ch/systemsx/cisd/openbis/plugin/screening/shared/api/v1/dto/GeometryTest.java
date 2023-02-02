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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the factory functions of {@link Geometry}.
 * 
 * @author Bernd Rinn
 */
public class GeometryTest
{

    @Test
    public void testFactoryMethods()
    {
        assertEquals(16, Geometry.createFromRowColDimensions(16, 24).getNumberOfRows());
        assertEquals(24, Geometry.createFromRowColDimensions(16, 24).getNumberOfColumns());
        assertEquals(Geometry.GEOMETRY_384_16X24, Geometry.createFromRowColDimensions(16, 24));
        assertEquals(Geometry.createFromCartesianDimensions(24, 16), Geometry
                .createFromRowColDimensions(16, 24));
        assertEquals(Geometry.createFromCartesianDimensions(new int[]
        { 24, 16 }), Geometry.createFromCartesianDimensions(24, 16));
        assertEquals(Geometry.createFromPlateGeometryString("384_16X24"), Geometry
                .createFromRowColDimensions(16, 24));
        assertFalse(Geometry.GEOMETRY_384_16X24.equals(Geometry.createFromRowColDimensions(24, 16)));
    }

}
