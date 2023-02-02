/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * An abstract <code>RemoteServiceServlet</code> that merges <i>Spring</i>'s {@link Controller} and manages this servlet the same way <i>Spring</i>
 * beans are.
 * 
 * @author Christian Ribeaud
 */
public abstract class GWTSpringController extends RemoteServiceServlet implements Controller,
        InitializingBean, ServletConfigAware, DisposableBean, BeanNameAware, RemoteService
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GWTSpringController.class);

    private static final long serialVersionUID = 1L;

    private ServletConfig servletConfig;

    private String beanName;

    //
    // RemoteServiceServlet
    //

    @Override
    public String processCall(final String payload) throws SerializationException
    {
        try
        {
            return super.processCall(payload);
        } catch (final Throwable th)
        {
            operationLog.error("Error processing request.", th);
            if (th instanceof Error)
            {
                throw (Error) th;
            } else if (th instanceof RuntimeException)
            {
                throw (RuntimeException) th;
            } else
            {
                throw new Error("Unexpected error: " + th.getMessage());
            }
        }
    }

    @Override
    public final ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    //
    // Controller
    //

    @Override
    public final ModelAndView handleRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        doPost(request, response);
        return null;
    }

    //
    // InitializingBean
    //

    /**
     * Note that {@link #setServletConfig(ServletConfig)} gets called before this method.
     */
    @Override
    public final void afterPropertiesSet() throws Exception
    {
        LogInitializer.init();
        if (operationLog.isTraceEnabled())
        {
            final String message =
                    "All the properties have been set for bean '" + beanName
                            + "'. Time to initialize this servlet.";
            operationLog.trace(message);
        }
        init(servletConfig);
    }

    //
    // ServletConfigAware
    //

    @Override
    public final void setServletConfig(final ServletConfig servletConfig)
    {
        assert servletConfig != null;
        if (operationLog.isTraceEnabled())
        {
            final String message =
                    "Setting servlet config for class '" + getClass().getSimpleName() + "'.";
            operationLog.trace(message);
        }
        this.servletConfig = servletConfig;
    }

    //
    // BeanNameAware
    //

    @Override
    public final void setBeanName(final String beanName)
    {
        this.beanName = beanName;
    }

}