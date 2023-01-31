/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.ITabularDataGraph;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SessionWorkspaceUtil;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTabularDataGraphServlet extends AbstractDatasetDownloadServlet
{
    private static final long serialVersionUID = 1L;

    public static final String DATASET_CODE_PARAM = "dataset";

    public static final String GRAPH_TYPE_CODE = "type";

    protected TabularDataGraphCollectionConfiguration configuration;

    protected static final String PROPERTIES_FILE_KEY = "properties-file";

    public static final String FILE_PATH_PARAM = "file";

    /**
     * A utility class for dealing with the URL parameters required to generate an image. This class makes sure all the required parameters are in the
     * request (it throws exceptions otherwise), and it defaults values for all optional parameters if they are not in the request.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected static class RequestParams
    {
        // optional parameters
        public final static String WIDTH_PARAM = "w";

        public final static String HEIGHT_PARAM = "h";

        private final String sessionId;

        private final String datasetCode;

        private final String filePathOrNull;

        private final String graphName;

        private final int width;

        private final int height;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getRequiredParameter(request, Utils.SESSION_ID_PARAM);
            datasetCode = getOptionalParameter(request, DATASET_CODE_PARAM);
            filePathOrNull = getOptionalParameter(request, FILE_PATH_PARAM);
            graphName = getOptionalParameter(request, GRAPH_TYPE_CODE, "dynamic");
            width = getIntParam(request, WIDTH_PARAM, 0);
            height = getIntParam(request, HEIGHT_PARAM, 0);
        }

        private static int getIntParam(HttpServletRequest request, String paramName,
                int defaultValue)
        {
            String value = request.getParameter(paramName);
            if (value == null)
                return defaultValue;

            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new UserFailureException("parameter " + paramName
                        + " should be an integer, but is: " + value);
            }
        }

        private static String getOptionalParameter(final HttpServletRequest request,
                String paramName)
        {
            String value = request.getParameter(paramName);
            return value;
        }

        private static String getOptionalParameter(final HttpServletRequest request,
                String paramName, String defaultValue)
        {
            String value = request.getParameter(paramName);
            if (null == value)
            {
                return defaultValue;
            }
            return value;
        }

        private static String getRequiredParameter(final HttpServletRequest request,
                String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                throw new UserFailureException("no value for the parameter " + paramName
                        + " found in the URL");
            }
            return value;
        }

        public String getSessionId()
        {
            return sessionId;
        }
    }

    /**
     *
     *
     */
    public AbstractTabularDataGraphServlet()
    {
        super();
    }

    /**
     * @param applicationContext
     */
    public AbstractTabularDataGraphServlet(ApplicationContext applicationContext)
    {
        super(applicationContext);
    }

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        // Only initialize the db properties once
        if (configuration != null)
            return;

        String propertiesFilePath = servletConfig.getInitParameter(PROPERTIES_FILE_KEY);
        configuration =
                TabularDataGraphCollectionConfiguration.getConfiguration(propertiesFilePath);
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Get the parameters from the request
            RequestParams params = new RequestParams(request);
            String sessionId = params.sessionId;
            String datasetCode = params.datasetCode;
            String filePathOrNull = params.filePathOrNull;

            if (datasetCode == null)
            {
                ExposablePropertyPlaceholderConfigurer config =
                        (ExposablePropertyPlaceholderConfigurer) ServiceProvider
                                .getApplicationContext()
                                .getBean(
                                        ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME);

                String workspace =
                        config.getResolvedProps().getProperty(
                                SessionWorkspaceUtil.SESSION_WORKSPACE_ROOT_DIR_KEY);

                if (workspace == null)
                {
                    workspace = SessionWorkspaceUtil.SESSION_WORKSPACE_ROOT_DIR_DEFAULT;
                }

                File sessionWorkspace =
                        new SessionWorkspaceProvider(new File(workspace), sessionId)
                                .getSessionWorkspace();

                filePathOrNull = sessionWorkspace.getAbsolutePath() + "/" + filePathOrNull;
            }

            // Get the session and user from the request
            HttpSession session = tryGetOrCreateSession(request, sessionId);
            if (session == null)
            {
                printSessionExpired(response);
                return;
            }
            // Check that the user has view access to the data
            // NOTE: This throws an exception -- it may be nicer to return an image for a
            // non-accessible dataset...
            if (datasetCode != null)
            {
                ensureDatasetAccessible(datasetCode, session, sessionId);
            } else
            {
                if (filePathOrNull == null || new File(filePathOrNull).exists() == false)
                {
                    throw new UserFailureException("File '" + filePathOrNull
                            + "' is not accessible.");
                }

            }

            // Get the tabular data
            ITabularData fileLines = getDatasetLines(request, datasetCode, filePathOrNull);

            // Generate an image into the stream
            ITabularDataGraph generator =
                    getConfiguration(request).getGraph(params.graphName, fileLines,
                            response.getOutputStream());

            response.setContentType(Utils.CONTENT_TYPE_PNG);
            String headerContentDisposition = "inline; filename=plot_" + (new Date().getTime());
            response.setHeader("Content-Disposition", headerContentDisposition);

            if (params.height > 0 && params.width > 0)
            {
                generator.generateImage(params.width, params.height);
            } else
            {
                generator.generateImage();
            }

        } catch (Exception e)
        {
            operationLog.error("", e);
            printErrorResponse(response, "Invalid Request");
        }
    }

    /**
     * Get the configuration for this graph. Defaults to the configuration ivar. Subclasses may override.
     */
    protected TabularDataGraphCollectionConfiguration getConfiguration(HttpServletRequest request)
    {
        return configuration;
    }

    protected abstract ITabularData getDatasetLines(HttpServletRequest request, String dataSetCode,
            String filePathOrNull)
            throws IOException;

}