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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Implementation of {@link AbstractEntityDeletionTestCase} based on internal API (V1).
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class EntityDeletionTest extends AbstractEntityDeletionTestCase
{
    /**
     * Trashes specified experiments for specified user session token.
     */
    @Override
    protected void deleteExperiments(List<String> experimentIdentifiers, String userSessionToken)
    {
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        for (String identifier : experimentIdentifiers)
        {
            identifiers.add(ExperimentIdentifierFactory.parse(identifier));
        }
        List<TechId> experimentIds = TechId.createList(commonServer.listExperiments(userSessionToken, identifiers));
        commonServer.deleteExperiments(userSessionToken, experimentIds, "test", DeletionType.TRASH);
    }

    /**
     * Trashes specified samples for specified user session token.
     */
    @Override
    protected void deleteSamples(List<String> samplePermIds, String userSessionToken)
    {
        Sample[] samples = loadSamples(samplePermIds);
        commonServer.deleteSamples(userSessionToken, TechId.createList(Arrays.asList(samples)), "test", DeletionType.TRASH);
    }

    /**
     * Trashes specified data sets for specified user session token.
     */
    @Override
    protected void deleteDataSets(List<String> dataSetCodes, String userSessionToken)
    {
        commonServer.deleteDataSets(userSessionToken, dataSetCodes, "test", DeletionType.TRASH, true);
    }

}
