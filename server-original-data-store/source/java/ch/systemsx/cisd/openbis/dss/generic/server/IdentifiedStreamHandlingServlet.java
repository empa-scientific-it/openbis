/*
 * Copyright 2011 ETH Zuerich, CISD
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Delivers the content of input streams from a {@link IStreamRepository}.
 *
 * @author Franz-Josef Elmer
 */
public class IdentifiedStreamHandlingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    public static final String SERVLET_NAME = "stream-content";

    public static final String STREAM_REPOSITORY_BEAN_ID = "stream-repository";

    public static final String STREAM_ID_PARAMETER_KEY = "streamID";

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            IdentifiedStreamHandlingServlet.class);

    private IStreamRepository streamRepository;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        BeanFactory applicationContext =
                (BeanFactory) context
                        .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        streamRepository = (IStreamRepository) applicationContext.getBean(STREAM_REPOSITORY_BEAN_ID);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        String streamID = req.getParameter(STREAM_ID_PARAMETER_KEY);
        InputStreamWithPath streamWithPath = streamRepository.getStream(streamID);
        String path = streamWithPath.getPath();
        String fileName = path;
        int indexOfLastSeparator = path.lastIndexOf('/');
        if (indexOfLastSeparator >= 0)
        {
            fileName = path.substring(indexOfLastSeparator + 1);
        }
        resp.setHeader("Content-Disposition", "inline; filename=" + fileName);

        resp.setContentType(Utils.getMimeType(fileName, true));
        InputStream stream = streamWithPath.getInputStream();
        ServletOutputStream outputStream = resp.getOutputStream();
        try
        {
            IOUtils.copyLarge(stream, outputStream);
        } finally
        {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(outputStream);
        }
    }

}
