/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.util.RuntimeCache;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

import net.sf.ehcache.CacheManager;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SearchCacheTest
{

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
    }

    @Test
    public void testCreateCacheWithoutSizeSpecified()
    {
        createCache(FileUtils.ONE_GB, null);

        AssertionUtil
                .assertContainsLines(
                        "INFO  OPERATION.RuntimeCache - Cache size has been set to its default value."
                                + " The default value is 25% (256m) of the memory available to the JVM (1g)."
                                + " If you would like to change this value, then please set 'ch.ethz.sis.openbis.v3.searchcache.size' system property in openbis.conf file.",
                        logRecorder.getLogContent());
    }

    @Test
    public void testCreateCacheWithAbsoluteSizeSpecified()
    {
        createCache(FileUtils.ONE_GB, "128m");

        AssertionUtil
                .assertContainsLines(
                        "INFO  OPERATION.RuntimeCache - Cache size was set to '128m' in 'ch.ethz.sis.openbis.v3.searchcache.size' system property.",
                        logRecorder.getLogContent());
    }

    @Test
    public void testCreateCacheWithRelativeSizeSpecified()
    {
        createCache(FileUtils.ONE_GB, "10%");

        char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

        AssertionUtil
                .assertContainsLines(
                        "INFO  OPERATION.RuntimeCache - Cache size was set to '10%' in 'ch.ethz.sis.openbis.v3.searchcache.size' system property."
                                + " The memory available to the JVM is 1g which gives a cache size of 102" + decimalSeparator + "4m",
                        logRecorder.getLogContent());
    }

    @Test
    public void testCreateCacheWithIncorrectSizeSpecified()
    {
        try
        {
            createCache(FileUtils.ONE_GB, "xyz");
            fail();
        } catch (IllegalArgumentException e)
        {
            assertEquals(e.getMessage(),
                    "Cache size was set to 'xyz' in 'ch.ethz.sis.openbis.v3.searchcache.size' system property."
                            + " This value is incorrect. Please set the property to an absolute value like '512m' or '1g'."
                            + " You can also use a value like '25%' to set the cache size relative to the memory available to the JVM.");
        }
    }

    @Test
    public void testPutDifferentKeysWithEntriesThatEventuallyDoNotFitInTheCache()
    {
        SearchCache cache = createCache(FileUtils.ONE_GB, "3k");

        SearchCacheKey key1 = createCacheKey("session1");
        SearchCacheEntry entry1 = createCacheEntry(FileUtils.ONE_KB);

        logRecorder.resetLogContent();
        cache.put(key1, entry1);
        AssertionUtil.assertMatches(
                "(?s).*Cache entry ([0-9]+) that contains 1 object has been put to the cache. Cache now contains 1 entry..*",
                logRecorder.getLogContent());

        SearchCacheKey key2 = createCacheKey("session2");
        SearchCacheEntry entry2 = createCacheEntry(FileUtils.ONE_KB);

        logRecorder.resetLogContent();
        cache.put(key2, entry2);
        AssertionUtil.assertMatches(
                "(?s).*Cache entry ([0-9]+) that contains 1 object has been put to the cache. Cache now contains 2 entries..*",
                logRecorder.getLogContent());

        SearchCacheKey key3 = createCacheKey("session3");
        SearchCacheEntry entry3 = createCacheEntry(FileUtils.ONE_KB);

        logRecorder.resetLogContent();
        cache.put(key3, entry3);

        AssertionUtil
                .assertMatches(
                        "(?s).*Cache entry ([0-9]+) that contains 1 object has been evicted from the cache. Cache now contains 1 entry..*",
                        logRecorder.getLogContent());

        AssertionUtil.assertMatches(
                "(?s).*Cache entry ([0-9]+) that contains 1 object has been put to the cache. Cache now contains 2 entries..*",
                logRecorder.getLogContent());
    }

    @Test
    public void testPutExistingKeyWithNewEntryThatDoesNotFitInTheCache()
    {
        SearchCache cache = createCache(FileUtils.ONE_GB, "1k");

        SearchCacheKey key = createCacheKey("session1");
        SearchCacheEntry entry = new SearchCacheEntry();

        logRecorder.resetLogContent();
        cache.put(key, entry);

        AssertionUtil.assertMatches(
                "(?s).*Cache entry ([0-9]+) that contains 0 objects has been put to the cache. Cache now contains 1 entry..*",
                logRecorder.getLogContent());

        logRecorder.resetLogContent();

        entry = new SearchCacheEntry();
        entry.setObjects(Arrays.asList(new byte[(int) (2 * FileUtils.ONE_KB)]));
        cache.put(key, entry);

        Assert.assertNull(cache.get(key));
    }

    @Test
    public void testPutExistingKeyWithUpdatedEntryThatDoesNotFitInTheCache()
    {
        SearchCache cache = createCache(FileUtils.ONE_GB, "1k");

        SearchCacheKey key = createCacheKey("session1");
        SearchCacheEntry entry = new SearchCacheEntry();

        logRecorder.resetLogContent();
        cache.put(key, entry);

        AssertionUtil.assertMatches(
                "(?s).*Cache entry ([0-9]+) that contains 0 objects has been put to the cache. Cache now contains 1 entry..*",
                logRecorder.getLogContent());

        logRecorder.resetLogContent();

        entry.setObjects(Arrays.asList(new byte[(int) (2 * FileUtils.ONE_KB)]));
        cache.put(key, entry);

        Assert.assertNull(cache.get(key));
    }

    private SearchCache createCache(final long memorySize, final String cacheSize)
    {
        String managerConfig = "<ehcache name='" + UUID.randomUUID() + "'></ehcache>";
        final CacheManager manager = new CacheManager(new ByteArrayInputStream(managerConfig.getBytes()));

        SearchCache cache = new SearchCache(
                new RuntimeCache<Serializable, Serializable>(manager, SearchCache.CACHE_NAME,
                        SearchCache.CACHE_SIZE_PROPERTY_NAME)
                    {
                        @Override
                        protected long getMemorySize()
                        {
                            return memorySize;
                        }

                        @Override
                        protected String getSystemProperty(String propertyName)
                        {
                            if (SearchCache.CACHE_SIZE_PROPERTY_NAME.equals(propertyName))
                            {
                                return cacheSize;
                            } else
                            {
                                return null;
                            }
                        }
                    });
        cache.initCache();
        return cache;
    }

    private SearchCacheKey createCacheKey(String sessionToken)
    {
        return new SearchCacheKey(sessionToken, new Object(), new Object());
    }

    private SearchCacheEntry createCacheEntry(long size)
    {
        SearchCacheEntry entry = new SearchCacheEntry();
        entry.setObjects(Arrays.asList(new byte[(int) size]));
        return entry;
    }
}
