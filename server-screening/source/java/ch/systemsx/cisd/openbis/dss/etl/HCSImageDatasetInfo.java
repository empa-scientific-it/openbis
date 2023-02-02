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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.openbis.dss.etl.dto.ImageDatasetInfo;

/**
 * Describes one HCS image dataset from imaging database.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageDatasetInfo extends HCSContainerDatasetInfo
{
    private final boolean storeChannelsOnExperimentLevel;

    private final ImageDatasetInfo imageDatasetInfo;

    public HCSImageDatasetInfo(HCSContainerDatasetInfo info, ImageDatasetInfo imageDatasetInfo,
            boolean storeChannelsOnExperimentLevel)
    {
        super.setContainerRows(info.getContainerRows());
        super.setContainerColumns(info.getContainerColumns());
        super.setContainerSamplePermId(info.getContainerSamplePermId());
        super.setDatasetPermId(info.getDatasetPermId());
        super.setExperimentPermId(info.getExperimentPermId());
        this.storeChannelsOnExperimentLevel = storeChannelsOnExperimentLevel;
        this.imageDatasetInfo = imageDatasetInfo;
    }

    public boolean isStoreChannelsOnExperimentLevel()
    {
        return storeChannelsOnExperimentLevel;
    }

    public ImageDatasetInfo getImageDatasetInfo()
    {
        return imageDatasetInfo;
    }

}
