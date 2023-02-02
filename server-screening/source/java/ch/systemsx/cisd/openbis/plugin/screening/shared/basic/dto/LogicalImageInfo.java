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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes one logical image in the image dataset. For HCS the logical image will consist of all images of the well. For microscopy the whole
 * dataset contains one logical image.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageInfo implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ImageDatasetEnrichedReference imageDataset;

    private List<ImageChannelStack> channelStacks;

    // GWT only
    @SuppressWarnings("unused")
    private LogicalImageInfo()
    {
    }

    public LogicalImageInfo(ImageDatasetEnrichedReference imageDataset,
            List<ImageChannelStack> channelStacks)
    {
        this.imageDataset = imageDataset;
        this.channelStacks = channelStacks;
    }

    public ImageDatasetEnrichedReference getImageDataset()
    {
        return imageDataset;
    }

    public ImageDatasetParameters getImageParameters()
    {
        return imageDataset.getImageDatasetParameters();
    }

    public String getExperimentPermId()
    {
        return getImageDatasetReference().getExperimentPermId();
    }

    public String getExperimentIdentifier()
    {
        return getImageDatasetReference().getExperimentIdentifier();
    }

    private DatasetReference getImageDatasetReference()
    {
        return imageDataset.getImageDataset().getDatasetReference();
    }

    public List<ImageChannelStack> getChannelStacks()
    {
        return channelStacks;
    }
}
