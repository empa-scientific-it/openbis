package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;

public interface CacheFactory
{

    ITimeProvider TIME_PROVIDER = new MockTimeProvider();

    Class<?> getCacheClass();

    ICache<Object> getCache(final IOperationContext iOperationContext);

}
