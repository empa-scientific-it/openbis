package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class MemoryCacheTest
{

    private static final int CACHE_SIZE = 10;

    private static int counter = 0;

    @DataProvider
    protected Object[][] cacheInstances()
    {
        return new Object[][]
        {
                { new MemoryCacheFactory(0).getCache(createContext()), false },
                { new MemoryCacheFactory(CACHE_SIZE).getCache(createContext()), true },
        };
    }

    private static IOperationContext createContext()
    {
        final int suffix = counter++;
        return new OperationContext(new Session("user" + suffix, "token" + suffix, new Principal(), "", 1));
    }

    @Test(dataProvider = "cacheInstances")
    public void testPut(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test that cache is empty

        assertTrue(cache.getCachedResults().isEmpty());
        assertTrue(cache.getKeyQueue().isEmpty());

        // General test that the put method sets values correctly

        fillCacheWithValues(cache);

        final Map<String, ImmutablePair<Long, Object>> cachedResults1 = cache.getCachedResults();
        assertEquals(cachedResults1.size(), CACHE_SIZE);

        final Queue<String> keyQueue1 = cache.getKeyQueue();
        assertEquals(keyQueue1.size(), CACHE_SIZE);
        assertEquals(keyQueue1.peek(), "k0");

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            checkCacheItem(cachedResults1, i);
        }

        // Adding an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);

        final Map<String, ImmutablePair<Long, Object>> cachedResults2 = cache.getCachedResults();
        assertEquals(cachedResults2.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue2 = cache.getKeyQueue();
        assertEquals(keyQueue2.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);
        assertEquals(keyQueue2.peek(), limited ? "k1" : "k0");

        checkCacheItem(cachedResults2, CACHE_SIZE);

        if (limited)
        {
            // The very first element should be evicted.
            assertNull(cachedResults2.get("k0"));
        } else
        {
            assertNotNull(cachedResults2.get("k0"));
        }

        // Replacing

        cache.put("k1", -1);

        final Map<String, ImmutablePair<Long, Object>> cachedResults3 = cache.getCachedResults();
        assertEquals(cachedResults3.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue3 = cache.getKeyQueue();
        assertEquals(keyQueue3.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        assertEquals(cachedResults3.get("k1").getRight(), -1);
        assertEquals(keyQueue3.peek(), limited ? "k1" : "k0");
    }

    private void checkCacheItem(final Map<String, ImmutablePair<Long, Object>> cachedResults, final int i)
    {
        final ImmutablePair<Long, Object> cacheItem = cachedResults.get("k" + i);
        assertNotNull(cacheItem);

        // Timer mock works in steps.
        assertEquals(cacheItem.getRight(), i);
    }

    @Test(dataProvider = "cacheInstances")
    public void testGet(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        assertNull(cache.get("k1"));

        // General test that the get method retrieves values correctly

        fillCacheWithValues(cache);

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            assertEquals(cache.get("k" + i), i);
        }

        // Add an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);
        assertEquals(cache.get("k" + CACHE_SIZE), CACHE_SIZE);

        if (limited)
        {
            // The very first element should be evicted.
            assertNull(cache.get("k0"));
        } else
        {
            assertNotNull(cache.get("k0"));
        }

        // Replace

        cache.put("k1", -1);
        assertEquals(cache.get("k1"), -1);
    }

    private void fillCacheWithValues(final MemoryCache<Object> cache)
    {
        for (int i = 0; i < CACHE_SIZE; i++)
        {
            cache.put("k" + i, i);
        }
    }

    @Test(dataProvider = "cacheInstances")
    public void testRemove(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        cache.remove("k0");

        // Remove not existing element

        fillCacheWithValues(cache);

        cache.remove("k" + CACHE_SIZE);

        assertEquals(cache.getCachedResults().size(), CACHE_SIZE);
        assertEquals(cache.getKeyQueue().size(), CACHE_SIZE);

        // Remove last element

        removeElement(cache, "k" + (CACHE_SIZE - 1));

        // Remove middle element

        removeElement(cache, "k" + (CACHE_SIZE - 1) / 2);

        // Remove first element

        removeElement(cache, "k0");

        // Remove all remaining elements in random order

        final List<String> keys = new ArrayList<>(cache.getCachedResults().keySet());
        Collections.shuffle(keys);
        keys.forEach(key -> removeElement(cache, key));

        // Test empty cache again

        cache.remove("k1");
        assertEquals(cache.getCachedResults().size(), 0);
        assertEquals(cache.getKeyQueue().size(), 0);
    }

    private void removeElement(final MemoryCache<Object> cache, final String key)
    {
        final int initialCacheSize = cache.getCachedResults().size();
        assertEquals(cache.getKeyQueue().size(), initialCacheSize);

        cache.remove(key);
        assertNull(cache.get(key));
        assertEquals(cache.getCachedResults().size(), initialCacheSize - 1);
        assertEquals(cache.getKeyQueue().size(), initialCacheSize - 1);
    }

    @Test(dataProvider = "cacheInstances")
    public void testContains(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        assertFalse(cache.contains("k0"));
        assertFalse(cache.contains(""));

        // General test that the contains method finds values correctly

        fillCacheWithValues(cache);

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            assertTrue(cache.contains("k" + i));
        }

        // Add an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);
        assertTrue(cache.contains("k" + CACHE_SIZE));

        if (limited)
        {
            // The very first element should be evicted.
            assertFalse(cache.contains("k0"));
        } else
        {
            assertTrue(cache.contains("k0"));
        }

        // Replace

        cache.put("k1", -1);
        assertTrue(cache.contains("k1"));
    }

    @Test(dataProvider = "cacheInstances")
    public void testClear(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        assertTrue(cache.getCachedResults().isEmpty());
        assertTrue(cache.getKeyQueue().isEmpty());
        cache.clear();
        assertTrue(cache.getCachedResults().isEmpty());
        assertTrue(cache.getKeyQueue().isEmpty());

        // General test that cache is cleared correctly

        fillCacheWithValues(cache);
        assertFalse(cache.getCachedResults().isEmpty());
        assertFalse(cache.getKeyQueue().isEmpty());
        cache.clear();
        assertTrue(cache.getCachedResults().isEmpty());
        assertTrue(cache.getKeyQueue().isEmpty());

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            assertFalse(cache.contains("k" + i));
            assertNull(cache.get("k" + i));
        }
    }

    @Test(dataProvider = "cacheInstances")
    public void testClearOld(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        cache.clearOld(0L);

        // Fill cache and empty it completely

        fillCacheWithValues(cache);
        final long time1 = cache.getCachedResults().get("k" + (CACHE_SIZE - 1)).getLeft() + 1;
        cache.clearOld(time1);
        assertTrue(cache.getCachedResults().isEmpty());
        assertTrue(cache.getKeyQueue().isEmpty());

        // Splitting the cache in half

        fillCacheWithValues(cache);
        final long time2 = cache.getCachedResults().get("k" + (CACHE_SIZE - 1) / 2).getLeft() + 1;
        cache.clearOld(time2);
        assertEquals(cache.getCachedResults().size(), CACHE_SIZE / 2);
        assertEquals(cache.getKeyQueue().size(), CACHE_SIZE / 2);
        assertTrue(cache.getCachedResults().values().stream().allMatch(
                cacheItem -> (Integer) cacheItem.getRight() > (CACHE_SIZE - 1) / 2));
    }

}