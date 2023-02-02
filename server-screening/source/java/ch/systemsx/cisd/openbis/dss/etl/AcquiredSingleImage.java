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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * Describes properties extracted for one logical screening image (note that one file can contain many logical images).
 * 
 * @author Tomasz Pylak
 */
public class AcquiredSingleImage extends AbstractHashable
{
    // null for non-HCS images
    private final Location wellLocationOrNull;

    private final Location tileLocation;

    private final String channelCode;

    // can be null
    private final Float timePointOrNull, depthOrNull;

    // can be null
    private Integer seriesNumberOrNull;

    private final RelativeImageReference imageFilePath; // relative to the original dataset
                                                        // directory

    private RelativeImageReference thumbnailFilePathOrNull;

    private IImageTransformerFactory imageTransformerFactoryOrNull;

    public AcquiredSingleImage(Location wellLocationOrNull, Location tileLocation,
            String channelCode, Float timePointOrNull, Float depthOrNull,
            Integer seriesNumberOrNull, RelativeImageReference imageFilePath,
            RelativeImageReference thumbnailFilePathOrNull)
    {
        this.wellLocationOrNull = wellLocationOrNull;
        this.tileLocation = tileLocation;
        this.channelCode = CodeNormalizer.normalize(channelCode);
        this.timePointOrNull = timePointOrNull;
        this.depthOrNull = depthOrNull;
        this.seriesNumberOrNull = seriesNumberOrNull;
        this.imageFilePath = imageFilePath;
        this.thumbnailFilePathOrNull = thumbnailFilePathOrNull;
    }

    public Location tryGetWellLocation()
    {
        return wellLocationOrNull;
    }

    /** Valid only in HCS case, do not call this method for microscopy images. */
    public int getWellRow()
    {
        assert wellLocationOrNull != null : "wellLocationOrNull is null";
        return wellLocationOrNull.getY();
    }

    /** Valid only in HCS case, do not call this method for microscopy images. */
    public int getWellColumn()
    {
        assert wellLocationOrNull != null : "wellLocationOrNull is null";
        return wellLocationOrNull.getX();
    }

    public int getTileRow()
    {
        return tileLocation.getY();
    }

    public int getTileColumn()
    {
        return tileLocation.getX();
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public Float tryGetTimePoint()
    {
        return timePointOrNull;
    }

    public Float tryGetDepth()
    {
        return depthOrNull;
    }

    public RelativeImageReference getImageReference()
    {
        return imageFilePath;
    }

    public RelativeImageReference getThumbnailFilePathOrNull()
    {
        return thumbnailFilePathOrNull;
    }

    public Integer tryGetSeriesNumber()
    {
        return seriesNumberOrNull;
    }

    // ---- setters

    public void setSeriesNumber(int seriesNumber)
    {
        this.seriesNumberOrNull = seriesNumber;
    }

    public IImageTransformerFactory tryGetImageTransformerFactory()
    {
        return imageTransformerFactoryOrNull;
    }

    public void setImageTransformerFactory(IImageTransformerFactory imageTransformerFactoryOrNull)
    {
        this.imageTransformerFactoryOrNull = imageTransformerFactoryOrNull;
    }
}
