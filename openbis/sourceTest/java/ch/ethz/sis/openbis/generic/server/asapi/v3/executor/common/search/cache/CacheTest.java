package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class CacheTest extends AbstractFileSystemTestCase
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
                        { new FileCacheFactory(0).getCache(createContext(), workingDirectory), false },
                        { new FileCacheFactory(CACHE_SIZE).getCache(createContext(), workingDirectory), true },
                };
    }

    private static IOperationContext createContext()
    {
        final int suffix = counter++;
        return new OperationContext(new Session("user" + suffix, "token" + suffix, new Principal(), "", 1));
    }

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test(dataProvider = "cacheInstances")
    public void testPut(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test that cache is empty

        Assert.assertNotNull(workingDirectory.listFiles());
        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertTrue(cache.getKeyQueue().isEmpty());

        // General test that the put method sets values correctly

        fillCacheWithValues(cache);

        Assert.assertEquals(getCacheSize(cache), CACHE_SIZE);

        final Queue<String> keyQueue1 = cache.getKeyQueue();
        Assert.assertEquals(keyQueue1.size(), CACHE_SIZE);
        Assert.assertEquals(keyQueue1.peek(), "k0");

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            checkCacheItem(cache, i);
        }

        // Adding an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);

        Assert.assertEquals(getCacheSize(cache), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue2 = cache.getKeyQueue();
        Assert.assertEquals(keyQueue2.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);
        Assert.assertEquals(keyQueue2.peek(), limited ? "k1" : "k0");

        checkCacheItem(cache, CACHE_SIZE);

        if (limited)
        {
            // The very first element should be evicted.
            Assert.assertNull(getCacheItem(cache, "k0"));
        } else
        {
            Assert.assertNotNull(getCacheItem(cache, "k0"));
        }

        // Replacing

        cache.put("k1", -1);

        Assert.assertEquals(getCacheSize(cache), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        final Queue<String> keyQueue3 = cache.getKeyQueue();
        Assert.assertEquals(keyQueue3.size(), limited ? CACHE_SIZE : CACHE_SIZE + 1);

        Assert.assertEquals(getCacheItem(cache, "k1").getRight(), -1);
        Assert.assertEquals(keyQueue3.peek(), limited ? "k1" : "k0");
    }

    private void checkCacheItem(final ITestableCache<Object> cache, final int i)
    {
        final ImmutablePair<Long, Object> cacheItem = getCacheItem(cache, "k" + i);

        Assert.assertNotNull(cacheItem);

        // Timer mock works in steps.
        Assert.assertEquals(cacheItem.getRight(), i);
    }

    private File getCacheFile(final String key)
    {
        return new File(workingDirectory.getPath() + File.separator + key);
    }

    @Test(dataProvider = "cacheInstances")
    public void testGet(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        Assert.assertNull(cache.get("k1"));

        // General test that the get method retrieves values correctly

        fillCacheWithValues(cache);

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            Assert.assertEquals(cache.get("k" + i), i);
        }

        // Add an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);
        Assert.assertEquals(cache.get("k" + CACHE_SIZE), CACHE_SIZE);

        if (limited)
        {
            // The very first element should be evicted.
            Assert.assertNull(cache.get("k0"));
        } else
        {
            Assert.assertNotNull(cache.get("k0"));
        }

        // Replace

        cache.put("k1", -1);
        Assert.assertEquals(cache.get("k1"), -1);
    }

    private void fillCacheWithValues(final ITestableCache<Object> cache)
    {
        for (int i = 0; i < CACHE_SIZE; i++)
        {
            cache.put("k" + i, i);
        }
    }

    @Test(dataProvider = "cacheInstances")
    public void testRemove(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        cache.remove("k0");

        // Remove not existing element

        fillCacheWithValues(cache);

        cache.remove("k" + CACHE_SIZE);

        Assert.assertEquals(getCacheSize(cache), CACHE_SIZE);
        Assert.assertEquals(cache.getKeyQueue().size(), CACHE_SIZE);

        // Remove last element

        removeElement(cache, "k" + (CACHE_SIZE - 1));

        // Remove middle element

        removeElement(cache, "k" + (CACHE_SIZE - 1) / 2);

        // Remove first element

        removeElement(cache, "k0");

        // Remove all remaining elements in random order

        final Map<String, ImmutablePair<Long, Object>> cachedResults = cache.getCachedResults();

        final List<String> keys = cachedResults == null
                ? Arrays.stream(workingDirectory.listFiles()).map(File::getName).collect(Collectors.toList())
                : new ArrayList<>(cachedResults.keySet());

        Collections.shuffle(keys);
        keys.forEach(key -> removeElement(cache, key));

        // Test empty cache again

        cache.remove("k1");
        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertEquals(cache.getKeyQueue().size(), 0);
    }

    private void removeElement(final ITestableCache<Object> cache, final String key)
    {
        final int initialCacheSize = getCacheSize(cache);
        Assert.assertEquals(cache.getKeyQueue().size(), initialCacheSize);

        cache.remove(key);
        Assert.assertNull(cache.get(key));
        Assert.assertEquals(getCacheSize(cache), initialCacheSize - 1);
        Assert.assertEquals(cache.getKeyQueue().size(), initialCacheSize - 1);
    }

    @Test(dataProvider = "cacheInstances")
    public void testContains(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        Assert.assertFalse(cache.contains("k0"));
        Assert.assertFalse(cache.contains(""));

        // General test that the contains method finds values correctly

        fillCacheWithValues(cache);

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            Assert.assertTrue(cache.contains("k" + i));
        }

        // Add an extra element that should evict the first one in the limited cache

        cache.put("k" + CACHE_SIZE, CACHE_SIZE);
        Assert.assertTrue(cache.contains("k" + CACHE_SIZE));

        if (limited)
        {
            // The very first element should be evicted.
            Assert.assertFalse(cache.contains("k0"));
        } else
        {
            Assert.assertTrue(cache.contains("k0"));
        }

        // Replace

        cache.put("k1", -1);
        Assert.assertTrue(cache.contains("k1"));
    }

    @Test(dataProvider = "cacheInstances")
    public void testClear(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertTrue(cache.getKeyQueue().isEmpty());
        cache.clear();
        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertTrue(cache.getKeyQueue().isEmpty());

        // General test that cache is cleared correctly

        fillCacheWithValues(cache);
        Assert.assertTrue(getCacheSize(cache) > 0);
        Assert.assertFalse(cache.getKeyQueue().isEmpty());
        cache.clear();
        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertTrue(cache.getKeyQueue().isEmpty());

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            Assert.assertFalse(cache.contains("k" + i));
            Assert.assertNull(cache.get("k" + i));
        }
    }

    @Test(dataProvider = "cacheInstances")
    public void testClearOld(final ITestableCache<Object> cache, final boolean limited)
    {
        // Test empty cache

        cache.clearOld(0L);

        // Fill cache and empty it completely

        fillCacheWithValues(cache);
        final long time1 = getCacheItem(cache, "k" + (CACHE_SIZE - 1)).getLeft() + 1;
        cache.clearOld(time1);
        Assert.assertEquals(getCacheSize(cache), 0);
        Assert.assertTrue(cache.getKeyQueue().isEmpty());

        // Splitting the cache in half

        fillCacheWithValues(cache);
        final long time2 = getCacheItem(cache, "k" + (CACHE_SIZE - 1) / 2).getLeft() + 1;
        cache.clearOld(time2);
        Assert.assertEquals(getCacheSize(cache), CACHE_SIZE / 2);
        Assert.assertEquals(cache.getKeyQueue().size(), CACHE_SIZE / 2);

        Assert.assertTrue(Arrays.stream(workingDirectory.list()).map(key -> getCacheItem(cache, key))
                .allMatch(cacheItem -> (Integer) cacheItem.getRight() > (CACHE_SIZE - 1) / 2));
    }

    private int getCacheSize(final ITestableCache<Object> cache)
    {
        final Map<String, ImmutablePair<Long, Object>> cachedResults = cache.getCachedResults();
        return cachedResults == null ? workingDirectory.listFiles().length : cachedResults.size();
    }

    private ImmutablePair<Long, Object> getCacheItem(final ITestableCache<Object> cache, final String key)
    {
        final Map<String, ImmutablePair<Long, Object>> cachedResults = cache.getCachedResults();
        if (cachedResults == null)
        {
            final File cacheFile = getCacheFile(key);

            if (cacheFile.isFile())
            {
                try (final ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile)))
                {
                    return (ImmutablePair<Long, Object>) in.readObject();
                } catch (final IOException | ClassNotFoundException e)
                {
                    Assert.fail(String.format("Error reading value from cache. [key=%s]", key), e);
                    return null;
                }
            } else
            {
                return null;
            }
        } else
        {
            return cachedResults.get(key);
        }
    }

}