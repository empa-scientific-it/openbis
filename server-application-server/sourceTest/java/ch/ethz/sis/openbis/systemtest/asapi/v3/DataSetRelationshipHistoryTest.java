/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRelationshipHistoryTest extends AbstractTest
{
    private SpacePermId spaceId;

    private ProjectPermId projectId;

    private ExperimentPermId experimentId1;

    private ExperimentPermId experimentId2;

    private SamplePermId sampleId1;

    private SamplePermId sampleId2;

    private DataSetPermId experimentDataSet;

    private DataSetPermId sampleDataSet;

    @BeforeMethod
    public void setUp()
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("DataSetRelationshipHistoryTest");
        spaceId = v3api.createSpaces(systemSessionToken, Arrays.asList(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode("TEST");
        projectId = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation)).get(0);

        ExperimentCreation experimentCreation1 = new ExperimentCreation();
        experimentCreation1.setTypeId(new EntityTypePermId("DELETION_TEST"));
        experimentCreation1.setProjectId(projectId);
        experimentCreation1.setCode("TEST1");
        ExperimentCreation experimentCreation2 = new ExperimentCreation();
        experimentCreation2.setTypeId(new EntityTypePermId("DELETION_TEST"));
        experimentCreation2.setProjectId(projectId);
        experimentCreation2.setCode("TEST2");
        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation1, experimentCreation2));
        experimentId1 = experiments.get(0);
        experimentId2 = experiments.get(1);

        SampleCreation sampleCreation1 = new SampleCreation();
        sampleCreation1.setTypeId(new EntityTypePermId("DELETION_TEST"));
        sampleCreation1.setSpaceId(spaceId);
        sampleCreation1.setProjectId(projectId);
        sampleCreation1.setCode("TEST1");
        SampleCreation sampleCreation2 = new SampleCreation();
        sampleCreation2.setTypeId(new EntityTypePermId("DELETION_TEST"));
        sampleCreation2.setSpaceId(spaceId);
        sampleCreation2.setProjectId(projectId);
        sampleCreation2.setCode("TEST2");
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation1, sampleCreation2));
        sampleId1 = samples.get(0);
        sampleId2 = samples.get(1);

        DataSetCreation dataSetCreation1 = createDataSet("EXPERIMENT");
        dataSetCreation1.setExperimentId(experimentId1);
        DataSetCreation dataSetCreation2 = createDataSet("SAMPLE");
        dataSetCreation2.setSampleId(sampleId1);
        List<DataSetPermId> dataSets = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation1, dataSetCreation2));
        experimentDataSet = dataSets.get(0);
        sampleDataSet = dataSets.get(1);
    }

    @Test
    public void moveExperimentDataSetToSample()
    {
        // Given
        // move experiment data set to a sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.setSampleId(sampleId1);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        assertEquals(v3api.getExperiments(systemSessionToken, Arrays.asList(experimentId1), fetchOptions).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
        assertRelationshipHistory(history.get(1), sampleId1, DataSetRelationType.SAMPLE, dataSet.getModificationDate(), null);
    }

    @Test
    public void moveExperimentDataSetToAnotherExperiment()
    {
        // Given
        // move experiment data set to another experiment
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.setExperimentId(experimentId2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete original experiment
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experimentId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        assertEquals(v3api.getExperiments(systemSessionToken, Arrays.asList(experimentId1), fetchOptions).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
        assertRelationshipHistory(history.get(1), experimentId2, DataSetRelationType.EXPERIMENT, dataSet.getModificationDate(), null);
    }

    @Test
    public void moveSampleDataSetToAnotherSample()
    {
        // Given
        // move sample data set to another sample
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(sampleDataSet);
        update.setSampleId(sampleId2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete original sample
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(sampleId1), fetchOptions).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(sampleDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), sampleId1, DataSetRelationType.SAMPLE, dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
        assertRelationshipHistory(history.get(1), sampleId2, DataSetRelationType.SAMPLE, dataSet.getModificationDate(), null);
    }

    @Test
    public void moveSampleDataSetToAnExperiment()
    {
        // Given
        // move sample data set to an experiment
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(sampleDataSet);
        update.setExperimentId(experimentId1);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete original sample
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleId1), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        assertEquals(v3api.getSamples(systemSessionToken, Arrays.asList(sampleId1), fetchOptions).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(sampleDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getModificationDate(), null);
        assertRelationshipHistory(history.get(1), sampleId1, DataSetRelationType.SAMPLE, dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
    }

    @Test
    void createAndRemoveChild()
    {
        // Given
        // create a child
        DataSetCreation creation = createDataSet("CHILD");
        creation.setExperimentId(experimentId1);
        creation.setParentIds(Arrays.asList(experimentDataSet));
        DataSetPermId childId = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove child
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.getChildIds().remove(childId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete child
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(childId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(childId), new DataSetFetchOptions()).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), childId, DataSetRelationType.CHILD);
    }

    @Test
    void createAndRemoveParent()
    {
        // Given
        // create a parent
        DataSetCreation creation = createDataSet("PARENT");
        creation.setExperimentId(experimentId1);
        creation.setChildIds(Arrays.asList(experimentDataSet));
        DataSetPermId parentId = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove parent
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.getParentIds().remove(parentId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete child
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(parentId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(parentId), new DataSetFetchOptions()).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), parentId, DataSetRelationType.PARENT);
    }

    @Test
    void createAndRemoveComponent()
    {
        // Given
        // create a component
        DataSetCreation creation = createDataSet("COMPONENT");
        creation.setExperimentId(experimentId1);
        creation.setContainerIds(Arrays.asList(experimentDataSet));
        DataSetPermId componentId = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove component
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.getComponentIds().remove(componentId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete component
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(componentId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(componentId), new DataSetFetchOptions()).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), componentId, DataSetRelationType.COMPONENT);
    }

    @Test
    void createAndRemoveContainer()
    {
        // Given
        // create a container
        DataSetCreation creation = createDataSet("CONTAINER");
        creation.setExperimentId(experimentId1);
        creation.setComponentIds(Arrays.asList(experimentDataSet));
        DataSetPermId containerId = v3api.createDataSets(systemSessionToken, Arrays.asList(creation)).get(0);
        // remove container
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(experimentDataSet);
        update.getContainerIds().remove(containerId);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(update));
        // delete container
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(containerId), deletionOptions);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getDataSets(systemSessionToken, Arrays.asList(containerId), new DataSetFetchOptions()).size(), 0);

        // When
        DataSet dataSet = getDataSetHistory(experimentDataSet);
        List<HistoryEntry> history = dataSet.getHistory();

        // Then
        assertEquals(history.size(), 2);
        assertRelationshipHistory(history.get(0), experimentId1, DataSetRelationType.EXPERIMENT, dataSet.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), containerId, DataSetRelationType.CONTAINER);
    }

    private DataSetCreation createDataSet(String code)
    {
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(code);
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        return dataSetCreation;
    }

    private DataSet getDataSetHistory(DataSetPermId dataSetId)
    {
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        return v3api.getDataSets(systemSessionToken, Arrays.asList(dataSetId), fo).get(dataSetId);
    }

}
