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

package ch.systemsx.cisd.openbis.remoteapitest;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * Test cases which have access to the public API services of a running, fully-fledged openBIS server. The tests do not see the server internals and
 * thus they must employ black-box testing strategies.
 * 
 * @author Kaloyan Enimanev
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class RemoteApiTestCase extends AbstractTransactionalTestNGSpringContextTests
{
    private Server server;

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        TestInitializer.init();
        server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(TestInstanceHostUtils.getOpenBISPort());
        server.addConnector(connector);
        DispatcherServlet dispatcherServlet = new DispatcherServlet()
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected WebApplicationContext findWebApplicationContext()
                {
                    XmlBeanFactory f =
                            new XmlBeanFactory(new FileSystemResource(
                                    "../openbis/resource/server/spring-servlet.xml"));
                    GenericWebApplicationContext wac = new GenericWebApplicationContext(f);
                    try
                    {
                        springTestContextPrepareTestInstance();
                    } catch (Exception ex)
                    {
                        throw new RuntimeException("Cannot initialize test instance:"
                                + ex.getMessage(), ex);
                    }
                    wac.setParent(applicationContext);
                    wac.refresh();
                    return wac;
                }
            };
        ServletContextHandler sch =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        sch.addServlet(new ServletHolder(dispatcherServlet), "/*");
        server.setStopAtShutdown(true);
        server.start();
    }
}
