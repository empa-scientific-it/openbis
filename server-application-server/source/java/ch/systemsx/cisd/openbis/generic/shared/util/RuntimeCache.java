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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration.MaxDepthExceededBehavior;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.parser.MemorySizeFormatter;
import ch.systemsx.cisd.common.parser.PercentFormatter;

/**
 * @author Franz-Josef Elmer
 */
public class RuntimeCache<K, V>
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RuntimeCache.class);

    private CacheManager cacheManager;

    private String name;

    private String cacheSizePropertyName;

    public RuntimeCache(CacheManager cacheManager, String name, String cacheSizePropertyName)
    {
        this.cacheManager = cacheManager;
        this.name = name;
        this.cacheSizePropertyName = cacheSizePropertyName;
    }

    @SuppressWarnings("unchecked")
    public V get(K key)
    {
        Element element = getCache().get(key);
        return element == null ? null : ((ValueWrapper<V>) element.getObjectValue()).getValue();
    }

    public void put(K key, V value)
    {
        Element element = getCache().get(key);

        if (element == null)
        {
            getCache().put(new Element(key, new ValueWrapper<V>(value)));
        } else
        {
            // do not call put() when updating an existing value as the eviction won't work properly
            @SuppressWarnings("unchecked")
            ValueWrapper<V> existingValue = (ValueWrapper<V>) element.getObjectValue();
            existingValue.setValue(value);
            // manually log update message because the cache does not log anything during recalculateSize() call
            RuntimeCacheEventListenerFactory.getListener().notifyElementUpdated(getCache(), element);
            getCache().recalculateSize(key);
        }
    }

    public boolean remove(K key)
    {
        return getCache().remove(key);
    }

    @SuppressWarnings("unchecked")
    public List<K> getKeys()
    {
        return getCache().getKeys();
    }

    public List<V> getValues()
    {
        List<V> values = new ArrayList<V>(getKeys().size());

        for (K key : getKeys())
        {
            values.add(get(key));
        }

        return values;
    }

    public void initCache()
    {
        Cache cache = cacheManager.getCache(name);

        if (cache == null)
        {
            operationLog.info("Creating the cache: " + name);

            CacheConfiguration config = new CacheConfiguration();
            config.setName(name);
            config.setEternal(false);
            config.maxBytesLocalHeap(getCacheSize(), MemoryUnit.BYTES);
            config.persistence(new PersistenceConfiguration().strategy(Strategy.NONE));
            config.setTimeToIdleSeconds(3600);
            config.setTimeToLiveSeconds(0);
            config.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU);
            config.addCacheEventListenerFactory(new CacheEventListenerFactoryConfiguration().className(RuntimeCacheEventListenerFactory.class
                    .getName()));
            config.sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(10000000).maxDepthExceededBehavior(MaxDepthExceededBehavior.CONTINUE));

            cache = new Cache(config);
            cacheManager.addCache(cache);
        } else
        {
            operationLog.info("The cache " + name + " already exists. It must have been configured in ehcache.xml file.");
        }
    }

    protected long getCacheSize()
    {
        String propertyValue = getSystemProperty(cacheSizePropertyName);

        if (propertyValue == null || propertyValue.trim().length() == 0)
        {
            return getCacheDefaultSize();
        } else
        {
            try
            {
                long cacheSize = MemorySizeFormatter.parse(propertyValue);
                operationLog.info("Cache size was set to '" + propertyValue + "' in '" + cacheSizePropertyName + "' system property.");
                return cacheSize;
            } catch (IllegalArgumentException e1)
            {
                try
                {
                    int cachePercent = PercentFormatter.parse(propertyValue);
                    long cacheSize = (long) ((cachePercent / 100.0) * getMemorySize());
                    operationLog.info("Cache size was set to '" + propertyValue + "' in '" + cacheSizePropertyName
                            + "' system property. The memory available to the JVM is " + MemorySizeFormatter.format(getMemorySize())
                            + " which gives a cache size of " + MemorySizeFormatter.format(cacheSize));
                    return cacheSize;
                } catch (IllegalArgumentException e2)
                {
                    throw new IllegalArgumentException("Cache size was set to '" + propertyValue + "' in '" + cacheSizePropertyName
                            + "' system property. This value is incorrect. Please set the property to an absolute value like '512m' or '1g'."
                            + " You can also use a value like '25%' to set the cache size relative to the memory available to the JVM.");
                }
            }
        }
    }

    protected long getMemorySize()
    {
        return Runtime.getRuntime().maxMemory();
    }

    protected String getSystemProperty(String propertyName)
    {
        return System.getProperty(propertyName);
    }

    private Cache getCache()
    {
        return cacheManager.getCache(name);
    }

    private long getCacheDefaultSize()
    {
        long memorySize = getMemorySize();
        long cacheSize = memorySize / 4;
        operationLog.info("Cache size has been set to its default value. The default value is 25% (" + MemorySizeFormatter.format(cacheSize) + ")"
                + " of the memory available to the JVM (" + MemorySizeFormatter.format(memorySize) + ")."
                + " If you would like to change this value, then please set '"
                + cacheSizePropertyName + "' system property in openbis.conf file.");
        return cacheSize;
    }

    private static final class ValueWrapper<V>
    {
        private V value;

        ValueWrapper(V value)
        {
            this.value = value;
        }

        public V getValue()
        {
            return value;
        }

        public void setValue(V value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return String.valueOf(value);
        }

    }
}
