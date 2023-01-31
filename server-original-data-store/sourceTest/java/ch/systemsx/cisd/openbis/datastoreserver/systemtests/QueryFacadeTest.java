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

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Jakub Straszewski
 */
@Test(groups = "slow")
public class QueryFacadeTest extends AbstractQueryFacadeTest
{
    IQueryApiFacade queryFacade;

    @BeforeMethod
    public void beforeMethod()
    {
        queryFacade = createServiceFacade("test");
    }

    private IQueryApiFacade createServiceFacade(String userName)
    {
        return FacadeFactory.create(TestInstanceHostUtils.getOpenBISUrl(), userName, "a");
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices()
    {
        return queryFacade.listAggregationServices();
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions()
    {
        return queryFacade.listTableReportDescriptions();
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String serviceCode,
            Map<String, Object> parameters)
    {
        return createReportFromAggregationService("test", serviceCode, parameters);
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String user, String serviceCode,
            Map<String, Object> parameters)
    {
        AggregationServiceDescription service = getAggregationServiceDescription(serviceCode);
        return createServiceFacade(user).createReportFromAggregationService(service, parameters);
    }

    @Override
    public QueryTableModel createReportFromDataSets(ReportDescription description,
            List<String> dataSetCodes)
    {
        return queryFacade.createReportFromDataSets(description, dataSetCodes);
    }

    @Override
    public IGeneralInformationService getGeneralInformationService()
    {
        return queryFacade.getGeneralInformationService();
    }

    @Override
    public String getSessionToken()
    {
        return queryFacade.getSessionToken();
    }

    @Override
    public String getTestId()
    {
        return "FACADE";
    }
}
