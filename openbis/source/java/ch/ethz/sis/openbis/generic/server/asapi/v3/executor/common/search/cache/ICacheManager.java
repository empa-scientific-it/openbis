package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.util.Date;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

public interface ICacheManager
{

    ICache<Object> getCache(IOperationContext context);

    void clearCacheOfUser(String sessionToken);

    void clearOld(Date date);

    Map<String, ICache<Object>> getCacheByUserSessionToken();

    Class<?> getCacheClass();

    void setCacheClass(Class<?> cacheClass);

}
