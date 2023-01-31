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

import ch.systemsx.cisd.common.spring.PropertyPlaceholderUtils;
import ch.systemsx.cisd.openbis.common.conversation.manager.BaseServiceConversationClientManager;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationApplicationServerUrl;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationDataStoreClientId;

/**
 * The service conversation client manager used by DSS.
 * 
 * @author pkupczyk
 */
public class ServiceConversationClientManager extends BaseServiceConversationClientManager
        implements IServiceConversationClientManagerLocal
{

    private ServiceConversationDataStoreClientId dataStoreClientId;

    private ServiceConversationApplicationServerUrl applicationServerUrl;

    private int applicationServerTimeoutInMillis;

    @Override
    public IServiceForDataStoreServer getETLService(String sessionToken)
    {
        return getService(applicationServerUrl.getServerUrl(applicationServerTimeoutInMillis),
                IServiceForDataStoreServer.class, sessionToken, dataStoreClientId,
                applicationServerTimeoutInMillis);
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreClientId = new ServiceConversationDataStoreClientId(dataStoreCode);
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
