/*
 * Copyright 2018 ETH Zuerich, SIS
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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteSqlExecutor extends AbstractExecuteExecutor implements IExecuteSqlExecutor
{

    @Override
    public TableModel execute(IOperationContext context, String sql, SqlExecutionOptions options)
    {
        if (sql == null)
        {
            throw new UserFailureException("Sql cannot be null");
        }
        if (options == null)
        {
            throw new UserFailureException("Sql execution options cannot be null");
        }
        if (options.getDatabaseId() == null)
        {
            throw new UserFailureException("Database id cannot be null");
        }

        DatabaseDefinition database = getDatabase(context, options.getDatabaseId());

        authorizationExecutor.canExecute(context, sql, database);

        return doExecute(context, sql, database, options.getParameters());
    }

}
