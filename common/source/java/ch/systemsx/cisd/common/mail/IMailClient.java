/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.mail;

import javax.activation.DataHandler;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Implementations of this interface are able to send email.
 * 
 * @author Franz-Josef Elmer
 */
public interface IMailClient
{

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>.
     * 
     * @param replyToOrNull reply-to part of the email header. Can be <code>null</code>.
     * @param fromOrNull from part of the email header. Can be <code>null</code>. If specified -
     *            will overwrite the 'from' value specified for the client.
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    public void sendMessage(String subject, String content, String replyToOrNull, From fromOrNull,
            String... recipients) throws EnvironmentFailureException;

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>, includig the given <var>attachment</var>
     * 
     * @param replyToOrNull reply-to part of the email header. Can be <code>null</code>.
     * @param fromOrNull from part of the email header. Can be <code>null</code>. If specified -
     *            will overwrite the 'from' value specified for the client.
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    public void sendMessageWithAttachment(final String subject, final String content,
            final String filename, final DataHandler attachmentContent, final String replyToOrNull,
            final From fromOrNull, final String... recipients) throws EnvironmentFailureException;

}