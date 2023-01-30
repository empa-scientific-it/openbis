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

import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionDBStoreDAO
{

    PersonPE findPersonById(Long personId);

    OperationExecutionPE findExecutionByCode(String code);

    void createExecution(OperationExecutionPE executionPE);

    void updateExecutionProgress(String code, String progress);

    void deleteExecution(OperationExecutionPE executionPE);

    List<OperationExecutionPE> findAllExecutions();

    List<OperationExecutionPE> findExecutionsToBeFailedAfterServerRestart(Date serverStartDate);

    List<OperationExecutionPE> findExecutionsToBeTimeOutPending();

    List<OperationExecutionPE> findExecutionsToBeTimedOut();

    List<OperationExecutionPE> findExecutionsToBeDeleted();

}
