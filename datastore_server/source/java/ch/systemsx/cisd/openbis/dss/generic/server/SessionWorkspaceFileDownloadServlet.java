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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.authentication.pat.FileBasedPersonalAccessTokenDAO;
import ch.systemsx.cisd.common.servlet.HttpServletRequestUtils;
import ch.systemsx.cisd.openbis.common.pat.PersonalAccessTokenConverter;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * Servlet that handles the download of files from the session workspace. The content type of the response is guessed from the downloaded file name.
 * <ul>
 * <li>Accepted HTTP methods: GET</li>
 * <li>HTTP request parameters: sessionID (String required), filePath (String required)</li>
 * </ul>
 * 
 * @author pkupczyk
 */
public class SessionWorkspaceFileDownloadServlet extends HttpServlet
{

    private static final String FILE_PATH_PARAM = "filePath";

    private static final long serialVersionUID = 1L;

    private IDssServiceRpcGeneric service;

    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);

        StreamSupportingHttpInvokerServiceExporter serviceExporter =
                ServiceProvider.getDssServiceRpcGeneric();
        service = (IDssServiceRpcGeneric) serviceExporter.getService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        SessionWorkspaceFileDownloadRequest downloadRequest =
                new SessionWorkspaceFileDownloadRequest(request);

        downloadRequest.validate();

        InputStream fileStream =
                service.getFileFromSessionWorkspace(downloadRequest.getSessionId(),
                        downloadRequest.getFilePath());

        SessionWorkspaceFileDownloadResponse downloadResponse =
                new SessionWorkspaceFileDownloadResponse(response);
        downloadResponse.writeFile(downloadRequest.getFilePath(), fileStream);
    }

    private class SessionWorkspaceFileDownloadRequest
    {

        private HttpServletRequest request;

        public SessionWorkspaceFileDownloadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            String sessionId = HttpServletRequestUtils.getStringParameter(request, Utils.SESSION_ID_PARAM);
            sessionId = new PersonalAccessTokenConverter(new FileBasedPersonalAccessTokenDAO()).convert(sessionId);
            return sessionId;
        }

        public String getFilePath()
        {
            return HttpServletRequestUtils.getStringParameter(request, FILE_PATH_PARAM);
        }

        public void validate()
        {
            if (getSessionId() == null)
            {
                throw new IllegalArgumentException(Utils.SESSION_ID_PARAM
                        + " parameter cannot be null");
            }
            if (getFilePath() == null)
            {
                throw new IllegalArgumentException(FILE_PATH_PARAM + " parameter cannot be null");
            }
        }

    }

    private class SessionWorkspaceFileDownloadResponse
    {

        private HttpServletResponse response;

        public SessionWorkspaceFileDownloadResponse(HttpServletResponse response)
        {
            this.response = response;
        }

        public void writeFile(String filePath, InputStream fileStream) throws IOException
        {
            ServletOutputStream outputStream = null;

            try
            {
                String fileName = FilenameUtils.getName(filePath);
                response.setHeader("Content-Disposition", "inline; filename=" + fileName);
                response.setContentType(URLConnection.guessContentTypeFromName(fileName));
                response.setStatus(HttpServletResponse.SC_OK);
                outputStream = response.getOutputStream();
                IOUtils.copyLarge(fileStream, outputStream);
            } finally
            {
                IOUtils.closeQuietly(fileStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

    }

}
