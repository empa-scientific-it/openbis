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

/**
 * @author Franz-Josef Elmer
 */
public class ImageTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private static final String SLASH = "/";

    private static String getPath(String dataSetCode, String dataSetLocation, String originalPath)
    {
        int indexOfLocation = originalPath.indexOf(dataSetLocation);
        if (indexOfLocation < 0)
        {
            throw new IllegalArgumentException("Data set location '" + dataSetLocation
                    + "' inconsistent with original path: " + originalPath);
        }
        String relativePath = originalPath.substring(indexOfLocation + dataSetLocation.length());
        return getPath(dataSetCode, relativePath);
    }

    private static String getPath(String dataSetCode, String relativePath)
    {
        String delimiter = "";
        if (false == relativePath.startsWith(SLASH))
        {
            delimiter = SLASH;
        }
        return dataSetCode + delimiter + relativePath;
    }

    private String path;

    private int maxThumbnailWidth;

    private int maxThumbnailHeight;

    public ImageTableCell(String dataSetCode, String dataSetLocation, String originalPath,
            int maxThumbnailWidth, int maxThumbnailHeight)
    {
        this(getPath(dataSetCode, dataSetLocation, originalPath), maxThumbnailWidth, maxThumbnailHeight);
    }

    /**
     * @param relativePathFromDataSetRoot the relative path to this image file starting from the data set root.
     */
    public ImageTableCell(String dataSetCode, String relativePathFromDataSetRoot,
            int maxThumbnailWidth,
            int maxThumbnailHeight)
    {
        this(getPath(dataSetCode, relativePathFromDataSetRoot), maxThumbnailWidth,
                maxThumbnailHeight);
    }

    public ImageTableCell(String path, int maxThumbnailWidth, int maxThumbnailHeight)
    {
        this.path = path;
        this.maxThumbnailWidth = maxThumbnailWidth;
        this.maxThumbnailHeight = maxThumbnailHeight;
    }

    public String getPath()
    {
        return path;
    }

    public int getMaxThumbnailWidth()
    {
        return maxThumbnailWidth;
    }

    public int getMaxThumbnailHeight()
    {
        return maxThumbnailHeight;
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return toString().compareTo(String.valueOf(o));
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || (obj instanceof ImageTableCell && ((ImageTableCell) obj).path.equals(path));
    }

    @Override
    public int hashCode()
    {
        return path.hashCode();
    }

    @Override
    public String toString()
    {
        return path;
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private ImageTableCell()
    {
    }
}
