package ch.systemsx.cisd.openbis.generic.server;

import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;

public class ConcurrentOperationLimiterTest
{

    @Test
    public void testTwoOperationsWithTheSameLimitBlockEachOther() throws Exception
    {
        MessageChannel mainChannel = new MessageChannelBuilder(1000).name("main").getChannel();
        MessageChannel threadsChannel = new MessageChannelBuilder(1000).name("threads").getChannel();

        ConcurrentOperation<Void> operation1 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    threadsChannel.send("operation-1-start");
                    mainChannel.assertNextMessage("thread-2-start");
                    // sleep for a while to give thread 2 a chance to execute (should not happen as the limit is set to 1)
                    ConcurrencyUtilities.sleep(100);
                    threadsChannel.send("operation-1-finish");
                    return null;
                }
            };

        ConcurrentOperation<Void> operation2 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    threadsChannel.send("operation-2");
                    return null;
                }
            };

        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "1000");
        properties.setProperty("concurrent-operation-limiter.limits", "test-limit");
        properties.setProperty("concurrent-operation-limiter.test-limit.operation", "test");
        properties.setProperty("concurrent-operation-limiter.test-limit.limit", "1");

        ConcurrentOperationLimiter limiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(properties));

        executeLimitedFromNewThread(limiter, "test", operation1);
        threadsChannel.assertNextMessage("operation-1-start");

        executeLimitedFromNewThread(limiter, "test", operation2);
        mainChannel.send("thread-2-start");

        threadsChannel.assertNextMessage("operation-1-finish");
        threadsChannel.assertNextMessage("operation-2");
    }

    @Test
    public void testTwoOperationsWithDifferentLimitsDoNotBlockEachOther() throws Exception
    {
        MessageChannel mainChannel = new MessageChannelBuilder(1000).name("main").getChannel();
        MessageChannel thread1Channel = new MessageChannelBuilder(1000).name("thread1").getChannel();
        MessageChannel thread2Channel = new MessageChannelBuilder(1000).name("thread2").getChannel();

        ConcurrentOperation<Void> operation1 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    thread1Channel.send("operation-1-start");
                    thread2Channel.assertNextMessage("operation-2-start");
                    mainChannel.send("operation-finish");
                    return null;
                }
            };

        ConcurrentOperation<Void> operation2 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    thread1Channel.assertNextMessage("operation-1-start");
                    thread2Channel.send("operation-2-start");
                    mainChannel.send("operation-finish");
                    return null;
                }
            };

        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "1000");
        properties.setProperty("concurrent-operation-limiter.limits", "test-limit-1, test-limit-2");
        properties.setProperty("concurrent-operation-limiter.test-limit-1.operation", "test-1");
        properties.setProperty("concurrent-operation-limiter.test-limit-1.limit", "1");
        properties.setProperty("concurrent-operation-limiter.test-limit-2.operation", "test-2");
        properties.setProperty("concurrent-operation-limiter.test-limit-2.limit", "1");

        ConcurrentOperationLimiter limiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(properties));

        executeLimitedFromNewThread(limiter, "test-1", operation1);
        executeLimitedFromNewThread(limiter, "test-2", operation2);

        mainChannel.assertNextMessage("operation-finish");
        mainChannel.assertNextMessage("operation-finish");
    }

    @Test
    public void testNestedOperationsInTheSameThreadDoNotBlockEachOther() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "1000");
        properties.setProperty("concurrent-operation-limiter.limits", "test-limit");
        properties.setProperty("concurrent-operation-limiter.test-limit.operation", "test");
        properties.setProperty("concurrent-operation-limiter.test-limit.limit", "1");

        ConcurrentOperationLimiter limiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(properties));

        ConcurrentOperation<Void> operationNested = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    return null;
                }
            };

        ConcurrentOperation<Void> operation = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    // make sure an operation won't block itself if it tries to make a nested executeLimited call
                    limiter.executeLimitedWithTimeout("test", operationNested);
                    return null;
                }
            };

        limiter.executeLimitedWithTimeout("test", operation);
    }

    @Test
    public void testOperationsAreExecutedInTheSameOrderAsTheyAppeared() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "1000");
        properties.setProperty("concurrent-operation-limiter.limits", "test-limit");
        properties.setProperty("concurrent-operation-limiter.test-limit.operation", "test");
        properties.setProperty("concurrent-operation-limiter.test-limit.limit", "1");

        MessageChannel mainChannel = new MessageChannelBuilder(1000).name("main").getChannel();
        MessageChannel threadsChannel = new MessageChannelBuilder(1000).name("threads").getChannel();

        ConcurrentOperationLimiter limiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(properties));

        ConcurrentOperation<Void> operation1 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    threadsChannel.send("operation-1-start");
                    mainChannel.assertNextMessage("other-threads-start");
                    // sleep for a while to give other threads a chance to execute (should not happen as the limit is set to 1)
                    ConcurrencyUtilities.sleep(100);
                    return null;
                }
            };

        ConcurrentOperation<Void> operation2 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    threadsChannel.send("operation-2-start");
                    ConcurrencyUtilities.sleep(100);
                    threadsChannel.send("operation-2-finish");
                    return null;
                }
            };

        ConcurrentOperation<Void> operation3 = new ConcurrentOperation<Void>()
            {
                @Override
                public Void execute()
                {
                    threadsChannel.send("operation-3-start");
                    ConcurrencyUtilities.sleep(100);
                    threadsChannel.send("operation-3-finish");
                    return null;
                }
            };

        executeLimitedFromNewThread(limiter, "test", operation1);
        threadsChannel.assertNextMessage("operation-1-start");

        executeLimitedFromNewThread(limiter, "test", operation2);
        executeLimitedFromNewThread(limiter, "test", operation3);
        mainChannel.send("other-threads-start");

        threadsChannel.assertNextMessage("operation-2-start");
        threadsChannel.assertNextMessage("operation-2-finish");
        threadsChannel.assertNextMessage("operation-3-start");
        threadsChannel.assertNextMessage("operation-3-finish");
    }

    private void executeLimitedFromNewThread(ConcurrentOperationLimiter limiter, String operationName, ConcurrentOperation<?> operation)
    {
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    limiter.executeLimitedWithTimeout(operationName, operation);
                }

            }).start();
    }

}
