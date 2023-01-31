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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.hcs.PlateGeometry;
import ch.systemsx.cisd.hcs.WellGeometry;

/**
 * Test cases for corresponding {@link HCSImageCheckList} class.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageCheckListTest
{

    private static final WellGeometry WELL_GEOMETRY = new WellGeometry(1, 2);

    private static final PlateGeometry PLATE_GEOMETRY = new PlateGeometry(2, 1);

    private final static HCSImageCheckList createImageCheckList()
    {
        return new HCSImageCheckList(1, PLATE_GEOMETRY, WELL_GEOMETRY);
    }

    @Test
    public final void testConstructor()
    {
        try
        {
            new HCSImageCheckList(0, null, null);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e)
        {
            assertEquals("Number of channels smaller than one.", e.getMessage());
        }
        try
        {
            new HCSImageCheckList(1, null, null);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e)
        {
            assertEquals("Unspecified plate geometry.", e.getMessage());
        }
        try
        {
            new HCSImageCheckList(1, PLATE_GEOMETRY, null);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e)
        {
            assertEquals("Unspecified well geometry.", e.getMessage());
        }
        new HCSImageCheckList(1, PLATE_GEOMETRY, WELL_GEOMETRY);
    }

    @Test
    public final void testCheckOff()
    {
        final HCSImageCheckList checkList = createImageCheckList();
        assertEquals(4, checkList.getCheckedOnFullLocations().size());
        try
        {
            checkList.checkOff(1, new Location(2, 1), new Location(1, 1));
            fail("Wrong well location.");
        } catch (final IllegalArgumentException ex)
        {
        }
        try
        {
            checkList.checkOff(1, new Location(1, 2), new Location(1, 2));
            fail("Wrong tile location.");
        } catch (final IllegalArgumentException ex)
        {
        }
        checkList.checkOff(1, new Location(1, 2), new Location(2, 1));
        assertEquals(3, checkList.getCheckedOnFullLocations().size());
        try
        {
            checkList.checkOff(1, new Location(1, 2), new Location(2, 1));
            fail("Image already handled.");
        } catch (IllegalArgumentException ex)
        {
        }
        checkList.checkOff(1, new Location(1, 1), new Location(1, 1));
        checkList.checkOff(1, new Location(1, 1), new Location(2, 1));
        checkList.checkOff(1, new Location(1, 2), new Location(1, 1));
        assertEquals(0, checkList.getCheckedOnFullLocations().size());
    }
}
