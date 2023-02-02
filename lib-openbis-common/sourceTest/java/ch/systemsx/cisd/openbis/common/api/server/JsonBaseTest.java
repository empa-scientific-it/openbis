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

package ch.systemsx.cisd.openbis.common.api.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URL;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * By-product of BIS-35. Can be used as a basis for testing JSON calls.
 * 
 * @author anttil
 */
public class JsonBaseTest
{
    private EchoService echo;

    private Server server;

    @Test(enabled = false)
    public void test1() throws Exception
    {
        String response = echo.echo("test string");
        assertThat(response, is("test string"));
    }

    public interface EchoService
    {
        public String echo(String input);
    }

    public static class EchoServiceBean implements EchoService
    {

        @Override
        public String echo(String input)
        {
            System.out.println("Service called with input " + input);
            return input;
        }

    }

    @RequestMapping({ "/service.json" })
    public static class MappedJsonServiceExporter extends JsonServiceExporter
    {
    }

    @BeforeClass
    public void beforeClass() throws Exception
    {
        LogInitializer.init();

        server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector clientConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        clientConnector.setPort(8882);
        server.addConnector(clientConnector);

        DispatcherServlet clientDispatcherServlet = new DispatcherServlet()
            {

                private static final long serialVersionUID = -4168976089074757513L;

                @Override
                protected WebApplicationContext findWebApplicationContext()
                {

                    GenericWebApplicationContext ctx = new GenericWebApplicationContext();
                    AnnotationConfigUtils.registerAnnotationConfigProcessors(ctx);

                    GenericBeanDefinition echoService = new GenericBeanDefinition();
                    echoService.setBeanClass(EchoServiceBean.class);
                    ctx.registerBeanDefinition("echoService", echoService);

                    GenericBeanDefinition jsonExporter = new GenericBeanDefinition();
                    jsonExporter.setBeanClass(MappedJsonServiceExporter.class);
                    MutablePropertyValues values = new MutablePropertyValues();
                    values.addPropertyValue("service", ctx.getBean("echoService"));
                    values.addPropertyValue("serviceInterface", EchoService.class);
                    jsonExporter.setPropertyValues(values);
                    ctx.registerBeanDefinition("jsonExporter", jsonExporter);

                    ctx.refresh();
                    return ctx;
                }
            };

        ServletContextHandler clientSch =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        clientSch.addServlet(new ServletHolder(clientDispatcherServlet), "/*");
        server.start();

        JsonRpcHttpClient client =
                new JsonRpcHttpClient(new URL("http://localhost:8882/service.json"));
        echo = ProxyUtil.createClientProxy(this.getClass().getClassLoader(), EchoService.class, client);
    }

    @AfterClass
    public void afterClass() throws Exception
    {
        server.stop();
    }
}
