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

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetAwareHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.util.HttpRequestUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends AbstractDatasetDownloadServlet
{
    private static final String PATH_INFO_DB = "path-info-db";

    private static final String DATA_SOURCES = "data-sources";

    private static final long serialVersionUID = 1L;

    private static final String HTML_MODE_DISPLAY = "html";

    private static final String TEXT_MODE_DISPLAY = "txt";

    private static final String SIMPLE_HTML_MODE_DISPLAY = "simpleHtml";

    static final String AUTO_RESOLVE_KEY = "autoResolve";

    static final String FORCE_AUTO_RESOLVE_KEY = "forceAutoResolve";

    static final String MAIN_DATA_SET_PATH_KEY = "mdsPath";

    static final String MAIN_DATA_SET_PATTERN_KEY = "mdsPattern";

    static final String DISABLE_LINKS = "disableLinks";

    static final String IS_LINK_DATASET = "is_link_data";

    private static String DOWNLOAD_URL;

    static void setDownloadUrl(String downloadUrl)
    {
        if (downloadUrl.endsWith("/"))
        {
            DOWNLOAD_URL = downloadUrl.substring(0, downloadUrl.length() - 1);
        } else
        {
            DOWNLOAD_URL = downloadUrl;
        }
    }

    public DatasetDownloadServlet()
    {
    }

    DatasetDownloadServlet(ApplicationContext applicationContext)
    {
        super(applicationContext);
    }

    // helper class to store parsed URL request
    private static class RequestParams
    {
        private final String dataSetCode;

        private final String pathInfo;

        private final String sessionIdOrNull;

        private final String urlPrefixWithDataset;

        private final String displayMode;

        private final boolean autoResolve;

        private final String mainDataSetPathOrNull;

        private final String mainDataSetPatternOrNull;

        private final boolean forceAutoResolve;

        private final boolean disableLinks;

        private final String referrerDomain;

        private final boolean forceReferrerDomainOnEmptyDownloadURL;

        private final boolean isLinkData;

        public RequestParams(String dataSetCode, String pathInfo, String sessionIdOrNull,
                String urlPrefixWithDataset, String displayMode, boolean autoResolve,
                String mainDataSetPathOrNull, String mainDataSetPatternOrNull,
                boolean forceAutoResolve, boolean disableLinks, String referrerDomain, boolean forceReferrerDomainOnEmptyDownloadURL,
                boolean isLinkData)
        {
            this.dataSetCode = dataSetCode;
            this.pathInfo = pathInfo;
            this.sessionIdOrNull = sessionIdOrNull;
            this.urlPrefixWithDataset = urlPrefixWithDataset;
            this.displayMode = displayMode;
            this.autoResolve = autoResolve;
            this.mainDataSetPathOrNull = mainDataSetPathOrNull;
            this.mainDataSetPatternOrNull = mainDataSetPatternOrNull;
            this.forceAutoResolve = forceAutoResolve;
            this.disableLinks = disableLinks;
            this.referrerDomain = referrerDomain;
            this.forceReferrerDomainOnEmptyDownloadURL = forceReferrerDomainOnEmptyDownloadURL;
            this.isLinkData = isLinkData;
        }

        public boolean isAutoResolve()
        {
            return autoResolve;
        }

        public boolean isForceAutoResolve()
        {
            return forceAutoResolve;
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

        public String tryGetMainDataSetPath()
        {
            return mainDataSetPathOrNull;
        }

        public String tryGetMainDataSetPattern()
        {
            return mainDataSetPatternOrNull;
        }

        public boolean isDisableLinks()
        {
            return disableLinks;
        }

        public String getReferrerDomain()
        {
            return referrerDomain;
        }

        public boolean isForceReferrerDomainOnEmptyDownloadURL()
        {
            return forceReferrerDomainOnEmptyDownloadURL;
        }

        public boolean isLinkData()
        {
            return isLinkData;
        }

    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        IRendererFactory rendererFactory = null;
        boolean isLinkData = false;
        try
        {
            RequestParams requestParams = parseRequestURL(request, DATA_STORE_SERVER_WEB_APPLICATION_NAME);
            isLinkData = requestParams.isLinkData();
            rendererFactory = createRendererFactory(requestParams.getDisplayMode());

            HttpSession session = tryGetOrCreateSession(request, requestParams.tryGetSessionId());
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

            String dataSources = DataStoreServer.getConfigParameters().getProperties().getProperty(DATA_SOURCES);
            boolean isPathInfoEnabled = dataSources != null && dataSources.contains(PATH_INFO_DB);
            if (!isPathInfoEnabled)
            {
                printEmptyPage(rendererFactory, request, response);
            } else if (isLinkData)
            {
                printMessage("No content information is stored with this linked dataset.", rendererFactory, response);
            } else
            {
                printError(rendererFactory, request, response, e);
            }

        }

    }

    private void printResponse(final HttpServletResponse response,
            IRendererFactory rendererFactory, RequestParams requestParams, HttpSession session)
            throws UnsupportedEncodingException, IOException
    {
        String dataSetCode = requestParams.getDataSetCode();
        RenderingContext context = createRenderingContext(requestParams, dataSetCode, session);
        try
        {
            renderPage(rendererFactory, response, dataSetCode, context, requestParams, session);
        } finally
        {
            // close the root content when we are done working with it
            if (context != null && context.getRootContent() != null)
            {
                context.getRootContent().close();
            }
        }
    }

    private RenderingContext createRenderingContext(RequestParams requestParams,
            String dataSetCode, HttpSession session)
    {
        IHierarchicalContent rootContent = null;
        try
        {
            String sessionTokenOrNull = requestParams.tryGetSessionId();
            IHierarchicalContentProvider contentProvider =
                    applicationContext.getHierarchicalContentProvider(sessionTokenOrNull);
            rootContent = contentProvider.asContent(dataSetCode);
        } catch (IllegalArgumentException ex)
        {
            throw new UserFailureException(ex.getMessage());
        }
        try
        {
            RenderingContext context =
                    new RenderingContext(rootContent, requestParams.getURLPrefix(),
                            requestParams.getPathInfo(), requestParams.tryGetSessionId());
            return context;
        } catch (IllegalArgumentException ex)
        {
            if (rootContent != null)
            {
                rootContent.close();
            }
            throw ex; // rethrow
        }
    }

    private IRendererFactory createRendererFactory(String displayMode)
    {
        if (displayMode.equals(SIMPLE_HTML_MODE_DISPLAY) || displayMode.equals(HTML_MODE_DISPLAY))
        {
            return new SimpleHTMLRendererFactory();
        } else
        {
            return new PlainTextRendererFactory();
        }
    }

    private static RequestParams parseRequestURL(HttpServletRequest request, String applicationName)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final String urlPrefix = "/" + applicationName + "/";
        final String requestURI = (new URI(request.getRequestURI())).getPath();
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

        String sessionIDOrNull = request.getParameter(Utils.SESSION_ID_PARAM);
        sessionIDOrNull = ServiceProvider.getPersonalAccessTokenConverter().convert(sessionIDOrNull);

        String displayMode = getDisplayMode(request);

        Boolean autoResolveOrNull = Boolean.valueOf(request.getParameter(AUTO_RESOLVE_KEY));
        boolean autoResolve = autoResolveOrNull != null && autoResolveOrNull;
        Boolean forceAutoResolveOrNull =
                Boolean.valueOf(request.getParameter(FORCE_AUTO_RESOLVE_KEY));
        boolean forceAutoResolve = forceAutoResolveOrNull != null && forceAutoResolveOrNull;
        String mainDataSetPathOrNull = request.getParameter(MAIN_DATA_SET_PATH_KEY);
        String mainDataSetPatternOrNull = request.getParameter(MAIN_DATA_SET_PATTERN_KEY);
        boolean shouldSetMainDataSetParamsToNull =
                autoResolve == false && forceAutoResolve == false;
        Boolean disableLinks = Boolean.valueOf(request.getParameter(DISABLE_LINKS));

        if (shouldSetMainDataSetParamsToNull || StringUtils.isBlank(mainDataSetPathOrNull))
        {
            mainDataSetPathOrNull = null;
        }
        if (shouldSetMainDataSetParamsToNull || StringUtils.isBlank(mainDataSetPatternOrNull))
        {
            mainDataSetPatternOrNull = null;
        }

        // When using Apache mod_rewrite, clients will get redirected from https to http because openBIS is actually runs under a http connector
        // Issue explained here http://stackoverflow.com/questions/18891210/how-to-reproduce-sendredirect-issue-https-change-to-http
        // The next code is used to detect this case and the referrerDomain will be used for redirects when DOWNLOAD_URL is empty
        String requestScheme = request.getScheme();
        String referrer = request.getHeader("referer");
        String referrerScheme = null;
        String referrerDomain = null;
        boolean forceReferrerDomainOnEmptyDownloadURL = false;

        if (referrer != null)
        {
            int indexOfDomainEnd = StringUtils.ordinalIndexOf(referrer, "/", 3);
            if (indexOfDomainEnd != -1)
            {
                referrerDomain = referrer.substring(0, indexOfDomainEnd);
                int indexOfSchemeEnd = referrerDomain.indexOf("://");
                if (indexOfSchemeEnd != -1)
                {
                    referrerScheme = referrerDomain.substring(0, indexOfSchemeEnd);
                }

            }
        }

        if (requestScheme != null &&
                referrerScheme != null &&
                requestScheme.equals(referrerScheme) == false)
        {
            forceReferrerDomainOnEmptyDownloadURL = true;
        }
        //

        String isLink = request.getParameter(IS_LINK_DATASET);
        boolean isLinkData = "true".equals(isLink);

        return new RequestParams(dataSetCode, pathInfo, sessionIDOrNull, urlPrefixWithDataset,
                displayMode, autoResolve, mainDataSetPathOrNull, mainDataSetPatternOrNull,
                forceAutoResolve, disableLinks, referrerDomain, forceReferrerDomainOnEmptyDownloadURL, isLinkData);
    }

    private static String getDisplayMode(HttpServletRequest request)
    {
        String displayMode = request.getParameter(DISPLAY_MODE_PARAM);
        if (displayMode == null)
        {
            displayMode = HTML_MODE_DISPLAY;
        }
        return displayMode;
    }

    private void printEmptyPage(IRendererFactory rendererFactory, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException
    {
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = response.getWriter();
        writer.flush();
        writer.close();
    }

    private void printMessage(String message, IRendererFactory rendererFactory,
            final HttpServletResponse response) throws IOException
    {
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = response.getWriter();
        IMessageRenderer renderer = rendererFactory.createMessageRenderer();
        renderer.setWriter(writer);
        renderer.printMessage(message);
        writer.flush();
        writer.close();
    }

    private void printError(IRendererFactory rendererFactory, final HttpServletRequest request,
            final HttpServletResponse response, Exception exception) throws IOException
    {
        if (exception instanceof HighLevelException)
        {
            operationLog.error(exception.getMessage());
        } else if (operationLog.isInfoEnabled())
        {
            StringBuffer url = request.getRequestURL();
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString))
            {
                url.append("?").append(queryString);
            }
            operationLog.error("Request " + url + " caused an exception: ", exception);
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
            String dataSetCode, RenderingContext renderingContext, RequestParams requestParams,
            HttpSession session) throws IOException
    {

        IHierarchicalContentNode node = renderingContext.getContentNode();
        if (node.exists() == false)
        {
            throw new EnvironmentFailureException("Resource '" + node.getRelativePath()
                    + "' does not exist in data set '" + dataSetCode + "'.");
        }

        String sessionIdOrNull = requestParams.tryGetSessionId();
        ensureDatasetAccessible(dataSetCode, session, sessionIdOrNull);
        if (node.isDirectory())
        {
            if (requestParams.isAutoResolve())
            {
                autoResolve(rendererFactory, response, dataSetCode, renderingContext,
                        requestParams, session, node, false);
            } else if (requestParams.isForceAutoResolve())
            {
                autoResolve(rendererFactory, response, dataSetCode, renderingContext,
                        requestParams, session, node, true);
            } else
            {
                createPage(rendererFactory, response, dataSetCode, renderingContext, node, requestParams.isDisableLinks());
            }
        } else
        {
            deliverFile(response, dataSetCode, node, requestParams.getDisplayMode());
        }
    }

    private void autoResolve(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, RequestParams requestParams,
            HttpSession session, IHierarchicalContentNode dirNode, boolean shouldForce)
            throws IOException
    {
        assert dirNode.exists() && dirNode.isDirectory();
        List<IHierarchicalContentNode> mainDataSets =
                AutoResolveUtils.findSomeMatchingFiles(renderingContext.getRootContent(),
                        requestParams.tryGetMainDataSetPath(),
                        requestParams.tryGetMainDataSetPattern());
        if (mainDataSets.size() == 1 || (mainDataSets.size() > 1 && shouldForce))
        {
            String newRelativePath = mainDataSets.get(0).getRelativePath();
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext, newRelativePath);
            autoResolveRedirect(response, newRenderingContext, requestParams);
        } else if (AutoResolveUtils.continueAutoResolving(requestParams.tryGetMainDataSetPattern(),
                dirNode))
        {
            assert dirNode.getChildNodes().size() == 1;
            String childName = dirNode.getChildNodes().get(0).getName();
            String oldRelativePath = renderingContext.getRelativePath();
            String pathPrefix = StringUtils.isBlank(oldRelativePath) ? "" : (oldRelativePath + "/");
            String newRelativePath = pathPrefix + childName;
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext, newRelativePath);
            autoResolveRedirect(response, newRenderingContext, requestParams);
        } else
        {
            createPage(rendererFactory, response, dataSetCode, renderingContext, dirNode, requestParams.isDisableLinks());
        }
    }

    private static void autoResolveRedirect(HttpServletResponse response,
            RenderingContext newContext, RequestParams requestParams) throws IOException
    {
        String urlPrefix = newContext.getUrlPrefix();
        String relativePath = newContext.getRelativePath();
        String sessionIdOrNull = newContext.getSessionIdOrNull();
        String newLocationDomain = null;
        if (DOWNLOAD_URL.isEmpty() && requestParams.isForceReferrerDomainOnEmptyDownloadURL())
        {
            newLocationDomain = requestParams.getReferrerDomain();
        } else
        {
            newLocationDomain = DOWNLOAD_URL;
        }
        final String newLocation =
                newLocationDomain + urlPrefix + "/" + relativePath
                        + Utils.createUrlParameterForSessionId("?", sessionIdOrNull) + "&disableLinks=" + requestParams.isDisableLinks();

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Auto resolve redirect: '%s', context: %s",
                    newLocation, newContext));
        }
        response.sendRedirect(newLocation);
    }

    private void createPage(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, IHierarchicalContentNode dirNode, Boolean disableLinks)
            throws IOException
    {
        assert dirNode.isDirectory();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("For data set '%s' show directory '%s'", dataSetCode,
                    (dirNode.getRelativePath() == null) ? "/" : dirNode.getRelativePath()));
        }
        IDirectoryRenderer directoryRenderer =
                rendererFactory.createDirectoryRenderer(renderingContext);
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = null;
        try
        {
            HttpRequestUtils.setNoCacheHeaders(response);
            writer = response.getWriter();
            directoryRenderer.setWriter(writer);
            directoryRenderer.printHeader();
            String relativeParentPath = renderingContext.getRelativeParentPath();
            if (relativeParentPath != null)
            {
                directoryRenderer.printLinkToParentDirectory(relativeParentPath, disableLinks);
            }
            List<IHierarchicalContentNode> children = dirNode.getChildNodes();
            HierarchicalContentUtils.sortNodes(children);
            for (IHierarchicalContentNode childNode : children)
            {
                String name = childNode.getName();
                String relativePath = childNode.getRelativePath();
                String normalizedRelativePath = relativePath.replace('\\', '/');
                if (childNode.isDirectory())
                {
                    directoryRenderer.printDirectory(name, normalizedRelativePath, childNode.getFileLength(), disableLinks);
                } else
                {
                    Integer checksumOrNull =
                            childNode.isChecksumCRC32Precalculated() ? childNode.getChecksumCRC32()
                                    : null;
                    directoryRenderer.printFile(name, normalizedRelativePath,
                            childNode.getFileLength(), checksumOrNull, shouldLinksBeDisabled(childNode, disableLinks));
                }
            }
            directoryRenderer.printFooter();
            writer.flush();

        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }
    
    private boolean shouldLinksBeDisabled(IHierarchicalContentNode node, boolean disableLinks)
    {
        if (disableLinks)
        {
            return true;
        }
        IDatasetLocation dataSet = tryExtractDataSet(node);
        if (dataSet != null && dataSet.getDataSetCode().equals(dataSet.getDataSetLocation()))
        {
            return true;
        }
        return disableLinks;
    }
    
    private IDatasetLocation tryExtractDataSet(IHierarchicalContentNode node)
    {
        if (node instanceof VirtualHierarchicalContent.VirtualNode)
        {
            IHierarchicalContentNode wrappedNode = ((VirtualHierarchicalContent.VirtualNode) node).lastNode();
            if (wrappedNode instanceof DataSetAwareHierarchicalContentNode)
            {
                return ((DataSetAwareHierarchicalContentNode) wrappedNode).getDataSet();
            }
        }
        return null;
    }

    private void deliverFile(final HttpServletResponse response, String dataSetCode,
            IHierarchicalContentNode fileNode, String displayMode) throws IOException,
            FileNotFoundException
    {
        assert fileNode.isDirectory() == false;

        String infoPostfix;
        ResponseContentStream responseStream;
        Size thumbnailSize = tryAsThumbnailDisplayMode(displayMode);
        if (thumbnailSize != null)
        {
            BufferedImage image = createThumbnail(fileNode, thumbnailSize, null);
            infoPostfix = " as a thumbnail.";
            responseStream = createResponseContentStream(image, fileNode.getName());
        } else
        {
            infoPostfix = ".";
            responseStream = createResponseContentStream(fileNode, displayMode);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSetCode + "' deliver file '"
                    + fileNode.getRelativePath() + "' (" + responseStream.getSize() + " bytes)"
                    + infoPostfix);
        }
        writeResponseContent(responseStream, response);
    }

    /**
     * @param image is the content of the response
     * @param fileNameOrNull specified if image was generated from one file
     */
    private final static ResponseContentStream createResponseContentStream(BufferedImage image,
            String fileNameOrNull) throws IOException
    {
        return ResponseContentStream.createPNG(image, fileNameOrNull);
    }

    private static ResponseContentStream createResponseContentStream(
            IHierarchicalContentNode contentNode, String displayMode) throws FileNotFoundException
    {
        String contentType = Utils.getMimeType(contentNode, displayMode.equals(TEXT_MODE_DISPLAY));

        return ResponseContentStream.create(contentNode.getInputStream(),
                contentNode.getFileLength(), contentType, contentNode.getName());
    }

}
