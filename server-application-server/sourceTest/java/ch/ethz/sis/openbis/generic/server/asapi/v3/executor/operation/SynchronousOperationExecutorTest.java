/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;

public class SynchronousOperationExecutorTest
{

    private Mockery mockery;

    private IOperationContext context;

    private IOperationExecutionStore executionStore;

    private IOperationsExecutor operationsExecutor;

    private List<? extends IOperation> operations;

    private List<? extends IOperationResult> operationResults;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    private void beforeMethod()
    {
        mockery = new Mockery();

        context = mockery.mock(IOperationContext.class);
        executionStore = mockery.mock(IOperationExecutionStore.class);
        operationsExecutor = mockery.mock(IOperationsExecutor.class);
        operations = mockery.mock(List.class, "operations");
        operationResults = mockery.mock(List.class, "operationResults");
    }

    @Test
    public void testExecuteWithoutExecutionId()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();

        mockery.checking(new Expectations()
            {
                {
                    oneOf(operationsExecutor).execute(context, operations, options);
                    will(returnValue(operationResults));
                }
            });

        executeAndAssertResults(options, operationResults);
    }

    @Test
    public void testExecuteWithExecutionId()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);
                    oneOf(executionStore).executionRunning(context, options.getExecutionId());

                    oneOf(operationsExecutor).execute(context, operations, options);
                    will(returnValue(operationResults));

                    oneOf(executionStore).executionFinished(context, options.getExecutionId(), operationResults);
                }
            });

        executeAndAssertResults(options, operationResults);
    }

    @Test
    public void testExecuteWithOperationThatFails()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        final TestException exception = new TestException();

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);
                    oneOf(executionStore).executionRunning(context, options.getExecutionId());

                    oneOf(operationsExecutor).execute(context, operations, options);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context, options.getExecutionId(), new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(options, exception);
    }

    @Test
    public void testExecuteWithExecutionNewThatFails()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        final TestException exception = new TestException();

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context, options.getExecutionId(), new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(options, exception);
    }

    @Test
    public void testExecuteWithExecutionRunningThatFails()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        final TestException exception = new TestException();

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);

                    oneOf(executionStore).executionRunning(context, options.getExecutionId());
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context, options.getExecutionId(), new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(options, exception);
    }

    @Test
    public void testExecuteWithExecutionFinishedThatFails()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        final TestException exception = new TestException();

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);
                    oneOf(executionStore).executionRunning(context, options.getExecutionId());

                    oneOf(operationsExecutor).execute(context, operations, options);
                    will(returnValue(operationResults));

                    oneOf(executionStore).executionFinished(context, options.getExecutionId(), operationResults);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context, options.getExecutionId(), new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(options, exception);
    }

    @Test
    public void testExecuteWithExecutionFailedThatFails()
    {
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        final TestException exception1 = new TestException("testException1");
        final TestException exception2 = new TestException("testException2");

        mockery.checking(new Expectations()
            {
                {
                    oneOf(executionStore).executionNew(context, options.getExecutionId(), operations, options);
                    oneOf(executionStore).executionRunning(context, options.getExecutionId());

                    oneOf(operationsExecutor).execute(context, operations, options);
                    will(throwException(exception1));

                    oneOf(executionStore).executionFailed(context, options.getExecutionId(), new OperationExecutionError(exception1));
                    will(throwException(exception2));
                }
            });

        executeAndAssertException(options, exception2);
    }

    private SynchronousOperationExecutionResults execute(final SynchronousOperationExecutionOptions options)
    {
        SynchronousOperationExecutor executor = new SynchronousOperationExecutor(executionStore, operationsExecutor);
        return executor.execute(context, operations, options);
    }

    private void executeAndAssertResults(SynchronousOperationExecutionOptions options, List<? extends IOperationResult> results)
    {
        SynchronousOperationExecutionResults result = execute(options);
        Assert.assertEquals(result.getResults(), results);
        mockery.assertIsSatisfied();
    }

    private void executeAndAssertException(final SynchronousOperationExecutionOptions options, Exception exception)
    {
        try
        {
            execute(options);
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e, exception);
        }
        mockery.assertIsSatisfied();
    }

}
