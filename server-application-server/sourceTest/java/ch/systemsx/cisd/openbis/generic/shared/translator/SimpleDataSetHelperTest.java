/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class SimpleDataSetHelperTest extends AssertJUnit
{
    @Test
    public void testTranslateList()
    {
        AbstractExternalData ds1 = create(1);
        AbstractExternalData ds2 = create(2);
        AbstractExternalData ds3 = new ContainerDataSet();

        List<SimpleDataSetInformationDTO> list =
                SimpleDataSetHelper.filterAndTranslate(Arrays.asList(ds1, ds2, ds3));

        check(1, list.get(0));
        check(2, list.get(1));
        assertEquals("42", list.get(1).getDataSetShareId());
        assertEquals(2, list.size());
    }

    @Test
    public void testTranslateDescription()
    {
        DatasetDescription description = DataSetTranslator.translateToDescription(create(1));

        SimpleDataSetInformationDTO result = SimpleDataSetHelper.translate(description);

        check(1, result);
    }

    private AbstractExternalData create(long id)
    {
        PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setId(id);
        dataSet.setCode("ds-" + id);
        dataSet.setComplete(true);
        dataSet.setDataProducerCode("producer");
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode("MY-TYPE");
        dataSet.setDataSetType(dataSetType);
        DataStore store = new DataStore();
        store.setCode("MY-STORE");
        dataSet.setDataStore(store);
        dataSet.setDerived(true);
        Experiment experiment = new Experiment();
        experiment.setCode("MY-EXPERIMENT");
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("MY-EXPERIMENT-TYPE");
        experiment.setExperimentType(experimentType);
        Project project = new Project();
        project.setCode("MY-PROJECT");
        Space space = new Space();
        space.setCode("MY-SPACE");
        project.setSpace(space);
        experiment.setProject(project);
        dataSet.setExperiment(experiment);
        FileFormatType fileFormatType = new FileFormatType();
        fileFormatType.setCode("MY_FILE_FORMAT");
        dataSet.setFileFormatType(fileFormatType);
        dataSet.setLocation("my-location");
        Sample sample = new Sample();
        sample.setCode("MY-SAMPLE");
        dataSet.setSample(sample);
        dataSet.setShareId("42");
        dataSet.setSize(137L);
        dataSet.setSpeedHint(42);
        dataSet.setStatus(DataSetArchivingStatus.ARCHIVED);
        return dataSet;
    }

    public void check(long expectedID, SimpleDataSetInformationDTO dataSet)
    {
        assertEquals("ds-" + expectedID, dataSet.getDataSetCode());
        assertEquals("MY-TYPE", dataSet.getDataSetType());
        assertEquals("my-location", dataSet.getDataSetLocation());
        assertEquals("MY-EXPERIMENT", dataSet.getExperimentCode());
        assertEquals("MY-PROJECT", dataSet.getProjectCode());
        assertEquals("MY-SPACE", dataSet.getSpaceCode());
        assertEquals("MY-SAMPLE", dataSet.getSampleCode());
        assertEquals(Long.valueOf(137), dataSet.getDataSetSize());
        assertEquals(42, dataSet.getSpeedHint());
    }
}
