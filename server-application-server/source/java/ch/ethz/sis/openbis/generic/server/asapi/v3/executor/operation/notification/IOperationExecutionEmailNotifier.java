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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionEmailNotifier
{

    public void executionFinished(String code, String description, List<String> operations, List<String> results,
            OperationExecutionEmailNotification notification);

    public void executionFailed(String code, String description, List<String> operations, String error,
            OperationExecutionEmailNotification notification);

}
