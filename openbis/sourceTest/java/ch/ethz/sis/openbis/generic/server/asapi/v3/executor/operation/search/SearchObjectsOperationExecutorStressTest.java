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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search;

import static ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_DEFAULT;
import static ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.FileCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ICache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.MemoryCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiterConfig;
import ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SearchObjectsOperationExecutorStressTest
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SearchObjectsOperationExecutorStressTest.class);

    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY + File.separator +
            UNIT_TEST_WORKING_DIRECTORY);

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
    }

    public Object[][] provideFactories()
    {
        final int cacheSize = 10 * (int) FileUtils.ONE_KB;
        return new Object[][]
                {
                        { 0, new MemoryCacheFactory(0)},
                        { 0, new FileCacheFactory(0)},
                        { cacheSize, new MemoryCacheFactory(cacheSize)},
                        { cacheSize, new FileCacheFactory(cacheSize)},
                };
    }

    @DataProvider(name = "cache factories with evicting", indices = {2, 3})
    public Object[][] provideFactoriesWithEvicting()
    {
        return provideFactories();
    }

    @DataProvider(name = "cache factories without evicting", indices = {0, 1})
    public Object[][] provideFactoriesWithoutEvicting()
    {
        return provideFactories();
    }

    @Test(timeOut = 30000, dataProvider = "cache factories without evicting")
    public void testConcurrencyWithoutEvicting(final int cacheSize, final CacheFactory cacheFactory)
    {
        for (int run = 0; run < 5; run++)
        {
            StressTestSearchMethodExecutor executor = testConcurrency(cacheSize, cacheFactory);

            for (Map.Entry<SearchCacheKey, Integer> entry : executor.getSearchCounts().entrySet())
            {
                if (entry.getValue() != 1)
                {
                    executor.addError("Run " + run + " : " + entry.getValue() + " searches were executed instead of 1 for the key " + entry.getKey());
                }
            }

            if (executor.getErrors().size() > 0)
            {
                fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    @Test(timeOut = 30000, dataProvider = "cache factories with evicting")
    public void testConcurrencyWithEvicting(final int cacheSize, final CacheFactory cacheFactory)
    {
        for (int run = 0; run < 5; run++)
        {
            StressTestSearchMethodExecutor executor = testConcurrency(cacheSize, cacheFactory);

            if (executor.getErrors().size() > 0)
            {
                fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    private StressTestSearchMethodExecutor testConcurrency(final long cacheSize, final CacheFactory cacheFactory)
    {
        final int threadCount = 5;

        final StressTestSearchMethodExecutor executor = new StressTestSearchMethodExecutor(cacheFactory);

        final Map<String, IOperationContext> contexts = new LinkedHashMap<>();
        final List<SearchCacheKey> keys = prepareSearchCacheKeys(cacheSize, executor, contexts, 0, 20);

        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++)
        {
            Thread thread = new Thread(() ->
            {
                try
                {
                    for (int i = 0; i < keys.size() * 2; i++)
                    {
                        SearchCacheKey<AbstractObjectSearchCriteria, FetchOptions> key = keys.get((int) (Math.random() * keys.size()));
                        IOperationContext context = contexts.get(key.getSessionToken());
                        TestSearchOperation operation = new TestSearchOperation(key.getCriteria(), key.getFetchOptions());
                        Map<TestSearchOperation, TestSearchOperationResult> results = executor.execute(context, Arrays.asList(operation));

                        Object actualResult = results.get(operation).getSearchResult().getObjects().get(0);
                        Object expectedResult = executor.getSearchResult(key);

                        if (false == actualResult.equals(expectedResult))
                        {
                            executor.addError("Actual search result: " + actualResult + " but expected: " + expectedResult + " for key: "
                                    + key);
                        }
                    }
                } catch (Throwable throwable)
                {
                    executor.addError(
                            "Exception in thread " + Thread.currentThread().getName() + ": " + ExceptionUtils.getStackTrace(throwable));
                }

            });
            thread.setName("Stress test thread # " + (t + 1));
            threads.add(thread);
        }

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            } catch (InterruptedException ex)
            {
                operationLog.error("INTERRUPTED EXCEPTION on " + thread.getName());
            }
            operationLog.info(thread.getName() + " has been finished");
        }

        return executor;
    }

    @Test(timeOut = 30000, dataProvider = "cache factories without evicting")
    public void testEvictionByDate(final int cacheSize, final CacheFactory cacheFactory)
            throws InterruptedException
    {
        final int sessionCount = 5;

        final StressTestSearchMethodExecutor executor = new StressTestSearchMethodExecutor(cacheFactory);

        final Map<String, IOperationContext> olderContexts = new LinkedHashMap<>();
        for (int s = 0; s < sessionCount; s++)
        {
            final Session session = new Session("user" + s, "token" + s, new Principal(), "", 1);
            olderContexts.put(session.getSessionToken(), new OperationContext(session));
        }

        final Map<String, IOperationContext> newerContexts = new LinkedHashMap<>();
        for (int s = sessionCount; s < 2 * sessionCount; s++)
        {
            final Session session = new Session("user" + s, "token" + s, new Principal(), "", 1);
            newerContexts.put(session.getSessionToken(), new OperationContext(session));
        }

        final Map<String, IOperationContext> contexts = new HashMap<>();
        contexts.putAll(olderContexts);
        contexts.putAll(newerContexts);

        final List<SearchCacheKey> olderKeys = prepareSearchCacheKeys(cacheSize,
                executor, olderContexts, 0, 20);
        populateCache(executor, contexts, olderKeys);

        Thread.sleep(1);
        final Date newerKeysDate = new Date();
        Thread.sleep(1);

        final List<SearchCacheKey> newerKeys = prepareSearchCacheKeys(cacheSize, executor, newerContexts, 20, 20);
        populateCache(executor, contexts, newerKeys);

        final int olderKeysSize = olderKeys.size();
        final List<SearchCacheKey> keys = new ArrayList<>(olderKeysSize + newerKeys.size());
        keys.addAll(olderKeys);
        keys.addAll(newerKeys);

        olderContexts.values().forEach(context -> executor.getCache(context).clearOld(newerKeysDate));

        for (int i = 0; i < keys.size(); i++)
        {
            final int index = (int) (Math.random() * keys.size());
            final SearchCacheKey<AbstractObjectSearchCriteria, FetchOptions> key =
                    keys.get(index);

            final IOperationContext context = contexts.get(key.getSessionToken());
            final TestSearchOperation operation = new TestSearchOperation(key.getCriteria(), key.getFetchOptions());
            
            if (index < olderKeysSize)
            {
                final Object cachedResult = executor.getSearchResultFromCache(context, key);

                if (cachedResult != null)
                {
                    fail("Fetched cache value should be null but was: " + cachedResult + " for key: " + key);
                }
            } else
            {
                final Object cachedResult = executor.getSearchResultFromCache(context, key);

                if (cachedResult == null)
                {
                    fail("Fetched cache value should not be null but was null for key: " + key);
                }

                final Object expectedResult = executor.getSearchResult(key);
                final Map<TestSearchOperation, TestSearchOperationResult> results = executor.execute(context,
                        Collections.singletonList(operation));
                final Object actualResult = results.get(operation).getSearchResult().getObjects().get(0);

                if (!actualResult.equals(expectedResult))
                {
                    fail("Actual search result: " + actualResult + " but expected: " + expectedResult + " for key: "
                            + key);
                }
            }
        }
    }

    private void populateCache(final StressTestSearchMethodExecutor executor,
            final Map<String, IOperationContext> contexts, final List<SearchCacheKey> keys)
    {
        for (final SearchCacheKey<AbstractObjectSearchCriteria, FetchOptions> key : keys)
        {
            final IOperationContext context = contexts.get(key.getSessionToken());
            final TestSearchOperation operation = new TestSearchOperation(key.getCriteria(), key.getFetchOptions());
            executor.execute(context, Collections.singletonList(operation));
        }
    }

    private List<SearchCacheKey> prepareSearchCacheKeys(final long cacheSize,
            final StressTestSearchMethodExecutor executor, final Map<String, IOperationContext> contexts,
            final int startingKey, final int keyVersionCount)
    {
        final List<SearchCacheKey> keys = new ArrayList<>();
        for (IOperationContext context : contexts.values())
        {
            for (int k = startingKey; k < startingKey + keyVersionCount; k++)
            {
                SpaceSearchCriteria spaceSearchCriteria = new SpaceSearchCriteria();
                spaceSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey spaceKey =
                        new SearchCacheKey(context.getSession().getSessionToken(), spaceSearchCriteria,
                                new SpaceFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(spaceKey);
                executor.setSearchResult(spaceKey, new RandomSizeArray(cacheSize));

                ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
                projectSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey projectKey =
                        new SearchCacheKey(context.getSession().getSessionToken(), projectSearchCriteria,
                                new ProjectFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(projectKey);
                executor.setSearchResult(projectKey, new RandomSizeArray(cacheSize));

                ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
                experimentSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey experimentKey =
                        new SearchCacheKey(context.getSession().getSessionToken(), experimentSearchCriteria,
                                new ExperimentFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(experimentKey);
                executor.setSearchResult(experimentKey, new RandomSizeArray(cacheSize));

                SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
                sampleSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey sampleKey =
                        new SearchCacheKey(context.getSession().getSessionToken(), sampleSearchCriteria,
                                new SampleFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(sampleKey);
                executor.setSearchResult(sampleKey, new RandomSizeArray(cacheSize));
            }
        }
        return keys;
    }

    private static class StressTestSearchMethodExecutor extends SearchObjectsOperationExecutor
    {

        private final Map<SearchCacheKey, Integer> searchCounts = new HashMap<>();

        private final Map<SearchCacheKey, Object> searchResults = new HashMap<>();

        private final List<String> errors = Collections.synchronizedList(new ArrayList<>());

        private final CacheFactory cacheFactory;

        public StressTestSearchMethodExecutor(final CacheFactory cacheFactory)
        {
            this.cacheFactory = cacheFactory;
            this.operationLimiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(
                    new Properties()));

            setCacheClass(cacheFactory.getCacheClass());
        }

        @Override
        protected List doSearch(final IOperationContext context, final AbstractSearchCriteria criteria,
                final FetchOptions fetchOptions)
        {
            final SearchCacheKey key = new SearchCacheKey(context.getSession().getSessionToken(), criteria,
                    fetchOptions);

            synchronized (searchCounts)
            {
                Integer searchCount = searchCounts.get(key);
                if (searchCount == null)
                {
                    searchCount = 0;
                }
                searchCounts.put(key, ++searchCount);
            }

            try
            {
                Thread.sleep(1);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            return Collections.singletonList(key);
        }

        @Override
        protected Map doTranslate(final TranslationContext translationContext, final Collection ids,
                final FetchOptions fetchOptions)
        {
            final SearchCacheKey key = (SearchCacheKey) ids.iterator().next();

            return Collections.singletonMap(key, searchResults.get(key));
        }

        @Override
        protected ICache<Object> getCache(final IOperationContext context)
        {
            final Map<String, ICache<Object>> cacheByUserSessionToken = getCacheByUserSessionToken();
            final String sessionToken = context.getSession().getSessionToken();
            ICache<Object> cache = cacheByUserSessionToken.get(sessionToken);
            if (cache == null)
            {
                cache = cacheFactory.getCache(context);
                cacheByUserSessionToken.put(sessionToken, cache);
            }
            return cache;
        }

        public Object getSearchResultFromCache(final IOperationContext context, final SearchCacheKey key)
        {
            final ICache<Object> cache = getCache(context);
            final String cacheKey = getMD5Hash(key.getCriteria().toString());
            return cache.get(cacheKey);
        }

        public Map<SearchCacheKey, Integer> getSearchCounts()
        {
            return searchCounts;
        }

        public void setSearchResult(SearchCacheKey key, Object result)
        {
            searchResults.put(key, result);
        }

        public Object getSearchResult(SearchCacheKey key)
        {
            return searchResults.get(key);
        }

        public void addError(String error)
        {
            errors.add(error);
        }

        public List<String> getErrors()
        {
            return errors;
        }

        @Override
        protected ISearchObjectExecutor getExecutor()
        {
            throw new IllegalStateException("This should never be called as we did override doSearchAndTranslate()");
        }

        @Override
        protected ITranslator getTranslator()
        {
            throw new IllegalStateException("This should never be called as we did override doSearchAndTranslate()");
        }

        @Override
        protected ILocalSearchManager getSearchManager()
        {
            throw new IllegalStateException("This method should not be invoked.");
        }

        @Override
        protected Class getOperationClass()
        {
            return TestSearchOperation.class;
        }

        @Override
        protected SearchObjectsOperationResult getOperationResult(SearchResult searchResult)
        {
            return new TestSearchOperationResult(searchResult);
        }

    }

    private interface CacheFactory
    {

        Class<?> getCacheClass();

        ICache<Object> getCache(final IOperationContext iOperationContext);

    }

    private static class MemoryCacheFactory implements CacheFactory
    {

        final int cacheSize;

        private MemoryCacheFactory(final int cacheSize)
        {
            this.cacheSize = cacheSize;
        }

        @Override
        public Class<?> getCacheClass()
        {
            return MemoryCache.class;
        }

        @Override
        public ICache<Object> getCache(final IOperationContext iOperationContext)
        {
            return new MemoryCache<>(new CacheOptionsVO(cacheSize, null, null, false));
        }

    }

    private static class FileCacheFactory implements CacheFactory
    {

        /** Whether at least one instance of this cache has been created. */
        private static boolean applyCalled = false;

        private final int cacheSize;

        private FileCacheFactory(final int cacheSize)
        {
            this.cacheSize = cacheSize;
        }

        @Override
        public Class<?> getCacheClass()
        {
            return FileCache.class;
        }

        @Override
        public ICache<Object> getCache(final IOperationContext context)
        {
            final Properties properties = new Properties();
            final File workingDirectory = createDirectoryInUnitTestRoot(getClass().getName());
            properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

            final String sessionToken = context.getSession().getSessionToken();
            final FileCache<Object> fileCache = new FileCache<>(
                    new CacheOptionsVO(cacheSize, properties, sessionToken, false));

            synchronized (FileCacheFactory.class)
            {
                if (!applyCalled)
                {
                    applyCalled = true;
                    assertCacheDirectoryEmpty(properties, sessionToken);
                }
            }

            return fileCache;
        }

        /**
         * Asserts that cache is cleared before start.
         *
         * @param properties configuration properties.
         * @param sessionToken current session token.
         */
        @SuppressWarnings("ConstantConditions")
        private void assertCacheDirectoryEmpty(final Properties properties, final String sessionToken)
        {
            final String cacheDirString = PropertyUtils.getProperty(properties, SESSION_WORKSPACE_ROOT_DIR_KEY,
                    SESSION_WORKSPACE_ROOT_DIR_DEFAULT) + File.separator + FileCache.CACHE_FOLDER_NAME +
                    File.separator + sessionToken.replaceAll("\\W+", "");
            final File cacheDir = new File(cacheDirString);

            assertTrue(cacheDir.isDirectory());
            assertEquals(cacheDir.listFiles().length, 0);
        }

        protected final File createDirectoryInUnitTestRoot(String dirName)
        {
            final File directory = new File(UNIT_TEST_ROOT_DIRECTORY, dirName);
            directory.mkdirs();
            directory.deleteOnExit();
            return directory;
        }

    }

    private static class RandomSizeArray
    {

        private final byte[] array;

        public RandomSizeArray(long maxSize)
        {
            array = new byte[(int) (Math.random() * maxSize)];
        }

        @Override
        public int hashCode()
        {
            return array.length;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RandomSizeArray other = (RandomSizeArray) obj;
            return array.length == other.array.length;
        }

    }

}
