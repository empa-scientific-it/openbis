/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import java.io.Serializable;
import java.util.List;

/**
 * @author pkupczyk
 */
public class DownloadState implements Serializable
{

    private static final long serialVersionUID = 1L;

    private IUserSessionId userSessionId;

    private DownloadSessionId downloadSessionId;

    private List<IDownloadItemId> itemIds;

    private int wishedNumberOfStreams;

    private int allowedNumberOfStreams;

    private int currentNumberOfStreams;

    public DownloadState(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, List<IDownloadItemId> itemIds, int wishedNumberOfStreams,
            int allowedNumberOfStreams, int currentNumberOfStreams)
    {
        this.userSessionId = userSessionId;
        this.downloadSessionId = downloadSessionId;
        this.itemIds = itemIds;
        this.wishedNumberOfStreams = wishedNumberOfStreams;
        this.allowedNumberOfStreams = allowedNumberOfStreams;
        this.currentNumberOfStreams = currentNumberOfStreams;
    }

    public IUserSessionId getUserSessionId()
    {
        return userSessionId;
    }

    public DownloadSessionId getDownloadSessionId()
    {
        return downloadSessionId;
    }

    public List<IDownloadItemId> getItemIds()
    {
        return itemIds;
    }

    public int getWishedNumberOfStreams()
    {
        return wishedNumberOfStreams;
    }

    public int getAllowedNumberOfStreams()
    {
        return allowedNumberOfStreams;
    }

    public int getCurrentNumberOfStreams()
    {
        return currentNumberOfStreams;
    }

}
