/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;

/**
 * Intermediate DTO containing tokens from which image info {@link ImageFileInfo} can be extracted (if one finds it useful).
 * 
 * @author Tomasz Pylak
 */
public class UnparsedImageFileInfo extends AbstractHashable
{
    // can be null
    private String wellLocationToken;

    private String tileLocationToken;

    private String channelToken;

    // can be null
    private String timepointToken;

    // can be null
    private String depthToken;

    // can be null
    private String seriesNumberToken;

    /** can be null */
    public String getWellLocationToken()
    {
        return wellLocationToken;
    }

    public void setWellLocationToken(String wellLocationToken)
    {
        this.wellLocationToken = wellLocationToken;
    }

    public String getTileLocationToken()
    {
        return tileLocationToken;
    }

    public void setTileLocationToken(String tileLocationToken)
    {
        this.tileLocationToken = tileLocationToken;
    }

    public String getChannelToken()
    {
        return channelToken;
    }

    public void setChannelToken(String channelToken)
    {
        this.channelToken = channelToken;
    }

    /** can be null */
    public String getTimepointToken()
    {
        return timepointToken;
    }

    public void setTimepointToken(String timepointToken)
    {
        this.timepointToken = timepointToken;
    }

    /** can be null */
    public String getDepthToken()
    {
        return depthToken;
    }

    public void setDepthToken(String depthToken)
    {
        this.depthToken = depthToken;
    }

    public String getSeriesNumberToken()
    {
        return seriesNumberToken;
    }

    public void setSeriesNumberToken(String seriesNumberToken)
    {
        this.seriesNumberToken = seriesNumberToken;
    }
}