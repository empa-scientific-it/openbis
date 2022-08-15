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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IOperationListener;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperation;
import ch.systemsx.cisd.openbis.generic.server.IConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pkupczyk
 */
public abstract class OperationExecutor<OPERATION extends IOperation, RESULT extends IOperationResult> implements IOperationExecutor
{
    @Autowired
    protected IConcurrentOperationLimiter operationLimiter;

    private static List<IOperationListener<IOperation, IOperationResult>> operationListeners;

    public static void setOperationListeners(List<IOperationListener<IOperation, IOperationResult>> operationListeners) {
        OperationExecutor.operationListeners = operationListeners;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<IOperation, IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        Map<OPERATION, RESULT> results = new HashMap<OPERATION, RESULT>();
        Class<? extends OPERATION> operationClass = getOperationClass();

        for (IOperation operation : operations)
        {
            if (operation != null && operationClass.isAssignableFrom(operation.getClass()))
            {
                OPERATION theOperation = (OPERATION) operation;
                RESULT result;

                if (context.isAsync())
                {
                    result = operationLimiter.executeLimitedWithTimeoutAsync(theOperation.getClass().getSimpleName(), new ConcurrentOperation<RESULT>()
                        {
                            @Override
                            public RESULT execute()
                            {
                                beforeOperation(context.getSession(), theOperation);
                                RESULT result = null;
                                RuntimeException runtimeException = null;
                                try {
                                    result = doExecute(context, theOperation);
                                } catch (RuntimeException e) {
                                    runtimeException = e;
                                } finally {
                                    afterOperation(context.getSession(), theOperation, result, runtimeException);
                                    if (runtimeException != null) {
                                        throw runtimeException;
                                    }
                                }
                                return result;
                            }
                        });
                } else
                {
                    result = operationLimiter.executeLimitedWithTimeout(theOperation.getClass().getSimpleName(), new ConcurrentOperation<RESULT>()
                        {
                            @Override
                            public RESULT execute()
                            {
                                beforeOperation(context.getSession(), theOperation);
                                RESULT result = null;
                                RuntimeException runtimeException = null;
                                try {
                                    result = doExecute(context, theOperation);
                                } catch (RuntimeException e) {
                                    runtimeException = e;
                                } finally {
                                    afterOperation(context.getSession(), theOperation, result, runtimeException);
                                    if (runtimeException != null) {
                                        throw runtimeException;
                                    }
                                }
                                return result;
                            }
                        });
                }

                results.put(theOperation, result);
            }
        }

        return (Map<IOperation, IOperationResult>) results;
    }

    protected abstract Class<? extends OPERATION> getOperationClass();

    protected abstract RESULT doExecute(IOperationContext context, OPERATION operation);

    private void beforeOperation(Session session, OPERATION operation) {
        if (operationListeners != null && !operationListeners.isEmpty()) {
            IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();
            for (IOperationListener<IOperation, IOperationResult> operationListener:operationListeners) {
                operationListener.beforeOperation(applicationServerApi, session, operation);
            }
        }
    }

    private void afterOperation(Session session, OPERATION operation, RESULT result, RuntimeException runtimeException) {
        if (operationListeners != null && !operationListeners.isEmpty()) {
            IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();
            session.getPrincipal().getUserId();
            session.getSessionToken();
            for (IOperationListener<IOperation, IOperationResult> operationListener:operationListeners) {
                operationListener.afterOperation(applicationServerApi, session, operation, result, runtimeException);
            }
        }
    }
}
