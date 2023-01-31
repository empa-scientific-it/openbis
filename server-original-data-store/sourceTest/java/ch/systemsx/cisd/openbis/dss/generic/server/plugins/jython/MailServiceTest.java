/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.MailService.IEmailSenderService;

/**
 * Tests for {@link MailService} class.
 * 
 * @author Piotr Buczek
 */
public class MailServiceTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE_RECIPIENT = "example.recipient@bsse.ethz.ch";

    private static final String EXAMPLE_SUBJECT = "example subject";

    private static final String EXAMPLE_BODY = "example body";

    private Mockery context;

    private IMailClient mailClient;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
    }

    @AfterMethod
    public void afterTest()
    {
        context.assertIsSatisfied();
    }

    private IEmailSenderService createEmailSenderService(String recipientAddress)
    {
        return MailService.createEmailSenderService(mailClient, recipientAddress);
    }

    private IEmailSenderService createEmailSenderService()
    {
        return createEmailSenderService(EXAMPLE_RECIPIENT);
    }

    @Test
    public void testSendEmailWithoutAttachments()
    {
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessage(EXAMPLE_SUBJECT, EXAMPLE_BODY, null, null,
                            new EMailAddress(EXAMPLE_RECIPIENT));
                }
            });
        createEmailSenderService().trySendEmail(EXAMPLE_SUBJECT, EXAMPLE_BODY);
    }

    @Test
    public void testSendEmailWithTextAttachment() throws IOException
    {
        final String attachmentText = "attachment\ntext";
        final String attachmentFileName = "attachment.txt";

        final RecordingMatcher<String> subjectMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> fileNameMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> recipientsMatcher =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessageWithAttachment(with(subjectMatcher),
                            with(contentMatcher), with(fileNameMatcher), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsMatcher));
                }
            });
        createEmailSenderService().trySendEmailWithTextAttachment(EXAMPLE_SUBJECT, EXAMPLE_BODY,
                attachmentFileName, attachmentText);

        assertEquals(EXAMPLE_SUBJECT, subjectMatcher.recordedObject());
        assertEquals(EXAMPLE_BODY, contentMatcher.recordedObject());
        assertEquals(1, recipientsMatcher.recordedObject().length);
        assertEquals(EXAMPLE_RECIPIENT, recipientsMatcher.recordedObject()[0].tryGetEmailAddress());
        @SuppressWarnings("unchecked")
        List<String> fileLines =
                IOUtils.readLines(attachmentMatcher.recordedObject().getInputStream());
        assertEquals("[attachment, text]", fileLines.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSendEmailWithFileAttachment() throws IOException
    {
        final String attachmentText = "file attachment\ntext";
        final File attachment = new File(workingDirectory, "a.txt");
        FileUtilities.writeToFile(attachment, attachmentText);

        final String attachmentFilePath = attachment.getPath();
        final String attachmentFileName = "attachment.txt";

        final RecordingMatcher<String> subjectMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> fileNameMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> recipientsMatcher =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessageWithAttachment(with(subjectMatcher),
                            with(contentMatcher), with(fileNameMatcher), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsMatcher));
                }
            });
        createEmailSenderService().trySendEmailWithFileAttachment(EXAMPLE_SUBJECT, EXAMPLE_BODY,
                attachmentFileName, attachmentFilePath);

        assertEquals(EXAMPLE_SUBJECT, subjectMatcher.recordedObject());
        assertEquals(EXAMPLE_BODY, contentMatcher.recordedObject());
        assertEquals(1, recipientsMatcher.recordedObject().length);
        assertEquals(EXAMPLE_RECIPIENT, recipientsMatcher.recordedObject()[0].tryGetEmailAddress());
        @SuppressWarnings("unchecked")
        List<String> fileLines =
                IOUtils.readLines(attachmentMatcher.recordedObject().getInputStream());
        assertEquals("[file attachment, text]", fileLines.toString());
        context.assertIsSatisfied();
    }
}
