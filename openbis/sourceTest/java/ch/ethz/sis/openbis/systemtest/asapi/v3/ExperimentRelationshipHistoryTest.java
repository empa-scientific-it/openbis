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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentRelationshipHistoryTest extends AbstractTest
{
    private SpacePermId spaceId;

    private ProjectPermId projectId1;

    private ProjectPermId projectId2;

    private ExperimentPermId experimentId1;

    private ExperimentPermId experimentId2;

    @BeforeMethod
    public void setUp()
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("ExperimentRelationshipHistoryTest");
        spaceId = v3api.createSpaces(systemSessionToken, Arrays.asList(spaceCreation)).get(0);
        ProjectCreation projectCreation1 = new ProjectCreation();
        projectCreation1.setSpaceId(spaceId);
        projectCreation1.setCode("TEST1");
        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setSpaceId(spaceId);
        projectCreation2.setCode("TEST2");
        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation1, projectCreation2));
        projectId1 = projects.get(0);
        projectId2 = projects.get(1);
        ExperimentCreation experimentCreation1 = new ExperimentCreation();
        experimentCreation1.setCode("TEST1");
        experimentCreation1.setTypeId(new EntityTypePermId("DELETION_TEST"));
        experimentCreation1.setProjectId(projectId1);
        ExperimentCreation experimentCreation2 = new ExperimentCreation();
        experimentCreation2.setCode("TEST2");
        experimentCreation2.setTypeId(new EntityTypePermId("DELETION_TEST"));
        experimentCreation2.setProjectId(projectId2);
        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation1, experimentCreation2));
        experimentId1 = experiments.get(0);
        experimentId2 = experiments.get(1);
    }

    @Test
    public void moveExperimentToAnotherProject()
    {
        // Given
        // move experiment to another project
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId1);
        update.setProjectId(projectId2);
        v3api.updateExperiments(systemSessionToken, Arrays.asList(update));
        // delete original project
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteProjects(systemSessionToken, Arrays.asList(projectId1), deletionOptions);
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        assertEquals(v3api.getProjects(systemSessionToken, Arrays.asList(projectId1), fetchOptions).size(), 0);

        // When
        List<RelationHistoryEntry> history = getExperimentHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), projectId1, ExperimentRelationType.PROJECT);
        assertRelationshipHistory(history.get(1), projectId2, ExperimentRelationType.PROJECT);
    }

    @Test
    public void moveProjectSampleToAnExperimentAndThanToAnotherOne()
    {
        // Given
        // create project sample
        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST");
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setSpaceId(spaceId);
        creation.setProjectId(projectId1);
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // move sample to an experiment
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setSpaceId(spaceId);
        update.setExperimentId(experimentId1);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // move sample to another experiment
        update.setExperimentId(experimentId2);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete sample
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(id), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getExperimentHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, ExperimentRelationType.SAMPLE);
        assertRelationshipHistory(history.get(1), projectId1, ExperimentRelationType.PROJECT);
    }

    @Test
    public void moveExperimentSampleToAnotherExperiment()
    {
        // Given
        // create an experiment sample
        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST");
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setSpaceId(spaceId);
        creation.setProjectId(projectId1);
        creation.setExperimentId(experimentId1);
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(creation)).get(0);
        // move sample to another experiment
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setSpaceId(spaceId);
        update.setProjectId(projectId2);
        update.setExperimentId(experimentId2);
        v3api.updateSamples(systemSessionToken, Arrays.asList(update));
        // delete sample
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(id), new SampleFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getExperimentHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, ExperimentRelationType.SAMPLE);
        assertRelationshipHistory(history.get(1), projectId1, ExperimentRelationType.PROJECT);
    }

    @Test
    public void moveExperimentDataSetToAnotherExperiment()
    {
        // Given
        // create a data set
        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST");
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setExperimentId(experimentId1);
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // move data set to another experiment
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setExperimentId(experimentId2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete data set
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(id), new DataSetFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getExperimentHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, ExperimentRelationType.DATA_SET);
        assertRelationshipHistory(history.get(1), projectId1, ExperimentRelationType.PROJECT);

    }

    @Test
    public void moveExperimentDataSetToASample()
    {
        // Given
        // create a data set
        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST");
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setExperimentId(experimentId1);
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // create sample
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TEST");
        sampleCreation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setProjectId(projectId1);
        SamplePermId sampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);
        // move data set to this sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setSampleId(sampleId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete data set
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(id), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(id), new DataSetFetchOptions()).size(), 0);

        // When
        List<RelationHistoryEntry> history = getExperimentHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), id, ExperimentRelationType.DATA_SET);
        assertRelationshipHistory(history.get(1), projectId1, ExperimentRelationType.PROJECT);
    }

    private List<RelationHistoryEntry> getExperimentHistory()
    {
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withHistory();
        Experiment experiment = v3api.getExperiments(systemSessionToken, Arrays.asList(experimentId1), fo).get(experimentId1);
        return experiment.getHistory().stream().map(e -> (RelationHistoryEntry) e).collect(Collectors.toList());
    }
}
