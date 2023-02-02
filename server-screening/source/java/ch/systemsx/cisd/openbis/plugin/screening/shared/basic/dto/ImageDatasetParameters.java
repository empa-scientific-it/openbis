/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the images in the dataset: tiles geometry, channels, dataset code and plate geometry if it is HCS image.
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetParameters implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // dataset code for which plate parameters are valid
    private String datasetCode;

    private Integer rowsNumOrNull;

    private Integer colsNumOrNull;

    private int tileRowsNum;

    private int tileColsNum;

    private List<InternalImageChannel> channels;

    // true if any well in the dataset has a time series (or depth stack) of images
    private boolean isMultidimensional;

    private String mergedChannelTransformerFactorySignatureOrNull;

    public Integer tryGetRowsNum()
    {
        return rowsNumOrNull;
    }

    public void setRowsNum(int rowsNum)
    {
        this.rowsNumOrNull = rowsNum;
    }

    public Integer tryGetColsNum()
    {
        return colsNumOrNull;
    }

    public void setColsNum(int colsNum)
    {
        this.colsNumOrNull = colsNum;
    }

    public int getTileRowsNum()
    {
        return tileRowsNum;
    }

    public void setTileRowsNum(int tileRowsNum)
    {
        this.tileRowsNum = tileRowsNum;
    }

    public int getTileColsNum()
    {
        return tileColsNum;
    }

    public void setTileColsNum(int tileColsNum)
    {
        this.tileColsNum = tileColsNum;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public List<String> getChannelsCodes()
    {
        final List<String> channelCodes = new ArrayList<String>();
        for (InternalImageChannel channel : channels)
        {
            channelCodes.add(channel.getCode());
        }
        return channelCodes;
    }

    public List<String> getChannelsLabels()
    {
        final List<String> channelLabels = new ArrayList<String>();
        for (InternalImageChannel channel : channels)
        {
            channelLabels.add(channel.getLabel());
        }
        return channelLabels;
    }

    public int getChannelsNumber()
    {
        return channels.size();
    }

    public List<InternalImageChannel> getChannels()
    {
        return channels;
    }

    /** never null, can be empty if channel does not exist or no transformations are available */
    public List<InternalImageTransformationInfo> getAvailableImageTransformationsFor(
            String channelCode)
    {
        List<InternalImageTransformationInfo> result =
                new ArrayList<InternalImageTransformationInfo>();
        result.add(new InternalImageTransformationInfo(
                ScreeningConstants.USER_DEFINED_RESCALING_CODE, "User defined",
                "User defined intensity rescaling", "", false));

        for (InternalImageChannel channel : channels)
        {
            if (channel.getCode().equalsIgnoreCase(channelCode))
            {
                result.addAll(channel.getAvailableImageTransformations());
                return result;
            }
        }
        return result;
    }

    public boolean isMultidimensional()
    {
        return isMultidimensional;
    }

    public void setIsMultidimensional(boolean isMultidimensional)
    {
        this.isMultidimensional = isMultidimensional;
    }

    public void setInternalChannels(List<InternalImageChannel> channels)
    {
        this.channels = channels;
    }

    public List<InternalImageChannel> getInternalChannels()
    {
        return channels;
    }

    /**
     * @param channelCodeOrNull null for merged channels (transformationCode is ignored in that case)
     */
    public String tryGetTransformerFactorySignature(String channelCodeOrNull,
            String transformationCode)
    {
        if (channelCodeOrNull == null)
        {
            return mergedChannelTransformerFactorySignatureOrNull;
        }
        List<InternalImageTransformationInfo> transformations =
                getAvailableImageTransformationsFor(channelCodeOrNull);
        for (InternalImageTransformationInfo transformation : transformations)
        {
            if (transformation.getCode().equalsIgnoreCase(transformationCode))
            {
                return transformation.getTransformationSignature();
            }
        }
        return null;
    }

    public String tryGetMergedChannelTransformerFactorySignature()
    {
        return mergedChannelTransformerFactorySignatureOrNull;
    }

    public void setMergedChannelTransformerFactorySignature(
            String mergedChannelTransformerFactorySignatureOrNull)
    {
        this.mergedChannelTransformerFactorySignatureOrNull =
                mergedChannelTransformerFactorySignatureOrNull;
    }

}
