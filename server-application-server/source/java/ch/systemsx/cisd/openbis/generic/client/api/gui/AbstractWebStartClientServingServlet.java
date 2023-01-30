/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.api.gui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * A servlet the generates the JNLP document for serving a web-start client.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractWebStartClientServingServlet extends AbstractServlet
{
    private static final String SERVER_URL = BasicConstant.SERVER_URL_PARAMETER;

    private static final String CODEBASE_URL = BasicConstant.CODEBASE_PARAMETER;

    private final Logger operationLog;

    protected AbstractWebStartClientServingServlet()
    {
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
    }

    @Override
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception, IOException
    {
        try
        {
            response.setContentType("application/x-java-jnlp-file");
            PrintWriter writer =
                    new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
            Template template = getJnlpTemplate().createFreshCopy();
            String sessionToken = bindTemplateParameters(request, template);
            writer.print(template.createText());
            writer.close();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(getInfoLogText(request, sessionToken));
            }
        } catch (UserFailureException ex)
        {
            operationLog.error("Couldn't create JNLP file", ex);
            printError(response, ex.getMessage());
        }
    }

    private String getServiceURL(HttpServletRequest request)
    {
        String url = getParam(request, SERVER_URL);
        try
        {
            URL baseURL = new URL(url);
            String protocol = baseURL.getProtocol();
            String host = baseURL.getHost();
            int port = baseURL.getPort();
            return new URL(protocol, host, port, "/openbis/openbis/").toString();
        } catch (MalformedURLException ex)
        {
            throw new EnvironmentFailureException("Invalid URL", ex);
        }
    }

    protected String getCodebaseUrl(HttpServletRequest request)
    {
        return getParam(request, CODEBASE_URL);
    }

    private String getParam(HttpServletRequest request, String paramName)
    {
        String value = request.getParameter(paramName);
        if (value == null)
        {
            throw new UserFailureException("Missing URL parameter '" + paramName + "'.");
        }
        return value;
    }

    private void printError(HttpServletResponse response, String errorMessage) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.write("<html><body><b>" + errorMessage + "</b></body></html>");
        writer.flush();
        writer.close();
    }

    /**
     * Return the text that should go to the info log.
     * <p>
     * This method will only be called if logging at the info level is on.
     */
    protected String getInfoLogText(HttpServletRequest request, String sessionToken)
    {
        return "Launch web start client " + getMainClassName() + " for session " + sessionToken;
    }

    protected String bindTemplateParameters(HttpServletRequest request, Template template)
    {
        template.bind("title", getTitle());
        template.bind("description", getDescription());
        template.bind("base-URL", getCodebaseUrl(request));
        template.bind("main-class", getMainClassName());
        template.bind("service-URL", getServiceURL(request));
        String sessionToken = getSessionToken(request);
        template.bind("session-id", sessionToken);
        return sessionToken;
    }

    /**
     * Return a template for the JNLP.
     */
    abstract protected Template getJnlpTemplate();

    abstract protected String getMainClassName();

    abstract protected String getTitle();

    protected String getDescription()
    {
        return getTitle();
    }

}
