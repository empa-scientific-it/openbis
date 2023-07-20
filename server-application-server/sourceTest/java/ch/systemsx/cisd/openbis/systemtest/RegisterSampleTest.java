/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class RegisterSampleTest extends BaseTest
{
    private Sample sampleWithExperiment;

    private SampleType sampleType;

    private PropertyType samplePropertyType;

    private PropertyType vocaPropertyType;

    @BeforeMethod
    void createFixture() throws Exception
    {
        NewVocabulary vocabulary = new NewVocabulary();
        vocabulary.setCode("LEVEL");
        VocabularyTerm term1 = new VocabularyTerm();
        term1.setCode("ONE");
        VocabularyTerm term2 = new VocabularyTerm();
        term2.setCode("TWO");
        vocabulary.setTerms(Arrays.asList(term1, term2));
        commonServer.registerVocabulary(systemSessionToken, vocabulary);

        samplePropertyType = new PropertyType();
        samplePropertyType.setCode("SAMPLE_PROP");
        samplePropertyType.setDataType(new DataType(DataTypeCode.SAMPLE));
        samplePropertyType.setLabel("Sample");
        samplePropertyType.setDescription("test");
        commonServer.registerPropertyType(systemSessionToken, samplePropertyType);

        vocaPropertyType = new PropertyType();
        vocaPropertyType.setCode("VOCA_PROP");
        vocaPropertyType.setDataType(new DataType(DataTypeCode.CONTROLLEDVOCABULARY));
        vocaPropertyType.setLabel("Vocabulary");
        vocaPropertyType.setVocabulary(vocabulary);
        vocaPropertyType.setDescription("test");
        commonServer.registerPropertyType(systemSessionToken, vocaPropertyType);

        sampleType = new SampleType();
        sampleType.setCode("SAMPLE_WITH_PROPS");
        sampleType.setGeneratedCodePrefix("SWS-");
        commonServer.registerSampleType(systemSessionToken, sampleType);
        NewETPTAssignment assignment1 = new NewETPTAssignment();
        assignment1.setEntityKind(EntityKind.SAMPLE);
        assignment1.setEntityTypeCode(sampleType.getCode());
        assignment1.setPropertyTypeCode(samplePropertyType.getCode());
        commonServer.assignPropertyType(systemSessionToken, assignment1);
        NewETPTAssignment assignment2 = new NewETPTAssignment();
        assignment2.setEntityKind(EntityKind.SAMPLE);
        assignment2.setEntityTypeCode(sampleType.getCode());
        assignment2.setPropertyTypeCode(vocaPropertyType.getCode());
        commonServer.assignPropertyType(systemSessionToken, assignment2);

        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sampleWithExperiment = create(aSample().inExperiment(experiment));
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeVocabulary()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(vocaPropertyType);
        entityProperty.setValue("ONE");
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());

        // Then
        assertEquals(sample.getIdentifier(), newSample.getIdentifier());
        assertEquals(sample.getProperties().get(0).getVocabularyTerm().getCode(), "ONE");
        assertEquals(sample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeVocabularyWIthUnknownTerm()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(vocaPropertyType);
        entityProperty.setValue("UNKNOWN");
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        assertUserFailureException(Void -> genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet()),
                // Then
                "Vocabulary value 'UNKNOWN' of property 'VOCA_PROP' is not valid.");
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeSampleReferingWithPermId()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sampleWithExperiment.getPermId());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());

        // Then
        assertEquals(sample.getIdentifier(), newSample.getIdentifier());
        assertEquals(sample.getProperties().get(0).getSample().getPermId(), sampleWithExperiment.getPermId());
        assertEquals(sample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeSampleReferingWithUnknownPermId()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue("123-789");
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        assertUserFailureException(Void -> genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet()),
                // Then
                "No sample could be found for perm id 123-789.");
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeSampleReferingWithIdentifier()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sampleWithExperiment.getIdentifier());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());

        // Then
        assertEquals(sample.getIdentifier(), newSample.getIdentifier());
        assertEquals(sample.getProperties().get(0).getSample().getPermId(), sampleWithExperiment.getPermId());
        assertEquals(sample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testRegisterSampleWithPropertyOfDataTypeSampleReferingWithUnknownIdentifier()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sampleWithExperiment.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue("/UNKNOWN_SAMPLE");
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);

        // When
        assertUserFailureException(Void -> genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet()),
                // Then
                "No sample could be found for identifier /UNKNOWN_SAMPLE.");
    }
}
