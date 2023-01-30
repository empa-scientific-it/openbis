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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteSqlOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteSqlOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteSqlOperationExecutor extends OperationExecutor<ExecuteSqlOperation, ExecuteSqlOperationResult>
        implements IExecuteSqlOperationExecutor
{
    @Autowired
    private IExecuteSqlExecutor executor;

    @Override
    protected Class<? extends ExecuteSqlOperation> getOperationClass()
    {
        return ExecuteSqlOperation.class;
    }

    @Override
    protected ExecuteSqlOperationResult doExecute(IOperationContext context, ExecuteSqlOperation operation)
    {
        TableModel result = executor.execute(context, operation.getSql(), operation.getOptions());
        return new ExecuteSqlOperationResult(result);
    }

}
