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

package ch.systemsx.cisd.openbis.generic.shared.conversation;

import ch.systemsx.cisd.openbis.common.api.client.IServicePinger;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationServerManagerRemote;

/**
 * @author pkupczyk
 */
public class ServiceConversationApplicationServerUrl
{

    private String applicationServerUrl;

    private String clientUrl;

    private String serverUrl;

    public ServiceConversationApplicationServerUrl(String applicationServerUrl)
    {
        this.applicationServerUrl = applicationServerUrl;
    }

    public String getClientUrl(int timeout)
    {
        if (clientUrl == null)
        {
            IServicePinger<IServiceConversationClientManagerRemote> pinger =
                    new IServicePinger<IServiceConversationClientManagerRemote>()
                        {
                            @Override
                            public void ping(IServiceConversationClientManagerRemote service)
                            {
                                service.ping();
                            }
                        };

            ServiceFinder finder =
                    new ServiceFinder("openbis", IServiceConversationClientManagerRemote.PATH);
            clientUrl =
                    finder.createServiceUrl(IServiceConversationClientManagerRemote.class,
                            applicationServerUrl, pinger, timeout);
        }

        return clientUrl;
    }

    public String getServerUrl(int timeout)
    {
        if (serverUrl == null)
        {
            IServicePinger<IServiceConversationServerManagerRemote> pinger =
                    new IServicePinger<IServiceConversationServerManagerRemote>()
                        {
                            @Override
                            public void ping(IServiceConversationServerManagerRemote service)
                            {
                                service.ping();
                            }
                        };
            ServiceFinder finder =
                    new ServiceFinder("openbis", IServiceConversationServerManagerRemote.PATH);
            serverUrl =
                    finder.createServiceUrl(IServiceConversationServerManagerRemote.class,
                            applicationServerUrl, pinger, timeout);
        }

        return serverUrl;
    }

}
