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
import java.util.stream.Collectors;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRelationshipHistoryTest extends AbstractTest
{
    private SpacePermId spaceId1;

    private SpacePermId spaceId2;

    private ProjectPermId projectId1;

    private ProjectPermId projectId2;

    private ExperimentPermId experimentId1;

    private ExperimentPermId experimentId2;

    private SamplePermId sharedSampleId;

    private SamplePermId spaceSampleId;

    private SamplePermId projectSampleId;

    private SamplePermId experimentSampleId;

    @BeforeMethod
    public void setUp()
    {
        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode("SampleRelationshipHistoryTest1");
        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode("SampleRelationshipHistoryTest2");
        List<SpacePermId> spaces = v3api.createSpaces(systemSessionToken, Arrays.asList(spaceCreation1, spaceCreation2));
        spaceId1 = spaces.get(0);
        spaceId2 = spaces.get(1);

        ProjectCreation projectCreation1 = new ProjectCreation();
        projectCreation1.setSpaceId(spaceId2);
        projectCreation1.setCode("TEST1");
        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setSpaceId(spaceId2);
        projectCreation2.setCode("TEST2");
        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation1, projectCreation2));
        projectId1 = projects.get(0);
        projectId2 = projects.get(1);

        ExperimentCreation experimentCreation1 = new ExperimentCreation();
        experimentCreation1.setProjectId(projectId2);
        experimentCreation1.setCode("TEST1");
        experimentCreation1.setTypeId(new EntityTypePermId("DELETION_TEST"));
        ExperimentCreation experimentCreation2 = new ExperimentCreation();
        experimentCreation2.setProjectId(projectId2);
        experimentCreation2.setCode("TEST2");
        experimentCreation2.setTypeId(new EntityTypePermId("DELETION_TEST"));
        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation1, experimentCreation2));
        experimentId1 = experiments.get(0);
        experimentId2 = experiments.get(1);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SHARED");
        sampleCreation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        sharedSampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);
        sampleCreation.setCode("SPACE");
        sampleCreation.setSpaceId(spaceId1);
        spaceSampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);
        sampleCreation.setCode("PROJECT");
        sampleCreation.setSpaceId(spaceId2);
        sampleCreation.setProjectId(projectId1);
        projectSampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);
        sampleCreation.setCode("EXPERIMENT");
        sampleCreation.setProjectId(projectId2);
        sampleCreation.setExperimentId(experimentId1);
        experimentSampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);
    }

    @Test
    void moveSharedSampleToASpace()
    {
        // Given
        // move shared sample to a space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sharedSampleId);
        update.setSpaceId(spaceId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(sharedSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), null, null);
        assertRelationshipHistory(history.get(1), spaceId1, SampleRelationType.SPACE);
    }

    @Test
    void moveSharedSampleToAProject()
    {
        // Given
        // move shared sample to a project
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sharedSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(sharedSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), null, null);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    void moveSharedSampleToAnExperiment()
    {
        // Given
        // move shared sample to an experiment
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sharedSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId2);
        update.setExperimentId(experimentId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(sharedSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), null, null);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    @Test
    void turnSpaceSampleIntoSharedSample()
    {
        // Given
        // remove space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(spaceSampleId);
        update.setSpaceId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete space
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(spaceId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(spaceSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, SampleRelationType.SPACE);
        assertRelationshipHistory(history.get(1), null, null);
    }

    @Test
    void moveSpaceSampleToAnotherSpace()
    {
        // Given
        // move space sample to another space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(spaceSampleId);
        update.setSpaceId(spaceId2);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original space
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(spaceId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(spaceSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, SampleRelationType.SPACE);
        assertRelationshipHistory(history.get(1), spaceId2, SampleRelationType.SPACE);
    }

    @Test
    void moveSpaceSampleToProject()
    {
        // Given
        // move space sample to a project
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(spaceSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original space
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(spaceId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(spaceSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, SampleRelationType.SPACE);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    void moveSpaceSampleToExperiment()
    {
        // Given
        // move space sample to an experiment
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(spaceSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId2);
        update.setExperimentId(experimentId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original space
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(spaceId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(spaceSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), spaceId1, SampleRelationType.SPACE);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    @Test
    void turnProjectSampleIntoSharedSample()
    {
        // Given
        // remove space and project
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.setSpaceId(null);
        update.setProjectId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original project
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteProjects(systemSessionToken, Arrays.asList(projectId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), projectId1, SampleRelationType.PROJECT);
        assertRelationshipHistory(history.get(1), null, null);
    }

    @Test
    void moveProjectSampleToASpace()
    {
        // Given
        // move project sample to a space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.setSpaceId(spaceId1);
        update.setProjectId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original project
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteProjects(systemSessionToken, Arrays.asList(projectId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), projectId1, SampleRelationType.PROJECT);
        assertRelationshipHistory(history.get(1), spaceId1, SampleRelationType.SPACE);
    }

    @Test
    void moveProjectSampleToAnotherProject()
    {
        // Given
        // move project sample to another project
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.setProjectId(projectId2);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original project
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteProjects(systemSessionToken, Arrays.asList(projectId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), projectId1, SampleRelationType.PROJECT);
        assertRelationshipHistory(history.get(1), projectId2, SampleRelationType.PROJECT);
    }

    @Test
    void moveProjectSampleToAnExperiment()
    {
        // Given
        // move project sample to a space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId2);
        update.setExperimentId(experimentId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original project
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteProjects(systemSessionToken, Arrays.asList(projectId1), deletionOptions);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), projectId1, SampleRelationType.PROJECT);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    @Test
    void turnExperimentSampleIntoSharedSample()
    {
        // Given
        // remove space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(experimentSampleId);
        update.setSpaceId(null);
        update.setProjectId(null);
        update.setExperimentId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, SampleRelationType.EXPERIMENT);
        assertRelationshipHistory(history.get(1), null, null);
    }

    @Test
    void moveExperimentSampleToASpace()
    {
        // Given
        // move experiment sample to a space
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(experimentSampleId);
        update.setSpaceId(spaceId1);
        update.setProjectId(null);
        update.setExperimentId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, SampleRelationType.EXPERIMENT);
        assertRelationshipHistory(history.get(1), spaceId1, SampleRelationType.SPACE);
    }

    @Test
    void moveExperimentSampleToAProject()
    {
        // Given
        // move experiment sample to a project
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(experimentSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId1);
        update.setExperimentId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, SampleRelationType.EXPERIMENT);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    void moveExperimentSampleToAnotherExperiment()
    {
        // Given
        // move experiment sample to another experiment
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(experimentSampleId);
        update.setSpaceId(spaceId2);
        update.setProjectId(projectId2);
        update.setExperimentId(experimentId2);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, SampleRelationType.EXPERIMENT);
        assertRelationshipHistory(history.get(1), experimentId2, SampleRelationType.EXPERIMENT);
    }

    @Test
    void createAndRemoveChild()
    {
        // Given
        // create a child
        SampleCreation creation = new SampleCreation();
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode("CHILD");
        creation.setSpaceId(spaceId2);
        creation.setProjectId(projectId1);
        creation.setParentIds(Arrays.asList(projectSampleId));
        SamplePermId childId = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove child
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.getChildIds().remove(childId);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete child
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(childId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(childId), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), childId, SampleRelationType.CHILD);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);

    }

    @Test
    void createAndRemoveParent()
    {
        // Given
        // create a parent
        SampleCreation creation = new SampleCreation();
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode("PARENT");
        creation.setSpaceId(spaceId2);
        creation.setProjectId(projectId1);
        creation.setChildIds(Arrays.asList(projectSampleId));
        SamplePermId parentId = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove parent
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.getParentIds().remove(parentId);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete parent
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(parentId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(parentId), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), parentId, SampleRelationType.PARENT);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    void createAndRemoveComponent()
    {
        // Given
        // create a component
        SampleCreation creation = new SampleCreation();
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode("COMPONENT");
        creation.setSpaceId(spaceId2);
        creation.setProjectId(projectId1);
        creation.setContainerId(projectSampleId);
        SamplePermId componentId = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove component
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.getComponentIds().remove(componentId);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete component
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(componentId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(componentId), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), componentId, SampleRelationType.COMPONENT);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    void createAndRemoveContainer()
    {
        // Given
        // create a container
        SampleCreation creation = new SampleCreation();
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode("CONTAINER");
        creation.setSpaceId(spaceId2);
        creation.setProjectId(projectId1);
        creation.setComponentIds(Arrays.asList(projectSampleId));
        SamplePermId containerId = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove container
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(projectSampleId);
        update.setContainerId(null);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete container
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(containerId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(containerId), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(projectSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), containerId, SampleRelationType.CONTAINER);
        assertRelationshipHistory(history.get(1), projectId1, SampleRelationType.PROJECT);
    }

    @Test
    public void moveSampleDataSetToAnotherSample()
    {
        // Given
        // create a data set for a sample
        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST");
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setSampleId(experimentSampleId);
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // move data set to another sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setSampleId(projectSampleId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete data set
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(id), new DataSetFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, SampleRelationType.DATA_SET);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    @Test
    public void moveExperimentDataSetToASample()
    {
        // Given
        // create a data set for an experiment
        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST");
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setExperimentId(experimentId1);
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // move data set to a sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setSampleId(experimentSampleId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete data set
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(id), new DataSetFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, SampleRelationType.DATA_SET);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    @Test
    public void moveSampleDataSetToAnExperiment()
    {
        // Given
        // create a data set for an experiment
        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST");
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setSampleId(experimentSampleId);
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // move data set to a sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setExperimentId(experimentId1);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete data set
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(id), new DataSetFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getSampleHistory(experimentSampleId);

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, SampleRelationType.DATA_SET);
        assertRelationshipHistory(history.get(1), experimentId1, SampleRelationType.EXPERIMENT);
    }

    private List<RelationHistoryEntry> getSampleHistory(SamplePermId sampleId)
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        Sample sample = v3api.getSamples(systemSessionToken, Arrays.asList(sampleId), fo).get(sampleId);
        return sample.getHistory().stream().map(e -> (RelationHistoryEntry) e).collect(Collectors.toList());
    }
}
