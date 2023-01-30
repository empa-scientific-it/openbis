/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes the way in which cache should be used when computing the result set.
 * 
 * @author Tomasz Pylak
 */
public class ResultSetFetchConfig<K> implements IsSerializable
{
    public enum ResultSetFetchMode implements IsSerializable
    {
        COMPUTE_AND_CACHE, CLEAR_COMPUTE_AND_CACHE, RECOMPUTE_AND_CACHE, FETCH_FROM_CACHE,
        FETCH_FROM_CACHE_AND_RECOMPUTE
    }

    private ResultSetFetchConfig.ResultSetFetchMode mode;

    private K resultSetKeyOrNull;

    /**
     * Instruction to compute a new result and to cache it. Should be used only if the result is computed for the first time and has not been cached
     * yet.
     */
    public static <K> ResultSetFetchConfig<K> createComputeAndCache()
    {
        return new ResultSetFetchConfig<K>(ResultSetFetchMode.COMPUTE_AND_CACHE, null);
    }

    /**
     * Instruction to clear the cache at the specified key, recompute the result and cache it again.
     */
    public static <K> ResultSetFetchConfig<K> createClearComputeAndCache(K resultSetKey)
    {
        return new ResultSetFetchConfig<K>(ResultSetFetchMode.CLEAR_COMPUTE_AND_CACHE, resultSetKey);
    }

    /** Instruction to fetch the result at the specified key in the cache. */
    public static <K> ResultSetFetchConfig<K> createFetchFromCache(K resultSetKey)
    {
        return new ResultSetFetchConfig<K>(ResultSetFetchMode.FETCH_FROM_CACHE, resultSetKey);
    }

    /** Instruction to recompute result and to cache it for the specified key. */
    public static <K> ResultSetFetchConfig<K> createRecomputeAndCache(K resultSetKey)
    {
        return new ResultSetFetchConfig<K>(ResultSetFetchMode.RECOMPUTE_AND_CACHE, resultSetKey);
    }

    /**
     * Instruction to fetch the result at the specified key in the cache an then recompute the custom columns and distinct filter values. Remember
     * that rows are filtered anyway, even without recomputing.
     */
    public static <K> ResultSetFetchConfig<K> createFetchFromCacheAndRecompute(K resultSetKey)
    {
        return new ResultSetFetchConfig<K>(ResultSetFetchMode.FETCH_FROM_CACHE_AND_RECOMPUTE,
                resultSetKey);
    }

    private ResultSetFetchConfig(ResultSetFetchConfig.ResultSetFetchMode mode, K resultSetKeyOrNull)
    {
        this.mode = mode;
        this.resultSetKeyOrNull = resultSetKeyOrNull;
    }

    private ResultSetFetchConfig()
    {
    }

    public ResultSetFetchConfig.ResultSetFetchMode getMode()
    {
        return mode;
    }

    /**
     * If mode is COMPUTE_AND_CACHE, returns null.<br>
     * If mode is FETCH_FROM_CACHE, returns a key which uniquely identifies a result set in the cache on the server side.<br>
     * If mode is CLEAR_CACHE_AND_RECOMPUTE, returns a key to the item in the cache which should be removed.<br>
     */
    public K tryGetResultSetKey()
    {
        return resultSetKeyOrNull;
    }

    @Override
    public String toString()
    {
        return "(mode = " + mode + ", resultSetKey = " + resultSetKeyOrNull + ")";
    }
}