package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import static ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_DEFAULT;
import static ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;

public class FileCache<V> implements ICache<V>
{
    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, FileCache.class);

    /** Name of the cache subfolder in session workspace. */
    private static final String CACHE_FOLDER_NAME = "cache";

    private final int capacity;

    private final Queue<String> keyQueue;

    private final Set<String> writingKeys = new CopyOnWriteArraySet<>();

    private final String cacheDirString;

    private final File cacheDir;

    public FileCache(final int capacity, final Properties serviceProperties, final String sessionToken)
    {
        if (capacity < 0)
        {
            throw new RuntimeException("capacity cannot be negative.");
        }

        this.capacity = capacity > 0 ? capacity : Integer.MAX_VALUE;
        keyQueue = capacity > 0 ? new ArrayDeque<>(this.capacity) : new ArrayDeque<>();

        cacheDirString = PropertyUtils.getProperty(serviceProperties, SESSION_WORKSPACE_ROOT_DIR_KEY,
                SESSION_WORKSPACE_ROOT_DIR_DEFAULT) + File.separator + CACHE_FOLDER_NAME + File.separator +
                sessionToken.replaceAll("\\W+", "");
        cacheDir = new File(cacheDirString);

        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }

        cacheDir.deleteOnExit();
    }

    @Override
    public synchronized void put(final String key, final V value)
    {
        if (!contains(key) && !writingKeys.contains(key))
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
            try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile)))
            {
                out.writeObject(value);
            } catch (final IOException e)
            {
                OPERATION_LOG.error(String.format("Error storing value in cache. [key=%s, value=%s]", key, value), e);
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
        try (final ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile)))
        {
            return (V) in.readObject();
        } catch (final IOException | ClassNotFoundException e)
        {
            OPERATION_LOG.error(String.format("Reading value from cache. [key=%s]", key), e);
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

    private File getCacheFile(final String key)
    {
        return new File(cacheDirString + File.separator + key);
    }

}
