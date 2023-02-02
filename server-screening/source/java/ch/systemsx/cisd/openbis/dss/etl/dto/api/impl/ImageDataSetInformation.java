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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Extends {@link DataSetInformation} with information about images needed in HCS/Microscopy.
 * 
 * @author Tomasz Pylak
 */
public class ImageDataSetInformation extends BasicDataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private File incomingDirectory;

    /**
     * Path to the incoming folder with images, relative to the dataset directory. E.g. if the incoming folder name is X and the transaction's dataset
     * registration code put it inside 'original' folder, then this path points to "original/X'.
     */
    private File datasetRelativeImagesFolderPath;

    private ImageDataSetStructure imageDataSetStructure;

    private ThumbnailsInfo thumbnailsInfos;

    private boolean registerAsOverviewImageDataSet;

    private boolean generateOverviewImagesFromRegisteredImages;

    private int maximumImageWidth;

    private int maximumImageHeight;

    private Integer colorDepth;

    private final List<DataSetInformation> secondaryDataSets = new ArrayList<DataSetInformation>();

    private IImageGenerationAlgorithm imageGenerationAlgorithm;

    public File getIncomingDirectory()
    {
        return incomingDirectory;
    }

    /** @returns null if no thumbnails will be stored */
    public ThumbnailsInfo getThumbnailsInfos()
    {
        return thumbnailsInfos;
    }

    public ImageDataSetStructure getImageDataSetStructure()
    {
        return imageDataSetStructure;
    }

    public File getDatasetRelativeImagesFolderPath()
    {
        return datasetRelativeImagesFolderPath;
    }

    /** Sets the folder where all original images are located initially. */
    public void setIncomingDirectory(File incomingDirectory)
    {
        this.incomingDirectory = incomingDirectory;
    }

    public void setDatasetRelativeImagesFolderPath(File datasetRelativeImagesFolderPath)
    {
        this.datasetRelativeImagesFolderPath = datasetRelativeImagesFolderPath;
    }

    public void setThumbnailsInfo(ThumbnailsInfo thumbnailsInfo)
    {
        this.thumbnailsInfos = thumbnailsInfo;
    }

    public void setImageDataSetStructure(ImageDataSetStructure imageDataSetStructure)
    {
        this.imageDataSetStructure = imageDataSetStructure;
    }

    // -------

    public int getMaximumImageWidth()
    {
        return maximumImageWidth;
    }

    public void setMaximumImageWidth(int maximumImageWidth)
    {
        this.maximumImageWidth = maximumImageWidth;
    }

    public int getMaximumImageHeight()
    {
        return maximumImageHeight;
    }

    public void setMaximumImageHeight(int maximumImageHeight)
    {
        this.maximumImageHeight = maximumImageHeight;
    }

    public List<Channel> getChannels()
    {
        return imageDataSetStructure.getChannels();
    }

    public List<ChannelColorComponent> getChannelColorComponents()
    {
        return imageDataSetStructure.getChannelColorComponents();
    }

    public void setChannels(List<Channel> channels)
    {
        imageDataSetStructure.setChannels(channels);
    }

    public void setChannels(List<Channel> channels,
            List<ChannelColorComponent> channelColorComponents)
    {
        imageDataSetStructure.setChannels(channels, channelColorComponents);
    }

    public boolean getRegisterAsOverviewImageDataSet()
    {
        return registerAsOverviewImageDataSet;
    }

    public void setRegisterAsOverviewImageDataSet(boolean registerAsOverviewImageDataSet)
    {
        this.registerAsOverviewImageDataSet = registerAsOverviewImageDataSet;
    }

    public boolean isGenerateOverviewImagesFromRegisteredImages()
    {
        return generateOverviewImagesFromRegisteredImages;
    }

    public void setGenerateOverviewImagesFromRegisteredImages(
            boolean generateOverviewImagesFromRegisteredImages)
    {
        this.generateOverviewImagesFromRegisteredImages =
                generateOverviewImagesFromRegisteredImages;
    }

    public Integer getColorDepth()
    {
        return colorDepth;
    }

    public void setColorDepth(Integer colorDepth)
    {
        this.colorDepth = colorDepth;
    }

    public void addSecondaryDataSetInformation(DataSetInformation dataSetInformation)
    {
        secondaryDataSets.add(dataSetInformation);
    }

    public List<DataSetInformation> getSecondaryDataSets()
    {
        return secondaryDataSets;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder(super.toString());
        appendNameAndObject(buffer, "images structure", imageDataSetStructure);
        appendNameAndObject(buffer, "container dataset", tryGetContainerDatasetPermId());
        if (secondaryDataSets.isEmpty() == false)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (DataSetInformation dataSet : secondaryDataSets)
            {
                builder.append(dataSet.getDataSetCode());
            }
            appendNameAndObject(buffer, "secondary datasets", builder);
        }
        appendNameAndObject(buffer, "original dataset", this.getDataSetCode());
        if (maximumImageHeight > 0 && maximumImageWidth > 0)
        {
            appendNameAndObject(buffer, "bounding box", maximumImageWidth + "x"
                    + maximumImageHeight);
        }
        if (getColorDepth() != null)
        {
            appendNameAndObject(buffer, "color depth", this.getColorDepth());
        }
        if (getThumbnailsInfos() != null)
        {
            appendNameAndObject(buffer, "thumbnail", getThumbnailsInfos());
        } else
        {
            appendNameAndObject(buffer, "thumbnail", "none");
        }
        return buffer.toString();
    }

    public void setImageGenerationAlgorithm(IImageGenerationAlgorithm imageGenerationAlgorithm)
    {
        this.imageGenerationAlgorithm = imageGenerationAlgorithm;
    }

    public IImageGenerationAlgorithm getImageGenerationAlgorithm()
    {
        return imageGenerationAlgorithm;
    }
}
