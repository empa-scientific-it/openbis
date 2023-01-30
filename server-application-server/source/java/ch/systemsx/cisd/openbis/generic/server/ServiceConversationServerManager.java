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

import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientDetails;
import ch.systemsx.cisd.openbis.common.conversation.manager.BaseServiceConversationServerManager;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationServerManagerLocal;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationDataStoreClientId;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationDataStoreUrl;

/**
 * The service conversation server manager used by AS.
 * 
 * @author pkupczyk
 */
public class ServiceConversationServerManager extends BaseServiceConversationServerManager
        implements IServiceConversationServerManagerLocal, InitializingBean
{

    private Map<Object, ServiceConversationClientDetails> dataStoreIdToDataStoreDetailsMap =
            new HashMap<Object, ServiceConversationClientDetails>();

    private IServiceForDataStoreServer etlService;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        addService(IServiceForDataStoreServer.class, etlService);
    }

    @Override
    protected ServiceConversationClientDetails getClientDetailsForClientId(Object clientId)
    {
        if (clientId instanceof ServiceConversationDataStoreClientId)
        {
            return dataStoreIdToDataStoreDetailsMap.get(clientId);
        }
        return null;
    }

    @Override
    public void setDataStoreInformation(String dataStoreCode, String dataStoreUrl,
            int dataStoreTimeoutInMinutes)
    {
        String dataStoreClientUrl =
                new ServiceConversationDataStoreUrl(dataStoreUrl).getClientUrl();
        int dataStoreTimeoutInMillis = dataStoreTimeoutInMinutes * 60 * 1000;
        dataStoreIdToDataStoreDetailsMap.put(
                new ServiceConversationDataStoreClientId(dataStoreCode),
                new ServiceConversationClientDetails(dataStoreClientUrl, dataStoreTimeoutInMillis));
    }

    public void setEtlService(IServiceForDataStoreServer etlService)
    {
        ProxyFactory factory = new ProxyFactory(etlService);
        this.etlService = (IServiceForDataStoreServer) factory.getProxy();
    }

}
