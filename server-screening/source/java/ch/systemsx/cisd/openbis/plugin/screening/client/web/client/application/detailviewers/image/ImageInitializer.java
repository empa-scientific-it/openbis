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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.image;

import com.reveregroup.gwt.imagepreloader.FitImageLoadHandler;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LogicalImageClickHandler;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;

/**
 * @author pkupczyk
 */
public abstract class ImageInitializer
{

    private String sessionId;

    private LogicalImageChannelsReference channelReferences;

    private int imageWidth;

    private int imageHeight;

    private FitImageLoadHandler imageLoadHandler;

    private LogicalImageClickHandler imageClickHandler;

    public abstract int getImageRow();

    public abstract int getImageColumn();

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public LogicalImageChannelsReference getChannelReferences()
    {
        return channelReferences;
    }

    public void setChannelReferences(LogicalImageChannelsReference channelReferences)
    {
        this.channelReferences = channelReferences;
    }

    public int getImageWidth()
    {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth)
    {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight()
    {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight)
    {
        this.imageHeight = imageHeight;
    }

    public FitImageLoadHandler getImageLoadHandler()
    {
        return imageLoadHandler;
    }

    public void setImageLoadHandler(FitImageLoadHandler imageLoadHandler)
    {
        this.imageLoadHandler = imageLoadHandler;
    }

    public LogicalImageClickHandler getImageClickHandler()
    {
        return imageClickHandler;
    }

    public void setImageClickHandler(LogicalImageClickHandler imageClickHandler)
    {
        this.imageClickHandler = imageClickHandler;
    }

}
