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

package ch.systemsx.cisd.openbis.plugin.query.server.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Franz-Josef Elmer
 */
class QueryApiLogger extends AbstractServerLogger implements IQueryApiServer
{

    QueryApiLogger(ISessionManager<Session> sessionManager, IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public String tryToAuthenticateAtQueryServer(String userID, String userPassword)
    {
        return null;
    }

    @Override
    public List<QueryDescription> listQueries(String sessionToken)
    {
        logAccess(sessionToken, "list_queries");
        return null;
    }

    @Override
    public QueryTableModel executeQuery(String sessionToken, long queryID,
            Map<String, String> parameterBindings)
    {
        logAccess(sessionToken, "execute_query", "QUERY(%s) PARAMETERS(%s)", queryID,
                parameterBindings.size());
        return null;
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions(String sessionToken)
    {
        logAccess(sessionToken, "list_table_report_descriptions");
        return null;
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String dataStoreCode,
            String serviceKey, List<String> dataSetCodes)
    {
        logAccess(sessionToken, "create_report_from_data_sets",
                "DATA_STORE(%s) SERVICE(%s) DATA_SETS(%s)", dataStoreCode, serviceKey, dataSetCodes);
        return null;
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes)
    {
        logAccess(sessionToken, "create_report_from_data_sets", "SERVICE(%s) DATA_SETS(%s)",
                serviceKey, dataSetCodes);
        return null;
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
    {
        logAccess(sessionToken, "list_aggregation_services");
        return null;
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String dataStoreCode, String serviceKey, Map<String, Object> parameters)
    {
        logAccess(sessionToken, "create_report_from_aggregation_service",
                "DATA_STORE(%s) SERVICE(%s) PARAMETER_KEYS(%s)", dataStoreCode, serviceKey, parameters.keySet());
        return null;
    }

}
