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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * A Table Cell that is used for generated images.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GeneratedImageTableCell implements ISerializableComparable
{
    // Servlet parameters
    public final static String IMAGE_WIDTH_PARAM = "w";

    public final static String IMAGE_HEIGHT_PARAM = "h";

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String path;

    private int imageWidth;

    private int imageHeight;

    private int thumbnailWidth;

    private int thumbnailHeight;

    private HashMap<String, Object> parameters;

    public GeneratedImageTableCell(String path, int imageWidth, int imageHeight,
            int thumbnailWidth, int thumbnailHeight)
    {
        this.path = path;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.parameters = new HashMap<String, Object>();
    }

    // For GWT
    @SuppressWarnings("unused")
    private GeneratedImageTableCell()
    {
    }

    public String getPath()
    {
        return path;
    }

    int getImageWidth()
    {
        return imageWidth;
    }

    int getImageHeight()
    {
        return imageHeight;
    }

    public int getMaxThumbnailWidth()
    {
        return thumbnailWidth;
    }

    public int getMaxThumbnailHeight()
    {
        return thumbnailHeight;
    }

    public void addParameter(String name, Object value)
    {
        parameters.put(name, value);
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return toString().compareTo(String.valueOf(o));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (!(obj instanceof GeneratedImageTableCell))
            return false;

        GeneratedImageTableCell other = (GeneratedImageTableCell) obj;

        return other.toString().equals(toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return getHTMLString("url/", "sessionToken");
    }

    /**
     * Get a string containing HTML that shows a thumbnail image with a link to the larger image
     */
    public String getHTMLString(String downloadURL, String sessionID)
    {
        String urlRoot = downloadURL + "/" + path;
        // Need to do everything doubly, since we need urls for the images and the thumbnails
        URLMethodWithParameters urlMethodImage = new URLMethodWithParameters(urlRoot);
        URLMethodWithParameters urlMethodThumb = new URLMethodWithParameters(urlRoot);
        urlMethodImage.addParameter("sessionID", sessionID);
        urlMethodThumb.addParameter("sessionID", sessionID);

        for (Map.Entry<String, Object> entry : parameters.entrySet())
        {
            urlMethodImage.addParameter(entry.getKey(), entry.getValue());
            urlMethodThumb.addParameter(entry.getKey(), entry.getValue());
        }

        // Add dimension parameters for the full image and thumbnail
        urlMethodImage.addParameter(IMAGE_WIDTH_PARAM, imageWidth);
        urlMethodImage.addParameter(IMAGE_HEIGHT_PARAM, imageHeight);

        urlMethodThumb.addParameter(IMAGE_WIDTH_PARAM, thumbnailWidth);
        urlMethodThumb.addParameter(IMAGE_HEIGHT_PARAM, thumbnailHeight);

        return URLMethodWithParameters.createEmbededImageHtml(urlMethodThumb.toString(),
                urlMethodImage.toString(), thumbnailWidth, thumbnailHeight);
    }

}
