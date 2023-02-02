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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnexpectedMessagePayloadException;

/**
 * A class that a client can use to receive messages from a service.
 * 
 * @author Bernd Rinn
 */
class ClientMessenger implements IServiceConversation
{
    private final IServiceMessageTransport transportToService;

    private final String serviceConversationId;

    private final ClientResponseMessageQueue responseMessageQueue;

    private final ClientResponseMessageMultiplexer responseMessageMultiplexer;

    private final int serviceMessageTimeoutMillis;

    private final int serverWorkQueueSizeAtStartup;

    private int outgoingMessageIdx;

    private final AtomicBoolean serviceExceptionSignaled = new AtomicBoolean();

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ClientMessenger.class);

    ClientMessenger(ServiceConversationDTO serviceConversationDTO,
            IServiceMessageTransport transportToService,
            ClientResponseMessageQueue responseMessageQueue,
            ClientResponseMessageMultiplexer responseMessageMultiplexer)
    {
        assert transportToService != null;
        this.serviceConversationId = serviceConversationDTO.getServiceConversationId();
        assert serviceConversationId != null;
        this.serviceMessageTimeoutMillis = serviceConversationDTO.getClientTimeoutInMillis();
        this.serverWorkQueueSizeAtStartup = serviceConversationDTO.getWorkQueueSize();
        this.transportToService = transportToService;
        this.responseMessageQueue = responseMessageQueue;
        this.responseMessageMultiplexer = responseMessageMultiplexer;
        responseMessageMultiplexer.addConversation(serviceConversationId,
                new IServiceMessageTransportWithControl()
                    {
                        @Override
                        public void send(ServiceMessage message)
                        {
                            ClientMessenger.this.responseMessageQueue.send(message);
                        }

                        @Override
                        public void sendException(ServiceMessage message)
                        {
                            ClientMessenger.this.serviceExceptionSignaled.set(true);
                            ClientMessenger.this.responseMessageQueue.send(message);
                        }
                    });
    }

    @Override
    public void send(Serializable message)
    {
        checkServiceException();
        synchronized (this)
        {
            transportToService.send(new ServiceMessage(serviceConversationId,
                    nextOutgoingMessageIndex(), false, message));
        }
    }

    private void checkServiceException() throws ServiceExecutionException
    {
        if (serviceExceptionSignaled.getAndSet(false))
        {
            try
            {
                final ServiceMessage messageOrNull = responseMessageQueue.poll(0);
                if (messageOrNull != null && messageOrNull.isException())
                {
                    throw new ServiceExecutionException(messageOrNull.getConversationId(),
                            messageOrNull.tryGetExceptionDescription());
                }
            } catch (InterruptedException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public void terminate()
    {
        synchronized (this)
        {
            transportToService.send(ServiceMessage.terminate(serviceConversationId));
        }
    }

    @Override
    public int getServerWorkQueueSizeAtStartup()
    {
        return serverWorkQueueSizeAtStartup;
    }

    @Override
    public <T extends Serializable> T receive(Class<T> messageClass)
    {
        try
        {
            return handleMessage(getMessage(serviceMessageTimeoutMillis), messageClass, true);
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public <T extends Serializable> T tryReceive(Class<T> messageClass, int timeoutMillis)
    {
        try
        {
            return handleMessage(getMessage(timeoutMillis), messageClass, false);
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private ServiceMessage getMessage(int timeout) throws InterruptedException
    {
        ServiceMessage message = responseMessageQueue.poll(timeout);
        while (message != null && message.getProgress() != null)
        {
            operationLog.debug(message.getProgress());
            message = responseMessageQueue.poll(timeout);
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    private <T> T handleMessage(ServiceMessage message, Class<T> messageClass,
            boolean throwExceptionOnNull)
    {
        if (message == null)
        {
            if (throwExceptionOnNull)
            {
                final TimeoutExceptionUnchecked exception =
                        new TimeoutExceptionUnchecked(
                                "Timeout while waiting on message from service.");
                final String exceptionDescription =
                        ServiceExecutionException.getDescriptionFromException(exception);
                try
                {
                    synchronized (this)
                    {
                        transportToService.send(new ServiceMessage(serviceConversationId,
                                nextOutgoingMessageIndex(), true, exceptionDescription));
                    }
                } catch (RuntimeException ex)
                {
                    operationLog.warn("Sending time out exception failed", ex);
                }
                throw exception;
            } else
            {
                return null;
            }
        }
        if (message.isException())
        {
            throw new ServiceExecutionException(message.getConversationId(),
                    message.tryGetExceptionDescription());
        }

        final Object payload = message.getPayload();
        if (messageClass != null && payload != null
                && messageClass.isAssignableFrom(payload.getClass()) == false)
        {
            throw new UnexpectedMessagePayloadException(payload.getClass(), messageClass);
        }
        return (T) payload;
    }

    private int nextOutgoingMessageIndex()
    {
        return outgoingMessageIdx++;
    }

    @Override
    public String getId()
    {
        return serviceConversationId;
    }

    @Override
    public void close()
    {
        responseMessageMultiplexer.removeConversation(serviceConversationId);
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }
}
