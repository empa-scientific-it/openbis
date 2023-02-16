/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleToDataSetRelatedEntitiesTranslatorTest extends AssertJUnit
{
    private SampleToDataSetRelatedEntitiesTranslator translator;

    private static final Long SAMPLE_ID = Long.valueOf(1);

    private static final String SAMPLE_PERM_ID = "perm-id";

    private static final String SAMPLE_CODE = "sample-code";

    private static final String SAMPLE_IDENTIFIER = "/space/sample-code";

    private static final Long SAMPLE_TYPE_ID = Long.valueOf(1);

    private static final String SAMPLE_TYPE_CODE = "sample-type";

    @BeforeMethod
    public void setUp()
    {
        ArrayList<SampleType> sampleTypes = createSampleTypes();
        ArrayList<Sample> samples = createSamples();
        translator = new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
    }

    private ArrayList<Sample> createSamples()
    {
        ArrayList<Sample> samples = new ArrayList<Sample>();

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(SAMPLE_ID);
        initializer.setPermId(SAMPLE_PERM_ID);
        initializer.setCode(SAMPLE_CODE);
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        initializer.setSampleTypeId(SAMPLE_TYPE_ID);
        initializer.setSampleTypeCode(SAMPLE_TYPE_CODE);
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
        initializer.setRegistrationDetails(registrationDetails);
        Sample sample = new Sample(initializer);
        samples.add(sample);
        return samples;
    }

    private ArrayList<SampleType> createSampleTypes()
    {
        ArrayList<SampleType> sampleTypes = new ArrayList<SampleType>();
        SampleType sampleType = new SampleType();
        sampleType.setId(Long.valueOf(1));
        sampleType.setCode("SAMPLE_TYPE");
        sampleTypes.add(sampleType);
        return sampleTypes;
    }

    @Test
    public void testTranslator()
    {
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        assertNotNull(dsre);
        List<? extends IEntityInformationHolder> entities = dsre.getEntities();
        assertEquals(1, entities.size());
    }

    @Test
    public void testTranslatorWithEmptySampleTypes()
    {
        ArrayList<SampleType> sampleTypes = new ArrayList<SampleType>();
        ArrayList<Sample> samples = createSamples();
        SampleToDataSetRelatedEntitiesTranslator myTranslator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);

        DataSetRelatedEntities dsre = myTranslator.convertToDataSetRelatedEntities();
        assertNotNull(dsre);
        List<? extends IEntityInformationHolder> entities = dsre.getEntities();
        assertEquals(0, entities.size());
    }
}
