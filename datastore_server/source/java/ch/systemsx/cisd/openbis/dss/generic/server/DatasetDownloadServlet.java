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

import static ch.systemsx.cisd.openbis.dss.generic.server.DatasetDownloadService.APPLICATION_CONTEXT_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends HttpServlet
{
    private static final String TEXT_MODE_DISPLAY = "txt";

    static final String DATA_SET_KEY = "data-set";

    static final String DATASET_CODE_KEY = "dataSetCode";

    static final String SESSION_ID_KEY = "sessionID";

    static final String DISPLAY_MODE_KEY = "mode";

    static final String BINARY_CONTENT_TYPE = "binary";

    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadServlet.class);

    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatasetDownloadServlet.class);

    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>()
        {
            public int compare(File file1, File file2)
            {
                return createSortableName(file1).compareTo(createSortableName(file2));
            }

            private String createSortableName(File file)
            {
                return (file.isDirectory() ? "D" : "F") + file.getName().toUpperCase();
            }
        };

    private ApplicationContext applicationContext;

    public DatasetDownloadServlet()
    {
    }

    DatasetDownloadServlet(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        try
        {
            ServletContext context = servletConfig.getServletContext();
            applicationContext = (ApplicationContext) context.getAttribute(APPLICATION_CONTEXT_KEY);
        } catch (Exception ex)
        {
            notificationLog.fatal("Failure during '" + servletConfig.getServletName()
                    + "' servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    // helper class to store parsed URL request
    private static class RequestParams
    {
        private final String dataSetCode;

        private final String pathInfo;

        private final String sessionIdOrNull;

        private final boolean isPlainTextMode;

        private final String urlPrefixWithDataset;

        public RequestParams(String dataSetCode, String pathInfo, String sessionIdOrNull,
                String urlPrefixWithDataset, boolean isPlainTextMode)
        {
            this.dataSetCode = dataSetCode;
            this.pathInfo = pathInfo;
            this.sessionIdOrNull = sessionIdOrNull;
            this.urlPrefixWithDataset = urlPrefixWithDataset;
            this.isPlainTextMode = isPlainTextMode;
        }

        public String getDataSetCode()
        {
            return dataSetCode;
        }

        public String getPathInfo()
        {
            return pathInfo;
        }

        public String tryGetSessionId()
        {
            return sessionIdOrNull;
        }

        public boolean isPlainTextMode()
        {
            return isPlainTextMode;
        }

        public String getURLPrefix()
        {
            return urlPrefixWithDataset;
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        IRendererFactory rendererFactory = null;
        try
        {
            RequestParams requestParams =
                    parseRequestURL(request, applicationContext.getApplicationName());
            rendererFactory = createRendererFactory(requestParams.isPlainTextMode());

            obtainDataSetFromServer(requestParams.getDataSetCode(),
                    requestParams.tryGetSessionId(), request);

            HttpSession session = request.getSession(false);
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                printResponse(response, rendererFactory, requestParams, session);
            }
        } catch (Exception e)
        {
            if (rendererFactory == null)
            {
                rendererFactory = new PlainTextRendererFactory();
            }
            printError(rendererFactory, request, response, e);
        }
    }

    private void printResponse(final HttpServletResponse response,
            IRendererFactory rendererFactory, RequestParams requestParams, HttpSession session)
            throws UnsupportedEncodingException, IOException
    {
        String dataSetCode = requestParams.getDataSetCode();
        ExternalDataPE dataSet = tryToGetDataSet(session, dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set '" + dataSetCode + "'.");
        }
        File rootDir = createDataSetRootDirectory(dataSet);
        RenderingContext context =
                new RenderingContext(rootDir, requestParams.getURLPrefix(), requestParams
                        .getPathInfo());
        renderPage(rendererFactory, response, dataSet, context);
    }

    private IRendererFactory createRendererFactory(boolean plainTextMode)
    {
        if (plainTextMode)
        {
            return new PlainTextRendererFactory();
        } else
        {
            return new HTMLRendererFactory();
        }
    }

    private static RequestParams parseRequestURL(HttpServletRequest request, String applicationName)
            throws UnsupportedEncodingException
    {
        final String urlPrefix = "/" + applicationName + "/";
        final String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
        if (requestURI.startsWith(urlPrefix) == false)
        {
            throw new EnvironmentFailureException("Request URI '" + requestURI
                    + "' expected to start with '" + urlPrefix + "'.");
        }
        final String fullPathInfo = requestURI.substring(urlPrefix.length());
        final int indexOfFirstSeparator = fullPathInfo.indexOf('/');
        final String dataSetCode;
        final String pathInfo;
        if (indexOfFirstSeparator < 0)
        {
            dataSetCode = fullPathInfo;
            pathInfo = "";
        } else
        {
            dataSetCode = fullPathInfo.substring(0, indexOfFirstSeparator);
            pathInfo = fullPathInfo.substring(indexOfFirstSeparator + 1);
        }
        final String urlPrefixWithDataset =
                requestURI.substring(0, requestURI.length() - pathInfo.length());

        final String sessionIDOrNull = request.getParameter(SESSION_ID_KEY);
        final String displayMode = request.getParameter(DISPLAY_MODE_KEY);
        final boolean isTextMode = (displayMode != null && displayMode.equals(TEXT_MODE_DISPLAY));

        return new RequestParams(dataSetCode, pathInfo, sessionIDOrNull, urlPrefixWithDataset,
                isTextMode);
    }

    private void printError(IRendererFactory rendererFactory, final HttpServletRequest request,
            final HttpServletResponse response, Exception exception) throws IOException
    {
        if (exception instanceof UserFailureException == false)
        {
            StringBuffer url = request.getRequestURL();
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString))
            {
                url.append("?").append(queryString);
            }
            operationLog.error("Request " + url + " caused an exception: ", exception);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info("User failure: " + exception.getMessage());
        }
        String message = exception.getMessage();
        String errorText = StringUtils.isBlank(message) ? exception.toString() : message;
        IErrorRenderer errorRenderer = rendererFactory.createErrorRenderer();
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = response.getWriter();
        errorRenderer.setWriter(writer);
        errorRenderer.printErrorMessage(errorText);
        writer.flush();
        writer.close();
    }

    private void renderPage(IRendererFactory rendererFactory, HttpServletResponse response,
            ExternalDataPE dataSet, RenderingContext renderingContext) throws IOException
    {
        File file = renderingContext.getFile();
        if (file.exists() == false)
        {
            throw new EnvironmentFailureException("File '" + file.getName() + "' does not exist.");
        }
        if (file.isDirectory())
        {
            createPage(rendererFactory, response, dataSet, renderingContext, file);
        } else
        {
            deliverFile(response, dataSet, file);
        }
    }

    private void createPage(IRendererFactory rendererFactory, HttpServletResponse response,
            ExternalDataPE dataSet, RenderingContext renderingContext, File file)
            throws IOException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSet.getCode() + "' show directory "
                    + file.getAbsolutePath());
        }
        IDirectoryRenderer directoryRenderer =
                rendererFactory.createDirectoryRenderer(renderingContext);
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            directoryRenderer.setWriter(writer);
            directoryRenderer.printHeader(dataSet);
            String relativeParentPath = renderingContext.getRelativeParentPath();
            if (relativeParentPath != null)
            {
                directoryRenderer.printLinkToParentDirectory(relativeParentPath);
            }
            File[] children = file.listFiles();
            Arrays.sort(children, FILE_COMPARATOR);
            for (File child : children)
            {
                String name = child.getName();
                File rootDir = renderingContext.getRootDir();
                String relativePath = FileUtilities.getRelativeFile(rootDir, child);
                String normalizedRelativePath = relativePath.replace('\\', '/');
                if (child.isDirectory())
                {
                    directoryRenderer.printDirectory(name, normalizedRelativePath);
                } else
                {
                    directoryRenderer.printFile(name, normalizedRelativePath, child.length());
                }
            }
            directoryRenderer.printFooter();
            writer.flush();

        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private void deliverFile(final HttpServletResponse response, ExternalDataPE dataSet, File file)
            throws IOException, FileNotFoundException
    {
        long size = file.length();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSet.getCode() + "' deliver file "
                    + file.getAbsolutePath() + " (" + size + " bytes).");
        }
        response.setContentLength((int) size);
        response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
        ServletOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        response.setContentType(BINARY_CONTENT_TYPE);
        try
        {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, outputStream);
        } finally
        {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void printSessionExpired(final HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.write("<html><body>Download session expired.</body></html>");
        writer.flush();
        writer.close();
    }

    private void obtainDataSetFromServer(String dataSetCode, String sessionIdOrNull,
            final HttpServletRequest request)
    {
        if (sessionIdOrNull != null)
        {
            IETLLIMSService dataSetService = applicationContext.getDataSetService();
            ExternalDataPE dataSet = dataSetService.tryGetDataSet(sessionIdOrNull, dataSetCode);
            if (operationLog.isInfoEnabled())
            {
                String actionDesc = (dataSet != null) ? "obtained from" : "not found in";
                operationLog.info(String.format("Data set '%s' %s openBIS server.", dataSetCode,
                        actionDesc));
            }
            HttpSession session = request.getSession(true);
            ConfigParameters configParameters = applicationContext.getConfigParameters();
            session.setMaxInactiveInterval(configParameters.getSessionTimeout());
            if (dataSet != null)
            {
                putDataSetToMap(session, dataSetCode, dataSet);
            }
        }
    }

    private File createDataSetRootDirectory(ExternalDataPE dataSet)
    {
        String path = dataSet.getLocation();
        LocatorTypePE locatorType = dataSet.getLocatorType();
        if (locatorType.getCode().equals(LocatorType.DEFAULT_LOCATOR_TYPE_CODE))
        {
            path = applicationContext.getConfigParameters().getStorePath() + "/" + path;
        }
        File dataSetRootDirectory = new File(path);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new UserFailureException("Data set '" + dataSet.getCode()
                    + "' not found in store at '" + dataSetRootDirectory.getAbsolutePath() + "'.");
        }
        return dataSetRootDirectory;
    }

    private void putDataSetToMap(HttpSession session, String dataSetCode, ExternalDataPE dataSet)
    {
        getDataSets(session).put(dataSetCode, dataSet);
    }

    private ExternalDataPE tryToGetDataSet(HttpSession session, String dataSetCode)
    {
        return getDataSets(session).get(dataSetCode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ExternalDataPE> getDataSets(HttpSession session)
    {
        Map<String, ExternalDataPE> map =
                (Map<String, ExternalDataPE>) session.getAttribute(DATA_SET_KEY);
        if (map == null)
        {
            map = new HashMap<String, ExternalDataPE>();
            session.setAttribute(DATA_SET_KEY, map);
        }
        return map;
    }

}
