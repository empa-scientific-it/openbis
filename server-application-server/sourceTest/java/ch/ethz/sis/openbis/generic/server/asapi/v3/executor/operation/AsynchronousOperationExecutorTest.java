package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.ConsoleLogger;

public class AsynchronousOperationExecutorTest
{

    private Mockery mockery;

    private IOperationExecutionConfig executionConfig;

    private IOperationExecutionIdFactory executionIdFactory;

    private IOperationExecutionStore executionStore;

    private IAsynchronousOperationThreadPoolExecutor poolExecutor;

    private IOperationContext context1;

    private IOperationContext context2;

    private List<? extends IOperation> operations1;

    private List<? extends IOperation> operations2;

    private List<? extends IOperationResult> operationResults1;

    private List<? extends IOperationResult> operationResults2;

    private AsynchronousOperationExecutionOptions options1;

    private AsynchronousOperationExecutionOptions options2;

    private OperationExecutionPermId executionId1 = new OperationExecutionPermId();

    private OperationExecutionPermId executionId2 = new OperationExecutionPermId();

    private Collection<AsynchronousOperationExecutor> executors = new ArrayList<AsynchronousOperationExecutor>();

    @SuppressWarnings("unchecked")
    @BeforeMethod
    private void beforeMethod()
    {
        mockery = new Mockery();

        executionConfig = mockery.mock(IOperationExecutionConfig.class);
        executionIdFactory = mockery.mock(IOperationExecutionIdFactory.class);
        executionStore = mockery.mock(IOperationExecutionStore.class);
        poolExecutor = mockery.mock(IAsynchronousOperationThreadPoolExecutor.class);

        context1 = mockery.mock(IOperationContext.class, "context1");
        context2 = mockery.mock(IOperationContext.class, "context2");

        options1 = new AsynchronousOperationExecutionOptions();
        options2 = new AsynchronousOperationExecutionOptions();

        executionId1 = new OperationExecutionPermId();
        executionId2 = new OperationExecutionPermId();

        operations1 = mockery.mock(List.class, "operations1");
        operations2 = mockery.mock(List.class, "operations2");

        operationResults1 = mockery.mock(List.class, "operationResults1");
        operationResults2 = mockery.mock(List.class, "operationResults2");
    }

    @AfterMethod
    private void afterMethod()
    {
        for (AsynchronousOperationExecutor executor : executors)
        {
            executor.shutdown();
        }
        executors.clear();
    }

    @Test
    public void testExecuteWithOperationThatSucceeds()
    {
        final AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();
        final OperationExecutionPermId executionId = new OperationExecutionPermId();
        final MessageChannel channel = createMessageChannel();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId));

                    oneOf(executionStore).executionNew(context1, executionId, operations1, options);
                    oneOf(executionStore).executionScheduled(context1, executionId);
                    oneOf(executionStore).executionRunning(context1, executionId);

                    oneOf(poolExecutor).execute(context1, executionId, operations1, options);
                    will(returnValue(operationResults1));

                    oneOf(executionStore).executionFinished(context1, executionId, operationResults1);
                    will(new SendChannelMessageAction(channel, "executionFinished"));
                }
            });

        executeAndAssertResults(context1, operations1, options, executionId);

        channel.assertNextMessage("executionFinished");
        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithOperationThatFails()
    {
        final TestException exception = new TestException();
        final MessageChannel channel = createMessageChannel();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    oneOf(executionStore).executionScheduled(context1, executionId1);
                    oneOf(executionStore).executionRunning(context1, executionId1);

                    oneOf(poolExecutor).execute(context1, executionId1, operations1, options1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                    will(new SendChannelMessageAction(channel, "executionFailed"));
                }
            });

        executeAndAssertResults(context1, operations1, options1, executionId1);

        channel.assertNextMessage("executionFailed");
        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithExecutionNewThatFails()
    {
        final TestException exception = new TestException();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(context1, operations1, options1, exception);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithExecutionScheduledThatFails()
    {
        final TestException exception = new TestException();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);

                    oneOf(executionStore).executionScheduled(context1, executionId1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                }
            });

        executeAndAssertException(context1, operations1, options1, exception);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithExecutionRunningThatFails()
    {
        final TestException exception = new TestException();
        final MessageChannel channel = createMessageChannel();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    oneOf(executionStore).executionScheduled(context1, executionId1);

                    oneOf(executionStore).executionRunning(context1, executionId1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                    will(new SendChannelMessageAction(channel, "executionFailed"));
                }
            });

        executeAndAssertResults(context1, operations1, options1, executionId1);

        channel.assertNextMessage("executionFailed");
        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithExecutionFinishedThatFails()
    {
        final TestException exception = new TestException();
        final MessageChannel channel = createMessageChannel();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    oneOf(executionStore).executionScheduled(context1, executionId1);
                    oneOf(executionStore).executionRunning(context1, executionId1);

                    oneOf(poolExecutor).execute(context1, executionId1, operations1, options1);
                    will(returnValue(operationResults1));

                    oneOf(executionStore).executionFinished(context1, executionId1, operationResults1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                    will(new SendChannelMessageAction(channel, "executionFailed"));
                }
            });

        executeAndAssertResults(context1, operations1, options1, executionId1);

        channel.assertNextMessage("executionFailed");
        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithExecutionFailedThatFails()
    {
        final TestException exception = new TestException();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultThreadPoolExpectations();

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    will(throwException(exception));

                    oneOf(executionStore).executionFailed(context1, executionId1, new OperationExecutionError(exception));
                    will(throwException(exception));
                }
            });

        executeAndAssertException(context1, operations1, options1, exception);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithThreadPoolExhausted() throws Exception
    {
        final MessageChannel mainChannel = createMessageChannel("mainChannel");
        final MessageChannel executionChannel = createMessageChannel("executionChannel");

        mockery.checking(new TestExpectations()
            {
                {
                    addThreadPoolExpectations(1, 1);

                    // execution 1

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    will(new SendChannelMessageAction(executionChannel, "1_new"));

                    oneOf(executionStore).executionScheduled(context1, executionId1);
                    will(new SendChannelMessageAction(executionChannel, "1_scheduled"));

                    oneOf(executionStore).executionRunning(context1, executionId1);
                    will(new SendChannelMessageAction(executionChannel, "1_running"));

                    oneOf(poolExecutor).execute(context1, executionId1, operations1, options1);
                    will(new TestAction()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                executionChannel.send("1_invoke_started");

                                mainChannel.assertNextMessage("1_invoke_continue");

                                executionChannel.send("1_invoke_finished");

                                return operationResults1;
                            }
                        });

                    oneOf(executionStore).executionFinished(context1, executionId1, operationResults1);
                    will(new SendChannelMessageAction(executionChannel, "1_finished"));

                    // execution 2

                    oneOf(executionIdFactory).createExecutionId(context2);
                    will(returnValue(executionId2));

                    oneOf(executionStore).executionNew(context2, executionId2, operations2, options2);
                    will(new SendChannelMessageAction(executionChannel, "2_new"));

                    oneOf(executionStore).executionScheduled(context2, executionId2);
                    will(new SendChannelMessageAction(executionChannel, "2_scheduled"));

                    oneOf(executionStore).executionRunning(context2, executionId2);
                    will(new SendChannelMessageAction(executionChannel, "2_running"));

                    oneOf(poolExecutor).execute(context2, executionId2, operations2, options2);
                    will(new TestAction()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                executionChannel.send("2_invoke_started");
                                executionChannel.send("2_invoke_finished");
                                return operationResults2;
                            }
                        });

                    oneOf(executionStore).executionFinished(context2, executionId2, operationResults2);
                    will(new SendChannelMessageAction(executionChannel, "2_finished"));
                }
            });

        AsynchronousOperationExecutor executor = createExecutor();

        AsynchronousOperationExecutionResults result = executor.execute(context1, operations1, options1);
        Assert.assertEquals(result.getExecutionId(), executionId1);

        executionChannel.assertNextMessage("1_new");
        executionChannel.assertNextMessage("1_scheduled");
        executionChannel.assertNextMessage("1_running");
        executionChannel.assertNextMessage("1_invoke_started");

        AsynchronousOperationExecutionResults result2 = executor.execute(context2, operations2, options2);
        Assert.assertEquals(result2.getExecutionId(), executionId2);

        executionChannel.assertNextMessage("2_new");
        executionChannel.assertNextMessage("2_scheduled");

        Thread.sleep(100);

        mainChannel.send("1_invoke_continue");

        executionChannel.assertNextMessage("1_invoke_finished");
        executionChannel.assertNextMessage("1_finished");

        executionChannel.assertNextMessage("2_running");
        executionChannel.assertNextMessage("2_invoke_started");
        executionChannel.assertNextMessage("2_invoke_finished");
        executionChannel.assertNextMessage("2_finished");

        mockery.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithThreadPoolNotExhausted() throws Exception
    {
        final MessageChannel mainChannel = createMessageChannel("mainChannel");
        final MessageChannel executionChannel = createMessageChannel("executionChannel");

        mockery.checking(new TestExpectations()
            {
                {
                    addThreadPoolExpectations(2, 2);

                    // execution 1

                    oneOf(executionIdFactory).createExecutionId(context1);
                    will(returnValue(executionId1));

                    oneOf(executionStore).executionNew(context1, executionId1, operations1, options1);
                    will(new SendChannelMessageAction(executionChannel, "1_new"));

                    oneOf(executionStore).executionScheduled(context1, executionId1);
                    will(new SendChannelMessageAction(executionChannel, "1_scheduled"));

                    oneOf(executionStore).executionRunning(context1, executionId1);
                    will(new SendChannelMessageAction(executionChannel, "1_running"));

                    oneOf(poolExecutor).execute(context1, executionId1, operations1, options1);
                    will(new TestAction()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                executionChannel.send("1_invoke_started");

                                mainChannel.assertNextMessage("1_invoke_continue");

                                executionChannel.send("1_invoke_finished");

                                return operationResults1;
                            }
                        });

                    oneOf(executionStore).executionFinished(context1, executionId1, operationResults1);
                    will(new SendChannelMessageAction(executionChannel, "1_finished"));

                    // execution 2

                    oneOf(executionIdFactory).createExecutionId(context2);
                    will(returnValue(executionId2));

                    oneOf(executionStore).executionNew(context2, executionId2, operations2, options2);
                    will(new SendChannelMessageAction(executionChannel, "2_new"));

                    oneOf(executionStore).executionScheduled(context2, executionId2);
                    will(new SendChannelMessageAction(executionChannel, "2_scheduled"));

                    oneOf(executionStore).executionRunning(context2, executionId2);
                    will(new SendChannelMessageAction(executionChannel, "2_running"));

                    oneOf(poolExecutor).execute(context2, executionId2, operations2, options2);
                    will(new TestAction()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                executionChannel.send("2_invoke_started");

                                mainChannel.assertNextMessage("2_invoke_continue");

                                executionChannel.send("2_invoke_finished");
                                return operationResults2;
                            }
                        });

                    oneOf(executionStore).executionFinished(context2, executionId2, operationResults2);
                    will(new SendChannelMessageAction(executionChannel, "2_finished"));
                }
            });

        AsynchronousOperationExecutor executor = createExecutor();

        AsynchronousOperationExecutionResults result = executor.execute(context1, operations1, options1);
        Assert.assertEquals(result.getExecutionId(), executionId1);

        executionChannel.assertNextMessage("1_new");
        executionChannel.assertNextMessage("1_scheduled");
        executionChannel.assertNextMessage("1_running");
        executionChannel.assertNextMessage("1_invoke_started");

        AsynchronousOperationExecutionResults result2 = executor.execute(context2, operations2, options2);
        Assert.assertEquals(result2.getExecutionId(), executionId2);

        executionChannel.assertNextMessage("2_new");
        executionChannel.assertNextMessage("2_scheduled");
        executionChannel.assertNextMessage("2_running");
        executionChannel.assertNextMessage("2_invoke_started");

        Thread.sleep(100);
        mainChannel.send("1_invoke_continue");

        executionChannel.assertNextMessage("1_invoke_finished");
        executionChannel.assertNextMessage("1_finished");

        Thread.sleep(100);
        mainChannel.send("2_invoke_continue");

        executionChannel.assertNextMessage("2_invoke_finished");
        executionChannel.assertNextMessage("2_finished");

        mockery.assertIsSatisfied();
    }

    private MessageChannel createMessageChannel()
    {
        return createMessageChannel("channel");
    }

    private MessageChannel createMessageChannel(String name)
    {
        return new MessageChannelBuilder(1000).name(name).logger(new ConsoleLogger()).getChannel();
    }

    private AsynchronousOperationExecutor createExecutor()
    {
        AsynchronousOperationExecutor executor = new AsynchronousOperationExecutor(executionConfig, executionIdFactory, executionStore, poolExecutor);
        executors.add(executor);
        return executor;
    }

    private void executeAndAssertResults(IOperationContext context, final List<? extends IOperation> operations,
            AsynchronousOperationExecutionOptions options, OperationExecutionPermId executionId)
    {
        AsynchronousOperationExecutionResults result = createExecutor().execute(context, operations, options);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getExecutionId(), executionId);
    }

    private void executeAndAssertException(final IOperationContext context, final List<? extends IOperation> operations,
            final AsynchronousOperationExecutionOptions options, Exception exception)
    {
        try
        {
            createExecutor().execute(context, operations, options);
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e, exception);
        }
    }

    private class TestExpectations extends Expectations
    {

        protected void addThreadPoolExpectations(int threadPoolCoreSize, int threadPoolMaxSize)
        {
            oneOf(executionConfig).getThreadPoolName();
            will(returnValue(UUID.randomUUID().toString()));

            oneOf(executionConfig).getThreadPoolCoreSize();
            will(returnValue(threadPoolCoreSize));

            oneOf(executionConfig).getThreadPoolMaxSize();
            will(returnValue(threadPoolMaxSize));

            oneOf(executionConfig).getThreadPoolKeepAliveTime();
            will(returnValue(0));
        }

        protected void addDefaultThreadPoolExpectations()
        {
            addThreadPoolExpectations(1, 1);
        }

    }

    private class SendChannelMessageAction extends CustomAction
    {

        private MessageChannel channel;

        private String message;

        public SendChannelMessageAction(MessageChannel channel, String message)
        {
            super("sendChannelMessage");
            this.channel = channel;
            this.message = message;
        }

        @Override
        public Object invoke(Invocation invocation) throws Throwable
        {
            channel.send(message);
            return null;
        }

    }

}
