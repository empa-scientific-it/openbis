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

package ch.systemsx.cisd.common.serviceconversation.server;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnexpectedMessagePayloadException;

/**
 * A bidirectional messenger on the server side.
 * 
 * @author Bernd Rinn
 */
class BidirectionalServiceMessenger
{
    private final BlockingQueue<ServiceMessage> incoming =
            new LinkedBlockingQueue<ServiceMessage>();

    private final String conversationId;

    private final IServiceMessageTransport responseMessenger;

    private final int messageReceivingTimeoutMillis;

    private int messageIdxLastSeen = -1;

    private final AtomicBoolean interrupted = new AtomicBoolean();

    private final IInternalServiceMessenger serviceMessenger;

    BidirectionalServiceMessenger(String conversationId, int messageReceivingTimeoutMillis,
            IServiceMessageTransport responseMessenger)
    {
        this.conversationId = conversationId;
        this.messageReceivingTimeoutMillis = messageReceivingTimeoutMillis;
        this.responseMessenger = responseMessenger;
        this.serviceMessenger = createServiceMessenger();
    }

    private IInternalServiceMessenger createServiceMessenger()
    {
        return new IInternalServiceMessenger()
            {
                private int outgoingMessageIdx;

                private int nextOutgoingMessageIndex()
                {
                    return outgoingMessageIdx++;
                }

                @Override
                public <T extends Serializable> T receive(Class<T> messageClass)
                {
                    final T payload =
                            tryReceive(messageClass, messageReceivingTimeoutMillis);
                    if (payload == null)
                    {
                        final String msg = "Timeout while waiting for message from client.";
                        ServiceConversationServer.operationLog.error(String.format(
                                "[id: %s] %s", conversationId, msg));
                        throw new TimeoutExceptionUnchecked(msg);
                    }
                    return payload;
                }

                @Override
                @SuppressWarnings("unchecked")
                public <T extends Serializable> T tryReceive(Class<T> messageClass,
                        int timeoutMillis)
                {
                    if (interrupted.get())
                    {
                        throw new InterruptedExceptionUnchecked();
                    }
                    final Serializable payload;
                    try
                    {
                        final ServiceMessage message =
                                incoming.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                        if (message == null)
                        {
                            return null;
                        }
                        payload = message.getPayload();
                    } catch (InterruptedException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                    if (messageClass != null
                            && messageClass.isAssignableFrom(payload.getClass()) == false)
                    {
                        throw new UnexpectedMessagePayloadException(payload.getClass(),
                                messageClass);
                    }
                    return (T) payload;
                }

                @Override
                public synchronized void send(Serializable message)
                {
                    if (interrupted.get())
                    {
                        throw new InterruptedExceptionUnchecked();
                    }
                    responseMessenger.send(new ServiceMessage(conversationId,
                            nextOutgoingMessageIndex(), false, message));
                }

                @Override
                public synchronized void sendException(String errorMsg)
                {
                    responseMessenger.send(new ServiceMessage(conversationId,
                            nextOutgoingMessageIndex(), true, errorMsg));
                }

                @Override
                public synchronized void sendProgress(ProgressInfo progress)
                {
                    responseMessenger.send(new ServiceMessage(conversationId,
                            nextOutgoingMessageIndex(), progress));
                }

                @Override
                public String getId()
                {
                    return conversationId;
                }
            };
    }

    IInternalServiceMessenger getServiceMessenger()
    {
        return serviceMessenger;
    }

    public void sendToService(ServiceMessage message)
    {
        if (message.getMessageIdx() <= messageIdxLastSeen)
        {
            // Drop duplicate message.
            return;
        } else
        {
            messageIdxLastSeen = message.getMessageIdx();
        }
        incoming.add(message);
    }

    public void markAsInterrupted()
    {
        interrupted.set(true);
    }

    public boolean isMarkedAsInterrupted()
    {
        return interrupted.get();
    }
}