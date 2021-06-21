/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP2;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Test cases for corresponding {@link SampleBO} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleBOTest extends AbstractBOTest
{
    private static final int VERSION = 4711;

    private static final String SAMPLE_TYPE = "sample-type";

    private static final TechId SAMPLE_TECH_ID = CommonTestUtils.TECH_ID;

    private static final String DB = "DB";

    private static final String DILUTION_PLATE = "DILUTION_PLATE";

    private static final String MASTER_PLATE = "MASTER_PLATE";

    private static final String DEFAULT_SAMPLE_CODE = "XX";

    static SamplePE createSample(final String sampleCode)
    {
        return createSample(sampleCode, "sample-type-code");
    }

    private static SamplePE createAnySample()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(DEFAULT_SAMPLE_CODE);
        sample.setModificationDate(new Date());
        sample.setSampleType(createSampleTypePE(SAMPLE_TYPE));
        return sample;
    }

    private static SampleTypePE createSampleTypePE(String typeCode)
    {
        SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(typeCode);
        sampleType.setSampleTypePropertyTypes(new HashSet<SampleTypePropertyTypePE>());
        return sampleType;
    }

    private static SamplePE createSample(final SampleIdentifier sampleIdentifier,
            final String sampleTypeCode)
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(sampleIdentifier.getSampleCode());
        sample.setRegistrator(EXAMPLE_PERSON);
        sample.setSampleType(createSampleTypePE(sampleTypeCode));
        return sample;
    }

    private static SamplePE createSample(final String sampleCode, final String sampleTypeCode)
    {
        return createSample(SampleIdentifier.createHomeGroup(sampleCode), sampleTypeCode);
    }

    private final static IEntityProperty createSampleProperty()
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue("blue");
        final PropertyType propertyType = new PropertyType();
        propertyType.setLabel("color");
        propertyType.setCode("color");
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    private final static SampleIdentifier getGroupSampleIdentifier(final String code)
    {
        return new SampleIdentifier(IdentifierHelper.createGroupIdentifier(EXAMPLE_GROUP), code);
    }

    private final static SampleIdentifier getSharedSampleIdentifier(final String code)
    {
        return new SampleIdentifier(code);
    }

    @Test
    public void testDefineAndSaveSampleWithProperties()
    {
        final NewSample newSample = new NewSample();

        newSample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        newSample.setSampleType(createSampleType(MASTER_PLATE));

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(MASTER_PLATE);
        sampleType.setId(new Long(21L));
        final IEntityProperty sampleProperty = createSampleProperty();
        newSample.setProperties(new IEntityProperty[]
        { sampleProperty });
        final SamplePropertyPE samplePropertyPE = new SamplePropertyPE();
        samplePropertyPE.setRegistrator(EXAMPLE_SESSION.tryGetPerson());
        final SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
        samplePropertyPE.setEntityTypePropertyType(sampleTypePropertyType);

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, spaceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(MASTER_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            MASTER_PLATE, EXAMPLE_PERSON);
                    final List<SamplePropertyPE> set = new ArrayList<SamplePropertyPE>();
                    set.add(samplePropertyPE);
                    will(returnValue(set));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with(any(String.class)),
                            with(any(SpacePE.class)));
                    will(returnValue(null));

                    allowing(relationshipService).removeSampleFromContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)));

                    BaseMatcher<SamplePE> matcher = new BaseMatcher<SamplePE>()
                        {
                            @Override
                            public void describeTo(final Description description)
                            {
                            }

                            @Override
                            public boolean matches(final Object item)
                            {
                                if (item instanceof SamplePE == false)
                                {
                                    return false;
                                }
                                final SamplePE sample = (SamplePE) item;
                                assertEquals(EXAMPLE_SESSION.tryGetHomeGroup(), sample.getSpace());
                                assertEquals(newSample.getIdentifier(), sample
                                        .getSampleIdentifier().toString());
                                assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
                                return true;
                            }
                        };
                    one(sampleDAO).createOrUpdateSample(with(matcher), with(EXAMPLE_PERSON));

                    extracted(sampleType);

                }

                @SuppressWarnings("unchecked")
                private void extracted(final SampleTypePE type)
                {
                    one(propertiesConverter).checkMandatoryProperties(
                            with(aNonNull(Collection.class)), with(type));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final Set<SamplePropertyPE> properties = sampleBO.getSample().getProperties();
        assertEquals(1, properties.size());
        assertSame(samplePropertyPE, properties.iterator().next());

        sampleBO.save();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public final void testDefineSampleHappyCase()
    {
        final SampleIdentifier sampleIdentifier =
                getGroupSampleIdentifier("SAMPLE_CONTAINER:" + DEFAULT_SAMPLE_CODE);
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier.toString());
        newSample.setSampleType(createSampleType(DILUTION_PLATE));

        final SampleIdentifier generatedFromIdentifier =
                getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSample.setParentIdentifier(generatedFromIdentifier.toString());

        final SampleIdentifier containerIdentifier = getGroupSampleIdentifier("SAMPLE_CONTAINER");
        newSample.setContainerIdentifier(containerIdentifier.toString());

        newSample.setProperties(IEntityProperty.EMPTY_ARRAY);

        final SamplePE generatedFrom = new SamplePE();
        generatedFrom.setRegistrator(EXAMPLE_PERSON);
        generatedFrom.setSpace(EXAMPLE_GROUP);
        generatedFrom.setCode("SAMPLE_GENERATOR");

        final SamplePE container = new SamplePE();
        container.setRegistrator(EXAMPLE_PERSON);
        container.setSpace(EXAMPLE_GROUP);
        container.setCode("SAMPLE_CONTAINER");

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(DILUTION_PLATE);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getRelationshipTypeDAO();
                    will(returnValue(relationshipTypeDAO));

                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, spaceDAO);

                    one(sampleDAO).tryFindByCodeAndSpace(generatedFromIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(generatedFrom));

                    one(sampleDAO).tryFindByCodeAndSpace(containerIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(container));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with(any(String.class)),
                            with(any(SpacePE.class)));
                    will(returnValue(null));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));

                    one(relationshipService).assignSampleToContainer(with(any(IAuthSession.class)),
                            with(any(SamplePE.class)), with(any(SamplePE.class)));

                    one(relationshipService).addParentToSample(with(any(IAuthSession.class)),
                            with(any(SamplePE.class)), with(any(SamplePE.class)));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final SamplePE sample = sampleBO.getSample();
        assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
        assertSame(sampleType, sample.getSampleType());

        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    @SuppressWarnings("unchecked")
    public final void testEditSharedSampleWithAuthorizationFailure()
    {
        final SamplePE sample = createSample("sampleCode", (SpacePE) null);

        prepareTryToLoadOfSampleWithId(sample);

        context.checking(new Expectations()
            {
                {
                    one(entityOperationChecker).assertInstanceSampleUpdateAllowed(
                            with(any(IAuthSession.class)), with(any(List.class)));
                    will(throwException(new AuthorizationFailureException("Update not allowed")));
                }
            });

        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                        .<NewAttachment> emptyList(), 0, null, null, new String[] {}));
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    @SuppressWarnings("unchecked")
    public final void testDefineSharedSampleWithAuthorizationFailure()
    {
        context.checking(new Expectations()
            {
                {
                    one(entityOperationChecker).assertInstanceSampleCreationAllowed(
                            with(any(IAuthSession.class)), with(any(List.class)));
                    will(throwException(new AuthorizationFailureException("Creation not allowed")));
                }
            });

        NewSample newSample = new NewSample();
        newSample.setIdentifier(getSharedSampleIdentifier("sampleCode").toString());
        createSampleBO().define(newSample);
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testDetachFromExperiment()
    {
        final SamplePE sample = createSampleWithExperiment();

        prepareExperimentUpdateOnly(sample);
        context.checking(new Expectations()
            {
                {
                    one(entityOperationChecker).assertInstanceSampleUpdateAllowed(with(any(IAuthSession.class)), with(any(List.class)));
                    one(relationshipService).unassignSampleFromExperiment(EXAMPLE_SESSION, sample);
                }
            });

        ExperimentIdentifier experimentIdentifier = null;

        updateSampleExperiment(SAMPLE_TECH_ID, sample, experimentIdentifier);
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testDetachFromExperimentWithDatasetsFails()
    {
        final SamplePE sample = createSampleWithExperiment();
        DataPE dataset = new DataPE();
        dataset.setCode("DS1");
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode("T");
        dataset.setDataSetType(dataSetType);
        sample.addDataSet(dataset);
        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    one(entityOperationChecker).assertInstanceSampleUpdateAllowed(
                            with(any(IAuthSession.class)), with(any(List.class)));
                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(true));
                }
            });

        try
        {
            updateSampleExperiment(SAMPLE_TECH_ID, sample, null);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("The dataset 'DS1' cannot be connected to the sample '/XX' because the new sample "
                    + "is not connected to any experiment and the data set type (T) doesn't "
                    + "match one of the following regular expressions: NE.*.", e.getMessage());
        }
    }

    @Test
    public final void testEditSampleChangeGroupToShared()
    {
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));

                    one(relationshipService).shareSample(with(any(IAuthSession.class)),
                            with(any(SamplePE.class)));
                }
            });
        String newSampleIdentifierWithoutDb = "/" + sample.getCode();
        assertNotNull(sample.getSpace());
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                        .<NewAttachment> emptyList(), VERSION, SampleIdentifierFactory
                        .parse(newSampleIdentifierWithoutDb), null, null));
        context.assertIsSatisfied();

    }

    public final void testEditExperiment()
    {
        final ProjectPE project = createProject();
        // create experiment which we will attach the sample
        final ExperimentPE experimentToAttach = new ExperimentPE();
        experimentToAttach.setId(1L);
        experimentToAttach.setCode("exp1");
        experimentToAttach.setProject(project);
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode("exp1");
        experimentIdentifier.setProjectCode(project.getCode());
        experimentIdentifier.setSpaceCode(project.getSpace().getCode());

        // create a sample already attached to an experiment
        final ExperimentPE sampleExperiment = new ExperimentPE();
        sampleExperiment.setId(2L);
        sampleExperiment.setCode("exp2");
        sampleExperiment.setProject(project);
        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        sample.setCode("sampleCode");
        sample.setExperiment(sampleExperiment);
        sample.setSpace(EXAMPLE_GROUP);
        sample.setSampleType(createSampleTypePE(SAMPLE_TYPE));

        int version = VERSION;
        sample.setVersion(version);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    allowing(projectDAO).tryFindProject(
                            experimentIdentifier.getSpaceCode(),
                            experimentIdentifier.getProjectCode());
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project,
                            experimentIdentifier.getExperimentCode());
                    will(returnValue(experimentToAttach));

                    one(relationshipService).assignSampleToExperiment(EXAMPLE_SESSION, sample, experimentToAttach);
                    one(relationshipService).assignSampleToProject(EXAMPLE_SESSION, sample, project);
                }
            });
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, experimentIdentifier, null, Collections
                        .<NewAttachment> emptyList(), version, null, null, null));

        context.assertIsSatisfied();
    }

    @Test
    public final void testEditSampleParent()
    {
        final SamplePE parent = createSample("sampleParent", EXAMPLE_GROUP);
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(daoFactory).getRelationshipTypeDAO();
                    will(returnValue(relationshipTypeDAO));

                    allowing(spaceDAO).tryFindSpaceByCode(
                            parent.getSpace().getCode());
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndSpace(parent.getCode(), EXAMPLE_GROUP);
                    will(returnValue(parent));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with(any(String.class)),
                            with(any(SpacePE.class)));
                    will(returnValue(null));

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));

                    allowing(relationshipService).removeSampleFromContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)));

                    one(relationshipService).addParentToSample(with(any(IAuthSession.class)),
                            with(any(SamplePE.class)), with(any(SamplePE.class)));

                }
            });
        assertNull(sample.getGeneratedFrom());
        String[] modifiedParents = new String[]
        { parent.getIdentifier() };
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                        .<NewAttachment> emptyList(), VERSION, IdentifierHelper.sample(sample),
                        null, modifiedParents));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditSampleParents()
    {
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);
        final SamplePE parent1Group1 = createSample("sampleParent1", EXAMPLE_GROUP);
        final SamplePE parent2Group1 = createSample("sampleParent2", EXAMPLE_GROUP);
        final SamplePE parent3Group2 = createSample("sampleParent3", EXAMPLE_GROUP2);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(daoFactory).getRelationshipTypeDAO();
                    will(returnValue(relationshipTypeDAO));

                    allowing(spaceDAO).tryFindSpaceByCode(
                            EXAMPLE_GROUP.getCode());
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(spaceDAO).tryFindSpaceByCode(
                            EXAMPLE_GROUP2.getCode());
                    will(returnValue(EXAMPLE_GROUP2));

                    allowing(sampleDAO).tryFindByCodeAndSpace(parent1Group1.getCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(parent1Group1));

                    allowing(sampleDAO).tryFindByCodeAndSpace(parent2Group1.getCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(parent2Group1));

                    allowing(sampleDAO).tryFindByCodeAndSpace(parent3Group2.getCode(),
                            EXAMPLE_GROUP2);
                    will(returnValue(parent3Group2));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with(any(String.class)),
                            with(any(SpacePE.class)));
                    will(returnValue(null));

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));

                    allowing(relationshipService).removeSampleFromContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)));

                    atLeast(3).of(relationshipService).addParentToSample(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)),
                            with(any(SamplePE.class)));

                }
            });
        assertEquals(0, sample.getParents().size());
        String[] modifiedParents =
                new String[]
                { parent1Group1.getIdentifier(), parent2Group1.getCode(),
                        parent3Group2.getIdentifier() };
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                        .<NewAttachment> emptyList(), VERSION, IdentifierHelper.sample(sample),
                        null, modifiedParents));
        context.assertIsSatisfied();
    }

    private SamplePE createSample(String code, SpacePE group)
    {
        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        sample.setCode(code);
        sample.setSpace(group);
        sample.setSampleType(createSampleTypePE(SAMPLE_TYPE));
        return sample;
    }

    @Test
    public final void testEditSampleContainer()
    {
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);
        final SamplePE container = createSample("sampleContainer", EXAMPLE_GROUP);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    allowing(spaceDAO).tryFindSpaceByCode(
                            container.getSpace().getCode());
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndSpace(container.getCode(), EXAMPLE_GROUP);
                    will(returnValue(container));

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));

                    allowing(relationshipService).unassignSampleFromExperiment(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)));

                    allowing(spaceDAO).tryFindSpaceByCode(with("MY_GROUP"));
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with("sampleCode"),
                            with(any(SpacePE.class)));
                    will(returnValue(sample));

                    oneOf(relationshipService).assignSampleToContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)),
                            with(any(SamplePE.class)));

                }
            });
        assertNull(sample.getContainer());
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                        .<NewAttachment> emptyList(), VERSION, IdentifierHelper.sample(sample),
                        container.getSampleIdentifier().toString(), null));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditAndSaveSamplesContainerFromDifferentGroup()
    {
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);
        final SamplePE container = createSample("sampleContainer", EXAMPLE_GROUP2);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    allowing(spaceDAO).tryFindSpaceByCode(
                            container.getSpace().getCode());
                    will(returnValue(EXAMPLE_GROUP2));

                    allowing(sampleDAO).tryFindByCodeAndSpace(container.getCode(), EXAMPLE_GROUP2);
                    will(returnValue(container));

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));

                    one(sampleDAO).createOrUpdateSample(sample, EXAMPLE_PERSON);
                    one(propertiesConverter).checkMandatoryProperties(sample.getProperties(),
                            sample.getSampleType());

                    allowing(spaceDAO).tryFindSpaceByCode(with("MY_GROUP"));
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with("sampleCode"),
                            with(any(SpacePE.class)));
                    will(returnValue(sample));

                    oneOf(relationshipService).assignSampleToContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)),
                            with(any(SamplePE.class)));
                }
            });
        assertNull(sample.getContainer());
        SampleBO bo = createSampleBO();
        bo.update(new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                .<NewAttachment> emptyList(), VERSION, IdentifierHelper.sample(sample), container
                .getSampleIdentifier().toString(), null));
        bo.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditSampleNoExperimentForSampleWithDatasets()
    {
        final SamplePE sample = createSample("sampleCode", EXAMPLE_GROUP);
        DataPE dataset = new DataPE();
        dataset.setCode("DS1");
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode("T");
        dataset.setDataSetType(dataSetType);
        sample.addDataSet(dataset);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(123L);
        sample.setExperiment(experiment);

        sample.setVersion(VERSION);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(true));
                }
            });

        try
        {
            createSampleBO().update(
                    new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, Collections
                            .<NewAttachment> emptyList(), VERSION, null, null, null));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The dataset 'DS1' cannot be connected to the sample '/MY_GROUP/sampleCode' "
                    + "because the new sample is not connected to any experiment and the data set type (T) "
                    + "doesn't match one of the following regular expressions: NE.*.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditStaleSampleFails()
    {
        SamplePE sample = new SamplePE();
        sample.setVersion(VERSION - 1);
        sample.setId(SAMPLE_TECH_ID.getId());
        sample.setSpace(EXAMPLE_GROUP);

        prepareTryToLoadOfSampleWithId(sample);
        try
        {
            SampleUpdatesDTO updates =
                    new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, null, 
                            Collections.<NewAttachment> emptyList(), VERSION, null, null, null);
            createSampleBO().update(updates);
        } catch (UserFailureException e)
        {
            return;
        }
        fail("The edition of stale sample should throw an exception");
    }

    @SuppressWarnings("deprecation")
    public final void testFailToDefineSharedSampleWithParentInAGroup()
    {
        final SampleIdentifier sharedSampleIdentifier =
                getSharedSampleIdentifier(DEFAULT_SAMPLE_CODE);
        final NewSample newSharedSample = new NewSample();
        newSharedSample.setIdentifier(sharedSampleIdentifier.toString());
        final SampleType sampleType = createSampleType(DILUTION_PLATE);
        newSharedSample.setSampleType(sampleType);

        final SampleIdentifier parentGroupIdentifier = getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSharedSample.setParentIdentifier(parentGroupIdentifier.toString());

        newSharedSample.setProperties(IEntityProperty.EMPTY_ARRAY);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, spaceDAO);

                    final SamplePE groupParent = new SamplePE();
                    groupParent.setRegistrator(EXAMPLE_PERSON);
                    groupParent.setSpace(EXAMPLE_GROUP);
                    groupParent.setCode("SAMPLE_GENERATOR");
                    one(sampleDAO).tryFindByCodeAndSpace(parentGroupIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(groupParent));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(asPE(sampleType)));

                    one(propertiesConverter).convertProperties(newSharedSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSharedSample);
        boolean exceptionThrown = false;
        try
        {
            sampleBO.save();
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("has to be in the same group"));
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUndefinedSample()
    {
        try
        {
            createSampleBO().getSample();
            fail("UserFailureException expected");
        } catch (final IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }
    }

    @Test
    public void testLoadByGroupSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getGroupSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfGroupSample(sampleIdentifier, sample);

        SampleBO sampleBO = createSampleBO();
        sampleBO.loadBySampleIdentifier(sampleIdentifier);

        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownContainer()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(DILUTION_PLATE));
        sample.setContainerIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, spaceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(IEntityProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndSpace("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public final void testRegisterSampleWithUnknownParent()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(DILUTION_PLATE));
        sample.setParentIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, spaceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(IEntityProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndSpace("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));

                    allowing(sampleDAO).tryFindByCodeAndSpace(with(any(String.class)),
                            with(any(SpacePE.class)));
                    will(returnValue(null));

                    allowing(relationshipService).removeSampleFromContainer(
                            with(any(IAuthSession.class)), with(any(SamplePE.class)));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToLoadBySampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfInstanceSample(sampleIdentifier, sample);

        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);

        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToLoadByUnkownSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        prepareTryToLoadOfInstanceSample(sampleIdentifier, null);

        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        try
        {
            sampleBO.getSample();
            fail("IllegalStateException expected.");
        } catch (IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }

        assertSame(null, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    private ProjectPE createProject()
    {
        ProjectPE project = new ProjectPE();
        project.setCode("code");
        project.setSpace(EXAMPLE_GROUP);
        return project;
    }

    private final SampleBO createSampleBO()
    {
        return new SampleBO(daoFactory, EXAMPLE_SESSION, propertiesConverter, relationshipService,
                entityOperationChecker, managedPropertyEvaluatorFactory, new DataSetTypeWithoutExperimentChecker("NE.*"));
    }

    private SampleType createSampleType(final String sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    private static SampleTypePE asPE(SampleType sampleType)
    {
        SampleTypePE pe = new SampleTypePE();
        pe.setCode(sampleType.getCode());
        return pe;
    }

    private void prepareExperimentUpdateOnly(final SamplePE sample)
    {
        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        prepareNoDatasetsFound(sample);
    }

    private void prepareNoDatasetsFound(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).hasDataSet(with(sample));
                    will(returnValue(false));
                }
            });
    }

    private void prepareNoPropertiesToUpdate(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(propertiesConverter).updateProperties(sample.getProperties(),
                            sample.getSampleType(), null, EXAMPLE_PERSON,
                            Collections.<String> emptySet());
                }
            });
    }

    private void prepareTryToLoadOfGroupSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(spaceDAO).tryFindSpaceByCode(
                            sampleIdentifier.getSpaceLevel().getSpaceCode());
                    SpacePE group = new SpacePE();
                    will(returnValue(group));

                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndSpace(sampleCode, group);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareTryToLoadOfInstanceSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode);
                    will(returnValue(sample));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareTryToLoadOfSampleWithId(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryGetByTechId(with(new TechId(sample.getId())),
                            with(any(String[].class)));
                    will(returnValue(sample));
                }
            });
    }

    private SamplePE createSampleWithExperiment()
    {
        final SamplePE sample = createAnySample();
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(1234L);
        sample.setExperiment(experiment);
        sample.setId(SAMPLE_TECH_ID.getId());
        return sample;
    }

    private void updateSampleExperiment(final TechId sampleId, final SamplePE sample,
            ExperimentIdentifier experimentIdentifier)
    {
        createSampleBO().update(
                new SampleUpdatesDTO(sampleId, null, experimentIdentifier, null, Collections
                        .<NewAttachment> emptyList(), sample.getVersion(), null, null, null));
    }
}
