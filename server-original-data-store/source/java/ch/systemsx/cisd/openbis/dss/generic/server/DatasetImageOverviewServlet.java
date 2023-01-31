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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.DatasetImageOverviewUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageResolutionKind;

/**
 * Servlet that dispatches requests for image representative for a dataset to a plugin depending on dataset type.
 * 
 * @author Piotr Buczek
 */
public class DatasetImageOverviewServlet extends AbstractDatasetDownloadServlet
{

    private static final long serialVersionUID = 1L;

    private static DatasetImageOverviewConfiguration configuration;

    /**
     * A utility class for dealing with the URL parameters required to generate an overview image. This class makes sure all the required parameters
     * are in the request.
     * 
     * @author Piotr Buczek
     */
    protected static class RequestParams
    {

        private final String sessionId;

        private final String datasetCode;

        private final String datasetTypeCode;

        private final ImageResolutionKind resolution;

        public RequestParams(HttpServletRequest request)
        {
            sessionId =
                    getRequiredParameter(request, DatasetImageOverviewUtilities.SESSION_ID_PARAM);
            datasetCode =
                    getRequiredParameter(request,
                            DatasetImageOverviewUtilities.PERM_ID_PARAMETER_KEY);
            datasetTypeCode =
                    getRequiredParameter(request, DatasetImageOverviewUtilities.TYPE_PARAMETER_KEY);
            resolution =
                    ImageResolutionKind.valueOf(getRequiredParameter(request,
                            DatasetImageOverviewUtilities.RESOLUTION_PARAMETER_KEY));
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
            String datasetTypeCode = params.datasetTypeCode;
            ImageResolutionKind resolution = params.resolution;

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
            ensureDatasetAccessible(datasetCode, session, sessionId);

            ResponseContentStream responseStream =
                    createImageResponse(session, sessionId, datasetCode, datasetTypeCode,
                            resolution);

            if (responseStream != null && operationLog.isDebugEnabled())
            {
                operationLog.debug("Image for data set '" + datasetCode + "' of type "
                        + datasetTypeCode + " was delivered.");
            }

            writeResponseContent(responseStream, response);
        } catch (Exception e)
        {
            operationLog.error("", e);
            printErrorResponse(response, "Invalid Request");
        }
    }

    private ResponseContentStream createImageResponse(HttpSession session, String sessionToken, String dataSetCode,
            String dataSetTypeCode, ImageResolutionKind resolution)
    {
        IHierarchicalContent content =
                applicationContext.getHierarchicalContentProvider(sessionToken).asContent(dataSetCode);
        IDatasetImageOverviewPlugin plugin =
                configuration.getDatasetImageOverviewPlugin(dataSetTypeCode);
        return plugin.createImageOverview(dataSetCode, dataSetTypeCode, content, resolution);
    }

    // static initialization is used to simplify usage of properties

    static void initConfiguration(Properties properties)
    {
        configuration = DatasetImageOverviewConfiguration.createConfiguration(properties);
    }

}
