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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

/**
 * Test cases for the {@Link ServiceConversationCollection} class.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationTest
{
    @BeforeTest
    public void init()
    {
        LogInitializer.init();
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

        public void send(ServiceMessage message)
        {
            // Send all messages twice to test detection of duplicate messages.
            server.getIncomingMessageTransport().send(message);
            server.getIncomingMessageTransport().send(message);
        }

        public ServiceConversationDTO startConversation(String typeId)
        {
            return server.startConversation(typeId, "dummyClient");
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
        final ServiceConversationServer server = new ServiceConversationServer(100);
        server.addServiceType(factory);
        final TestClientServerConnection dummyRemoteServer = new TestClientServerConnection(server);
        final ServiceConversationClient client =
                new ServiceConversationClient(dummyRemoteServer, dummyRemoteServer);
        dummyRemoteServer.setResponseMessageTransport(client.getIncomingResponseMessageTransport());
        return new ServiceConversationServerAndClientHolder(server, client);
    }

    private static class SingleEchoService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            messenger.send(messenger.receive(String.class));
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    public IService create()
                    {
                        return new SingleEchoService();
                    }

                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    public String getServiceTypeId()
                    {
                        return "singleEcho";
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

    private static class EchoService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            try
            {
                System.err.println(Thread.currentThread().getName());
                while (true)
                {
                    messenger.send(messenger.receive(String.class));
                }
            } catch (RuntimeException ex)
            {
                // Show exception
                ex.printStackTrace();
                // This doesn't matter: the exception goes into the void.
                throw ex;
            }
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    public IService create()
                    {
                        return new EchoService();
                    }

                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    public String getServiceTypeId()
                    {
                        return "echo";
                    }
                };
        }
    }

    @Test
    public void testMultipleEchoServiceTerminateHappyCase() throws Exception
    {
        final ServiceConversationServerAndClientHolder holder =
                createServerAndClient(EchoService.createFactory());
        final IServiceConversation conversation = holder.client.startConversation("echo");

        conversation.send("One");
        assertEquals("One", conversation.receive(String.class));

        conversation.send("Two");
        assertEquals("Two", conversation.receive(String.class));

        conversation.send("Three");
        assertEquals("Three", conversation.receive(String.class));

        conversation.terminate();
        for (int i = 0; i < 100
                && holder.server.hasConversation(conversation.getId()); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(holder.server.hasConversation(conversation.getId()));
    }

    @Test
    public void testMultipleEchoServiceTerminateLowLevelHappyCase() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(100);
        conversations.addServiceType(EchoService.createFactory());
        final BlockingQueue<ServiceMessage> messageQueue =
                new LinkedBlockingQueue<ServiceMessage>();
        conversations.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
            {
                public void send(ServiceMessage message)
                {
                    messageQueue.add(message);
                }
            });
        final String id =
                conversations.startConversation("echo", "dummyClient").getServiceConversationId();
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
        final ServiceConversationServer conversations = new ServiceConversationServer(100);
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "echo";
                }
            });
        final BlockingQueue<ServiceMessage> messageQueue =
                new LinkedBlockingQueue<ServiceMessage>();
        conversations.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
            {
                public void send(ServiceMessage message)
                {
                    messageQueue.add(message);
                }
            });
        final String id =
                conversations.startConversation("echo", "dummyClient").getServiceConversationId();
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;
        conversations.getIncomingMessageTransport().send(new ServiceMessage(id, 0, false, "One"));
        ServiceMessage m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("One", m.getPayload());

        // Wait for timeout to happen.
        for (int i = 0; i < 100 && conversations.hasConversation(id); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(conversations.hasConversation(id));
        m = messageQueue.take();
        assertTrue(m.isException());
        assertTrue(m.tryGetExceptionDescription().startsWith(
                "ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked:"));
    }

    @Test(expectedExceptions = UnknownServiceTypeException.class)
    public void testUnknownService() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(100);
        conversations.addClientResponseTransport("dummyClient", new IServiceMessageTransport()
            {
                public void send(ServiceMessage message)
                {
                }
            });
        conversations.startConversation("echo", "dummyClient");
    }

    @Test(expectedExceptions = UnknownClientException.class)
    public void testUnknownClient() throws Exception
    {
        final ServiceConversationServer conversations = new ServiceConversationServer(100);
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "echo";
                }
            });
        conversations.startConversation("echo", "dummyClient");
    }

    private static class ExceptionThrowingService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            throw new RuntimeException("Don't like you!");
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    public IService create()
                    {
                        return new ExceptionThrowingService();
                    }

                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    public String getServiceTypeId()
                    {
                        return "throwException";
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
            fail();
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class EventuallyExceptionThrowingService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            messenger.send("OK1");
            messenger.send("OK2");
            messenger.send("OK3");
            throw new RuntimeException("Don't like you!");
        }

        static IServiceFactory createFactory()
        {
            return new IServiceFactory()
                {
                    public IService create()
                    {
                        return new EventuallyExceptionThrowingService();
                    }

                    public int getClientTimeoutMillis()
                    {
                        return 100;
                    }

                    public String getServiceTypeId()
                    {
                        return "throwException";
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
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class DelayedService implements IService
    {
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
                    public IService create()
                    {
                        return new DelayedService();
                    }

                    public int getClientTimeoutMillis()
                    {
                        return 50;
                    }

                    public String getServiceTypeId()
                    {
                        return "delayed";
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
