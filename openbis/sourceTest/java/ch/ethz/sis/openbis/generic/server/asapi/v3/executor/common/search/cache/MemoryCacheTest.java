package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Date;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search.MemoryCacheFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class MemoryCacheTest
{

    private static final int CACHE_SIZE = 10;

    private static int counter = 0;

    @DataProvider
    protected Object[][] cacheImplementations()
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

    @BeforeMethod
    public void setUp()
    {
    }

    @AfterMethod
    public void tearDown()
    {
    }

    @Test(dataProvider = "cacheImplementations")
    public void testPut(final MemoryCache<Object> cache, final boolean limited)
    {
        // Test that cache is empty

        assertEquals(cache.getCachedResults().size(), 0);

        // General test that put sets the values correctly

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            cache.put("k" + i, i);
        }

        final Map<String, ImmutablePair<Date, Object>> cachedResults1 = cache.getCachedResults();
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

        final Map<String, ImmutablePair<Date, Object>> cachedResults2 = cache.getCachedResults();
        assertEquals(cachedResults2.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue2 = cache.getKeyQueue();
        assertEquals(keyQueue2.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);
        assertEquals(keyQueue2.peek(), limited ? "k1" : "k0");

        checkCacheItem(cachedResults2, CACHE_SIZE);

        if (limited)
        {
            // The very first element should be evicted.
            assertNull(cachedResults2.get("k0"));
        }

        // Replacing

        cache.put("k1", -1);

        final Map<String, ImmutablePair<Date, Object>> cachedResults3 = cache.getCachedResults();
        assertEquals(cachedResults3.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue3 = cache.getKeyQueue();
        assertEquals(keyQueue3.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        assertEquals(cachedResults3.get("k1").getRight(), -1);
        assertEquals(keyQueue3.peek(), limited ? "k1" : "k0");
    }

    private void checkCacheItem(final Map<String, ImmutablePair<Date, Object>> cachedResults1, final int i)
    {
        final ImmutablePair<Date, Object> cacheItem = cachedResults1.get("k" + i);
        assertNotNull(cacheItem);

        // Timer mock works in steps.
        assertEquals(cacheItem.getLeft().getTime(), i * 1000L);
        assertEquals(cacheItem.getRight(), i);
    }

    @Test(dataProvider = "cacheImplementations")
    public void testGet(final MemoryCache<Object> cache, final boolean limited)
    {
    }

    @Test(dataProvider = "cacheImplementations")
    public void testRemove(final MemoryCache<Object> cache, final boolean limited)
    {
    }

    @Test(dataProvider = "cacheImplementations")
    public void testContains(final MemoryCache<Object> cache, final boolean limited)
    {
    }

    @Test(dataProvider = "cacheImplementations")
    public void testClear(final MemoryCache<Object> cache, final boolean limited)
    {
    }

    @Test(dataProvider = "cacheImplementations")
    public void testClearOld(final MemoryCache<Object> cache, final boolean limited)
    {
    }

}