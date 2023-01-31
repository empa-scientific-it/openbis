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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Jakub Straszewski
 */
@Test(groups = "slow")
public class QueryApiJsonDssServiceRpcGenericTest extends AbstractQueryFacadeTest
{
    private IGeneralInformationService openbisService;

    private IDssServiceRpcGeneric dssRpcService;

    private String sessionToken;

    @BeforeClass
    public void beforeClass() throws IOException
    {
        openbisService = JsonDssServiceRpcGenericTest.createOpenbisService();
        dssRpcService = JsonDssServiceRpcGenericTest.createDssRpcService();

        sessionToken = openbisService.tryToAuthenticateForAllServices("test", "1");
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String serviceKey,
            Map<String, Object> parameters)
    {
        return dssRpcService.createReportFromAggregationService(sessionToken, serviceKey,
                parameters);
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String user, String serviceCode,
            Map<String, Object> parameters)
    {
        String userSessionToken = openbisService.tryToAuthenticateForAllServices(user, "1");
        return dssRpcService.createReportFromAggregationService(userSessionToken, serviceCode,
                parameters);
    }

    @Override
    public QueryTableModel createReportFromDataSets(ReportDescription description,
            List<String> dataSetCodes)
    {
        return dssRpcService.createReportFromDataSets(sessionToken, description.getKey(),
                dataSetCodes);
    }

    @Override
    public IGeneralInformationService getGeneralInformationService()
    {
        return openbisService;
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices()
    {
        return dssRpcService.listAggregationServices(sessionToken);
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions()
    {
        return dssRpcService.listTableReportDescriptions(sessionToken);
    }

    @Override
    public String getTestId()
    {
        return "RPC";
    }
}
