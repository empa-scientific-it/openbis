/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

public class FileCache<V> implements ITestableCache<V>
{
    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, FileCache.class);

    /** Whether at least one instance of this cache has been created. */
    private static boolean instanceCreated = false;

    private final int capacity;

    private final Queue<String> keyQueue;

    private final Set<String> writingKeys = new CopyOnWriteArraySet<>();

    private final String cacheDirPath;

    private final File cacheDir;

    /** If true cache values will be stored asynchronously in a separate thread. */
    private final boolean asyncStorage;

    private final ITimeProvider timeProvider;

    public FileCache(final CacheOptionsVO cacheOptionsVO)
    {
        this(cacheOptionsVO, PropertyUtils.getProperty(cacheOptionsVO.getServiceProperties(),
                OperationExecutionConfig.CACHE_DIRECTORY, OperationExecutionConfig.CACHE_DIRECTORY_DEFAULT) +
                File.separator + cacheOptionsVO.getSessionToken().replaceAll("\\W+", ""));
    }

    FileCache(final CacheOptionsVO cacheOptionsVO, final String cacheDirPath)
    {
        this.timeProvider = cacheOptionsVO.getTimeProvider();
        this.asyncStorage = cacheOptionsVO.isAsyncStorage();

        final int capacity = cacheOptionsVO.getCapacity();
        if (capacity < 0)
        {
            throw new RuntimeException("Capacity cannot be negative.");
        }

        this.capacity = capacity > 0 ? capacity : Integer.MAX_VALUE;
        this.keyQueue = capacity > 0 ? new ArrayDeque<>(this.capacity) : new ArrayDeque<>();

        this.cacheDirPath = cacheDirPath;
        this.cacheDir = new File(cacheDirPath);

        if (!instanceCreated)
        {
            instanceCreated = true;
            deleteDir(cacheDir);
        }
        this.cacheDir.mkdirs();

        this.cacheDir.deleteOnExit();
    }

    @Override
    public synchronized void put(final String key, final V value)
    {
        if (writingKeys.contains(key) == false)
        {
            writingKeys.add(key);

            final boolean containsKey = contains(key);
            if (containsKey == false)
            {
                final int queueSize = keyQueue.size();

                if (queueSize > capacity)
                {
                    throw new RuntimeException(String.format("Cash has exceeded the allocated capacity. "
                            + "[queueSize=%d, capacity=%d]", queueSize, capacity));
                }

                if (queueSize == capacity)
                {
                    final String removedKey = keyQueue.remove();
                    final boolean deleted = getCacheFile(removedKey).delete();

                    if (!deleted)
                    {
                        final String message = getCacheFile(removedKey).exists()
                                ? String.format("The key removed from the queue cannot be removed from the cache. "
                                + "[removedKey=%s]", removedKey)
                                : String.format("Cache file to remove is not found. [removedKey=%s]", removedKey);
                        throw new RuntimeException(message);
                    }
                }
            }

            storeValue(key, value);

            if (containsKey == false)
            {
                keyQueue.add(key);
            }

            writingKeys.remove(key);
        }
    }

    private void storeValue(final String key, final V value)
    {
        final File cacheFile = getCacheFile(key);
        cacheFile.deleteOnExit();

        final Runnable fileOutputRunnable = () ->
        {
            try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile)))
            {
                out.writeObject(new ImmutablePair<>(timeProvider.getTimeInMilliseconds(), value));
            } catch (final IOException e)
            {
                OPERATION_LOG.error(String.format("Error storing value in cache. [key=%s, value=%s]", key, value),
                        e);
            }
        };

        if (asyncStorage)
        {
            new Thread(fileOutputRunnable).start();
        } else
        {
            fileOutputRunnable.run();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V get(final String key)
    {
        final File cacheFile = getCacheFile(key);

        if (cacheFile.isFile())
        {
            try (final ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile)))
            {
                final ImmutablePair<Long, V> cachedResult = (ImmutablePair<Long, V>) in.readObject();
                return cachedResult != null ? cachedResult.getRight() : null;
            } catch (final IOException | ClassNotFoundException e)
            {
                OPERATION_LOG.error(String.format("Error reading value from cache. [key=%s]", key), e);
                return null;
            }
        } else
        {
            return null;
        }
    }

    @Override
    public synchronized void remove(final String key)
    {
        keyQueue.remove(key);
        getCacheFile(key).delete();
    }

    @Override
    public boolean contains(final String key)
    {
        return getCacheFile(key).isFile();
    }

    @Override
    public synchronized void clear()
    {
        keyQueue.clear();
        try
        {
            FileUtils.cleanDirectory(cacheDir);
        } catch (final IOException e)
        {
            throw new RuntimeException(String.format("Cannot clean cache. [cacheDir=%s]", cacheDir), e);
        }
    }

    @Override
    public void clearOld(final long time)
    {
        final File cacheDir = new File(cacheDirPath);

        final File[] files = cacheDir.listFiles();

        for (final File file : files)
        {
            ImmutablePair<Long, V> cachedResult;
            try (final ObjectInputStream in = new ObjectInputStream(new FileInputStream(file)))
            {
                cachedResult = (ImmutablePair<Long, V>) in.readObject();
            } catch (final IOException | ClassNotFoundException e)
            {
                OPERATION_LOG.error(String.format("Error reading value from file. [file=%s]", file), e);
                cachedResult = null;
            }

            if (cachedResult != null && time > cachedResult.getLeft())
            {
                file.delete();
                keyQueue.removeIf(s -> Objects.equals(s, file.getName()));
            }
        }
    }

    private File getCacheFile(final String key)
    {
        return new File(cacheDirPath + File.separator + key.replaceAll("[^\\w-]+", ""));
    }

    private static void deleteDir(final File dir) {
        final File[] files = dir.listFiles();
        if (files != null)
        {
            Arrays.stream(files).forEach(FileCache::deleteDir);
        }
        dir.delete();
    }

    @Override
    public Map<String, ImmutablePair<Long, V>> getCachedResults()
    {
        return null;
    }

    @Override
    public Queue<String> getKeyQueue()
    {
        return keyQueue;
    }

}
