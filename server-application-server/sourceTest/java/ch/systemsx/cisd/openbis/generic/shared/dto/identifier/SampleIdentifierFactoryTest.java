/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author felmer
 */
public class SampleIdentifierFactoryTest extends AssertJUnit
{
    @Test
    public void testParseSharedSampleIdentifier()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("/B");

        assertEquals(true, id.isDatabaseInstanceLevel());
        assertEquals(false, id.isSpaceLevel());
        assertEquals(false, id.isInsideHomeSpace());
        assertEquals(null, id.getSpaceLevel());
        assertEquals("B", id.getSampleCode());
        assertEquals("B", id.getSampleSubCode());
        assertEquals(null, id.tryGetContainerCode());
    }

    @Test
    public void testParseSharedSampleIdentifierWithSubCode()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("/B:C");

        assertEquals(true, id.isDatabaseInstanceLevel());
        assertEquals(false, id.isSpaceLevel());
        assertEquals(false, id.isInsideHomeSpace());
        assertEquals(null, id.getSpaceLevel());
        assertEquals("B:C", id.getSampleCode());
        assertEquals("C", id.getSampleSubCode());
        assertEquals("B", id.tryGetContainerCode());
    }

    @Test
    public void testParseSpaceSampleIdentifierWithSpace()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("/A/B");

        assertEquals(false, id.isDatabaseInstanceLevel());
        assertEquals(true, id.isSpaceLevel());
        assertEquals(false, id.isInsideHomeSpace());
        assertEquals("A", id.getSpaceLevel().getSpaceCode());
        assertEquals("B", id.getSampleCode());
        assertEquals("B", id.getSampleSubCode());
        assertEquals(null, id.tryGetContainerCode());
    }

    @Test
    public void testParseSpaceSampleIdentifierWithHomeSpace()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("//B");

        assertEquals(false, id.isDatabaseInstanceLevel());
        assertEquals(true, id.isSpaceLevel());
        assertEquals(true, id.isInsideHomeSpace());
        assertEquals(null, id.getSpaceLevel().getSpaceCode());
        assertEquals("B", id.getSampleCode());
        assertEquals("B", id.getSampleSubCode());
        assertEquals(null, id.tryGetContainerCode());
    }

    @Test
    public void testParseSpaceSampleIdentifierWithHomeSpaceAndSubCode()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("//B:C");

        assertEquals(false, id.isDatabaseInstanceLevel());
        assertEquals(true, id.isSpaceLevel());
        assertEquals(true, id.isInsideHomeSpace());
        assertEquals(null, id.getSpaceLevel().getSpaceCode());
        assertEquals("B:C", id.getSampleCode());
        assertEquals("C", id.getSampleSubCode());
        assertEquals("B", id.tryGetContainerCode());
    }

    @Test
    public void testParseSpaceSampleIdentifierWithDefaultSpace()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("//B", "/A");

        assertEquals(false, id.isDatabaseInstanceLevel());
        assertEquals(true, id.isSpaceLevel());
        assertEquals(false, id.isInsideHomeSpace());
        assertEquals("A", id.getSpaceLevel().getSpaceCode());
        assertEquals("B", id.getSampleCode());
        assertEquals("B", id.getSampleSubCode());
        assertEquals(null, id.tryGetContainerCode());
    }

    @Test
    public void testParseSpaceSampleIdentifierWithSpaceAndSubCode()
    {
        SampleIdentifier id = SampleIdentifierFactory.parse("/A/B:C");

        assertEquals(false, id.isDatabaseInstanceLevel());
        assertEquals(true, id.isSpaceLevel());
        assertEquals(false, id.isInsideHomeSpace());
        assertEquals("A", id.getSpaceLevel().getSpaceCode());
        assertEquals("B:C", id.getSampleCode());
        assertEquals("C", id.getSampleSubCode());
        assertEquals("B", id.tryGetContainerCode());
    }
}
