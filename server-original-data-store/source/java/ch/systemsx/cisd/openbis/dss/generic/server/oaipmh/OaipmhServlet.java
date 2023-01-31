/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * <p>
 * A servlet that handles OAI-PMH protocol requests (see http://www.openarchives.org/OAI/openarchivesprotocol.html for more details on OAI-PMH).
 * </p>
 * <p>
 * <ul>
 * The requests are handled in two steps:
 * <li>user authentication</li>
 * <li>response generation</li>
 * </ul>
 * <p>
 * The user authentication step is handled by {@link ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IAuthenticationHandler}. The handler is
 * configured via "authentication-handler" property. The response generation step is handled by
 * {@link ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IRequestHandler}. The handler is configured via "request-handler" property. The servlet
 * is meant to be configured as a DSS core plugin of type "services". An example of such a configuration is presented below:
 * </p>
 * 
 * <pre>
 * class = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.OaipmhServlet
 * path = /oaipmh/*
 * request-handler = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandler
 * request-handler.script-path = handler.py
 * authentication-handler = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.BasicHttpAuthenticationHandler
 * </pre>
 * 
 * @author pkupczyk
 */
public class OaipmhServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private static final String AUTHENTICATION_HANDLER_PARAMETER_NAME = "authentication-handler";

    private static final String REQUEST_HANDLER_PARAMETER_NAME = "request-handler";

    private IAuthenticationHandler authenticationHandler;

    private IRequestHandler requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        this.authenticationHandler = initHandler(config, AUTHENTICATION_HANDLER_PARAMETER_NAME, IAuthenticationHandler.class);
        this.requestHandler = initHandler(config, REQUEST_HANDLER_PARAMETER_NAME, IRequestHandler.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends IConfigurable> T initHandler(ServletConfig config, String handlerParameterName, Class<T> handlerInterface)
            throws ServletException
    {
        String className = config.getInitParameter(handlerParameterName);

        if (className == null || className.trim().length() == 0)
        {
            throw new ServletException("Parameter '" + handlerParameterName + "' is null or empty");
        } else
        {
            try
            {
                Class<?> clazz = Class.forName(className);

                if (false == handlerInterface.isAssignableFrom(clazz))
                {
                    throw new ServletException("Handler class '" + clazz.getName() + "' specified in '" + handlerParameterName
                            + "' parameter does not implement '"
                            + handlerInterface.getName() + "' interface");
                } else
                {
                    IConfigurable instance = (IConfigurable) clazz.newInstance();

                    Properties properties = new Properties();
                    Enumeration<String> parameterNames = config.getInitParameterNames();
                    String handlerParameterNamePrefix = handlerParameterName + ".";

                    // pass in the mandatory service plugin property "path"
                    properties.setProperty("path", config.getInitParameter(ConfigParameters.PLUGIN_SERVICE_PATH_KEY));

                    // pass in request-handler plugin properties
                    while (parameterNames.hasMoreElements())
                    {
                        String parameterName = parameterNames.nextElement();

                        if (parameterName.startsWith(handlerParameterNamePrefix))
                        {
                            properties.setProperty(parameterName.substring(handlerParameterNamePrefix.length()),
                                    config.getInitParameter(parameterName));
                        }
                    }

                    instance.init(properties);
                    return (T) instance;
                }

            } catch (Exception ex)
            {
                throw new ServletException("Cannot create an instance of handler class '" + className + "'", ex);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        SessionContextDTO session = authenticationHandler.handle(req, resp);

        if (session != null)
        {
            resp.setContentType("text/xml");
            requestHandler.handle(session, req, resp);
        }
    }

}
