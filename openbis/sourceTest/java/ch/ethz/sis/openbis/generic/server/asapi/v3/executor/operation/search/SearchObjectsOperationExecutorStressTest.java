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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperationLimiterConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SearchObjectsOperationExecutorStressTest
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SearchObjectsOperationExecutorStressTest.class);

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
    }

    @Test(timeOut = 30000)
    public void testConcurrencyWithoutEvicting() throws InterruptedException
    {
        for (int run = 0; run < 5; run++)
        {
            StressTestSearchMethodExecutor executor = testConcurrency(0);

            for (Map.Entry<SearchCacheKey, Integer> entry : executor.getSearchCounts().entrySet())
            {
                if (entry.getValue() != 1)
                {
                    executor.addError("Run " + run + " : " + entry.getValue() + " searches were executed instead of 1 for the key " + entry.getKey());
                }
            }

            if (executor.getErrors().size() > 0)
            {
                Assert.fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    @Test(timeOut = 30000)
    public void testConcurrencyWithEvicting() throws InterruptedException
    {
        for (int run = 0; run < 5; run++)
        {
            StressTestSearchMethodExecutor executor = testConcurrency(10 * FileUtils.ONE_KB);

            if (executor.getErrors().size() > 0)
            {
                Assert.fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    private StressTestSearchMethodExecutor testConcurrency(long cacheSize) throws InterruptedException
    {
        int SESSION_COUNT = 5;
        int THREAD_COUNT = 5;
        int KEY_VERSION_COUNT = 20;

        final StressTestSearchMethodExecutor executor = new StressTestSearchMethodExecutor(cacheSize);

        final Map<String, IOperationContext> contexts = new LinkedHashMap<String, IOperationContext>();
        for (int s = 0; s < SESSION_COUNT; s++)
        {
            Session session = new Session("user" + s, "token" + s, new Principal(), "", 1);
            contexts.put(session.getSessionToken(), new OperationContext(session));
        }

        final List<SearchCacheKey> keys = new ArrayList<SearchCacheKey>();
        for (IOperationContext context : contexts.values())
        {
            for (int k = 0; k < KEY_VERSION_COUNT; k++)
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

        List<Thread> threads = new ArrayList<Thread>();

        for (int t = 0; t < THREAD_COUNT; t++)
        {
            Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
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

    private static class StressTestSearchMethodExecutor extends SearchObjectsOperationExecutor
    {

        private final Map<SearchCacheKey, Integer> searchCounts = new HashMap<>();

        private final Map<SearchCacheKey, Object> searchResults = new HashMap<>();

        private final List<String> errors = Collections.synchronizedList(new ArrayList<String>());

        private final int cacheSize;

        public StressTestSearchMethodExecutor(final long cacheSize)
        {
            this.cacheSize = (int) cacheSize;
            this.operationLimiter = new ConcurrentOperationLimiter(new ConcurrentOperationLimiterConfig(
                    new Properties()));
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
            final Map<String, ICache<Object>> cacheByUserSessionToken = this.getCacheByUserSessionToken();
            ICache<Object> cache = cacheByUserSessionToken.get(context.getSession().getSessionToken());
            if (cache == null)
            {
                cache = new MemoryCache<>(cacheSize);
                cacheByUserSessionToken.put(context.getSession().getSessionToken(), cache);
            }
            return cache;
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
