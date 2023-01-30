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
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component
public class AsynchronousOperationExecutor implements IAsynchronousOperationExecutor
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, AsynchronousOperationExecutor.class);

    private NamingThreadPoolExecutor executionThreadPool;

    private IOperationExecutionIdFactory executionIdFactory;

    @Autowired
    private IOperationExecutionConfig executionConfig;

    @Autowired
    private IOperationExecutionStore executionStore;

    @Autowired
    private IAsynchronousOperationThreadPoolExecutor poolExecutor;

    public AsynchronousOperationExecutor()
    {
        executionIdFactory = new IOperationExecutionIdFactory()
            {
                @Override
                public OperationExecutionPermId createExecutionId(IOperationContext context)
                {
                    return new OperationExecutionPermId();
                }
            };
    }

    AsynchronousOperationExecutor(IOperationExecutionConfig executionConfig, IOperationExecutionIdFactory executionIdFactory,
            IOperationExecutionStore executionStore,
            IAsynchronousOperationThreadPoolExecutor poolExecutor)
    {
        this.executionIdFactory = executionIdFactory;
        this.executionConfig = executionConfig;
        this.executionStore = executionStore;
        this.poolExecutor = poolExecutor;
        init();
    }

    @PostConstruct
    private void init()
    {
        executionThreadPool =
                new NamingThreadPoolExecutor(executionConfig.getThreadPoolName(), executionConfig.getThreadPoolCoreSize(),
                        executionConfig.getThreadPoolMaxSize(), executionConfig.getThreadPoolKeepAliveTime(), TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>()).daemonize();
    }

    @Override
    public AsynchronousOperationExecutionResults execute(final IOperationContext context, final List<? extends IOperation> operations,
            final AsynchronousOperationExecutionOptions options)
    {
        final OperationExecutionPermId executionId = executionIdFactory.createExecutionId(context);

        try
        {
            executionStore.executionNew(context, executionId, operations, options);
            executionStore.executionScheduled(context, executionId);
            executionThreadPool.submit(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        try
                        {
                            // Transaction from the thread where the execution was scheduled is not available here,
                            // therefore we need to call beans with @Transactional annotation again. That's why we need the poolExecutor.

                            // Execution store is annotated to always create its own transactions to be independent from the execution
                            // (information about the execution must remain in operation_executions table even if the execution fails).

                            // The try/catch block wraps around poolExecutor because at the end of its execute method
                            // a transaction is flushed and any potential problems with the database commit
                            // will be thrown from it.

                            executionStore.executionRunning(context, executionId);
                            List<IOperationResult> results = poolExecutor.execute(context, executionId, operations, options);
                            executionStore.executionFinished(context, executionId, results);
                            return null;

                        } catch (Exception e)
                        {
                            log.error(e);
                            executionStore.executionFailed(context, executionId, new OperationExecutionError(e));
                            throw e;
                        }
                    }
                });

        } catch (Exception e)
        {
            log.error(e);
            executionStore.executionFailed(context, executionId, new OperationExecutionError(e));
            throw e;
        }

        return new AsynchronousOperationExecutionResults(executionId);
    }

    public void shutdown()
    {
        if (executionThreadPool != null)
        {
            executionThreadPool.shutdown();
        }
    }

}
