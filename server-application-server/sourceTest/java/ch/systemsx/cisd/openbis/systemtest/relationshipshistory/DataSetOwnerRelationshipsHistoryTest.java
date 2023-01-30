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

package ch.systemsx.cisd.openbis.systemtest.relationshipshistory;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Pawel Glyzewski
 */
public class DataSetOwnerRelationshipsHistoryTest extends AbstractRelationshipsHistoryTest
{
    @Test
    public void testMoveDataSetToDifferentSample()
    {
        TechId dataId = new TechId(4);
        TechId sampleId = new TechId(1023);
        TechId newSampleId = new TechId(1042);

        logIntoCommonClientService();

        AbstractExternalData dataSet = genericClientService.getDataSetInfo(dataId);
        Sample sample = genericClientService.getSampleInfo(sampleId);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setSampleIdentifierOrNull(sample.getIdentifier());

        genericClientService.updateDataSet(updates);

        List<SampleRelationshipsHistory> sampleHistory =
                getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + sampleId.getId() + "; relationType=OWNER; dataId="
                        + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                        + "; authorId=2; valid=true]", sampleHistory.iterator().next().toString());

        List<DataSetRelationshipsHistory> dataSetHistory =
                getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(1, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + dataId.getId() + "; relationType=OWNED; sampId="
                        + sampleId.getId() + "; entityPermId=" + sample.getPermId()
                        + "; authorId=2; valid=true]", dataSetHistory.iterator().next().toString());

        dataSet = genericClientService.getDataSetInfo(dataId);
        Sample newSample = genericClientService.getSampleInfo(newSampleId);

        updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setSampleIdentifierOrNull(newSample.getIdentifier());

        genericClientService.updateDataSet(updates);

        sampleHistory = getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + sampleId.getId() + "; relationType=OWNER; dataId="
                        + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                        + "; authorId=2; valid=false]", sampleHistory.iterator().next().toString());
        sampleHistory = getSampleRelationshipsHistory(newSampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + newSampleId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=true]", sampleHistory.iterator().next().toString());

        dataSetHistory = getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(2, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + dataId.getId() + "; relationType=OWNED; sampId="
                        + sampleId.getId() + "; entityPermId=" + sample.getPermId()
                        + "; authorId=2; valid=false]", dataSetHistory.iterator().next().toString());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; sampId="
                + newSampleId.getId() + "; entityPermId=" + newSample.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.get(1).toString());
    }

    @Test
    public void testMoveDataSetToDifferentExperiment()
    {
        TechId dataId = new TechId(4);
        TechId experimentId = new TechId(22);
        TechId newExperimentId = new TechId(2);

        logIntoCommonClientService();

        AbstractExternalData dataSet = genericClientService.getDataSetInfo(dataId);
        Experiment experiment = commonClientService.getExperimentInfo(experimentId);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setExperimentIdentifierOrNull(experiment.getIdentifier());

        genericClientService.updateDataSet(updates);

        List<ExperimentRelationshipsHistory> experimentHistory =
                getExperimentRelationshipsHistory(experimentId.getId());
        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.iterator().next().toString());

        List<DataSetRelationshipsHistory> dataSetHistory =
                getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(1, dataSetHistory.size());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + experiment.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.iterator().next().toString());

        dataSet = genericClientService.getDataSetInfo(dataId);
        Experiment newExperiment = commonClientService.getExperimentInfo(newExperimentId);

        updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setExperimentIdentifierOrNull(newExperiment.getIdentifier());

        genericClientService.updateDataSet(updates);

        experimentHistory = getExperimentRelationshipsHistory(experimentId.getId());
        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=false]", experimentHistory.iterator().next().toString());
        experimentHistory = getExperimentRelationshipsHistory(newExperimentId.getId());
        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + newExperimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.iterator().next().toString());

        dataSetHistory = getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(2, dataSetHistory.size());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + experiment.getPermId()
                + "; authorId=2; valid=false]", dataSetHistory.iterator().next().toString());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + newExperimentId.getId() + "; entityPermId=" + newExperiment.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.get(1).toString());
    }

    @Test
    public void testMoveDataSetFromExperimentToSampleAndBack()
    {
        TechId dataId = new TechId(4);
        TechId experimentId = new TechId(22);
        TechId sampleId = new TechId(1023);
        TechId newExperimentId = new TechId(22);

        logIntoCommonClientService();

        AbstractExternalData dataSet = genericClientService.getDataSetInfo(dataId);
        Experiment experiment = commonClientService.getExperimentInfo(experimentId);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setExperimentIdentifierOrNull(experiment.getIdentifier());

        genericClientService.updateDataSet(updates);

        List<ExperimentRelationshipsHistory> experimentHistory =
                getExperimentRelationshipsHistory(experimentId.getId());
        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.iterator().next().toString());

        List<DataSetRelationshipsHistory> dataSetHistory =
                getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(1, dataSetHistory.size());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + experiment.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.iterator().next().toString());

        dataSet = genericClientService.getDataSetInfo(dataId);
        Sample sample = genericClientService.getSampleInfo(sampleId);

        updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setSampleIdentifierOrNull(sample.getIdentifier());

        genericClientService.updateDataSet(updates);

        experimentHistory = getExperimentRelationshipsHistory(experimentId.getId());
        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=false]", experimentHistory.iterator().next().toString());

        List<SampleRelationshipsHistory> sampleHistory =
                getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + sampleId.getId() + "; relationType=OWNER; dataId="
                        + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                        + "; authorId=2; valid=true]", sampleHistory.iterator().next().toString());

        dataSetHistory = getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(2, dataSetHistory.size());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + experiment.getPermId()
                + "; authorId=2; valid=false]", dataSetHistory.iterator().next().toString());
        assertEquals(
                "[mainDataId=" + dataId.getId() + "; relationType=OWNED; sampId="
                        + sampleId.getId() + "; entityPermId=" + sample.getPermId()
                        + "; authorId=2; valid=true]", dataSetHistory.get(1).toString());

        dataSet = genericClientService.getDataSetInfo(dataId);
        Experiment newExperiment = commonClientService.getExperimentInfo(newExperimentId);

        updates = new DataSetUpdates();
        updates.setDatasetId(dataId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(dataSet.getVersion());
        updates.setProperties(dataSet.getProperties());
        updates.setExperimentIdentifierOrNull(newExperiment.getIdentifier());

        genericClientService.updateDataSet(updates);

        experimentHistory = getExperimentRelationshipsHistory(experimentId.getId());
        assertEquals(2, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=false]", experimentHistory.iterator().next().toString());
        assertEquals("[mainExpeId=" + newExperimentId.getId() + "; relationType=OWNER; dataId="
                + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.get(1).toString());

        sampleHistory = getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + sampleId.getId() + "; relationType=OWNER; dataId="
                        + dataId.getId() + "; entityPermId=" + dataSet.getPermId()
                        + "; authorId=2; valid=false]", sampleHistory.iterator().next().toString());

        dataSetHistory = getDataSetRelationshipsHistory(dataId.getId());

        assertEquals(3, dataSetHistory.size());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + experiment.getPermId()
                + "; authorId=2; valid=false]", dataSetHistory.iterator().next().toString());
        assertEquals(
                "[mainDataId=" + dataId.getId() + "; relationType=OWNED; sampId="
                        + sampleId.getId() + "; entityPermId=" + sample.getPermId()
                        + "; authorId=2; valid=false]", dataSetHistory.get(1).toString());
        assertEquals("[mainDataId=" + dataId.getId() + "; relationType=OWNED; expeId="
                + newExperimentId.getId() + "; entityPermId=" + newExperiment.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.get(2).toString());
    }

}
