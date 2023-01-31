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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * Servlet that handles the upload of files to the session workspace when Uploader2 library is in HTML4 form fallback mode. Files are uploaded in one
 * multipart request therefore no information about progress is provided. This servlet is currently NOT USED as Uploader2 library cannot handle IE in
 * the fallback mode yet.
 * <ul>
 * <li>Accepted HTTP methods: multipart POST</li>
 * <li>HTTP request parameters: sessionID (String required)</li>
 * <li>HTTP request body: files to be uploaded</li>
 * </ul>
 * 
 * @author pkupczyk
 */
public final class SessionWorkspaceFormUploadServlet extends HttpServlet
{

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        SessionWorkspaceFormUploadRequest uploadRequest =
                new SessionWorkspaceFormUploadRequest(request);

        uploadRequest.validate();

        try
        {
            FileItemIterator iterator = uploadRequest.getFiles();

            while (iterator.hasNext())
            {
                FileItemStream file = null;
                InputStream stream = null;

                try
                {
                    file = iterator.next();
                    stream = file.openStream();
                    service.putFileToSessionWorkspace(uploadRequest.getSessionId(), file.getName(),
                            stream);
                } finally
                {
                    IOUtils.closeQuietly(stream);
                }
            }

        } catch (FileUploadException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }

    }

    private class SessionWorkspaceFormUploadRequest
    {

        private HttpServletRequest request;

        public SessionWorkspaceFormUploadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            return request.getParameter(Utils.SESSION_ID_PARAM);
        }

        public FileItemIterator getFiles() throws FileUploadException, IOException
        {
            ServletFileUpload upload = new ServletFileUpload();
            return upload.getItemIterator(request);
        }

        public void validate()
        {
            if (ServletFileUpload.isMultipartContent(request) == false)
            {
                throw new IllegalArgumentException(
                        "The session workspace form upload accepts only multipart requests");
            }
            if (getSessionId() == null)
            {
                throw new IllegalArgumentException(Utils.SESSION_ID_PARAM
                        + " parameter cannot be null");
            }
        }

    }

}
