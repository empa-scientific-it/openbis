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

package ch.systemsx.cisd.openbis.plugin.query.client.web.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;
import ch.systemsx.cisd.openbis.plugin.query.client.web.server.resultset.QueryExpressionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Piotr Buczek
 */
@Component(value = ResourceNames.QUERY_PLUGIN_SERVICE)
public class QueryClientService extends AbstractClientService implements IQueryClientService
{

    private static final String QUERY_EXECUTION_ERROR_MSG =
            "Problem occured during query execution.<br><br>Check that all provided parameter values are correct. "
                    + "If everything seems fine contact query registrator or instance admin about a possible bug in the query definition.";

    @Resource(name = ResourceNames.QUERY_PLUGIN_SERVER)
    private IQueryServer queryServer;

    public QueryClientService()
    {
    }

    @Private
    QueryClientService(final IQueryServer queryServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.queryServer = queryServer;
    }

    @Override
    protected IServer getServer()
    {
        return queryServer;
    }

    //
    // IQueryClientService
    //

    @Override
    public int initDatabases()
    {
        final String sessionToken = getSessionToken();
        return queryServer.initDatabases(sessionToken);
    }

    @Override
    public List<QueryDatabase> listQueryDatabases()
    {
        final String sessionToken = getSessionToken();
        return queryServer.listQueryDatabases(sessionToken);
    }

    @Override
    public TableModelReference createQueryResultsReport(QueryDatabase database, String sqlQuery,
            QueryParameterBindings bindingsOrNull)
    {
        final String sessionToken = getSessionToken();
        final TableModel tableModel =
                queryServer.queryDatabase(sessionToken, database, sqlQuery, bindingsOrNull, false);
        return createTableModelReference(tableModel);
    }

    @Override
    public List<ParameterValue> listParameterValues(QueryDatabase database, String sqlQuery)
    {
        final String sessionToken = getSessionToken();
        final TableModel tableModel =
                queryServer.queryDatabase(sessionToken, database, sqlQuery, null, true);
        // TreeSet is used because we want distinct values and we want them to be sorted
        Set<ParameterValue> valuesSet = new TreeSet<ParameterValue>();
        boolean withDescription = tableModel.getHeader().size() > 1;
        for (TableModelRow row : tableModel.getRows())
        {
            final String value = row.getValues().get(0).toString();
            final String description = withDescription ? row.getValues().get(1).toString() : null;
            valuesSet.add(new ParameterValue(value, description));
        }
        return new ArrayList<ParameterValue>(valuesSet);
    }

    @Override
    public TableModelReference createQueryResultsReport(TechId query,
            QueryParameterBindings bindingsOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final TableModel tableModel =
                    queryServer.queryDatabase(sessionToken, query, bindingsOrNull);
            return createTableModelReference(tableModel);
        } catch (final UserFailureException e)
        {
            if (operationLog.isInfoEnabled())
            {
                // we do not log this as an error, because it can be only user's fault
                operationLog.info(QUERY_EXECUTION_ERROR_MSG + " DETAILS: " + e.getMessage(), e);
            }
            throw UserFailureExceptionTranslator.translate(e, QUERY_EXECUTION_ERROR_MSG);
        }
    }

    private TableModelReference createTableModelReference(TableModel tableModel)
    {
        String resultSetKey = saveReportInCache(tableModel);
        return new TableModelReference(resultSetKey, tableModel.getHeader(),
                tableModel.tryGetMessage());
    }

    @Override
    public List<QueryExpression> listQueries(QueryType queryType, BasicEntityType entityTypeOrNull)
    {
        final String sessionToken = getSessionToken();
        return queryServer.listQueries(sessionToken, queryType, entityTypeOrNull);
    }

    @Override
    public TypedTableResultSet<QueryExpression> listQueries(
            final IResultSetConfig<String, TableModelRowWithObject<QueryExpression>> resultSetConfig)
    {
        return listEntities(new QueryExpressionProvider(queryServer, getSessionToken()),
                resultSetConfig);
    }

    @Override
    public String prepareExportQueries(
            TableExportCriteria<TableModelRowWithObject<QueryExpression>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public void registerQuery(NewQuery query)
    {
        queryServer.registerQuery(getSessionToken(), query);
    }

    @Override
    public void deleteQueries(List<TechId> filterIds)
    {
        queryServer.deleteQueries(getSessionToken(), filterIds);
    }

    @Override
    public void updateQuery(IQueryUpdates queryUpdate)
    {
        queryServer.updateQuery(getSessionToken(), queryUpdate);
    }
}
