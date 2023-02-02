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

import java.io.Serializable;

import ch.systemsx.cisd.common.serviceconversation.IServiceMessenger;
import ch.systemsx.cisd.common.serviceconversation.server.IService;
import ch.systemsx.cisd.common.serviceconversation.server.IServiceFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.openbis.common.conversation.message.ServiceConversationMethodInvocation;

/**
 * A factory for service conversation services. Runs incoming MethodInvocations.
 * 
 * @author anttil
 */
abstract class ServiceConversationServiceFactory implements IServiceFactory
{

    private ServiceConversationServer server;

    private String serviceName;

    private Object service;

    public ServiceConversationServiceFactory(ServiceConversationServer server, String serviceName,
            Object service)
    {
        this.server = server;
        this.serviceName = serviceName;
        this.service = service;
    }

    @Override
    public final String getServiceTypeId()
    {
        return this.serviceName;
    }

    @Override
    public IService create()
    {
        return new IService()
            {

                @Override
                public void run(IServiceMessenger messenger)
                {
                    try
                    {
                        ServiceConversationMethodInvocation call =
                                (ServiceConversationMethodInvocation) messenger
                                        .receive(Serializable.class);

                        Serializable result =
                                call.executeOn(service, server, messenger.getId(),
                                        getProgressInterval(messenger.getId()));

                        messenger.send(result);
                    } finally
                    {
                        onConversationFinish(messenger.getId());
                    }
                }
            };
    }

    @Override
    public int getClientTimeoutMillis()
    {
        // we don't want to suggest any timeout to the client - it should choose the timeout itself
        return -1;
    }

    protected abstract int getProgressInterval(String conversationId);

    protected abstract void onConversationFinish(String conversationId);

    @Override
    public boolean interruptServiceOnClientException()
    {
        return false;
    }

}
