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
public class ImgImageZoomLevelTransformationDTO extends AbstractImgIdentifiable
{
    @ResultColumn("zoom_level_id")
    private long imageZoomLevelId;

    @ResultColumn("channel_id")
    private long channelId;

    @ResultColumn("image_transformation_id")
    private long imageTransformationId;

    protected ImgImageZoomLevelTransformationDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageZoomLevelTransformationDTO(long imageZoomLevelId, long channelId,
            long imageTransformationId)
    {
        this.imageZoomLevelId = imageTransformationId;
        this.channelId = channelId;
        this.imageTransformationId = imageTransformationId;
    }

    public long getImageZoomLevelId()
    {
        return imageZoomLevelId;
    }

    public void setImageZoomLevelId(long imageZoomLevelId)
    {
        this.imageZoomLevelId = imageZoomLevelId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(long channelId)
    {
        this.channelId = channelId;
    }

    public long getImageTransformationId()
    {
        return imageTransformationId;
    }

    public void setImageTransformationId(long imageTransformationId)
    {
        this.imageTransformationId = imageTransformationId;
    }
}
