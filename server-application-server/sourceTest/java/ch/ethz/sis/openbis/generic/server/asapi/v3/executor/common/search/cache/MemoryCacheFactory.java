/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;

public final class MemoryCacheFactory implements CacheFactory
{

    final int cacheSize;

    public MemoryCacheFactory(final int cacheSize)
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
        return new MemoryCache<>(new CacheOptionsVO(cacheSize, null, null, false, TIME_PROVIDER));
    }

}
