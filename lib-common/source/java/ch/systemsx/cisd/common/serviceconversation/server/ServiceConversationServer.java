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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ITerminableFuture;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.INamedCallable;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.serviceconversation.IServiceMessageTransport;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownClientException;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceTypeException;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;

/**
 * A collection of service conversations.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationServer
{
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ServiceConversationServer.class);

    private final int shutdownTimeoutMillis;

    private final NamingThreadPoolExecutor executor;

    private final Map<String, IServiceFactory> serviceFactoryMap =
            new ConcurrentHashMap<String, IServiceFactory>();

    private final Map<String, IServiceMessageTransport> responseMessageMap =
            new ConcurrentHashMap<String, IServiceMessageTransport>();

    private final ConversationMap conversations = new ConversationMap();

    private final Random rng = new Random();

    private final IServiceMessageTransport incomingTransport = new IServiceMessageTransport()
        {
            @Override
            public void send(ServiceMessage message)
            {
                final String conversationId = message.getConversationId();
                final ServiceConversationRecord record = conversations.get(conversationId);
                if (record == null)
                {
                    if (conversations.recentlySeen(conversationId) == false)
                    {
                        operationLog.error(String.format(
                                "Message for unknown service conversation '%s'", conversationId));
                    }
                    return;
                }
                if (message.hasPayload())
                {
                    record.getMessenger().sendToService(message);
                } else
                {
                    if (serviceShouldBeInterrupted(record, message))
                    {
                        if (message.isException())
                        {
                            operationLog.error(String.format(
                                    "[id: %s] Client execution exception.\n%s", conversationId,
                                    message.tryGetExceptionDescription()));
                        } else
                        {
                            operationLog
                                    .error(String
                                            .format("[id: %s] Client requests termination of service conversation.",
                                                    conversationId));
                        }
                        record.getMessenger().markAsInterrupted();
                        record.getController().cancel(true);
                    }
                }
            }

            private boolean serviceShouldBeInterrupted(final ServiceConversationRecord record,
                    ServiceMessage message)
            {
                if (record.getMessenger().isMarkedAsInterrupted())
                {
                    return false;
                }
                return message.isTerminate() || record.isInterruptServerOnClientException();
            }
        };

    public ServiceConversationServer()
    {
        this(ServiceConversationServerConfig.create());
    }

    public ServiceConversationServer(ServiceConversationServerConfig config)
    {
        this.executor =
                new NamingThreadPoolExecutor("Service Conversations", config.getWorkQueueSize())
                        .corePoolSize(config.getNumberOfCoreThreads()).maximumPoolSize(
                                config.getMaxNumberOfThreads());
        if (config.isDaemonize())
        {
            this.executor.daemonize();
        }
        this.shutdownTimeoutMillis = config.getShutdownTimeoutMillis();
    }

    //
    // Initial setup
    //

    /**
     * Adds a new service type to this conversation object.
     */
    public void addServiceType(IServiceFactory factory)
    {
        final String id = factory.getServiceTypeId();
        if (serviceFactoryMap.containsKey(id))
        {
            throw new IllegalArgumentException("Service type '" + id + "' is already registered.");
        }
        serviceFactoryMap.put(id, factory);
    }

    //
    // Client setup
    //

    /**
     * Adds the client transport (to be called when client connects).
     */
    public void addClientResponseTransport(String clientId,
            IServiceMessageTransport responseTransport)
    {
        responseMessageMap.put(clientId, responseTransport);
    }

    /**
     * Removes the client transport (to be called when client disconnects).
     * 
     * @return <code>true</code> if the client transport was removed.
     */
    public boolean removeClientResponseTransport(String clientId)
    {
        return responseMessageMap.remove(clientId) != null;
    }

    /**
     * Returns the transport for incoming messages from clients.
     */
    public IServiceMessageTransport getIncomingMessageTransport()
    {
        return incomingTransport;
    }

    /**
     * Starts a service conversation of type <var>typeId</var>.
     * 
     * @param typeId The service type of the conversation.
     * @param clientId The id of the client, used to find a suitable transport to communicate back the messages from the service to the client.
     * @param messageReceivingTimeoutMillis The time in milli-seconds that a service conversation method waits for an incoming message from the client
     *            before timing out.
     * @return The information about the service conversation started.
     */
    public ServiceConversationDTO startConversation(final String typeId, final String clientId,
            final int messageReceivingTimeoutMillis)
    {
        final IServiceFactory serviceFactory = serviceFactoryMap.get(typeId);
        if (serviceFactory == null)
        {
            throw new UnknownServiceTypeException(typeId);
        }
        final IServiceMessageTransport responseMessenger = responseMessageMap.get(clientId);
        if (responseMessenger == null)
        {
            final UnknownClientException ex = new UnknownClientException(clientId);
            operationLog.error(ex.getMessage());
            throw ex;
        }
        final IService serviceInstance = serviceFactory.create();
        final String serviceConversationId =
                Long.toString(System.currentTimeMillis()) + "-" + rng.nextInt(Integer.MAX_VALUE);
        final BidirectionalServiceMessenger messenger =
                new BidirectionalServiceMessenger(serviceConversationId,
                        messageReceivingTimeoutMillis, responseMessenger);
        final ServiceConversationRecord record =
                new ServiceConversationRecord(messenger,
                        serviceFactory.interruptServiceOnClientException());
        conversations.put(serviceConversationId, record);
        try
        {
            final ITerminableFuture<Void> controller =
                    ConcurrencyUtilities.submit(executor, new INamedCallable<Void>()
                        {
                            @Override
                            public Void call(IStoppableExecutor<Void> stoppableExecutor)
                                    throws Exception
                            {
                                try
                                {
                                    serviceInstance.run(messenger.getServiceMessenger());
                                } catch (Throwable ex)
                                {
                                    if (ex instanceof InterruptedExceptionUnchecked == false)
                                    {
                                        final String errorMessage =
                                                ServiceExecutionException
                                                        .getDescriptionFromException(ex);
                                        try
                                        {
                                            messenger.getServiceMessenger().sendException(
                                                    errorMessage);
                                        } catch (Exception ex2)
                                        {
                                            operationLog
                                                    .error(
                                                            String.format(
                                                                    "[id: %s] Cannot send message about exception to client.",
                                                                    serviceConversationId), ex2);
                                        }
                                    }
                                } finally
                                {
                                    conversations.remove(serviceConversationId);
                                }
                                return null;
                            }

                            @Override
                            public String getCallableName()
                            {
                                return serviceConversationId + " (" + typeId + ")";
                            }

                        });
            record.setController(controller);
            return new ServiceConversationDTO(serviceConversationId,
                    serviceFactory.getClientTimeoutMillis(), executor.getQueue().size());
        } catch (Throwable th)
        {
            conversations.remove(serviceConversationId);
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        }
    }

    /**
     * Shuts down the server, waiting for ongoing conversations to finish for {@link ServiceConversationServerConfig#getShutdownTimeoutMillis()}
     * milli-seconds.
     * 
     * @return <code>true</code>, if the server was shut down properly.
     */
    public boolean shutdown()
    {
        try
        {
            for (ServiceConversationRecord record : conversations.values())
            {
                record.getController().cancel(true);
            }
            return executor.awaitTermination(shutdownTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    /**
     * Shuts down the server, not waiting for ongoing conversations to finish.
     * 
     * @return <code>true</code>, if the server was shut down properly.
     */
    public boolean shutdownNow()
    {
        try
        {
            for (ServiceConversationRecord record : conversations.values())
            {
                record.getController().cancel(true);
            }
            return executor.awaitTermination(0, TimeUnit.MILLISECONDS);
        } catch (Exception ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    /**
     * Returns <code>true</code> if this server has the given <var>conversationId</var>.
     */
    public boolean hasConversation(String conversationId)
    {
        return conversations.containsKey(conversationId);
    }

    public void reportProgress(String conversationId, ProgressInfo progress)
    {
        if (conversationId == null)
        {
            return;
        }
        if (!hasConversation(conversationId))
        {
            operationLog.warn("Progress reporting failed (" + progress + "). Conversation with id "
                    + conversationId + " does not exist");
            return;
        }

        BidirectionalServiceMessenger messenger =
                this.conversations.get(conversationId).getMessenger();
        messenger.getServiceMessenger().sendProgress(progress);
    }

}
