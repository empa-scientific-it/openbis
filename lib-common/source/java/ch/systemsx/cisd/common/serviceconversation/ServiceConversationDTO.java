/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;

/**
 * A data transfer object to provide the information about a new service conversation.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String serviceConversationId;

    private final int clientTimeoutInMillis;

    private final int workQueueSize;

    public ServiceConversationDTO(String serviceConversationId, int clientTimeout, int workQueueSize)
    {
        this.serviceConversationId = serviceConversationId;
        this.clientTimeoutInMillis = clientTimeout;
        this.workQueueSize = workQueueSize;
    }

    /**
     * Returns the id of the new service conversation.
     */
    public String getServiceConversationId()
    {
        return serviceConversationId;
    }

    /**
     * Returns the proposed message receiving timeout on the client for this conversation.
     */
    public int getClientTimeoutInMillis()
    {
        return clientTimeoutInMillis;
    }

    /**
     * Returns the length of the workqueue after submitting this conversation.
     * <p>
     * 0 means: The conversation is running.<br>
     * 1 means: The conversation is queued but is the next one to run when another conversation finishes.<br>
     * N > 1 means: The conversation is queued and there are N - 1 conversations to run first.
     */
    public int getWorkQueueSize()
    {
        return workQueueSize;
    }
}
