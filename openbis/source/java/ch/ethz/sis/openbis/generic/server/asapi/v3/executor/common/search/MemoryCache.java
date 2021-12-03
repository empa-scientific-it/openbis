package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

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

public class MemoryCache<V> implements ICache<V>
{

    private final int capacity;

    private final Map<String, ImmutablePair<Date, V>> cachedResults;

    private final Queue<String> keyQueue;

    private final Set<String> writingKeys = new CopyOnWriteArraySet<>();

    public MemoryCache(final int capacity)
    {
        if (capacity < 0)
        {
            throw new RuntimeException("capacity cannot be negative.");
        }

        this.capacity = capacity > 0 ? capacity : Integer.MAX_VALUE;
        cachedResults = capacity > 0 ? new ConcurrentHashMap<>(this.capacity) : new ConcurrentHashMap<>();
        keyQueue = capacity > 0 ? new ArrayDeque<>(this.capacity) : new ArrayDeque<>();
    }

    @Override
    public synchronized void put(final String key, final V value)
    {
        if (!contains(key) && !writingKeys.contains(key))
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
                final ImmutablePair<Date, V> removedValue = cachedResults.remove(removedKey);

                if (removedValue == null)
                {
                    throw new RuntimeException(String.format("The key removed from the queue cannot be found "
                            + "in the cache. [removedKey=%s]", removedKey));
                }
            }

            keyQueue.add(key);
            writingKeys.remove(key);
        }
        cachedResults.put(key, new ImmutablePair<>(new Date(), value));
    }

    @Override
    public V get(final String key)
    {
        final ImmutablePair<Date, V> cachedResult = cachedResults.get(key);
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
    public synchronized void clearOld(final Date date)
    {
        for (final Iterator<Map.Entry<String, ImmutablePair<Date, V>>> iterator = cachedResults.entrySet().iterator();
                iterator.hasNext();)
        {
            final Map.Entry<String, ImmutablePair<Date, V>> entry = iterator.next();

            if (date.after(entry.getValue().getLeft()))
            {
                iterator.remove();
                keyQueue.removeIf(s -> Objects.equals(s, entry.getKey()));
            }
        }
    }

}
