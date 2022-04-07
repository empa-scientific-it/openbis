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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.EnumSet;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleTest extends AssertJUnit
{
    private static final Long SAMPLE_ID = Long.valueOf(1);

    private static final String SAMPLE_PERM_ID = "perm-id";

    private static final String SAMPLE_CODE = "sample-code";

    private static final String SAMPLE_IDENTIFIER = "/space/sample-code";

    private static final Long SAMPLE_TYPE_ID = Long.valueOf(1);

    private static final String SAMPLE_TYPE_CODE = "sample-type";

    private Sample sample;

    @BeforeMethod
    public void setUp()
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(SAMPLE_ID);
        initializer.setPermId(SAMPLE_PERM_ID);
        initializer.setCode(SAMPLE_CODE);
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        initializer.setSampleTypeId(SAMPLE_TYPE_ID);
        initializer.setSampleTypeCode(SAMPLE_TYPE_CODE);
        initializer.putProperty("PROP1", "value1");
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
        initializer.setRegistrationDetails(registrationDetails);
        initializer.setRetrievedFetchOptions(EnumSet.of(SampleFetchOption.PROPERTIES));

        sample = new Sample(initializer);
    }

    @Test(expectedExceptions =
    { IllegalArgumentException.class })
    public void testInitialization()
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        new Sample(initializer);
    }

    @Test
    public void testEquals()
    {
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(SAMPLE_ID);
        initializer.setPermId(SAMPLE_PERM_ID);
        initializer.setCode(SAMPLE_CODE);
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        initializer.setSampleTypeId(SAMPLE_TYPE_ID);
        initializer.setSampleTypeCode(SAMPLE_TYPE_CODE);
        initializer.putProperty("PROP1", "value1");
        initializer.setRegistrationDetails(registrationDetails);
        Sample mySample = new Sample(initializer);
        assertTrue("Samples with the same id should be equal.", sample.equals(mySample));
        assertEquals(sample.hashCode(), mySample.hashCode());

        initializer = new SampleInitializer();
        initializer.setId(SAMPLE_ID);
        initializer.setPermId(SAMPLE_PERM_ID);
        initializer.setCode("different-code");
        initializer.setIdentifier("/a/different-identifier");
        initializer.setSampleTypeId(Long.valueOf(2));
        initializer.setSampleTypeCode("new-code");
        initializer.setRegistrationDetails(registrationDetails);
        mySample = new Sample(initializer);
        assertTrue("Samples with the same id should be equal.", sample.equals(mySample));
        assertEquals(sample.hashCode(), mySample.hashCode());

        initializer = new SampleInitializer();
        initializer.setId(Long.valueOf(2));
        initializer.setPermId(SAMPLE_PERM_ID);
        initializer.setCode(SAMPLE_CODE);
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        initializer.setSampleTypeId(SAMPLE_TYPE_ID);
        initializer.setSampleTypeCode(SAMPLE_TYPE_CODE);
        initializer.setRegistrationDetails(registrationDetails);
        mySample = new Sample(initializer);
        assertFalse("Samples with the different ids should not be equal.", sample.equals(mySample));
    }

    @Test
    public void testToString()
    {
        String stringRepresentation = sample.toString();
        assertEquals("Sample[/space/sample-code,sample-type,{PROP1=value1},parents=?,children=?]", stringRepresentation);
    }
}
