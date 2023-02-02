/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.conversation.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceConversationException;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientDetails;

/**
 * Service conversation server manager that dispatches calls coming from remote service conversation client managers to appropriate local services.
 * One instance of server manager can handle communication with multiple client managers located on different machines. To expose a local service for
 * remote conversational calls you must add the service to the manager using {@link #addService(Class, Object)} method. Moreover the
 * {@link #getClientDetailsForClientId(Object)} method must be implemented to return information which clients are recognized, what URLs should be
 * used for communicating them back and what timeouts should be used for their conversations.
 * 
 * @author pkupczyk
 */
public abstract class BaseServiceConversationServerManager implements
        IServiceConversationServerManagerRemote
{

    private ServiceConversationServer server;

    private Map<ServiceConversationClientDetails, IServiceConversationClientManagerRemote> clientDetailsToClientMap;

    private Map<String, ServiceConversationClientDetails> conversationIdToClientDetailsMap;

    public BaseServiceConversationServerManager()
    {
        server = new ServiceConversationServer();

        clientDetailsToClientMap =
                Collections
                        .synchronizedMap(new HashMap<ServiceConversationClientDetails, IServiceConversationClientManagerRemote>());
        conversationIdToClientDetailsMap =
                Collections
                        .synchronizedMap(new HashMap<String, ServiceConversationClientDetails>());
    }

    /**
     * Method that can be used for registering local services that should be exposed for remote service conversation calls.
     */
    protected void addService(Class<?> serviceInterface, Object service)
    {
        ServiceConversationServiceFactory serviceFactory =
                new ServiceConversationServiceFactory(server, serviceInterface.getName(), service)
                    {
                        @Override
                        protected int getProgressInterval(String conversationId)
                        {
                            ServiceConversationClientDetails clientDetails =
                                    conversationIdToClientDetailsMap.get(conversationId);

                            if (clientDetails == null)
                            {
                                throw new UnknownServiceConversationException(
                                        String.format(
                                                "Tried to report progress for an unknown service conversation '%s'",
                                                conversationId));
                            } else
                            {
                                // try to report progress 10 times within the timeout time,
                                // but keep the interval in range(1ms, 60sec)
                                return Math.min(Math.max(clientDetails.getTimeout() / 10, 1),
                                        60 * 1000);
                            }
                        }

                        @Override
                        protected void onConversationFinish(String conversationId)
                        {
                            conversationIdToClientDetailsMap.remove(conversationId);
                        }
                    };
        server.addServiceType(serviceFactory);
    }

    @Override
    public ServiceConversationDTO startConversation(String sessionToken, String serviceName,
            Object clientId)
    {
        ServiceConversationClientDetails clientDetails = getClientDetailsForClientId(clientId);

        if (clientDetails == null)
        {
            throw new IllegalArgumentException(
                    "Received a service conversation request from an unknown client (" + clientId
                            + ")");
        }

        server.addClientResponseTransport(sessionToken, getClientForClientDetails(clientDetails));
        ServiceConversationDTO conversation =
                server.startConversation(serviceName, sessionToken, clientDetails.getTimeout());
        conversationIdToClientDetailsMap
                .put(conversation.getServiceConversationId(), clientDetails);

        return conversation;
    }

    @Override
    public void send(ServiceMessage message)
    {
        server.getIncomingMessageTransport().send(message);
    }

    @Override
    public void ping()
    {
    }

    public int getConversationCount()
    {
        return conversationIdToClientDetailsMap.size();
    }

    public int getClientCount()
    {
        return clientDetailsToClientMap.size();
    }

    /**
     * Returns detailed information about a client basing on a client id. If it returns null then the client is treated as unknown.
     */
    protected abstract ServiceConversationClientDetails getClientDetailsForClientId(Object clientId);

    private synchronized IServiceConversationClientManagerRemote getClientForClientDetails(
            ServiceConversationClientDetails clientDetails)
    {
        IServiceConversationClientManagerRemote client =
                clientDetailsToClientMap.get(clientDetails);

        if (client == null)
        {
            client =
                    HttpInvokerUtils.createServiceStub(
                            IServiceConversationClientManagerRemote.class, clientDetails.getUrl(),
                            clientDetails.getTimeout());
            clientDetailsToClientMap.put(clientDetails, client);
        }

        return client;
    }

}
