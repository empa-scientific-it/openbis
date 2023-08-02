/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class SampleTest extends MainSuite
{
    @Test
    public void newSampleIsListedInSampleBrowser() throws Exception
    {
        Sample sample = create(aSample());

        assertThat(browserEntryOf(sample), exists());
    }

    @Test
    public void propertiesOfSampleTypeAreAskedForInSampleRegistration() throws Exception
    {

        PropertyType booleanType = create(aBooleanPropertyType());
        PropertyType integerType = create(anIntegerPropertyType());
        SampleType sampleType = create(aSampleType());

        create(aSamplePropertyTypeAssignment().with(sampleType).with(booleanType));
        create(aSamplePropertyTypeAssignment().with(sampleType).with(integerType));

        assertThat(sampleRegistrationPageFor(sampleType), hasInputsForProperties(booleanType,
                integerType));
    }

}
