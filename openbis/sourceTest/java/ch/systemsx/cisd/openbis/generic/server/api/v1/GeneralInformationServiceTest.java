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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiterConfig;
import ch.systemsx.cisd.openbis.generic.server.IConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.ExternalDataPEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
// PLEASE, if you add here a new test add also a system test to
// ch.systemsx.cisd.openbis.systemtest.api.v1.GeneralInformationServiceTest
@Friend(toClasses = RoleAssignmentPE.class)
public class GeneralInformationServiceTest extends AbstractServerTestCase
{
    private GeneralInformationService service;

    private ICommonServer commonServer;

    private ISampleLister sampleLister2;

    private ICommonBusinessObjectFactory boFactory;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        sampleLister2 = context.mock(ISampleLister.class, "sampleListerForAPI");

        IConcurrentOperationLimiter operationLimiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(new Properties()));

        service =
                new GeneralInformationService(sessionManager, daoFactory, boFactory,
                        propertiesBatchManager, commonServer, operationLimiter)
                    {
                        @Override
                        protected ISampleLister createSampleLister(PersonPE person)
                        {
                            return sampleLister2;
                        }
                    };
        session.setPerson(ManagerTestTool.createPerson());
    }

    @Test
    public void testListNamedRoleSets()
    {
        prepareGetSession();

        Map<String, Set<Role>> namedRoleSets = service.listNamedRoleSets(SESSION_TOKEN);

        List<Entry<String, Set<Role>>> entries =
                new ArrayList<Entry<String, Set<Role>>>(namedRoleSets.entrySet());
        Collections.sort(entries, new Comparator<Entry<String, Set<Role>>>()
            {
                @Override
                public int compare(Entry<String, Set<Role>> e1, Entry<String, Set<Role>> e2)
                {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
        assertNamedRoles("INSTANCE_ADMIN", "[ADMIN(instance)]", entries.get(0));
        assertNamedRoles("INSTANCE_DISABLED", "[]", entries.get(1));
        assertNamedRoles("INSTANCE_ETL_SERVER", "[ADMIN(instance), " + "ETL_SERVER(instance)]",
                entries.get(2));
        assertNamedRoles("INSTANCE_OBSERVER", "[ADMIN(instance), OBSERVER(instance)]",
                entries.get(3));
        assertNamedRoles("SPACE_ADMIN", "[ADMIN(instance), ADMIN(space)]", entries.get(4));
        assertNamedRoles("SPACE_ETL_SERVER", "[ADMIN(instance), "
                + "ETL_SERVER(instance), ETL_SERVER(space)]", entries.get(5));
        assertNamedRoles("SPACE_OBSERVER", "[ADMIN(instance), ADMIN(space), OBSERVER(instance), "
                + "OBSERVER(space), POWER_USER(space), USER(space)]", entries.get(6));
        assertNamedRoles("SPACE_POWER_USER", "[ADMIN(instance), ADMIN(space), POWER_USER(space)]",
                entries.get(7));
        assertNamedRoles("SPACE_USER",
                "[ADMIN(instance), ADMIN(space), POWER_USER(space), USER(space)]", entries.get(8));
        assertEquals(9, entries.size());
        context.assertIsSatisfied();
    }

    private void assertNamedRoles(String expectedName, String expectedRoles,
            Entry<String, Set<Role>> entry)
    {
        assertEquals(expectedName, entry.getKey());
        List<Role> roles = new ArrayList<Role>(entry.getValue());
        Collections.sort(roles, new Comparator<Role>()
            {
                @Override
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedName, expectedRoles, roles.toString());
    }

    @Test
    public void testListSpacesWithProjectsAndRoleAssignments()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    RoleAssignmentPE assignment1 =
                            createUserAssignment("user1", null, RoleCode.ADMIN);
                    RoleAssignmentPE assignment2 =
                            createUserAssignment("user2", "s2", RoleCode.OBSERVER);
                    RoleAssignmentPE assignment3 =
                            createUserAssignment("user1", "s1", RoleCode.USER);
                    will(returnValue(Arrays.asList(assignment1, assignment2, assignment3)));

                    one(groupDAO).listSpaces();
                    List<SpacePE> spaces = createSpaces("s1", "s2", "s3");
                    will(returnValue(spaces));

                    one(projectDAO).listProjects(spaces.get(0));
                    ProjectPE a = new ProjectPE();
                    a.setId(1L);
                    a.setPermId("1");
                    a.setCode("a");
                    a.setSpace(spaces.get(0));
                    ProjectPE b = new ProjectPE();
                    b.setId(2L);
                    b.setPermId("2");
                    b.setCode("b");
                    b.setSpace(spaces.get(0));
                    will(returnValue(Arrays.asList(a, b)));

                    one(projectDAO).listProjects(spaces.get(1));
                    will(returnValue(Arrays.asList()));

                    one(projectDAO).listProjects(spaces.get(2));
                    ProjectPE c = new ProjectPE();
                    c.setId(3L);
                    c.setPermId("3");
                    c.setCode("c");
                    c.setSpace(spaces.get(0));
                    will(returnValue(Arrays.asList(c)));
                }
            });

        List<SpaceWithProjectsAndRoleAssignments> spaces =
                service.listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);

        assertSpaceAndProjects("s1", "[/s1/a, /s1/b]", spaces.get(0));
        assertRoles("[]", spaces.get(0).getRoles("unknown user"));
        assertRoles("[ADMIN(instance), USER(space)]", spaces.get(0).getRoles("user1"));
        assertRoles("[]", spaces.get(0).getRoles("user2"));

        assertSpaceAndProjects("s2", "[]", spaces.get(1));
        assertRoles("[ADMIN(instance)]", spaces.get(1).getRoles("user1"));
        assertRoles("[OBSERVER(space)]", spaces.get(1).getRoles("user2"));

        assertSpaceAndProjects("s3", "[/s3/c]", spaces.get(2));
        assertRoles("[ADMIN(instance)]", spaces.get(2).getRoles("user1"));
        assertRoles("[]", spaces.get(2).getRoles("user2"));

        assertEquals(3, spaces.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamples()
    {
        prepareGetSession();
        final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher = RecordingMatcher.create();
        prepareSearchForSamples(criteriaMatcher, 1);

        List<Sample> result =
                service.searchForSamples(SESSION_TOKEN, createSearchCriteriaForSample());

        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        assertEquals("ATTRIBUTE CODE: a code AND PROPERTY MY_PROPERTY2: a property value"
                + " (with wildcards)", criteriaMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamplesWithExperiment()
    {
        prepareGetSession();
        final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher = RecordingMatcher.create();
        prepareSearchForSamples(criteriaMatcher, 0);
        prepareSearchForAssociatedSamples(criteriaMatcher);

        List<Sample> result =
                service.searchForSamples(SESSION_TOKEN,
                        createSearchCriteriaForSampleWithExperiment());

        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        assertEquals("[ATTRIBUTE CODE: a code AND PROPERTY MY_PROPERTY2: a property value, "
                + "[EXPERIMENT: ATTRIBUTE CODE: a code AND ATTRIBUTE PROJECT: "
                + "a project AND PROPERTY EXP_PROPERTY: exp property value] (with wildcards)]",
                criteriaMatcher.getRecordedObjects().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamplesWithParent()
    {
        prepareGetSession();
        final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher = RecordingMatcher.create();
        prepareSearchForSamples(criteriaMatcher, 2);
        context.checking(new Expectations()
            {
                {
                    one(sampleLister).getChildToParentsIdsMap(Arrays.asList(42L));
                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    result.put(42L, new HashSet<Long>(Arrays.asList(42L)));
                    will(returnValue(result));
                }
            });

        List<Sample> result =
                service.searchForSamples(SESSION_TOKEN, createSearchCriteriaForSampleWithParent());

        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        assertEquals("[ATTRIBUTE CODE: a code AND PROPERTY MY_PROPERTY2: a property value, "
                // check parent subcriteria
                + "[SAMPLE_PARENT: ATTRIBUTE CODE: parent code AND "
                + "PROPERTY PARENT_PROPERTY: parent property value] (with wildcards), "
                + "ATTRIBUTE CODE: parent code AND PROPERTY PARENT_PROPERTY: "
                + "parent property value (with wildcards)]",
                criteriaMatcher.getRecordedObjects()
                        .toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamplesWithChild()
    {
        prepareGetSession();
        final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher = RecordingMatcher.create();
        prepareSearchForSamples(criteriaMatcher, 2);
        context.checking(new Expectations()
            {
                {
                    one(sampleLister).getParentToChildrenIdsMap(Arrays.asList(42L));
                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    result.put(42L, new HashSet<Long>(Arrays.asList(42L)));
                    will(returnValue(result));
                }
            });

        List<Sample> result =
                service.searchForSamples(SESSION_TOKEN, createSearchCriteriaForSampleWithChild());

        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        assertEquals("[ATTRIBUTE CODE: a code AND PROPERTY MY_PROPERTY2: a property value, "
                // check parent subcriteria
                + "[SAMPLE_CHILD: ATTRIBUTE CODE: child code AND "
                + "PROPERTY CHILD_PROPERTY: child property value] (with wildcards), "
                + "ATTRIBUTE CODE: child code AND PROPERTY CHILD_PROPERTY: "
                + "child property value (with wildcards)]",
                criteriaMatcher.getRecordedObjects()
                        .toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamplesWithContainer()
    {
        prepareGetSession();
        final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher = RecordingMatcher.create();
        prepareSearchForSamples(criteriaMatcher, 1);

        List<Sample> result =
                service.searchForSamples(SESSION_TOKEN,
                        createSearchCriteriaForSampleWithContainer());

        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        assertEquals("[ATTRIBUTE CODE: a code AND PROPERTY MY_PROPERTY2: a property value, "
                + "[SAMPLE_CONTAINER: ATTRIBUTE CODE: container code "
                + "AND PROPERTY CONTAINER_PROPERTY: container property value] (with wildcards)]",
                criteriaMatcher.getRecordedObjects().toString());
        context.assertIsSatisfied();
    }

    private void prepareSearchForAssociatedSamples(
            final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher)
    {
        context.checking(new Expectations()
            {
                {
                    one(hibernateSearchDAO).searchForEntityIds(with(session.getUserName()),
                            with(criteriaMatcher), with(EntityKind.SAMPLE));
                    will(returnValue(Arrays.asList(42L)));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareSearchForSamples(
            final RecordingMatcher<DetailedSearchCriteria> criteriaMatcher,
            final int numberOfSearches)
    {
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleLister(session, session.tryGetPerson().getId());
                    will(returnValue(sampleLister));

                    exactly(numberOfSearches).of(hibernateSearchDAO).searchForEntityIds(
                            with(session.getUserName()), with(criteriaMatcher),
                            with(EntityKind.SAMPLE));
                    will(returnValue(new ArrayList<Long>(Arrays.asList(42L))));

                    one(hibernateSearchDAO).getResultSetSizeLimit();
                    will(returnValue(10));

                    one(sampleLister2).getSamples(with(Arrays.asList(42L)),
                            with(EnumSet.of(SampleFetchOption.PROPERTIES)),
                            with(any(IValidator.class)));
                    ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleBuilder sample =
                            new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleBuilder(1L)
                                    .identifier("/space/code").permID("permId").code("code")
                                    .type("sample-type").typeID(123);
                    will(returnValue(Collections.singletonList(sample.getSample())));
                }
            });
    }

    @Test
    public void testListDataSets()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleTypes(SESSION_TOKEN);
                    SampleType returnSampleType = new SampleType();
                    returnSampleType.setId(Long.valueOf(1));
                    returnSampleType.setCode("sample-type");
                    will(returnValue(Collections.singletonList(returnSampleType)));

                    one(commonServer).listRelatedDataSets(with(SESSION_TOKEN),
                            with(any(DataSetRelatedEntities.class)), with(true));
                    DataSetBuilder dataSet =
                            new DataSetBuilder()
                                    .type("ds-type")
                                    .code("ds-code")
                                    .experiment(
                                            new ExperimentBuilder()
                                                    .identifier("/space/project/exp")
                                                    .getExperiment())
                                    .sample(new SampleBuilder("/space/code").getSample());
                    will(returnValue(Collections.singletonList(dataSet.getDataSet())));
                }
            });

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(Long.valueOf(1));
        initializer.setPermId("permId");
        initializer.setCode("code");
        initializer.setIdentifier("/space/code");
        initializer.setSampleTypeId(Long.valueOf(1));
        initializer.setSampleTypeCode("sample-type");
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
        initializer.setRegistrationDetails(registrationDetails);

        Sample owner = new Sample(initializer);
        List<DataSet> result =
                service.listDataSets(SESSION_TOKEN, Collections.singletonList(owner));
        assertEquals(1, result.size());
        DataSet resultDataSet = result.get(0);
        assertEquals("ds-code", resultDataSet.getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetsWithEmptySampleList()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleTypes(SESSION_TOKEN);
                    SampleTypeBuilder sampleType =
                            new SampleTypeBuilder().id(1L).code("sample-type");
                    will(returnValue(Collections.singletonList(sampleType.getSampleType())));

                    one(commonServer).listRelatedDataSets(with(SESSION_TOKEN),
                            with(any(DataSetRelatedEntities.class)), with(true));
                    will(returnValue(new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData>()));
                }
            });
        ArrayList<Sample> samples = new ArrayList<Sample>();
        List<DataSet> result = service.listDataSets(SESSION_TOKEN, samples);
        assertEquals(0, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetsForSample()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleExternalData(with(SESSION_TOKEN),
                            with(new TechId(1)), with(true));
                    DataSetBuilder dataSet =
                            new DataSetBuilder()
                                    .type("ds-type")
                                    .code("ds-code")
                                    .experiment(
                                            new ExperimentBuilder()
                                                    .identifier("/space/project/exp")
                                                    .getExperiment())
                                    .sample(new SampleBuilder("/space/code").getSample());
                    will(returnValue(Collections.singletonList(dataSet.getDataSet())));
                }
            });

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(Long.valueOf(1));
        initializer.setPermId("permId");
        initializer.setCode("code");
        initializer.setIdentifier("/space/code");
        initializer.setSampleTypeId(Long.valueOf(1));
        initializer.setSampleTypeCode("sample-type");
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
        initializer.setRegistrationDetails(registrationDetails);

        Sample owner = new Sample(initializer);
        List<DataSet> result = service.listDataSetsForSample(SESSION_TOKEN, owner, true);
        assertEquals(1, result.size());
        DataSet resultDataSet = result.get(0);
        assertEquals("ds-code", resultDataSet.getCode());
        context.assertIsSatisfied();
    }

    private void prepareSearchForExperiments()
    {
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    RoleAssignmentPE assignment0 =
                            createUserAssignment("user0", null, RoleCode.ADMIN);
                    RoleAssignmentPE assignment1 =
                            createUserAssignment("user1", "SPACE-1", RoleCode.USER);
                    RoleAssignmentPE assignment2 =
                            createUserAssignment("user1", "SPACE-2", RoleCode.ADMIN);
                    will(returnValue(Arrays.asList(assignment0, assignment1, assignment2)));

                    one(groupDAO).listSpaces();
                    List<SpacePE> spaces = createSpaces("SPACE-1", "SPACE-2");
                    will(returnValue(spaces));

                    one(projectDAO).listProjects(spaces.get(0));
                    ProjectPE project1 = new ProjectPE();
                    project1.setId(1L);
                    project1.setPermId("1");
                    project1.setCode("PROJECT-1");
                    project1.setSpace(spaces.get(0));
                    will(returnValue(Collections.singletonList(project1)));

                    one(projectDAO).listProjects(spaces.get(1));
                    will(returnValue(Collections.emptyList()));

                    ExperimentType returnExperimentType = new ExperimentType();
                    returnExperimentType.setCode("EXP-TYPE-CODE");
                    one(commonServer).listExperimentTypes(SESSION_TOKEN);
                    will(returnValue(Collections.singletonList(returnExperimentType)));

                    ProjectIdentifier projectIdentifier =
                            new ProjectIdentifier("SPACE-1", "PROJECT-1");
                    one(commonServer).listExperiments(SESSION_TOKEN, returnExperimentType,
                            Collections.singletonList(projectIdentifier));

                    Person registrator = new Person();
                    registrator.setEmail("mail@mail.com");
                    registrator.setFirstName("First");
                    registrator.setLastName("Last");
                    registrator.setUserId("personId");

                    ExperimentBuilder experiment =
                            new ExperimentBuilder().id(1L).code("EXP-CODE").permID("EXP-PERMID")
                                    .identifier("/SPACE-1/PROJECT-1/EXP-CODE")
                                    .type(returnExperimentType.getCode()).registrator(registrator)
                                    .date(new Date());
                    will(returnValue(Collections.singletonList(experiment.getExperiment())));
                }
            });
    }

    @Test
    public void testListExperiments()
    {
        prepareGetSession();
        prepareSearchForExperiments();
        List<SpaceWithProjectsAndRoleAssignments> enrichedSpaces =
                service.listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : enrichedSpaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> result = service.listExperiments(SESSION_TOKEN, projects, "EXP-TYPE-CODE");
        assertEquals(1, result.size());
        Experiment resultExperiment = result.get(0);
        assertEquals("/SPACE-1/PROJECT-1/EXP-CODE", resultExperiment.getIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSetMetaData()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    ExperimentPE experiment = new ExperimentPE();
                    experiment.setCode("E1");
                    ExperimentTypePE experimentType = new ExperimentTypePE();
                    experimentType.setCode("MY_EXPERIMENT_TYPE");
                    experiment.setExperimentType(experimentType);
                    ProjectPE project = new ProjectPE();
                    project.setCode("P");
                    project.setSpace(new SpacePEBuilder()
                            .code("S").getSpace());
                    experiment.setProject(project);
                    one(dataSetDAO).tryToFindDataSetByCode("ds1");
                    ExternalDataPEBuilder ds1 =
                            new ExternalDataPEBuilder(1).kind(DataSetKind.PHYSICAL.name()).code("ds1").type("T1").store("S")
                                    .experiment(experiment)
                                    .property("alpha", DataTypeCode.REAL, "3.14159")
                                    .property("status", DataTypeCode.VARCHAR, "normal");
                    will(returnValue(ds1.getDataSet()));
                    one(metaprojectDAO).listMetaprojectsForEntity(session.tryGetPerson(),
                            ds1.getDataSet());
                    will(returnValue(new HashSet<Metaproject>()));

                    one(dataSetDAO).tryToFindDataSetByCode("ds2");
                    ExternalDataPEBuilder ds2 =
                            new ExternalDataPEBuilder(1).code("ds2").type("T2").store("S")
                                    .experiment(experiment)
                                    .property("status", DataTypeCode.VARCHAR, "low")
                                    .parent(ds1.getDataSet());
                    will(returnValue(ds2.getDataSet()));
                    one(metaprojectDAO).listMetaprojectsForEntity(session.tryGetPerson(),
                            ds2.getDataSet());
                    will(returnValue(new HashSet<Metaproject>()));
                }
            });

        List<DataSet> dataSets =
                service.getDataSetMetaData(SESSION_TOKEN, Arrays.asList("ds1", "ds2"));

        assertEquals("ds1", dataSets.get(0).getCode());
        assertEquals("T1", dataSets.get(0).getDataSetTypeCode());
        assertEquals("/S/P/E1", dataSets.get(0).getExperimentIdentifier());
        assertEquals(null, dataSets.get(0).getSampleIdentifierOrNull());
        HashMap<String, String> properties = dataSets.get(0).getProperties();
        assertEquals("3.14159", properties.get("ALPHA"));
        assertEquals("normal", properties.get("STATUS"));
        assertEquals(2, properties.size());
        assertEquals("[ds2]", dataSets.get(0).getChildrenCodes().toString());
        assertEquals("[]", dataSets.get(0).getParentCodes().toString());

        assertEquals("ds2", dataSets.get(1).getCode());
        assertEquals("T2", dataSets.get(1).getDataSetTypeCode());
        assertEquals("/S/P/E1", dataSets.get(1).getExperimentIdentifier());
        assertEquals(null, dataSets.get(1).getSampleIdentifierOrNull());
        assertEquals("{STATUS=low}", dataSets.get(1).getProperties().toString());
        assertEquals("[]", dataSets.get(1).getChildrenCodes().toString());
        assertEquals("[ds1]", dataSets.get(1).getParentCodes().toString());

        assertEquals(2, dataSets.size());
        context.assertIsSatisfied();
    }

    private SearchCriteria createSearchCriteriaForSample()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("MY_PROPERTY2", "a property value"));
        return sc;
    }

    private SearchCriteria createSearchCriteriaForSampleParent()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause
                .createAttributeMatch(MatchClauseAttribute.CODE, "parent code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("PARENT_PROPERTY",
                "parent property value"));
        return sc;
    }

    private SearchCriteria createSearchCriteriaForSampleChild()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "child code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("CHILD_PROPERTY", "child property value"));
        return sc;
    }

    private SearchCriteria createSearchCriteriaForSampleContainer()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "container code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("CONTAINER_PROPERTY",
                "container property value"));
        return sc;
    }

    private SearchCriteria createSearchCriteriaForExperiment()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
                "a project"));
        sc.addMatchClause(MatchClause.createPropertyMatch("EXP_PROPERTY", "exp property value"));
        return sc;
    }

    private SearchCriteria createSearchCriteriaForSampleWithExperiment()
    {
        SearchCriteria mainCriteria = createSearchCriteriaForSample();
        SearchCriteria expCriteria = createSearchCriteriaForExperiment();
        mainCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(expCriteria));
        return mainCriteria;
    }

    private SearchCriteria createSearchCriteriaForSampleWithParent()
    {
        SearchCriteria mainCriteria = createSearchCriteriaForSample();
        SearchCriteria parentCriteria = createSearchCriteriaForSampleParent();
        mainCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria));
        return mainCriteria;
    }

    private SearchCriteria createSearchCriteriaForSampleWithChild()
    {
        SearchCriteria mainCriteria = createSearchCriteriaForSample();
        SearchCriteria childCriteria = createSearchCriteriaForSampleChild();
        mainCriteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(childCriteria));
        return mainCriteria;
    }

    private SearchCriteria createSearchCriteriaForSampleWithContainer()
    {
        SearchCriteria mainCriteria = createSearchCriteriaForSample();
        SearchCriteria containerCriteria = createSearchCriteriaForSampleContainer();
        mainCriteria.addSubCriteria(SearchSubCriteria
                .createSampleContainerCriteria(containerCriteria));
        return mainCriteria;
    }

    private void assertSpaceAndProjects(String expectedSpaceCode, String expectedProjects,
            SpaceWithProjectsAndRoleAssignments space)
    {
        assertEquals(expectedSpaceCode, space.getCode());
        List<Project> projects = space.getProjects();
        Collections.sort(projects, new Comparator<Project>()
            {
                @Override
                public int compare(Project p1, Project p2)
                {
                    return p1.toString().compareTo(p2.toString());
                }
            });
        assertEquals(expectedProjects, projects.toString());
    }

    private void assertRoles(String expectedRoles, Set<Role> roles)
    {
        List<Role> list = new ArrayList<Role>(roles);
        Collections.sort(list, new Comparator<Role>()
            {
                @Override
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedRoles, list.toString());
    }

    private RoleAssignmentPE createUserAssignment(String userID, String spaceCodeOrNull,
            RoleCode roleCode)
    {
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        if (spaceCodeOrNull != null)
        {
            assignment.setSpace(createGroup(spaceCodeOrNull));
        }
        assignment.setRole(roleCode);
        PersonPE person = new PersonPE();
        person.setUserId(userID);
        assignment.setPersonInternal(person);
        return assignment;
    }

    private List<SpacePE> createSpaces(String... codes)
    {
        List<SpacePE> list = new ArrayList<SpacePE>();
        for (String code : codes)
        {
            list.add(createGroup(code));
        }
        return list;
    }

}
