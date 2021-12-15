package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICache;

public interface CacheFactory
{

    Class<?> getCacheClass();

    ICache<Object> getCache(final IOperationContext iOperationContext);

}
