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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.io.Serializable;

import ch.systemsx.cisd.imagereaders.ImageID;

/**
 * Immutable value class of an image ID based on series index, time series index, focal plane index, and color channel index. It will be used to
 * identify images in a container image file format like multi-page TIFF.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageIdentifier implements Comparable<ImageIdentifier>, Serializable
{
    private static final long serialVersionUID = 1L;

    public static final ImageIdentifier NULL = new ImageIdentifier(0, 0, 0, 0);

    private final int seriesIndex;

    private final int timeSeriesIndex;

    private final int focalPlaneIndex;

    private final int colorChannelIndex;

    /**
     * Creates an instance for the specified series index, time series (or T) index, focal plane (or Z) index, color channel index.
     */
    public ImageIdentifier(int seriesIndex, int timeSeriesIndex, int focalPlaneIndex,
            int colorChannelIndex)
    {
        this.seriesIndex = seriesIndex;
        this.timeSeriesIndex = timeSeriesIndex;
        this.focalPlaneIndex = focalPlaneIndex;
        this.colorChannelIndex = colorChannelIndex;
    }

    public int getSeriesIndex()
    {
        return seriesIndex;
    }

    public int getTimeSeriesIndex()
    {
        return timeSeriesIndex;
    }

    public int getFocalPlaneIndex()
    {
        return focalPlaneIndex;
    }

    public int getColorChannelIndex()
    {
        return colorChannelIndex;
    }

    public String getUniqueStringIdentifier()
    {
        return new ImageID(getSeriesIndex(), getTimeSeriesIndex(), getFocalPlaneIndex(),
                getColorChannelIndex()).getID();
    }

    @Override
    public int compareTo(ImageIdentifier that)
    {
        int diff = seriesIndex - that.seriesIndex;
        if (diff != 0)
        {
            return diff;
        }
        diff = timeSeriesIndex - that.timeSeriesIndex;
        if (diff != 0)
        {
            return diff;
        }
        diff = focalPlaneIndex - that.focalPlaneIndex;
        if (diff != 0)
        {
            return diff;
        }
        return colorChannelIndex - that.colorChannelIndex;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ImageIdentifier == false)
        {
            return false;
        }
        ImageIdentifier that = (ImageIdentifier) obj;
        return seriesIndex == that.seriesIndex && timeSeriesIndex == that.timeSeriesIndex
                && focalPlaneIndex == that.focalPlaneIndex
                && colorChannelIndex == that.colorChannelIndex;
    }

    @Override
    public int hashCode()
    {
        return ((((seriesIndex * 37) + timeSeriesIndex) * 37) + focalPlaneIndex) * 37
                + colorChannelIndex;
    }

    @Override
    public String toString()
    {
        return seriesIndex + "." + timeSeriesIndex + "." + focalPlaneIndex + "."
                + colorChannelIndex;
    }

}
