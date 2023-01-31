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

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.MailService.IEmailSenderService;

/**
 * Tests for {@link EmailSender} class.
 * 
 * @author Piotr Buczek
 */
public class EmailSenderTest extends AssertJUnit
{
    private static final String DEFAULT_SUBJECT = "default subject";

    private static final String DEFAULT_BODY = "default body";

    private Mockery context;

    private IEmailSenderService service;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEmailSenderService.class);
    }

    @AfterMethod
    public void afterTest()
    {
        context.assertIsSatisfied();
    }

    private EmailSender createSender()
    {
        return createSender(DEFAULT_SUBJECT, DEFAULT_BODY);
    }

    private EmailSender createSender(String defaultSubject, String defaultBodyText)
    {
        return new EmailSender(service, defaultSubject, defaultBodyText);
    }

    @Test
    public void testSendEmptyEmail()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).trySendEmail(null, null);
                }
            });
        createSender(null, null).send();
    }

    @Test
    public void testSendDefaultEmail()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).trySendEmail(DEFAULT_SUBJECT, DEFAULT_BODY);
                }
            });
        createSender().send();
        context.assertIsSatisfied();
    }

    @Test
    public void testSendEmailWithSpecifiedSubjectAndBody()
    {
        final String subject = "s";
        final String body = "b";
        context.checking(new Expectations()
            {
                {
                    one(service).trySendEmail(subject, body);
                }
            });
        createSender().withSubject(subject).withBody(body).send();
        context.assertIsSatisfied();
    }

    @Test
    public void testSendEmailWithTextAttachment() throws IOException
    {
        final String attachmentText = "attachment text";
        final String attachmentFileName = "attachment.txt";
        context.checking(new Expectations()
            {
                {
                    one(service).trySendEmailWithTextAttachment(DEFAULT_SUBJECT, DEFAULT_BODY,
                            attachmentFileName, attachmentText);
                }
            });
        createSender().withAttachedText(attachmentText, attachmentFileName).send();
        context.assertIsSatisfied();
    }

    @Test
    public void testSendEmailWithFileAttachment() throws IOException
    {
        final String attachmentFilePath = "attachment/file/path";
        final String attachmentFileName = "attachment.txt";
        context.checking(new Expectations()
            {
                {
                    one(service).trySendEmailWithFileAttachment(DEFAULT_SUBJECT, DEFAULT_BODY,
                            attachmentFileName, attachmentFilePath);
                }
            });
        createSender().withAttachedFile(attachmentFilePath, attachmentFileName).send();
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateEmailFailWithUnspecifiedAttachmentFileName() throws IOException
    {
        final String attachmentText = "attachment text";
        try
        {
            createSender().withAttachedText(attachmentText, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unspecified attachment name.", ex.getMessage());
        }

        final String attachmentFilePath = "attachment/file/path";
        try
        {
            createSender().withAttachedFile(attachmentFilePath, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unspecified attachment name.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateEmailFailWithBothAttachmentFilePathAndTextSpecified() throws IOException
    {
        final String attachmentText = "attachment text";
        final String attachmentFilePath = "attachment/file/path";
        final String attachmentFileName = "attachment.txt";

        try
        {
            createSender().withAttachedText(attachmentText, attachmentFileName).withAttachedFile(
                    attachmentFilePath, attachmentFileName);
            fail("expected IllegalStateException");
        } catch (IllegalStateException ex)
        {
            assertEquals("Attachment text was already set.", ex.getMessage());
        }

        try
        {
            createSender().withAttachedFile(attachmentFilePath, attachmentFileName)
                    .withAttachedText(attachmentText, attachmentFileName);
            fail("expected IllegalStateException");
        } catch (IllegalStateException ex)
        {
            assertEquals("Attachment file path was already set.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

}
