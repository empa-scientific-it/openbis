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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.hcs.Geometry;

/**
 * Test cases for {@link TileMapper}.
 * 
 * @author Izabela Adamczyk
 */
public class TileMapperTest extends AssertJUnit
{

    @Test
    public void testCreateWithNoMappingString() throws Exception
    {
        assertNull(TileMapper.tryCreate(null, new Geometry(1, 1)));
    }

    @Test
    public void testCreateWithNullGeometry() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            TileMapper.tryCreate("1", null);
        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateWithCorrectMappingValues() throws Exception
    {
        TileMapper mapper = TileMapper.tryCreate("0,1;2,3;4,5", new Geometry(3, 2));
        assertEquals(1, mapper.tryGetLocation(0).getX());
        assertEquals(1, mapper.tryGetLocation(0).getY());

        assertEquals(1, mapper.tryGetLocation(2).getX());
        assertEquals(2, mapper.tryGetLocation(2).getY());

        assertEquals(1, mapper.tryGetLocation(4).getX());
        assertEquals(3, mapper.tryGetLocation(4).getY());

        assertEquals(2, mapper.tryGetLocation(1).getX());
        assertEquals(1, mapper.tryGetLocation(1).getY());

        assertEquals(2, mapper.tryGetLocation(3).getX());
        assertEquals(2, mapper.tryGetLocation(3).getY());

        assertEquals(2, mapper.tryGetLocation(5).getX());
        assertEquals(3, mapper.tryGetLocation(5).getY());
    }

    @Test
    public void testCreateWithCorrectMappingValuesAndZero() throws Exception
    {
        TileMapper mapper = TileMapper.tryCreate("0,-1;-1,3;4,-1", new Geometry(3, 2));
        assertEquals(1, mapper.tryGetLocation(0).getX());
        assertEquals(1, mapper.tryGetLocation(0).getY());

        assertEquals(1, mapper.tryGetLocation(4).getX());
        assertEquals(3, mapper.tryGetLocation(4).getY());

        assertEquals(2, mapper.tryGetLocation(3).getX());
        assertEquals(2, mapper.tryGetLocation(3).getY());

        assertNull(mapper.tryGetLocation(1));
        assertNull(mapper.tryGetLocation(2));
        assertNull(mapper.tryGetLocation(5));
    }

    @Test
    public void testCreateWithMisssingMappingValuesRow() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            TileMapper.tryCreate("1,2;3,4", new Geometry(3, 2));
        } catch (IllegalArgumentException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateWithMisssingMappingValuesColumn() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            TileMapper.tryCreate("1;3;5", new Geometry(3, 2));
        } catch (IllegalArgumentException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateWithTooManyMappingValuesRows() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            TileMapper.tryCreate("1,2;3,4;5,6;7,8", new Geometry(3, 2));
        } catch (IllegalArgumentException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCreateWithTooManyMappingValuesColumns() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            TileMapper.tryCreate("1,2,3;4,5,6;7,8,9", new Geometry(3, 2));
        } catch (IllegalArgumentException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}
