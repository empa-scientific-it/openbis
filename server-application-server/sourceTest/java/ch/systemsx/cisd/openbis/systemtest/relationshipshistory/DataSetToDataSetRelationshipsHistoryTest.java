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

/**
 * @author Pawel Glyzewski
 */
public class DataSetToDataSetRelationshipsHistoryTest extends AbstractRelationshipsHistoryTest
{
    @Test
    public void testAttachDetachContainedDataSet()
    {
        TechId containerId = new TechId(13);
        TechId containedId = new TechId(14);

        logIntoCommonClientService();

        List<DataSetRelationshipsHistory> dataSetHistory =
                getDataSetRelationshipsHistory(containedId.getId());
        int containedHistoryInitialSize = dataSetHistory.size();
        dataSetHistory = getDataSetRelationshipsHistory(containerId.getId());
        int containerHistoryInitialSize = dataSetHistory.size();

        AbstractExternalData container = genericClientService.getDataSetInfo(containerId);
        AbstractExternalData contained = genericClientService.getDataSetInfo(containedId);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(containerId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(container.getVersion());
        updates.setProperties(container.getProperties());
        updates.setExperimentIdentifierOrNull(container.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(container.getSampleIdentifier());
        updates.setModifiedContainedDatasetCodesOrNull(new String[0]);

        genericClientService.updateDataSet(updates);

        dataSetHistory =
                getDataSetRelationshipsHistory(containedId.getId());
        assertEquals(containedHistoryInitialSize, dataSetHistory.size());
        dataSetHistory = getDataSetRelationshipsHistory(containerId.getId());
        assertEquals(containerHistoryInitialSize, dataSetHistory.size());

        container = genericClientService.getDataSetInfo(containerId);

        updates = new DataSetUpdates();
        updates.setDatasetId(containerId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(container.getVersion());
        updates.setProperties(container.getProperties());
        updates.setExperimentIdentifierOrNull(container.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(container.getSampleIdentifier());
        updates.setModifiedContainedDatasetCodesOrNull(new String[]
        { contained.getCode() });

        genericClientService.updateDataSet(updates);

        dataSetHistory = getDataSetRelationshipsHistory(containedId.getId());
        assertEquals(containedHistoryInitialSize + 1, dataSetHistory.size());
        assertEquals("[mainDataId=" + containedId.getId() + "; relationType=COMPONENT; dataId="
                + containerId.getId() + "; entityPermId=" + container.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.get(dataSetHistory.size() - 1).toString());

        dataSetHistory = getDataSetRelationshipsHistory(containerId.getId());
        assertEquals(containerHistoryInitialSize + 1, dataSetHistory.size());
        assertEquals("[mainDataId=" + containerId.getId() + "; relationType=CONTAINER; dataId="
                + containedId.getId() + "; entityPermId=" + contained.getPermId()
                + "; authorId=2; valid=true]", dataSetHistory.get(dataSetHistory.size() - 1).toString());

        container = genericClientService.getDataSetInfo(containerId);

        updates = new DataSetUpdates();
        updates.setDatasetId(containerId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(container.getVersion());
        updates.setProperties(container.getProperties());
        updates.setExperimentIdentifierOrNull(container.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(container.getSampleIdentifier());
        updates.setModifiedContainedDatasetCodesOrNull(new String[0]);

        genericClientService.updateDataSet(updates);

        dataSetHistory = getDataSetRelationshipsHistory(containedId.getId());
        assertEquals(containedHistoryInitialSize + 1, dataSetHistory.size());
        assertEquals("[mainDataId=" + containedId.getId() + "; relationType=COMPONENT; dataId="
                + containerId.getId() + "; entityPermId=" + container.getPermId()
                + "; authorId=2; valid=false]", dataSetHistory.get(dataSetHistory.size() - 1).toString());

        dataSetHistory = getDataSetRelationshipsHistory(containerId.getId());
        assertEquals(containerHistoryInitialSize + 1, dataSetHistory.size());
        assertEquals("[mainDataId=" + containerId.getId() + "; relationType=CONTAINER; dataId="
                + containedId.getId() + "; entityPermId=" + contained.getPermId()
                + "; authorId=2; valid=false]", dataSetHistory.get(dataSetHistory.size() - 1).toString());
    }

    @Test
    public void testParentChildRelationship()
    {
        TechId childId = new TechId(12);
        TechId parent1Id = new TechId(10);
        TechId parent2Id = new TechId(11);

        logIntoCommonClientService();

        AbstractExternalData child = genericClientService.getDataSetInfo(childId);
        AbstractExternalData parent1 = genericClientService.getDataSetInfo(parent1Id);
        AbstractExternalData parent2 = genericClientService.getDataSetInfo(parent2Id);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(childId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(child.getVersion());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(child.getSampleIdentifier());
        updates.setModifiedParentDatasetCodesOrNull(new String[0]);

        genericClientService.updateDataSet(updates);

        List<DataSetRelationshipsHistory> dataSetHistory =
                getDataSetRelationshipsHistory(childId.getId());
        assertEquals(0, dataSetHistory.size());
        dataSetHistory = getDataSetRelationshipsHistory(parent1Id.getId());
        assertEquals(0, dataSetHistory.size());
        dataSetHistory = getDataSetRelationshipsHistory(parent2Id.getId());
        assertEquals(0, dataSetHistory.size());

        child = genericClientService.getDataSetInfo(childId);

        updates = new DataSetUpdates();
        updates.setDatasetId(childId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(child.getVersion());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(child.getSampleIdentifier());
        updates.setModifiedParentDatasetCodesOrNull(new String[]
        { parent1.getCode(), parent2.getCode() });

        genericClientService.updateDataSet(updates);

        dataSetHistory = getDataSetRelationshipsHistory(childId.getId());
        assertEquals(2, dataSetHistory.size());
        for (DataSetRelationshipsHistory history : dataSetHistory)
        {
            if (history.getDataId() == parent1Id.getId())
            {
                assertEquals("[mainDataId=" + childId.getId() + "; relationType=CHILD; dataId="
                        + parent1.getId() + "; entityPermId=" + parent1.getPermId()
                        + "; authorId=2; valid=true]", history.toString());
            } else
            {
                assertEquals("[mainDataId=" + childId.getId() + "; relationType=CHILD; dataId="
                        + parent2.getId() + "; entityPermId=" + parent2.getPermId()
                        + "; authorId=2; valid=true]", history.toString());
            }
        }

        dataSetHistory = getDataSetRelationshipsHistory(parent1Id.getId());
        assertEquals(1, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + parent1.getId() + "; relationType=PARENT; dataId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=true]",
                dataSetHistory.iterator().next().toString());
        dataSetHistory = getDataSetRelationshipsHistory(parent2Id.getId());
        assertEquals(1, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + parent2.getId() + "; relationType=PARENT; dataId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=true]",
                dataSetHistory.iterator().next().toString());

        child = genericClientService.getDataSetInfo(childId);

        updates = new DataSetUpdates();
        updates.setDatasetId(childId);
        updates.setFileFormatTypeCode("PROPRIETARY");
        updates.setVersion(child.getVersion());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment().getIdentifier());
        updates.setSampleIdentifierOrNull(child.getSampleIdentifier());
        updates.setModifiedParentDatasetCodesOrNull(new String[0]);

        genericClientService.updateDataSet(updates);

        dataSetHistory = getDataSetRelationshipsHistory(childId.getId());
        assertEquals(2, dataSetHistory.size());
        for (DataSetRelationshipsHistory history : dataSetHistory)
        {
            if (history.getDataId() == parent1Id.getId())
            {
                assertEquals("[mainDataId=" + childId.getId() + "; relationType=CHILD; dataId="
                        + parent1.getId() + "; entityPermId=" + parent1.getPermId()
                        + "; authorId=2; valid=false]", history.toString());
            } else
            {
                assertEquals("[mainDataId=" + childId.getId() + "; relationType=CHILD; dataId="
                        + parent2.getId() + "; entityPermId=" + parent2.getPermId()
                        + "; authorId=2; valid=false]", history.toString());
            }
        }

        dataSetHistory = getDataSetRelationshipsHistory(parent1Id.getId());
        assertEquals(1, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + parent1.getId() + "; relationType=PARENT; dataId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=false]",
                dataSetHistory.iterator().next().toString());
        dataSetHistory = getDataSetRelationshipsHistory(parent2Id.getId());
        assertEquals(1, dataSetHistory.size());
        assertEquals(
                "[mainDataId=" + parent2.getId() + "; relationType=PARENT; dataId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=false]",
                dataSetHistory.iterator().next().toString());
    }
}
