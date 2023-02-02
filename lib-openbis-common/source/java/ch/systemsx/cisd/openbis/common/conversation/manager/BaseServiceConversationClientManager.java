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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientWithConversationTracking;

/**
 * Service conversation client manager that dispatches calls to remote service conversation servers managers. One instance of client manager can
 * handle communication with multiple server managers located on different machines. To make a conversational call to a remote service you must first
 * obtain a reference to a service that supports service conversation communication using {@link #getService(String, Class, String, Object, int)}
 * method. All the method calls on that service will be automatically translated into appropriate service conversation messages behind the scenes.
 * 
 * @author pkupczyk
 */
public class BaseServiceConversationClientManager implements
        IServiceConversationClientManagerRemote
{

    private Map<ClientConfig, ServiceConversationClientWithConversationTracking> clientConfigToClientMap;

    private Map<String, ServiceConversationClientWithConversationTracking> conversationIdToClientMap;

    public BaseServiceConversationClientManager()
    {
        clientConfigToClientMap =
                Collections
                        .synchronizedMap(new HashMap<ClientConfig, ServiceConversationClientWithConversationTracking>());
        conversationIdToClientMap =
                Collections
                        .synchronizedMap(new HashMap<String, ServiceConversationClientWithConversationTracking>());
    }

    @Override
    public void send(ServiceMessage message)
    {
        ServiceConversationClientWithConversationTracking client =
                getClientForConversationId(message.getConversationId());

        if (client != null)
        {
            client.receiveMessage(message);
        }
    }

    @Override
    public void ping()
    {
    }

    /**
     * Method that returns a reference to a service that supports service conversation communication.
     * 
     * @param serverUrl The URL of the service conversation server manager where the given service has been registered.
     * @param serviceInterface The interface of the service to be returned.
     * @param sessionToken The session token that will be used to uniquely identify the conversations.
     * @param clientId The id of the client manager that has to be recognized by the server. Basing on this id the server will decide where to send
     *            the responses to and what timeout to use.
     * @param clientTimeout The timeout of the client manager.
     */
    public <T> T getService(String serverUrl, Class<T> serviceInterface, String sessionToken,
            Object clientId, int clientTimeout)
    {
        return ServiceConversationServiceProxy.newInstance(
                getClientForServerUrl(serverUrl, clientId, clientTimeout), serviceInterface,
                sessionToken);
    }

    public int getConversationCount()
    {
        return conversationIdToClientMap.size();
    }

    public int getClientCount()
    {
        return clientConfigToClientMap.size();
    }

    private synchronized ServiceConversationClientWithConversationTracking getClientForServerUrl(
            String serverUrl, Object clientId, int clientTimeout)
    {
        ClientConfig clientConfig = new ClientConfig(serverUrl, clientId, clientTimeout);

        ServiceConversationClientWithConversationTracking client =
                clientConfigToClientMap.get(clientConfig);

        if (client == null)
        {
            client =
                    new ServiceConversationClientWithConversationTracking(serverUrl, clientId,
                            clientTimeout)
                        {
                            @Override
                            public void onConversationStart(IServiceConversation conversation)
                            {
                                conversationIdToClientMap.put(conversation.getId(), this);
                            }

                            @Override
                            public void onConversationClose(IServiceConversation conversation)
                            {
                                conversationIdToClientMap.remove(conversation.getId());
                            }
                        };
            clientConfigToClientMap.put(clientConfig, client);
        }

        return client;
    }

    private ServiceConversationClientWithConversationTracking getClientForConversationId(
            String conversationId)
    {
        return conversationIdToClientMap.get(conversationId);
    }

    private static class ClientConfig
    {
        private String serverUrl;

        private Object clientId;

        private int clientTimeout;

        public ClientConfig(String serverUrl, Object clientId, int clientTimeout)
        {
            this.serverUrl = serverUrl;
            this.clientId = clientId;
            this.clientTimeout = clientTimeout;
        }

        @Override
        public int hashCode()
        {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(serverUrl);
            builder.append(clientId);
            builder.append(clientTimeout);
            return builder.toHashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (obj.getClass() != getClass())
                return false;

            ClientConfig that = (ClientConfig) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(serverUrl, that.serverUrl);
            builder.append(clientId, that.clientId);
            builder.append(clientTimeout, that.clientTimeout);
            return builder.isEquals();
        }

    }

}
