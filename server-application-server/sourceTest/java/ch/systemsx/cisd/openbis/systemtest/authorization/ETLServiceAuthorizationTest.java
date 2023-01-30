/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SessionBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ETLServiceAuthorizationTest extends BaseTest
{
    private Space space;

    private Space anotherSpace;

    private Project project;

    private Experiment experiment;

    private Sample sample;

    private Project anotherProject;

    private Sample sharedSample;

    private Sample childSharedSample;

    @BeforeMethod
    public void createSomeEntities()
    {
        space = create(aSpace());
        anotherSpace = create(aSpace());
        project = create(aProject().inSpace(space));
        anotherProject = create(aProject().inSpace(anotherSpace));
        experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));
        sharedSample = create(aSample());
        childSharedSample = create(aSample().withParent(sharedSample));
        create(aSample().inExperiment(experiment));
    }

    @Test
    public void testListSamplesForInstanceAdmin()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        List<Sample> samples =
                etlService.listSamples(sessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));

        assertEquals(2, samples.size());
    }
    
    @Test
    public void testListSharedSampleByASpaceUser()
    {
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_USER, space));

        List<Sample> samples = etlService.listSamples(sessionToken, ListSampleCriteria.createForParent(new TechId(sharedSample)));
        
        assertEquals(childSharedSample.getId(), samples.get(0).getId());
        assertEquals(1, samples.size());
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testListSamplesForObserverForAnotherSpace()
    {
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, anotherSpace));

        etlService.listSamples(sessionToken,
                ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));
    }

    @Test
    public void testListProjectsForInstanceAdmin()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        List<Project> projects = etlService.listProjects(sessionToken);

        assertContainsProject(project.getIdentifier(), projects);
        assertContainsProject(anotherProject.getIdentifier(), projects);
    }

    @Test
    public void testListProjectsForObserverForSpace()
    {
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));

        List<Project> projects = etlService.listProjects(sessionToken);

        assertEquals(1, projects.size());
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testRegistrationOfSamplesForUnauthorizedUser()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));
        SessionBuilder session = aSession().withSpaceRole(RoleCode.POWER_USER, space);
        create(session);
        String userID = session.getUserID();
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<ProjectUpdatesDTO> projectUpdates = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();
        List<ExperimentUpdatesDTO> experimentUpdates =
                Collections.<ExperimentUpdatesDTO> emptyList();
        List<SampleUpdatesDTO> sampleUpdates = Collections.emptyList();
        NewSample newSample = new NewSample();
        newSample.setIdentifier(anotherSpace.getIdentifier() + "/SAMPLE-1");
        newSample.setSampleType(sample.getSampleType());
        List<NewSample> sampleRegistrations = Arrays.asList(newSample);
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<MaterialUpdateDTO> materialUpdates = Collections.emptyList();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = Collections.emptyList();
        List<NewMetaproject> metaprojectRegistrations = Collections.emptyList();
        List<MetaprojectUpdatesDTO> metaprojectUpdates = Collections.emptyList();
        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        TechId registrationid = new TechId(etlService.drawANewUniqueID(sessionToken));

        etlService.performEntityOperations(sessionToken, new AtomicEntityOperationDetails(
                registrationid, userID, spaceRegistrations, projectRegistrations, projectUpdates,
                experimentRegistrations, experimentUpdates, sampleUpdates, sampleRegistrations,
                materialRegistrations, materialUpdates, dataSetRegistrations, dataSetUpdates,
                metaprojectRegistrations, metaprojectUpdates, vocabularyUpdates));
    }

    private void assertContainsProject(String expectedProjectIdentifer, List<Project> projects)
    {
        for (Project p : projects)
        {
            if (p.getIdentifier().equals(expectedProjectIdentifer))
            {
                return;
            }
        }
        fail("Missing project " + expectedProjectIdentifer + " in " + projects);
    }
}
