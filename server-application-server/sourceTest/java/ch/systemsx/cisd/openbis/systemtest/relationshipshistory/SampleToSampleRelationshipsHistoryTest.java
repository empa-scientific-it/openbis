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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Pawel Glyzewski
 */
public class SampleToSampleRelationshipsHistoryTest extends AbstractRelationshipsHistoryTest
{
    @Test
    public void testAttachDetachContainedSamples()
    {
        TechId containerId = new TechId(1008);
        TechId containedId = new TechId(1009);

        logIntoCommonClientService();

        Sample container = genericClientService.getSampleInfo(containerId);
        Sample contained = genericClientService.getSampleInfo(containedId);

        SampleUpdates updates = new SampleUpdates();
        updates.setSampleIdentifier(contained.getIdentifier());
        updates.setSampleId(containedId);
        updates.setVersion(contained.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(null);
        updates.setProperties(contained.getProperties());
        updates.setExperimentIdentifierOrNull(contained.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(contained.getExperiment()));

        genericClientService.updateSample(updates);

        List<SampleRelationshipsHistory> sampleHistory =
                getSampleRelationshipsHistory(contained.getId());
        assertEquals(0, sampleHistory.size());
        sampleHistory = getSampleRelationshipsHistory(container.getId());
        assertEquals(0, sampleHistory.size());

        contained = genericClientService.getSampleInfo(containedId);

        updates = new SampleUpdates();
        updates.setSampleIdentifier(contained.getIdentifier());
        updates.setSampleId(containedId);
        updates.setVersion(contained.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(container.getIdentifier());
        updates.setProperties(contained.getProperties());
        updates.setExperimentIdentifierOrNull(contained.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(contained.getExperiment()));

        genericClientService.updateSample(updates);

        sampleHistory = getSampleRelationshipsHistory(contained.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + contained.getId() + "; relationType=CONTAINED; sampId="
                + container.getId() + "; entityPermId=" + container.getPermId()
                + "; authorId=2; valid=true]", sampleHistory.get(0).toString());
        sampleHistory = getSampleRelationshipsHistory(container.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + container.getId() + "; relationType=CONTAINER; sampId="
                + contained.getId() + "; entityPermId=" + contained.getPermId()
                + "; authorId=2; valid=true]", sampleHistory.get(0).toString());

        contained = genericClientService.getSampleInfo(containedId);

        updates = new SampleUpdates();
        updates.setSampleIdentifier(contained.getIdentifier());
        updates.setSampleId(containedId);
        updates.setVersion(contained.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(null);
        updates.setProperties(contained.getProperties());
        updates.setExperimentIdentifierOrNull(contained.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(contained.getExperiment()));

        genericClientService.updateSample(updates);

        sampleHistory = getSampleRelationshipsHistory(contained.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + contained.getId() + "; relationType=CONTAINED; sampId="
                + container.getId() + "; entityPermId=" + container.getPermId()
                + "; authorId=2; valid=false]", sampleHistory.get(0).toString());
        sampleHistory = getSampleRelationshipsHistory(container.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + container.getId() + "; relationType=CONTAINER; sampId="
                + contained.getId() + "; entityPermId=" + contained.getPermId()
                + "; authorId=2; valid=false]", sampleHistory.get(0).toString());
    }

    @Test
    public void testParentChildRelationshipsHistory()
    {
        TechId childId = new TechId(981);
        TechId parentId = new TechId(646);

        logIntoCommonClientService();

        Sample child = genericClientService.getSampleInfo(childId);
        Sample parent = genericClientService.getSampleInfo(parentId);

        SampleUpdates updates = new SampleUpdates();
        updates.setSampleIdentifier(child.getIdentifier());
        updates.setSampleId(childId);
        updates.setVersion(child.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(child.getContainer() == null ? null : child
                .getContainer().getIdentifier());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(child.getExperiment()));
        updates.setModifiedParentCodesOrNull(new String[0]);

        genericClientService.updateSample(updates);

        List<SampleRelationshipsHistory> sampleHistory =
                getSampleRelationshipsHistory(child.getId());
        assertEquals(0, sampleHistory.size());
        sampleHistory = getSampleRelationshipsHistory(parent.getId());
        assertEquals(0, sampleHistory.size());

        child = genericClientService.getSampleInfo(childId);

        updates = new SampleUpdates();
        updates.setSampleIdentifier(child.getIdentifier());
        updates.setSampleId(childId);
        updates.setVersion(child.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(child.getContainer() == null ? null : child
                .getContainer().getIdentifier());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(child.getExperiment()));
        updates.setModifiedParentCodesOrNull(new String[]
        { parent.getIdentifier() });

        genericClientService.updateSample(updates);

        sampleHistory = getSampleRelationshipsHistory(child.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + child.getId() + "; relationType=CHILD; sampId=" + parent.getId()
                        + "; entityPermId=" + parent.getPermId() + "; authorId=2; valid=true]",
                sampleHistory.get(0).toString());

        sampleHistory = getSampleRelationshipsHistory(parent.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + parent.getId() + "; relationType=PARENT; sampId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=true]",
                sampleHistory.get(0).toString());

        child = genericClientService.getSampleInfo(childId);

        updates = new SampleUpdates();
        updates.setSampleIdentifier(child.getIdentifier());
        updates.setSampleId(childId);
        updates.setVersion(child.getVersion());
        updates.setSessionKey(SESSION_KEY);
        updates.setContainerIdentifierOrNull(child.getContainer() == null ? null : child
                .getContainer().getIdentifier());
        updates.setProperties(child.getProperties());
        updates.setExperimentIdentifierOrNull(child.getExperiment() == null ? null
                : ExperimentIdentifier.createIdentifier(child.getExperiment()));
        updates.setModifiedParentCodesOrNull(new String[0]);

        genericClientService.updateSample(updates);

        sampleHistory = getSampleRelationshipsHistory(child.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + child.getId() + "; relationType=CHILD; sampId=" + parent.getId()
                        + "; entityPermId=" + parent.getPermId() + "; authorId=2; valid=false]",
                sampleHistory.get(0).toString());

        sampleHistory = getSampleRelationshipsHistory(parent.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals(
                "[mainSampId=" + parent.getId() + "; relationType=PARENT; sampId=" + child.getId()
                        + "; entityPermId=" + child.getPermId() + "; authorId=2; valid=false]",
                sampleHistory.get(0).toString());
    }
}
