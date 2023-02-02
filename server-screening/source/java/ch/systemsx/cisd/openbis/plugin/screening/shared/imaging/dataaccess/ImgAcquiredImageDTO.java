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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Tomasz Pylak
 */
public class ImgAcquiredImageDTO extends AbstractImageTransformerFactoryHolder
{
    @ResultColumn("IMG_ID")
    private long imageId;

    @ResultColumn("CHANNEL_STACK_ID")
    private long channelStackId;

    @ResultColumn("CHANNEL_ID")
    private long channelId;

    // can be null if there is no thumbnail
    @ResultColumn("THUMBNAIL_ID")
    private Long thumbnailId;

    public long getImageId()
    {
        return imageId;
    }

    public void setImageId(long imageId)
    {
        this.imageId = imageId;
    }

    public Long getThumbnailId()
    {
        return thumbnailId;
    }

    public void setThumbnailId(Long thumbnailId)
    {
        this.thumbnailId = thumbnailId;
    }

    public long getChannelStackId()
    {
        return channelStackId;
    }

    public void setChannelStackId(long channelStackId)
    {
        this.channelStackId = channelStackId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(long channelId)
    {
        this.channelId = channelId;
    }
}