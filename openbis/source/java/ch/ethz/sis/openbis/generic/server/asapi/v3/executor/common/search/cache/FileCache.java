package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;

public class FileCache<V> implements ICache<V>
{
    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, FileCache.class);

    /** Whether at least one instance of this cache has been created. */
    private static boolean instanceCreated = false;

    private final int capacity;

    private final Queue<String> keyQueue;

    private final Set<String> writingKeys = new CopyOnWriteArraySet<>();

    private final String cacheDirString;

    private final File cacheDir;

    /** If true cache values will be stored asynchronously in a separate thread. */
    private final boolean asyncStorage;

    public FileCache(final CacheOptionsVO cacheOptionsVO)
    {
        this.asyncStorage = cacheOptionsVO.isAsyncStorage();

        final int capacity = cacheOptionsVO.getCapacity();
        if (capacity < 0)
        {
            throw new RuntimeException("capacity cannot be negative.");
        }

        this.capacity = capacity > 0 ? capacity : Integer.MAX_VALUE;
        keyQueue = capacity > 0 ? new ArrayDeque<>(this.capacity) : new ArrayDeque<>();

        cacheDirString = PropertyUtils.getProperty(cacheOptionsVO.getServiceProperties(),
                OperationExecutionConfig.CACHE_DIRECTORY, OperationExecutionConfig.CACHE_DIRECTORY_DEFAULT) +
                File.separator + cacheOptionsVO.getSessionToken().replaceAll("\\W+", "");
        cacheDir = new File(cacheDirString);

        if (!instanceCreated)
        {
            instanceCreated = true;
            deleteDir(cacheDir);
        }
        cacheDir.mkdirs();

        cacheDir.deleteOnExit();
    }

    @Override
    public synchronized void put(final String key, final V value)
    {
        if (contains(key) == false && writingKeys.contains(key) == false)
        {
            writingKeys.add(key);

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

            final File cacheFile = getCacheFile(key);
            cacheFile.deleteOnExit();

            final Runnable fileOutputRunnable = () ->
            {
                try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile)))
                {
                    out.writeObject(new ImmutablePair<>(new Date(), value));
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

            keyQueue.add(key);
            writingKeys.remove(key);
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
                final ImmutablePair<Date, V> cachedResult = (ImmutablePair<Date, V>) in.readObject();
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
    public void remove(final String key)
    {
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
        cacheDir.delete();
        cacheDir.mkdir();
    }

    @Override
    public void clearOld(final Date date)
    {
        final File cacheDir = new File(cacheDirString);

        final File[] files = cacheDir.listFiles();

        for (final File file : files)
        {
            ImmutablePair<Date, V> cachedResult;
            try (final ObjectInputStream in = new ObjectInputStream(new FileInputStream(file)))
            {
                cachedResult = (ImmutablePair<Date, V>) in.readObject();
            } catch (final IOException | ClassNotFoundException e)
            {
                OPERATION_LOG.error(String.format("Error reading value from file. [file=%s]", file), e);
                cachedResult = null;
            }

            if (cachedResult != null && date.after(cachedResult.getLeft()))
            {
                file.delete();
                keyQueue.removeIf(s -> Objects.equals(s, file.getName()));
            }
        }
    }

    private File getCacheFile(final String key)
    {
        return new File(cacheDirString + File.separator + key);
    }

    private static void deleteDir(final File dir) {
        final File[] files = dir.listFiles();
        if (files != null)
        {
            Arrays.stream(files).forEach(FileCache::deleteDir);
        }
        dir.delete();
    }

}
