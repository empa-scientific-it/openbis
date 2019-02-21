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
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.StopWatch;

/**
 * A download. It can be created using {@link DownloadClient#createDownload(IUserSessionId)} method. Once created, items to be downloaded have to be
 * specified with {@link #addItem(IDownloadItemId)} and {@link #addItems(java.util.Collection)} methods. Downloads are asynchronous (i.e. run in the
 * background and do not block the current thread). Therefore, to handle download results or be notified about a failed download a listener has to be
 * registered (see {@link #addListener(IDownloadListener)}. A list of events a download listener can notify about is defined in
 * {@link IDownloadListener} interface. Once the items to be downloaded and the listener are specified we can finally start the actual download
 * process using {@link #start()} method. At this point a new download session is created at the download server and a user's session is validated.
 * Depending on the preferences (see {@link #setPreferences(DownloadPreferences)} method) the download is single-threaded or multi-threaded (in either
 * case it does not block the current thread). Increasing the number of threads i.e. increasing the number of wished download streams (via
 * {@link DownloadPreferences}) can improve the overall performance of the download, still will consume more resources at both download client and
 * download server sides. In case a download server is under a heavy load it may reject the wished number of streams and reduce the allowed
 * concurrency. All this happens automatically under the hood without a need of any reaction from the user of the download client.
 * 
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

    private Map<IDownloadItemId, Set<Integer>> chunksDownloaded = new HashMap<IDownloadItemId, Set<Integer>>();

    private List<DownloadThread> downloadThreads = new LinkedList<DownloadThread>();

    private List<IDownloadListener> listeners = new ArrayList<IDownloadListener>();

    private LinkedBlockingQueue<IListenerExecution> listenersQueue = new LinkedBlockingQueue<IListenerExecution>();

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

    private synchronized void setStatus(DownloadStatus newStatus, Collection<Exception> exceptions)
    {
        if (newStatus.equals(DownloadStatus.STARTED))
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
        } else if (newStatus.equals(DownloadStatus.FINISHED))
        {
            if (status.equals(DownloadStatus.STARTED))
            {
                status = DownloadStatus.FINISHED;
                finishDownloadSession();
                notifyDownloadFinished();
                finishDownloadThreads();
                finishListenersThread();
                if (config.getLogger().isEnabled(LogLevel.INFO))
                {
                    config.getLogger().log(getClass(), LogLevel.INFO, "Download state changed to: " + status);
                }
            }
        } else if (newStatus.equals(DownloadStatus.FAILED))
        {
            if (status.equals(DownloadStatus.NEW) || status.equals(DownloadStatus.STARTED))
            {
                status = DownloadStatus.FAILED;
                finishDownloadSession();
                notifyDownloadFailed(exceptions);
                finishDownloadThreads();
                finishListenersThread();
                if (config.getLogger().isEnabled(LogLevel.INFO))
                {
                    config.getLogger().log(getClass(), LogLevel.INFO, "Download state changed to: " + status);
                }
            }
        }
    }

    private synchronized void refreshStatus()
    {
        Collection<DownloadStatus> threadStatuses = new HashSet<DownloadStatus>();
        Collection<Exception> threadExceptions = new LinkedList<Exception>();

        for (DownloadThread downloadThread : downloadThreads)
        {
            threadStatuses.add(downloadThread.getStatus());
            threadExceptions.add(downloadThread.getException());
        }

        if (threadStatuses.contains(DownloadStatus.STARTED))
        {
            setStatus(DownloadStatus.STARTED, null);
        }

        if (threadStatuses.contains(DownloadStatus.FINISHED) && false == threadStatuses.contains(DownloadStatus.NEW)
                && false == threadStatuses.contains(DownloadStatus.STARTED))
        {
            setStatus(DownloadStatus.FINISHED, null);
        }

        if (threadStatuses.equals(Collections.singleton(DownloadStatus.FAILED)))
        {
            setStatus(DownloadStatus.FAILED, threadExceptions);
        }
    }

    public void start() throws DownloadException
    {
        if (status.equals(DownloadStatus.NEW))
        {
            setStatus(DownloadStatus.STARTED, null);
        } else
        {
            throw new IllegalStateException("Download has been already started.");
        }

        try
        {
            startDownloadSession();
            startListenersThread();
            startDownloadThreads();

        } catch (Exception e)
        {
            if (config.getLogger().isEnabled(LogLevel.ERROR))
            {
                config.getLogger().log(getClass(), LogLevel.ERROR, "Couldn't start download", e);
            }
            setStatus(DownloadStatus.FAILED, Collections.singleton(e));
            throw new DownloadException("Couldn't start a download", e, false);
        }
    }

    public void await()
    {
        try
        {
            await(null);
        } catch (TimeoutException e)
        {
            // never happens
        }
    }

    public void await(int timeoutInMillis) throws TimeoutException
    {
        await((Integer) timeoutInMillis);
    }

    private void await(Integer timeoutInMillisOrNull) throws TimeoutException
    {
        if (status.equals(DownloadStatus.NEW))
        {
            throw new IllegalStateException("Download has to be started before waiting for the results");
        }

        if (timeoutInMillisOrNull != null && timeoutInMillisOrNull <= 0)
        {
            throw new IllegalArgumentException("Timeout should be > 0");
        }

        List<Thread> threads = new ArrayList<Thread>();
        threads.addAll(downloadThreads);
        if (listenersThread != null)
        {
            threads.add(listenersThread);
        }

        Long timeoutTimeOrNull = timeoutInMillisOrNull != null ? System.currentTimeMillis() + timeoutInMillisOrNull : null;

        try
        {
            for (Thread thread : threads)
            {
                if (timeoutTimeOrNull != null)
                {
                    long millisLeft = timeoutTimeOrNull - System.currentTimeMillis();

                    if (millisLeft > 0)
                    {
                        thread.join(millisLeft);
                    } else
                    {
                        throw new TimeoutException();
                    }
                } else
                {
                    thread.join();
                }
            }
        } catch (InterruptedException e)
        {
            if (config.getLogger().isEnabled(LogLevel.WARN))
            {
                config.getLogger().log(getClass(), LogLevel.WARN, "Got interrupted while waiting for the results", e);
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public Map<IDownloadItemId, Path> getResults() throws DownloadException
    {
        if (false == status.equals(DownloadStatus.FINISHED))
        {
            throw new IllegalStateException(
                    "Results are available only for downloads that have successfully finished (current download status: " + status + ")");
        }

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

        return itemPaths;
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

        if (downloadSession.getRanges() != null)
        {
            for (Map.Entry<IDownloadItemId, DownloadRange> entry : downloadSession.getRanges().entrySet())
            {
                IDownloadItemId itemId = entry.getKey();
                DownloadRange itemRange = entry.getValue();

                Set<Integer> itemChunksToDownload = new LinkedHashSet<Integer>();
                for (int i = itemRange.getStart(); i <= itemRange.getEnd(); i++)
                {
                    itemChunksToDownload.add(i);
                }

                chunksToDownload.put(itemId, itemChunksToDownload);
            }

            if (config.getLogger().isEnabled(LogLevel.DEBUG))
            {
                config.getLogger().log(getClass(), LogLevel.DEBUG, "Item ids to download: " + itemIdsToDownload);
                config.getLogger().log(getClass(), LogLevel.DEBUG, "Chunks to download: " + chunksToDownload);
            }
        }
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
        if (downloadSession == null)
        {
            return;
        }

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
        if (listenersThread != null)
        {
            listenersThread.addFinishMarker();
        }
    }

    private void finishDownloadThreads()
    {
        for (DownloadThread thread : downloadThreads)
        {
            try
            {
                thread.interrupt();
            } catch (Exception e)
            {
                if (config.getLogger().isEnabled(LogLevel.WARN))
                {
                    config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't interrupt a download thread", e);
                }
            }
        }
    }

    private void notifyDownloadStarted()
    {
        notify(new IListenerExecution()
            {

                @Override
                public String getDescription()
                {
                    return "Download started";
                }

                @Override
                public void execute()
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
        notify(new IListenerExecution()
            {
                @Override
                public String getDescription()
                {
                    return "Download finished";
                }

                @Override
                public void execute()
                {
                    try
                    {
                        Map<IDownloadItemId, Path> itemPaths = getResults();

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
        notify(new IListenerExecution()
            {
                @Override
                public String getDescription()
                {
                    return "Download failed";
                }

                @Override
                public void execute()
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
        notify(new IListenerExecution()
            {
                @Override
                public String getDescription()
                {
                    return "Item started " + itemId;
                }

                public void execute()
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
        notify(new IListenerExecution()
            {
                @Override
                public String getDescription()
                {
                    return "Item finished " + itemId;
                }

                @Override
                public void execute()
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
                            config.getLogger().log(getClass(), LogLevel.WARN, "Couldn't notify listeners about finished item " + itemId, e);
                        }
                    }
                }
            });
    }

    private void notify(IListenerExecution listenerExecution)
    {
        if (listenersThread != null)
        {
            listenersQueue.add(listenerExecution);
        } else
        {
            try
            {
                listenerExecution.execute();
            } catch (Exception e)
            {
                if (config.getLogger().isEnabled(LogLevel.WARN))
                {
                    config.getLogger().log(getClass(), LogLevel.WARN, "Listener has thrown an exception", e);
                }
            }
        }
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
            IListenerExecution listener = null;

            while (false == isFinishMarker(listener))
            {
                try
                {
                    listener = listenersQueue.poll(1000, TimeUnit.MILLISECONDS);

                    if (listener != null)
                    {
                        StopWatch watch = new StopWatch();
                        watch.start();

                        listener.execute();

                        watch.stop();
                        if (config.getLogger().isEnabled(LogLevel.DEBUG))
                        {
                            config.getLogger().log(getClass(), LogLevel.DEBUG,
                                    "Took " + watch + " to execute listener '" + listener.getDescription() + "'");
                        }
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

        private boolean isFinishMarker(IListenerExecution listener)
        {
            return listener instanceof FinishMarker;
        }

        private class FinishMarker implements IListenerExecution
        {

            @Override
            public String getDescription()
            {
                return "Finish marker";
            }

            @Override
            public void execute()
            {
            }
        }

    }

    private interface IListenerExecution
    {

        public String getDescription();

        public void execute();

    }

    private class DownloadThread extends Thread
    {

        private DownloadStreamId streamId;

        private DownloadStatus status = DownloadStatus.STARTED;

        private Exception exception;

        public DownloadThread(int threadIndex, DownloadStreamId streamId)
        {
            super("download-" + downloadSession.getDownloadSessionId().getId() + "-" + (threadIndex + 1));
            setDaemon(true);
            this.streamId = streamId;
        }

        @Override
        public void run()
        {
            try
            {
                while (false == isInterrupted() && false == itemIdsToDownload.equals(itemIdsDownloaded))
                {
                    config.getRetryProvider().executeWithRetry(new IRetryAction<Void>()
                        {
                            @Override
                            public Void execute() throws DownloadException
                            {
                                DownloadInputStreamReader reader = null;

                                try
                                {
                                    reader = getChunkReader();
                                    Chunk chunk = null;

                                    while ((chunk = readChunk(reader)) != null)
                                    {
                                        storeChunk(chunk);
                                    }

                                    if (config.getLogger().isEnabled(LogLevel.DEBUG))
                                    {
                                        config.getLogger().log(getClass(), LogLevel.DEBUG, "Input stream finished");
                                    }
                                } finally
                                {
                                    if (reader != null)
                                    {
                                        closeChunkReader(reader);
                                    }
                                }

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
                    config.getLogger().log(getClass(), LogLevel.ERROR, "Download thread failed", e);
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
            DownloadClientDownload.this.refreshStatus();
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

        private Chunk readChunk(DownloadInputStreamReader reader) throws DownloadException
        {
            try
            {
                return reader.read();
            } catch (Exception e)
            {
                throw new DownloadException("Couldn't read a chunk: " + e.getMessage(), e, true);
            }
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
                    Set<Integer> itemChunksDownloaded = chunksDownloaded.get(chunk.getDownloadItemId());

                    if (itemChunksDownloaded == null)
                    {
                        itemChunksDownloaded = new LinkedHashSet<Integer>();
                        chunksDownloaded.put(chunk.getDownloadItemId(), itemChunksDownloaded);
                        notifyItemStarted(chunk.getDownloadItemId());
                    }

                    itemChunksToDownload.remove(chunk.getSequenceNumber());
                    itemChunksDownloaded.add(chunk.getSequenceNumber());

                    if (itemChunksToDownload.isEmpty())
                    {
                        itemIdsDownloaded.add(chunk.getDownloadItemId());
                        chunksToDownload.remove(chunk.getDownloadItemId());
                        notifyItemFinished(chunk.getDownloadItemId());
                    }
                }

                if (config.getLogger().isEnabled(LogLevel.DEBUG))
                {
                    config.getLogger().log(getClass(), LogLevel.DEBUG, "Item ids to download: " + itemIdsToDownload);
                    config.getLogger().log(getClass(), LogLevel.DEBUG, "Item ids downloaded: " + itemIdsDownloaded);
                    config.getLogger().log(getClass(), LogLevel.DEBUG, "Chunks to download: " + chunksToDownload);
                    config.getLogger().log(getClass(), LogLevel.DEBUG, "Chunks downloaded: " + chunksDownloaded);
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
