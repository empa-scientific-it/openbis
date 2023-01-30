/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;

/**
 * A message is a text that is tagged (typed).
 * 
 * @author Christian Ribeaud
 */
public final class Message
{
    @Private
    final static String XML_MESSAGE_TEMPLATE = "<message type=\"%s\">%s</message>";

    @Private
    final static String XML_MESSAGE_WITH_CDATA_TEMPLATE =
            "<message type=\"%s\"><![CDATA[%s]]></message>";

    private final Type type;

    private final String messageText;

    public Message(final Type type, final String messageText)
    {
        assert type != null : "Unspecified type.";
        assert messageText != null : "Unspecified message text.";
        this.type = type;
        this.messageText = messageText;
    }

    public final static Message createInfoMessage(final String messageText)
    {
        return new Message(Type.INFO, messageText);
    }

    public final static Message createWarnMessage(final String messageText)
    {
        return new Message(Type.WARN, messageText);
    }

    public final static Message createErrorMessage(final String messageText)
    {
        return new Message(Type.ERROR, messageText);
    }

    public final String toXml()
    {
        // Characters like "<" and "&" are illegal in XML elements.
        if (StringUtils.containsAny(messageText, "&<"))
        {
            return String.format(XML_MESSAGE_WITH_CDATA_TEMPLATE, type.getLabel(), messageText);
        }
        return String.format(XML_MESSAGE_TEMPLATE, type.getLabel(), messageText);
    }

    //
    // Helper classes
    //

    private static enum Type
    {
        ERROR("error"), WARN("warning"), INFO("info");

        private final String label;

        private Type(final String label)
        {
            this.label = label;
        }

        public final String getLabel()
        {
            return label;
        }

    }
}
