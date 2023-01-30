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

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Facade for openBIS query service.
 * 
 * @author Franz-Josef Elmer
 */
public interface IQueryApiFacade
{
    /**
     * Return the session token for the logged-in user.
     */
    @Retry
    public String getSessionToken();

    /**
     * Lists all queries the user has access rights.
     */
    @Retry
    public List<QueryDescription> listQueries();

    /**
     * Executes specified query by using specified parameter bindings.
     */
    @Retry
    public QueryTableModel executeQuery(long queryID, Map<String, String> parameterBindings);

    /**
     * Returns meta data for all reporting plugins which deliver a table.
     */
    @Retry
    public List<ReportDescription> listTableReportDescriptions();

    /**
     * Creates for the specified data sets and specified report description a report. Available report descriptions can be obtained by
     * {@link #listTableReportDescriptions()}.
     */
    @Retry
    public QueryTableModel createReportFromDataSets(ReportDescription reportDescription,
            List<String> dataSetCodes);

    /**
     * Creates for the specified data sets and specified report key a report. It groups the data sets by a data store and creates a report for each
     * group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method. Available
     * report keys can be obtained by {@link #listTableReportDescriptions()}.
     */
    @Retry
    public QueryTableModel createReportFromDataSets(String reportKey, List<String> dataSetCodes);

    /**
     * Returns a remote access to the {@link IGeneralInformationService}.
     */
    @Retry
    public IGeneralInformationService getGeneralInformationService();

    /**
     * List the available aggregation and ingestion services
     */
    @Retry
    public List<AggregationServiceDescription> listAggregationServices();

    /**
     * Executes the specified aggregation or ingestion service for the specified parameters and creates a report. Available service descriptions can
     * be obtained by {@link #listAggregationServices()}.
     */
    @Retry
    public QueryTableModel createReportFromAggregationService(
            AggregationServiceDescription service, Map<String, Object> parameters);

    /**
     * Logs current user out.
     */
    public void logout();

}