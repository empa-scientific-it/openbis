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
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

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
                cache = (ICache<Object>) cacheClass.getConstructor(CacheOptionsVO.class)
                        .newInstance(new CacheOptionsVO(cacheCapacity, serviceProperties, sessionToken, true,
                                SystemTimeProvider.SYSTEM_TIME_PROVIDER));
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
            cacheByUserSessionToken.values().forEach(cache -> cache.clearOld(date.getTime()));
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
