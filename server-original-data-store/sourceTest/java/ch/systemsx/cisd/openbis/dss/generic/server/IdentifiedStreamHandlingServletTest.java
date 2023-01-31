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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class IdentifiedStreamHandlingServletTest extends AssertJUnit
{
    private static final class MockStream extends ByteArrayInputStream
    {
        boolean closeInvoked;

        public MockStream(byte[] buf)
        {
            super(buf);
        }

        @Override
        public void close() throws IOException
        {
            closeInvoked = true;
            super.close();
        }
    }

    private MockStream mockStream;

    private Mockery context;

    private IStreamRepository streamRepository;

    private IdentifiedStreamHandlingServlet servlet;

    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

    @BeforeMethod
    public void setUp() throws ServletException
    {
        mockStream = new MockStream("hello world!".getBytes());
        context = new Mockery();
        streamRepository = context.mock(IStreamRepository.class);
        servletRequest = context.mock(HttpServletRequest.class);
        servletResponse = context.mock(HttpServletResponse.class);
        final ServletConfig servletConfig = context.mock(ServletConfig.class);
        final ServletContext servletContext = context.mock(ServletContext.class);
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        context.checking(new Expectations()
            {
                {
                    one(servletConfig).getServletContext();
                    will(returnValue(servletContext));

                    one(servletContext).getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
                    will(returnValue(beanFactory));

                    one(beanFactory).getBean(IdentifiedStreamHandlingServlet.STREAM_REPOSITORY_BEAN_ID);
                    will(returnValue(streamRepository));
                }
            });
        servlet = new IdentifiedStreamHandlingServlet();
        servlet.init(servletConfig);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test() throws Exception
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
                    one(servletRequest).getParameter(
                            IdentifiedStreamHandlingServlet.STREAM_ID_PARAMETER_KEY);
                    will(returnValue("id"));

                    one(streamRepository).getStream("id");
                    will(returnValue(new InputStreamWithPath(mockStream, "a/b/c/file.txt")));

                    one(servletResponse).setHeader("Content-Disposition", "inline; filename=file.txt");
                    one(servletResponse).setContentType("binary");

                    one(servletResponse).getOutputStream();
                    will(returnValue(new ServletOutputStream()
                        {
                            @Override
                            public void write(int b) throws IOException
                            {
                                output.write(b);
                            }

                            @Override
                            public boolean isReady()
                            {
                                // TODO Auto-generated method stub
                                return true;
                            }

                            @Override
                            public void setWriteListener(WriteListener arg0)
                            {
                                // TODO Auto-generated method stub

                            }
                        }));
                }
            });

        servlet.doGet(servletRequest, servletResponse);

        assertEquals("hello world!", output.toString());
        assertTrue("Close method hasn't been invoked.", mockStream.closeInvoked);
        context.assertIsSatisfied();
    }

}
