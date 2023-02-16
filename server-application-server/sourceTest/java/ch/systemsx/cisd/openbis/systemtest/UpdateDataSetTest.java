/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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

import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

public class UpdateDataSetTest extends BaseTest
{

    Sample sample;

    @BeforeMethod
    void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));
    }

    @Test
    public void dataSetUpdateDoesntSetSampleToNull()
    {
        AbstractExternalData dataSet = create(aDataSet().inSample(sample));
        DataSetBatchUpdatesDTO update = new DataSetBatchUpdatesDTO();
        update.setDatasetId(new TechId(dataSet));
        update.setDatasetCode(dataSet.getCode());
        update.setVersion(dataSet.getVersion());
        update.setDetails(new DataSetBatchUpdateDetails());
        update.setProperties(Collections.<IEntityProperty> emptyList());
        update.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(dataSet.getSample()));
        update.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(dataSet.getExperiment().getIdentifier()));

        etlService.performEntityOperations(systemSessionToken, new AtomicEntityOperationDetailsBuilder().dataSetUpdate(update).getDetails());

        assertThat(dataSet, inSample(sample));
        assertThat(dataSet, inExperiment(sample.getExperiment()));
    }
}
