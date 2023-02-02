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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import java.util.Arrays;
import java.util.List;

/**
 * DTO which points to acquired images of one dataset in chosen channels.
 * 
 * @author Tomasz Pylak
 */
public class DatasetAcquiredImagesReference
{
    private final String datasetCode;

    private final ImageChannelStackReference channelStackReference;

    /** Can be null if merged channels are requested. */
    private final List<String> channelCodesOrNull;

    public static DatasetAcquiredImagesReference createForMergedChannels(String datasetCode,
            ImageChannelStackReference channelStackReference)
    {
        return new DatasetAcquiredImagesReference(datasetCode, channelStackReference, null);
    }

    public static DatasetAcquiredImagesReference createForSingleChannel(String datasetCode,
            ImageChannelStackReference channelStackReference, String channelCode)
    {
        return new DatasetAcquiredImagesReference(datasetCode, channelStackReference,
                Arrays.asList(channelCode));
    }

    public static DatasetAcquiredImagesReference createForManyChannels(String datasetCode,
            ImageChannelStackReference channelStackReference, List<String> channelCodes)
    {
        return new DatasetAcquiredImagesReference(datasetCode, channelStackReference, channelCodes);
    }

    /** @param channelCodesOrNull null if channels should be merged */
    private DatasetAcquiredImagesReference(String datasetCode,
            ImageChannelStackReference channelStackReference, List<String> channelCodesOrNull)
    {
        assert datasetCode != null;
        assert channelStackReference != null;

        this.datasetCode = datasetCode;
        this.channelStackReference = channelStackReference;
        this.channelCodesOrNull = channelCodesOrNull;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public ImageChannelStackReference getChannelStackReference()
    {
        return channelStackReference;
    }

    /**
     * Check if all exiting channels of the image should be merged (and not a single one or a subset). We want to treat the case where merged channels
     * were requested in the same way as the case where all channel names have been enumerated.
     */
    public boolean isMergeAllChannels(List<String> allChannelsCodes)
    {
        if (channelCodesOrNull == null)
        {
            return true;
        }
        List<String> wantedChannelCodes = channelCodesOrNull;
        if (allChannelsCodes.size() == 1)
        {
            return false; // there is only one channel in total, single channel transformation
                          // should be used
        }
        for (String existingChannelCode : allChannelsCodes)
        {
            if (wantedChannelCodes.indexOf(existingChannelCode) == -1)
            {
                return false;
            }
        }
        return true;
    }

    public List<String> getChannelCodes(List<String> allChannelsCodes)
    {
        if (channelCodesOrNull == null)
        {
            return allChannelsCodes;
        } else
        {
            return channelCodesOrNull;
        }
    }

    @Override
    public String toString()
    {
        String channelsDesc =
                channelCodesOrNull == null ? "all channels merged" : "channels="
                        + channelCodesOrNull;
        return "[datasetCode=" + datasetCode + ", channelStackReference=" + channelStackReference
                + ", " + channelsDesc + "]";
    }

}
