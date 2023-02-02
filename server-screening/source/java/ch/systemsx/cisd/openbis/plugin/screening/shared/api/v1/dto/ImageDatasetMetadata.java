package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Metadata information about images in a specified dataset.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("ImageDatasetMetadata")
public class ImageDatasetMetadata implements Serializable
{
    private static final long serialVersionUID = 1L;

    private IImageDatasetIdentifier imageDataset;

    private int channelsNumber;

    private List<String> channelNames;

    private List<String> channelCodes;

    private List<String> channelLabels;

    private List<ImageChannel> channels;

    private int tilesNumber;

    private int tilesRows;

    private int tilesCols;

    private int width;

    private int height;

    private int thumbnailWidth;

    private int thumbnailHeight;

    public ImageDatasetMetadata(IImageDatasetIdentifier dataset, List<ImageChannel> channels,
            int tilesRows, int tilesCols, int width, int height, int thumbnailWidth,
            int thumbnailHeight)
    {
        this.imageDataset = dataset;
        Collections.sort(channels);
        this.channels = channels;
        this.channelsNumber = channels.size();
        this.channelNames = new ArrayList<String>(channelsNumber);
        this.channelCodes = new ArrayList<String>(channelsNumber);
        this.channelLabels = new ArrayList<String>(channelsNumber);
        for (ImageChannel c : channels)
        {
            channelNames.add(c.getCode());
            channelCodes.add(c.getCode());
            channelLabels.add(c.getLabel());
        }
        this.tilesRows = tilesRows;
        this.tilesCols = tilesCols;
        this.tilesNumber = tilesRows * tilesCols;
        this.width = width;
        this.height = height;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailWidth = thumbnailWidth;
    }

    /**
     * Identifier of this image dataset.
     */
    public IImageDatasetIdentifier getImageDataset()
    {
        return imageDataset;
    }

    /**
     * Number of channels (wavelengths) in which images have been acquired for this dataset.
     */
    public int getNumberOfChannels()
    {
        return channelsNumber;
    }

    /**
     * Returns the list of channels for this dataset.
     * 
     * @since 1.9
     */
    public List<ImageChannel> getChannels()
    {
        return channels;
    }

    /**
     * Names of channels in which images have been acquired for this dataset.
     * 
     * @deprecated use {@link #getChannels()} instead.
     */
    @Deprecated
    public List<String> getChannelNames()
    {
        return channelNames;
    }

    /**
     * Returns channel codes.
     * <p>
     * <i>Note: If channel codes are unspecified channel names are returned. This will be the case if a serialized instance of a previous of this
     * class will be deserialized.</i>
     */
    public List<String> getChannelCodes()
    {
        return channelCodes == null ? channelNames : channelCodes;
    }

    /**
     * Returns channel labels.
     * <p>
     * <i>Note: If channel labels are unspecified channel names are returned. This will be the case if a serialized instance of a previous of this
     * class will be deserialized.</i>
     * 
     * @deprecated use {@link #getChannels()} instead.
     */
    @Deprecated
    public List<String> getChannelLabels()
    {
        return channelLabels == null ? channelNames : channelLabels;
    }

    /**
     * Number of rows of image tiles (or "fields") available for each well.
     * <p>
     * <i>Note: Will be 0 if the server does not support API version 1.6</i>
     * 
     * @since 1.6
     */
    public int getTilesRows()
    {
        return tilesRows;
    }

    /**
     * Number of columns of image tiles (or "fields") available for each well.
     * <p>
     * <i>Note: Will be 0 if the server does not support API version 1.6</i>
     * 
     * @since 1.6
     */
    public int getTilesCols()
    {
        return tilesCols;
    }

    /**
     * Number of image tiles (or "fields") available for each well.
     */
    public int getNumberOfTiles()
    {
        return tilesNumber;
    }

    /**
     * Width of the images in this dataset.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Height of the images in this dataset.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * The width of the thumbnail images, or 0 if no thumbnails are available for this data set.
     * 
     * @since 1.6
     */
    public int getThumbnailWidth()
    {
        return thumbnailWidth;
    }

    /**
     * The height of the thumbnail images, or 0 if no thumbnails are available for this data set.
     * 
     * @since 1.6
     */
    public int getThumbnailHeight()
    {
        return thumbnailHeight;
    }

    /**
     * <code>true</code>, if this data set has thumbnails, <code>false</code> otherwise.
     * 
     * @since 1.6
     */
    public boolean hasThumbnails()
    {
        return thumbnailHeight > 0 && thumbnailWidth > 0;
    }

    @Override
    public String toString()
    {
        String thumbnailsDesc =
                hasThumbnails() ? ". Thumbnail resolution: " + thumbnailWidth + "x"
                        + thumbnailHeight + "." : "";
        return "Dataset " + imageDataset + " has [" + getChannelCodes() + "] channels, "
                + tilesNumber + " tiles. Image resolution: " + width + "x" + height
                + thumbnailsDesc;
    }

    // Java de-serialization

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        if (channels == null)
        {
            final List<String> codes = getChannelCodes();
            final List<String> labels = getChannelLabels();
            channels = new ArrayList<ImageChannel>(channelsNumber);
            for (int i = 0; i < channelsNumber; ++i)
            {
                channels.add(new ImageChannel(codes.get(i), labels.get(i)));
            }
        }
    }

    //
    // JSON-RPC
    //

    private ImageDatasetMetadata()
    {
    }

    private void setImageDataset(IImageDatasetIdentifier imageDataset)
    {
        this.imageDataset = imageDataset;
    }

    private void setNumberOfChannels(int numberOfChannels)
    {
        this.channelsNumber = numberOfChannels;
    }

    private void setChannelNames(List<String> channelNames)
    {
        this.channelNames = channelNames;
    }

    private void setChannelCodes(List<String> channelCodes)
    {
        this.channelCodes = channelCodes;
    }

    private void setChannelLabels(List<String> channelLabels)
    {
        this.channelLabels = channelLabels;
    }

    private void setChannels(List<ImageChannel> channels)
    {
        this.channels = channels;
    }

    private void setNumberOfTiles(int numberOfTiles)
    {
        this.tilesNumber = numberOfTiles;
    }

    private void setTilesRows(int tilesRows)
    {
        this.tilesRows = tilesRows;
    }

    private void setTilesCols(int tilesCols)
    {
        this.tilesCols = tilesCols;
    }

    private void setWidth(int width)
    {
        this.width = width;
    }

    private void setHeight(int height)
    {
        this.height = height;
    }

    private void setThumbnailWidth(int thumbnailWidth)
    {
        this.thumbnailWidth = thumbnailWidth;
    }

    private void setThumbnailHeight(int thumbnailHeight)
    {
        this.thumbnailHeight = thumbnailHeight;
    }

}