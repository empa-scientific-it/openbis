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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A download client that can be used to download items from a given download server (see {@link IDownloadServer}). The client can handle multiple
 * download sessions and multiple users simultaneously. All the complexity related with a download process like:
 * <ul>
 * <li>chunk splicing and checksums</li>
 * <li>error handling and retry logic</li>
 * <li>threads and synchronization</li>
 * </ul>
 * is hidden from the end user and is handled automatically by the download client. To create a new download use
 * {@link #createDownload(IUserSessionId)} method. The method returns {@link DownloadClientDownload} object which represents a single download
 * session. Using that object we can define what items should be downloaded (see {@link DownloadClientDownload#addItem(IDownloadItemId)} method) and
 * what to do with these items once they get downloaded (see {@link DownloadClientDownload#addListener(IDownloadListener)} method). The overall
 * behavior of the download client can be customized via {@link DownloadClientConfig}.
 * 
 * @author pkupczyk
 */
public class DownloadClient
{

    private DownloadClientConfig config;

    private Map<IUserSessionId, List<DownloadClientDownload>> downloads = new HashMap<IUserSessionId, List<DownloadClientDownload>>();

    public DownloadClient(DownloadClientConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        if (config.getServer() == null)
        {
            throw new IllegalArgumentException("Server cannot be null");
        }

        if (config.getStore() == null)
        {
            throw new IllegalArgumentException("Store cannot be null");
        }

        if (config.getDeserializerProvider() == null)
        {
            throw new IllegalArgumentException("Deserializer provider cannot be null");
        }

        if (config.getLogger() == null)
        {
            config.setLogger(new NullLogger());
        }

        if (config.getRetryProvider() == null)
        {
            config.setRetryProvider(new DefaultRetryProvider(config.getLogger()));
        }

        this.config = config;
    }

    /**
     * Creates a download for a given user. One user can create multiple downloads that will run in parallel. For more details how to specify items to
     * be downloaded, define a download listener and start the actual download process please check {@link DownloadClientDownload} class
     * documentation.
     */
    public DownloadClientDownload createDownload(IUserSessionId userSessionId)
    {
        if (userSessionId == null)
        {
            throw new IllegalArgumentException("User session id cannot be null");
        }

        DownloadClientDownload download = new DownloadClientDownload(config, userSessionId);

        synchronized (downloads)
        {
            List<DownloadClientDownload> userDownloads = downloads.get(userSessionId);

            if (userDownloads == null)
            {
                userDownloads = new LinkedList<DownloadClientDownload>();
                downloads.put(userSessionId, userDownloads);
            }

            userDownloads.add(download);
        }

        return download;
    }

    /**
     * Returns a list of downloads for a given user.
     */
    public List<DownloadClientDownload> getDownloads(IUserSessionId userSessionId)
    {
        synchronized (downloads)
        {
            List<DownloadClientDownload> userDownloads = downloads.get(userSessionId);

            if (userDownloads == null || userDownloads.isEmpty())
            {
                return Collections.emptyList();
            } else
            {
                return Collections.unmodifiableList(userDownloads);
            }
        }
    }

}
