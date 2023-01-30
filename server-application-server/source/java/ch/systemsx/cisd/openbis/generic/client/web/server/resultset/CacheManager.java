/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.security.TokenGenerator;

/**
 * Caches objects of type T. Uses key types K.
 * 
 * @author Tomasz Pylak
 */
public class CacheManager<K, T>
{
    private final IResultSetKeyGenerator<K> keyProvider;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CacheManager.class);

    private final Map<K, T> results = new HashMap<K, T>();

    public CacheManager(final IResultSetKeyGenerator<K> resultSetKeyProvider)
    {
        this.keyProvider = resultSetKeyProvider;
    }

    /** @return manager which assumes that keys are Strings and is able to generate them */
    public static <T> CacheManager<String, T> createCacheManager()
    {
        return new CacheManager<String, T>(new TokenBasedResultSetKeyGenerator());
    }

    public final synchronized T tryGetData(K dataKey)
    {
        assert dataKey != null : "data key unsipecified";
        return results.get(dataKey);
    }

    /**
     * saves data in the cache.
     * 
     * @return key at which data was cached
     */
    public final synchronized K saveData(T data)
    {
        K dataKey = keyProvider.createKey();
        results.put(dataKey, data);
        return dataKey;
    }

    public final synchronized void removeData(final K dataKey)
    {
        assert dataKey != null : "Unspecified data key holder.";
        if (results.remove(dataKey) != null)
        {
            operationLog.debug(String.format("Result set for key '%s' has been removed.", dataKey));
        } else
        {
            operationLog
                    .debug(String.format("No result set for key '%s' could be found.", dataKey));
        }
    }

    public static final class TokenBasedResultSetKeyGenerator implements
            IResultSetKeyGenerator<String>
    {

        private static final long serialVersionUID = 1L;

        private final TokenGenerator tokenGenerator;

        public TokenBasedResultSetKeyGenerator()
        {
            this.tokenGenerator = new TokenGenerator();
        }

        //
        // IResultSetKeyProvider
        //

        @Override
        public final String createKey()
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis());
        }
    }
}
