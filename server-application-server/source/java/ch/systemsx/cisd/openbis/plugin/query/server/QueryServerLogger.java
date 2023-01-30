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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
class QueryServerLogger extends AbstractServerLogger implements IQueryServer
{
    QueryServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public int initDatabases(String sessionToken)
    {
        logAccess(sessionToken, "init_databases");
        return 0;
    }

    public String tryToGetQueryDatabaseLabel(String sessionToken)
    {
        logAccess(sessionToken, "try_to_get_query_database_label");
        return null;
    }

    public List<String> listQueryDatabaseLabels(String sessionToken)
    {
        logAccess(sessionToken, "list_query_database_labels");
        return null;
    }

    @Override
    public List<QueryDatabase> listQueryDatabases(String sessionToken)
    {
        logAccess(sessionToken, "list_query_databases");
        return null;
    }

    @Override
    public List<QueryExpression> listQueries(String sessionToken, QueryType queryType,
            BasicEntityType entityTypeOrNull)
    {
        logAccess(sessionToken, "list_queries", "QUERY_TYPE(%s) ENTITY_TYPE(%s)", queryType,
                entityTypeOrNull);
        return null;
    }

    @Override
    public void registerQuery(String sessionToken, NewQuery expression)
    {
        logTracking(sessionToken, "register_query", "EXPRESSION(%s)", expression.getName());
    }

    @Override
    public void deleteQueries(String sessionToken, List<TechId> filterIds)
    {
        logTracking(sessionToken, "delete_queries", "QUERIES(%s)", filterIds.size());
    }

    @Override
    public void updateQuery(String sessionToken, IQueryUpdates updates)
    {
        logTracking(sessionToken, "update_query", "ID(%s) QUERY_NAME(%s)", updates.getId(),
                updates.getName());
    }

    @Override
    public TableModel queryDatabase(String sessionToken, QueryDatabase database, String sqlQuery,
            QueryParameterBindings bindings, boolean onlyPerform)
    {
        logAccess(sessionToken, "query_database", "DB(%s) SQL(%s) BINDINGS(%s) ACTION(%s)",
                database, sqlQuery, bindings, onlyPerform ? "perform" : "create & perform");
        return null;

    }

    @Override
    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings)
    {
        logAccess(sessionToken, "query_database", "QUERY(%s) BINDINGS(%s)", queryId, bindings);
        return null;
    }

}
