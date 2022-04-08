/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectFreezingTest extends FreezingTest
{
    private static final String PREFIX = "PFT-";

    private static final String PROJECT_1 = PREFIX + "1";

    private static final String PROJECT_2 = PREFIX + "2";

    private ProjectPermId project1;

    private ProjectPermId project2;

    @BeforeMethod
    public void createExamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ProjectCreation p1 = project(DEFAULT_SPACE_ID, PROJECT_1);
        p1.setAttachments(Arrays.asList(attachment("f1.txt", "T1", "my t1", "abcdefgh")));
        ProjectCreation p2 = project(DEFAULT_SPACE_ID, PROJECT_2);
        List<ProjectPermId> projects = v3api.createProjects(sessionToken, Arrays.asList(p1, p2));
        project1 = projects.get(0);
        project2 = projects.get(1);
        v3api.logout(sessionToken);
    }

    @Test
    public void testDelete()
    {
        // Given
        setFrozenFlagForProjects(true, project2);
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteProjects(systemSessionToken, Arrays.asList(project2), deletionOptions),
                // Then
                "ERROR: Operation DELETE is not allowed because project " + PROJECT_2 + " is frozen.");
    }

    @Test
    public void testDeleteMoltenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project2);
        setFrozenFlagForProjects(false, project2);
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deleteProjects(systemSessionToken, Arrays.asList(project2), deletionOptions);

        // Then
        assertEquals(getProject(project2), null);
    }

    @Test
    public void testSetDescription()
    {
        // Given
        setFrozenFlagForProjects(true, project1);
        assertEquals(getProject(project1).getDescription(), null);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.setDescription("hello");

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "ERROR: Operation UPDATE is not allowed because project " + PROJECT_1 + " is frozen.");
    }

    @Test
    public void testChangeDescription()
    {
        // Given
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.setDescription("hello");
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        setFrozenFlagForProjects(true, project1);
        assertEquals(getProject(project1).getDescription(), "hello");
        ProjectUpdate projectUpdate2 = new ProjectUpdate();
        projectUpdate2.setProjectId(project1);
        projectUpdate2.setDescription("hello2");

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate2)),
                // Then
                "ERROR: Operation UPDATE is not allowed because project " + PROJECT_1 + " is frozen.");
    }

    @Test
    public void testChangeDescriptionForMoltenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project1);
        assertEquals(getProject(project1).getDescription(), null);
        setFrozenFlagForProjects(false, project1);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.setDescription("hello");

        // When
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        // Then
        assertEquals(getProject(project1).getDescription(), "hello");
    }

    @Test
    public void testAddAttachment()
    {
        // Given
        setFrozenFlagForProjects(true, project2);
        assertEquals(getProject(project2).getAttachments().toString(), "[]");
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project2);
        projectUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "ERROR: Operation INSERT ATTACHMENT is not allowed because project " + PROJECT_2 + " is frozen.");
    }

    @Test
    public void testAddAttachmentForMoltenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project2);
        assertEquals(getProject(project2).getAttachments().toString(), "[]");
        setFrozenFlagForProjects(false, project2);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project2);
        projectUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        // Then
        assertEquals(getProject(project2).getAttachments().get(0).getDescription(), "my f2");
    }

    @Test
    public void testDeleteAttachment()
    {
        // Given
        setFrozenFlagForProjects(true, project1);
        assertEquals(getProject(project1).getAttachments().get(0).getDescription(), "my t1");
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "ERROR: Operation DELETE ATTACHMENT is not allowed because project " + PROJECT_1 + " is frozen.");
    }

    @Test
    public void testDeleteAttachmentForMoltenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project1);
        assertEquals(getProject(project1).getAttachments().get(0).getDescription(), "my t1");
        setFrozenFlagForProjects(false, project1);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        // Then
        assertEquals(getProject(project1).getAttachments().size(), 0);
    }

    @Test
    public void testAssertProjectHasNoDeletedExperiments()
    {
        // Given
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("EXP-" + System.currentTimeMillis());
        experimentCreation.setProjectId(project1);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST", EntityKind.EXPERIMENT));
        List<ExperimentPermId> experimentIds = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation));
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteExperiments(systemSessionToken, experimentIds, deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.freezeForExperiments();

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "Can not freeze project " + project1 + " because it has 1 experiments in the trashcan (1 deletion sets):\n"
                        + "1 experiments (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before project "
                        + project1 + " can be frozen.\n");
    }

    @Test
    public void testAssertProjectHasNoDeletedSamples()
    {
        // Given
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE-" + System.currentTimeMillis());
        sampleCreation.setSpaceId(DEFAULT_SPACE_ID);
        sampleCreation.setProjectId(project1);
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation));
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteSamples(systemSessionToken, sampleIds, deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project1);
        projectUpdate.freezeForSamples();

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "Can not freeze project " + project1 + " because it has 1 objects in the trashcan (1 deletion sets):\n"
                        + "1 objects (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before project "
                        + project1 + " can be frozen.\n");
    }
}
