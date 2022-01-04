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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICache;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class SearchCacheCleanupListener implements Session.ISessionCleaner
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final ICache<?> cache;

    private final String key;

    public SearchCacheCleanupListener(final ICache<?> cache, final String key)
    {
        this.cache = cache;
        this.key = key;
    }

    @Override
    public void cleanup()
    {
        if (cache.contains(key))
        {
            operationLog.info("Clean up cached search result on logout.");
            cache.remove(key);
        }
    }

}
