/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.shared.util.RuntimeCache;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class TableDataCache<K, T>
{

    public static final String CACHE_NAME = "tableDataCache";

    public static final String CACHE_SIZE_PROPERTY_NAME = "ch.ethz.sis.openbis.generic.client.web.tabledatacache.size";

    @Autowired
    private CacheManager cacheManager;

    private RuntimeCache<K, T> runtimeCache;

    public TableDataCache()
    {
    }

    public TableDataCache(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    public T getTableData(K key)
    {
        return runtimeCache.get(key);
    }

    public void putTableData(K key, T table)
    {
        runtimeCache.put(key, table);
    }

    public boolean removeTableData(K key)
    {
        return runtimeCache.remove(key);
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
