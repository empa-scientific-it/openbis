package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.io.Serializable;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * DTO with information about one image file
 * 
 * @author Tomasz Pylak
 */
public class ImageFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private WellLocation wellLocationOrNull;

    private Location tileLocation;

    private String channelCode;

    private final String imageRelativePath;

    private Float timepointOrNull;

    private Float depthOrNull;

    private Integer seriesNumberOrNull;

    private ImageIdentifier imageIdentifier;

    private String uniqueImageIdentifier;

    private String containerDataSetCode;

    public ImageFileInfo(String channelCode, int tileRow, int tileColumn, String imageRelativePath)
    {
        assert channelCode != null;
        assert imageRelativePath != null;

        this.channelCode = channelCode;
        this.imageRelativePath = imageRelativePath;
        setTile(tileRow, tileColumn);
    }

    public String tryGetUniqueStringIdentifier()
    {
        if (imageIdentifier != null)
        {
            return imageIdentifier.getUniqueStringIdentifier();
        } else if (uniqueImageIdentifier != null)
        {
            return uniqueImageIdentifier;
        }

        return null;
    }

    public Integer tryGetWellRow()
    {
        return wellLocationOrNull == null ? null : wellLocationOrNull.getRow();
    }

    public Integer tryGetWellColumn()
    {
        return wellLocationOrNull == null ? null : wellLocationOrNull.getColumn();
    }

    public WellLocation tryGetWellLocation()
    {
        return wellLocationOrNull;
    }

    public boolean hasWellLocation()
    {
        return wellLocationOrNull != null;
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

    public String getImageRelativePath()
    {
        return imageRelativePath;
    }

    public Float tryGetTimepoint()
    {
        return timepointOrNull;
    }

    public Float tryGetDepth()
    {
        return depthOrNull;
    }

    public Integer tryGetSeriesNumber()
    {
        return seriesNumberOrNull;
    }

    // --- setters

    public void setImageIdentifier(ImageIdentifier imageIdentifier)
    {
        this.imageIdentifier = imageIdentifier;
    }

    public void setWell(int row, int column)
    {
        this.wellLocationOrNull = new WellLocation(row, column);
    }

    /** @return true if well row and column could be parsed */
    public boolean setWell(String wellText)
    {
        try
        {
            this.wellLocationOrNull = WellLocation.parseLocationStr(wellText);
        } catch (Exception e)
        {
            // do nothing
        }
        return wellLocationOrNull != null;
    }

    public void setTile(int row, int column)
    {
        this.tileLocation = Location.createLocationFromRowAndColumn(row, column);
    }

    public void setTimepoint(Float value)
    {
        this.timepointOrNull = value;
    }

    public void setDepth(Float value)
    {
        this.depthOrNull = value;
    }

    public void setSeriesNumber(Integer value)
    {
        this.seriesNumberOrNull = value;
    }

    public void setUniqueImageIdentifier(String uniqueImageIdentifier)
    {
        this.uniqueImageIdentifier = uniqueImageIdentifier;
    }

    public String getContainerDataSetCode()
    {
        return containerDataSetCode;
    }

    public void setContainerDataSetCode(String containerDataSetCode)
    {
        this.containerDataSetCode = containerDataSetCode;
    }

    @Override
    public String toString()
    {
        return "ImageFileInfo [well=" + wellLocationOrNull + ", tile=" + tileLocation
                + ", channel=" + channelCode + ", path=" + imageRelativePath + ", timepoint="
                + timepointOrNull + ", depth=" + depthOrNull + ", seriesNumber="
                + seriesNumberOrNull + "]";
    }
}