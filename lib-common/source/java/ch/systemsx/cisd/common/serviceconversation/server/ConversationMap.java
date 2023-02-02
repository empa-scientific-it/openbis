/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A set of recently seen service conversation ids.
 * 
 * @author Bernd Rinn
 */
class ConversationMap
{
    private final int MAX_SIZE_INSPECT = 100;

    private final long MAX_AGE_MILLIS = 60000L;

    private final Map<String, ServiceConversationRecord> conversations =
            new ConcurrentHashMap<String, ServiceConversationRecord>();

    private final Map<String, Long> recentlySeenMap = new ConcurrentHashMap<String, Long>();

    private void addHistoric(String serviceConversationId)
    {
        final long now = System.currentTimeMillis();
        recentlySeenMap.put(serviceConversationId, now);
        cleanUpOld(now);
    }

    private void cleanUpOld(final long now)
    {
        if (recentlySeenMap.size() > MAX_SIZE_INSPECT)
        {
            final Iterator<Entry<String, Long>> it = recentlySeenMap.entrySet().iterator();
            while (it.hasNext())
            {
                final Entry<String, Long> entry = it.next();
                if (now - entry.getValue() > MAX_AGE_MILLIS)
                {
                    it.remove();
                }
            }
        }
    }

    boolean recentlySeen(String serviceConversationId)
    {
        return recentlySeenMap.containsKey(serviceConversationId);
    }

    ServiceConversationRecord get(Object key)
    {
        return conversations.get(key);
    }

    ServiceConversationRecord put(String key, ServiceConversationRecord value)
    {
        return conversations.put(key, value);
    }

    ServiceConversationRecord remove(String serviceConversationId)
    {
        addHistoric(serviceConversationId);
        return conversations.remove(serviceConversationId);
    }

    boolean containsKey(Object key)
    {
        return conversations.containsKey(key);
    }

    Collection<ServiceConversationRecord> values()
    {
        return conversations.values();
    }

}
