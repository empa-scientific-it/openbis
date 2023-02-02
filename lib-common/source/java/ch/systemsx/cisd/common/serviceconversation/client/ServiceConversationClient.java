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

import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;

/**
 * The client for the service conversations.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationClient
{
    private final IRemoteServiceConversationServer server;

    private final IServiceMessageTransport transportToServer;

    private final ClientResponseMessageMultiplexer responseMessageMultiplexer =
            new ClientResponseMessageMultiplexer();

    public ServiceConversationClient(IRemoteServiceConversationServer server,
            IServiceMessageTransport transportToServer)
    {
        this.server = server;
        this.transportToServer = transportToServer;
    }

    /**
     * Returns the incoming transport for response messages.
     */
    public IServiceMessageTransport getIncomingResponseMessageTransport()
    {
        return responseMessageMultiplexer.getIncomingTransport();
    }

    /**
     * Starts a service conversation of type <var>typeId</var>.
     * 
     * @param typeId The service type of the conversation.
     * @return a {@link IServiceConversation} to communicate with the service.
     */
    public IServiceConversation startConversation(final String typeId)
    {
        final ClientResponseMessageQueue responseMessageQueue = new ClientResponseMessageQueue();
        final ServiceConversationDTO serviceConversationRecord = server.startConversation(typeId);
        final ClientMessenger clientMessenger =
                new ClientMessenger(serviceConversationRecord, transportToServer,
                        responseMessageQueue, responseMessageMultiplexer);
        return clientMessenger;
    }
}
