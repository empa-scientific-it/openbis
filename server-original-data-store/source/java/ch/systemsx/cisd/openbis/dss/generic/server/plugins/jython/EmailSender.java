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

import java.io.IOException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.MailService.IEmailSenderService;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailSender;

/**
 * @author Piotr Buczek
 */
public class EmailSender implements IEmailSender
{
    private final IEmailSenderService service;

    private String subject;

    private String bodyText;

    private String attachmentNameOrNull;

    // it shouldn't be possible to set both attachmentFilePath and attachmentText

    private String attachmentFilePathOrNull;

    private String attachmentTextOrNull;

    public EmailSender(IEmailSenderService service, String defaultSubject, String defaultBodyText)
    {
        this.service = service;
        this.subject = defaultSubject;
        this.bodyText = defaultBodyText;
    }

    // builder

    @Override
    public IEmailSender withSubject(String aSubject)
    {
        this.subject = aSubject;
        return this;
    }

    @Override
    public IEmailSender withBody(String aBodyText)
    {
        this.bodyText = aBodyText;
        return this;
    }

    @Override
    public IEmailSender withAttachedFile(String filePath, String attachmentName)
    {
        if (attachmentName == null)
        {
            throw new IllegalArgumentException("Unspecified attachment name.");
        }
        if (attachmentTextOrNull != null)
        {
            throw new IllegalStateException("Attachment text was already set.");
        }
        this.attachmentFilePathOrNull = filePath;
        this.attachmentNameOrNull = attachmentName;
        return this;
    }

    @Override
    public IEmailSender withAttachedText(String text, String attachmentName)
    {
        if (attachmentName == null)
        {
            throw new IllegalArgumentException("Unspecified attachment name.");
        }
        if (attachmentFilePathOrNull != null)
        {
            throw new IllegalStateException("Attachment file path was already set.");
        }
        this.attachmentTextOrNull = text;
        this.attachmentNameOrNull = attachmentName;
        return this;
    }

    @Override
    public String toString()
    {
        return "EmailSender [subject=" + subject + ", bodyText=" + bodyText
                + ", attachmentNameOrNull=" + attachmentNameOrNull + ", attachmentFilePathOrNull="
                + attachmentFilePathOrNull + ", attachmentTextOrNull=" + attachmentTextOrNull + "]";
    }

    // sender

    @Override
    public void send()
    {
        try
        {
            if (attachmentNameOrNull == null)
            {
                service.trySendEmail(subject, bodyText);
            } else
            {
                if (attachmentFilePathOrNull != null)
                {
                    service.trySendEmailWithFileAttachment(subject, bodyText, attachmentNameOrNull,
                            attachmentFilePathOrNull);
                } else if (attachmentTextOrNull != null)
                {
                    service.trySendEmailWithTextAttachment(subject, bodyText, attachmentNameOrNull,
                            attachmentTextOrNull);
                } else
                {
                    // in general it shouldn't happen unless the script put None as an argument
                    throw new IllegalStateException(
                            "Neither file path nor text of attachment was specified");
                }
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
