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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link WellLocation} conversion routines.
 * 
 * @author Franz-Josef Elmer
 */
public class WellLocationTest
{

    @DataProvider(name = "valid spreadsheet locations")
    Object[][] provideCorrectData()
    {
        return new Object[][]
        {
                { "A01", 1, 1 },
                { "A1", 1, 1 },
                { "A123", 1, 123 },
                { "B09", 2, 9 },
                { "C23", 3, 23 },
                { "AA1", 27, 1 },
                { "ZZ1000", 26 * 26 + 26, 1000 },
                { "B05", 2, 5 },
                { "a01", 1, 1 },
                { "c23", 3, 23 },
                { "zZ1000", 26 * 26 + 26, 1000 } };
    }

    @Test(dataProvider = "valid spreadsheet locations")
    public void testParseLocationStrWithValidLocations(String location, int row, int col)
    {
        assertEquals(new WellLocation(row, col), WellLocation.parseLocationStr(location));
        assertEquals(new WellLocation(row, col), WellLocation.parseLocationStr(Integer
                .toString(row), Integer.toString(col)));
    }

    @DataProvider(name = "valid well ids")
    Object[][] provideWellIdStrings()
    {
        return new Object[][]
        {
                { "A1", 1, 1 },
                { "A123", 1, 123 },
                { "B9", 2, 9 },
                { "C23", 3, 23 },
                { "AA1", 27, 1 },
                { "ZZ1000", 26 * 26 + 26, 1000 } };
    }

    @Test(dataProvider = "valid well ids")
    public void testGetWellIdString(String location, int row, int col)
    {
        assertEquals(location, new WellLocation(row, col).toWellIdString());
    }

    @Test
    public void testParseLocationStrWithEmptyString()
    {
        try
        {
            WellLocation.parseLocationStr("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Unspecified location.", e.getMessage());
        }
    }

    @Test
    public void testParseLocationStrWithOnlyNumbers()
    {
        try
        {
            WellLocation.parseLocationStr("123");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Missing row part of the location: 123", e.getMessage());
        }
    }

    @Test
    public void testParseLocationStrWithInvalidLetter()
    {
        try
        {
            WellLocation.parseLocationStr("A?14");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid letter '?' in row location: A?", e.getMessage());
        }
    }

    @Test
    public void testParseLocationStrWithInvalidDigitLetter()
    {
        try
        {
            WellLocation.parseLocationStr("A1?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Column part of the location is not a number: 1?", e.getMessage());
        }
    }

    @Test
    public void testParseLocationStrWithNonPositiveNumber()
    {
        try
        {
            WellLocation.parseLocationStr("A0");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Column part of the location is not a positive number: 0", e.getMessage());
        }
    }

}
