package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.MemoryCache;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;

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
        return new MemoryCache<>(new CacheOptionsVO(cacheSize, null, null, false, new MockTimeProvider()));
    }

}
