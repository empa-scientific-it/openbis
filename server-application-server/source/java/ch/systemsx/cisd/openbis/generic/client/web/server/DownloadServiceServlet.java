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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.servlet.HttpServletRequestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;

/**
 * Servlet that handles the download of files from the session workspace. The content type of the response is guessed from the downloaded file name.
 * <ul>
 * <li>Accepted HTTP methods: GET</li>
 * <li>HTTP request parameters: sessionID (String required), filePath (String required)</li>
 * </ul>
 *
 * @author pkupczyk
 */
@Controller
public class DownloadServiceServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private static final String SESSION_ID_PARAM = "sessionID";

    private static final String FILE_PATH_PARAM = "filePath";

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @Autowired
    private IApplicationServerApi applicationServerApi;

    @Autowired
    private IPersonalAccessTokenConverter personalAccessTokenConverter;

    @Override
    @RequestMapping({ "/download", "/openbis/download" })
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        DownloadRequest downloadRequest = new DownloadRequest(request);

        downloadRequest.validate();

        File sessionWorkspace = sessionWorkspaceProvider.getSessionWorkspace(downloadRequest.getSessionId());
        File file = new File(sessionWorkspace, downloadRequest.getFilePath());

        DownloadResponse downloadResponse = new DownloadResponse(response);
        downloadResponse.writeFile(downloadRequest.getFilePath(), new BufferedInputStream(new FileInputStream(file)));
    }

    private class DownloadRequest
    {

        private final HttpServletRequest request;

        public DownloadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            String sessionId = HttpServletRequestUtils.getStringParameter(request, SESSION_ID_PARAM);
            sessionId = personalAccessTokenConverter.convert(sessionId);
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
                throw new IllegalArgumentException(SESSION_ID_PARAM
                        + " parameter cannot be null");
            }

            if (!applicationServerApi.isSessionActive(getSessionId()))
            {
                throw new IllegalArgumentException(SESSION_ID_PARAM + " parameter contains an invalid session token");
            }

            if (getFilePath() == null)
            {
                throw new IllegalArgumentException(FILE_PATH_PARAM + " parameter cannot be null");
            }

            if (getFilePath().contains("../"))
            {
                throw new IOExceptionUnchecked(FILE_PATH_PARAM + " parameter must not contain '../'");
            }

        }

    }

    private static class DownloadResponse
    {

        private final HttpServletResponse response;

        public DownloadResponse(HttpServletResponse response)
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
