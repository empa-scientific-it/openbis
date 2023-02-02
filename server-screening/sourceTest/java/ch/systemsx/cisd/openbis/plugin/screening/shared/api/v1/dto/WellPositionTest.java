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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class WellPositionTest extends AssertJUnit
{
    @Test
    public void testEquality()
    {
        WellPosition pos1 = new WellPosition(9, 4);
        WellPosition pos2 = new WellPosition(9, 4);
        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    public void testParseWellPositions()
    {
        assertEquals(0, WellPosition.parseWellPositions("").size());
        assertEquals("[[1, 2]]", WellPosition.parseWellPositions("1.2").toString());
        assertEquals("[[1, 2], [2, 3], [3, 4], [4, 5]]",
                WellPosition.parseWellPositions("1.2 2.3  3.4\t4.5").toString());
    }

    @Test
    public void testParseWellPoistionWithMissingDot()
    {
        try
        {
            WellPosition.parseWellPosition("A03");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid well description: Expecting a '.' in well description: A03", ex.getMessage());
        }
    }

    @Test
    public void testParseWellPoistionWithRowIsNotANumber()
    {
        try
        {
            WellPosition.parseWellPosition("a.1");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid well description: String before '.' isn't a number: a.1", ex.getMessage());
        }
    }

    @Test
    public void testParseWellPoistionWithRowIsLessThanOne()
    {
        try
        {
            WellPosition.parseWellPosition("0.1");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid well description: First row < 1: 0.1", ex.getMessage());
        }
    }

    @Test
    public void testParseWellPoistionWithColumnIsNotANumber()
    {
        try
        {
            WellPosition.parseWellPosition("1.a");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid well description: String after '.' isn't a number: 1.a", ex.getMessage());
        }
    }

    @Test
    public void testParseWellPoistionWithColumnIsLessThanOne()
    {
        try
        {
            WellPosition.parseWellPosition("1.0");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid well description: First column < 1: 1.0", ex.getMessage());
        }
    }
}
