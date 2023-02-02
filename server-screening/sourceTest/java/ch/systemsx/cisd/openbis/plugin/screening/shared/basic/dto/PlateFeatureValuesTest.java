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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link PlateFeatureValues}.
 * 
 * @author Bernd Rinn
 */
public class PlateFeatureValuesTest
{

    @Test
    public static void testAccessInBothCoordinateSystems()
    {
        final int rows = 3;
        final int cols = 4;
        final PlateFeatureValues values =
                new PlateFeatureValues(Geometry.createFromRowColDimensions(rows, cols));
        final int dimX = values.getGeometry().getDimX();
        final int dimY = values.getGeometry().getDimY();
        assertEquals(dimX, 4);
        assertEquals(dimY, 3);
        for (int row = 1; row <= rows; ++row)
        {
            for (int col = 1; col <= cols; ++col)
            {
                values.setForWellLocation(funcForRowCol(row, col), row, col);
            }
        }
        for (int x = 0; x < dimX; ++x)
        {
            for (int y = 0; y < dimY; ++y)
            {
                assertEquals(funcForXY(dimY, x, y), values.getForCartesianCoordinates(x, y));
            }
        }
    }

    @Test
    public static void testPersistence()
    {
        final int rows = 3;
        final int cols = 4;
        final PlateFeatureValues values =
                new PlateFeatureValues(Geometry.createFromRowColDimensions(rows, cols));
        final int dimX = values.getGeometry().getDimX();
        final int dimY = values.getGeometry().getDimY();
        assertEquals(dimX, 4);
        assertEquals(dimY, 3);
        for (int row = 1; row <= rows; ++row)
        {
            for (int col = 1; col <= cols; ++col)
            {
                values.setForWellLocation(funcForRowCol(row, col), row, col);
            }
        }
        final byte[] persistentForm = values.toByteArray();
        final PlateFeatureValues values2 = new PlateFeatureValues(persistentForm);
        assertEquals(values, values2);
        assertEquals(values.getGeometry(), values2.getGeometry());
    }

    private static float funcForXY(final int dimY, int x, int y)
    {
        return (dimY - y) * (x + 1) + (dimY - y) / (float) (x + 1);
    }

    private static float funcForRowCol(int row, int col)
    {
        return row * col + row / (float) col;
    }

}
