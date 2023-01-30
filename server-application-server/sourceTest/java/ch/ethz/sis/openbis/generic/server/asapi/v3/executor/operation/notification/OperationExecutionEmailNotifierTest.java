package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification;

import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.common.utilities.TestResources;

public class OperationExecutionEmailNotifierTest
{

    private final static String TEST_CODE = "testCode";

    private final static String TEST_DESCRIPTION = "testDescription";

    private final static String TEST_EMAIL_1 = "test1@email.com";

    private final static String TEST_EMAIL_2 = "test2@email.com";

    private final static String TEST_OPERATION_1 = "testOperation1";

    private final static String TEST_OPERATION_2 = "testOperation2";

    private final static String TEST_RESULT_1 = "testResult1";

    private final static String TEST_RESULT_2 = "testResult2";

    private final static String TEST_ERROR = "testError";

    private TestResources resources = new TestResources(getClass());

    private TestMailClient mailClient;

    private OperationExecutionEmailNotifier notifier;

    @BeforeMethod
    private void beforeMethod()
    {
        mailClient = new TestMailClient();
        notifier = new OperationExecutionEmailNotifier(mailClient);
    }

    @Test
    public void testExecutionFinishedWithoutOperationsAndResults()
    {
        executionFinished(null, null);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithOneOperationAndResult()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1), Arrays.asList(TEST_RESULT_1));
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithOneOperationAndWithoutResults()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1), null);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithoutOperationsAndWithOneResult()
    {
        executionFinished(null, Arrays.asList(TEST_RESULT_1));
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithManyOperationsAndResults()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), Arrays.asList(TEST_RESULT_1, TEST_RESULT_2));
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithManyOperationsAndOneResult()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), Arrays.asList(TEST_RESULT_1));
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithOneOperationAndManyResults()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1), Arrays.asList(TEST_RESULT_1, TEST_RESULT_2));
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithManyOperationsAndWithDescription()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), TEST_ERROR, TEST_DESCRIPTION);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithManyOperationsAndWithoutDescription()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), TEST_ERROR, null);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithoutOperationsAndError()
    {
        executionFailed(null, TEST_ERROR);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithoutOperationsAndWithoutError()
    {
        executionFailed(null, null);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithOneOperationAndWithDescription()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1), TEST_ERROR, TEST_DESCRIPTION);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithOneOperationAndWithoutDescription()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1), TEST_ERROR, null);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithOneOperationAndError()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1), TEST_ERROR);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithOneOperationAndWithoutError()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1), null);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithManyOperationsAndError()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), TEST_ERROR);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFailedWithManyOperationsAndWithoutError()
    {
        executionFailed(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), null);
        assertFailedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithManyOperationsAndWithDescription()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), Arrays.asList(TEST_RESULT_1, TEST_RESULT_2), TEST_DESCRIPTION);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithManyOperationsAndWithoutDescription()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1, TEST_OPERATION_2), Arrays.asList(TEST_RESULT_1, TEST_RESULT_2), null);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithOneOperationAndWithDescription()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1), Arrays.asList(TEST_RESULT_1), TEST_DESCRIPTION);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    @Test
    public void testExecutionFinishedWithOneOperationAndWithoutDescription()
    {
        executionFinished(Arrays.asList(TEST_OPERATION_1), Arrays.asList(TEST_RESULT_1), null);
        assertFinishedEmail(MethodUtils.getCurrentMethod().getName());
    }

    private void executionFinished(List<String> operations, List<String> results)
    {
        executionFinished(operations, results, TEST_DESCRIPTION);
    }

    private void executionFinished(List<String> operations, List<String> results, String description)
    {
        notifier.executionFinished(TEST_CODE, description, operations, results,
                new OperationExecutionEmailNotification(TEST_EMAIL_1, TEST_EMAIL_2));
    }

    private void executionFailed(List<String> operations, String error)
    {
        executionFailed(operations, error, TEST_DESCRIPTION);
    }

    private void executionFailed(List<String> operations, String error, String description)
    {
        notifier.executionFailed(TEST_CODE, description, operations, error, new OperationExecutionEmailNotification(TEST_EMAIL_1, TEST_EMAIL_2));
    }

    private void assertFinishedEmail(String expectedContentFileName)
    {
        assertEmail("Operation execution " + TEST_CODE + " finished", expectedContentFileName);
    }

    private void assertFailedEmail(String expectedContentFileName)
    {
        assertEmail("Operation execution " + TEST_CODE + " failed", expectedContentFileName);
    }

    private void assertEmail(String expectedSubject, String expectedContentFileName)
    {
        Assert.assertEquals(mailClient.actualSubject, expectedSubject);
        Assert.assertEquals(mailClient.actualContent,
                FileUtilities.loadExactToString(resources.getResourceFile(expectedContentFileName)));
        Assert.assertEquals(mailClient.actualReply, null);
        Assert.assertEquals(mailClient.actualFrom, null);

        Assert.assertEquals(mailClient.actualRecipients, new EMailAddress[] { new EMailAddress(TEST_EMAIL_1), new EMailAddress(TEST_EMAIL_2) });
    }

    private class TestMailClient implements IMailClient
    {

        private String actualSubject;

        private String actualContent;

        private EMailAddress actualReply;

        private EMailAddress actualFrom;

        private EMailAddress[] actualRecipients;

        @Override
        public void sendMessage(String subject, String content, String replyToOrNull, From fromOrNull, String... recipients)
                throws EnvironmentFailureException
        {
            Assert.fail();
        }

        @Override
        public void sendEmailMessage(String subject, String content, EMailAddress replyToOrNull, EMailAddress fromOrNull, EMailAddress... recipients)
                throws EnvironmentFailureException
        {
            this.actualSubject = subject;
            this.actualContent = content;
            this.actualReply = replyToOrNull;
            this.actualFrom = fromOrNull;
            this.actualRecipients = recipients;
        }

        @Override
        public void sendMessageWithAttachment(String subject, String content, String filename, DataHandler attachmentContent, String replyToOrNull,
                From fromOrNull, String... recipients) throws EnvironmentFailureException
        {
            Assert.fail();
        }

        @Override
        public void sendEmailMessageWithAttachment(String subject, String content, String filename, DataHandler attachmentContent,
                EMailAddress replyToOrNull, EMailAddress fromOrNull, EMailAddress... recipients) throws EnvironmentFailureException
        {
            Assert.fail();
        }

        @Override
        public void sendTestEmail()
        {
            Assert.fail();
        }
    }

}
