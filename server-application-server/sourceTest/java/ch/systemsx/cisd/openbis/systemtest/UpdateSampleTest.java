/*
 * Copyright 2020 ETH Zuerich, SIS
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
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class UpdateSampleTest extends BaseTest
{

    private SampleType sampleType;

    private PropertyType samplePropertyType;

    private PropertyType vocaPropertyType;

    private Sample sample1;

    private Sample sample2;

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
        sample1 = create(aSample().inExperiment(experiment));
        sample2 = create(aSample().inExperiment(experiment));
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeVocabulary()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(vocaPropertyType);
        entityProperty.setValue("ONE");
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(vocaPropertyType);
        entityProperty2.setValue("TWO");
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getVocabularyTerm().getCode(), "TWO");
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleSetByPermId()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue(sample1.getPermId());
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getSample().getPermId(), sample1.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleSetByIdentifier()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue(sample1.getIdentifier());
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getSample().getPermId(), sample1.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleChangeByPermId()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sample1.getPermId());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue(sample2.getPermId());
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getSample().getPermId(), sample2.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleChangeByIdentifier()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sample1.getPermId());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue(sample2.getIdentifier());
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getSample().getPermId(), sample2.getPermId());
        assertEquals(updatedSample.getProperties().get(0).getValue(), null);
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleChangeByUnknownIdentifier()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sample1.getPermId());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue("/UNKNOWN_SAMPLE");
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        assertUserFailureException(Void -> genericServer.updateSample(systemSessionToken, updates),
                // Then
                "No sample could be found for identifier /UNKNOWN_SAMPLE.");
    }

    @Test
    public void testUpdateSampleWithPropertyOfDataTypeSampleReset()
    {
        // Given
        NewSample newSample = new NewSample();
        newSample.setSampleType(sampleType);
        newSample.setIdentifier(sample1.getIdentifier() + "-1");
        EntityProperty entityProperty = new EntityProperty();
        entityProperty.setPropertyType(samplePropertyType);
        entityProperty.setValue(sample1.getPermId());
        IEntityProperty[] props = new IEntityProperty[] { entityProperty };
        newSample.setProperties(props);
        Sample sample = genericServer.registerSample(systemSessionToken, newSample, Collections.emptySet());
        EntityProperty entityProperty2 = new EntityProperty();
        entityProperty2.setPropertyType(samplePropertyType);
        entityProperty2.setValue(null);
        List<IEntityProperty> properties = Arrays.asList(entityProperty2);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sample), properties, null, null,
                Collections.emptySet(), sample.getVersion(), SampleIdentifierFactory.parse(sample.getIdentifier()),
                null, null);

        // When
        genericServer.updateSample(systemSessionToken, updates);

        // Then
        Sample updatedSample = genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(updatedSample.getPermId(), sample.getPermId());
        assertEquals(updatedSample.getProperties().size(), 0);
    }

}
