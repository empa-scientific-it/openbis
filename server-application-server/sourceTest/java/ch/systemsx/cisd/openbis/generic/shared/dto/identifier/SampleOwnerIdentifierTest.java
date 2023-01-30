/*
 * Copyright 2016 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleOwnerIdentifierTest
{
    private SampleOwnerIdentifier noOwner;
    private SpaceIdentifier spaceA;
    private SpaceIdentifier spaceB;
    private SpaceIdentifier homeSpace;
    private SampleOwnerIdentifier ownerSpaceA;
    private SampleOwnerIdentifier ownerSpaceB;
    private SampleOwnerIdentifier ownerHomeSpace;
    private ProjectIdentifier projectP1;
    private ProjectIdentifier projectP2;
    private ProjectIdentifier projectP3;
    private SampleOwnerIdentifier ownerProjectP1;
    private SampleOwnerIdentifier ownerProjectP2;
    private SampleOwnerIdentifier ownerProjectP3;

    @BeforeMethod
    public void setUpIdentifiers()
    {
        noOwner = new SampleOwnerIdentifier();
        spaceA = new SpaceIdentifier("A");
        spaceB = new SpaceIdentifier("B");
        homeSpace = SpaceIdentifier.createHome();
        ownerHomeSpace = new SampleOwnerIdentifier(homeSpace);
        ownerSpaceA = new SampleOwnerIdentifier(spaceA);
        ownerSpaceB = new SampleOwnerIdentifier(spaceB);
        projectP1 = new ProjectIdentifier(spaceA, "P1");
        ownerProjectP1 = new SampleOwnerIdentifier(projectP1);
        projectP2 = new ProjectIdentifier(spaceA, "P2");
        ownerProjectP2 = new SampleOwnerIdentifier(projectP2);
        projectP3 = new ProjectIdentifier(spaceB, "P3");
        ownerProjectP3 = new SampleOwnerIdentifier(projectP3);
    }
    
    @Test
    public void testIsAndGetProjectLevel()
    {
        assertIsAndGetProjectLevel(ownerProjectP1, true, projectP1);
        assertIsAndGetProjectLevel(ownerSpaceA, false, null);
        assertIsAndGetProjectLevel(noOwner, false, null);
    }
    
    private void assertIsAndGetProjectLevel(SampleOwnerIdentifier identifier, boolean expected, ProjectIdentifier project)
    {
        assertEquals(identifier.isProjectLevel(), expected);
        assertSame(identifier.getProjectLevel(), project);
    }
    
    @Test
    public void testIsAndGetSpaceLevel()
    {
        assertIsAndGetSpaceLevel(ownerProjectP1, false, spaceA);
        assertIsAndGetSpaceLevel(ownerSpaceA, true, spaceA);
        assertIsAndGetSpaceLevel(noOwner, false, null);
    }
    
    private void assertIsAndGetSpaceLevel(SampleOwnerIdentifier identifier, boolean expected, SpaceIdentifier space)
    {
        assertEquals(identifier.isSpaceLevel(), expected);
        if (space == null)
        {
            assertEquals(identifier.getSpaceLevel(), null);
        } else
        {
            assertSame(identifier.getSpaceLevel().getSpaceCode(), space.getSpaceCode());
        }
    }
    
    @Test
    public void testToStringForNoOwner()
    {
        assertEquals(noOwner.toString(), "/");
    }

    @Test
    public void testToStringForSpaceOwner()
    {
        assertEquals(ownerSpaceA.toString(), "/A/");
    }
    
    @Test
    public void testToStringForProjectOwner()
    {
        assertEquals(ownerProjectP1.toString(), "/A/P1/");
    }
    
    @Test
    public void testCompareTo()
    {
        assertCompareToContract(ownerProjectP1, ownerProjectP2);
        assertCompareToContract(ownerProjectP1, ownerProjectP3);
        assertCompareToContract(ownerProjectP2, ownerProjectP3);
        assertCompareToContract(ownerSpaceA, ownerProjectP1);
        assertCompareToContract(ownerSpaceA, ownerProjectP2);
        assertCompareToContract(ownerSpaceA, ownerProjectP3);
        assertCompareToContract(ownerSpaceB, ownerProjectP3);
        assertCompareToContract(ownerSpaceA, ownerSpaceB);
        assertCompareToContract(ownerHomeSpace, ownerSpaceB);
        assertCompareToContract(noOwner, ownerProjectP1);
        assertCompareToContract(noOwner, ownerSpaceA);
    }
    
    private void assertCompareToContract(SampleOwnerIdentifier id1, SampleOwnerIdentifier id2)
    {
        assertSameSign(id1.compareTo(id1), 0);
        assertSameSign(id1.compareTo(id2), -1);
        assertSameSign(id2.compareTo(id2), 0);
        assertSameSign(id2.compareTo(id1), 1);
    }
    
    private void assertSameSign(int actual, int expected)
    {
        if (actual < 0)
        {
            if (expected < 0)
            {
                return;
            }
        } else if (actual == 0)
        {
            if (expected == 0)
            {
                return;
            }
        } else if (expected > 0)
        {
            return;
        }
        fail("Different signs: expected [" + expected + "] actual [" + actual + "]");
    }

}
