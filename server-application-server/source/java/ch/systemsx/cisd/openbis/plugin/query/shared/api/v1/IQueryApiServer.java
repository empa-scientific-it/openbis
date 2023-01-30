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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Public API interface to query server (version 1.4).
 * 
 * @author Franz-Josef Elmer
 */
// DO NOT CHANGE THE INTERFACE CONTRACT IN A NON-BACKWARD COMPATIBLE WAY!
public interface IQueryApiServer extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "query";

    /**
     * Service part of the URL to access this service remotely.
     */
    public final static String QUERY_PLUGIN_SERVER_URL = "/rmi-" + SERVICE_NAME + "-v1";

    public static final String JSON_SERVICE_URL = QUERY_PLUGIN_SERVER_URL + ".json";

    /**
     * Tries to authenticate specified user with specified password. Returns session token if succeeded otherwise <code>null</code> is returned.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateAtQueryServer(String userID, String userPassword);

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    public void logout(String sessionToken);

    /**
     * Lists all queries available for the user of the specified session.
     */
    @Transactional(readOnly = true)
    public List<QueryDescription> listQueries(String sessionToken);

    /**
     * Executes specified query using specified parameter bindings.
     */
    @Transactional(readOnly = true)
    public QueryTableModel executeQuery(String sessionToken, long queryID,
            Map<String, String> parameterBindings);

    /**
     * Returns meta data for all reporting plugins which deliver a table.
     * 
     * @since 1.2
     */
    @Transactional(readOnly = true)
    public List<ReportDescription> listTableReportDescriptions(String sessionToken);

    /**
     * Creates for the specified data sets a report. Available reports can be obtained by {@link #listTableReportDescriptions(String)}.
     * 
     * @param dataStoreCode Code of the data store.
     * @param serviceKey Key of the data store service.
     * @since 1.2
     */
    @Transactional(readOnly = true)
    public QueryTableModel createReportFromDataSets(String sessionToken, String dataStoreCode,
            String serviceKey, List<String> dataSetCodes);

    /**
     * Creates for the specified data sets a report. It groups the data sets by a data store and creates a report for each group of objects on
     * appropriate data store server. Results from the data stores are combined and returned as a result of this method. Available report keys can be
     * obtained by {@link #listTableReportDescriptions(String)}.
     * 
     * @param serviceKey Key of the data store service.
     * @since 1.6
     */
    @Transactional(readOnly = true)
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes);

    /**
     * Returns metadata for all aggregation and ingestion services.
     * 
     * @since 1.3
     */
    @Transactional(readOnly = true)
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken);

    /**
     * Executes the specified aggregation or ingestion service for the specified parameters and creates a report. Available service descriptions can
     * be obtained by {@link #listAggregationServices(String)}. The service resolved to by the serviceKey must be a service of type REPORT.
     * 
     * @param sessionToken A valid session token.
     * @param dataStoreCode Code of the data store.
     * @param serviceKey Key of the service.
     * @param parameters Parameters to the service.
     * @since 1.3
     */
    @Transactional(readOnly = true)
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String dataStoreCode, String serviceKey, Map<String, Object> parameters);

}
