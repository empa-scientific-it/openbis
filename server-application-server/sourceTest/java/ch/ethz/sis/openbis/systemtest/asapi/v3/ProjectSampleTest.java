/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static junit.framework.Assert.fail;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.UpdateUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SessionBuilder;

/**
 * @author Franz-Josef Elmer
 */
@Transactional(transactionManager = "transaction-manager")
@Rollback(false)
@Test(groups = "project-samples")
public class ProjectSampleTest extends BaseTest
{
    private static final String SYSTEM_USER = "system";

    private static final SpacePermId HOME_SPACE_ID = new SpacePermId("DEFAULT");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ProjectSampleTest.class);

    private static final EntityTypePermId ENTITY_TYPE_UNKNOWN = new EntityTypePermId("UNKNOWN");

    @Autowired
    protected IApplicationServerApi v3api;

    private ISpaceId space1;

    private ISpaceId space2;

    private ProjectPermId project1inSpace1;

    private ProjectPermId project2inSpace1;

    private ProjectPermId project1inSpace2;

    private ProjectPermId project2inSpace2;

    private ProjectPermId project1inHomeSpace;

    private String adminSessionToken;

    private String adminUser;

    @BeforeClass
    public void createData()
    {
        List<SpacePermId> spaces = createSpaces(systemSessionToken, "SPACE1", "SPACE2");
        space1 = spaces.get(0);
        space2 = spaces.get(1);
        List<ProjectPermId> projects = createProjects(systemSessionToken, space1, "PROJECT1", "PROJECT2");
        project1inSpace1 = projects.get(0);
        project2inSpace1 = projects.get(1);
        project1inSpace2 = createProjects(systemSessionToken, space2, "PROJECT1").get(0);
        project2inSpace2 = createProjects(systemSessionToken, space2, "PROJECT2").get(0);
        project1inHomeSpace = createProjects(systemSessionToken, HOME_SPACE_ID, "PROJECT1").get(0);
        createSamples(systemSessionToken, null, null, null, "SHARED1", "SHARED2");
        createSamples(systemSessionToken, space1, null, null, "SAMPLE1", "SAMPLE2");
        createSamples(systemSessionToken, space1, project1inSpace1, null, "SAMPLE3", "SAMPLE4");
        createSamples(systemSessionToken, space2, project2inSpace2, null, "SAMPLE5", "SAMPLE6");
        waitAtLeastASecond(); // to allow checks on modification time stamps
        SessionBuilder session = aSession().withInstanceRole(RoleCode.ADMIN);
        adminUser = session.getUserID();
        adminSessionToken = create(session);
        commonServer.changeUserHomeSpace(adminSessionToken, new TechId(1)); // home space = DEFAULT
    }

    @Override
    @AfterTransaction
    @Test(enabled = false)
    public void cleanDatabase()
    {
        // super method deletes samples, experiments and data sets from the database
    }

    @Test
    public void testCreateASharedSampleWithASharedSampleAsComponent()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setContainerId(new SampleIdentifier(null, null, sampleCode));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions());
        assertEquals(samples.get(sampleIds.get(0)).getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(samples.get(sampleIds.get(1)).getIdentifier().getIdentifier(), "/" + sampleCode + ":A01");
    }

    @Test
    public void testCreateASharedSampleWithASpaceSampleAsComponent()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setContainerId(new SampleIdentifier(null, null, sampleCode));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions());
        assertEquals(samples.get(sampleIds.get(0)).getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(samples.get(sampleIds.get(1)).getIdentifier().getIdentifier(), "/SPACE1/" + sampleCode + ":A01");
    }

    @Test
    public void testCreateASharedSampleWithAProjectSampleAsComponent()
    {
        String projectCode = createUniqueCode("P");
        createProjects(systemSessionToken, space1, projectCode);
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(new ProjectIdentifier("SPACE1", projectCode));
        s2.setContainerId(new SampleIdentifier(null, null, sampleCode));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions());
        assertEquals(samples.get(sampleIds.get(0)).getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(samples.get(sampleIds.get(1)).getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode + ":A01");
    }

    @Test
    public void testCreateASharedSampleWithAProjectExperimentSampleAsComponent()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String experimentCode = createUniqueCode("E");
        createExperiments(systemSessionToken, project, experimentCode);
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(new ProjectIdentifier("SPACE1", projectCode));
        s2.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode, experimentCode));
        s2.setContainerId(new SampleIdentifier(null, null, sampleCode));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions());
        assertEquals(samples.get(sampleIds.get(0)).getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(samples.get(sampleIds.get(1)).getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode + ":A01");
    }

    @Test
    public void testCreateASharedSampleWithAnExperimentSampleAsComponent()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String experimentCode = createUniqueCode("E");
        createExperiments(systemSessionToken, project, experimentCode);
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode, experimentCode));
        s2.setContainerId(new SampleIdentifier(null, null, sampleCode));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions());
        assertEquals(samples.get(sampleIds.get(0)).getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(samples.get(sampleIds.get(1)).getIdentifier().getIdentifier(),
                "/SPACE1/" + projectCode + "/" + sampleCode + ":A01");
    }

    @Test
    public void testCreateSampleAndGetSamplesByPermId()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(sampleCode);
        sampleCreation.setTypeId(ENTITY_TYPE_UNKNOWN);
        sampleCreation.setSpaceId(space1);
        sampleCreation.setProjectId(project1inSpace1);

        List<SamplePermId> ids = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, ids, fetchOptions);
        Sample sample = samples.get(ids.get(0));
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + sampleCode);
        assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
    }

    @Test
    public void testCreateThreeSamplesWithSameCodeInDifferentProjectOfSameSpaceAndGetSamplesByIdentifier()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1inSpace1);
        SampleCreation s2 = new SampleCreation();
        s2.setCode(sampleCode);
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(project2inSpace1);
        SampleCreation s3 = new SampleCreation();
        s3.setCode(sampleCode);
        s3.setTypeId(ENTITY_TYPE_UNKNOWN);
        s3.setSpaceId(space1);

        v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2, s3));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE1/PROJECT2/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE1/" + sampleCode));
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT2");
        assertEquals(samples.get(ids.get(2)).getProject(), null);
    }

    @Test
    public void testCreateProjectSampleWithAProjectSampleComponentFromAnotherSpace()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1inSpace1);
        SamplePermId s1PermId = v3api.createSamples(systemSessionToken, Arrays.asList(s1)).get(0);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space2);
        s2.setProjectId(project1inSpace2);
        s2.setContainerId(s1PermId);

        v3api.createSamples(systemSessionToken, Arrays.asList(s2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withContainer();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE2/PROJECT1/" + sampleCode + ":A01"));
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/SPACE2/PROJECT1");
    }

    @Test
    public void testCreateProjectSampleWithComponentInHomeSpace()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(HOME_SPACE_ID);
        s1.setProjectId(project1inHomeSpace);
        SamplePermId s1PermId = v3api.createSamples(systemSessionToken, Arrays.asList(s1)).get(0);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(HOME_SPACE_ID);
        s2.setProjectId(project1inHomeSpace);
        s2.setContainerId(s1PermId);

        v3api.createSamples(systemSessionToken, Arrays.asList(s2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withContainer();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("//PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("//PROJECT1/" + sampleCode + ":A01"));
        Map<ISampleId, Sample> samples = v3api.getSamples(adminSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/DEFAULT/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/DEFAULT/PROJECT1");
    }

    @Test
    public void testCreateExperimentSample()
    {
        String sampleCode = createUniqueCode("S");
        String expCode = createUniqueCode("E");
        IExperimentId experimentId = createExperiments(systemSessionToken, project1inSpace1, expCode).get(0);
        Date now = sleep();

        SamplePermId sampleId = createSamples(adminSessionToken, space1, null, experimentId, sampleCode).get(0);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withProject().withModifier();
        fetchOptions.withExperiment().withModifier();
        Sample sample = v3api.getSamples(systemSessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + sampleCode);
        assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + expCode);
        assertModification(sample, sample, now, adminUser);
        assertModification(sample.getProject(), sample.getProject(), now, adminUser);
        assertModification(sample.getExperiment(), sample.getExperiment(), now, adminUser);
    }

    @Test
    public void testAssignSpaceSampleToAProject()
    {
        String sampleCode = createUniqueCode("S");
        createSamples(systemSessionToken, space1, null, null, sampleCode);
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/" + sampleCode));
        sampleUpdate.setProjectId(project);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier newSampleIdentifier = new SampleIdentifier("/SPACE1/" + projectCode + "/" + sampleCode);
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(newSampleIdentifier), fetchOptions);
        Sample sample = samples.get(newSampleIdentifier);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        Project sampleProject = sample.getProject();
        assertEquals(sampleProject.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode);
        assertModification(sampleProject, sampleProject, now, adminUser);
    }

    @Test
    public void testAssignHomeSpaceSampleToAProjectInHomeSpace()
    {
        String sampleCode = createUniqueCode("S");
        createSamples(systemSessionToken, HOME_SPACE_ID, null, null, sampleCode);
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, HOME_SPACE_ID, projectCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("//" + sampleCode));
        sampleUpdate.setProjectId(project);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier newSampleIdentifier = new SampleIdentifier("//" + projectCode + "/" + sampleCode);
        Map<ISampleId, Sample> samples = v3api.getSamples(adminSessionToken, Arrays.asList(newSampleIdentifier), fetchOptions);
        Sample sample = samples.get(newSampleIdentifier);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/DEFAULT/" + projectCode + "/" + sampleCode);
        Project sampleProject = sample.getProject();
        assertEquals(sampleProject.getIdentifier().getIdentifier(), "/DEFAULT/" + projectCode);
        assertModification(sampleProject, sampleProject, now, adminUser);
    }

    @Test
    public void testAssignProjectSampleToADifferentProjectInTheSameSpace()
    {
        String projectCode1 = createUniqueCode("P");
        ProjectPermId project1 = createProjects(systemSessionToken, space1, projectCode1).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId spaceSample = createSamples(systemSessionToken, space1, project1, null, sampleCode).get(0);
        String projectCode2 = createUniqueCode("P");
        ProjectPermId project2 = createProjects(systemSessionToken, space1, projectCode2).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/" + projectCode1 + "/" + sampleCode));
        sampleUpdate.setProjectId(project2);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(spaceSample), fetchOptions);
        Sample sample = samples.get(spaceSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2 + "/" + sampleCode);
        Project sampleProject = sample.getProject();
        assertEquals(sampleProject.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2);
        assertModification(sampleProject, sampleProject, now, adminUser);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Map<IProjectId, Project> projects = v3api.getProjects(systemSessionToken,
                Arrays.asList(project1), projectFetchOptions);
        assertModification(projects.get(project1), projects.get(project1), now, adminUser);
    }

    @Test
    public void testAssignProjectSampleWithComponentToAProjectInADifferentSpace()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project1 = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(project1);
        s2.setContainerId(new SampleIdentifier("/SPACE1/" + projectCode + "/" + sampleCode));
        v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));
        ProjectPermId project2 = createProjects(systemSessionToken, space2, projectCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/" + projectCode + "/" + sampleCode));
        sampleUpdate.setSpaceId(space2);
        sampleUpdate.setProjectId(project2);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier containerID = new SampleIdentifier("/SPACE2/" + projectCode + "/" + sampleCode);
        SampleIdentifier componentID = new SampleIdentifier("/SPACE1/" + projectCode + "/" + sampleCode + ":A01");
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(containerID, componentID), fetchOptions);
        Sample sample = samples.get(containerID);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE2/" + projectCode + "/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE2/" + projectCode);
        assertModification(project, project, now, adminUser);
        Sample component = samples.get(componentID);
        assertEquals(component.getModifier().getUserId(), SYSTEM_USER);
        assertEquals(component.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode + ":A01");
        Project componentProject = component.getProject();
        assertEquals(componentProject.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode);
        assertModification(componentProject, componentProject, now, adminUser);
    }

    @Test
    public void testAssignProjectSampleToAProjectInADifferentSpace()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project1 = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId spaceSample = createSamples(systemSessionToken, space1, project1, null, sampleCode).get(0);
        ProjectPermId project2 = createProjects(systemSessionToken, space2, projectCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space2);
        sampleUpdate.setProjectId(project2);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(spaceSample), fetchOptions);
        Sample sample = samples.get(spaceSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE2/" + projectCode + "/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE2/" + projectCode);
        assertModification(project, project, now, adminUser);
    }

    @Test
    public void testAssignExperimentWithProjectSamplesToADifferentProject()
    {
        String projectCode1 = createUniqueCode("P");
        String projectCode2 = projectCode1 + "A";
        createProjects(systemSessionToken, space1, projectCode1, projectCode2);
        String experimentCode = createUniqueCode("E");
        ProjectIdentifier project1 = new ProjectIdentifier("SPACE1", projectCode1);
        ExperimentPermId experiment = createExperiments(systemSessionToken, project1, experimentCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId sample = createSamples(systemSessionToken, space1, project1, experiment, sampleCode).get(0);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(new ProjectIdentifier("SPACE1", projectCode2));
        Date now = sleep();

        v3api.updateExperiments(adminSessionToken, Arrays.asList(experimentUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Sample experimentSample = v3api.getSamples(systemSessionToken, Arrays.asList(sample), fetchOptions).values().iterator().next();
        assertModification(experimentSample, experimentSample, now, adminUser);
        assertEquals(experimentSample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2 + "/" + sampleCode);
        Project project = experimentSample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2);
        assertModification(project, project, now, adminUser);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withModifier();
        Experiment sampleExperiment = v3api.getExperiments(systemSessionToken, Arrays.asList(experiment),
                experimentFetchOptions).values().iterator().next();
        assertModification(sampleExperiment, sampleExperiment, now, adminUser);
    }

    @Test
    public void testDeleteExperimentWithProjectSamples()
    {
        String projectCode = createUniqueCode("P");
        createProjects(systemSessionToken, space1, projectCode);
        String experimentCode = createUniqueCode("E");
        ProjectIdentifier project = new ProjectIdentifier("SPACE1", projectCode);
        ExperimentPermId experiment = createExperiments(systemSessionToken, project, experimentCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId sample = createSamples(systemSessionToken, space1, project, experiment, sampleCode).get(0);
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("a test");

        v3api.deleteExperiments(adminSessionToken, Arrays.asList(experiment), deletionOptions);

        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(sample), new SampleFetchOptions()).size(), 0);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withModifier();
        assertEquals(v3api.getExperiments(systemSessionToken, Arrays.asList(experiment),
                experimentFetchOptions).size(), 0);
    }

    @Test
    public void testUnassignProjectSampleFromProject()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setProjectId(null);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + sampleCode);
        assertEquals(sample.getProject(), null);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Map<IProjectId, Project> projects = v3api.getProjects(systemSessionToken,
                Arrays.asList(project), projectFetchOptions);
        Project previousProject = projects.values().iterator().next();
        assertModification(previousProject, previousProject, now, adminUser);
    }

    @Test
    public void testUnassignProjectSampleFromProjectAndSpace()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setProjectId(null);
        sampleUpdate.setSpaceId(null);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/" + sampleCode);
        assertEquals(sample.getProject(), null);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Map<IProjectId, Project> projects = v3api.getProjects(systemSessionToken,
                Arrays.asList(project), projectFetchOptions);
        Project previousProject = projects.values().iterator().next();
        assertModification(previousProject, previousProject, now, adminUser);
    }

    @Test
    public void testUnassignProjectExperimentSampleFromExperiment()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String experimentCode = createUniqueCode("E");
        ExperimentPermId experiment = createExperiments(systemSessionToken, project, experimentCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project, experiment, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setExperimentId(null);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        Project sampleProject = sample.getProject();
        assertEquals(sampleProject.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode);
        assertEquals(sampleProject.getModifier().getUserId(), SYSTEM_USER);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withModifier();
        Experiment previousExperiment = v3api.getExperiments(systemSessionToken, Arrays.asList(experiment),
                experimentFetchOptions).values().iterator().next();
        assertModification(previousExperiment, previousExperiment, now, adminUser);
    }

    @Test
    public void testUpdateProjectSample()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/SPACE1/" + projectCode);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Map<IProjectId, Project> projects = v3api.getProjects(systemSessionToken,
                Arrays.asList(project), projectFetchOptions);
        Project sampleProject = projects.values().iterator().next();
        assertEquals(sampleProject.getModifier().getUserId(), SYSTEM_USER);
    }

    @Test
    public void testAssignSpaceSampleToAnExperimentInTheSameSpace()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, null, null, sampleCode).get(0);
        String experimentCode = createUniqueCode("E");
        createExperiments(systemSessionToken, project, experimentCode);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode, experimentCode));
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        fetchOptions.withExperiment().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getProject().getModifier().getUserId(), adminUser);
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + experimentCode);
        assertModification(experiment, experiment, now, adminUser);
    }

    @Test
    public void testAssignProjectSampleToAnExperimentInTheSameProject()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project, null, sampleCode).get(0);
        String experimentCode = createUniqueCode("E");
        createExperiments(systemSessionToken, project, experimentCode);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode, experimentCode));
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        fetchOptions.withExperiment().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getProject().getModifier().getUserId(), SYSTEM_USER);
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + experimentCode);
        assertModification(experiment, experiment, now, adminUser);
    }

    @Test
    public void testAssignProjectSampleWithExperimentToAnotherExperimentInTheSameProject()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String experimentCode1 = createUniqueCode("E");
        String experimentCode2 = experimentCode1 + "A";
        createExperiments(systemSessionToken, project, experimentCode1, experimentCode2);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project,
                new ExperimentIdentifier("SPACE1", projectCode, experimentCode1), sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode, experimentCode2));
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        fetchOptions.withExperiment().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + sampleCode);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getProject().getModifier().getUserId(), SYSTEM_USER);
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode + "/" + experimentCode2);
        assertModification(experiment, experiment, now, adminUser);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withModifier();
        Experiment previousExperiment = v3api.getExperiments(systemSessionToken,
                Arrays.asList(new ExperimentIdentifier("SPACE1", projectCode, experimentCode1)),
                experimentFetchOptions).values().iterator().next();
        assertModification(previousExperiment, previousExperiment, now, adminUser);
    }

    @Test
    public void testAssignProjectSampleToAnExperimentInADifferentProjectOfTheSameSpace()
    {
        String projectCode1 = createUniqueCode("P");
        ProjectPermId project1 = createProjects(systemSessionToken, space1, projectCode1).get(0);
        String sampleCode = createUniqueCode("S");
        SamplePermId projectSample = createSamples(systemSessionToken, space1, project1, null, sampleCode).get(0);
        String experimentCode = createUniqueCode("E");
        String projectCode2 = createUniqueCode("P");
        ProjectPermId project2 = createProjects(systemSessionToken, space1, projectCode2).get(0);
        createExperiments(systemSessionToken, project2, experimentCode);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(projectSample);
        sampleUpdate.setProjectId(new ProjectIdentifier("SPACE1", projectCode2));
        sampleUpdate.setExperimentId(new ExperimentIdentifier("SPACE1", projectCode2, experimentCode));
        Date now = sleep();

        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        fetchOptions.withExperiment().withModifier();
        Map<ISampleId, Sample> samples = v3api.getSamples(systemSessionToken, Arrays.asList(projectSample), fetchOptions);
        Sample sample = samples.get(projectSample);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2 + "/" + sampleCode);
        assertModification(sample, sample, now, adminUser);
        assertModification(sample.getProject(), sample.getProject(), now, adminUser);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Project proj1 = v3api.getProjects(systemSessionToken, Arrays.asList(project1), projectFetchOptions).get(project1);
        assertModification(proj1, proj1, now, adminUser);
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().getIdentifier(), "/SPACE1/" + projectCode2 + "/" + experimentCode);
        assertModification(experiment, experiment, now, adminUser);
    }

    @Test
    public void testDeleteProjectSample()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        createSamples(systemSessionToken, space1, project, null, sampleCode);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        List<SampleIdentifier> sampleIds = Arrays.asList(new SampleIdentifier("/SPACE1/" + projectCode + "/" + sampleCode));
        Date now = sleep();

        v3api.deleteSamples(adminSessionToken, sampleIds, deletionOptions);

        assertEquals(v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions()).size(), 0);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Project sampleProject = v3api.getProjects(systemSessionToken, Arrays.asList(project),
                projectFetchOptions).values().iterator().next();
        assertModification(sampleProject, sampleProject, now, adminUser);
    }

    @Test
    public void testDeleteProjectExperimentSample()
    {
        String projectCode = createUniqueCode("P");
        ProjectPermId project = createProjects(systemSessionToken, space1, projectCode).get(0);
        String sampleCode = createUniqueCode("S");
        String experimentCode = createUniqueCode("E");
        ExperimentPermId experiment = createExperiments(systemSessionToken, project, experimentCode).get(0);
        SamplePermId sample = createSamples(systemSessionToken, space1, project, experiment, sampleCode).get(0);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        List<SamplePermId> sampleIds = Arrays.asList(sample);
        Date now = sleep();

        v3api.deleteSamples(adminSessionToken, sampleIds, deletionOptions);

        assertEquals(v3api.getSamples(systemSessionToken, sampleIds, new SampleFetchOptions()).size(), 0);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Project sampleProject = v3api.getProjects(systemSessionToken, Arrays.asList(project),
                projectFetchOptions).values().iterator().next();
        assertModification(sampleProject, sampleProject, now, adminUser);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withModifier();
        Experiment sampleExperiment = v3api.getExperiments(systemSessionToken, Arrays.asList(experiment),
                experimentFetchOptions).values().iterator().next();
        assertModification(sampleExperiment, sampleExperiment, now, adminUser);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndSpaceInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setSpaceId(space1);
        creation.setProjectId(project1inSpace2);

        try
        {
            v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            AssertionUtil.assertContains("Sample space must be the same as project space. Sample: "
                    + "/SPACE2/PROJECT1/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE", e.getMessage());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndNoSpaceInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setProjectId(project1inSpace1);

        try
        {
            v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            AssertionUtil.assertContains("Shared samples cannot be attached to projects. "
                    + "Sample: /SPACE1/PROJECT1/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE", e.getMessage());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndExperimentInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setSpaceId(space1);
        creation.setProjectId(project2inSpace1);
        String expCode = createUniqueCode("E");
        creation.setExperimentId(createExperiments(systemSessionToken, project1inSpace1, expCode).get(0));

        try
        {
            v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            AssertionUtil.assertContains("Sample project must be the same as experiment project. "
                    + "Sample: /SPACE1/PROJECT2/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT", e.getMessage());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSpaceSampleToProjectInDifferentSpace()
    {
        String code = createUniqueCode("S");
        ISampleId sampleId = createSamples(systemSessionToken, space1, null, null, code).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(project2inSpace2);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
                }
            }, "Sample space must be the same as project space. Sample: /SPACE2/PROJECT2/" + code
                    + " (perm id: " + sampleId + "), Project: /SPACE2/PROJECT2 (perm id: " + project2inSpace2 + ") "
                    + "(Context: [verifying (1/1) {\n" + "  \"entity\" : {\n" + "    \"class\" : \"SamplePE\",\n"
                    + "    \"permId\" : \"" + sampleId + "\",\n"
                    + "    \"identifier\" : \"/SPACE2/PROJECT2/" + code + "\"\n  }\n}])\n");
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSampleOfAnExperimentToProjectDifferentToTheExperimentProject()
    {
        String sampleCode = createUniqueCode("S");
        String expCode = createUniqueCode("E");
        IExperimentId experiment = createExperiments(systemSessionToken, project1inSpace1, expCode).get(0);
        SamplePermId sample = createSamples(systemSessionToken, space1, null, experiment, sampleCode).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample);
        sampleUpdate.setProjectId(project2inSpace1);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
                }
            }, "Sample project must be the same as experiment project. Sample: /SPACE1/PROJECT2/" + sampleCode
                    + " (perm id: " + sample + "), Project: /SPACE1/PROJECT2 (perm id: " + project2inSpace1
                    + "), Experiment: /SPACE1/PROJECT1/" + expCode + " (perm id: " + experiment + ") "
                    + "(Context: [verifying (1/1) {\n" + "  \"entity\" : {\n"
                    + "    \"class\" : \"SamplePE\",\n"
                    + "    \"permId\" : \"" + sample + "\",\n"
                    + "    \"identifier\" : \"/SPACE1/PROJECT2/" + sampleCode + "\"\n  }\n}])\n");

    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSharedSampleToProject()
    {
        String code = createUniqueCode("S");
        ISampleId sharedSample = createSamples(systemSessionToken, null, null, null, code).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setProjectId(project1inSpace1);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
                }
            }, "Shared samples cannot be attached to projects. Sample: /SPACE1/PROJECT1/" + code
                    + " (perm id: " + sharedSample + "), Project: /SPACE1/PROJECT1 (perm id: " + project1inSpace1 + ") "
                    + "(Context: [verifying (1/1) {\n"
                    + "  \"entity\" : {\n"
                    + "    \"class\" : \"SamplePE\",\n"
                    + "    \"permId\" : \"" + sharedSample + "\",\n"
                    + "    \"identifier\" : \"/SPACE1/PROJECT1/" + code + "\"\n  }\n}])\n");
    }

    @Test(priority = -1)
    public void testSearchForSamplesWithProject() throws InterruptedException
    {

        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();

        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/DEFAULT/DEFAULT/DEFAULT");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/DEFAULT/DEFAULT");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(2).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE4");
        assertEquals(result.getObjects().get(2).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(3).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE5");
        assertEquals(result.getObjects().get(3).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getObjects().get(4).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE6");
        assertEquals(result.getObjects().get(4).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getTotalCount(), 5);
    }

    @Test(priority = -1)
    public void testSearchForSamplesWithCodeAndWithProject()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatEndsWith("3");
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();

        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getTotalCount(), 1);
    }

    @Test(priority = -1)
    public void testSearchForSamplesWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withSpace().withCode().thatEquals("SPACE1");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();

        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE4");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getTotalCount(), 2);
    }

    @Test(priority = -1)
    public void testSearchForSamplesWithProjectWithPermId()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withPermId().thatEquals(project2inSpace2.getPermId());
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();

        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE6");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getTotalCount(), 2);
    }

    @Test(priority = -1)
    public void testSearchForSamplesWithoutProjects()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatStartsWith("SAMP");
        searchCriteria.withoutProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();

        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/SAMPLE1");
        assertEquals(result.getObjects().get(0).getProject(), null);
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/SAMPLE2");
        assertEquals(result.getObjects().get(1).getProject(), null);
        assertEquals(result.getTotalCount(), 2);
    }

    private void assertUserFailureException(IDelegatedAction action, String expectedExceptionMessage)
    {
        try
        {
            action.execute();
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            StringBuilder builder = new StringBuilder();
            for (String line : ex.getMessage().split("\n"))
            {
                if (line.contains("\"id\"") == false)
                {
                    builder.append(line).append("\n");
                }
            }
            assertEquals(builder.toString(), expectedExceptionMessage);
        }
    }

    private void assertModification(IModificationDateHolder modificationDateHolder, IModifierHolder modifierHolder,
            Date date, String modifier)
    {
        assertNotOlder(modificationDateHolder.getModificationDate(), date);
        assertEquals(modifierHolder.getModifier().getUserId(), modifier);
    }

    private void assertNotOlder(Date actualDate, Date referenceDate)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String renderedReferenceDate = format.format(referenceDate.getTime());
        String renderedActualDate = format.format(actualDate);
        assertEquals(renderedReferenceDate.compareTo(renderedActualDate) <= 0, true,
                renderedActualDate + " > " + renderedReferenceDate);
    }

    private Date sleep()
    {
        Date now = daoFactory.getTransactionTimestamp();
        return now;
    }

    private List<SpacePermId> createSpaces(String sessionToken, String... spaceCodes)
    {
        List<SpaceCreation> newSpaces = new ArrayList<SpaceCreation>();
        for (String spaceCode : spaceCodes)
        {
            SpaceCreation space = new SpaceCreation();
            space.setCode(spaceCode);
            newSpaces.add(space);
        }
        return v3api.createSpaces(sessionToken, newSpaces);
    }

    private List<ProjectPermId> createProjects(String sessionToken, ISpaceId spaceId, String... projectCodes)
    {
        List<ProjectCreation> newProjects = new ArrayList<ProjectCreation>();
        for (String projectCode : projectCodes)
        {
            ProjectCreation project = new ProjectCreation();
            project.setSpaceId(spaceId);
            project.setCode(projectCode);
            newProjects.add(project);
        }
        return v3api.createProjects(sessionToken, newProjects);
    }

    private List<SamplePermId> createSamples(String sessionToken, ISpaceId spaceOrNull,
            IProjectId projectOrNull, IExperimentId experimentOrNull, String... codes)
    {
        List<SampleCreation> newSamples = new ArrayList<SampleCreation>();
        for (String code : codes)
        {
            SampleCreation sample = new SampleCreation();
            sample.setTypeId(ENTITY_TYPE_UNKNOWN);
            sample.setSpaceId(spaceOrNull);
            sample.setProjectId(projectOrNull);
            sample.setExperimentId(experimentOrNull);
            sample.setCode(code);
            newSamples.add(sample);
        }
        return v3api.createSamples(sessionToken, newSamples);
    }

    private List<ExperimentPermId> createExperiments(String sessionToken, IProjectId project, String... codes)
    {
        List<ExperimentCreation> newExperiments = new ArrayList<ExperimentCreation>();
        for (String code : codes)
        {
            ExperimentCreation experiment = new ExperimentCreation();
            experiment.setCode(code);
            experiment.setTypeId(ENTITY_TYPE_UNKNOWN);
            experiment.setProjectId(project);
            newExperiments.add(experiment);
        }
        return v3api.createExperiments(sessionToken, newExperiments);
    }

    private String createUniqueCode(String prefix)
    {
        return prefix + "-" + System.currentTimeMillis();
    }

    private void waitAtLeastASecond()
    {
        try
        {
            Thread.sleep(1100);
        } catch (InterruptedException ex)
        {
            // silently ignored
        }
    }
}
