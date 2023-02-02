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

package ch.systemsx.cisd.common.serviceconversation.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceConversationException;

/**
 * A client-side multiplexer for incoming messages from the server.
 * 
 * @author Bernd Rinn
 */
class ClientResponseMessageMultiplexer
{
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ClientResponseMessageMultiplexer.class);

    private final Map<String, IServiceMessageTransportWithControl> conversations =
            new ConcurrentHashMap<String, IServiceMessageTransportWithControl>();

    /**
     * Returns the transport for incoming response messages.
     */
    IServiceMessageTransport getIncomingTransport()
    {
        return new IServiceMessageTransport()
            {
                @Override
                public void send(ServiceMessage message)
                {
                    final String conversationId = message.getConversationId();
                    final IServiceMessageTransportWithControl transport =
                            conversations.get(conversationId);
                    if (transport == null)
                    {
                        final String msg =
                                String.format("Message for unknown service conversation '%s'",
                                        conversationId);
                        operationLog.error(msg);
                        throw new UnknownServiceConversationException(msg);
                    }
                    if (message.isException())
                    {
                        transport.sendException(message);
                    } else
                    {
                        transport.send(message);
                    }
                }
            };
    }

    /**
     * Adds a new conversation to the multiplexer.
     */
    void addConversation(String serviceConversationId,
            IServiceMessageTransportWithControl responseMessageTransport)
    {
        conversations.put(serviceConversationId, responseMessageTransport);
    }

    /**
     * Removes a conversation from the multiplexer.
     * 
     * @return <code>true</code> if the conversation was removed and <code>false</code>, if a conversation with the given id could not be found.
     */
    boolean removeConversation(String serviceConversationId)
    {
        return conversations.remove(serviceConversationId) != null;
    }

}
