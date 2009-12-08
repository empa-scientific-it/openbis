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

import static ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer.APPLICATION_CONTEXT_KEY;
import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends HttpServlet
{
    private static final class Size
    {
        private final int width;

        private final int height;

        Size(int width, int height)
        {
            this.width = width;
            this.height = height;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

    private static final Size DEFAULT_THUMBNAIL_SIZE = new Size(100, 60);

    private static final String TEXT_MODE_DISPLAY = "txt";

    private static final String HTML_MODE_DISPLAY = "html";

    private static final String THUMBNAIL_MODE_DISPLAY = "thumbnail";

    static final String DATA_SET_KEY = "data-set";

    static final String DATASET_CODE_KEY = "dataSetCode";

    static final String SESSION_ID_KEY = "sessionID";

    static final String DISPLAY_MODE_KEY = "mode";

    static final String BINARY_CONTENT_TYPE = "binary";

    static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";

    private static final MimetypesFileTypeMap MIMETYPES = new MimetypesFileTypeMap();

    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadServlet.class);

    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatasetDownloadServlet.class);

    // @Private
    static String getMimeType(File f, boolean plainTextMode)
    {
        if (plainTextMode)
        {
            return BINARY_CONTENT_TYPE;
        } else
        {
            if (FilenameUtils.getExtension(f.getName()).length() == 0)
            {
                return PLAIN_TEXT_CONTENT_TYPE;
            } else
            {
                return MIMETYPES.getContentType(f.getName().toLowerCase());
            }
        }
    }

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

        private final String urlPrefixWithDataset;

        private final String displayMode;

        public RequestParams(String dataSetCode, String pathInfo, String sessionIdOrNull,
                String urlPrefixWithDataset, String displayMode)
        {
            this.dataSetCode = dataSetCode;
            this.pathInfo = pathInfo;
            this.sessionIdOrNull = sessionIdOrNull;
            this.urlPrefixWithDataset = urlPrefixWithDataset;
            this.displayMode = displayMode;
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

        public String getDisplayMode()
        {
            return displayMode;
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
                    parseRequestURL(request, DATA_STORE_SERVER_WEB_APPLICATION_NAME);
            rendererFactory = createRendererFactory(requestParams.getDisplayMode());

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
        ExternalData dataSet = tryToGetDataSet(session, dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set '" + dataSetCode + "'.");
        }
        File rootDir = createDataSetRootDirectory(dataSet);
        RenderingContext context =
                new RenderingContext(rootDir, requestParams.getURLPrefix(), requestParams
                        .getPathInfo());
        renderPage(rendererFactory, response, dataSet, context, requestParams);
    }

    private IRendererFactory createRendererFactory(String displayMode)
    {
        if (displayMode.equals(HTML_MODE_DISPLAY))
        {
            return new HTMLRendererFactory();
        } else
        {
            return new PlainTextRendererFactory();
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
        String displayMode = request.getParameter(DISPLAY_MODE_KEY);
        if (displayMode == null)
        {
            displayMode = HTML_MODE_DISPLAY;
        }

        return new RequestParams(dataSetCode, pathInfo, sessionIDOrNull, urlPrefixWithDataset,
                displayMode);
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
            ExternalData dataSet, RenderingContext renderingContext, RequestParams requestParams)
            throws IOException
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
            deliverFile(response, dataSet, file, requestParams.getDisplayMode());
        }
    }

    private void createPage(IRendererFactory rendererFactory, HttpServletResponse response,
            ExternalData dataSet, RenderingContext renderingContext, File file) throws IOException
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

    private void deliverFile(final HttpServletResponse response, ExternalData dataSet, File file,
            String displayMode) throws IOException, FileNotFoundException
    {
        ServletOutputStream outputStream = null;
        InputStream inputStream = null;
        String contentType;
        int size;
        String infoPostfix;
        if (displayMode.startsWith(THUMBNAIL_MODE_DISPLAY))
        {
            Size thumbnailSize = extractSize(displayMode);
            BufferedImage image = ImageUtil.loadImage(file);
            int width = thumbnailSize.getWidth();
            int height = thumbnailSize.getHeight();
            BufferedImage thumbnail = ImageUtil.createThumbnail(image, width, height);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "png", output);
            size = output.size();
            infoPostfix = " as a thumbnail.";
            contentType = "image/png";
            inputStream = new ByteArrayInputStream(output.toByteArray());
        } else
        {
            size = (int) file.length();
            infoPostfix = " (" + size + " bytes).";
            contentType = getMimeType(file, displayMode.equals(TEXT_MODE_DISPLAY));
            inputStream = new FileInputStream(file);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSet.getCode() + "' deliver file "
                    + file.getAbsolutePath() + infoPostfix);
        }
        response.setContentLength(size);
        response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
        response.setContentType(contentType);
        try
        {
            outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private Size extractSize(String displayMode)
    {
        String sizeDescription = displayMode.substring(THUMBNAIL_MODE_DISPLAY.length());
        int indexOfSeparator = sizeDescription.indexOf('x');
        if (indexOfSeparator < 0)
        {
            return DEFAULT_THUMBNAIL_SIZE;
        }
        try
        {
            int width = Integer.parseInt(sizeDescription.substring(0, indexOfSeparator));
            int height = Integer.parseInt(sizeDescription.substring(indexOfSeparator + 1));
            return new Size(width, height);
        } catch (NumberFormatException ex)
        {
            operationLog.warn("Invalid numbers in displayMode '" + displayMode
                    + "'. Default thumbnail size is used.");
            return DEFAULT_THUMBNAIL_SIZE;
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
        HttpSession session = request.getSession(false);
        if (session != null && tryToGetDataSet(session, dataSetCode) != null)
        {
            return; // dataset is already in the cache
        }
        if (sessionIdOrNull != null)
        {
            IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
            ExternalData dataSet = dataSetService.tryGetDataSet(sessionIdOrNull, dataSetCode);
            if (operationLog.isInfoEnabled())
            {
                String actionDesc = (dataSet != null) ? "obtained from" : "not found in";
                operationLog.info(String.format("Data set '%s' %s openBIS server.", dataSetCode,
                        actionDesc));
            }

            ConfigParameters configParameters = applicationContext.getConfigParameters();
            if (session == null)
            {
                session = request.getSession(true);
                session.setMaxInactiveInterval(configParameters.getSessionTimeout());
            }
            if (dataSet != null)
            {
                putDataSetToMap(session, dataSetCode, dataSet);
            }
        }
    }

    private File createDataSetRootDirectory(ExternalData dataSet)
    {
        String path = dataSet.getLocation();
        LocatorType locatorType = dataSet.getLocatorType();
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

    private void putDataSetToMap(HttpSession session, String dataSetCode, ExternalData dataSet)
    {
        getDataSets(session).put(dataSetCode, dataSet);
    }

    private ExternalData tryToGetDataSet(HttpSession session, String dataSetCode)
    {
        return getDataSets(session).get(dataSetCode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ExternalData> getDataSets(HttpSession session)
    {
        Map<String, ExternalData> map =
                (Map<String, ExternalData>) session.getAttribute(DATA_SET_KEY);
        if (map == null)
        {
            map = new HashMap<String, ExternalData>();
            session.setAttribute(DATA_SET_KEY, map);
        }
        return map;
    }

}
