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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import java.util.Map;

/**
 * Describes which image transformations should be applied. Note that image-level transformation is always applied for single channels.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformationParams
{
    private final boolean applyNonImageLevelTransformation;

    /** ignored if {@link #applyNonImageLevelTransformation} is false */
    private final boolean useMergedChannelsTransformation;

    /**
     * ignored if {@link #applyNonImageLevelTransformation} is false or if {@link #useMergedChannelsTransformation} is false
     */
    private final String singleChannelTransformationCodeOrNull;

    private Map<String, String> transformationsPerChannel;

    public ImageTransformationParams(boolean applyNonImageLevelTransformation,
            boolean useMergedChannelsTransformation, String singleChannelTransformationCodeOrNull,
            Map<String, String> transformationsPerChannel)
    {
        this.applyNonImageLevelTransformation = applyNonImageLevelTransformation;
        this.useMergedChannelsTransformation = useMergedChannelsTransformation;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
        this.transformationsPerChannel = transformationsPerChannel;
    }

    public boolean isApplyNonImageLevelTransformation()
    {
        return applyNonImageLevelTransformation;
    }

    public boolean isUseMergedChannelsTransformation()
    {
        return useMergedChannelsTransformation;
    }

    public String tryGetSingleChannelTransformationCode()
    {
        return singleChannelTransformationCodeOrNull;
    }

    public String tryGetTransformationCodeForChannel(String channelCode)
    {
        if (channelCode == null || transformationsPerChannel == null)
        {
            return null;
        }
        return transformationsPerChannel.get(channelCode);
    }

    public Map<String, String> tryGetTransformationCodeForChannels()
    {
        if (transformationsPerChannel == null)
        {
            return null;
        }
        return transformationsPerChannel;
    }
}
