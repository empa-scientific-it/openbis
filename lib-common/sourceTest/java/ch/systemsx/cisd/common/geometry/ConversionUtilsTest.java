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

package ch.systemsx.cisd.common.geometry;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class ConversionUtilsTest
{
    @DataProvider(name = "valid spreadsheet locations")
    public Object[][] provideCorrectData()
    {
        return new Object[][]
        {
                { "A01", 0, 0 },
                { "A1", 0, 0 },
                { "A123", 0, 122 },
                { "B09", 1, 8 },
                { "C23", 2, 22 },
                { "AA1", 26 + 1 - 1, 0 },
                { "ZZ1000", 26 * 26 + 26 - 1, 999 },
                { "B05", 1, 4 },
                { "a01", 0, 0 },
                { "c23", 2, 22 },
                { "zZ1000", 26 * 26 + 26 - 1, 999 } };
    }

    @Test(dataProvider = "valid spreadsheet locations")
    public void testParseSpreadsheetLocationWithValidLocations(String location, int x, int y)
    {
        assertEquals(new Point(x, y), ConversionUtils.parseSpreadsheetLocation(location));
    }

    @Test
    public void testParseSpreadsheetLocationWithEmptyString()
    {
        try
        {
            ConversionUtils.parseSpreadsheetLocation("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Unspecified location.", e.getMessage());
        }
    }

    @Test
    public void testParseSpreadsheetLocationWithOnlyNumbers()
    {
        try
        {
            ConversionUtils.parseSpreadsheetLocation("123");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Missing letter part of the location: 123", e.getMessage());
        }
    }

    @Test
    public void testParseSpreadsheetLocationWithInvalidLetter()
    {
        try
        {
            ConversionUtils.parseSpreadsheetLocation("A?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid letter '?' in location: A?", e.getMessage());
        }
    }

    @Test
    public void testParseSpreadsheetLocationWithInvalidDigitLetter()
    {
        try
        {
            ConversionUtils.parseSpreadsheetLocation("A1?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Number part of the location is not a number: A1?", e.getMessage());
        }
    }

    @Test
    public void testParseSpreadsheetLocationWithNonPositiveNumber()
    {
        try
        {
            ConversionUtils.parseSpreadsheetLocation("A0");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Number part of the location is not a positive number: A0", e.getMessage());
        }
    }

}
