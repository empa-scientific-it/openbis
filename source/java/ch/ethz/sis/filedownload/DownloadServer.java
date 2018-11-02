package ch.ethz.sis.filedownload;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

/**
 * @author pkupczyk
 */
public class DownloadServer implements IDownloadServer
{

    private DownloadServerConfig config;

    private Map<DownloadSessionId, DownloadServerDownload> downloads = new ConcurrentHashMap<DownloadSessionId, DownloadServerDownload>();

    public DownloadServer(DownloadServerConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        if (config.getSessionManager() == null)
        {
            throw new IllegalArgumentException("Session manager cannot be null");
        }

        if (config.getChunkProvider() == null)
        {
            throw new IllegalArgumentException("Chunks provider cannot be null");
        }

        if (config.getSerializerProvider() == null)
        {
            throw new IllegalArgumentException("Serializer provider cannot be null");
        }

        if (config.getConcurrencyProvider() == null)
        {
            throw new IllegalArgumentException("Concurrency provider cannot be null");
        }

        if (config.getLogger() == null)
        {
            config.setLogger(new NullLogger());
        }

        this.config = config;
    }

    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds,
            DownloadPreferences preferences) throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException
    {
        if (config.getLogger().isEnabled(LogLevel.INFO))
        {
            config.getLogger().log(getClass(), LogLevel.INFO,
                    "startDownloadSession - user session id: " + userSessionId + ", item ids: " + itemIds + ", preferences: " + preferences);
        }

        if (userSessionId == null)
        {
            throw new IllegalArgumentException("User session id cannot be null");
        }

        if (itemIds == null || itemIds.isEmpty())
        {
            throw new IllegalArgumentException("Item ids cannot be null or empty");
        }

        for (IDownloadItemId itemId : itemIds)
        {
            if (itemId == null)
            {
                throw new IllegalArgumentException("Item id cannot be null");
            }
        }

        if (preferences == null)
        {
            throw new IllegalArgumentException("Preferences cannot be null");
        }

        if (preferences.getWishedNumberOfStreams() != null && preferences.getWishedNumberOfStreams() <= 0)
        {
            throw new IllegalArgumentException("Wished number of streams cannot be <= 0");
        }

        config.getSessionManager().checkSessionId(userSessionId);

        Map<IUserSessionId, List<Integer>> allowedNumberOfStreamsMap = new HashMap<IUserSessionId, List<Integer>>();
        Map<IUserSessionId, List<Integer>> currentNumberOfStreamsMap = new HashMap<IUserSessionId, List<Integer>>();

        for (DownloadServerDownload download : downloads.values())
        {
            List<Integer> userAllowedNumberOfStreams = allowedNumberOfStreamsMap.get(download.getUserSessionId());
            List<Integer> userCurrentNumberOfStreams = currentNumberOfStreamsMap.get(download.getUserSessionId());

            if (userAllowedNumberOfStreams == null)
            {
                userAllowedNumberOfStreams = new LinkedList<Integer>();
                allowedNumberOfStreamsMap.put(download.getUserSessionId(), userAllowedNumberOfStreams);
            }

            if (userCurrentNumberOfStreams == null)
            {
                userCurrentNumberOfStreams = new LinkedList<Integer>();
                currentNumberOfStreamsMap.put(download.getUserSessionId(), userCurrentNumberOfStreams);
            }

            userAllowedNumberOfStreams.add(download.getAllowedNumberOfStreams());
            userCurrentNumberOfStreams.add(download.getCurrentNumberOfStreams());
        }

        int allowedNumberOfStreams = config.getConcurrencyProvider().getAllowedNumberOfStreams(userSessionId, preferences.getWishedNumberOfStreams(),
                allowedNumberOfStreamsMap, currentNumberOfStreamsMap);

        if (allowedNumberOfStreams <= 0)
        {
            throw new IllegalArgumentException("Allowed number of streams cannot be <= 0");
        }

        DownloadServerDownload download =
                new DownloadServerDownload(config, userSessionId, itemIds, preferences.getWishedNumberOfStreams(), allowedNumberOfStreams);
        downloads.put(download.getDownloadSessionId(), download);

        if (config.getLogger().isEnabled(LogLevel.INFO))
        {
            config.getLogger().log(getClass(), LogLevel.INFO, "startDownloadSession - downloadSessionId: " + download.getDownloadSessionId()
                    + ", ranges: " + download.getRanges() + ", allowedNumberOfStreams: " + download.getAllowedNumberOfStreams());
        }

        return new DownloadSession(download.getDownloadSessionId(), download.getRanges(), download.getStreamIds());
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        if (config.getLogger().isEnabled(LogLevel.INFO))
        {
            config.getLogger().log(getClass(), LogLevel.INFO, "queue - downloadSessionId: " + downloadSessionId + ", ranges: " + ranges);
        }

        if (downloadSessionId == null)
        {
            throw new IllegalArgumentException("Download session id cannot be null");
        }

        if (ranges == null || ranges.isEmpty())
        {
            throw new IllegalArgumentException("Ranges cannot be null or empty");
        }

        for (DownloadRange range : ranges)
        {
            if (range == null)
            {
                throw new IllegalArgumentException("Range cannot be null");
            }
        }

        DownloadServerDownload download = downloads.get(downloadSessionId);

        if (download == null)
        {
            throw new InvalidDownloadSessionException(downloadSessionId);
        }

        config.getSessionManager().checkSessionId(download.getUserSessionId());

        download.queue(ranges);
    }

    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        if (config.getLogger().isEnabled(LogLevel.INFO))
        {
            config.getLogger().log(getClass(), LogLevel.INFO, "download - downloadSessionId: " + downloadSessionId);
        }

        if (downloadSessionId == null)
        {
            throw new IllegalArgumentException("Download session id cannot be null");
        }

        if (streamId == null)
        {
            throw new IllegalArgumentException("Stream id cannot be null");
        }

        DownloadServerDownload download = downloads.get(downloadSessionId);

        if (download == null)
        {
            throw new InvalidDownloadSessionException(downloadSessionId);
        }

        config.getSessionManager().checkSessionId(download.getUserSessionId());

        return download.download(streamId, numberOfChunksOrNull);
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        if (config.getLogger().isEnabled(LogLevel.INFO))
        {
            config.getLogger().log(getClass(), LogLevel.INFO, "finishDownloadSession - downloadSessionId: " + downloadSessionId);
        }

        if (downloadSessionId == null)
        {
            throw new IllegalArgumentException("Download session id cannot be null");
        }

        DownloadServerDownload download = downloads.get(downloadSessionId);

        if (download != null)
        {
            download.finish();
            downloads.remove(downloadSessionId);
        }

    }

}
