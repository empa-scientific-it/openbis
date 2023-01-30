/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.openbis.systemtest.AbstractDataSetAssignmentTestCase;

/**
 * Data set assignment tests for API V3.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignDataSetToExperimentAndSampleTest extends AbstractDataSetAssignmentTestCase
{

    @Autowired
    protected IApplicationServerApi v3api;

    @Override
    protected void reassignToExperiment(String dataSetCode, String experimentIdentifierOrNull, String userSessionToken)
    {
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(new DataSetPermId(dataSetCode));

        if (experimentIdentifierOrNull == null)
        {
            dataSetUpdate.setExperimentId(null);
        } else
        {
            dataSetUpdate.setExperimentId(new ExperimentIdentifier(experimentIdentifierOrNull));
        }

        v3api.updateDataSets(userSessionToken, Arrays.asList(dataSetUpdate));
    }

    @Override
    protected void reassignToSample(String dataSetCode, String samplePermIdOrNull, String userSessionToken)
    {
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(new DataSetPermId(dataSetCode));

        if (samplePermIdOrNull == null)
        {
            dataSetUpdate.setSampleId(null);
        } else
        {
            dataSetUpdate.setSampleId(new SamplePermId(samplePermIdOrNull));
        }
        v3api.updateDataSets(userSessionToken, Arrays.asList(dataSetUpdate));
    }

    @Override
    protected String getErrorMessage(Exception e)
    {
        String msg = e.getMessage();
        int index = msg.indexOf(" (Context: [");

        if (index != -1)
        {
            return msg.substring(0, index);
        } else
        {
            return msg;
        }
    }

}
