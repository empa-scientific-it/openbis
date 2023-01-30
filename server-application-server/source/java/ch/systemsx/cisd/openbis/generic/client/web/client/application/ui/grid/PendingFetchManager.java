/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;

/**
 * A helper class for keeping track of pending fetches.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class PendingFetchManager
{
    // The current pending fetch, or null if there is none.
    private ResultSetFetchConfig<String> pendingFetchConfigOrNull = null;

    public PendingFetchManager()
    {

    }

    /**
     * Pushes a fetchConfig to the stack of pending fetches, though we currently only keep track of one pending fetch.
     */
    public void pushPendingFetchConfig(ResultSetFetchConfig<String> fetchConfig)
    {
        pendingFetchConfigOrNull = fetchConfig;
    }

    /**
     * Returns a pending fetch config. If {@link #hasPendingFetch()} is true, the return value is non-null. Otherwise, it is null.
     */
    public ResultSetFetchConfig<String> tryTopPendingFetchConfig()
    {
        return pendingFetchConfigOrNull;
    }

    /**
     * Return true if a fetch is pending.
     */
    public boolean hasPendingFetch()
    {
        return null != pendingFetchConfigOrNull;
    }

    /**
     * Return true if a fetch is pending.
     */
    public boolean hasNoPendingFetch()
    {
        return false == hasPendingFetch();
    }

    /**
     * Notes that a pending fetch has completed.
     */
    public void popPendingFetch()
    {
        // We only keep track of 1 pending fetch, so just clear it.
        pendingFetchConfigOrNull = null;
    }
}
