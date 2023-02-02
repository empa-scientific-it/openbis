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

package ch.systemsx.cisd.openbis.dss.etl.dto;

/**
 * DTO which stores time, depth and series number (all optional).
 * 
 * @author Tomasz Pylak
 */
public final class ImageSeriesPoint implements Comparable<ImageSeriesPoint>
{
    private final Float timeOrNull;

    private final Float depthOrNull;

    private final Integer seriesNumberOrNull;

    public ImageSeriesPoint(Float timeOrNull, Float depthOrNull, Integer seriesNumberOrNull)
    {
        this.timeOrNull = timeOrNull;
        this.depthOrNull = depthOrNull;
        this.seriesNumberOrNull = seriesNumberOrNull;
    }

    public Float getTime()
    {
        return timeOrNull;
    }

    public Float getDepth()
    {
        return depthOrNull;
    }

    public Integer getSeriesNumber()
    {
        return seriesNumberOrNull;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((depthOrNull == null) ? 0 : depthOrNull.hashCode());
        result = prime * result + ((timeOrNull == null) ? 0 : timeOrNull.hashCode());
        result =
                prime * result + ((seriesNumberOrNull == null) ? 0 : seriesNumberOrNull.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImageSeriesPoint other = (ImageSeriesPoint) obj;
        if (depthOrNull == null)
        {
            if (other.depthOrNull != null)
                return false;
        } else if (!depthOrNull.equals(other.depthOrNull))
            return false;
        if (timeOrNull == null)
        {
            if (other.timeOrNull != null)
                return false;
        } else if (!timeOrNull.equals(other.timeOrNull))
            return false;
        if (seriesNumberOrNull == null)
        {
            if (other.seriesNumberOrNull != null)
                return false;
        } else if (!seriesNumberOrNull.equals(other.seriesNumberOrNull))
            return false;
        return true;
    }

    @Override
    public int compareTo(ImageSeriesPoint o)
    {
        int depthCompare = compareConstituent(depthOrNull, o.depthOrNull);
        if (depthCompare != 0)
        {
            return depthCompare;
        }

        int timeCompare = compareConstituent(timeOrNull, o.timeOrNull);
        if (timeCompare != 0)
        {
            return timeCompare;
        }

        return compareConstituent(seriesNumberOrNull, o.seriesNumberOrNull);
    }

    private <T extends Comparable<T>> int compareConstituent(T myValueOrNull, T otherValueOrNull)
    {
        int compareValue;
        if (myValueOrNull != null)
        {
            if (otherValueOrNull != null)
            {
                compareValue = myValueOrNull.compareTo(otherValueOrNull);
            } else
            {
                compareValue = 0;
            }
        } else
        {
            if (otherValueOrNull != null)
            {
                compareValue = -1;
            } else
            {
                compareValue = 0;
            }
        }
        return compareValue;
    }
}