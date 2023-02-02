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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto;

/**
 * Used to point to some channels of an image dataset (including overlays).
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetChannel
{
    private final String datasetCode;

    private final String datastoreHostUrl;

    private final String channelCode;

    public ImageDatasetChannel(String datasetCode, String datastoreHostUrl, String channelCode)
    {
        this.datasetCode = datasetCode;
        this.datastoreHostUrl = datastoreHostUrl;
        this.channelCode = channelCode;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getDatastoreHostUrl()
    {
        return datastoreHostUrl;
    }

    public String getChannelCode()
    {
        return channelCode;
    }
}
