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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Superclass for servlets supporting file download.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractFileDownloadServlet extends AbstractServlet
{
    protected AbstractFileDownloadServlet()
    {
        setRequireSession(true);
    }

    abstract protected FileContent getFileContent(final HttpServletRequest request)
            throws Exception;

    @Override
    protected void respondToRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception, IOException
    {
        FileContent fileContent = getFileContent(request);
        if (fileContent != null)
        {
            response.setContentLength(fileContent.getContent().length);
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileContent.getFileName() + "\"");
            final ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(fileContent.getContent());
            outputStream.flush();
            outputStream.close();
        }
    }

    //
    // Helper classes
    //

    public final static class FileContent
    {
        private final byte[] content;

        private final String fileName;

        public FileContent(final byte[] content, final String fileName)
        {
            this.content = content;
            this.fileName = fileName;
        }

        public final byte[] getContent()
        {
            return content;
        }

        public final String getFileName()
        {
            return fileName;
        }
    }
}
