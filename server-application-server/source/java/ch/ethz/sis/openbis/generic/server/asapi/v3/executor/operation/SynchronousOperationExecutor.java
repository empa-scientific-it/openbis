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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component
public class SynchronousOperationExecutor implements ISynchronousOperationExecutor
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, SynchronousOperationExecutor.class);

    @Autowired
    private IOperationExecutionStore executionStore;

    @Autowired
    private IOperationsExecutor operationsExecutor;

    public SynchronousOperationExecutor()
    {
    }

    SynchronousOperationExecutor(IOperationExecutionStore executionStore, IOperationsExecutor operationsExecutor)
    {
        this.executionStore = executionStore;
        this.operationsExecutor = operationsExecutor;
    }

    @Override
    public SynchronousOperationExecutionResults execute(IOperationContext context, List<? extends IOperation> operations,
            SynchronousOperationExecutionOptions options)
    {
        OperationExecutionPermId executionId = options.getExecutionId();

        if (executionId == null && options.getNotification() != null)
        {
            throw new UserFailureException(
                    "Synchronous operation execution must have an execution id specified for a notification to work.");
        }

        try
        {
            if (executionId != null)
            {
                executionStore.executionNew(context, executionId, operations, options);
                executionStore.executionRunning(context, executionId);
            }

            List<IOperationResult> results = operationsExecutor.execute(context, operations, options);

            if (executionId != null)
            {
                executionStore.executionFinished(context, executionId, results);
            }

            return new SynchronousOperationExecutionResults(results);
        } catch (Exception e)
        {
            log.error(e);
            if (executionId != null)
            {
                executionStore.executionFailed(context, executionId, new OperationExecutionError(e));
            }

            throw e;
        }
    }

}
