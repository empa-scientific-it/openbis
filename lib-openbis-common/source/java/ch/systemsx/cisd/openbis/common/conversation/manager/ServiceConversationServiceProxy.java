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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientWithConversationTracking;
import ch.systemsx.cisd.openbis.common.conversation.message.ServiceConversationMethodInvocation;

/**
 * Dynamic proxy that converts a given service method calls into appropriate service conversation messages.
 * 
 * @author anttil
 */
class ServiceConversationServiceProxy implements InvocationHandler
{

    private ServiceConversationClientWithConversationTracking client;

    private Class<?> serviceInterface;

    private String sessionToken;

    private ServiceConversationServiceProxy(
            ServiceConversationClientWithConversationTracking client, Class<?> serviceInterface,
            String sessionToken)
    {
        this.client = client;
        this.serviceInterface = serviceInterface;
        this.sessionToken = sessionToken;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        IServiceConversation conversation = null;
        try
        {
            conversation = client.startConversation(serviceInterface, sessionToken);
            conversation.send(new ServiceConversationMethodInvocation(m.getName(), m
                    .getParameterTypes(), args));
            return conversation.receive(Serializable.class);
        } finally
        {
            if (conversation != null)
            {
                client.closeConversation(conversation);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T newInstance(ServiceConversationClientWithConversationTracking client,
            Class<T> serviceInterface, String sessionToken)
    {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]
        { serviceInterface }, new ServiceConversationServiceProxy(client, serviceInterface,
                sessionToken));
    }

}
