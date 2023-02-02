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

import ch.systemsx.cisd.common.serviceconversation.server.ProgressInfo;

/**
 * A service message which is part of a service conversation.
 * 
 * @author Bernd Rinn
 */
public class ServiceMessage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String conversationId;

    private final int messageIdx;

    private final Serializable payload;

    private final ProgressInfo progress;

    private final String exceptionDescription;

    public static ServiceMessage terminate(String conversationId)
    {
        return new ServiceMessage(conversationId, 0, false, null);
    }

    public ServiceMessage(String conversationId, int messageId, boolean exception,
            Serializable payload)
    {
        this.conversationId = conversationId;
        this.progress = null;
        this.messageIdx = messageId;
        if (exception)
        {
            this.payload = null;
            this.exceptionDescription = payload.toString();
        } else
        {
            this.payload = payload;
            this.exceptionDescription = null;
        }
    }

    public ServiceMessage(String conversationId, int messageId, ProgressInfo progress)
    {
        this.conversationId = conversationId;
        this.progress = progress;
        this.messageIdx = messageId;
        this.payload = null;
        this.exceptionDescription = null;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public int getMessageIdx()
    {
        return messageIdx;
    }

    public Serializable getPayload()
    {
        return payload;
    }

    public boolean isTerminate()
    {
        return (payload == null) && (exceptionDescription == null);
    }

    public boolean isException()
    {
        return (payload == null) && (exceptionDescription != null);
    }

    public boolean hasPayload()
    {
        return (payload != null);
    }

    public String tryGetExceptionDescription()
    {
        return exceptionDescription;
    }

    public ProgressInfo getProgress()
    {
        return this.progress;
    }

    @Override
    public String toString()
    {
        if (isTerminate())
        {
            return "ServiceMessage [conversationId=" + conversationId + ", TERMINATE]";
        } else if (isException())
        {
            return "ServiceMessage [conversationId=" + conversationId + ", messageIdx="
                    + messageIdx + ", exceptionDescription=" + exceptionDescription + "]";
        } else
        {
            return "ServiceMessage [conversationId=" + conversationId + ", messageIdx="
                    + messageIdx + ", payload=" + payload + ", progress=" + progress + "]";
        }
    }
}