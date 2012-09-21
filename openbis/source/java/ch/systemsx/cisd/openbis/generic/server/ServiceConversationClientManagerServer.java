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

package ch.systemsx.cisd.openbis.generic.server;

import javax.annotation.Resource;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * @author pkupczyk
 */

@Controller
@RequestMapping(
    { IServiceConversationClientManagerRemote.PATH, "/openbis" + IServiceConversationClientManagerRemote.PATH })
public class ServiceConversationClientManagerServer extends HttpInvokerServiceExporter
{
    @Resource(name = ResourceNames.SERVICE_CONVERSATION_CLIENT_MANAGER)
    private IServiceConversationClientManagerRemote clientManager;

    @Override
    public void afterPropertiesSet()
    {
        setServiceInterface(IServiceConversationClientManagerRemote.class);
        setService(clientManager);
        super.afterPropertiesSet();
    }

}
