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
package ch.ethz.sis.openbis.generic.server.asapi.v3;

import static org.jmock.Expectations.any;
import static org.jmock.Expectations.equal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.RecordingMatcherRepository;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SamplePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Jakub Straszewski
 */
public class SampleServerApiTest extends AbstractApplicationServerApiTestCase
{
    private static final EntityTypePermId ENTITY_TYPE_ID = new EntityTypePermId(ENTITY_TYPE);

    private static final SpacePermId SPACE_ID = new SpacePermId(SPACE_CODE);

    private static final SpacePermId UNKNOWN_SPACE_ID = new SpacePermId("UNKNOWN_SPACE_ID");

    private static final String UNKNOWN_ENTITY_TYPE = "UNKNOWN_ENTITY_TYPE";

    private static final TagCode TAG_CODE_ID = new TagCode("TAG_NAME_ID");

    private static final TagCode UNKNOWN_TAG_CODE_ID = new TagCode("UNKNOWN_TAG_NAME_ID");

    private static final ExperimentPermId UNKNOWN_EXPERIMENT_ID = new ExperimentPermId("UNKNOWN_EXPERIMENT_ID");

    private static final ExperimentPermId EXPERIMENT_ID = new ExperimentPermId("EXPERIMENT_ID");

    private static final SamplePermId UNKNOWN_SAMPLE_ID = new SamplePermId("UNKNOWN_SAMPLE_ID");

    /**
     * The id of some other sample, that can be used as parent or container.
     */
    private static final SamplePermId KNOWN_SAMPLE_ID = new SamplePermId("KNOWN_SAMPLE_ID");

    private static final long KNOWN_SAMPLE_TECH_ID = 1L;

    private static final Long NEW_SAMPLE_TECH_ID = 1000L;

    private static final String KNOWN_SAMPLE_CODE = "KNOWN_SAMPLE_CODE";

    private static final String SAMPLE_NAME = "SAMPLE_NAME";

    private SampleTypePE type1;

    private SpacePE space;

    private ExperimentPE experiment;

    private PropertyTypePE propertyType1;

    private MetaprojectPE tag;

    private SamplePE knownSample;

    @BeforeMethod
    public void setUpExamples()
    {
        SampleTypePEBuilder builder1 =
                new SampleTypePEBuilder().code(ENTITY_TYPE);
        type1 = builder1.getSampleType();
        space = new SpacePEBuilder()
                .code(SPACE_CODE).getSpace();

        experiment = new ExperimentPE();
        experiment.setPermId(EXPERIMENT_ID.getPermId());

        knownSample = new SamplePEBuilder().code(KNOWN_SAMPLE_CODE).permID(KNOWN_SAMPLE_ID.getPermId()).
                type(type1).id(KNOWN_SAMPLE_TECH_ID).getSample();

        // builder1.assign(PROPERTY_TYPE_CODE);
        // propertyType1 = new PropertyTypePE();
        // propertyType1.setCode(PROPERTY_TYPE_CODE);
        // DataTypePE dataType = new DataTypePE();
        // dataType.setCode(DataTypeCode.VARCHAR);
        // propertyType1.setType(dataType);
        // tag = new MetaprojectPE();
        // tag.setName(TAG_NAME_ID.getName());
        // tag.setOwner(person);
    }

    @Test(enabled = false)
    public void testNotExistingCreationId()
    {
        prepareCreationEnvironment();

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(ENTITY_TYPE_ID);
        sampleParent.setSpaceId(SPACE_ID);
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(ENTITY_TYPE_ID);
        sampleChild.setSpaceId(SPACE_ID);
        sampleChild.setParentIds(Arrays.asList(new CreationId("NON_EXISTING")));

        assertFailingCreateSamples("Unknown sample creation id: NON_EXISTING (Context: [register samples - verify relationships])",
                sampleParent, sampleChild);
    }

    @Test(enabled = false)
    public void testUnknownExperiment()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setSpaceId(SPACE_ID);
        sample.setExperimentId(UNKNOWN_EXPERIMENT_ID);

        assertFailingCreateSamples("Unknown ids [UNKNOWN_EXPERIMENT_ID] (Context: [register sample SAMPLE_NAME])",
                sample);
    }

    @Test(enabled = false)
    public void testUnkownSpaceDefined()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setSpaceId(UNKNOWN_SPACE_ID);
        sample.setExperimentId(EXPERIMENT_ID);

        assertFailingCreateSamples("No space found with this id: UNKNOWN_SPACE_ID (Context: [register sample SAMPLE_NAME])",
                sample);
    }

    @Test(enabled = false)
    public void testNoSpaceDefinedCreatesSharedSample()
    {
        prepareCreationEnvironment();

        SamplePE samplePE = new SamplePE();
        samplePE.setId(NEW_SAMPLE_TECH_ID);

        prepareListSamplesByIDs(any(Collection.class), Arrays.asList(samplePE));
        prepareListSampleParents(equal(Arrays.asList(new TechId(NEW_SAMPLE_TECH_ID))), Collections.<TechId> emptySet());

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);

        assertCreateSampleOK(sample);
    }

    @Test(enabled = false)
    public void testNoCodeSpecified()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setSpaceId(SPACE_ID);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);

        assertFailingCreateSamples("No code for sample provided (Context: [register sample null])", sample);
    }

    @Test(enabled = false)
    public void testUnknownType()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setTypeId(new EntityTypePermId(UNKNOWN_ENTITY_TYPE));
        sample.setSpaceId(SPACE_ID);

        assertFailingCreateSamples("No entity type found with this id: UNKNOWN_ENTITY_TYPE (Context: [register sample SAMPLE_NAME])",
                sample);
    }

    @Test(enabled = false)
    public void testUnknownContainer()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setSpaceId(SPACE_ID);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);
        sample.setContainerId(UNKNOWN_SAMPLE_ID);

        assertFailingCreateSamples("Unknown ids [UNKNOWN_SAMPLE_ID] (Context: [register samples - verify relationships])", sample);
    }

    @Test(enabled = false)
    public void testUnknownParent()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setSpaceId(SPACE_ID);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);
        sample.setChildIds(Arrays.asList(UNKNOWN_SAMPLE_ID));

        assertFailingCreateSamples("Unknown ids [UNKNOWN_SAMPLE_ID] (Context: [register samples - verify relationships])", sample);
    }

    @Test(enabled = false)
    public void testUnknownChild()
    {
        prepareCreationEnvironment();

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setSpaceId(SPACE_ID);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);
        sample.setParentIds(Arrays.asList(UNKNOWN_SAMPLE_ID));

        assertFailingCreateSamples("Unknown ids [UNKNOWN_SAMPLE_ID] (Context: [register samples - verify relationships])", sample);
    }

    @Test(enabled = false)
    public void testCycleParentChildDependencies()
    {
        SamplePE samplePE = new SamplePE();
        samplePE.setId(NEW_SAMPLE_TECH_ID);
        samplePE.setCode("CYCLE");

        prepareCreationEnvironment();

        prepareSetSampleParents(any(Long.class), equal(Arrays.asList(KNOWN_SAMPLE_TECH_ID)));
        prepareSetSampleChildren(any(Long.class), equal(Arrays.asList(KNOWN_SAMPLE_TECH_ID)));

        prepareListSamplesByIDs(any(Collection.class), Arrays.asList(samplePE));

        prepareListSampleParents(equal(Arrays.asList(new TechId(NEW_SAMPLE_TECH_ID))), Collections.singleton(new TechId(KNOWN_SAMPLE_TECH_ID)));
        prepareListSampleParents(equal(Arrays.asList(new TechId(KNOWN_SAMPLE_TECH_ID))), Collections.singleton(new TechId(NEW_SAMPLE_TECH_ID)));

        SampleCreation sample = new SampleCreation();
        sample.setCode(SAMPLE_NAME);
        sample.setSpaceId(SPACE_ID);
        sample.setTypeId(ENTITY_TYPE_ID);
        sample.setExperimentId(EXPERIMENT_ID);
        sample.setParentIds(Arrays.asList(KNOWN_SAMPLE_ID));
        sample.setChildIds(Arrays.asList(KNOWN_SAMPLE_ID));

        assertFailingCreateSamples("Circular parent dependency found (Context: [verify parent relations for sample CYCLE])", sample);
    }

    private RecordingMatcherRepository prepareCreationEnvironment()
    {
        prepareGetSession();
        prepareFindEntityTypeByCode(EntityKind.SAMPLE, UNKNOWN_ENTITY_TYPE, null);

        prepareEntityTypes(type1);

        prepareFindSpace(SPACE_CODE, space);

        prepareFindSpace(UNKNOWN_SPACE_ID.getPermId(), null);

        prepareFindPropertyTypeByCode(PROPERTY_TYPE_CODE, propertyType1);

        prepareFindTag(session.getUserName(), TAG_CODE_ID.getCode(), tag);
        prepareFindTag(session.getUserName(), UNKNOWN_TAG_CODE_ID.getCode(), null);

        prepareFindExperiment(UNKNOWN_EXPERIMENT_ID.getPermId(), null);
        prepareFindExperiment(EXPERIMENT_ID.getPermId(), experiment);

        prepareFindSample(UNKNOWN_SAMPLE_ID.getPermId(), null);
        prepareFindSample(KNOWN_SAMPLE_ID.getPermId(), knownSample);

        prepareListEntityPropertyTypesEmpty();
        prepareHasNoDataSets();

        RecordingMatcherRepository repository = new RecordingMatcherRepository();
        prepareCreateAttachment(repository);
        final RecordingMatcher<SamplePE> sampleRecorder =
                repository.getRecordingMatcher(SamplePE.class);
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).createOrUpdateSample(with(sampleRecorder),
                            with(person));
                }
            });
        return repository;
    }

    private void assertCreateSampleOK(SampleCreation... samples)
    {
        List<SamplePermId> result = server.createSamples(SESSION_TOKEN, Arrays.asList(samples));
        assertEquals(samples.length, result.size());
    }

    private void assertFailingCreateSamples(String expectedExceptionMessage,
            SampleCreation... samples)
    {
        try
        {
            server.createSamples(SESSION_TOKEN, Arrays.asList(samples));
            fail("Expecting UserFailureException");
        } catch (UserFailureException e)
        {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }
}
