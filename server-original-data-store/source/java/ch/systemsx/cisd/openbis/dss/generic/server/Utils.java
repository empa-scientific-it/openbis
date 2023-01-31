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

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
public class Utils
{

    public static final String SESSION_ID_PARAM = "sessionID";

    static final String BINARY_CONTENT_TYPE = "binary";

    static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";

    static final MimetypesFileTypeMap MIMETYPES = new MimetypesFileTypeMap();

    static final String CONTENT_TYPE_PNG = "image/png";

    static
    {
        MIMETYPES.addMimeTypes("application/xml qcml");
        MIMETYPES.addMimeTypes("application/pdf pdf");
        MIMETYPES.addMimeTypes("image/svg+xml svg");
        MIMETYPES.addMimeTypes("video/webm webm");
        MIMETYPES.addMimeTypes("video/mp4 mp4");
        MIMETYPES.addMimeTypes("text/css css");
        MIMETYPES.addMimeTypes("text/javascript js");
    }

    static String createUrlParameterForSessionId(String prefix, String sessionIdOrNull)
    {
        return sessionIdOrNull == null ? "" : prefix + Utils.SESSION_ID_PARAM + "="
                + sessionIdOrNull;
    }

    static String getMimeType(IHierarchicalContentNode fileNode, boolean plainTextMode)
    {
        return getMimeType(fileNode.getName(), plainTextMode);
    }

    static String getMimeType(String fileName, boolean plainTextMode)
    {
        if (plainTextMode)
        {
            return BINARY_CONTENT_TYPE;
        } else
        {
            String extension = FilenameUtils.getExtension(fileName);
            if (extension.length() == 0)
            {
                return PLAIN_TEXT_CONTENT_TYPE;
            } else if (extension.equalsIgnoreCase("png"))
            {
                return CONTENT_TYPE_PNG;
            } else
            {
                return MIMETYPES.getContentType(fileName.toLowerCase());
            }
        }
    }

}
