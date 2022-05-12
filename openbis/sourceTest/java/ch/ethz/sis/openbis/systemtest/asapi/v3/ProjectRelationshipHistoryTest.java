/*
 * Copyright 2021 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectRelationshipHistoryTest extends AbstractTest
{
    private SpacePermId spaceId1;

    private SpacePermId spaceId2;

    private ProjectPermId projectId1;

    private ProjectPermId projectId2;

    @BeforeMethod
    public void setUp()
    {
        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode("ProjectRelationshipHistoryTest1");
        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode("ProjectRelationshipHistoryTest2");
        List<SpacePermId> spaces = v3api.createSpaces(systemSessionToken, Arrays.asList(spaceCreation1, spaceCreation2));
        spaceId1 = spaces.get(0);
        spaceId2 = spaces.get(1);
        ProjectCreation projectCreation1 = new ProjectCreation();
        projectCreation1.setSpaceId(spaceId1);
        projectCreation1.setCode("TEST1");
        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setSpaceId(spaceId2);
        projectCreation2.setCode("TEST2");
        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation1, projectCreation2));
        projectId1 = projects.get(0);
        projectId2 = projects.get(1);
    }

    @Test
    public void moveProjectToAnotherSpace()
    {
        // Given
        // move project to another space
        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId1);
        update.setSpaceId(spaceId2);
        v3api.updateProjects(systemSessionToken, Arrays.asList(update));
        // delete original space
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(spaceId1), deletionOptions);
        assertEquals(v3api.getSpaces(systemSessionToken, Arrays.asList(spaceId1), new SpaceFetchOptions()).size(), 0);

        // When
        Project project = getProjectHistory();
        List<HistoryEntry> history = project.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, ProjectRelationType.SPACE, project.getRegistrationDate(), project.getModificationDate());
        assertRelationshipHistory(history.get(1), spaceId2, ProjectRelationType.SPACE, project.getModificationDate(), null);
    }

    @Test
    public void moveExperimentToAnotherProject()
    {
        // Given
        // create an experiment
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST");
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setProjectId(projectId1);
        ExperimentPermId id = v3api.createExperiments(systemSessionToken, Arrays.asList(creation)).get(0);
        // move experiment to a project
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(id);
        update.setProjectId(projectId2);
        v3api.updateExperiments(systemSessionToken, Arrays.asList(update));
        // delete experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getExperiments(systemSessionToken, Arrays.asList(id), new ExperimentFetchOptions()).size(), 0);

        // When
        Project project = getProjectHistory();
        List<HistoryEntry> history = project.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, ProjectRelationType.SPACE, project.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), id, ProjectRelationType.EXPERIMENT);
    }

    @Test
    public void moveProjectSampleToAnotherSpace()
    {
        // Given
        // create an sample
        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST");
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setSpaceId(spaceId1);
        creation.setProjectId(projectId1);
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // move sample to another space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setSpaceId(spaceId2);
        update.setProjectId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete sample
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(id), new SampleFetchOptions()).size(), 0);

        // When
        Project project = getProjectHistory();
        List<HistoryEntry> history = project.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, ProjectRelationType.SPACE, project.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), id, ProjectRelationType.SAMPLE);
    }

    private Project getProjectHistory()
    {
        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withHistory();
        return v3api.getProjects(systemSessionToken, Arrays.asList(projectId1), fo).get(projectId1);
    }
}
