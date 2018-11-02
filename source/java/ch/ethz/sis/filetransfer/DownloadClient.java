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
