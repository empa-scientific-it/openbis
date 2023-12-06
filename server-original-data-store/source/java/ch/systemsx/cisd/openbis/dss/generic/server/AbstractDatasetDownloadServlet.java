/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
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
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.image.IntensityRescaling.IImageToPixelsConverter;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * Superclass for dataset download servlets. Provides functionality to deliver content of files and images, does not deal with browsing directories.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDatasetDownloadServlet extends HttpServlet
{
    protected static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractDatasetDownloadServlet.class);

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractDatasetDownloadServlet.class);

    private static final long serialVersionUID = 1L;

    private static final long IMAGE_CACHE_AGE_IN_SECONDS = 60 * 60 * 2;

    private static final Size DEFAULT_THUMBNAIL_SIZE = new Size(100, 60);

    private static final String THUMBNAIL_MODE_DISPLAY = "thumbnail";

    static final String DISPLAY_MODE_PARAM = "mode";

    static final String DATABASE_INSTANCE_SESSION_KEY = "database-instance";

    static final String DATA_SET_ACCESS_SESSION_KEY = "data-set-access";

    static final String DATA_SET_SESSION_KEY = "data-set";

    protected ApplicationContext applicationContext;

    public AbstractDatasetDownloadServlet()
    {
    }

    // for tests only
    AbstractDatasetDownloadServlet(ApplicationContext applicationContext)
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
            applicationContext =
                    (ApplicationContext) context
                            .getAttribute(DataStoreServer.APPLICATION_CONTEXT_KEY);

            // Look for the additional configuration parameters and initialize the servlet using
            // them
            Enumeration<String> e = servletConfig.getInitParameterNames();
            if (e.hasMoreElements())
                doSpecificInitialization(e, servletConfig);
        } catch (Exception ex)
        {
            notificationLog.fatal("Failure during '" + servletConfig.getServletName()
                    + "' servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            super.service(request, response);
        } finally
        {
            applicationContext.getShareIdManager().releaseLocks();
        }
    }

    /**
     * Do any additional initialization using information from the properties passed in. Subclasses may override.
     */
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        return;
    }

    protected final HttpSession tryGetOrCreateSession(final HttpServletRequest request,
            String sessionIdOrNull)
    {
        HttpSession session = request.getSession(false);

        if (session == null && sessionIdOrNull == null)
        {
            // a) The session is expired and b) we do not have openbis session id provided.
            // So a) metadata about datasets are not in the session and b) we cannot get them from
            // openbis.
            // CR, 2010-08-30, LMS-1706, Logging
            StringBuilder sb = new StringBuilder();
            sb.append("Could not create a servlet session since no existing servlet session is available, "
                    + "and the openBIS session ID was not provided as a parameter:");
            appendRequestParameters(request, sb);
            appendServletSessionTimeout(sb);
            operationLog.error(sb.toString());
            return null;
        }
        if (session == null)
        {
            session = request.getSession(true);
            ConfigParameters configParameters = applicationContext.getConfigParameters();
            session.setMaxInactiveInterval(configParameters.getSessionTimeout());

            // CR, 2010-08-30, LMS-1706, Logging
            StringBuilder sb = new StringBuilder();
            sb.append("Creating a new session with the following parameters:");
            appendRequestParameters(request, sb);
            appendServletSessionTimeout(sb);
            operationLog.info(sb.toString());
        }
        if (sessionIdOrNull != null)
        {
            session.setAttribute("openbis-session-id", sessionIdOrNull);
        }

        String sessionToken = session.getAttribute("openbis-session-id").toString();
        if (applicationContext.getSessionTokenCache().isValidSessionToken(sessionToken) == false)
        {
            return null;
        }
        return session;
    }

    private void appendServletSessionTimeout(StringBuilder sb)
    {
        sb.append(" Session Timeout: ");
        sb.append(applicationContext.getConfigParameters().getSessionTimeout());
        sb.append(" sec");
    }

    private void appendRequestParameters(final HttpServletRequest request, StringBuilder sb)
    {
        Enumeration<String> e = request.getParameterNames();
        sb.append(" [");
        while (e.hasMoreElements())
        {
            String name = e.nextElement();
            sb.append(name);
            sb.append("=");
            sb.append(request.getParameter(name));
            if (e.hasMoreElements())
            {
                sb.append(",");
            }
        }
        sb.append("]");
    }

    protected final static void printSessionExpired(final HttpServletResponse response)
            throws IOException
    {
        printErrorResponse(response, "Download session expired.");
    }

    protected final static void printErrorResponse(final HttpServletResponse response,
            String errorMessage) throws IOException
    {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.write("<html><body>" + errorMessage + "</body></html>");
        writer.flush();
        writer.close();
    }

    protected final void writeResponseContent(ResponseContentStream responseStream,
            final HttpServletResponse response) throws IOException
    {
        response.setHeader("Content-Disposition", responseStream.getHeaderContentDisposition());
        if (responseStream.getSize() <= Integer.MAX_VALUE)
        {
            response.setContentLength((int) responseStream.getSize());
        } else
        {
            response.addHeader("Content-Length", Long.toString(responseStream.getSize()));
        }

        if (Utils.CONTENT_TYPE_PNG.equals(responseStream.getContentType()))
        {
            response.addHeader("Cache-Control", "max-age=" + IMAGE_CACHE_AGE_IN_SECONDS);
        }

        response.setContentType(responseStream.getContentType());

        ServletOutputStream outputStream = null;
        InputStream content = responseStream.getInputStream();
        try
        {
            outputStream = response.getOutputStream();
            IOUtils.copy(content, outputStream);
        } finally
        {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected static final BufferedImage createThumbnail(IHierarchicalContentNode fileNode,
            Size thumbnailSize, IImageToPixelsConverter converterOrNull)
    {
        BufferedImage image = ImageUtil.loadImageForDisplay(fileNode, converterOrNull);
        return createThumbnail(image, thumbnailSize, converterOrNull);
    }

    protected static BufferedImage createThumbnail(BufferedImage image, Size thumbnailSize,
            IImageToPixelsConverter converterOrNull)
    {
        int width = thumbnailSize.getWidth();
        int height = thumbnailSize.getHeight();
        return ImageUtil.createThumbnailForDisplay(image, width, height, converterOrNull);
    }

    // if display mode describes a thumbnail return its expected size
    protected static Size tryAsThumbnailDisplayMode(String displayMode)
    {
        if (displayMode.startsWith(THUMBNAIL_MODE_DISPLAY))
        {
            return extractSize(displayMode);
        } else
        {
            return null;
        }
    }

    private static Size extractSize(String displayMode)
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

    protected final File getStoreRootPath()
    {
        return applicationContext.getConfigParameters().getStorePath();
    }

    // ---

    protected final DatabaseInstance getDatabaseInstance(HttpSession session)
    {
        DatabaseInstance databaseInstance =
                (DatabaseInstance) session.getAttribute(DATABASE_INSTANCE_SESSION_KEY);
        if (databaseInstance == null)
        {
            databaseInstance = applicationContext.getDataSetService().getHomeDatabaseInstance();
            session.setAttribute(DATABASE_INSTANCE_SESSION_KEY, databaseInstance);
        }
        return databaseInstance;
    }

    // ---

    protected final void ensureDatasetAccessible(String dataSetCode, HttpSession session,
            String sessionIdOrNull)
    {
        if (isDatasetAccessible(dataSetCode, sessionIdOrNull, session) == false)
        {
            throw new UserFailureException("Data set '" + dataSetCode + "' is not accessible.");
        }
    }

    private boolean isDatasetAccessible(String dataSetCode, String sessionIdOrNull,
            HttpSession session)
    {
        Boolean access = getDataSetAccess(session).get(dataSetCode);
        if (access == null)
        {
            if (tryToGetCachedDataSet(session, dataSetCode) != null)
            {
                return true; // access already checked and granted
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(
                        "Check access to the data set '%s' at openBIS server.", dataSetCode));
            }
            IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
            ensureSessionIdSpecified(sessionIdOrNull);
            try
            {
                dataSetService.checkDataSetAccess(sessionIdOrNull, dataSetCode);
                access = true;
            } catch (UserFailureException ex)
            {
                operationLog.error(String.format(
                        "Error when checking access to the data set '%s' at openBIS server: %s",
                        dataSetCode, ex.getMessage()));
                return false; // do not save this in cache, try to connect to AS next time
            }
            getDataSetAccess(session).put(dataSetCode, access);
        }
        return access;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getDataSetAccess(HttpSession session)
    {
        Map<String, Boolean> map =
                (Map<String, Boolean>) session.getAttribute(DATA_SET_ACCESS_SESSION_KEY);
        if (map == null)
        {
            map = new HashMap<String, Boolean>();
            session.setAttribute(DATA_SET_ACCESS_SESSION_KEY, map);
        }
        return map;
    }

    protected final AbstractExternalData tryToGetCachedDataSet(HttpSession session,
            String dataSetCode)
    {
        return getDataSets(session).get(dataSetCode);
    }

    @SuppressWarnings("unchecked")
    protected final Map<String, AbstractExternalData> getDataSets(HttpSession session)
    {
        // Need to synchronize on the class, since there could be multiple instances trying to
        // access this attribute.
        synchronized (AbstractDatasetDownloadServlet.class)
        {
            Map<String, AbstractExternalData> map =
                    (Map<String, AbstractExternalData>) session.getAttribute(DATA_SET_SESSION_KEY);
            if (map == null)
            {
                map = new HashMap<String, AbstractExternalData>();
                session.setAttribute(DATA_SET_SESSION_KEY, map);
            }
            return map;
        }
    }

    protected final void ensureSessionIdSpecified(String sessionIdOrNull)
    {
        if (sessionIdOrNull == null)
        {
            throw new EnvironmentFailureException("Session id not specified in the URL");
        }
    }

}
