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

import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationServerManagerRemote;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;

/**
 * @author pkupczyk
 */
public class ServiceConversationDataStoreUrl
{

    private String dataStoreUrl;

    public ServiceConversationDataStoreUrl(String dataStoreUrl)
    {
        this.dataStoreUrl = dataStoreUrl;
    }

    public String getClientUrl()
    {
        return dataStoreUrl + "/" + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME
                + IServiceConversationClientManagerRemote.PATH;
    }

    public String getServerUrl()
    {
        return dataStoreUrl + "/" + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME
                + IServiceConversationServerManagerRemote.PATH;
    }

}
