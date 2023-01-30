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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class NormalizedSampleIdentifierTest
{
    private NewSample newSample;

    @BeforeMethod
    public void setUpExamples()
    {
        newSample = new NewSample();
        newSample.setDefaultSpaceIdentifier("DEFAULT");
    }

    @Test
    public void testNewSharedSample()
    {
        newSample.setIdentifier("/a");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testNewSharedSampleWithCodeNoHomeSpaceNoDefaultSpace()
    {
        newSample.setIdentifier("a");
        newSample.setDefaultSpaceIdentifier(null);
        
        try
        {
            new NormalizedSampleIdentifier(newSample, null);
        } catch (UserFailureException ex)
        {
            assertEquals(ex.getMessage(), "Cannot determine space for sample A");
        }
    }
    
    @Test
    public void testNewSharedSampleComponent()
    {
        newSample.setIdentifier("/a:b");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/B");
        assertEquals(identifier.getContainerIdentifier(), "/A");
        assertEquals(identifier.getContainerCode(), "A");
    }
    
    @Test
    public void testNewSharedSampleComponentWithCurrentContainer()
    {
        newSample.setIdentifier("/b");
        newSample.setDefaultSpaceIdentifier(null);
        newSample.setCurrentContainerIdentifier("/a");
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/B");
        assertEquals(identifier.getContainerIdentifier(), "/A");
        assertEquals(identifier.getContainerCode(), "A");
    }
    
    @Test
    public void testNewSpaceSample()
    {
        newSample.setIdentifier("/a/b");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testNewSpaceSampleComponent()
    {
        newSample.setIdentifier("/a/b:c");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/C");
        assertEquals(identifier.getContainerIdentifier(), "/A/B");
        assertEquals(identifier.getContainerCode(), "B");
    }
    
    @Test
    public void testNewSpaceSampleComponentWithNormalizedIdentifier()
    {
        newSample.setIdentifier("/a/c");
        newSample.setDefaultSpaceIdentifier(null);
        newSample.setCurrentContainerIdentifier("/a/b");

        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/C");
        assertEquals(identifier.getContainerIdentifier(), "/A/B");
        assertEquals(identifier.getContainerCode(), "B");
    }
    
    @Test
    public void testNewSpaceSampleComponentWithContainerCodeOnly()
    {
        newSample.setIdentifier("/a/c");
        newSample.setDefaultSpaceIdentifier(null);
        newSample.setCurrentContainerIdentifier("b");
        
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/C");
        assertEquals(identifier.getContainerIdentifier(), "/A/B");
        assertEquals(identifier.getContainerCode(), "B");
    }
    
    @Test
    public void testNewSpaceSampleComponentWithCurrentContainer()
    {
        newSample.setIdentifier("/a/b:c");
        newSample.setDefaultSpaceIdentifier(null);
        newSample.setCurrentContainerIdentifier("/a/x");
        
        try
        {
            new NormalizedSampleIdentifier(newSample, null);
            Assert.fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(ex.getMessage(), "Container specified twice: /A/B:C and /A/X");
        }
    }
    
    @Test
    public void testNewSpaceSampleWithHomeSpace()
    {
        newSample.setIdentifier("b");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, "HOME");
        
        assertEquals(identifier.getSampleIdentifier(), "/HOME/B");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testNewSpaceSampleComponentWithHomeSpace()
    {
        newSample.setIdentifier("c");
        newSample.setDefaultSpaceIdentifier(null);
        newSample.setCurrentContainerIdentifier("/a/b");
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, "HOME");
        
        assertEquals(identifier.getSampleIdentifier(), "/HOME/C");
        assertEquals(identifier.getContainerIdentifier(), "/A/B");
        assertEquals(identifier.getContainerCode(), "B");
    }
    
    @Test
    public void testNewSpaceSampleWithDefaultSpace()
    {
        newSample.setIdentifier("b");
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/DEFAULT/B");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testNewSpaceSampleComponentWithDefaultSpace()
    {
        newSample.setIdentifier("b:c");
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/DEFAULT/C");
        assertEquals(identifier.getContainerIdentifier(), "/DEFAULT/B");
        assertEquals(identifier.getContainerCode(), "B");
    }
    
    @Test
    public void testNewProjectSample()
    {
        newSample.setIdentifier("/a/b/c");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B/C");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testNewProjectSampleComponent()
    {
        newSample.setIdentifier("/a/b/c:d");
        newSample.setDefaultSpaceIdentifier(null);
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(newSample, null);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B/D");
        assertEquals(identifier.getContainerIdentifier(), "/A/B/C");
        assertEquals(identifier.getContainerCode(), "C");
    }
    
    @Test
    public void testSpaceSample()
    {
        Sample sample = new SampleBuilder().identifier("/A/B").getSample();
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(sample);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testSpaceSampleComponentWithoutContainer()
    {
        Sample sample = new SampleBuilder().identifier("/A/B:C").getSample();
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(sample);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/C");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testSpaceSampleComponentWithContainer()
    {
        Sample container = new SampleBuilder().identifier("/G/H").getSample();
        Sample sample = new SampleBuilder().identifier("/A/B:C").partOf(container).getSample();
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(sample);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/C");
        assertEquals(identifier.getContainerIdentifier(), "/G/H");
        assertEquals(identifier.getContainerCode(), "H");
    }
    
    @Test
    public void testProjectSample()
    {
        Sample sample = new SampleBuilder().identifier("/A/B/C").getSample();
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(sample);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B/C");
        assertEquals(identifier.getContainerIdentifier(), null);
        assertEquals(identifier.getContainerCode(), null);
    }
    
    @Test
    public void testProjectSampleComponent()
    {
        Sample container = new SampleBuilder().identifier("/G/H/I").getSample();
        Sample sample = new SampleBuilder().identifier("/A/B/C:D").partOf(container).getSample();
        
        NormalizedSampleIdentifier identifier = new NormalizedSampleIdentifier(sample);
        
        assertEquals(identifier.getSampleIdentifier(), "/A/B/D");
        assertEquals(identifier.getContainerIdentifier(), "/G/H/I");
        assertEquals(identifier.getContainerCode(), "I");
    }

}
