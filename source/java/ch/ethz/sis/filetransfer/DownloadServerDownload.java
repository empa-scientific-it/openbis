package ch.ethz.sis.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
class DownloadServerDownload
{

    private DownloadServerConfig config;

    private IUserSessionId userSessionId;

    private DownloadSessionId downloadSessionId = new DownloadSessionId();

    private List<IDownloadItemId> itemIds;

    private Map<IDownloadItemId, DownloadRange> ranges;

    private Map<IDownloadItemId, List<Chunk>> chunksByItemId;

    private Map<Integer, Chunk> chunksBySequenceNumber;

    private ChunkQueue queue;

    private Integer wishedNumberOfStreams;

    private int allowedNumberOfStreams;

    private Map<DownloadStreamId, DownloadInputStream> streams = new HashMap<DownloadStreamId, DownloadInputStream>();

    public DownloadServerDownload(DownloadServerConfig config, IUserSessionId userSessionId, List<IDownloadItemId> itemIds,
            Integer wishedNumberOfStreams, int allowedNumberOfStreams) throws DownloadException
    {
        this.config = config;
        this.userSessionId = userSessionId;
        this.itemIds = itemIds;
        this.wishedNumberOfStreams = wishedNumberOfStreams;
        this.allowedNumberOfStreams = allowedNumberOfStreams;

        this.chunksByItemId = initChunksByItemId(itemIds, config.getChunkProvider());
        this.chunksBySequenceNumber = initChunksBySequenceNumber(chunksByItemId);
        this.ranges = initRanges(chunksByItemId);
        this.queue = new ChunkQueue();

        for (int i = 0; i < allowedNumberOfStreams; i++)
        {
            streams.put(new DownloadStreamId(), null);
        }
    }

    private static Map<IDownloadItemId, List<Chunk>> initChunksByItemId(List<IDownloadItemId> itemIds, IChunkProvider chunksProvider)
            throws DownloadException
    {
        Map<IDownloadItemId, List<Chunk>> chunksByItemId = chunksProvider.getChunks(itemIds);

        if (chunksByItemId == null || chunksByItemId.isEmpty())
        {
            throw new IllegalArgumentException("Chunks for item ids: " + itemIds + " were null or empty.");
        }

        for (IDownloadItemId itemId : itemIds)
        {
            List<Chunk> itemChunks = chunksByItemId.get(itemId);

            if (itemChunks == null || itemChunks.isEmpty())
            {
                throw new IllegalArgumentException("Chunks for item id: " + itemId + " were null or empty.");
            }
        }

        return chunksByItemId;
    }

    private static Map<Integer, Chunk> initChunksBySequenceNumber(Map<IDownloadItemId, List<Chunk>> chunksByItemId)
    {
        Map<Integer, Chunk> chunksBySequenceNumber = new HashMap<Integer, Chunk>();

        for (List<Chunk> itemChunks : chunksByItemId.values())
        {
            for (Chunk itemChunk : itemChunks)
            {
                chunksBySequenceNumber.put(itemChunk.getSequenceNumber(), itemChunk);
            }
        }

        return chunksBySequenceNumber;
    }

    private static Map<IDownloadItemId, DownloadRange> initRanges(Map<IDownloadItemId, List<Chunk>> chunksByItemId)
    {
        Map<IDownloadItemId, DownloadRange> ranges = new HashMap<IDownloadItemId, DownloadRange>();

        for (Map.Entry<IDownloadItemId, List<Chunk>> entry : chunksByItemId.entrySet())
        {
            IDownloadItemId itemId = entry.getKey();
            List<Chunk> itemChunks = entry.getValue();

            int start = itemChunks.get(0).getSequenceNumber();
            int end = itemChunks.get(itemChunks.size() - 1).getSequenceNumber();

            ranges.put(itemId, new DownloadRange(start, end));
        }

        return ranges;
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
        return Collections.unmodifiableList(itemIds);
    }

    public Map<IDownloadItemId, DownloadRange> getRanges()
    {
        return Collections.unmodifiableMap(ranges);
    }

    public List<Chunk> getChunks(IDownloadItemId itemId)
    {
        List<Chunk> list = chunksByItemId.get(itemId);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    public List<DownloadStreamId> getStreamIds()
    {
        synchronized (streams)
        {
            return Collections.unmodifiableList(new ArrayList<DownloadStreamId>(streams.keySet()));
        }
    }

    public DownloadState getState()
    {
        int currentNumberOfStreams = 0;

        synchronized (streams)
        {
            for (DownloadInputStream stream : streams.values())
            {
                if (stream != null)
                {
                    currentNumberOfStreams++;
                }
            }
        }

        return new DownloadState(userSessionId, downloadSessionId, itemIds, wishedNumberOfStreams, allowedNumberOfStreams, currentNumberOfStreams);
    }

    public InputStream download(DownloadStreamId streamId, Integer numberOfChunksOrNull) throws InvalidDownloadStreamException, DownloadException
    {
        synchronized (streams)
        {
            if (false == streams.containsKey(streamId))
            {
                throw new InvalidDownloadStreamException(streamId);
            }

            DownloadInputStream stream = streams.get(streamId);

            if (stream != null)
            {
                try
                {
                    stream.close();
                } catch (IOException e)
                {
                    if (config.getLogger().isEnabled(LogLevel.WARN))
                    {
                        config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't close a download stream", e);
                    }
                }
            }

            stream = new DownloadInputStream(config.getLogger(), queue, config.getSerializerProvider().createChunkSerializer(), numberOfChunksOrNull);
            streams.put(streamId, stream);

            if (config.getLogger().isEnabled(LogLevel.DEBUG))
            {
                config.getLogger().log(getClass(), LogLevel.DEBUG, "Returning input stream with queue: " + queue);
            }

            return stream;
        }
    }

    public void queue(List<DownloadRange> ranges)
    {
        queue.offer(ranges);
    }

    public void finish()
    {
        synchronized (streams)
        {
            for (InputStream stream : streams.values())
            {
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    } catch (IOException e)
                    {
                        if (config.getLogger().isEnabled(LogLevel.WARN))
                        {
                            config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't close a download stream", e);
                        }
                    }
                }
            }
        }
    }

    private class ChunkQueue implements IChunkQueue
    {

        private Queue<Integer> queueList = new LinkedList<Integer>();

        private Set<Integer> queueSet = new HashSet<Integer>();

        @Override
        public synchronized Chunk poll()
        {
            Integer sequenceNumber = queueList.poll();

            if (sequenceNumber != null)
            {
                queueSet.remove(sequenceNumber);
                return chunksBySequenceNumber.get(sequenceNumber);
            } else
            {
                return null;
            }
        }

        public synchronized void offer(List<DownloadRange> ranges)
        {
            for (DownloadRange range : ranges)
            {
                for (int sequenceNumber = range.getStart(); sequenceNumber <= range.getEnd(); sequenceNumber++)
                {
                    if (chunksBySequenceNumber.containsKey(sequenceNumber))
                    {
                        if (false == queueSet.contains(sequenceNumber))
                        {
                            queueSet.add(sequenceNumber);
                            queueList.add(sequenceNumber);
                        } else
                        {
                            // do not add duplicates
                        }
                    } else
                    {
                        throw new IllegalArgumentException("Unknown chunk with sequence number: " + sequenceNumber);
                    }
                }
            }
        }

        @Override
        public synchronized String toString()
        {
            return queueList.toString();
        }

    }

}
