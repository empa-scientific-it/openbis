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

import java.util.Collections;
import java.util.HashSet;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Data set assignment tests for Drop Box API V1/V2 (that is, via
 * {@link IServiceForDataStoreServer#performEntityOperations(String, ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails)}).
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignDataSetToExperimentAndSampleViaDropBoxAPITest extends AbstractDataSetAssignmentTestCase
{
    @Override
    protected void reassignToExperiment(String dataSetCode, String experimentIdentifierOrNull, String userSessionToken)
    {
        DataSetBatchUpdatesDTO dataSetUpdate = createUpdatesObject(dataSetCode);
        dataSetUpdate.getDetails().setExperimentUpdateRequested(true);
        ExperimentIdentifier experimentIdentifier = null;
        if (experimentIdentifierOrNull != null)
        {
            experimentIdentifier = ExperimentIdentifierFactory.parse(experimentIdentifierOrNull);
        }
        dataSetUpdate.setExperimentIdentifierOrNull(experimentIdentifier);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        etlService.performEntityOperations(userSessionToken, builder.dataSetUpdate(dataSetUpdate).getDetails());
    }

    @Override
    protected void reassignToSample(String dataSetCode, String samplePermIdOrNull, String userSessionToken)
    {
        DataSetBatchUpdatesDTO dataSetUpdate = createUpdatesObject(dataSetCode);
        dataSetUpdate.getDetails().setSampleUpdateRequested(true);
        SampleIdentifier sampleIdentifier = null;
        if (samplePermIdOrNull != null)
        {
            sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermIdOrNull);
        }
        dataSetUpdate.setSampleIdentifierOrNull(sampleIdentifier);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        etlService.performEntityOperations(userSessionToken, builder.dataSetUpdate(dataSetUpdate).getDetails());
    }

    private DataSetBatchUpdatesDTO createUpdatesObject(String dataSetCode)
    {
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, dataSetCode);
        DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        dataSetUpdate.setDetails(updateDetails);
        dataSetUpdate.setDatasetId(new TechId(dataSet));
        dataSetUpdate.setDatasetCode(dataSetCode);
        dataSetUpdate.setVersion(dataSet.getVersion());
        Experiment experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            dataSetUpdate.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
        }
        Sample sample = dataSet.getSample();
        if (sample != null)
        {
            dataSetUpdate.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample));
        }
        dataSetUpdate.setProperties(Collections.<IEntityProperty> emptyList());
        updateDetails.setPropertiesToUpdate(new HashSet<String>());
        return dataSetUpdate;
    }
}
