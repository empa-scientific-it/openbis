package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class DataSetDeletionTest extends DeletionTest
{
    @Test
    @Rollback(false)
    public void testAttributes() throws Exception
    {
        Date after = new Date();
        SpacePermId space = createSpace("SPACE");
        SamplePermId spaceSample = createSample(null, space, "SPACE_SAMPLE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");
        SamplePermId experimentSample = createSample(experiment, space, "EXPERIMENT_SAMPLE");

        DataSetPermId dataset = createDataSet(experiment, "DATA_SET");
        newTx();
        Date before = new Date();

        delete(dataset);
        delete(experimentSample, spaceSample);
        delete(experiment);
        delete(project);
        delete(space);

        HashMap<String, String> expectations = new HashMap<String, String>();

        expectations.put("CODE", dataset.getPermId());
        expectations.put("DATA_STORE", "STANDARD");
        expectations.put("ENTITY_TYPE", "DELETION_TEST");
        expectations.put("DATA_PRODUCER_CODE", null);
        expectations.put("REGISTRATOR", "test");

        expectations.put("SIZE", null);
        expectations.put("PRESENT_IN_ARCHIVE", "false");
        expectations.put("SHARE_ID", null);
        expectations.put("ARCHIVING_STATUS", "AVAILABLE");

        expectations.put("LOCATION", "test/location/DATA_SET");
        expectations.put("LOCATOR_TYPE", "RELATIVE_LOCATION");
        expectations.put("IS_COMPLETE", "U");
        expectations.put("SPEED_HINT", "-50");
        expectations.put("STORAGE_CONFIRMATION", "false");
        expectations.put("STORAGE_FORMAT", "PROPRIETARY");
        expectations.put("FILE_FORMAT_TYPE", "TIFF");

        assertAttributes(dataset.getPermId(), expectations);
        assertRegistrationTimestampAttribute(dataset.getPermId(), after, before);
    }

    @Test
    @Rollback(false)
    public void changeOwner() throws Exception
    {

        SpacePermId space = createSpace("SPACE");
        SamplePermId spaceSample = createSample(null, space, "SPACE_SAMPLE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");
        SamplePermId experimentSample = createSample(experiment, space, "EXPERIMENT_SAMPLE");

        DataSetPermId dataset = createDataSet(experiment, "DATA_SET");

        newTx();
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataset);
        update.setSampleId(spaceSample);
        update.setExperimentId(null);
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        newTx();
        update = new DataSetUpdate();
        update.setDataSetId(dataset);
        update.setSampleId(experimentSample);
        update.setExperimentId(experiment);
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        newTx();
        update = new DataSetUpdate();
        update.setDataSetId(dataset);
        update.setExperimentId(experiment);
        update.setSampleId(null);
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        newTx();
        delete(dataset);
        delete(experimentSample, spaceSample);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(dataset.getPermId(), "OWNED", experimentSet(experiment.getPermId()),
                sampleSet(spaceSample.getPermId()),
                sampleSet(experimentSample.getPermId()),
                experimentSet(experiment.getPermId()));
    }

    @Test
    @Rollback(false)
    public void changeChildren() throws Exception
    {

        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");

        DataSetPermId dataset = createDataSet(experiment, "DATA_SET");
        DataSetPermId child1 = createDataSet(experiment, "CHILD1");
        DataSetPermId child2 = createDataSet(experiment, "CHILD2");

        newTx();
        updateChildren(dataset, child1);

        newTx();
        updateChildren(dataset, child1, child2);

        newTx();
        updateChildren(dataset, child2);

        newTx();

        delete(dataset);
        delete(child1, child2);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(dataset.getPermId(), "PARENT",
                dataSetSet(child1.getPermId()),
                dataSetSet(child1.getPermId(), child2.getPermId()),
                dataSetSet(child2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeParents() throws Exception
    {

        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");

        DataSetPermId dataset = createDataSet(experiment, "DATA_SET");
        DataSetPermId parent1 = createDataSet(experiment, "PARENT1");
        DataSetPermId parent2 = createDataSet(experiment, "PARENT2");

        newTx();
        updateParents(dataset, parent1);

        newTx();
        updateParents(dataset, parent1, parent2);

        newTx();
        updateParents(dataset, parent2);

        newTx();

        delete(dataset);

        newTx();

        delete(parent1, parent2);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(dataset.getPermId(), "CHILD",
                dataSetSet(parent1.getPermId()),
                dataSetSet(parent1.getPermId(), parent2.getPermId()),
                dataSetSet(parent2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeContainers() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");

        DataSetPermId dataset = createDataSet(experiment, "DATA_SET");
        DataSetPermId container1 = createContainerDataSet(experiment, "CONTAINER1");
        DataSetPermId container2 = createContainerDataSet(experiment, "CONTAINER2");

        newTx();
        updateContainers(dataset, container1);

        newTx();
        updateContainers(dataset, container1, container2);

        newTx();
        updateContainers(dataset, container2);

        newTx();

        delete(dataset);

        newTx();

        delete(container1, container2);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(dataset.getPermId(), "COMPONENT",
                dataSetSet(container1.getPermId()),
                dataSetSet(container1.getPermId(), container2.getPermId()),
                dataSetSet(container2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeComponents() throws Exception
    {

        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");

        DataSetPermId dataset = createContainerDataSet(experiment, "DATA_SET");
        DataSetPermId component1 = createContainerDataSet(experiment, "COMPONENT1");
        DataSetPermId component2 = createContainerDataSet(experiment, "COMPONENT2");

        newTx();
        updateComponents(dataset, component1);

        newTx();
        updateComponents(dataset, component1, component2);

        newTx();
        updateComponents(dataset, component2);

        newTx();
        delete(component1, component2);
        delete(dataset);

        newTx();
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(dataset.getPermId(), "CONTAINER",
                unknownSet(component1.getPermId()),
                unknownSet(component1.getPermId(), component2.getPermId()),
                unknownSet(component2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeProperties() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");
        DataSetPermId dataset = createDataSet(experiment, "DATA_SET",
                "DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X");

        newTx();
        setProperties(dataset, "DESCRIPTION", "desc2", "ORGANISM", "GORILLA", "BACTERIUM", "BACTERIUM-Y");

        newTx();
        setProperties(dataset, "DESCRIPTION", null, "ORGANISM", null, "BACTERIUM", null);

        newTx();
        setProperties(dataset, "DESCRIPTION", "desc3", "ORGANISM", "DOG", "BACTERIUM", "BACTERIUM2");

        newTx();
        delete(dataset);
        delete(experiment);
        delete(project);
        delete(space);

        assertPropertiesHistory(dataset.getPermId(), "DESCRIPTION", "desc", "desc2", "", "desc3");
        assertPropertiesHistory(dataset.getPermId(), "ORGANISM", "FLY [ORGANISM]", "GORILLA [ORGANISM]", "", "DOG [ORGANISM]");
        assertPropertiesHistory(dataset.getPermId(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "BACTERIUM-Y [BACTERIUM]", "", "BACTERIUM2 [BACTERIUM]");
    }

}
