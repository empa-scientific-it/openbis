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

package ch.systemsx.cisd.openbis.generic.shared.util;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
public class RuntimeCacheEventListener implements CacheEventListener
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Override
    public void dispose()
    {
        operationLog.info("Disposing cache listener");
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element)
    {
        logOperation(cache, element, "has been evicted from the cache");
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element)
    {
        logOperation(cache, element, "has expired");
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException
    {
        logOperation(cache, element, "has been put to the cache");
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException
    {
        logOperation(cache, element, "has been removed from the cache");
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException
    {
        logOperation(cache, element, "has been updated");
    }

    @Override
    public void notifyRemoveAll(Ehcache cache)
    {
        operationLog.info("All entries have been removed from the cache.");
    }

    private void logOperation(Ehcache cache, Element element, String operation)
    {
        Object entry = element.getObjectValue();

        if (entry != null)
        {
            if (operationLog.isInfoEnabled())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Cache entry " + entry.hashCode() + " that contains " + entry + " " + operation + ".");

                int cacheSize = cache.getSize();

                if (cacheSize == 1)
                {
                    sb.append(" Cache now contains 1 entry.");
                } else
                {
                    sb.append(" Cache now contains " + cacheSize + " entries.");
                }

                operationLog.info(sb.toString());
            }
        }
    }

    @Override
    public Object clone()
    {
        return new RuntimeCacheEventListener();
    }

}
