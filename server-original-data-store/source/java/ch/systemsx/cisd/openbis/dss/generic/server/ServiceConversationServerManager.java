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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.spring.PropertyPlaceholderUtils;
import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientDetails;
import ch.systemsx.cisd.openbis.common.conversation.manager.BaseServiceConversationServerManager;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationApplicationServerClientId;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationApplicationServerUrl;

/**
 * The service conversation server manager used by DSS.
 * 
 * @author pkupczyk
 */
public class ServiceConversationServerManager extends BaseServiceConversationServerManager
        implements IServiceConversationServerManagerLocal, InitializingBean
{

    private IDataStoreService dataStoreService;

    private ServiceConversationApplicationServerUrl applicationServerUrl;

    private int applicationServerTimeoutInMillis;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        addService(IDataStoreService.class, dataStoreService);
    }

    @Override
    protected ServiceConversationClientDetails getClientDetailsForClientId(Object clientId)
    {
        if (clientId instanceof ServiceConversationApplicationServerClientId)
        {
            return new ServiceConversationClientDetails(
                    applicationServerUrl.getClientUrl(applicationServerTimeoutInMillis),
                    applicationServerTimeoutInMillis);
        }
        return null;
    }

    public void setDataStoreService(IDataStoreService dataStoreService)
    {
        this.dataStoreService = dataStoreService;
    }

    public void setApplicationServerUrl(String applicationServerUrl)
    {
        this.applicationServerUrl =
                new ServiceConversationApplicationServerUrl(applicationServerUrl);
    }

    public void setApplicationServerTimeout(String applicationServerTimeout)
    {
        this.applicationServerTimeoutInMillis =
                PropertyPlaceholderUtils.getInteger(applicationServerTimeout,
                        ConfigParameters.getDefaultServerTimeoutInMinutes()) * 60 * 1000;
    }

}
