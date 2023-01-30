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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.util.Date;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionStore
{

    public void executionNew(IOperationContext context, OperationExecutionPermId executionId, List<? extends IOperation> operations,
            IOperationExecutionOptions options);

    public void executionScheduled(IOperationContext context, OperationExecutionPermId executionId);

    public void executionRunning(IOperationContext context, OperationExecutionPermId executionId);

    public void executionFailed(IOperationContext context, OperationExecutionPermId executionId, IOperationExecutionError error);

    public void executionFinished(IOperationContext context, OperationExecutionPermId executionId, List<? extends IOperationResult> results);

    public void executionAvailability(IOperationContext context, OperationExecutionPermId executionId, OperationExecutionAvailability availability);

    public void executionSummaryAvailability(IOperationContext context, OperationExecutionPermId executionId,
            OperationExecutionAvailability availability);

    public void executionDetailsAvailability(IOperationContext context, OperationExecutionPermId executionId,
            OperationExecutionAvailability availability);

    public void synchronizeProgress();

    public OperationExecution getExecution(IOperationContext context, IOperationExecutionId executionId, OperationExecutionFetchOptions fetchOptions);

    public List<OperationExecution> getExecutions(IOperationContext context, OperationExecutionFetchOptions fetchOptions);

    public List<OperationExecution> getExecutionsToBeFailedAfterServerRestart(IOperationContext context, Date serverStartDate,
            OperationExecutionFetchOptions fetchOptions);

    public List<OperationExecution> getExecutionsToBeTimeOutPending(IOperationContext context, OperationExecutionFetchOptions fetchOptions);

    public List<OperationExecution> getExecutionsToBeTimedOut(IOperationContext context, OperationExecutionFetchOptions fetchOptions);

    public List<OperationExecution> getExecutionsToBeDeleted(IOperationContext context, OperationExecutionFetchOptions fetchOptions);

}
