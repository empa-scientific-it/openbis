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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;

/**
 * @author Tomasz Pylak
 */
public class ImgChannelStackDTO
{
    @ResultColumn("ID")
    private long id;

    // x and y are kind of a two dimensional sequence number, (e.g. tile column)
    @ResultColumn("X")
    private Integer column;

    // x and y are kind of a two dimensional sequence number, (e.g. tile row, 1 is the first row)
    // Some use case may only use x and leave y alone.
    @ResultColumn("Y")
    private Integer row;

    // can be null
    @ResultColumn("Z_in_M")
    private Float z;

    // can be null
    @ResultColumn("T_in_SEC")
    private Float t;

    @ResultColumn("SERIES_NUMBER")
    private Integer seriesNumber;

    @ResultColumn("DS_ID")
    private long datasetId;

    @ResultColumn("SPOT_ID")
    private Long spotId;

    @ResultColumn("IS_REPRESENTATIVE")
    private boolean isRepresentative;

    @SuppressWarnings("unused")
    private ImgChannelStackDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgChannelStackDTO(long id, int row, int column, long datasetId, Long spotIdOrNull,
            Float tOrNull, Float zOrNull, Integer seriesNumber, boolean isRepresentative)
    {
        this.id = id;
        this.row = row;
        this.column = column;
        this.datasetId = datasetId;
        this.spotId = spotIdOrNull;
        this.t = tOrNull;
        this.z = zOrNull;
        this.seriesNumber = seriesNumber;
        this.isRepresentative = isRepresentative;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Integer getColumn()
    {
        return column;
    }

    public Integer getRow()
    {
        return row;
    }

    public Float getZ()
    {
        return z;
    }

    public void setZ(Float z)
    {
        this.z = z;
    }

    public Float getT()
    {
        return t;
    }

    public void setT(Float t)
    {
        this.t = t;
    }

    public Integer getSeriesNumber()
    {
        return seriesNumber;
    }

    public void setSeriesNumber(Integer seriesNumber)
    {
        this.seriesNumber = seriesNumber;
    }

    public long getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(long datasetId)
    {
        this.datasetId = datasetId;
    }

    /** can be null */
    public Long getSpotId()
    {
        return spotId;
    }

    public void setSpotId(Long spotId)
    {
        this.spotId = spotId;
    }

    public boolean getIsRepresentative()
    {
        return isRepresentative;
    }

    public void setRepresentative(boolean isRepresentative)
    {
        this.isRepresentative = isRepresentative;
    }

    @Override
    // use all fields besides id
    public int hashCode()
    {
        // NOTE: isRepresentative field is derived and can be skipped
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (datasetId ^ (datasetId >>> 32));
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((row == null) ? 0 : row.hashCode());
        result = prime * result + ((spotId == null) ? 0 : spotId.hashCode());
        result = prime * result + ((t == null) ? 0 : t.hashCode());
        result = prime * result + ((z == null) ? 0 : z.hashCode());
        result = prime * result + ((seriesNumber == null) ? 0 : seriesNumber.hashCode());
        return result;
    }

    @Override
    // use all fields besides id
    public boolean equals(Object obj)
    {
        // NOTE: isRepresentative field is derived and can be skipped
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        ImgChannelStackDTO other = (ImgChannelStackDTO) obj;
        if (datasetId != other.datasetId)
            return false;
        if (column == null)
        {
            if (other.column != null)
                return false;
        } else if (!column.equals(other.column))
            return false;
        if (row == null)
        {
            if (other.row != null)
                return false;
        } else if (!row.equals(other.row))
            return false;

        if (spotId == null)
        {
            if (other.spotId != null)
                return false;
        } else if (!spotId.equals(other.spotId))
            return false;

        if (t == null)
        {
            if (other.t != null)
                return false;
        } else if (!t.equals(other.t))
            return false;
        if (z == null)
        {
            if (other.z != null)
                return false;
        } else if (!z.equals(other.z))
            return false;
        if (seriesNumber == null)
        {
            if (other.seriesNumber != null)
                return false;
        } else if (!seriesNumber.equals(other.seriesNumber))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

}
