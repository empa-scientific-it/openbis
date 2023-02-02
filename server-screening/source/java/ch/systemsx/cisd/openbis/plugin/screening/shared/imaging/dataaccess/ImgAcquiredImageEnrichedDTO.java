/*
 * Copyright 2012 ETH Zuerich, CISD
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

/**
 * @author Pawel Glyzewski
 */
public class ImgAcquiredImageEnrichedDTO extends ImgAcquiredImageDTO
{
    @ResultColumn("SPOT_X")
    private Integer spotColumn;

    // position in the container, one-based (e.g. well row, 1 is the first row)
    @ResultColumn("SPOT_Y")
    private Integer spotRow;

    @ResultColumn("IMAGE_PATH")
    private String imageFilePath;

    @ResultColumn("IMAGE_ID")
    private String imageIdOrNull;

    @ResultColumn("IMAGE_COLOR")
    private String imageColorComponentOrNull;

    @ResultColumn("THUMBNAIL_PATH")
    private String filePath;

    @ResultColumn("THUMBNAIL_IMAGE_ID")
    private String thumbnailImageIdOrNull;

    @ResultColumn("THUMBNAIL_COLOR")
    private String thumbnailColorComponentOrNull;

    @ResultColumn("CHANNEL_CODE")
    private String channelCode;

    // x and y are kind of a two dimensional sequence number, (e.g. tile column)
    @ResultColumn("X")
    private Integer tileColumn;

    // x and y are kind of a two dimensional sequence number, (e.g. tile row, 1 is the first row)
    // Some use case may only use x and leave y alone.
    @ResultColumn("Y")
    private Integer tileRow;

    // can be null
    @ResultColumn("Z_in_M")
    private Float z;

    // can be null
    @ResultColumn("T_in_SEC")
    private Float t;

    @ResultColumn("SERIES_NUMBER")
    private Integer seriesNumber;

    public Integer getSpotColumn()
    {
        return spotColumn;
    }

    public void setSpotColumn(Integer spotColumn)
    {
        this.spotColumn = spotColumn;
    }

    public Integer getSpotRow()
    {
        return spotRow;
    }

    public void setSpotRow(Integer spotRow)
    {
        this.spotRow = spotRow;
    }

    public String getImageFilePath()
    {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath)
    {
        this.imageFilePath = imageFilePath;
    }

    public String getImageIdOrNull()
    {
        return imageIdOrNull;
    }

    public void setImageIdOrNull(String imageIdOrNull)
    {
        this.imageIdOrNull = imageIdOrNull;
    }

    public String getImageColorComponentOrNull()
    {
        return imageColorComponentOrNull;
    }

    public void setImageColorComponentOrNull(String imageColorComponentOrNull)
    {
        this.imageColorComponentOrNull = imageColorComponentOrNull;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getThumbnailImageIdOrNull()
    {
        return thumbnailImageIdOrNull;
    }

    public void setThumbnailImageIdOrNull(String thumbnailImageIdOrNull)
    {
        this.thumbnailImageIdOrNull = thumbnailImageIdOrNull;
    }

    public String getThumbnailColorComponentOrNull()
    {
        return thumbnailColorComponentOrNull;
    }

    public void setThumbnailColorComponentOrNull(String thumbnailColorComponentOrNull)
    {
        this.thumbnailColorComponentOrNull = thumbnailColorComponentOrNull;
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    public Integer getTileColumn()
    {
        return tileColumn;
    }

    public void setTileColumn(Integer tileColumn)
    {
        this.tileColumn = tileColumn;
    }

    public Integer getTileRow()
    {
        return tileRow;
    }

    public void setTileRow(Integer tileRow)
    {
        this.tileRow = tileRow;
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
}
