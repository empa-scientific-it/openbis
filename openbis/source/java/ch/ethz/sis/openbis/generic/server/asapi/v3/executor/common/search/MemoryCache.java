package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MemoryCache implements ICache
{

    private final int capacity;

    private final Map<String, Collection<Long>> cachedResults;

    private final Queue<String> hashCodeQueue;

    public MemoryCache(final int capacity)
    {
        if (capacity <= 0)
        {
            throw new RuntimeException("capacity should be a positive number.");
        }

        this.capacity = capacity;
        cachedResults = new HashMap<>(capacity);
        hashCodeQueue = new ArrayDeque<>(capacity);
    }

    @Override
    public void add(final String hashCode, final Collection<Long> results)
    {
        if (!isCacheAvailable(hashCode))
        {
            final int cacheSize = cachedResults.size();
            final int queueSize = hashCodeQueue.size();

            if (cacheSize != queueSize)
            {
                throw new RuntimeException(String.format("Cache size and queue size differ. "
                        + "[cashSize=%d, queueSize=%d]", cacheSize, queueSize));
            }

            if (cacheSize > capacity)
            {
                throw new RuntimeException("Cash has exceeded the allocated capacity.");
            }

            if (cacheSize == capacity)
            {
                final String removedHashCode = hashCodeQueue.remove();
                final Collection<Long> removedValue = cachedResults.remove(removedHashCode);

                if (removedValue == null)
                {
                    throw new RuntimeException(String.format("The removed from the queue hash code cannot be found "
                            + "in the cache. [removedHashCode=%s]", removedHashCode));
                }
            }

            hashCodeQueue.add(hashCode);
            cachedResults.put(hashCode, results);
        } else
        {
            cachedResults.put(hashCode, results);
        }
    }

    @Override
    public Collection<Long> get(final String hashCode)
    {
        return cachedResults.get(hashCode);
    }

    @Override
    public boolean isCacheAvailable(final String hashCode)
    {
        return cachedResults.containsKey(hashCode);
    }

    @Override
    public void clear()
    {
        cachedResults.clear();
        hashCodeQueue.clear();
    }

}
