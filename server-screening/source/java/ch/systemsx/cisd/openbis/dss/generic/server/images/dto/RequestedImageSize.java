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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * Describes the maximal requested size of the image and if it should be enlarged to the requested size in case when it is smaller.
 * 
 * @author Tomasz Pylak
 */
public class RequestedImageSize extends AbstractHashable
{
    public static RequestedImageSize createOriginal()
    {
        return new RequestedImageSize(null, false);
    }

    private final Size thumbnailSizeOrNull;

    private final boolean enlargeIfNecessary;

    private final boolean highQualityRescalingRequired;

    public RequestedImageSize(Size thumbnailSizeOrNull, boolean enlargeIfNecessary)
    {
        this(thumbnailSizeOrNull, enlargeIfNecessary, false);
    }

    public RequestedImageSize(Size thumbnailSizeOrNull, boolean enlargeIfNecessary,
            boolean highQualityRescalingRequired)
    {
        this.thumbnailSizeOrNull = thumbnailSizeOrNull;
        this.enlargeIfNecessary = enlargeIfNecessary;
        this.highQualityRescalingRequired = highQualityRescalingRequired;
    }

    /** original size if null */
    public Size tryGetThumbnailSize()
    {
        return thumbnailSizeOrNull;
    }

    public boolean isThumbnailRequired()
    {
        return thumbnailSizeOrNull != null;
    }

    public boolean enlargeIfNecessary()
    {
        return enlargeIfNecessary;
    }

    public boolean isHighQualityRescalingRequired()
    {
        return highQualityRescalingRequired;
    }

    @Override
    public String toString()
    {
        return thumbnailSizeOrNull == null ? "original size" : thumbnailSizeOrNull.toString();
    }
}
