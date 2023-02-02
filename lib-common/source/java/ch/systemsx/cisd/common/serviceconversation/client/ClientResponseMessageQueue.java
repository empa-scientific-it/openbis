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

package ch.systemsx.cisd.common.serviceconversation.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;

/**
 * A class that holds a queue for response messages to a client.
 * 
 * @author Bernd Rinn
 */
class ClientResponseMessageQueue implements IServiceMessageTransport
{
    private int messageIdxLastSeen = -1;

    private final BlockingQueue<ServiceMessage> messageQueue =
            new LinkedBlockingQueue<ServiceMessage>();

    ServiceMessage poll(int timeoutMillis) throws InterruptedException
    {
        return messageQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void send(ServiceMessage message)
    {
        if (message.getMessageIdx() <= messageIdxLastSeen)
        {
            return;
        } else
        {
            messageIdxLastSeen = message.getMessageIdx();
        }
        if (message.isException())
        {
            messageQueue.clear();
        }
        messageQueue.add(message);
    }
}