/*
 * Copyright 2008 ETH Zuerich, CISD
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Serves the template files for entity batch registration.
 *
 * @author Izabela Adamczyk
 */
@Controller
public class FileTemplateServiceServlet extends AbstractFileDownloadServlet
{

    @Resource(name = ResourceNames.COMMON_SERVICE)
    private ICommonClientService service;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    protected IOpenBisSessionManager sessionManager;

    protected FileTemplateServiceServlet()
    {
        setSynchronizeOnSession(true);
        setRequireSession(false); // To allow upload a file for usage from an API given a session token we will manage an alternative session
                                  // validation programatically.
    }

    @Override
    @RequestMapping({ "/template-download", "/openbis/template-download" })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        super.respondToRequest(request, response);
    }

    protected Session getSession(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        return sessionManager.getSession(sessionToken);
    }

    protected HttpSession getSession(final HttpServletRequest request)
    {
        // We must have a session reaching this point. See the constructor where we set
        HttpSession session = request.getSession(false);
        String sessionToken = request.getParameter("sessionID");

        // If no session is found, the user from an API have a chance to give the sessionID
        if (session == null && sessionToken != null && !sessionToken.isEmpty())
        {
            Session sessionFromToken = getSession(sessionToken);
            if (sessionFromToken != null)
            {
                session = request.getSession();
                session.setAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY, sessionFromToken.getSessionToken());
            }
        }

        // Corner Case - Same session is been used with a different API Token, update the session token since is the same browser.
        if (session != null && sessionToken != null && !sessionToken.isEmpty())
        {
            String tokenBeingUsed = (String) session.getAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
            if (!sessionToken.equals(tokenBeingUsed))
            {
                Session sessionFromToken = getSession(sessionToken);
                if (sessionFromToken != null)
                {
                    session.setAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY, sessionFromToken.getSessionToken());
                }
            }
        }
        return session;
    }

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        // Throw Exception if no session was found or created from API token
        if (getSession(request) == null)
        {
            throw new HttpSessionRequiredException("Pre-existing session required but none found");
        }

        final String kind = request.getParameter(GenericConstants.ENTITY_KIND_KEY_PARAMETER);
        final String type = request.getParameter(GenericConstants.ENTITY_TYPE_KEY_PARAMETER);
        String format = request.getParameter(GenericConstants.FILE_FORMAT_PARAMETER);
        if (format == null)
        {
            format = "tsv";
        }
        final String autoGenerate = request.getParameter(GenericConstants.AUTO_GENERATE);
        final String withExperimentsParameter =
                request.getParameter(GenericConstants.WITH_EXPERIMENTS);
        final boolean withExperiments =
                withExperimentsParameter != null && Boolean.parseBoolean(withExperimentsParameter) ? true
                        : false;
        String withSpaceParameter = request.getParameter(GenericConstants.WITH_SPACE);
        boolean withSpace = withSpaceParameter == null || Boolean.parseBoolean(withSpaceParameter);

        final String operationKindParameter =
                request.getParameter(GenericConstants.BATCH_OPERATION_KIND);
        final BatchOperationKind operationKind = BatchOperationKind.valueOf(operationKindParameter);
        if (StringUtils.isNotBlank(kind) && StringUtils.isNotBlank(type))
        {
            String fileContent =
                    service.getTemplate(EntityKind.valueOf(kind), type, format != null ? format : "tsv", Boolean
                            .parseBoolean(autoGenerate), withExperiments, withSpace, operationKind);
            byte[] value = fileContent.getBytes();
            
            String fileName = kind + "-" + type + "-template." + format;
            return new FileContent(value, fileName);
        } else
        {
            return null;
        }
    }

}
