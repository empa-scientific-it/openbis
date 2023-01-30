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

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Data set assignment tests for API V1/V2.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignDataSetToExperimentAndSampleTest extends AbstractDataSetAssignmentTestCase
{
    @Override
    protected void reassignToExperiment(String dataSetCode, String experimentIdentifierOrNull, String userSessionToken)
    {
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, dataSetCode);
        if (experimentIdentifierOrNull != null)
        {
            Experiment experiment = etlService.tryGetExperiment(systemSessionToken,
                    ExperimentIdentifierFactory.parse(experimentIdentifierOrNull));
            perform(anUpdateOf(dataSet).toExperiment(experiment).as(userSessionToken));
        } else
        {
            perform(anUpdateOf(dataSet).removingExperiment().as(userSessionToken));
        }
    }

    @Override
    protected void reassignToSample(String dataSetCode, String samplePermIdOrNull, String userSessionToken)
    {
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, dataSetCode);
        if (samplePermIdOrNull != null)
        {
            SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermIdOrNull);
            Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
            perform(anUpdateOf(dataSet).toSample(sample).as(userSessionToken));
        } else
        {
            perform(anUpdateOf(dataSet).removingSample().as(userSessionToken));
        }
    }
}
