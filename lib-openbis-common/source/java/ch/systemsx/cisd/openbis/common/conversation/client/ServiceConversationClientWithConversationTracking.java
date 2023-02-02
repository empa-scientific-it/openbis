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

package ch.systemsx.cisd.openbis.common.conversation.client;

import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.client.IRemoteServiceConversationServer;
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceConversationClient;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationServerManagerRemote;

/**
 * Service conversation client that keeps track of the started conversations and provides {@link #onConversationStart(IServiceConversation)} and
 * {@link #onConversationClose(IServiceConversation)} methods that can be overwritten to perform some additional actions on these events.
 * 
 * @author pkupczyk
 */
public class ServiceConversationClientWithConversationTracking
{

    private ServiceConversationClient client;

    public ServiceConversationClientWithConversationTracking(String serverUrl, Object clientId,
            int clientTimeout)
    {
        IServiceConversationServerManagerRemote serverManager =
                createServerManager(serverUrl, clientTimeout);
        IRemoteServiceConversationServer server =
                createServer(serverManager, clientId, clientTimeout);

        this.client = new ServiceConversationClient(server, serverManager);
    }

    public IServiceConversation startConversation(Class<?> serviceInterface, String sessionToken)
    {
        TypeId typeId = new TypeId(serviceInterface.getName(), sessionToken);
        IServiceConversation conversation = client.startConversation(typeId.format());
        onConversationStart(conversation);
        return conversation;
    }

    /**
     * Method that is called whenever a new conversation is started.
     */
    public void onConversationStart(IServiceConversation conversation)
    {
        // does nothing by default
    }

    public void receiveMessage(ServiceMessage message)
    {
        client.getIncomingResponseMessageTransport().send(message);
    }

    public void closeConversation(IServiceConversation conversation)
    {
        conversation.close();
        onConversationClose(conversation);
    }

    /**
     * Method that is called whenever a new conversation is closed.
     */
    public void onConversationClose(IServiceConversation conversation)
    {
        // does nothing by default
    }

    private IServiceConversationServerManagerRemote createServerManager(String serverUrl,
            int clientTimeout)
    {
        return HttpInvokerUtils.createServiceStub(IServiceConversationServerManagerRemote.class,
                serverUrl, clientTimeout);
    }

    private IRemoteServiceConversationServer createServer(
            final IServiceConversationServerManagerRemote serverManager, final Object clientId,
            final int clientTimeout)
    {
        return new IRemoteServiceConversationServer()
            {
                @Override
                public ServiceConversationDTO startConversation(String typeIdStr)
                {
                    TypeId typeId = TypeId.parse(typeIdStr);

                    ServiceConversationDTO conversation =
                            serverManager.startConversation(typeId.getSessionToken(),
                                    typeId.getServiceName(), clientId);

                    return new ServiceConversationDTO(conversation.getServiceConversationId(),
                            clientTimeout, conversation.getWorkQueueSize());
                }
            };
    }

    private static class TypeId
    {

        private String serviceName;

        private String sessionToken;

        public TypeId(String serviceName, String sessionToken)
        {
            this.serviceName = serviceName;
            this.sessionToken = sessionToken;
        }

        private String format()
        {
            return serviceName + "," + sessionToken;
        }

        private static TypeId parse(String str)
        {
            String[] parts = str.split(",");
            return new TypeId(parts[0], parts[1]);
        }

        public String getServiceName()
        {
            return serviceName;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

    }

}
