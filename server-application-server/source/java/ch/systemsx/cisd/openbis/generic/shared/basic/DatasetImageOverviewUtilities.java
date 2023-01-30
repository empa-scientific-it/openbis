/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageResolutionKind;

/**
 * @author Piotr Buczek
 */
public class DatasetImageOverviewUtilities
{
    public static final String SERVLET_NAME = "image-overview";

    /** The HTTP URL parameter used to specify the data set identifier. */
    public static final String PERM_ID_PARAMETER_KEY = "permId";

    /** The HTTP URL parameter used to specify the data set type code. */
    public static final String TYPE_PARAMETER_KEY = "type";

    /** The HTTP URL parameter used to specify the resolution of a requested image. */
    public static final String RESOLUTION_PARAMETER_KEY = "resolution";

    /** The HTTP URL parameter used to specify the DSS session id. */
    public static final String SESSION_ID_PARAM = "session_id";

    private final static String createLink(final String dssBaseURL, final String permId,
            final String typeCode, final ImageResolutionKind resolution, final String sessionId)
    {
        URLMethodWithParameters ulrWithParameters =
                new URLMethodWithParameters(dssBaseURL + "/" + SERVLET_NAME);
        ulrWithParameters.addParameter(PERM_ID_PARAMETER_KEY, permId);
        ulrWithParameters.addParameter(TYPE_PARAMETER_KEY, typeCode);
        ulrWithParameters.addParameter(RESOLUTION_PARAMETER_KEY, resolution);
        ulrWithParameters.addParameter(SESSION_ID_PARAM, sessionId);
        return ulrWithParameters.toString();
    }

    /** generates URL of an image on Data Store server */
    public static String createEmbededImageHtml(final String dssBaseURL, final String permId,
            final String typeCode, final String sessionId)
    {
        final String imageURL =
                createLink(dssBaseURL, permId, typeCode, ImageResolutionKind.SMALL, sessionId);
        final String linkURL =
                createLink(dssBaseURL, permId, typeCode, ImageResolutionKind.NORMAL, sessionId);

        return URLMethodWithParameters.createEmbededImageHtml(imageURL, linkURL, -1, -1);
    }

}
