/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.IPrincipalProvider;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class SingleSignOnServlet extends AbstractServlet
{
    private static final String OPENBIS_COOKIE = "openbis";

    public static final String SERVLET_NAME = "ssos";

    public static final String SESSION_ID_KEY = "session-id-key";

    public static final String DEFAULT_SESSION_ID_KEY = "Shib-Session-ID";

    public static final String USER_ID_KEY = "user-id-key";

    public static final String DEFAULT_USER_ID_KEY = "mail";

    public static final String FIRST_NAME_KEY = "first-name-key";

    public static final String DEFAULT_FIRST_NAME_KEY = "givenName";

    public static final String LAST_NAME_KEY = "last-name-key";

    public static final String DEFAULT_LAST_NAME_KEY = "surname";

    public static final String EMAIL_KEY = "email-key";

    public static final String DEFAULT_EMAIL_KEY = "mail";

    public static final String REDIRECT_URL_PARAMETER_NAME = "redirect-url";

    public static final String REDIRECT_URL_TEMPLATE_PARAMETER_NAME = "redirect-url-template";

    public static final String REDIRECT_URL_KEY = "redirect-url";

    public static final String DEFAULT_REDIRECT_URL = "webapp/eln-lims";

    private static final String SINGLE_SIGN_ON_REDIRECT_URL_TEMPLATE_PROPERTY = "single-sign-on.redirect-url-template";

    private static final String DEFAULT_SINGLE_SIGN_ON_REDIRECT_URL_TEMPLATE = "https://${host}/openbis/webapp/eln-lims";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SingleSignOnServlet.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    @Autowired
    private IApplicationServerInternalApi applicationServerApi;

    private final Map<String, String> sessionTokenBySessionId = new HashMap<>();

    private Template template;

    @Override
    protected void initServletContext(ServletContext servletContext)
    {
        template = new Template(configurer.getResolvedProps().getProperty(SINGLE_SIGN_ON_REDIRECT_URL_TEMPLATE_PROPERTY,
                DEFAULT_SINGLE_SIGN_ON_REDIRECT_URL_TEMPLATE));
        template.createFreshCopy().bind("host", ""); // Check that template contains '${host}'
    }

    @Override
    @RequestMapping({ SERVLET_NAME })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        operationLog.info("handle sso event");
        removeStaleSessions();
        String sessionId = getHeader(request, SESSION_ID_KEY, DEFAULT_SESSION_ID_KEY);
        synchronized (this)
        {
            String sessionToken = sessionTokenBySessionId.get(sessionId);
            String returnURL = request.getParameter("return");
            if (returnURL != null)
            {
                handleLogOut(request, response, sessionId, sessionToken, returnURL);
            } else
            {
                handleLogIn(request, response, sessionId, sessionToken);
            }
        }
    }

    private void removeStaleSessions()
    {
        for (Entry<String, String> entry : new ArrayList<>(sessionTokenBySessionId.entrySet()))
        {
            String sessionToken = entry.getValue();
            if (sessionManager.tryGetSession(sessionToken) == null)
            {
                sessionTokenBySessionId.remove(entry.getKey());
            }
        }
    }

    private void handleLogIn(HttpServletRequest request, HttpServletResponse response, String sessionId, String sessionToken) throws IOException
    {
        if (sessionToken != null)
        {
            Session session = sessionManager.tryGetSession(sessionToken);
            if (session != null)
            {
                sessionToken = session.getSessionToken();
                redirectToApp(request, response, sessionToken);
                return;
            }
        }
        String userId = ServerUtils.escapeEmail(getHeader(request, USER_ID_KEY, DEFAULT_USER_ID_KEY));
        String firstName = getHeader(request, FIRST_NAME_KEY, DEFAULT_FIRST_NAME_KEY);
        String lastName = getHeader(request, LAST_NAME_KEY, DEFAULT_LAST_NAME_KEY);
        String email = getHeader(request, EMAIL_KEY, DEFAULT_EMAIL_KEY);
        Principal principal = new Principal(userId, firstName, lastName, email, true);
        sessionToken = sessionManager.tryToOpenSession(userId, new IPrincipalProvider()
        {
            @Override
            public Principal tryToGetPrincipal(String userID)
            {
                return principal;
            }
        });
        applicationServerApi.registerUser(sessionToken);
        sessionTokenBySessionId.put(sessionId, sessionToken);

        operationLog.info("Session token " + sessionToken + " created for SSO session id " + sessionId + " (" + sessionTokenBySessionId.size() + ")");
        redirectToApp(request, response, sessionToken);
    }

    private void handleLogOut(HttpServletRequest request, HttpServletResponse response, String sessionId, String sessionToken, String returnURL)
            throws IOException
    {
        operationLog.info("log out session id: " + sessionId);
        sessionTokenBySessionId.remove(sessionId);
        if (sessionToken != null)
        {
            Session session = sessionManager.tryGetSession(sessionToken);
            if (session != null)
            {
                sessionManager.closeSession(sessionToken);
                operationLog.info("Session " + sessionToken + " closed.");
            }
        }
        operationLog.info("Redirect to " + returnURL);
        removeOpenbisCookies(request, response);
        response.sendRedirect(returnURL);
    }

    private void redirectToApp(HttpServletRequest request, HttpServletResponse response, String sessionToken) throws IOException
    {
        String redirectUrl = null;

        if (StringUtils.isNotBlank(request.getParameter(REDIRECT_URL_PARAMETER_NAME)))
        {
            redirectUrl = request.getParameter(REDIRECT_URL_PARAMETER_NAME);
        } else if (StringUtils.isNotBlank(request.getParameter(REDIRECT_URL_TEMPLATE_PARAMETER_NAME)))
        {
            String host = request.getHeader("X-Forwarded-Host");
            Template template = new Template(request.getParameter(REDIRECT_URL_TEMPLATE_PARAMETER_NAME)).createFreshCopy();
            template.bind("host", host);
            redirectUrl = template.createText();
        } else
        {
            String host = request.getHeader("X-Forwarded-Host");
            Template template = this.template.createFreshCopy();
            template.bind("host", host);
            redirectUrl = configurer.getResolvedProps().getProperty(REDIRECT_URL_KEY, template.createText());
        }

        operationLog.info("redirect to " + redirectUrl);

        removeOpenbisCookies(request, response);
        Cookie cookie = new Cookie(OPENBIS_COOKIE, sessionToken);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.sendRedirect(redirectUrl);
    }

    private void removeOpenbisCookies(HttpServletRequest request, HttpServletResponse response)
    {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equals(OPENBIS_COOKIE))
            {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    private String getHeader(HttpServletRequest request, String keyProperty, String defaultKey)
    {
        String key = configurer.getResolvedProps().getProperty(keyProperty, defaultKey);
        String header = request.getHeader(key);
        if (header != null)
        {
            return header;
        }
        operationLog.error("Missing header '" + key + "'.");
        throw new IllegalArgumentException("Missing header '" + key + "'.");
    }

}
