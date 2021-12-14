package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CacheOptionsVO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

@Component("cache-manager")
public class CacheManager implements ICacheManager
{

    private final Map<String, ICache<Object>> cacheByUserSessionToken = new ConcurrentHashMap<>();

    private Properties serviceProperties;

    private int cacheCapacity = OperationExecutionConfig.CACHE_CAPACITY_DEFAULT;

    private Class<?> cacheClass;

    @Override
    public ICache<Object> getCache(final IOperationContext context)
    {
        final String sessionToken = context.getSession().getSessionToken();
        ICache<Object> cache = cacheByUserSessionToken.get(sessionToken);
        if (cache == null)
        {
            try
            {
                cache = (ICache<Object>) cacheClass.getConstructor(CacheOptionsVO.class).newInstance(
                        new CacheOptionsVO(cacheCapacity, serviceProperties, sessionToken, true));
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e)
            {
                throw new RuntimeException("Error creating cache instance.", e);
            }

            cacheByUserSessionToken.put(sessionToken, cache);
        }
        return cache;
    }

    @Override
    public void clearCacheOfUser(final String sessionToken)
    {
        final ICache<Object> cache = cacheByUserSessionToken.get(sessionToken);
        if (cache != null)
        {
            cache.clear();
        }

        cacheByUserSessionToken.remove(sessionToken);
    }

    @Override
    public void clearOld(final Date date)
    {
        synchronized (cacheByUserSessionToken)
        {
            cacheByUserSessionToken.values().forEach(cache -> cache.clearOld(date));
        }
    }

    @Override
    public Map<String, ICache<Object>> getCacheByUserSessionToken()
    {
        return cacheByUserSessionToken;
    }

    @Override
    public Class<?> getCacheClass()
    {
        return cacheClass;
    }

    @Override
    public void setCacheClass(final Class<?> cacheClass)
    {
        this.cacheClass = cacheClass;
    }

    @SuppressWarnings("unchecked")
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private void setServicePropertiesPlaceholder(ExposablePropertyPlaceholderConfigurer servicePropertiesPlaceholder)
            throws ClassNotFoundException
    {
        serviceProperties = servicePropertiesPlaceholder.getResolvedProps();

        cacheCapacity = PropertyUtils.getInt(serviceProperties, OperationExecutionConfig.CACHE_CAPACITY,
                OperationExecutionConfig.CACHE_CAPACITY_DEFAULT);

        final String cacheImplementationClassName =
                PropertyUtils.getProperty(serviceProperties, OperationExecutionConfig.CACHE_CLASS);
        setCacheClass(cacheImplementationClassName != null ? Class.forName(cacheImplementationClassName) : null);
    }

}
