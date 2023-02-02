/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("LogicalImageInfo")
public class LogicalImageInfo implements Serializable
{

    private static final long serialVersionUID = 1L;

    private ImageDatasetEnrichedReference imageDataset;

    private List<ImageChannelStack> channelStacks;

    public ImageDatasetEnrichedReference getImageDataset()
    {
        return imageDataset;
    }

    public void setImageDataset(ImageDatasetEnrichedReference imageDataset)
    {
        this.imageDataset = imageDataset;
    }

    public List<ImageChannelStack> getChannelStacks()
    {
        return channelStacks;
    }

    public void setChannelStacks(List<ImageChannelStack> channelStacks)
    {
        this.channelStacks = channelStacks;
    }

}
