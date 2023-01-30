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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteSearchDomainServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteSearchDomainServiceOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class ExecuteSearchDomainServiceOperationExecutor
        extends OperationExecutor<ExecuteSearchDomainServiceOperation, ExecuteSearchDomainServiceOperationResult>
        implements IExecuteSearchDomainServiceOperationExecutor
{
    @Autowired
    private IExecuteSearchDomainServiceExecutor executor;

    @Override
    protected Class<? extends ExecuteSearchDomainServiceOperation> getOperationClass()
    {
        return ExecuteSearchDomainServiceOperation.class;
    }

    @Override
    protected ExecuteSearchDomainServiceOperationResult doExecute(IOperationContext context, ExecuteSearchDomainServiceOperation operation)
    {
        SearchResult<SearchDomainServiceExecutionResult> result = executor.execute(context, operation.getOptions());
        return new ExecuteSearchDomainServiceOperationResult(result);
    }

}
