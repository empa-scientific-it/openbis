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

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteOperationExecutor implements IExecuteOperationExecutor
{

    @Autowired
    private ISynchronousOperationExecutor synchronousExecutor;

    @Autowired
    private IAsynchronousOperationExecutor asynchronousExecutor;

    @Override
    @Transactional
    public IOperationExecutionResults execute(Session session, List<? extends IOperation> operations, IOperationExecutionOptions options)
    {
        if (options instanceof SynchronousOperationExecutionOptions)
        {
            return synchronousExecutor.execute(new OperationContext(session, false), operations, (SynchronousOperationExecutionOptions) options);
        } else if (options instanceof AsynchronousOperationExecutionOptions)
        {
            return asynchronousExecutor.execute(new OperationContext(session, true), operations, (AsynchronousOperationExecutionOptions) options);
        } else
        {
            throw new IllegalArgumentException("Operation execution options " + options + " are not supported");
        }
    }
}
