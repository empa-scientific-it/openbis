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

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionProgress;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionFSStore
{

    void executionNew(String code, List<? extends IOperation> operations);

    void executionProgressed(String code, OperationExecutionProgress progress);

    void executionFailed(String code, IOperationExecutionError error);

    void executionFinished(String code, List<? extends IOperationResult> results);

    void executionAvailability(String permId, OperationExecutionAvailability availability);

    void executionSummaryAvailability(String code, OperationExecutionAvailability summaryAvailability);

    void executionDetailsAvailability(String code, OperationExecutionAvailability detailsAvailability);

    OperationExecutionFS getExecution(String code, OperationExecutionFSFetchOptions fo);

}
