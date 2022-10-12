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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.common.servlet.HttpServletRequestUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * Servlet that handles the upload of files to the session workspace when Uploader2 library is in HTML5 FileAPI mode. Files are uploaded slice by
 * slice to give a feedback about the upload progress.
 * <ul>
 * <li>Accepted HTTP methods: POST</li>
 * <li>HTTP request parameters: sessionID (String required), id (Integer required), startByte (Integer required), endByte (Integer required), filename
 * (String required)</li>
 * <li>HTTP request body: file slice to be uploaded</li>
 * </ul>
 * 
 * @author pkupczyk
 */
public class SessionWorkspaceFileUploadServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private static final String ID_PARAM = "id";

    private static final String START_BYTE_PARAM = "startByte";

    private static final String END_BYTE_PARAM = "endByte";

    private static final String FILE_NAME_PARAM = "filename";

    private static final String SIZE_PARAM = "size";

    private static final String STATUS_PARAM = "status";

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
        SessionWorkspaceFileUploadRequest uploadRequest =
                new SessionWorkspaceFileUploadRequest(request);

        uploadRequest.validate();

        long bytes =
                service.putFileSliceToSessionWorkspace(uploadRequest.getSessionId(),
                        uploadRequest.getFileName(), uploadRequest.getStartByte(),
                        uploadRequest.getFile());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(ID_PARAM, uploadRequest.getId());
        resultMap.put(START_BYTE_PARAM, uploadRequest.getStartByte());
        resultMap.put(END_BYTE_PARAM, uploadRequest.getEndByte());
        resultMap.put(FILE_NAME_PARAM, uploadRequest.getFileName());
        resultMap.put(SIZE_PARAM, bytes);
        resultMap.put(STATUS_PARAM, "ok");

        SessionWorkspaceFileUploadResponse uploadResponse =
                new SessionWorkspaceFileUploadResponse(response);

        uploadResponse.writeJson(resultMap);
    }

    private class SessionWorkspaceFileUploadRequest
    {

        private HttpServletRequest request;

        public SessionWorkspaceFileUploadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            String sessionId = HttpServletRequestUtils.getStringParameter(request, Utils.SESSION_ID_PARAM);
            sessionId = ServiceProvider.getPersonalAccessTokenConverter().convert(sessionId);
            return sessionId;
        }

        public Integer getId()
        {
            return HttpServletRequestUtils.getIntegerParameter(request, ID_PARAM);
        }

        public Long getStartByte()
        {
            return HttpServletRequestUtils.getNumberParameter(request, START_BYTE_PARAM);
        }

        public Long getEndByte()
        {
            return HttpServletRequestUtils.getNumberParameter(request, END_BYTE_PARAM);
        }

        public String getFileName()
        {
            return HttpServletRequestUtils.getStringParameter(request, FILE_NAME_PARAM);
        }

        public InputStream getFile() throws IOException
        {
            return request.getInputStream();
        }

        public void validate()
        {
            if (getSessionId() == null)
            {
                throw new IllegalArgumentException(Utils.SESSION_ID_PARAM
                        + " parameter cannot be null");
            }
            if (getId() == null)
            {
                throw new IllegalArgumentException(ID_PARAM + " parameter cannot be null");
            }
            if (getStartByte() == null)
            {
                throw new IllegalArgumentException(START_BYTE_PARAM + " parameter cannot be null");
            }
            if (getEndByte() == null)
            {
                throw new IllegalArgumentException(END_BYTE_PARAM + " parameter cannot be null");
            }
            if (getFileName() == null)
            {
                throw new IllegalArgumentException(FILE_NAME_PARAM + " parameter cannot be null");
            }
        }

    }

    private class SessionWorkspaceFileUploadResponse
    {

        private HttpServletResponse response;

        public SessionWorkspaceFileUploadResponse(HttpServletResponse response)
        {
            this.response = response;
        }

        public void writeJson(Object object) throws IOException
        {
            response.setContentType("text/json");
            response.setStatus(HttpServletResponse.SC_OK);

            String json = new ObjectMapper().writeValueAsString(object);

            if (json != null)
            {
                response.getWriter().write(json);
            }
        }

    }

}
