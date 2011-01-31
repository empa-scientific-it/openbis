/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * Configuration parameters which describe how thumbnails should be generated.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailsStorageFormat
{
    /** Maximum default width and height of a thumbnail */
    public static final int DEFAULT_THUMBNAIL_MAX_SIZE = 200;

    /** Maximum default width and height of a thumbnail */
    public static final boolean DEFAULT_COMPRESS_THUMBNAILS = false;

    // ---

    private int maxWidth = DEFAULT_THUMBNAIL_MAX_SIZE;

    private int maxHeight = DEFAULT_THUMBNAIL_MAX_SIZE;

    private boolean storeCompressed = DEFAULT_COMPRESS_THUMBNAILS;

    public int getMaxWidth()
    {
        return maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public boolean isStoreCompressed()
    {
        return storeCompressed;
    }

    /** Sets the maximum width of a thumbnail. */
    public void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    /** Sets the maximum height of a thumbnail. */
    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    /** Sets if each thumbnail should be additionally compressed (lostless) before it is stored. */
    public void setStoreCompressed(boolean storeCompressed)
    {
        this.storeCompressed = storeCompressed;
    }

}
