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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Piotr Buczek
 */
public interface IQueryClientServiceAsync extends IClientServiceAsync
{

    /** @see IQueryClientService#initDatabases() */
    public void initDatabases(AsyncCallback<Integer> callback);

    /** @see IQueryClientService#listQueryDatabases() */
    public void listQueryDatabases(AsyncCallback<List<QueryDatabase>> callback);

    /** @see IQueryClientService#createQueryResultsReport(TechId, QueryParameterBindings) */
    public void createQueryResultsReport(TechId techId, QueryParameterBindings bindingsOrNull,
            AsyncCallback<TableModelReference> callback);

    /**
     * @see IQueryClientService#createQueryResultsReport(QueryDatabase, String, QueryParameterBindings)
     */
    public void createQueryResultsReport(QueryDatabase queryDatabase, String sqlQuery,
            QueryParameterBindings bindingsOrNull, AsyncCallback<TableModelReference> callback);

    /**
     * @see IQueryClientService#listParameterValues(QueryDatabase, String)
     */
    public void listParameterValues(QueryDatabase queryDatabase, String sqlQuery,
            AsyncCallback<List<ParameterValue>> listParameterValuesCallback);

    /** @see IQueryClientService#listQueries(IResultSetConfig) */
    public void listQueries(IResultSetConfig<String, TableModelRowWithObject<QueryExpression>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<QueryExpression>> callback);

    /** @see IQueryClientService#listQueries(QueryType, BasicEntityType) */
    public void listQueries(QueryType queryType, BasicEntityType entityTypeOrNull,
            AsyncCallback<List<QueryExpression>> callback) throws UserFailureException;

    /** @see IQueryClientService#prepareExportQueries(TableExportCriteria) */
    public void prepareExportQueries(TableExportCriteria<TableModelRowWithObject<QueryExpression>> criteria,
            AsyncCallback<String> callback);

    /** @see IQueryClientService#registerQuery(NewQuery) */
    public void registerQuery(NewQuery query, AsyncCallback<Void> callback);

    /** @see IQueryClientService#deleteQueries(List) */
    public void deleteQueries(List<TechId> filterIds, AsyncCallback<Void> callback);

    /** @see IQueryClientService#updateQuery(IQueryUpdates) */
    public void updateQuery(final IQueryUpdates queryUpdate, AsyncCallback<Void> callback);

}
