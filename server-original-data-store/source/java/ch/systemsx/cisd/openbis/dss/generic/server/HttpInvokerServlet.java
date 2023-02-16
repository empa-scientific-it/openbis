/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * A more generic version of the DataStoreServlet above.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2010-04-21, Tomasz Pylak: Refactor to make the reference to DataStoreServlet use the HttpInvokerServlet.
public class HttpInvokerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final HttpRequestHandler target;

    private final String description;

    public HttpInvokerServlet(HttpRequestHandler target, String description)
    {
        this.target = target;
        this.description = description;
    }

    @Override
    public void init() throws ServletException
    {
        DataStoreServer.operationLog.info("[http-invoker] RPC service available at " + description);
    }

    // Code copied from org.springframework.web.context.support.HttpRequestHandlerServlet
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        LocaleContextHolder.setLocale(request.getLocale());
        try
        {
            this.target.handleRequest(request, response);
        } catch (HttpRequestMethodNotSupportedException ex)
        {
            String[] supportedMethods = ex.getSupportedMethods();
            if (supportedMethods != null)
            {
                response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods,
                        ", "));
            }
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
        } finally
        {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}