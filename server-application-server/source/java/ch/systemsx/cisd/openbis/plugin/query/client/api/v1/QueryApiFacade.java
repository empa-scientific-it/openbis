/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Franz-Josef Elmer
 */
class QueryApiFacade implements IQueryApiFacade
{
    private final IQueryApiServer service;

    private final IGeneralInformationService generalInformationService;

    private final String sessionToken;

    private final int serviceMajorVersion;

    private final int serviceMinorVersion;

    QueryApiFacade(IQueryApiServer service, IGeneralInformationService generalInformationService,
            String sessionToken)
    {
        this.service = service;
        this.generalInformationService = generalInformationService;
        this.sessionToken = sessionToken;

        this.serviceMajorVersion = this.service.getMajorVersion();
        this.serviceMinorVersion = this.service.getMinorVersion();

    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    @Override
    public void logout()
    {
        service.logout(sessionToken);
    }

    @Override
    public IGeneralInformationService getGeneralInformationService()
    {
        return generalInformationService;
    }

    @Override
    public List<QueryDescription> listQueries()
    {
        return service.listQueries(sessionToken);
    }

    @Override
    public QueryTableModel executeQuery(long queryID, Map<String, String> parameterBindings)
    {
        return service.executeQuery(sessionToken, queryID, parameterBindings);
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions()
    {
        return service.listTableReportDescriptions(sessionToken);
    }

    @Override
    public QueryTableModel createReportFromDataSets(ReportDescription reportDescription,
            List<String> dataSetCodes)
    {
        return service.createReportFromDataSets(sessionToken, reportDescription.getDataStoreCode(),
                reportDescription.getKey(), dataSetCodes);
    }

    @Override
    public QueryTableModel createReportFromDataSets(String reportKey, List<String> dataSetCodes)
    {
        return service.createReportFromDataSets(sessionToken, reportKey, dataSetCodes);
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices()
    {
        checkMinimalServerVersion(1, 3);
        return service.listAggregationServices(sessionToken);
    }

    @Override
    public QueryTableModel createReportFromAggregationService(
            AggregationServiceDescription serviceDescription, Map<String, Object> parameters)
    {
        checkMinimalServerVersion(1, 3);
        return service.createReportFromAggregationService(sessionToken,
                serviceDescription.getDataStoreCode(), serviceDescription.getServiceKey(),
                parameters);
    }

    /**
     * Utility method to check that the server has at least the minimal required version.
     */
    private void checkMinimalServerVersion(int majorVersion, int minorVersion)
    {
        if ((serviceMajorVersion <= majorVersion) && (serviceMinorVersion < minorVersion))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The service \"listAggregationServices\" is not available on this server.");
            sb.append(" Server version must be greater than 1.3");
            sb.append(" (server version is ");
            sb.append(serviceMajorVersion);
            sb.append(".");
            sb.append(serviceMinorVersion);

            throw new UnsupportedOperationException(sb.toString());
        }
    }
}
