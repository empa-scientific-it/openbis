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

package ch.ethz.sis.openbis.generic.server.asapi.v3.task;

import java.util.Date;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;

/**
 * @author pkupczyk
 */
public class OperationExecutionMarkFailedAfterServerRestartMaintenanceTask extends AbstractOperationExecutionMarkMaintenanceTask
{

    private Date serverStartDate = new Date();

    @Override
    protected void doExecute()
    {
        markFailed();
    }

    private void markFailed()
    {
        final List<OperationExecution> executions =
                getExecutionStore().getExecutionsToBeFailedAfterServerRestart(getOperationContext(), serverStartDate,
                        new OperationExecutionFetchOptions());

        if (false == executions.isEmpty())
        {
            getOperationLog().info("found " + executions.size() + " execution(s) to be marked " + OperationExecutionState.FAILED);
        }

        markOperationExecutions(executions, new MarkAction()
            {

                @Override
                public void mark(OperationExecution execution)
                {
                    getExecutionStore().executionFailed(getOperationContext(), execution.getPermId(),
                            new OperationExecutionError("Execution interrupted by a server restart"));
                }
            });
    }

}
