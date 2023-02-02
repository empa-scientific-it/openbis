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

/**
 * The <i>JavaMail API</i> supports the following standard properties.
 * 
 * @author Christian Ribeaud
 */
public final class JavaMailProperties
{

    private JavaMailProperties()
    {
        // Can not be instantiated.
    }

    /** Specifies the default message access protocol. */
    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    /** The initial debug mode. */
    public static final String MAIL_DEBUG = "mail.debug";

    /** The return email address of the current user. */
    public static final String MAIL_FROM = "mail.from";

    /** The host name of the mail server for Transports. */
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";

    /** The host port of the mail server for Transports. */
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /** The user name to use when connecting to the mail server. */
    public static final String MAIL_SMTP_USER = "mail.smtp.user";

    /** Whether authentication is needed when connecting to the mail server. */
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

}
