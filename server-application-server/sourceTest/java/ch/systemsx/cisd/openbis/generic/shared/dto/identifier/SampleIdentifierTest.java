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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link SampleIdentifier} class.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleIdentifierTest
{
    final static String SAMPLE_TYPE = "TYPE1";

    @Test
    public final void testToStringDbLevel()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier("SAMP");
        assertEquals(sampleIdentifier.toString(), "/SAMP");
    }

    @Test
    public final void testToStringSpaceLevelHomeBd()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier("GR"),
                        "SAMP");
        assertEquals(sampleIdentifier.toString(), "/GR/SAMP");
    }

    @Test
    public final void testToStringSpaceLevel()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier("GR"), "SAMP");
        assertEquals(sampleIdentifier.toString(), "/GR/SAMP");
    }

    @Test
    public final void testToStringHomeSpaceLevel()
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifier.createHomeGroup("SAMP");
        assertEquals(sampleIdentifier.toString(), "SAMP");
    }
    
    @Test
    public final void testToStringProjectLevel()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new ProjectIdentifier("A", "P1"), "SAMP");
        assertEquals(sampleIdentifier.toString(), "/A/P1/SAMP");
    }

    @Test
    public final void testCompareIdentifiers()
    {
        String s1 = "AG";
        String s2 = "BG";
        String c1 = "A";
        String c2 = "B";
        SampleIdentifier[] expectedOrder =
                new SampleIdentifier[]
                { SampleIdentifier.createHomeGroup(c1), SampleIdentifier.createHomeGroup(c2),
                        new SampleIdentifier(new SpaceIdentifier(s1), c1),
                        new SampleIdentifier(new SpaceIdentifier(s1), c2),
                        new SampleIdentifier(new SpaceIdentifier(s2), c1),
                        new SampleIdentifier(new SpaceIdentifier(s2), c2),
                        new SampleIdentifier(new ProjectIdentifier(s2, "P1"), c1),
                        new SampleIdentifier(new ProjectIdentifier(s2, "P1"), c2),
                        new SampleIdentifier(new ProjectIdentifier(s2, "P2"), c2),
                };

        SampleIdentifier[] idents = revert(expectedOrder);
        Arrays.sort(idents);
        assertEquals(Arrays.asList(expectedOrder), Arrays.asList(idents));
    }

    @Test
    public void testEquals()
    {
        SampleIdentifier id1 = new SampleIdentifierFactory("/SPACE/SAMP").createIdentifier();
        SampleIdentifier id2 = new SampleIdentifierFactory("/SPACE/SAMP").createIdentifier();
        assertEquals(id1, id2);
    }

    @Test
    public void testEquals2()
    {
        SampleIdentifier identifier1 =
                new SampleIdentifierFactory("/SPACE/SAMP").createIdentifier();
        assertEquals("/SPACE/SAMP", identifier1.toString());

        SampleIdentifier identifier2 =
                new SampleIdentifier(new SpaceIdentifier(
                        "SPACE"), "SAMP");
        assertEquals(identifier1, identifier2);
    }

    private static SampleIdentifier[] revert(SampleIdentifier[] expectedOrder)
    {
        SampleIdentifier[] idents = new SampleIdentifier[expectedOrder.length];
        for (int i = 0; i < idents.length; i++)
        {
            idents[idents.length - i - 1] = expectedOrder[i];
        }
        return idents;
    }
}
