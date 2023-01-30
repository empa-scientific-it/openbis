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

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.shared.util.RuntimeCache;

/**
 * @author pkupczyk
 */
@Component
public class SearchCache<CRITERIA, FETCH_OPTIONS, OBJECT> implements ISearchCache<CRITERIA, FETCH_OPTIONS, OBJECT>
{

    public static final String CACHE_NAME = "searchCache";

    public static final String CACHE_SIZE_PROPERTY_NAME = "ch.ethz.sis.openbis.v3.searchcache.size";

    @Autowired
    private CacheManager cacheManager;

    private RuntimeCache<SearchCacheKey<CRITERIA, FETCH_OPTIONS>, SearchCacheEntry<OBJECT>> runtimeCache;

    public SearchCache()
    {
    }

    public SearchCache(RuntimeCache<SearchCacheKey<CRITERIA, FETCH_OPTIONS>, SearchCacheEntry<OBJECT>> runtimeCache)
    {
        this.runtimeCache = runtimeCache;
    }

    @Override
    public SearchCacheEntry<OBJECT> get(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key)
    {
        return runtimeCache.get(key);
    }

    @Override
    public void put(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key, SearchCacheEntry<OBJECT> entry)
    {
        runtimeCache.put(key, entry);
    }

    @Override
    public void remove(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key)
    {
        runtimeCache.remove(key);
    }

    @PostConstruct
    public void initCache()
    {
        if (runtimeCache == null)
        {
            runtimeCache = new RuntimeCache<>(cacheManager, CACHE_NAME, CACHE_SIZE_PROPERTY_NAME);
        }
        runtimeCache.initCache();
    }
}
