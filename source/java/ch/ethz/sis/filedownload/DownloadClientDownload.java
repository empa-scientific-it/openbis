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

package ch.ethz.sis.filedownload;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author pkupczyk
 */
public class DownloadClientDownload
{

    private DownloadClientConfig config;

    private DownloadPreferences preferences = new DownloadPreferences();

    private IUserSessionId userSessionId;

    private DownloadSession downloadSession;

    private Set<IDownloadItemId> itemIdsToDownload = new LinkedHashSet<IDownloadItemId>();

    private Set<IDownloadItemId> itemIdsDownloaded = new LinkedHashSet<IDownloadItemId>();

    private Map<IDownloadItemId, Set<Integer>> chunksToDownload = new HashMap<IDownloadItemId, Set<Integer>>();

    private List<DownloadThread> downloadThreads = new LinkedList<DownloadThread>();

    private List<IDownloadListener> listeners = new ArrayList<IDownloadListener>();

    private LinkedBlockingQueue<Runnable> listenersQueue = new LinkedBlockingQueue<Runnable>();

    private ListenersThread listenersThread;

    private DownloadStatus status = DownloadStatus.NEW;

    DownloadClientDownload(DownloadClientConfig config, IUserSessionId userSessionId)
    {
        this.config = config;
        this.userSessionId = userSessionId;
    }

    public IUserSessionId getUserSession()
    {
        return userSessionId;
    }

    public DownloadSessionId getDownloadSessionId()
    {
        if (status.equals(DownloadStatus.NEW))
        {
            throw new IllegalStateException("Download session id cannot be read before a download is started.");
        }
        return downloadSession != null ? downloadSession.getDownloadSessionId() : null;
    }

    public void addItem(IDownloadItemId itemId)
    {
        addItems(Collections.singleton(itemId));
    }

    public void addItems(Collection<IDownloadItemId> itemIds)
    {
        if (itemIds == null)
        {
            throw new IllegalArgumentException("Item ids cannot be null");
        }
        if (false == status.equals(DownloadStatus.NEW))
        {
            throw new IllegalStateException("Item ids cannot be added once a download is started.");
        }
        for (IDownloadItemId itemId : itemIds)
        {
            if (itemId == null)
            {
                throw new IllegalArgumentException("Item id cannot be null");
            }
            this.itemIdsToDownload.add(itemId);
        }
    }

    public List<IDownloadItemId> getItems()
    {
        return Collections.unmodifiableList(new ArrayList<IDownloadItemId>(itemIdsToDownload));
    }

    public void addListener(IDownloadListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
    }

    public void setPreferences(DownloadPreferences preferences)
    {
        if (preferences == null)
        {
            throw new IllegalArgumentException("Preferences cannot be null");
        }
        if (false == status.equals(DownloadStatus.NEW))
        {
            throw new IllegalStateException("Preferences cannot be set once a download is started.");
        }
        this.preferences = preferences;
    }

    public DownloadPreferences gePreferences()
    {
        return this.preferences;
    }

    public DownloadStatus getStatus()
    {
        return status;
    }

    public void start() throws DownloadException
    {
        if (false == status.equals(DownloadStatus.NEW))
        {
            throw new IllegalStateException("Download has been already started.");
        }

        startDownloadSession();
        startListenersThread();
        startDownloadThreads();
    }

    private void startListenersThread()
    {
        listenersThread = new ListenersThread();
        listenersThread.start();
    }

    private void startDownloadSession() throws DownloadException
    {
        if (itemIdsToDownload == null || itemIdsToDownload.isEmpty())
        {
            throw new DownloadException("Item ids cannot be null or empty", false);
        }
        if (preferences == null)
        {
            throw new DownloadException("Preferences cannot be null", false);
        }

        config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
            {
                @Override
                public Void execute() throws DownloadException
                {
                    downloadSession =
                            config.getServer().startDownloadSession(userSessionId, new ArrayList<IDownloadItemId>(itemIdsToDownload), preferences);
                    return null;
                }
            });

        config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
            {
                @Override
                public Void execute() throws DownloadException
                {
                    config.getServer().queue(downloadSession.getDownloadSessionId(),
                            new ArrayList<DownloadRange>(downloadSession.getRanges().values()));
                    return null;
                }
            });
    }

    private void startDownloadThreads()
    {
        for (int threadIndex = 0; threadIndex < downloadSession.getStreamIds().size(); threadIndex++)
        {
            DownloadStreamId streamId = downloadSession.getStreamIds().get(threadIndex);
            DownloadThread thread = new DownloadThread(threadIndex, streamId);
            downloadThreads.add(thread);
        }

        for (DownloadThread downloadThread : downloadThreads)
        {
            downloadThread.start();
        }
    }

    private void finishDownloadSession()
    {
        try
        {
            config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
                {
                    @Override
                    public Void execute() throws DownloadException
                    {
                        config.getServer().finishDownloadSession(downloadSession.getDownloadSessionId());
                        return null;
                    }
                });
        } catch (DownloadException e)
        {
            if (config.getLogger().isEnabled(LogLevel.WARN))
            {
                config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't finish a download session: " + downloadSession.getDownloadSessionId(), e);
            }
        }
    }

    private void finishListenersThread()
    {
        listenersThread.addFinishMarker();
    }

    private synchronized void updateStatus()
    {
        Collection<DownloadStatus> statuses = new HashSet<DownloadStatus>();
        Collection<Exception> exceptions = new LinkedList<Exception>();

        for (DownloadThread downloadThread : downloadThreads)
        {
            statuses.add(downloadThread.getStatus());
            exceptions.add(downloadThread.getException());
        }

        if (statuses.contains(DownloadStatus.STARTED))
        {
            if (status.equals(DownloadStatus.NEW))
            {
                status = DownloadStatus.STARTED;
                notifyDownloadStarted();
                if (config.getLogger().isEnabled(LogLevel.INFO))
                {
                    config.getLogger().log(getClass(), LogLevel.INFO, "Download state changed to: " + status);
                }
            }
        } else if (statuses.equals(Collections.singleton(DownloadStatus.FAILED)))
        {
            if (status.equals(DownloadStatus.STARTED))
            {
                status = DownloadStatus.FAILED;
                finishDownloadSession();
                notifyDownloadFailed(exceptions);
                finishListenersThread();
                if (config.getLogger().isEnabled(LogLevel.INFO))
                {
                    config.getLogger().log(getClass(), LogLevel.INFO, "Download state changed to: " + status);
                }
            }
        } else if (statuses.equals(Collections.singleton(DownloadStatus.FINISHED)))
        {
            if (status.equals(DownloadStatus.STARTED))
            {
                status = DownloadStatus.FINISHED;
                finishDownloadSession();
                notifyDownloadFinished();
                finishListenersThread();
                if (config.getLogger().isEnabled(LogLevel.INFO))
                {
                    config.getLogger().log(getClass(), LogLevel.INFO, "Download state changed to: " + status);
                }
            }
        }
    }

    private void notifyDownloadStarted()
    {
        listenersQueue.add(new Runnable()
            {
                @Override
                public void run()
                {
                    for (IDownloadListener listener : listeners)
                    {
                        listener.onDownloadStarted();
                    }
                }
            });
    }

    private void notifyDownloadFinished()
    {
        listenersQueue.add(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Map<IDownloadItemId, Path> itemPaths = new HashMap<IDownloadItemId, Path>();

                        for (IDownloadItemId itemId : itemIdsDownloaded)
                        {
                            Path itemPath = config.getRetryProvider().executeWithRetry(new IRetryAction<Path>()
                                {
                                    @Override
                                    public Path execute() throws DownloadException
                                    {
                                        return config.getStore().getItemPath(userSessionId, downloadSession.getDownloadSessionId(), itemId);
                                    }
                                });

                            itemPaths.put(itemId, itemPath);
                        }

                        for (IDownloadListener listener : listeners)
                        {
                            listener.onDownloadFinished(itemPaths);
                        }
                    } catch (DownloadException e)
                    {
                        if (config.getLogger().isEnabled(LogLevel.WARN))
                        {
                            config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't notify listeners about a finished download", e);
                        }
                    }
                }
            });
    }

    private void notifyDownloadFailed(Collection<Exception> e)
    {
        listenersQueue.add(new Runnable()
            {
                @Override
                public void run()
                {
                    for (IDownloadListener listener : listeners)
                    {
                        listener.onDownloadFailed(e);
                    }
                }
            });
    }

    private void notifyItemStarted(IDownloadItemId itemId)
    {
        listenersQueue.add(new Runnable()
            {
                public void run()
                {
                    for (IDownloadListener listener : listeners)
                    {
                        listener.onItemStarted(itemId);
                    }
                }
            });
    }

    private void notifyItemFinished(IDownloadItemId itemId)
    {
        listenersQueue.add(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Path itemPath = config.getRetryProvider().executeWithRetry(new IRetryAction<Path>()
                            {
                                @Override
                                public Path execute() throws DownloadException
                                {
                                    return config.getStore().getItemPath(userSessionId, downloadSession.getDownloadSessionId(), itemId);
                                }
                            });

                        for (IDownloadListener listener : listeners)
                        {
                            listener.onItemFinished(itemId, itemPath);
                        }
                    } catch (DownloadException e)
                    {
                        if (config.getLogger().isEnabled(LogLevel.WARN))
                        {
                            config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't notify listeners about a finished item", e);
                        }
                    }
                }
            });
    }

    private class ListenersThread extends Thread
    {

        public ListenersThread()
        {
            super("download-listeners-" + downloadSession.getDownloadSessionId());
            setDaemon(true);
        }

        @Override
        public void run()
        {
            Runnable listener = null;

            while (false == isFinishMarker(listener))
            {
                try
                {
                    listener = listenersQueue.poll(1000, TimeUnit.MILLISECONDS);

                    if (listener != null)
                    {
                        listener.run();
                    }
                } catch (Exception e)
                {
                    if (config.getLogger().isEnabled(LogLevel.WARN))
                    {
                        config.getLogger().log(getClass(), LogLevel.WARN, "Listener has thrown an exception", e);
                    }
                }
            }
        }

        private void addFinishMarker()
        {
            listenersQueue.add(new FinishMarker());
        }

        private boolean isFinishMarker(Runnable listener)
        {
            return listener instanceof FinishMarker;
        }

        private class FinishMarker implements Runnable
        {

            @Override
            public void run()
            {
            }
        }

    }

    private class DownloadThread extends Thread
    {

        private DownloadStreamId streamId;

        private DownloadStatus status = DownloadStatus.NEW;

        private Exception exception;

        public DownloadThread(int threadIndex, DownloadStreamId streamId)
        {
            super("download-" + downloadSession.getDownloadSessionId().getUuid() + "-" + (threadIndex + 1));
            setDaemon(true);
            this.streamId = streamId;
        }

        @Override
        public void run()
        {
            setStatus(DownloadStatus.STARTED);

            try
            {
                while (false == itemIdsToDownload.equals(itemIdsDownloaded))
                {
                    config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
                        {
                            @Override
                            public Void execute() throws DownloadException
                            {
                                DownloadInputStreamReader reader = getChunkReader();
                                Chunk chunk = null;

                                while ((chunk = readChunk(reader)) != null)
                                {
                                    storeChunk(chunk);
                                }

                                closeChunkReader(reader);
                                requeueChunks();

                                return null;
                            }
                        });
                }

                setStatus(DownloadStatus.FINISHED);

            } catch (Exception e)
            {
                if (config.getLogger().isEnabled(LogLevel.ERROR))
                {
                    config.getLogger().log(getClass(), LogLevel.ERROR, "Download failed", e);
                }
                setStatus(DownloadStatus.FAILED);
                setException(e);
            }
        }

        private DownloadStatus getStatus()
        {
            return status;
        }

        private void setStatus(DownloadStatus status)
        {
            this.status = status;
            DownloadClientDownload.this.updateStatus();
        }

        private void setException(Exception exception)
        {
            this.exception = exception;
        }

        private Exception getException()
        {
            return exception;
        }

        private DownloadInputStreamReader getChunkReader() throws DownloadException
        {
            return config.getRetryProvider().executeWithRetry(new IRetryAction<DownloadInputStreamReader>()
                {
                    @Override
                    public DownloadInputStreamReader execute() throws DownloadException
                    {
                        InputStream stream = config.getServer().download(downloadSession.getDownloadSessionId(), streamId, null);
                        return new DownloadInputStreamReader(config.getLogger(), stream, config.getDeserializerProvider().createChunkDeserializer());
                    }
                });
        }

        private void closeChunkReader(DownloadInputStreamReader reader)
        {
            try
            {
                reader.close();
            } catch (Exception e)
            {
                if (config.getLogger().isEnabled(LogLevel.WARN))
                {
                    config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't close a download stream reader", e);
                }
            }
        }

        private Chunk readChunk(DownloadInputStreamReader reader) throws DownloadException
        {
            try
            {
                return reader.read();
            } catch (Exception e)
            {
                throw new DownloadException("Couldn't read a chunk", e, true);
            }
        }

        private void storeChunk(Chunk chunk) throws DownloadException
        {
            config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
                {
                    @Override
                    public Void execute() throws DownloadException
                    {
                        config.getStore().storeChunk(userSessionId, downloadSession.getDownloadSessionId(), chunk);
                        return null;
                    }
                });

            synchronized (DownloadClientDownload.this)
            {
                if (false == itemIdsDownloaded.contains(chunk.getDownloadItemId()))
                {
                    Set<Integer> itemChunksToDownload = chunksToDownload.get(chunk.getDownloadItemId());

                    if (itemChunksToDownload == null)
                    {
                        DownloadRange itemRange = downloadSession.getRanges().get(chunk.getDownloadItemId());

                        itemChunksToDownload = new LinkedHashSet<Integer>();
                        for (int i = itemRange.getStart(); i <= itemRange.getEnd(); i++)
                        {
                            itemChunksToDownload.add(i);
                        }

                        chunksToDownload.put(chunk.getDownloadItemId(), itemChunksToDownload);
                        notifyItemStarted(chunk.getDownloadItemId());
                    }

                    itemChunksToDownload.remove(chunk.getSequenceNumber());

                    if (itemChunksToDownload.isEmpty())
                    {
                        itemIdsDownloaded.add(chunk.getDownloadItemId());
                        chunksToDownload.remove(chunk.getDownloadItemId());
                        notifyItemFinished(chunk.getDownloadItemId());
                    }
                }
            }
        }

        private void requeueChunks() throws DownloadException
        {
            List<DownloadRange> ranges = new LinkedList<DownloadRange>();

            synchronized (DownloadClientDownload.this)
            {
                for (Set<Integer> itemChunksToDownload : chunksToDownload.values())
                {
                    if (itemChunksToDownload.isEmpty())
                    {
                        continue;
                    }

                    Iterator<Integer> iterator = itemChunksToDownload.iterator();
                    Integer start = iterator.next();
                    Integer end = start;

                    while (iterator.hasNext())
                    {
                        Integer current = iterator.next();

                        if (current == end + 1)
                        {
                            end = current;
                        } else
                        {
                            ranges.add(new DownloadRange(start, end));
                            start = current;
                            end = current;
                        }
                    }

                    ranges.add(new DownloadRange(start, end));
                }
            }

            if (false == ranges.isEmpty())
            {
                config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
                    {
                        @Override
                        public Void execute() throws DownloadException
                        {
                            config.getServer().queue(downloadSession.getDownloadSessionId(), ranges);
                            return null;
                        }
                    });
            }
        }

    }

}
