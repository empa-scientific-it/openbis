package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

public class MemoryCache<V> implements ITestableCache<V>
{

    private final int capacity;

    private final Map<String, ImmutablePair<Long, V>> cachedResults;

    private final Queue<String> keyQueue;

    private final Set<String> writingKeys = new CopyOnWriteArraySet<>();

    private final ITimeProvider timeProvider;

    public MemoryCache(final CacheOptionsVO cacheOptionsVO)
    {
        this.timeProvider = cacheOptionsVO.getTimeProvider();

        final int capacity = cacheOptionsVO.getCapacity();
        if (capacity < 0)
        {
            throw new RuntimeException("Capacity cannot be negative.");
        }

        this.capacity = capacity > 0 ? capacity : Integer.MAX_VALUE;
        cachedResults = capacity > 0 ? new ConcurrentHashMap<>(this.capacity) : new ConcurrentHashMap<>();
        keyQueue = capacity > 0 ? new ArrayDeque<>(this.capacity) : new ArrayDeque<>();
    }

    @Override
    public synchronized void put(final String key, final V value)
    {
        if (contains(key) == false && writingKeys.contains(key) == false)
        {
            writingKeys.add(key);

            final int cacheSize = cachedResults.size();
            final int queueSize = keyQueue.size();

            if (cacheSize != queueSize)
            {
                throw new RuntimeException(String.format("Cache size and queue size differ. "
                        + "[cashSize=%d, queueSize=%d]", cacheSize, queueSize));
            }

            if (cacheSize > capacity)
            {
                throw new RuntimeException(String.format("Cash has exceeded the allocated capacity. "
                        + "[cashSize=%d, capacity=%d]", cacheSize, capacity));
            }

            if (cacheSize == capacity)
            {
                final String removedKey = keyQueue.remove();
                final ImmutablePair<?, ?> removedValue = cachedResults.remove(removedKey);

                if (removedValue == null)
                {
                    throw new RuntimeException(String.format("The key removed from the queue cannot be found "
                            + "in the cache. [removedKey=%s]", removedKey));
                }
            }

            keyQueue.add(key);
            writingKeys.remove(key);
        }
        cachedResults.put(key, new ImmutablePair<>(timeProvider.getTimeInMilliseconds(), value));
    }

    @Override
    public V get(final String key)
    {
        final ImmutablePair<?, V> cachedResult = cachedResults.get(key);
        return cachedResult != null ? cachedResult.getRight() : null;
    }

    @Override
    public synchronized void remove(final String key)
    {
        keyQueue.remove(key);
        cachedResults.remove(key);
    }

    @Override
    public boolean contains(final String key)
    {
        return cachedResults.containsKey(key);
    }

    @Override
    public synchronized void clear()
    {
        cachedResults.clear();
        keyQueue.clear();
    }

    @Override
    public synchronized void clearOld(final long time)
    {
        for (final Iterator<Map.Entry<String, ImmutablePair<Long, V>>> iterator = cachedResults.entrySet().iterator();
                iterator.hasNext();)
        {
            final Map.Entry<String, ImmutablePair<Long, V>> entry = iterator.next();

            if (time > entry.getValue().getLeft())
            {
                iterator.remove();
                keyQueue.removeIf(s -> Objects.equals(s, entry.getKey()));
            }
        }
    }

    @Override
    public Map<String, ImmutablePair<Long, V>> getCachedResults()
    {
        return cachedResults;
    }

    @Override
    public Queue<String> getKeyQueue()
    {
        return keyQueue;
    }

}
