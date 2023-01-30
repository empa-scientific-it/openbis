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

import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionDBStore
{

    void executionNew(String code, Long owner, String description, String notification, List<String> operations, long availabilityTime,
            long summaryAvailabilityTime, long detailsAvailabilityTime, String detailsPath);

    void executionScheduled(String code);

    void executionRunning(String code);

    void executionProgressed(String code, String progress);

    void executionFailed(String code, String error);

    void executionFinished(String code, List<String> results);

    void executionAvailability(String code, OperationExecutionAvailability availability);

    void executionSummaryAvailability(String code, OperationExecutionAvailability summaryAvailability);

    void executionDetailsAvailability(String code, OperationExecutionAvailability detailsAvailability);

    OperationExecutionPE getExecution(String code);

    List<OperationExecutionPE> getExecutions();

    List<OperationExecutionPE> getExecutionsToBeFailedAfterServerRestart(Date serverStartDate);

    List<OperationExecutionPE> getExecutionsToBeTimeOutPending();

    List<OperationExecutionPE> getExecutionsToBeTimedOut();

    List<OperationExecutionPE> getExecutionsToBeDeleted();

}
