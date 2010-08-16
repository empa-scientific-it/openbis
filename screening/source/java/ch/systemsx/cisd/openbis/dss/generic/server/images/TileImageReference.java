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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * DTO to point to a screening image or its thumbnail, optionally with all channels merged.
 * 
 * @author Tomasz Pylak
 */
public class TileImageReference
{
    protected String sessionId;

    // original size if null
    protected Size thumbnailSizeOrNull;

    protected String datasetCode;

    protected ImageChannelStackReference channelStackReference;

    protected boolean mergeAllChannels;

    /** contains the channel name or {@link ScreeningConstants#MERGED_CHANNELS} */
    protected String channel;

    public String getSessionId()
    {
        return sessionId;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public Size tryGetThumbnailSize()
    {
        return thumbnailSizeOrNull;
    }

    public boolean isMergeAllChannels()
    {
        return mergeAllChannels;
    }

    public String getChannel()
    {
        return channel;
    }

    public ImageChannelStackReference getChannelStack()
    {
        return channelStackReference;
    }
}