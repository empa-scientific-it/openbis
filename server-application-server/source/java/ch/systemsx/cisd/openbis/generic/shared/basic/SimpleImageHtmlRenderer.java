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

/**
 * @author Tomasz Pylak
 */
public class SimpleImageHtmlRenderer
{
    /** generates URL of an image on Data Store server */
    public static String createEmbededDatastoreImageHtml(String imagePath, int width, int height,
            String downloadURL, String sessionID)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(downloadURL + "/"
                        + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/"
                        + imagePath);
        methodWithParameters.addParameter(GenericSharedConstants.SESSION_ID_PARAMETER, sessionID);
        String linkURL = methodWithParameters.toString();

        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);
        String imageURL = methodWithParameters.toString();
        return URLMethodWithParameters.createEmbededImageHtml(imageURL, linkURL, width, height);
    }

}
