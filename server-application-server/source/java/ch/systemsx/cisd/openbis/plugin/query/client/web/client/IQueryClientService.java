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

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
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
 * Service interface for the query module GWT client.
 * <p>
 * Each method should declare throwing {@link UserFailureException}. The authorization framework can throw it when the user has insufficient
 * privileges. If it is not marked, the GWT client will report unexpected exception.
 * </p>
 * 
 * @author Piotr Buczek
 */
public interface IQueryClientService extends IClientService
{

    /** Returns number of databases configured for queries. */
    public int initDatabases() throws UserFailureException;

    /** Returns databases used for queries. */
    public List<QueryDatabase> listQueryDatabases() throws UserFailureException;

    /** Returns results of the query with specified id. */
    public TableModelReference createQueryResultsReport(TechId queryId,
            QueryParameterBindings bindingsOrNull) throws UserFailureException;

    /** Returns results of the specified SQL query. */
    public TableModelReference createQueryResultsReport(QueryDatabase queryDatabase,
            String sqlQuery, QueryParameterBindings bindingsOrNull) throws UserFailureException;

    /** Returns a list of {@link ParameterValue}s from the specified SQL query. */
    public List<ParameterValue> listParameterValues(QueryDatabase queryDatabase, String sqlQuery)
            throws UserFailureException;

    /** Returns a list of all the canned custom queries created so far. */
    public List<QueryExpression> listQueries(QueryType queryType, BasicEntityType entityTypeOrNull)
            throws UserFailureException;

    /**
     * Returns all queries for the specified configuration.
     */
    public TypedTableResultSet<QueryExpression> listQueries(
            IResultSetConfig<String, TableModelRowWithObject<QueryExpression>> resultSetConfig) throws UserFailureException;

    /**
     * Prepares export of queries.
     */
    public String prepareExportQueries(TableExportCriteria<TableModelRowWithObject<QueryExpression>> criteria)
            throws UserFailureException;

    /**
     * Registers specified new query.
     */
    public void registerQuery(NewQuery query) throws UserFailureException;

    /**
     * Deletes specified queries.
     */
    public void deleteQueries(List<TechId> filterIds) throws UserFailureException;

    /**
     * Updates specified query.
     */
    public void updateQuery(final IQueryUpdates queryUpdate) throws UserFailureException;
}
