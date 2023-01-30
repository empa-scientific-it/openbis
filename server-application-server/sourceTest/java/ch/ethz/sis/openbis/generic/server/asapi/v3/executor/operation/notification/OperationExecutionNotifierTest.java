package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionNotification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

import junit.framework.Assert;

public class OperationExecutionNotifierTest
{

    private static final String TEST_CODE = "testCode";

    private static final String TEST_DESCRIPTION = "testDescription";

    private static final List<String> TEST_OPERATIONS = Arrays.asList("testOperation");

    private static final List<String> TEST_RESULTS = Arrays.asList("testResult");

    private static final String TEST_ERROR = "testError";

    private static final String TEST_EMAIL = "testEmail";

    private BufferedAppender logRecorder;

    private Mockery mockery;

    private IOperationExecutionEmailNotifier emailNotifier;

    @BeforeMethod
    private void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);

        mockery = new Mockery();
        emailNotifier = mockery.mock(IOperationExecutionEmailNotifier.class);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
    }

    @Test
    public void testExecutionNewWithSupportedNotification()
    {
        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionNew(TEST_CODE, new OperationExecutionEmailNotification(TEST_EMAIL));
    }

    @Test
    public void testExecutionNewWithUnsupportedNotification()
    {
        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        try
        {
            notifier.executionNew(TEST_CODE, new UnsupportedOperationExecutionNotification());
            Assert.fail();
        } catch (IllegalArgumentException e)
        {
            Assert.assertEquals(
                    "Unsupported notification ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification.OperationExecutionNotifierTest$UnsupportedOperationExecutionNotification found for operation execution with id testCode",
                    e.getMessage());
        }
    }

    @Test
    public void testExecutionFinishedWithSupportedNotification()
    {
        final OperationExecutionEmailNotification notification = new OperationExecutionEmailNotification(TEST_EMAIL);

        mockery.checking(new Expectations()
            {
                {
                    one(emailNotifier).executionFinished(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_RESULTS, notification);
                }
            });

        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFinished(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_RESULTS, notification);
    }

    @Test
    public void testExecutionFinishedWithUnsupportedNotification()
    {
        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFinished(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_RESULTS, new UnsupportedOperationExecutionNotification());

        AssertionUtil.assertContains("Couldn't notify about a finished execution", logRecorder.getLogContent());
        AssertionUtil.assertContains(
                "Unsupported notification ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification.OperationExecutionNotifierTest$UnsupportedOperationExecutionNotification found for operation execution with id testCode",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecutionFinishedWithFailingNotification()
    {
        final OperationExecutionEmailNotification notification = new OperationExecutionEmailNotification(TEST_EMAIL);

        mockery.checking(new Expectations()
            {
                {
                    one(emailNotifier).executionFinished(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_RESULTS, notification);
                    will(throwException(new RuntimeException("Cannot send email")));
                }
            });

        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFinished(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_RESULTS, notification);

        AssertionUtil.assertContains("Couldn't notify about a finished execution", logRecorder.getLogContent());
        AssertionUtil.assertContains("Cannot send email", logRecorder.getLogContent());
    }

    @Test
    public void testExecutionFailedWithSupportedNotification()
    {
        final OperationExecutionEmailNotification notification = new OperationExecutionEmailNotification(TEST_EMAIL);

        mockery.checking(new Expectations()
            {
                {
                    one(emailNotifier).executionFailed(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_ERROR, notification);
                }
            });

        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFailed(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_ERROR, notification);
    }

    @Test
    public void testExecutionFailedWithUnsupportedNotification()
    {
        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFailed(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_ERROR, new UnsupportedOperationExecutionNotification());

        AssertionUtil.assertContains("Couldn't notify about a failed execution", logRecorder.getLogContent());
        AssertionUtil.assertContains(
                "Unsupported notification ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification.OperationExecutionNotifierTest$UnsupportedOperationExecutionNotification found for operation execution with id testCode",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecutionFailedWithFailingNotification()
    {
        final OperationExecutionEmailNotification notification = new OperationExecutionEmailNotification(TEST_EMAIL);

        mockery.checking(new Expectations()
            {
                {
                    one(emailNotifier).executionFailed(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_ERROR, notification);
                    will(throwException(new RuntimeException("Cannot send email")));
                }
            });

        OperationExecutionNotifier notifier = new OperationExecutionNotifier(emailNotifier);
        notifier.executionFailed(TEST_CODE, TEST_DESCRIPTION, TEST_OPERATIONS, TEST_ERROR, notification);

        AssertionUtil.assertContains("Couldn't notify about a failed execution", logRecorder.getLogContent());
        AssertionUtil.assertContains("Cannot send email", logRecorder.getLogContent());
    }

    public static class UnsupportedOperationExecutionNotification implements IOperationExecutionNotification
    {

    }

}
