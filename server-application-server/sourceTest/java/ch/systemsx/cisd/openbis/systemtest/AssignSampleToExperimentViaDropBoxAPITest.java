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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignSampleToExperimentViaDropBoxAPITest extends AbstractAssignmentSampleToExperimentTestCase
{
    @Override
    protected void updateExperimentChangeSamples(String experimentIdentifier, List<String> samplePermIds,
            String userSessionToken)
    {
        ExperimentIdentifier identifier = ExperimentIdentifierFactory.parse(experimentIdentifier);
        Experiment experiment = etlService.tryGetExperiment(systemSessionToken, identifier);
        ExperimentUpdatesDTO experimentUpdate = new ExperimentUpdatesDTO();
        experimentUpdate.setExperimentId(new TechId(experiment));
        experimentUpdate.setProjectIdentifier(identifier);
        experimentUpdate.setVersion(experiment.getVersion());
        experimentUpdate.setProperties(Collections.<IEntityProperty> emptyList());
        experimentUpdate.setAttachments(Collections.<NewAttachment> emptyList());
        Sample[] samples = loadSamples(samplePermIds);
        experimentUpdate.setSampleCodes(Code.extractCodesToArray(Arrays.asList(samples)));
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        etlService.performEntityOperations(userSessionToken, builder.experimentUpdate(experimentUpdate).getDetails());
    }

    @Override
    protected void updateSampleChangeExperiment(String samplePermId, String experimentIdentifierOrNull,
            String userSessionToken)
    {
        SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermId);
        Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
        ExperimentIdentifier experimentIdentifier = null;
        if (experimentIdentifierOrNull != null)
        {
            experimentIdentifier = ExperimentIdentifierFactory.parse(experimentIdentifierOrNull);
            sampleIdentifier = new SampleIdentifier((SpaceIdentifier) experimentIdentifier, sampleIdentifier.getSampleCode());
        }
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        String containerIdentifier = null;
        if (sample.getContainer() != null)
        {
            containerIdentifier = sample.getContainer().getIdentifier();
        }
        builder.sampleUpdate(new SampleUpdatesDTO(new TechId(sample), Collections.<IEntityProperty> emptyList(),
                experimentIdentifier, null, Collections.<NewAttachment> emptyList(), sample.getVersion(),
                sampleIdentifier, containerIdentifier, null));
        etlService.performEntityOperations(userSessionToken, builder.getDetails());
    }

}
