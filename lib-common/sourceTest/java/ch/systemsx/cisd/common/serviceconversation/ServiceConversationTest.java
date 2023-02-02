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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.serviceconversation.client.IRemoteServiceConversationServer;
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceConversationClient;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;
import ch.systemsx.cisd.common.serviceconversation.server.IService;
import ch.systemsx.cisd.common.serviceconversation.server.IServiceFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServerConfig;

/**
 * Test cases for the {@Link ServiceConversationCollection} class.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationTest
{

    private static final int MESSAGE_RECEIVING_TIMEOUT_MILLIS = 500;

    @BeforeTest
    public void init()
    {
        LogInitializer.init();
    }

    private ServiceConversationServerConfig config()
    {
        return ServiceConversationServerConfig.create();
    }

    /**
     * This object encapsulates the client server connection for test purposes.
     */
    private static class TestClientServerConnection implements IRemoteServiceConversationServer,
            IServiceMessageTransport
    {
        private final ServiceConversationServer server;

        TestClientServerConnection(ServiceConversationServer server)
        {
            this.server = server;
        }

        void setResponseMessageTransport(final IServiceMessageTransport responseMessageTransport)
        {
            server.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
                {
                    @Override
                    public void send(ServiceMessage message)
                    {
                        int attempt = 0;
                        while (attempt++ < 10)
                        {
                            try
                            {
                                // Send all messages twice to test detection of duplicate messages.
                                responseMessageTransport.send(message);
                                responseMessageTransport.send(message);
                                break;
                            } catch (Exception ex)
                            {
                                ConcurrencyUtilities.sleep(10);
                            }
                        }
                    }
                });

        }

        @Override
        public void send(ServiceMessage message)
        {
            // Send all messages twice to test detection of duplicate messages.
            server.getIncomingMessageTransport().send(message);
            server.getIncomingMessageTransport().send(message);
        }

        @Override
        public ServiceConversationDTO startConversation(String typeId)
        {
            return server
                    .startConversation(typeId, "dummyClient", MESSAGE_RECEIVING_TIMEOUT_MILLIS);
        }

    }

    private static class ServiceConversationServerAndClientHolder
    {
        final ServiceConversationServer server;

        final ServiceConversationClient client;

        ServiceConversationServerAndClientHolder(ServiceConversationServer server,
                ServiceConversationClient client)
        {
            this.server = server;
            this.client = client;
        }

    }

    private ServiceConversationServerAndClientHolder createServerAndClient(IServiceFactory factory)
    {
        final ServiceConversationServer server = new ServiceConversationServer(config());
        server.addServiceType(factory);
        final TestClientServerConnection dummyRemoteServer = new TestClientServerConnection(server);
        final ServiceConversationClient client =
                new ServiceConversationClient(dummyRemoteServer, dummyRemoteServer);
        dummyRemoteServer.setResponseMessageTransport(client.getIncomingResponseMessageTransport());
        return new ServiceConversationServerAndClientHolder(server, client);
    }

    private ServiceConversationServerAndClientHolder createServerAndClientOneConversationOnly(
            IServiceFactory factory)
    {
        final ServiceConversationServer server =
                new ServiceConversationServer(config().numberOfCoreThreads(1).maxNumberOfThreads(1));
        server.addServiceType(factory);
        final TestClientServerConnection dummyRemoteServer = new TestClientServerConnection(server);
        final ServiceConversationClient client =
                new ServiceConversationClient(dummyRemoteServer, dummyRemoteServer);
        dummyRemoteServer.setResponseMessageTransport(client.getIncomingResponseMessageTransport());
        return new ServiceConversationServerAndClientHolder(server, client);
    }

    private ServiceConversationServerAndClientHolder createServerAndClientOneConcurrentConversation(
            IServiceFactory factory)
    {
        final ServiceConversationServer server =
                new ServiceConversationServer(config().numberOfCoreThreads(1).maxNumberOfThreads(1)
                        .workQueueSize(Integer.MAX_VALUE));
        server.addServiceType(factory);
        final TestClientServerConnection dummyRemoteServer = new TestClientServerConnection(server);
        final ServiceConversationClient client =
                new ServiceConversationClient(dummyRemoteServer, dummyRemoteServer);
        dummyRemoteServer.setResponseMessageTransport(client.getIncomingResponseMessageTransport());
        return new ServiceConversationServerAndClientHolder(server, client);
    }

    private static class SingleEchoService implements IService
    {
        @Override
        public void run(IServiceMessenger messenger)
        {
            messenger.send(messenger.receive(String.class));
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    @Override
                    public IService create()
                    {
                        return new SingleEchoService();
                    }

                    @Override
                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    @Override
                    public String getServiceTypeId()
                    {
                        return "singleEcho";
                    }

                    @Override
                    public boolean interruptServiceOnClientException()
                    {
                        return true;
                    }

                };
        }
    }

    @Test
    public void testSingleEchoServiceHappyCase() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(SingleEchoService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("singleEcho");
        messenger.send("Hallo Echo");
        assertEquals("Hallo Echo", messenger.receive(String.class));
        messenger.close();
    }

    @Test
    public void testSingleEchoServiceHappyCaseuseTryReceive() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(SingleEchoService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("singleEcho");
        messenger.send("Hallo Echo");
        String echo = null;
        int count = 0;
        while (count++ < 10)
        {
            echo = messenger.tryReceive(String.class, 0);
            if (echo != null)
            {
                break;
            }
            ConcurrencyUtilities.sleep(10L);
        }
        System.err.println("Got response after " + count + " attempts.");
        assertEquals("Hallo Echo", echo);
        messenger.close();
    }

    @Test
    public void testMultipleEchoServiceTerminateHappyCase() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClient(EchoService.createFactory(100));
        final IServiceConversation conversation = holder.client.startConversation("echo");

        conversation.send("One");
        assertEquals("One", conversation.receive(String.class));

        conversation.send("Two");
        assertEquals("Two", conversation.receive(String.class));

        conversation.send("Three");
        assertEquals("Three", conversation.receive(String.class));

        conversation.terminate();
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation.getId()));
    }

    @Test
    public void testTwoMultipleEchoServiceTerminateHappyCase() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClient(EchoService.createFactory(100));
        final IServiceConversation conversation1 = holder.client.startConversation("echo");
        final IServiceConversation conversation2 = holder.client.startConversation("echo");

        conversation1.send("One");
        assertEquals("One", conversation1.receive(String.class));

        conversation2.send("AAA");
        assertEquals("AAA", conversation2.receive(String.class));

        conversation1.send("Two");
        assertEquals("Two", conversation1.receive(String.class));

        conversation2.send("BBB");
        assertEquals("BBB", conversation2.receive(String.class));

        conversation1.send("Three");
        assertEquals("Three", conversation1.receive(String.class));

        conversation1.terminate();
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation1.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation1.getId()));
        assertTrue(holder.server.hasConversation(conversation2.getId()));

        conversation2.send("CCC");
        assertEquals("CCC", conversation2.receive(String.class));

        conversation2.terminate();
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation2.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation2.getId()));
    }

    @Test
    public void testTwoMultipleEchoServiceSecondRejected() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClientOneConversationOnly(EchoService.createFactory(100));
        holder.client.startConversation("echo");
        try
        {
            holder.client.startConversation("echo");
            fail("RejectedExecutionException expected.");
        } catch (RejectedExecutionException ex)
        {
        }
    }

    @Test
    public void testTwoMultipleEchoServiceSecondQueued() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClientOneConcurrentConversation(EchoService.createFactory(100));
        final IServiceConversation conversation1 = holder.client.startConversation("echo");
        assertEquals(0, conversation1.getServerWorkQueueSizeAtStartup());
        final IServiceConversation conversation2 = holder.client.startConversation("echo");
        assertEquals(1, conversation2.getServerWorkQueueSizeAtStartup());

        conversation1.send("One");
        assertEquals("One", conversation1.receive(String.class));

        conversation2.send("AAA");
        assertNull(conversation2.tryReceive(String.class, 10));

        conversation1.send("Two");
        assertEquals("Two", conversation1.receive(String.class));

        conversation2.send("BBB");
        assertNull(conversation2.tryReceive(String.class, 10));

        conversation1.send("Three");
        assertEquals("Three", conversation1.receive(String.class));

        conversation1.terminate();
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation1.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation1.getId()));
        assertTrue(holder.server.hasConversation(conversation2.getId()));

        // Get echos of queued messages.
        assertEquals("AAA", conversation2.receive(String.class));
        assertEquals("BBB", conversation2.receive(String.class));

        conversation2.send("CCC");
        assertEquals("CCC", conversation2.receive(String.class));

        conversation2.terminate();
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation2.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation2.getId()));
    }

    @Test
    public void testMultipleEchoServiceTerminateLowLevelHappyCase() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(config());
        conversations.addServiceType(EchoService.createFactory(100));
        final BlockingQueue<ServiceMessage> messageQueue =
                new LinkedBlockingQueue<ServiceMessage>();
        conversations.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
            {
                @Override
                public void send(ServiceMessage message)
                {
                    messageQueue.add(message);
                }
            });
        final String id =
                conversations.startConversation("echo", "dummyClient",
                        MESSAGE_RECEIVING_TIMEOUT_MILLIS).getServiceConversationId();
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;

        conversations.getIncomingMessageTransport().send(new ServiceMessage(id, 0, false, "One"));
        ServiceMessage m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("One", m.getPayload());

        conversations.getIncomingMessageTransport().send(new ServiceMessage(id, 1, false, "Two"));
        // Try to resend and check that the second one is swallowed.
        conversations.getIncomingMessageTransport().send(new ServiceMessage(id, 1, false, "Two"));
        m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("Two", m.getPayload());

        conversations.getIncomingMessageTransport().send(new ServiceMessage(id, 2, false, "Three"));
        m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("Three", m.getPayload());

        conversations.getIncomingMessageTransport().send(ServiceMessage.terminate(id));
        for (int i = 0; i < 100 && conversations.hasConversation(id); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(conversations.hasConversation(id));
    }

    @Test
    public void testEchoServiceTimeout() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClient(EchoService.createFactory(100));
        final IServiceConversation conversation = holder.client.startConversation("echo");
        assertTrue(holder.server.hasConversation(conversation.getId()));

        conversation.send("One");
        assertEquals("One", conversation.receive(String.class));
        conversation.send("Two");

        // Wait for timeout to happen.
        for (int i = 0; i < 100 && holder.server.hasConversation(conversation.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation.getId()));

        try
        {
            conversation.send("Three");
            fail("Server timeout not signaled to client");
        } catch (ServiceExecutionException ex)
        {
            assertTrue(
                    ex.getDescription(),
                    ex.getDescription().startsWith(
                            "ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked:"));
        }
    }

    @Test(expectedExceptions = UnknownServiceTypeException.class)
    public void testUnknownService() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(config());
        conversations.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
            {
                @Override
                public void send(ServiceMessage message)
                {
                }
            });
        conversations.startConversation("echo", "dummyClient", MESSAGE_RECEIVING_TIMEOUT_MILLIS);
    }

    @Test(expectedExceptions = UnknownClientException.class)
    public void testUnknownClient() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(config());
        conversations.addServiceType(new IServiceFactory()
            {
                @Override
                public IService create()
                {
                    return new EchoService();
                }

                @Override
                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                @Override
                public String getServiceTypeId()
                {
                    return "echo";
                }

                @Override
                public boolean interruptServiceOnClientException()
                {
                    return true;
                }
            });
        conversations.startConversation("echo", "dummyClient", MESSAGE_RECEIVING_TIMEOUT_MILLIS);
    }

    private static class ExceptionThrowingService implements IService
    {
        @Override
        public void run(IServiceMessenger messenger)
        {
            throw new RuntimeException("Don't like you!");
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    @Override
                    public IService create()
                    {
                        return new ExceptionThrowingService();
                    }

                    @Override
                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    @Override
                    public String getServiceTypeId()
                    {
                        return "throwException";
                    }

                    @Override
                    public boolean interruptServiceOnClientException()
                    {
                        return true;
                    }

                };
        }
    }

    @Test
    public void testServiceThrowsException() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(ExceptionThrowingService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("throwException");
        try
        {
            messenger.receive(Serializable.class);
            fail("Failed to detect error state on serve-side while receiving.");
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    @Test
    public void testServiceThrowsExceptionOnSend() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(ExceptionThrowingService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("throwException");
        ConcurrencyUtilities.sleep(20L);
        try
        {
            messenger.send("Test");
            fail("Failed to detect error state on serve-side while sending.");
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class EventuallyExceptionThrowingService implements IService
    {
        @Override
        public void run(IServiceMessenger messenger)
        {
            messenger.send("OK1");
            messenger.send("OK2");
            messenger.send("OK3");
            assertNull("Received an unexpected  message",
                    messenger.tryReceive(Serializable.class, 0));
            throw new RuntimeException("Don't like you!");
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    @Override
                    public IService create()
                    {
                        return new EventuallyExceptionThrowingService();
                    }

                    @Override
                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    @Override
                    public String getServiceTypeId()
                    {
                        return "throwException";
                    }

                    @Override
                    public boolean interruptServiceOnClientException()
                    {
                        return true;
                    }

                };
        }
    }

    @Test
    public void testServiceEventuallyThrowsException() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(EventuallyExceptionThrowingService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("throwException");
        ConcurrencyUtilities.sleep(100L);
        try
        {
            // The regular messages should have been cleared by now so that we get to see the
            // exception immediately.
            messenger.receive(Serializable.class);
            fail();
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription(), ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription(), ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class DelayedService implements IService
    {
        @Override
        public void run(IServiceMessenger messenger)
        {
            try
            {
                ConcurrencyUtilities.sleep(100L);
            } catch (InterruptedExceptionUnchecked ex)
            {
                System.err.println("DelayedService got interrupted.");
            }
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    @Override
                    public IService create()
                    {
                        return new DelayedService();
                    }

                    @Override
                    public int getClientTimeoutMillis()
                    {
                        return 50;
                    }

                    @Override
                    public String getServiceTypeId()
                    {
                        return "delayed";
                    }

                    @Override
                    public boolean interruptServiceOnClientException()
                    {
                        return true;
                    }
                };
        }
    }

    @Test
    public void testClientTimeout() throws Exception
    {
        final ServiceConversationClient client =
                createServerAndClient(DelayedService.createFactory()).client;
        final IServiceConversation messenger = client.startConversation("delayed");
        assertNull(messenger.tryReceive(Serializable.class, 0));
        try
        {
            messenger.receive(Serializable.class);
            fail();
        } catch (TimeoutExceptionUnchecked ex)
        {
        }
        // Wait for service to find out that the client timed out.
        ConcurrencyUtilities.sleep(100L);
    }

}
