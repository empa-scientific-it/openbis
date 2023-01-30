/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteCustomASServiceOperationExecutor extends OperationExecutor<ExecuteCustomASServiceOperation, ExecuteCustomASServiceOperationResult>
        implements IExecuteCustomASServiceOperationExecutor
{

    @Autowired
    private IExecuteCustomASServiceExecutor executor;

    @Override
    protected Class<? extends ExecuteCustomASServiceOperation> getOperationClass()
    {
        return ExecuteCustomASServiceOperation.class;
    }

    @Override
    protected ExecuteCustomASServiceOperationResult doExecute(IOperationContext context, ExecuteCustomASServiceOperation operation)
    {
        Object result = executor.execute(context, operation.getServiceId(), operation.getOptions());
        return new ExecuteCustomASServiceOperationResult(result);
    }

}
