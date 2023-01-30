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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.systemtest.AbstractEntityDeletionTestCase;

/**
 * Implementation of {@link AbstractEntityDeletionTestCase} based on V3 API.
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class EntityDeletionTest extends AbstractEntityDeletionTestCase
{

    private static final String CONTEXT_DESCRIPTION = " (Context: [])";

    @Autowired
    protected IApplicationServerApi v3api;

    @Override
    protected String createExpectedErrorMessage(SampleNode relatedSample, EntityNode outsiderNode)
    {
        return super.createExpectedErrorMessage(relatedSample, outsiderNode) + CONTEXT_DESCRIPTION;
    }

    @Override
    protected String createExpectedErrorMessage(EntityNode originalNode, EntityNode relatedEntity, EntityNode outsiderNode)
    {
        return super.createExpectedErrorMessage(originalNode, relatedEntity, outsiderNode) + CONTEXT_DESCRIPTION;
    }

    @Override
    protected String createExpectedErrorMessage(String userID, String... samples)
    {
        return super.createExpectedErrorMessage(userID, samples) + CONTEXT_DESCRIPTION;
    }

    @Override
    protected void deleteExperiments(List<String> experimentIdentifiers, String userSessionToken)
    {
        List<ExperimentIdentifier> experimentIds = new ArrayList<ExperimentIdentifier>();
        for (String identifier : experimentIdentifiers)
        {
            experimentIds.add(new ExperimentIdentifier(identifier));
        }
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteExperiments(userSessionToken, experimentIds, deletionOptions);
    }

    @Override
    protected void deleteSamples(List<String> samplePermIds, String userSessionToken)
    {
        List<SamplePermId> sampleIds = new ArrayList<SamplePermId>();
        for (String permId : samplePermIds)
        {
            sampleIds.add(new SamplePermId(permId));
        }
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteSamples(userSessionToken, sampleIds, deletionOptions);
    }

    @Override
    protected void deleteDataSets(List<String> dataSetCodes, String userSessionToken)
    {
        List<DataSetPermId> dataSetIds = new ArrayList<DataSetPermId>();
        for (String code : dataSetCodes)
        {
            dataSetIds.add(new DataSetPermId(code));
        }
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteDataSets(userSessionToken, dataSetIds, deletionOptions);
    }

}
