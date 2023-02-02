package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import org.apache.commons.lang3.StringUtils;

/**
 * Store well, channel and tile number to which an image belongs. Optionally stores timepoint/depth-scan/image series number.
 * 
 * @author Tomasz Pylak
 */
public class ImageMetadata
{
    private String channelCode;

    private int tileNumber;

    private String well;

    private Float timepointOrNull;

    private Float depthOrNull;

    private Integer seriesNumberOrNull;

    private ImageIdentifier imageIdentifierOrNull;

    public String getChannelCode()
    {
        return channelCode;
    }

    /** Sets channel code. */
    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    public int getTileNumber()
    {
        return tileNumber;
    }

    /** Sets tile number. It should start from 1. */
    public void setTileNumber(int tileNumber)
    {
        this.tileNumber = tileNumber;
    }

    public String getWell()
    {
        return well;
    }

    /** Sets well code (example: "A1") */
    public void setWell(String well)
    {
        this.well = well;
    }

    /** Sets the timepoint of the image. Optional. */
    public void setTimepoint(Float value)
    {
        this.timepointOrNull = value;
    }

    /** Sets the depth at which the image has been scanned. Optional. */
    public void setDepth(Float value)
    {
        this.depthOrNull = value;
    }

    /**
     * Sets the integer series number of the image. Optional. Used to order images when there are no time or depth dimentions but there is a series of
     * images for one well, channel and tile. Can be also used together with time and depth dimention.
     */
    public void setSeriesNumber(Integer value)
    {
        this.seriesNumberOrNull = value;
    }

    /**
     * Sets the id of the image inside a container image file format. This is optional and not needed for image files which contain only one image.
     */
    public void setImageIdentifier(ImageIdentifier imageIdentifier)
    {
        imageIdentifierOrNull = imageIdentifier;
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

    public ImageIdentifier tryGetImageIdentifier()
    {
        return imageIdentifierOrNull;
    }

    /**
     * Validates that tile number, channel and well (if argument is <code>false</code>) have been specified.
     * 
     * @throws IllegalStateException if the object is not valid.
     */
    public void ensureValid(boolean isMicroscopy)
    {
        if (tileNumber <= 0)
        {
            throw new IllegalStateException("Tile number has to be > 0, but is " + tileNumber);
        }
        if (StringUtils.isBlank(channelCode))
        {
            throw new IllegalStateException("Channel code is not specified");
        }
        if (StringUtils.isBlank(well) && isMicroscopy == false)
        {
            throw new IllegalStateException("Well is not specified");
        }
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ImageMetadata [channelCode=" + channelCode + ", tileNumber=" + tileNumber
                + ", well=" + well);
        if (timepointOrNull != null)
        {
            sb.append(", timepointOrNull =" + timepointOrNull);
        }
        if (depthOrNull != null)
        {
            sb.append(", depthOrNull=" + depthOrNull);
        }
        if (seriesNumberOrNull != null)
        {
            sb.append(", seriesNumberOrNull=" + seriesNumberOrNull);
        }
        if (imageIdentifierOrNull != null)
        {
            sb.append(", imageIdentifierOrNull=" + imageIdentifierOrNull);
        }
        sb.append("]");
        return sb.toString();
    }

}