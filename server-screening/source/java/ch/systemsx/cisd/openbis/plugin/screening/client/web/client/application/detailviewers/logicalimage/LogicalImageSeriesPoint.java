package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * @author pkupczyk
 */
class LogicalImageSeriesPoint implements Comparable<LogicalImageSeriesPoint>
{
    private final Float tOrNull, zOrNull;

    private final Integer seriesNumberOrNull;

    public LogicalImageSeriesPoint(ImageChannelStack stack)
    {
        this.tOrNull = stack.tryGetTimepoint();
        this.zOrNull = stack.tryGetDepth();
        this.seriesNumberOrNull = stack.tryGetSeriesNumber();
    }

    public LogicalImageSeriesPoint(Float tOrNull, Float zOrNull, Integer seriesNumberOrNull)
    {
        this.tOrNull = tOrNull;
        this.zOrNull = zOrNull;
        this.seriesNumberOrNull = seriesNumberOrNull;
    }

    public String getLabel()
    {
        String desc = "";
        if (isSeriesNumberPresent())
        {
            if (desc.length() > 0)
            {
                desc += ". ";
            }
            desc += "Series: " + seriesNumberOrNull;
        }
        if (isTimePointPresent())
        {
            if (desc.length() > 0)
            {
                desc += ". ";
            }
            desc += "Time: " + tOrNull + " sec";
        }
        if (isDepthPresent())
        {
            if (desc.length() > 0)
            {
                desc += ". ";
            }
            desc += "Depth: " + zOrNull;
        }
        return desc;
    }

    private boolean isDepthPresent()
    {
        return zOrNull != null;
    }

    Float getDepthOrNull()
    {
        return zOrNull;
    }

    private boolean isTimePointPresent()
    {
        return tOrNull != null;
    }

    Float getTimePointOrNull()
    {
        return tOrNull;
    }

    private boolean isSeriesNumberPresent()
    {
        return seriesNumberOrNull != null;
    }

    Integer getSeriesNumberOrNull()
    {
        return seriesNumberOrNull;
    }

    @Override
    public int compareTo(LogicalImageSeriesPoint o)
    {
        int cmp;
        cmp = compareNullable(seriesNumberOrNull, o.seriesNumberOrNull);
        if (cmp != 0)
            return cmp;
        cmp = compareNullable(tOrNull, o.tOrNull);
        if (cmp != 0)
            return cmp;
        return compareNullable(zOrNull, o.zOrNull);
    }

    private static <T extends Comparable<T>> int compareNullable(T v1OrNull, T v2OrNull)
    {
        if (v1OrNull == null)
        {
            return v2OrNull == null ? 0 : -1;
        } else
        {
            return v2OrNull == null ? 1 : v1OrNull.compareTo(v2OrNull);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isSeriesNumberPresent() ? seriesNumberOrNull.hashCode() : 0);
        result = prime * result + (isTimePointPresent() ? tOrNull.hashCode() : 0);
        result = prime * result + (isDepthPresent() ? zOrNull.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LogicalImageSeriesPoint other = (LogicalImageSeriesPoint) obj;
        if (isSeriesNumberPresent() == false)
        {
            if (other.isSeriesNumberPresent())
            {
                return false;
            }
        } else if (seriesNumberOrNull.equals(other.seriesNumberOrNull) == false)
        {
            return false;
        }
        if (isTimePointPresent() == false)
        {
            if (other.isTimePointPresent())
            {
                return false;
            }
        } else if (tOrNull.equals(other.tOrNull) == false)
        {
            return false;
        }
        if (isDepthPresent() == false)
        {
            if (other.isDepthPresent())
            {
                return false;
            }
        } else if (zOrNull.equals(other.zOrNull) == false)
        {
            return false;
        }
        return true;
    }
}
