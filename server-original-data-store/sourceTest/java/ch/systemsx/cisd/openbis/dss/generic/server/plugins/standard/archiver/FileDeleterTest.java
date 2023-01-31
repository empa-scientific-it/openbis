/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.IMailClientProvider;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class FileDeleterTest extends AbstractFileSystemTestCase
{
    private static final String EMAIL_ADDRESS = "admin@a.bc";

    private static final String SUBJECT = "deletion failed";

    private BufferedAppender logRecorder;

    private Mockery context;

    private File deletionRequestDir;

    private File dataFolder;

    private IMailClient mailClient;

    private Properties properties;

    MessageChannel deleterChannel;

    MessageChannel testrunnerChannel;

    private IMailClientProvider mailClientProvider;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        mailClientProvider = new IMailClientProvider()
            {
                @Override
                public IMailClient getMailClient()
                {
                    return mailClient;
                }
            };
        properties = new Properties();
        properties.setProperty(FileDeleter.DELETION_POLLING_TIME_KEY, "1 s");
        properties.setProperty(FileDeleter.DELETION_TIME_OUT_KEY, "5 s");
        properties.setProperty(FileDeleter.EMAIL_SUBJECT_KEY, SUBJECT);
        properties.setProperty(FileDeleter.EMAIL_TEMPLATE_KEY, "The following files couldn't be deleted:\n${"
                + FileDeleter.FILE_LIST_VARIABLE + "}");
        deleterChannel = new MessageChannelBuilder().name("deleter").getChannel();
        testrunnerChannel = new MessageChannelBuilder().name("testrunner").getChannel();

        deletionRequestDir = new File(workingDirectory, "deletion-requests");
        deletionRequestDir.mkdirs();
        dataFolder = new File(workingDirectory, "data-folder");
        dataFolder.mkdirs();
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        if (result.getStatus() == ITestResult.FAILURE)
        {
            fail(result.getName() + " failed. Log content:\n" + logRecorder.getLogContent());
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRequestDeletion() throws InterruptedException
    {
        FileDeleter fileDeleter = create(3, EMAIL_ADDRESS, null);
        File file = new File(dataFolder, "hi.txt");
        FileUtilities.writeToFile(file, "hello");

        fileDeleter.requestDeletion(file);
        fileDeleter.requestDeletion(file);

        List<String> requests = listRequests();
        assertEquals("[19700101-011000_1.deletionrequest, 19700101-011000_2.deletionrequest]",
                requests.toString());
        assertEquals(file.getAbsolutePath(),
                FileUtilities.loadToString(new File(deletionRequestDir, "19700101-011000_1.deletionrequest")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testPromptDeletion() throws InterruptedException
    {
        FileDeleter fileDeleter = create(3, EMAIL_ADDRESS, null);
        File file = new File(dataFolder, "hi.txt");
        FileUtilities.writeToFile(file, "hello");
        fileDeleter.start();
        assertNotNull(getDeleterThreadOrNull());

        deleterChannel.assertNextMessage("2 polls");
        assertEquals(true, file.exists());
        fileDeleter.requestDeletion(file);
        assertEquals("[19700101-011001_1.deletionrequest]", listRequests().toString());
        continueDeleter();

        deleterChannel.assertNextMessage("1 polls");
        assertEquals(false, file.exists());
        continueDeleter();

        deleterChannel.assertNextMessage("0 polls");
        continueDeleter();

        AssertionUtil.assertContainsLines("INFO  OPERATION.FileDeleter - Schedule for deletion: " + file + "\n" +
                "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + file + "': " + deletionRequestDir + "/19700101-011001_1.deletionrequest\n" +
                "INFO  OPERATION.FileDeleter - Successfully deleted: " + file.getAbsolutePath(),
                logRecorder.getLogContent());
        assertThreadFinishedAndEverythingIsEmpty();
    }

    @Test
    public void testDelayedDeletion() throws InterruptedException
    {
        FileDeleter fileDeleter = create(4, EMAIL_ADDRESS, null);
        File subFolder = new File(dataFolder, "subfolder");
        subFolder.mkdirs();
        File file = new File(subFolder, "hi.txt");
        FileUtilities.writeToFile(file, "hello");
        fileDeleter.start();
        assertNotNull(getDeleterThreadOrNull());

        deleterChannel.assertNextMessage("3 polls");
        assertEquals(true, subFolder.exists());
        fileDeleter.requestDeletion(subFolder);
        continueDeleter();

        deleterChannel.assertNextMessage("2 polls");
        // sub folder still exists since delete() fails because sub folder isn't empty
        assertEquals(true, subFolder.exists());
        file.delete(); // empties sub folder to allow delete() to be successful
        continueDeleter();

        deleterChannel.assertNextMessage("1 polls");
        assertEquals(false, file.exists());
        continueDeleter();

        deleterChannel.assertNextMessage("0 polls");
        continueDeleter();

        AssertionUtil.assertContainsLines("INFO  OPERATION.FileDeleter - Schedule for deletion: " + subFolder + "\n" +
                "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + subFolder + "': " + deletionRequestDir + "/19700101-011001_1.deletionrequest\n" +
                "INFO  OPERATION.FileDeleter - Successfully deleted: " + subFolder.getAbsolutePath(),
                logRecorder.getLogContent());
        assertThreadFinishedAndEverythingIsEmpty();
    }

    @Test
    public void testTimeoutDeletion() throws InterruptedException
    {
        FileDeleter fileDeleter = create(20, EMAIL_ADDRESS, null);
        File subFolder1 = new File(dataFolder, "subfolder1");
        subFolder1.mkdirs();
        File file1 = new File(subFolder1, "hi.txt");
        FileUtilities.writeToFile(file1, "hello one");
        File subFolder2 = new File(dataFolder, "subfolder2");
        subFolder2.mkdirs();
        File file2 = new File(subFolder2, "hi.txt");
        FileUtilities.writeToFile(file2, "hello two");
        RecordingMatcher<String> contentRecorder = prepareNotification(EMAIL_ADDRESS, null);
        fileDeleter.start();
        assertNotNull(getDeleterThreadOrNull());

        deleterChannel.assertNextMessage("19 polls");
        fileDeleter.requestDeletion(subFolder1);
        continueDeleter();

        deleterChannel.assertNextMessage("18 polls");
        continueDeleter();

        deleterChannel.assertNextMessage("17 polls");
        assertEquals(true, subFolder1.exists());
        fileDeleter.requestDeletion(subFolder2);
        continueDeleter();

        StringBuilder expectedLogBuilder = new StringBuilder();
        for (int i = 16; i >= 0; i--)
        {
            deleterChannel.assertNextMessage(i + " polls");
            assertEquals(true, subFolder1.exists());
            assertEquals(true, subFolder2.exists());
            continueDeleter();
            if (i < 11)
            {
                expectedLogBuilder.append("WARN  OPERATION.FileDeleter - ").append(subFolder1.getAbsolutePath());
                expectedLogBuilder.append("\n   Deletion requested at 1970-01-01 01:10:01 (");
                expectedLogBuilder.append(18 - i).append("sec elapsed)\n");
                expectedLogBuilder.append("WARN  OPERATION.FileDeleter - ").append(subFolder2.getAbsolutePath());
                expectedLogBuilder.append("\n   Deletion requested at 1970-01-01 01:10:03 (");
                expectedLogBuilder.append(16 - i).append("sec elapsed)\n");
            }
        }

        AssertionUtil.assertContainsLines("INFO  OPERATION.FileDeleter - Schedule for deletion: " + subFolder1 + "\n"
                + "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + subFolder1 + "': " + deletionRequestDir + "/19700101-011001_1.deletionrequest\n"
                + "INFO  OPERATION.FileDeleter - Schedule for deletion: " + subFolder2 + "\n"
                + "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + subFolder2 + "': " + deletionRequestDir + "/19700101-011003_2.deletionrequest\n"
                + "WARN  OPERATION.FileDeleter - " + subFolder1.getAbsolutePath() + "\n"
                + "   Deletion requested at 1970-01-01 01:10:01 (6sec elapsed)\n"
                + "WARN  OPERATION.FileDeleter - " + subFolder1.getAbsolutePath() + "\n"
                + "   Deletion requested at 1970-01-01 01:10:01 (7sec elapsed)\n"
                + expectedLogBuilder.toString().trim(), logRecorder.getLogContent());
        assertEquals("[The following files couldn't be deleted:\n"
                + subFolder1.getAbsolutePath() + "\n   Deletion requested at 1970-01-01 01:10:01 (9sec elapsed)\n"
                + subFolder2.getAbsolutePath() + "\n   Deletion requested at 1970-01-01 01:10:03 (7sec elapsed)\n"
                + ", The following files couldn't be deleted:\n"
                + subFolder1.getAbsolutePath() + "\n   Deletion requested at 1970-01-01 01:10:01 (14sec elapsed)\n"
                + subFolder2.getAbsolutePath() + "\n   Deletion requested at 1970-01-01 01:10:03 (12sec elapsed)\n]",
                contentRecorder.getRecordedObjects().toString());
        assertThreadFinishedAndChannelsAreEmpty();
        assertEquals("[19700101-011001_1.deletionrequest, 19700101-011003_2.deletionrequest]",
                listRequests().toString());
        assertEquals(true, subFolder1.exists());
        assertEquals(true, subFolder2.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testTimeoutDeletionWithoutEmail() throws InterruptedException
    {
        FileDeleter fileDeleter = create(11, null, null);
        File subFolder = new File(dataFolder, "subfolder");
        subFolder.mkdirs();
        File file1 = new File(subFolder, "hi.txt");
        FileUtilities.writeToFile(file1, "hello one");
        fileDeleter.start();
        assertNotNull(getDeleterThreadOrNull());

        deleterChannel.assertNextMessage("10 polls");
        fileDeleter.requestDeletion(subFolder);
        continueDeleter();

        StringBuilder expectedLogBuilder = new StringBuilder();
        for (int i = 9; i >= 0; i--)
        {
            deleterChannel.assertNextMessage(i + " polls");
            assertEquals(true, subFolder.exists());
            continueDeleter();
            if (i < 4)
            {
                expectedLogBuilder.append("WARN  OPERATION.FileDeleter - ").append(subFolder.getAbsolutePath());
                expectedLogBuilder.append("\n   Deletion requested at 1970-01-01 01:10:01 (");
                expectedLogBuilder.append(9 - i).append("sec elapsed)\n");
            }
        }

        AssertionUtil.assertContainsLines("INFO  OPERATION.FileDeleter - Schedule for deletion: " + subFolder + "\n"
                + "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + subFolder + "': " + deletionRequestDir + "/19700101-011001_1.deletionrequest\n"
                + expectedLogBuilder.toString().trim(), logRecorder.getLogContent());
        assertThreadFinishedAndChannelsAreEmpty();
        assertEquals("[19700101-011001_1.deletionrequest]", listRequests().toString());
        assertEquals(true, subFolder.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeletionRequestForNonExistingFile() throws InterruptedException
    {
        FileDeleter fileDeleter = create(2, null, null);
        File file = new File(dataFolder, "non-existend.txt");

        fileDeleter.start();

        deleterChannel.assertNextMessage("1 polls");
        fileDeleter.requestDeletion(file);
        continueDeleter();

        deleterChannel.assertNextMessage("0 polls");
        continueDeleter();

        AssertionUtil.assertContainsLines("INFO  OPERATION.FileDeleter - Schedule for deletion: " + file + "\n" +
                "INFO  OPERATION.FileDeleter - Deletion request file for '"
                + file + "': " + deletionRequestDir + "/19700101-011001_1.deletionrequest\n" +
                "INFO  OPERATION.FileDeleter - Successfully deleted: " + file.getAbsolutePath(),
                logRecorder.getLogContent());
        assertThreadFinishedAndEverythingIsEmpty();
    }

    @Test
    public void testInvalidDeletionRequestFile() throws InterruptedException
    {
        FileDeleter fileDeleter = create(2, null, null);
        FileUtilities.writeToFile(new File(deletionRequestDir, "invalid" + FileDeleter.FILE_TYPE), "hello");
        FileUtilities.writeToFile(new File(deletionRequestDir, "to-be-ignored.txt"), "hi");

        fileDeleter.start();

        deleterChannel.assertNextMessage("1 polls");
        continueDeleter();

        deleterChannel.assertNextMessage("0 polls");
        continueDeleter();

        assertEquals("", logRecorder.getLogContent());
        assertThreadFinishedAndChannelsAreEmpty();
        assertEquals("[invalid.deletionrequest, to-be-ignored.txt]", listRequests().toString());
        context.assertIsSatisfied();
    }

    private void assertThreadFinishedAndEverythingIsEmpty() throws InterruptedException
    {
        assertThreadFinishedAndChannelsAreEmpty();
        assertEquals("[]", listRequests().toString());
        context.assertIsSatisfied();
    }

    private void assertThreadFinishedAndChannelsAreEmpty() throws InterruptedException
    {
        Thread deleterThreadOrNull = getDeleterThreadOrNull();
        if (deleterThreadOrNull != null)
        {
            deleterThreadOrNull.join();
            assertEquals(null, getDeleterThreadOrNull());
        }
        deleterChannel.assertEmpty();
        testrunnerChannel.assertEmpty();
    }

    private Thread getDeleterThreadOrNull()
    {
        Template template = FileDeleter.THREAD_NAME_TEMPLATE.createFreshCopy();
        template.bind("directory", deletionRequestDir.toString());
        String deleterThreadName = template.createText();
        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            if (thread.getName().equals(deleterThreadName))
            {
                return thread;
            }
        }
        return null;
    }

    private void continueDeleter()
    {
        testrunnerChannel.send(TimeProviderWithMessageChannelInteraction.CONTINUE_MESSAGE);
    }

    private RecordingMatcher<String> prepareNotification(
            final String emailAddress, final String emailFromAddressOrNull)
    {
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    allowing(mailClient).sendEmailMessage(with(SUBJECT), with(contentRecorder),
                            with(new EMailAddress(emailAddress)), with(new EMailAddress(emailFromAddressOrNull)),
                            with(new EMailAddress[0]));
                }
            });
        return contentRecorder;
    }

    private List<String> listRequests()
    {
        List<String> requests = Arrays.asList(deletionRequestDir.list());
        Collections.sort(requests);
        return requests;
    }

    private FileDeleter create(int numberOfPolls, String emailAddressOrNull, String emailFromAddressOrNull)
    {
        if (emailAddressOrNull != null)
        {
            properties.setProperty(FileDeleter.EMAIL_ADDRESS_KEY, emailAddressOrNull);
        }
        if (emailFromAddressOrNull != null)
        {
            properties.setProperty(FileDeleter.EMAIL_FROM_ADDRESS_KEY, emailFromAddressOrNull);
        }
        TimeProviderWithMessageChannelInteraction timeProvider =
                new TimeProviderWithMessageChannelInteraction(deleterChannel, testrunnerChannel, numberOfPolls);
        FileDeleter fileDeleter = new FileDeleter(deletionRequestDir, timeProvider, mailClientProvider, properties);
        timeProvider.setDeleter(fileDeleter);
        return fileDeleter;
    }

}
