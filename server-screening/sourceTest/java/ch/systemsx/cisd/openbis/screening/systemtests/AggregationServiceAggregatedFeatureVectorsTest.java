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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.util.HashMap;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.query.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
{ "slow", "systemtest" })
public class AggregationServiceAggregatedFeatureVectorsTest extends AbstractScreeningSystemTestCase
{
    @Test
    public void testRegisteringFeatureVectors() throws Exception
    {
        IQueryApiServer queryApi = (IQueryApiServer) applicationContext.getBean(ResourceNames.QUERY_PLUGIN_SERVER);
        queryApi.createReportFromAggregationService(sessionToken, "DSS-SCREENING",
                "example-screening-jython-db-modifying-aggregation-service",
                new HashMap<String, Object>());
    }

}
