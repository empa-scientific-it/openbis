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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe cache for whether a given session is authorized to access a given data set.
 * 
 * @author Bernd Rinn
 */
public class DatasetAuthorizationCache
{

    private final static class KeyRecord
    {
        private final String sessionToken;

        private final String datasetCode;

        KeyRecord(String sessionToken, String datasetCode)
        {
            this.sessionToken = sessionToken;
            this.datasetCode = datasetCode;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((datasetCode == null) ? 0 : datasetCode.hashCode());
            result = prime * result + ((sessionToken == null) ? 0 : sessionToken.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            KeyRecord other = (KeyRecord) obj;
            if (datasetCode == null)
            {
                if (other.datasetCode != null)
                    return false;
            } else if (!datasetCode.equals(other.datasetCode))
                return false;
            if (sessionToken == null)
            {
                if (other.sessionToken != null)
                    return false;
            } else if (!sessionToken.equals(other.sessionToken))
                return false;
            return true;
        }
    }

    private final static class ValueRecord
    {
        private final boolean authorized;

        private final long timestamp;

        ValueRecord(boolean authorized)
        {
            this.authorized = authorized;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isAuthorized()
        {
            return authorized;
        }

        long getTimestamp()
        {
            return timestamp;
        }
    }

    private final Map<KeyRecord, ValueRecord> cache =
            new ConcurrentHashMap<KeyRecord, ValueRecord>();

    private Timer expirationTimer = new Timer(true);

    private final long cacheExpirationTimeMillis;

    /**
     * Create a new and empty cache and start an expiration timer for cleaning up.
     * <p>
     * <i>Note that the cache will never return stale authorization information. The expiration timer is only needed for freeing memory occupied by
     * the cache.</i>
     * 
     * @param cacheExpirationTimeMillis The time (in ms) after which an authorization value expires.
     * @param timerPeriodMillis The time (in ms) after which the expiration timer runs.
     */
    public DatasetAuthorizationCache(long cacheExpirationTimeMillis, long timerPeriodMillis)
    {
        this.cacheExpirationTimeMillis = cacheExpirationTimeMillis;
        this.expirationTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runExpiration();
                }
            }, 0L, timerPeriodMillis);
    }

    private boolean isExpired(ValueRecord v, long now)
    {
        return now - v.getTimestamp() > cacheExpirationTimeMillis;
    }

    private void runExpiration()
    {
        final long now = System.currentTimeMillis();
        final Iterator<Map.Entry<KeyRecord, ValueRecord>> it = cache.entrySet().iterator();
        while (it.hasNext())
        {
            final Map.Entry<KeyRecord, ValueRecord> e = it.next();
            if (isExpired(e.getValue(), now))
            {
                it.remove();
            }
        }
    }

    private ValueRecord tryGet(KeyRecord k)
    {
        final ValueRecord v = cache.get(k);
        if (v != null && isExpired(v, System.currentTimeMillis()))
        {
            cache.remove(k);
            return null;
        } else
        {
            return v;
        }
    }

    /**
     * Puts a new authorization value in the cache.
     * 
     * @param sessionToken The token of the session that is granted or denied access.
     * @param datasetCode The code of the data set that the session is granted or denied access to.
     * @param authorized Whether the session should be granted (<code>true</code>) or denied ( <code>false</code>) access to the data set.
     */
    public void put(String sessionToken, String datasetCode, boolean authorized)
    {
        cache.put(new KeyRecord(sessionToken, datasetCode), new ValueRecord(authorized));
    }

    /**
     * Puts new authorization values in the cache.
     * 
     * @param sessionToken The token of the session that is granted or denied access.
     * @param datasetCodes The codes of the data sets that the session is granted or denied access to.
     * @param authorized Whether the session should be granted (<code>true</code>) or denied ( <code>false</code>) access to the data seta.
     */
    public void putAll(String sessionToken, List<String> datasetCodes, boolean authorized)
    {
        final ValueRecord value = new ValueRecord(authorized);
        for (String datasetCode : datasetCodes)
        {
            cache.put(new KeyRecord(sessionToken, datasetCode), value);
        }
    }

    /**
     * Returns authorization state of the <var>datasetCode</var> for the session specified by <var>sessionToken</var>.
     * 
     * @param sessionToken The token of the session that the authorization information is for.
     * @param datasetCode The code of the data set that the session wants to access.
     * @return <code>true</code> if the session is granted access, <code>false</code> if the session is denied access and <code>null</code> if the
     *         cache has no information about this session and data set.
     */
    public Boolean tryGet(String sessionToken, String datasetCode)
    {
        final ValueRecord v = tryGet(new KeyRecord(sessionToken, datasetCode));
        return v != null ? v.isAuthorized() : null;
    }

    /**
     * Removes all entries from this cache.
     */
    public void clear()
    {
        cache.clear();
    }

}
