/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3.upload;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.dss.generic.server.Utils;
import ch.systemsx.cisd.openbis.dss.generic.server.pat.PersonalAccessTokenConverterFromEncapsulatedService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * @author Ganime Betul Akin
 */
@RestController
public class StoreShareFileUploadServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            StoreShareFileUploadServlet.class);

    public static final String SESSION_ID_PARAM = Utils.SESSION_ID_PARAM;

    public static final String DATA_SET_TYPE_PARAM = "dataSetType";

    public static final String IGNORE_FILE_PATH_PARAM = "ignoreFilePath";

    public static final String FOLDER_PATH_PARAM = "folderPath";

    public static final String UPLOAD_ID_PARAM = "uploadID";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        StoreShareFileUploadRequest uploadRequest = new StoreShareFileUploadRequest(request);

        uploadRequest.validate();

        try
        {
            FileItemIterator iterator = uploadRequest.getFiles();

            if (false == iterator.hasNext())
            {
                throw new UserFailureException("Please upload at least one file");
            }

            PutDataSetService putService = (PutDataSetService) ServiceProvider.getDataStoreService().getPutDataSetService();

            while (iterator.hasNext())
            {
                FileItemStream file = null;
                InputStream stream = null;

                try
                {
                    file = iterator.next();
                    stream = file.openStream();

                    /*
                     * Most browsers send only base file names for files which were uploaded via a regular html form. Still, there are some browsers
                     * (e.g. Opera) that are known to send file paths as well. To handle all browsers consistently by default we ignore file paths
                     * even if they are given. We only use the file paths if it has been explicitly requested via <code>IGNORE_FILE_PATH_PARAM</code>
                     * request parameter. This may be handy in contexts where we have a full control over what gets sent to the servlet (e.g. from a
                     * Python script that makes http requests to the servlet).
                     */
                    String filePath = uploadRequest.isIgnoreFilePath() ? FilenameUtils.getName(file.getName()) : file.getName();

                    operationLog.info("Received file '" + filePath + "' for upload id '" + uploadRequest.getUploadId() + "' and data set type '"
                            + uploadRequest.getDataSetType() + "'");

                    putService.putFileToStoreShare(uploadRequest.getSessionId(), uploadRequest.getFolderPath(), filePath,
                            uploadRequest.getDataSetType(), uploadRequest.getUploadId(), stream);

                } finally
                {
                    IOUtils.closeQuietly(stream);
                }
            }
        } catch (Exception e)
        {
            operationLog.error(e.getMessage());
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private class StoreShareFileUploadRequest
    {

        private HttpServletRequest request;

        public StoreShareFileUploadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            String sessionId = request.getParameter(SESSION_ID_PARAM);
            sessionId = new PersonalAccessTokenConverterFromEncapsulatedService().convert(sessionId);
            return sessionId;
        }

        public String getDataSetType()
        {
            return request.getParameter(DATA_SET_TYPE_PARAM);
        }

        public String getUploadId()
        {
            return request.getParameter(UPLOAD_ID_PARAM);
        }

        public boolean isIgnoreFilePath()
        {
            String str = request.getParameter(IGNORE_FILE_PATH_PARAM);

            if (str == null || str.isEmpty())
            {
                return true;
            } else
            {
                return Boolean.valueOf(str);
            }
        }

        public String getFolderPath()
        {
            return request.getParameter(FOLDER_PATH_PARAM);
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
                throw new UserFailureException("The file upload accepts only multipart requests");
            }
        }

    }
}
