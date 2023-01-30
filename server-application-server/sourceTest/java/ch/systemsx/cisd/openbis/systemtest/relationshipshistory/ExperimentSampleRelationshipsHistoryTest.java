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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Pawel Glyzewski
 */
public class ExperimentSampleRelationshipsHistoryTest extends AbstractRelationshipsHistoryTest
{
    @Test
    public void testMoveSampleToDifferentExperiment()
    {
        TechId sampleId = new TechId(1);
        TechId experimentId = new TechId(2);
        TechId newExperimentId = new TechId(4);

        logIntoCommonClientService();
        Sample samp = genericClientService.getSampleInfo(sampleId);
        Experiment expe = commonClientService.getExperimentInfo(experimentId);

        SampleUpdates sup = new SampleUpdates();
        sup.setSampleIdentifier(samp.getIdentifier());
        sup.setSampleId(sampleId);
        sup.setVersion(samp.getVersion());
        sup.setSessionKey(SESSION_KEY);
        sup.setContainerIdentifierOrNull(samp.getContainer() == null ? null : samp.getContainer()
                .getIdentifier());
        sup.setProperties(samp.getProperties());
        sup.setExperimentIdentifierOrNull(ExperimentIdentifier.createIdentifier(expe));

        genericClientService.updateSample(sup);

        List<SampleRelationshipsHistory> sampleHistory =
                getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(1, sampleHistory.size());
        assertEquals("[mainSampId=" + sampleId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + expe.getPermId()
                + "; authorId=2; valid=true]", sampleHistory.iterator().next().toString());

        List<ExperimentRelationshipsHistory> experimentHistory =
                getExperimentRelationshipsHistory(experimentId.getId());

        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; sampId="
                + sampleId.getId() + "; entityPermId=" + samp.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.iterator().next().toString());

        samp = genericClientService.getSampleInfo(sampleId);
        Experiment newExpe = commonClientService.getExperimentInfo(newExperimentId);

        sup = new SampleUpdates();
        sup.setSampleIdentifier(samp.getIdentifier());
        sup.setSampleId(sampleId);
        sup.setVersion(samp.getVersion());
        sup.setSessionKey(SESSION_KEY);
        sup.setContainerIdentifierOrNull(samp.getContainer() == null ? null : samp.getContainer()
                .getIdentifier());
        sup.setProperties(samp.getProperties());
        sup.setExperimentIdentifierOrNull(ExperimentIdentifier.createIdentifier(newExpe));

        genericClientService.updateSample(sup);

        sampleHistory = getSampleRelationshipsHistory(sampleId.getId());
        assertEquals(2, sampleHistory.size());
        assertEquals("[mainSampId=" + sampleId.getId() + "; relationType=OWNED; expeId="
                + experimentId.getId() + "; entityPermId=" + expe.getPermId()
                + "; authorId=2; valid=false]", sampleHistory.get(0).toString());
        assertEquals("[mainSampId=" + sampleId.getId() + "; relationType=OWNED; expeId="
                + newExperimentId.getId() + "; entityPermId=" + newExpe.getPermId()
                + "; authorId=2; valid=true]", sampleHistory.get(1).toString());

        experimentHistory = getExperimentRelationshipsHistory(experimentId.getId());

        assertEquals(1, experimentHistory.size());
        assertEquals("[mainExpeId=" + experimentId.getId() + "; relationType=OWNER; sampId="
                + sampleId.getId() + "; entityPermId=" + samp.getPermId()
                + "; authorId=2; valid=false]", experimentHistory.get(0).toString());
        experimentHistory = getExperimentRelationshipsHistory(newExperimentId.getId());
        assertEquals("[mainExpeId=" + newExperimentId.getId() + "; relationType=OWNER; sampId="
                + sampleId.getId() + "; entityPermId=" + samp.getPermId()
                + "; authorId=2; valid=true]", experimentHistory.get(0).toString());
    }
}
