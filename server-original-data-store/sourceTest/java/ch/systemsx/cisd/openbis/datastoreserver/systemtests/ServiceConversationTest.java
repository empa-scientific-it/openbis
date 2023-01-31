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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.DoAllAction;
import org.jmock.lib.action.ReturnValueAction;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Progress;
import ch.systemsx.cisd.openbis.common.conversation.client.ServiceConversationClientDetails;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.manager.BaseServiceConversationClientManager;
import ch.systemsx.cisd.openbis.common.conversation.manager.BaseServiceConversationServerManager;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationServerManagerRemote;
import ch.systemsx.cisd.openbis.common.spring.WaitAction;

@Test
@Listeners(TestReportCleaner.class)
public class ServiceConversationTest
{

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION, ServiceConversationTest.class);

    private static final String SERVER_PATH = "/conversationServer";

    private static final String CLIENT_PATH = "/conversationClient";

    private static final String SESSION_TOKEN_1 = "test-session-token-1";

    private static final String SESSION_TOKEN_2 = "test-session-token-2";

    private static final int TIMEOUT = 300;

    private static final Integer CLIENT_ID_1 = Integer.valueOf(1);

    private static final Integer CLIENT_ID_2 = Integer.valueOf(2);

    private static final Integer UNKNOWN_CLIENT_ID = Integer.valueOf(3);

    private Mockery context;

    private TestServiceWrapper1 serviceOnServerSideWrapper1;

    private TestServiceWrapper2 serviceOnServerSideWrapper2;

    private Integer clientPort1;

    private Integer clientPort2;

    private Integer serverPort;

    private BaseServiceConversationClientManager clientManager1;

    private BaseServiceConversationClientManager clientManager2;

    private BaseServiceConversationServerManager serverManager;

    private ServiceExporter<?> clientExporter1;

    private ServiceExporter<?> clientExporter2;

    private ServiceExporter<?> serverExporter;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();

        Integer[] ports = getPorts(3);
        clientPort1 = ports[0];
        clientPort2 = ports[1];
        serverPort = ports[2];

        serviceOnServerSideWrapper1 = new TestServiceWrapper1();
        serviceOnServerSideWrapper2 = new TestServiceWrapper2();

        clientManager1 = createClientManager();
        clientManager2 = createClientManager();
        serverManager = createServerManager();

        clientExporter1 = createClientExporter(getClientPort1(), clientManager1);
        clientExporter2 = createClientExporter(getClientPort2(), clientManager2);
        serverExporter = createServerExporter(serverManager);

        clientExporter1.getServer().start();
        clientExporter2.getServer().start();
        serverExporter.getServer().start();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception
    {
        stopServer(clientExporter1, getClientPort1());
        stopServer(clientExporter2, getClientPort2());
        stopServer(serverExporter, getServerPort());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        serviceOnServerSideWrapper1.setService(context.mock(TestService1.class));
        serviceOnServerSideWrapper2.setService(context.mock(TestService2.class));
    }

    private Integer[] getPorts(int portCount)
    {
        Set<Integer> ports = new HashSet<Integer>();

        ports: for (int i = 0; i < portCount; i++)
        {
            int attemptCounter = 0;

            while (attemptCounter < 10)
            {
                Integer port = 10000 + (int) (Math.random() * 10000);

                Socket s = null;
                try
                {
                    s = new Socket("localhost", port);
                    logger.info("Cannot pick port: " + port + " - it is already used.");
                } catch (IOException e)
                {
                    if (ports.contains(port))
                    {
                        logger.info("Cannot pick port: " + port + " - it is already used.");
                    } else
                    {
                        logger.info("Picking port: " + port + " - it is still unused.");
                        ports.add(port);
                        continue ports;
                    }

                } finally
                {
                    if (s != null)
                    {
                        try
                        {
                            s.close();
                        } catch (IOException e)
                        {
                            throw new RuntimeException("Could not find an unused port", e);
                        }
                    }
                }

                attemptCounter++;
            }

            throw new RuntimeException("Could not find an unused port after 10 attempts");
        }

        return ports.toArray(new Integer[] {});
    }

    private void stopServer(ServiceExporter<?> exporter, Integer port)
    {
        if (exporter != null && exporter.getServer().isStarted())
        {
            try
            {
                exporter.getServer().stop();
                logger.info("Stopped a service on port: " + port);
            } catch (Exception e)
            {
                logger.warn("Could not stop a service on port: " + port, e);
            }
        } else
        {
            logger.info("Didn't have to stop a service on port: " + port + " as it wasn't running.");
        }
    }

    private Integer getClientPort1()
    {
        return clientPort1;
    }

    private Integer getClientPort2()
    {
        return clientPort2;
    }

    private Integer getServerPort()
    {
        return serverPort;
    }

    private String getClientUrl1()
    {
        return "http://localhost:" + getClientPort1() + CLIENT_PATH;
    }

    private String getClientUrl2()
    {
        return "http://localhost:" + getClientPort2() + CLIENT_PATH;
    }

    private String getServerUrl()
    {
        return "http://localhost:" + getServerPort() + SERVER_PATH;
    }

    private <S> S getServiceOnClientSide1(Class<S> serviceInterface)
    {
        return clientManager1.getService(getServerUrl(), serviceInterface, SESSION_TOKEN_1,
                CLIENT_ID_1, TIMEOUT);
    }

    private <S> S getServiceOnClientSide2(Class<S> serviceInterface)
    {
        return clientManager2.getService(getServerUrl(), serviceInterface, SESSION_TOKEN_2,
                CLIENT_ID_2, TIMEOUT);
    }

    @Test(expectedExceptions = ServiceExecutionException.class, retryAnalyzer = RetryTen.class)
    public void testNonConversationalMethod() throws Exception
    {
        try
        {
            getServiceOnClientSide1(TestService1.class).nonConversationalMethod();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithoutReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithoutReturnValue();
                }
            });
        getServiceOnClientSide1(TestService1.class).methodWithoutReturnValue();
        assertNoMoreConversations();
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithNullReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithObjectReturnValue();
                    will(returnValue(null));
                }
            });
        Assert.assertNull(getServiceOnClientSide1(TestService1.class).methodWithObjectReturnValue());
        assertNoMoreConversations();
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithPrimitiveReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithPrimitiveReturnValue();
                    will(returnValue(1));
                }
            });

        Assert.assertEquals(getServiceOnClientSide1(TestService1.class)
                .methodWithPrimitiveReturnValue(), 1);
        assertNoMoreConversations();
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithSerializableReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService())
                            .methodWithSerializableReturnValue();
                    will(returnValue("abc"));

                }
            });
        Assert.assertEquals(getServiceOnClientSide1(TestService1.class)
                .methodWithSerializableReturnValue(), "abc");
        assertNoMoreConversations();
    }

    @Test(expectedExceptions = ServiceExecutionException.class, retryAnalyzer = RetryTen.class)
    public void testMethodWithNonSerializableReturnValue()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithObjectReturnValue();
                        will(returnValue(new Object()));
                    }
                });
            getServiceOnClientSide1(TestService1.class).methodWithObjectReturnValue();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithNullParameter()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).methodWithObjectParameter(null);
                }
            });
        getServiceOnClientSide1(TestService2.class).methodWithObjectParameter(null);
        assertNoMoreConversations();
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithPrimitiveParameter()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).methodWithPrimitiveParameter(1);
                }
            });
        getServiceOnClientSide1(TestService2.class).methodWithPrimitiveParameter(1);
        assertNoMoreConversations();
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithSerializableParameter()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).methodWithSerializableParameter(
                            "abc");
                }
            });
        getServiceOnClientSide1(TestService2.class).methodWithSerializableParameter("abc");
        assertNoMoreConversations();
    }

    @Test(expectedExceptions = RemoteAccessException.class, retryAnalyzer = RetryTen.class)
    public void testMethodWithNonSerializableParameter()
    {
        try
        {
            getServiceOnClientSide1(TestService2.class).methodWithObjectParameter(new Object());
        } finally
        {
            // wait for the server to timeout and clean up the conversation
            assertNoMoreConversations(TIMEOUT);
        }
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithAutomaticProgressShouldNotTimeout()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithAutomaticProgress();
                    will(new DoAllAction(new WaitAction(2 * TIMEOUT), new ReturnValueAction(1)));

                }
            });
        Assert.assertEquals(getServiceOnClientSide1(TestService1.class)
                .methodWithAutomaticProgress(), 1);
        assertNoMoreConversations();
    }

    @Test(expectedExceptions = TimeoutExceptionUnchecked.class, retryAnalyzer = RetryTen.class)
    public void testMethodWithManualProgressShouldTimeoutWhenProgressIsNotReported()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithManualProgress();
                        will(new WaitAction(2 * TIMEOUT));
                    }
                });
            getServiceOnClientSide1(TestService1.class).methodWithManualProgress();
        } finally
        {
            // wait for the server to wake up and clean up the conversation
            assertNoMoreConversations(TIMEOUT);
        }
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMethodWithManualProgressShouldNotTimeoutWhenProgressIsReported() throws InterruptedException
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithManualProgress();
                    will(new Action()
                        {

                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                for (int i = 1; i <= 10; i++)
                                {
                                    ServiceConversationsThreadContext.getProgressListener().update(
                                            "manualProgress", 10, i);
                                    ConcurrencyUtilities.sleep(TIMEOUT / 2);
                                }
                                return null;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                                description
                                        .appendText("processing method with manual progress reporting");
                            }
                        });
                }
            });

        getServiceOnClientSide1(TestService1.class).methodWithManualProgress();
        assertNoMoreConversations(5 * TIMEOUT);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, retryAnalyzer = RetryTen.class)
    public void testUnknownClient()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithoutReturnValue();
                    }
                });

            TestService1 service =
                    clientManager1.getService(getServerUrl(), TestService1.class, SESSION_TOKEN_1,
                            UNKNOWN_CLIENT_ID, TIMEOUT);
            service.methodWithoutReturnValue();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMultipleClientsWithSameService()
    {
        logger.info("testMultipleClientsWithSameService");
        testMultipleClients(TestService2.class, serviceOnServerSideWrapper2.getService(),
                TestService2.class, serviceOnServerSideWrapper2.getService());
        logger.info("testMultipleClientsWithSameService done");
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testMultipleClientsWithDifferentService()
    {
        logger.info("testMultipleClientsWithDifferentService");
        testMultipleClients(TestService1.class, serviceOnServerSideWrapper1.getService(),
                TestService2.class, serviceOnServerSideWrapper2.getService());
        logger.info("testMultipleClientsWithDifferentService done");
    }

    private void testMultipleClients(final Class<? extends TestService> serviceAInterface,
            final TestService serviceA, final Class<? extends TestService> serviceBInterface,
            final TestService serviceB)
    {
        final int NUMBER_OF_CALLS = 10;
        final MessageChannel channel = new MessageChannel(NUMBER_OF_CALLS * 2 * TIMEOUT);

        context.checking(new Expectations()
            {
                {
                    for (int i = 0; i < NUMBER_OF_CALLS; i++)
                    {
                        one(serviceA).echo(i);
                        will(returnValue(i));
                    }
                    for (int i = 0; i < NUMBER_OF_CALLS; i++)
                    {
                        one(serviceB).echo(NUMBER_OF_CALLS + i);
                        will(returnValue(NUMBER_OF_CALLS + i));
                    }
                }
            });

        Thread client1Thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    for (int i = 0; i < NUMBER_OF_CALLS; i++)
                    {
                        logger.info("CLIENT 1 THREAD: SERVICE A ECHO " + i);
                        Assert.assertEquals(getServiceOnClientSide1(serviceAInterface).echo(i), i);
                        logger.info("CLIENT 1 THREAD: SERVICE A ECHO " + i + " asserted");
                        ConcurrencyUtilities.sleep(TIMEOUT / NUMBER_OF_CALLS);
                        logger.info("CLIENT 1 THREAD: SERVICE A ECHO " + i + " slept");
                    }
                    channel.send("finished");
                    logger.info("CLIENT 1 THREAD: SENT FINISHED");
                }
            });

        Thread client2Thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    for (int i = 0; i < NUMBER_OF_CALLS; i++)
                    {
                        logger.info("CLIENT 2 THREAD: SERVICE B ECHO " + i);
                        Assert.assertEquals(
                                getServiceOnClientSide2(serviceBInterface)
                                        .echo(NUMBER_OF_CALLS + i), NUMBER_OF_CALLS + i);
                        logger.info("CLIENT 2 THREAD: SERVICE B ECHO " + i + " asserted");
                        ConcurrencyUtilities.sleep(TIMEOUT / NUMBER_OF_CALLS);
                        logger.info("CLIENT 2 THREAD: SERVICE B ECHO " + i + " slept");
                    }
                    channel.send("finished");
                    logger.info("CLIENT 2 THREAD: SENT FINISHED");
                }
            });

        client1Thread.start();
        client2Thread.start();

        channel.assertNextMessage("finished");
        channel.assertNextMessage("finished");

        assertNoMoreConversations();
    }

    private void assertNoMoreConversations()
    {
        assertNoMoreConversations(TIMEOUT);
    }

    private void assertNoMoreConversations(int additionalWaitingTime)
    {
        long maxFinishTime = System.currentTimeMillis() + additionalWaitingTime + 1000;

        while (System.currentTimeMillis() < maxFinishTime)
        {
            boolean noMoreConversations =
                    clientManager1.getConversationCount() == 0
                            && clientManager2.getConversationCount() == 0
                            && serverManager.getConversationCount() == 0;
            if (noMoreConversations)
            {
                return;
            } else
            {
                ConcurrencyUtilities.sleep(100);
            }
        }

        Assert.fail("Some conversations still remain open - client1: "
                + clientManager1.getConversationCount() + " client2: "
                + clientManager2.getConversationCount() + " server: "
                + serverManager.getConversationCount());
    }

    private BaseServiceConversationClientManager createClientManager()
    {
        return new BaseServiceConversationClientManager();
    }

    private BaseServiceConversationServerManager createServerManager()
    {
        return new BaseServiceConversationServerManager()
            {
                {
                    addService(TestService1.class, serviceOnServerSideWrapper1);
                    addService(TestService2.class, serviceOnServerSideWrapper2);
                }

                @Override
                protected ServiceConversationClientDetails getClientDetailsForClientId(
                        Object clientId)
                {
                    if (clientId.equals(CLIENT_ID_1))
                    {
                        return new ServiceConversationClientDetails(getClientUrl1(), TIMEOUT);
                    } else if (clientId.equals(CLIENT_ID_2))
                    {
                        return new ServiceConversationClientDetails(getClientUrl2(), TIMEOUT);
                    } else
                    {
                        throw new IllegalArgumentException("Unknown client");
                    }
                }
            };
    }

    private ServiceExporter<IServiceConversationClientManagerRemote> createClientExporter(int port,
            BaseServiceConversationClientManager manager)
    {
        try
        {
            ServiceExporter<IServiceConversationClientManagerRemote> exporter =
                    new ServiceExporter<IServiceConversationClientManagerRemote>(port, CLIENT_PATH,
                            IServiceConversationClientManagerRemote.class, manager);
            return exporter;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private ServiceExporter<IServiceConversationServerManagerRemote> createServerExporter(
            BaseServiceConversationServerManager manager)
    {
        try
        {
            ServiceExporter<IServiceConversationServerManagerRemote> exporter =
                    new ServiceExporter<IServiceConversationServerManagerRemote>(getServerPort(),
                            SERVER_PATH, IServiceConversationServerManagerRemote.class, manager);
            return exporter;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private class ServiceExporter<S>
    {

        private Server server;

        public ServiceExporter(final int port, final String path, final Class<?> serviceInterface,
                final S service)
        {
            this.server = new Server();
            HttpConfiguration httpConfig = new HttpConfiguration();
            ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
            connector.setPort(port);

            this.server.addConnector(connector);

            DispatcherServlet dispatcher = new DispatcherServlet()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected WebApplicationContext findWebApplicationContext()
                    {
                        GenericWebApplicationContext ctx = new GenericWebApplicationContext();

                        GenericBeanDefinition exporterBean = new GenericBeanDefinition();
                        exporterBean.setBeanClass(HttpInvokerServiceExporter.class);
                        MutablePropertyValues exporterProperties = new MutablePropertyValues();
                        exporterProperties.addPropertyValue("service", service);
                        exporterProperties.addPropertyValue("serviceInterface", serviceInterface);
                        exporterBean.setPropertyValues(exporterProperties);
                        ctx.registerBeanDefinition("serviceExporter", exporterBean);

                        GenericBeanDefinition mappingBean = new GenericBeanDefinition();
                        mappingBean.setBeanClass(SimpleUrlHandlerMapping.class);
                        Map<String, String> urlMap = new HashMap<String, String>();
                        urlMap.put(path, "serviceExporter");
                        MutablePropertyValues mappingProperties = new MutablePropertyValues();
                        mappingProperties.addPropertyValue("urlMap", urlMap);
                        mappingBean.setPropertyValues(mappingProperties);
                        ctx.registerBeanDefinition("mapping", mappingBean);

                        ctx.refresh();
                        return ctx;
                    }
                };

            ServletContextHandler servletCtx =
                    new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            servletCtx.addServlet(new ServletHolder(dispatcher), "/*");
        }

        public Server getServer()
        {
            return server;
        }

    }

    public static interface TestService
    {

        @Conversational(progress = Progress.MANUAL)
        public Object echo(Object parameter);

    }

    public static interface TestService1 extends TestService
    {

        public void nonConversationalMethod();

        @Conversational(progress = Progress.MANUAL)
        public void methodWithoutReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public int methodWithPrimitiveReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Serializable methodWithSerializableReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Object methodWithObjectReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Object methodWithManualProgress();

        @Conversational(progress = Progress.AUTOMATIC)
        public Object methodWithAutomaticProgress();

    }

    public static interface TestService2 extends TestService
    {

        @Conversational(progress = Progress.MANUAL)
        public void methodWithPrimitiveParameter(int parameter);

        @Conversational(progress = Progress.MANUAL)
        public void methodWithSerializableParameter(Serializable parameter);

        @Conversational(progress = Progress.MANUAL)
        public void methodWithObjectParameter(Object parameter);

    }

    public static interface TestServiceLocal
    {
        public void someLocalMethod();
    }

    public static class TestServiceWrapper1 implements TestServiceLocal, TestService1
    {

        private TestService1 service;

        @Override
        public void nonConversationalMethod()
        {
            service.nonConversationalMethod();
        }

        @Override
        public void methodWithoutReturnValue()
        {
            service.methodWithoutReturnValue();
        }

        @Override
        public int methodWithPrimitiveReturnValue()
        {
            return service.methodWithPrimitiveReturnValue();
        }

        @Override
        public Serializable methodWithSerializableReturnValue()
        {
            return service.methodWithSerializableReturnValue();
        }

        @Override
        public Object methodWithObjectReturnValue()
        {
            return service.methodWithObjectReturnValue();
        }

        @Override
        public Object methodWithManualProgress()
        {
            return service.methodWithManualProgress();
        }

        @Override
        public Object methodWithAutomaticProgress()
        {
            return service.methodWithAutomaticProgress();
        }

        @Override
        public Object echo(Object parameter)
        {
            return service.echo(parameter);
        }

        @Override
        public void someLocalMethod()
        {
        }

        public TestService1 getService()
        {
            return service;
        }

        public void setService(TestService1 service)
        {
            this.service = service;
        }

    }

    public static class TestServiceWrapper2 implements TestServiceLocal, TestService2
    {

        private TestService2 service;

        @Override
        public void methodWithPrimitiveParameter(int parameter)
        {
            service.methodWithPrimitiveParameter(parameter);
        }

        @Override
        public void methodWithSerializableParameter(Serializable parameter)
        {
            service.methodWithSerializableParameter(parameter);
        }

        @Override
        public void methodWithObjectParameter(Object parameter)
        {
            service.methodWithObjectParameter(parameter);
        }

        @Override
        public Object echo(Object parameter)
        {
            return service.echo(parameter);
        }

        @Override
        public void someLocalMethod()
        {
        }

        public TestService2 getService()
        {
            return service;
        }

        public void setService(TestService2 service)
        {
            this.service = service;
        }

    }

}
