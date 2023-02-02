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

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Test cases from {@link PlateGeometryOracle}.
 * 
 * @author Izabela Adamczyk
 */
public class PlateGeometryOracleTest extends AssertJUnit
{

    private static final String GEOMETRY_3X4 = "12_3X4";

    private static final String GEOMETRY_12_WELLS_3X4 = "12_WELLS_3X4";

    private static final String GEOMETRY_12_WELLS_4X3 = "12_WELLS_4X3";

    private static final String GEOMETRY_16_WELLS_4X4 = "16_WELLS_4X4";

    private static final String GEOMETRY_1200_WELLS_40X30 = "1200_WELLS_40X30";

    @Test
    public void testTwoSimilarMatchingGeometriesGetFirst() throws Exception
    {
        assertEquals(
                GEOMETRY_12_WELLS_3X4,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(1, 1)),
                        Arrays.asList(GEOMETRY_12_WELLS_3X4, GEOMETRY_12_WELLS_4X3)));
        assertEquals(
                GEOMETRY_12_WELLS_4X3,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(1, 1)),
                        Arrays.asList(GEOMETRY_12_WELLS_4X3, GEOMETRY_12_WELLS_3X4)));
    }

    @Test
    public void testGetSmallerGeometry() throws Exception
    {
        assertEquals(
                GEOMETRY_12_WELLS_3X4,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(1, 1)),
                        Arrays.asList(GEOMETRY_1200_WELLS_40X30, GEOMETRY_12_WELLS_3X4)));
    }

    @Test
    public void testGetSmallestMatchingGeometry() throws Exception
    {
        assertEquals(
                GEOMETRY_1200_WELLS_40X30,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(4, 3)),
                        Arrays.asList(GEOMETRY_12_WELLS_3X4, GEOMETRY_1200_WELLS_40X30)));
    }

    @Test
    public void testGetSmallestMatchingGeometryForManyLocations() throws Exception
    {
        assertEquals(GEOMETRY_12_WELLS_4X3, PlateGeometryOracle.figureGeometry(Arrays.asList(
                new WellLocation(1, 1), new WellLocation(4, 3)), Arrays.asList(
                GEOMETRY_12_WELLS_3X4, GEOMETRY_12_WELLS_4X3, GEOMETRY_1200_WELLS_40X30)));
    }

    @Test
    public void testGetSmallestMatchingGeometryForManyLocationsWithSimilarGeometries()
            throws Exception
    {
        assertEquals(GEOMETRY_1200_WELLS_40X30, PlateGeometryOracle.figureGeometry(Arrays.asList(
                new WellLocation(1, 1), new WellLocation(4, 3), new WellLocation(3, 4)), Arrays
                .asList(GEOMETRY_12_WELLS_3X4, GEOMETRY_12_WELLS_4X3, GEOMETRY_1200_WELLS_40X30)));
    }

    @Test
    public void testGetSmallestMatchingGeometryForManyLocationsWithGeometryMatchingMaxLocations()
            throws Exception
    {
        assertEquals(GEOMETRY_16_WELLS_4X4, PlateGeometryOracle.figureGeometry(Arrays.asList(
                new WellLocation(1, 1), new WellLocation(4, 3), new WellLocation(3, 4)), Arrays
                .asList(GEOMETRY_12_WELLS_3X4, GEOMETRY_12_WELLS_4X3, GEOMETRY_1200_WELLS_40X30,
                        GEOMETRY_16_WELLS_4X4)));
    }

    @Test
    public void testSimpleGeometryCode() throws Exception
    {
        assertEquals(
                GEOMETRY_3X4,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(2, 1)),
                        Arrays.asList(GEOMETRY_3X4)));

    }

    @Test
    public void testOneLocationOneGeometryMatching() throws Exception
    {
        assertEquals(
                GEOMETRY_12_WELLS_3X4,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(2, 1)),
                        Arrays.asList(GEOMETRY_12_WELLS_3X4)));

    }

    @Test
    public void testOneLocationOneGeometryMatchingMaxValues() throws Exception
    {
        assertEquals(
                GEOMETRY_12_WELLS_3X4,
                PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(3, 4)),
                        Arrays.asList(GEOMETRY_12_WELLS_3X4)));
    }

    @Test
    public void testOneLocationOneGeometryNotMatchingY() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(4, 3)),
                    Arrays.asList(GEOMETRY_12_WELLS_3X4));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testOneLocationOneGeometryNotMatchingX() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            PlateGeometryOracle.figureGeometry(Arrays.asList(new WellLocation(3, 5)),
                    Arrays.asList(GEOMETRY_12_WELLS_3X4));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}
