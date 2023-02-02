/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Pawel Glyzewski
 */
public class ImgImageZoomLevelTransformationEnrichedDTO extends ImgImageZoomLevelTransformationDTO
{
    @ResultColumn("image_transformation_code")
    private String imageTransformationCode;

    @ResultColumn("channel_code")
    private String channelCode;

    @ResultColumn("physical_dataset_perm_id")
    private String physicalDatasetPermId;

    @SuppressWarnings("unused")
    private ImgImageZoomLevelTransformationEnrichedDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageZoomLevelTransformationEnrichedDTO(String imageTransformationCode,
            String channelCode, String physicalDatasetPermId, long imageZoomLevelId,
            long channelId, long imageTransformationId)
    {
        super(imageZoomLevelId, channelId, imageTransformationId);

        this.imageTransformationCode = imageTransformationCode;
        this.channelCode = channelCode;
        this.physicalDatasetPermId = physicalDatasetPermId;
    }

    public String getTransformationCode()
    {
        return imageTransformationCode;
    }

    public void setTransformationCode(String transformationCode)
    {
        this.imageTransformationCode = transformationCode;
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    public String getPhysicalDatasetPermId()
    {
        return physicalDatasetPermId;
    }

    public void setPhysicalDatasetPermId(String physicalDatasetPermId)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
    }
}
