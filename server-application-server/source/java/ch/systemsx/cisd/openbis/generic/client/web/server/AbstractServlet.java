/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractServlet extends AbstractController
{
    protected final String getSessionToken(final HttpServletRequest request)
    {
        // First, try to obtain session from request parameters
        String sessionToken = request.getParameter(GenericSharedConstants.SESSION_ID_PARAMETER);
        // Second, try to obtain session from cookies
        if(sessionToken == null) {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for(Cookie cookie:cookies) {
                    if(cookie.getName().equals(GenericSharedConstants.SESSION_ID_COOKIE)) {
                        sessionToken = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (sessionToken != null)
        {
            return sessionToken;
        }
        return getParameter(request, SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
    }

    private String getParameter(final HttpServletRequest request, String parameterName)
    {
        // We must have a session reaching this point. See the constructor where we set
        // 'setRequireSession(true)'.
        final HttpSession session = request.getSession(false);
        assert session != null : "Session must be specified.";
        return ((String) session.getAttribute(parameterName));
    }

    protected final void writeResponse(final HttpServletResponse response, final String value)
            throws IOException
    {
        final PrintWriter writer = response.getWriter();
        writer.write(value);
        writer.flush();
        writer.close();
    }

    //
    // AbstractController
    //

    @Override
    protected final ModelAndView handleRequestInternal(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        try
        {
            respondToRequest(request, response);
        } catch (final UserFailureException ex)
        {
            writeResponse(response, ex.getMessage());
        }
        return null;
    }

    protected abstract void respondToRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception, IOException;

}
