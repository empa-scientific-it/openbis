/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailSender;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService;

import de.schlichtherle.io.FileInputStream;

/**
 * @author Piotr Buczek
 */
public class MailService implements IMailService
{
    static String DEFAULT_SUBJECT = "";

    static String DEFAULT_BODY_TEXT = "";

    private final IEmailSenderService senderService;

    private final String defaultSubject;

    private final String defaultBodyText;

    public MailService(IMailClient mailClient, String recipientAddressOrNull)
    {
        this(mailClient, recipientAddressOrNull, DEFAULT_SUBJECT, DEFAULT_BODY_TEXT);
    }

    public MailService(IMailClient mailClient, String recipientAddressOrNull,
            String defaultSubject, String defaultBodyText)
    {
        this.senderService = createEmailSenderService(mailClient, recipientAddressOrNull);
        this.defaultSubject = defaultSubject;
        this.defaultBodyText = defaultBodyText;
    }

    @Override
    public IEmailSender createEmailSender()
    {
        return new EmailSender(senderService, defaultSubject, defaultBodyText);
    }

    static IEmailSenderService createEmailSenderService(final IMailClient mailClient,
            final String recipientAddressOrNull)
    {
        final EMailAddress recipient = new EMailAddress(recipientAddressOrNull);
        return new IEmailSenderService()
            {

                @Override
                public void trySendEmail(String subject, String bodyText)
                {
                    mailClient.sendEmailMessage(subject, bodyText, null, null, recipient);
                }

                @Override
                public void trySendEmailWithTextAttachment(String subject, String bodyText,
                        String attachmentFileName, String attachmentText) throws IOException
                {
                    DataSource dataSource = new ByteArrayDataSource(attachmentText, "text/plain");
                    mailClient.sendEmailMessageWithAttachment(subject, bodyText,
                            attachmentFileName, new DataHandler(dataSource), null, null, recipient);
                }

                @Override
                public void trySendEmailWithFileAttachment(String subject, String bodyText,
                        String attachmentFileName, String attachmentFilePath)
                        throws FileNotFoundException, IOException
                {
                    DataSource dataSource =
                            new ByteArrayDataSource(new FileInputStream(attachmentFilePath),
                                    "text/plain");
                    mailClient.sendEmailMessageWithAttachment(subject, bodyText,
                            attachmentFileName, new DataHandler(dataSource), null, null, recipient);
                }

            };
    }

    static interface IEmailSenderService
    {
        void trySendEmail(String subject, String bodyText);

        void trySendEmailWithTextAttachment(String subject, String bodyText,
                String attachmentFileName, String attachmentText) throws IOException;

        void trySendEmailWithFileAttachment(String subject, String bodyText,
                String attachmentFileName, String attachmentFilePath) throws FileNotFoundException,
                IOException;
    }

}
