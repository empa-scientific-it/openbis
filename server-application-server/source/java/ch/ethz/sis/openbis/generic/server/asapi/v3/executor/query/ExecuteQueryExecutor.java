/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteQueryExecutor extends AbstractExecuteExecutor implements IExecuteQueryExecutor
{

    @Override
    public TableModel execute(IOperationContext context, IQueryId queryId, QueryExecutionOptions options)
    {
        if (queryId == null)
        {
            throw new UserFailureException("Query id cannot be null");
        }
        if (options == null)
        {
            throw new UserFailureException("Query execution options cannot be null");
        }

        QueryPE query = getQuery(context, queryId);
        DatabaseDefinition database = getDatabase(context, new QueryDatabaseName(query.getQueryDatabaseKey()));

        authorizationExecutor.canExecute(context, queryId, query);

        return doExecute(context, StringEscapeUtils.unescapeHtml4(query.getExpression()), database, options.getParameters());
    }

}
